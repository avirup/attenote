package com.uteacher.attendancetracker

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.uteacher.attendancetracker.ui.navigation.AppNavHost
import com.uteacher.attendancetracker.ui.navigation.AppRoute
import com.uteacher.attendancetracker.ui.theme.AttenoteTheme

class MainActivity : ComponentActivity() {
    private var navigateUpHandler: (() -> Boolean)? = null
    private var lastActionBarTitle: String? = null
    private var lastActionBarBackState: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        applyActionBarState(title = "Splash", showBack = false)

        setContent {
            AttenoteTheme {
                val navController = rememberNavController()
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
                        startDestination = AppRoute.Splash,
                        onActionBarChanged = { title, showBack ->
                            applyActionBarState(title = title, showBack = showBack)
                        }
                    )
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            val consumed = navigateUpHandler?.invoke() ?: false
            return consumed || super.onOptionsItemSelected(item)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun applyActionBarState(title: String, showBack: Boolean) {
        val bar = actionBar ?: return
        if (lastActionBarTitle != title) {
            bar.title = title
            lastActionBarTitle = title
        }
        if (lastActionBarBackState != showBack) {
            bar.setDisplayHomeAsUpEnabled(showBack)
            bar.setDisplayShowHomeEnabled(showBack)
            lastActionBarBackState = showBack
        }
    }
}
