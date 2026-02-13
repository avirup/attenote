package com.uteacher.attendancetracker.ui.navigation

import android.content.Context
import android.util.TypedValue
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.uteacher.attendancetracker.ui.screen.auth.AuthGateScreen
import com.uteacher.attendancetracker.ui.screen.createclass.CreateClassScreen
import com.uteacher.attendancetracker.ui.screen.dashboard.DashboardScreen
import com.uteacher.attendancetracker.ui.screen.manageclass.EditClassScreen
import com.uteacher.attendancetracker.ui.screen.manageclass.ManageClassListScreen
import com.uteacher.attendancetracker.ui.screen.managestudents.ManageStudentsScreen
import com.uteacher.attendancetracker.ui.screen.setup.SetupScreen
import com.uteacher.attendancetracker.ui.screen.splash.SplashScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: AppRoute,
    onActionBarChanged: (title: String, showBack: Boolean) -> Unit,
    onActionBarPrimaryActionChanged: (ActionBarPrimaryAction?) -> Unit
) {
    val context = LocalContext.current
    val topChromePadding = remember(context) {
        context.pxToDp(context.resolveStatusBarHeightPx() + context.resolveActionBarHeightPx())
    }
    val bottomChromePadding = remember(context) {
        context.pxToDp(context.resolveNavigationBarHeightPx())
    }
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier
            .fillMaxSize()
            .padding(top = topChromePadding, bottom = bottomChromePadding)
    ) {
        composable<AppRoute.Splash> {
            ConfigureActionBar(
                route = AppRoute.Splash,
                onActionBarChanged = onActionBarChanged,
                onActionBarPrimaryActionChanged = onActionBarPrimaryActionChanged
            )
            SplashScreen(
                onNavigateToSetup = {
                    navController.navigate(AppRoute.Setup) {
                        popUpTo<AppRoute.Splash> {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable<AppRoute.Setup> {
            ConfigureActionBar(
                route = AppRoute.Setup,
                onActionBarChanged = onActionBarChanged,
                onActionBarPrimaryActionChanged = onActionBarPrimaryActionChanged
            )
            SetupScreen(
                onNavigateToDashboard = {
                    navController.navigate(AppRoute.Dashboard) {
                        popUpTo<AppRoute.Setup> {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable<AppRoute.AuthGate> {
            ConfigureActionBar(
                route = AppRoute.AuthGate,
                onActionBarChanged = onActionBarChanged,
                onActionBarPrimaryActionChanged = onActionBarPrimaryActionChanged
            )
            AuthGateScreen(
                onNavigateToDashboard = {
                    navController.navigate(AppRoute.Dashboard) {
                        popUpTo<AppRoute.AuthGate> {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable<AppRoute.Dashboard> {
            ConfigureActionBar(
                route = AppRoute.Dashboard,
                onActionBarChanged = onActionBarChanged,
                onActionBarPrimaryActionChanged = onActionBarPrimaryActionChanged
            )
            DashboardScreen(
                onNavigateToCreateClass = {
                    navController.navigate(AppRoute.CreateClass)
                },
                onNavigateToManageClassList = {
                    navController.navigate(AppRoute.ManageClassList)
                },
                onNavigateToManageStudents = {
                    navController.navigate(AppRoute.ManageStudents)
                },
                onNavigateToSettings = {
                    navController.navigate(AppRoute.Settings)
                },
                onNavigateToTakeAttendance = { classId, scheduleId, date ->
                    navController.navigate(AppRoute.TakeAttendance(classId, scheduleId, date))
                },
                onNavigateToAddNote = { date, noteId ->
                    navController.navigate(AppRoute.AddNote(date = date, noteId = noteId))
                }
            )
        }

        composable<AppRoute.CreateClass> {
            ConfigureActionBar(
                route = AppRoute.CreateClass,
                onActionBarChanged = onActionBarChanged,
                onActionBarPrimaryActionChanged = onActionBarPrimaryActionChanged
            )
            CreateClassScreen(
                onNavigateBack = { navController.popBackStack() },
                onSetActionBarPrimaryAction = onActionBarPrimaryActionChanged
            )
        }

        composable<AppRoute.ManageClassList> {
            ConfigureActionBar(
                route = AppRoute.ManageClassList,
                onActionBarChanged = onActionBarChanged,
                onActionBarPrimaryActionChanged = onActionBarPrimaryActionChanged
            )
            ManageClassListScreen(
                onNavigateToEditClass = { classId ->
                    navController.navigate(AppRoute.EditClass(classId))
                }
            )
        }

        composable<AppRoute.EditClass> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoute.EditClass>()
            ConfigureActionBar(
                route = route,
                onActionBarChanged = onActionBarChanged,
                onActionBarPrimaryActionChanged = onActionBarPrimaryActionChanged
            )
            EditClassScreen(classId = route.classId)
        }

        composable<AppRoute.ManageStudents> {
            ConfigureActionBar(
                route = AppRoute.ManageStudents,
                onActionBarChanged = onActionBarChanged,
                onActionBarPrimaryActionChanged = onActionBarPrimaryActionChanged
            )
            ManageStudentsScreen()
        }

        composable<AppRoute.TakeAttendance> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoute.TakeAttendance>()
            ConfigureActionBar(
                route = route,
                onActionBarChanged = onActionBarChanged,
                onActionBarPrimaryActionChanged = onActionBarPrimaryActionChanged
            )
            PlaceholderScaffold(
                title = "Take Attendance",
                readinessStep = "11",
                routeParameters = listOf(
                    "classId" to route.classId.toString(),
                    "scheduleId" to route.scheduleId.toString(),
                    "date" to route.date
                )
            )
        }

        composable<AppRoute.AddNote> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoute.AddNote>()
            ConfigureActionBar(
                route = route,
                onActionBarChanged = onActionBarChanged,
                onActionBarPrimaryActionChanged = onActionBarPrimaryActionChanged
            )
            PlaceholderScaffold(
                title = "Add Note",
                readinessStep = "12",
                routeParameters = listOf(
                    "date" to route.date,
                    "noteId" to route.noteId.toString()
                )
            )
        }

        composable<AppRoute.Settings> {
            ConfigureActionBar(
                route = AppRoute.Settings,
                onActionBarChanged = onActionBarChanged,
                onActionBarPrimaryActionChanged = onActionBarPrimaryActionChanged
            )
            PlaceholderScaffold(title = "Settings", readinessStep = "13")
        }
    }
}

private fun Context.resolveStatusBarHeightPx(): Int {
    return resolveAndroidDimensionPx("status_bar_height")
}

private fun Context.resolveNavigationBarHeightPx(): Int {
    return resolveAndroidDimensionPx("navigation_bar_height")
}

private fun Context.resolveAndroidDimensionPx(name: String): Int {
    val resourceId = resources.getIdentifier(name, "dimen", "android")
    if (resourceId == 0) {
        return 0
    }
    return resources.getDimensionPixelSize(resourceId)
}

private fun Context.resolveActionBarHeightPx(): Int {
    val typedValue = TypedValue()
    val hasActionBarSize = theme.resolveAttribute(android.R.attr.actionBarSize, typedValue, true)
    if (!hasActionBarSize) {
        return 0
    }
    return TypedValue.complexToDimensionPixelSize(typedValue.data, resources.displayMetrics)
}

private fun Context.pxToDp(px: Int): Dp {
    return (px / resources.displayMetrics.density).dp
}

@Composable
private fun ConfigureActionBar(
    route: AppRoute,
    onActionBarChanged: (title: String, showBack: Boolean) -> Unit,
    onActionBarPrimaryActionChanged: (ActionBarPrimaryAction?) -> Unit
) {
    val policy = route.actionBarPolicy()
    SideEffect {
        onActionBarChanged(policy.title, policy.showBack)
        onActionBarPrimaryActionChanged(null)
    }
}

@Composable
private fun PlaceholderScaffold(
    title: String,
    readinessStep: String,
    routeParameters: List<Pair<String, String>> = emptyList(),
    actions: @Composable ColumnScope.() -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "$title Placeholder",
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = "Ready for implementation in Step $readinessStep",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (routeParameters.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Route Parameters:",
                style = MaterialTheme.typography.labelMedium
            )
            routeParameters.forEach { (name, value) ->
                Text(
                    text = "$name: $value",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        actions()
    }
}
