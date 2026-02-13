package com.uteacher.attendancetracker

import android.app.ActionBar
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.ImageView
import androidx.biometric.BiometricPrompt
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.DialogNavigator
import com.uteacher.attendancetracker.data.repository.BackupSupportRepository
import com.uteacher.attendancetracker.data.repository.SettingsPreferencesRepository
import com.uteacher.attendancetracker.ui.navigation.AppNavHost
import com.uteacher.attendancetracker.ui.navigation.AppRoute
import com.uteacher.attendancetracker.ui.navigation.ActionBarPrimaryAction
import com.uteacher.attendancetracker.ui.theme.AttenoteTheme
import com.uteacher.attendancetracker.util.BiometricHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import kotlin.math.roundToInt

class MainActivity : FragmentActivity() {

    private val settingsRepo: SettingsPreferencesRepository by inject()
    private val backupRepo: BackupSupportRepository by inject()
    private val biometricHelper: BiometricHelper by inject()

    private var showContent by mutableStateOf(false)
    private var startDestination: AppRoute? by mutableStateOf(null)

    private var navigateUpHandler: (() -> Boolean)? = null
    private var lastActionBarTitle: String? = null
    private var lastActionBarBackState: Boolean? = null
    private var lastDashboardBrandingState: Boolean? = null
    private var actionBarPrimaryAction: ActionBarPrimaryAction? = null
    private var dashboardWordmarkView: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        applyActionBarState(title = "Splash", showBack = false)

        lifecycleScope.launch {
            backupRepo.checkAndRecoverInterruptedRestore()
            val isSetupComplete = settingsRepo.isSetupComplete.first()
            val biometricEnabled = settingsRepo.biometricEnabled.first()
            Log.d(
                STARTUP_TAG,
                "Startup prefs: isSetupComplete=$isSetupComplete biometricEnabled=$biometricEnabled"
            )

            if (biometricEnabled && !biometricHelper.isDeviceSecure()) {
                showLockRemovedDialog()
                settingsRepo.setBiometricEnabled(false)
                startDestination = AppRoute.Dashboard
                showContent = true
                return@launch
            }

            startDestination = when {
                !isSetupComplete -> AppRoute.Splash
                biometricEnabled -> null
                else -> AppRoute.Dashboard
            }

            if (biometricEnabled) {
                showBiometricPrompt(
                    onSuccess = {
                        startDestination = AppRoute.Dashboard
                        showContent = true
                    },
                    onFailure = {
                        startDestination = AppRoute.AuthGate
                        showContent = true
                    }
                )
            } else {
                showContent = true
            }
        }

        setContent {
            AttenoteTheme {
                if (showContent && startDestination != null) {
                    val context = LocalContext.current
                    val navController = remember(startDestination) {
                        NavHostController(context).apply {
                            navigatorProvider.addNavigator(ComposeNavigator())
                            navigatorProvider.addNavigator(DialogNavigator())
                        }
                    }
                    DisposableEffect(navController) {
                        navigateUpHandler = { navController.popBackStack() }
                        onDispose { navigateUpHandler = null }
                    }
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppNavHost(
                            navController = navController,
                            startDestination = startDestination!!,
                            onActionBarChanged = { title, showBack ->
                                applyActionBarState(title = title, showBack = showBack)
                            },
                            onActionBarPrimaryActionChanged = { action ->
                                applyActionBarPrimaryAction(action)
                            }
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            val consumed = navigateUpHandler?.invoke() ?: false
            return consumed || super.onOptionsItemSelected(item)
        }
        if (item.itemId == MENU_ITEM_PRIMARY_ACTION) {
            actionBarPrimaryAction?.onClick?.invoke()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.clear()
        val action = actionBarPrimaryAction
        if (action != null) {
            menu.add(Menu.NONE, MENU_ITEM_PRIMARY_ACTION, Menu.NONE, action.title).apply {
                setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                isEnabled = action.enabled
            }
        }
        return super.onPrepareOptionsMenu(menu)
    }

    private fun showBiometricPrompt(
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onFailure()
                }
            }
        )
        biometricPrompt.authenticate(
            biometricHelper.createPromptInfo(
                title = "Unlock attenote",
                description = "Authenticate to access your data"
            )
        )
    }

    private fun showLockRemovedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Biometric Lock Disabled")
            .setMessage(
                "Your device lock screen was removed. Biometric lock has been disabled."
            )
            .setPositiveButton("OK", null)
            .show()
    }

    private fun applyActionBarState(title: String, showBack: Boolean) {
        val bar = actionBar ?: return
        ensureActionBarBranding(bar)

        val isDashboard = title == DASHBOARD_ACTION_BAR_TITLE
        if (lastDashboardBrandingState != isDashboard) {
            bar.setDisplayShowCustomEnabled(isDashboard)
            bar.setDisplayShowTitleEnabled(!isDashboard)
            if (isDashboard) {
                bar.customView = dashboardWordmarkView ?: createDashboardWordmarkView().also {
                    dashboardWordmarkView = it
                }
            }
            lastDashboardBrandingState = isDashboard
        }

        if (!isDashboard && lastActionBarTitle != title) {
            bar.title = title
            lastActionBarTitle = title
        }
        if (lastActionBarBackState != showBack) {
            bar.setDisplayHomeAsUpEnabled(showBack)
            lastActionBarBackState = showBack
        }
    }

    private fun ensureActionBarBranding(bar: ActionBar) {
        bar.setLogo(R.drawable.attenote_title_icon)
        bar.setDisplayUseLogoEnabled(true)
        bar.setDisplayShowHomeEnabled(true)
    }

    private fun createDashboardWordmarkView(): ImageView {
        val wordmarkHeightPx = (20f * resources.displayMetrics.density).roundToInt()
        return ImageView(this).apply {
            setImageResource(R.drawable.attenote_wordmark_title)
            contentDescription = "attenote"
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_START
            layoutParams = ActionBar.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                wordmarkHeightPx,
                Gravity.START or Gravity.CENTER_VERTICAL
            )
        }
    }

    private fun applyActionBarPrimaryAction(action: ActionBarPrimaryAction?) {
        val previous = actionBarPrimaryAction
        if (
            previous?.title == action?.title &&
            previous?.enabled == action?.enabled &&
            previous?.onClick === action?.onClick
        ) {
            return
        }
        actionBarPrimaryAction = action
        invalidateOptionsMenu()
    }

    private companion object {
        private const val STARTUP_TAG = "StartupGate"
        private const val MENU_ITEM_PRIMARY_ACTION = 1001
        private const val DASHBOARD_ACTION_BAR_TITLE = "Dashboard"
    }
}
