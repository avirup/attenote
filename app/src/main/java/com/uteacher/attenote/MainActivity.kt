package com.uteacher.attenote

import android.app.ActionBar
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
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
import com.uteacher.attenote.data.repository.BackupSupportRepository
import com.uteacher.attenote.data.repository.SettingsPreferencesRepository
import com.uteacher.attenote.ui.navigation.AppNavHost
import com.uteacher.attenote.ui.navigation.AppRoute
import com.uteacher.attenote.ui.navigation.ActionBarPrimaryAction
import com.uteacher.attenote.ui.theme.AttenoteTheme
import com.uteacher.attenote.util.BiometricHelper
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
    private var lastBrandedHeaderState: Boolean? = null
    private var actionBarPrimaryAction: ActionBarPrimaryAction? = null
    private var actionBarBrandView: LinearLayout? = null
    private var actionBarBrandWordmarkView: ImageView? = null
    private var actionBarBrandTitleView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = BRAND_PRIMARY_COLOR_INT
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
        if (item.itemId == MENU_ITEM_SECONDARY_ACTION) {
            actionBarPrimaryAction?.secondaryAction?.onClick?.invoke()
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
            configureActionMenuItem(
                menu = menu,
                itemId = MENU_ITEM_PRIMARY_ACTION,
                title = action.title,
                contentDescription = action.contentDescription,
                enabled = action.enabled,
                iconResId = action.iconResId,
                iconSizeDp = action.iconSizeDp,
                endPaddingDp = action.endPaddingDp,
                onClick = action.onClick
            )
            action.secondaryAction?.let { secondary ->
                configureActionMenuItem(
                    menu = menu,
                    itemId = MENU_ITEM_SECONDARY_ACTION,
                    title = secondary.title,
                    contentDescription = secondary.contentDescription,
                    enabled = secondary.enabled,
                    iconResId = secondary.iconResId,
                    iconSizeDp = secondary.iconSizeDp,
                    endPaddingDp = secondary.endPaddingDp,
                    onClick = secondary.onClick
                )
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
        ensureActionBarBrandView(bar)
        val shouldShowBack = showBack && SHOW_ACTION_BAR_BACK_BUTTON

        val isBrandedHeader = title == DASHBOARD_ACTION_BAR_TITLE || title == SPLASH_ACTION_BAR_TITLE
        if (lastBrandedHeaderState != isBrandedHeader) {
            actionBarBrandWordmarkView?.visibility = if (isBrandedHeader) View.VISIBLE else View.GONE
            actionBarBrandTitleView?.visibility = if (isBrandedHeader) View.GONE else View.VISIBLE
            lastBrandedHeaderState = isBrandedHeader
        }

        if (!isBrandedHeader && lastActionBarTitle != title) {
            actionBarBrandTitleView?.text = title
            lastActionBarTitle = title
        }
        if (lastActionBarBackState != shouldShowBack) {
            bar.setDisplayHomeAsUpEnabled(shouldShowBack)
            bar.setDisplayShowHomeEnabled(false)
            lastActionBarBackState = shouldShowBack
        }
    }

    private fun ensureActionBarBranding(bar: ActionBar) {
        bar.setBackgroundDrawable(ColorDrawable(BRAND_PRIMARY_COLOR_INT))
        bar.setDisplayUseLogoEnabled(false)
        bar.setDisplayShowTitleEnabled(false)
        bar.setDisplayShowCustomEnabled(true)
    }

    private fun ensureActionBarBrandView(bar: ActionBar) {
        if (actionBarBrandView != null) {
            if (bar.customView !== actionBarBrandView) {
                bar.customView = actionBarBrandView
            }
            return
        }
        val iconSizePx = dpToPx(19f)
        val spacingPx = dpToPx(4f)
        val wordmarkHeightPx = dpToPx(14f)

        val iconView = ImageView(this).apply {
            setImageResource(R.drawable.attenote_title_icon)
            scaleType = ImageView.ScaleType.FIT_CENTER
            layoutParams = LinearLayout.LayoutParams(iconSizePx, iconSizePx)
        }

        val wordmarkView = ImageView(this).apply {
            setImageResource(R.drawable.attenote_wordmark_title)
            contentDescription = "attenote"
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_START
            visibility = View.GONE
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                wordmarkHeightPx
            ).apply {
                marginStart = spacingPx
            }
        }

        val titleView = TextView(this).apply {
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            maxLines = 1
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = spacingPx
            }
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dpToPx(2f), 0, dpToPx(2f), 0)
            addView(iconView)
            addView(wordmarkView)
            addView(titleView)
            layoutParams = ActionBar.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.START or Gravity.CENTER_VERTICAL
            )
        }

        actionBarBrandView = container
        actionBarBrandWordmarkView = wordmarkView
        actionBarBrandTitleView = titleView
        bar.customView = container
    }

    private fun dpToPx(value: Float): Int {
        return (value * resources.displayMetrics.density).roundToInt()
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
        val previousSecondary = previous?.secondaryAction
        val nextSecondary = action?.secondaryAction
        if (
            previous?.title == action?.title &&
            previous?.iconResId == action?.iconResId &&
            previous?.contentDescription == action?.contentDescription &&
            previous?.iconSizeDp == action?.iconSizeDp &&
            previous?.endPaddingDp == action?.endPaddingDp &&
            previous?.enabled == action?.enabled &&
            previous?.onClick === action?.onClick &&
            previousSecondary?.title == nextSecondary?.title &&
            previousSecondary?.iconResId == nextSecondary?.iconResId &&
            previousSecondary?.contentDescription == nextSecondary?.contentDescription &&
            previousSecondary?.iconSizeDp == nextSecondary?.iconSizeDp &&
            previousSecondary?.endPaddingDp == nextSecondary?.endPaddingDp &&
            previousSecondary?.enabled == nextSecondary?.enabled &&
            previousSecondary?.onClick === nextSecondary?.onClick
        ) {
            return
        }
        actionBarPrimaryAction = action
        invalidateOptionsMenu()
    }

    private fun configureActionMenuItem(
        menu: Menu,
        itemId: Int,
        title: String,
        contentDescription: String,
        enabled: Boolean,
        iconResId: Int?,
        iconSizeDp: Float?,
        endPaddingDp: Float,
        onClick: () -> Unit
    ) {
        val item = menu.add(Menu.NONE, itemId, Menu.NONE, title)
        item.contentDescription = contentDescription
        item.tooltipText = contentDescription
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        item.isEnabled = enabled

        val resolvedIconRes = iconResId ?: return
        val iconSizePx = dpToPx(iconSizeDp ?: 16f)
        val actionEndPaddingPx = dpToPx(endPaddingDp)
        val actionIcon = AppCompatResources.getDrawable(this, resolvedIconRes)?.mutate()?.apply {
            setBounds(0, 0, iconSizePx, iconSizePx)
        }
        val iconView = ImageView(this).apply {
            setImageDrawable(actionIcon)
            this.contentDescription = contentDescription
            isEnabled = enabled
            alpha = if (enabled) 1f else 0.45f
            setPadding(0, 0, actionEndPaddingPx, 0)
            minimumWidth = 0
            minimumHeight = 0
            layoutParams = ViewGroup.LayoutParams(
                dpToPx(20f) + actionEndPaddingPx,
                dpToPx(20f)
            )
            setOnClickListener {
                if (enabled) {
                    onClick.invoke()
                }
            }
        }
        item.actionView = iconView
    }

    private companion object {
        private const val STARTUP_TAG = "StartupGate"
        private const val MENU_ITEM_PRIMARY_ACTION = 1001
        private const val MENU_ITEM_SECONDARY_ACTION = 1002
        private const val DASHBOARD_ACTION_BAR_TITLE = "Dashboard"
        private const val SPLASH_ACTION_BAR_TITLE = "Splash"
        private const val BRAND_PRIMARY_COLOR_INT = -10793744 // #5B4CF0
        private const val SHOW_ACTION_BAR_BACK_BUTTON = false
    }
}
