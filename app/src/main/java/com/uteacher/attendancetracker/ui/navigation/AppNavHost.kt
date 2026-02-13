package com.uteacher.attendancetracker.ui.navigation

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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.uteacher.attendancetracker.ui.screen.auth.AuthGateScreen
import com.uteacher.attendancetracker.ui.screen.dashboard.DashboardScreen
import com.uteacher.attendancetracker.ui.screen.setup.SetupScreen
import com.uteacher.attendancetracker.ui.screen.splash.SplashScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: AppRoute,
    onActionBarChanged: (title: String, showBack: Boolean) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable<AppRoute.Splash> {
            ConfigureActionBar(route = AppRoute.Splash, onActionBarChanged = onActionBarChanged)
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
            ConfigureActionBar(route = AppRoute.Setup, onActionBarChanged = onActionBarChanged)
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
            ConfigureActionBar(route = AppRoute.AuthGate, onActionBarChanged = onActionBarChanged)
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
            ConfigureActionBar(route = AppRoute.Dashboard, onActionBarChanged = onActionBarChanged)
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
            ConfigureActionBar(route = AppRoute.CreateClass, onActionBarChanged = onActionBarChanged)
            PlaceholderScaffold(title = "Create Class", readinessStep = "09")
        }

        composable<AppRoute.ManageClassList> {
            ConfigureActionBar(
                route = AppRoute.ManageClassList,
                onActionBarChanged = onActionBarChanged
            )
            PlaceholderScaffold(title = "Manage Classes", readinessStep = "09")
        }

        composable<AppRoute.EditClass> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoute.EditClass>()
            ConfigureActionBar(route = route, onActionBarChanged = onActionBarChanged)
            PlaceholderScaffold(
                title = "Edit Class",
                readinessStep = "09",
                routeParameters = listOf("classId" to route.classId.toString())
            )
        }

        composable<AppRoute.ManageStudents> {
            ConfigureActionBar(route = AppRoute.ManageStudents, onActionBarChanged = onActionBarChanged)
            PlaceholderScaffold(title = "Manage Students", readinessStep = "10")
        }

        composable<AppRoute.TakeAttendance> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoute.TakeAttendance>()
            ConfigureActionBar(route = route, onActionBarChanged = onActionBarChanged)
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
            ConfigureActionBar(route = route, onActionBarChanged = onActionBarChanged)
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
            ConfigureActionBar(route = AppRoute.Settings, onActionBarChanged = onActionBarChanged)
            PlaceholderScaffold(title = "Settings", readinessStep = "13")
        }
    }
}

@Composable
private fun ConfigureActionBar(
    route: AppRoute,
    onActionBarChanged: (title: String, showBack: Boolean) -> Unit
) {
    val policy = route.actionBarPolicy()
    SideEffect {
        onActionBarChanged(policy.title, policy.showBack)
    }
}

@Composable
private fun PlaceholderScaffold(
    title: String,
    readinessStep: String,
    routeParameters: List<Pair<String, String>> = emptyList(),
    actions: @Composable ColumnScope.() -> Unit = {}
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
}
