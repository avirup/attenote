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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.uteacher.attendancetracker.ui.theme.component.AttenoteButton

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: AppRoute = AppRoute.Splash,
    onActionBarChanged: (title: String, showBack: Boolean) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable<AppRoute.Splash> {
            ConfigureActionBar(
                route = AppRoute.Splash,
                onActionBarChanged = onActionBarChanged
            )
            SplashPlaceholder(
                onOpenSetup = { navController.navigate(AppRoute.Setup) },
                onOpenAuthGate = { navController.navigate(AppRoute.AuthGate) },
                onOpenDashboard = { navController.navigate(AppRoute.Dashboard) }
            )
        }

        composable<AppRoute.Setup> {
            ConfigureActionBar(
                route = AppRoute.Setup,
                onActionBarChanged = onActionBarChanged
            )
            PlaceholderScaffold(
                title = "Setup",
                readinessStep = "03"
            ) {
                AttenoteButton(
                    text = "Complete Setup",
                    onClick = {
                        navController.navigate(AppRoute.Dashboard) {
                            popUpTo<AppRoute.Splash> {
                                inclusive = true
                            }
                        }
                    }
                )
            }
        }

        composable<AppRoute.AuthGate> {
            ConfigureActionBar(
                route = AppRoute.AuthGate,
                onActionBarChanged = onActionBarChanged
            )
            AuthGatePlaceholder(
                onAuthSuccess = {
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
                onActionBarChanged = onActionBarChanged
            )
            DashboardPlaceholder(
                onOpenCreateClass = { navController.navigate(AppRoute.CreateClass) },
                onOpenManageClasses = { navController.navigate(AppRoute.ManageClassList) },
                onOpenEditClass = { navController.navigate(AppRoute.EditClass(classId = 1L)) },
                onOpenManageStudents = { navController.navigate(AppRoute.ManageStudents) },
                onOpenTakeAttendance = {
                    navController.navigate(
                        AppRoute.TakeAttendance(
                            classId = 1L,
                            scheduleId = 1L,
                            date = "2026-02-13"
                        )
                    )
                },
                onOpenAddNote = {
                    navController.navigate(
                        AppRoute.AddNote(
                            date = "2026-02-13",
                            noteId = -1L
                        )
                    )
                },
                onOpenSettings = { navController.navigate(AppRoute.Settings) }
            )
        }

        composable<AppRoute.CreateClass> {
            ConfigureActionBar(
                route = AppRoute.CreateClass,
                onActionBarChanged = onActionBarChanged
            )
            PlaceholderScaffold(
                title = "Create Class",
                readinessStep = "05"
            )
        }

        composable<AppRoute.ManageClassList> {
            ConfigureActionBar(
                route = AppRoute.ManageClassList,
                onActionBarChanged = onActionBarChanged
            )
            PlaceholderScaffold(
                title = "Manage Classes",
                readinessStep = "06"
            )
        }

        composable<AppRoute.EditClass> { backStackEntry ->
            val route: AppRoute.EditClass = backStackEntry.toRoute()
            ConfigureActionBar(
                route = route,
                onActionBarChanged = onActionBarChanged
            )
            PlaceholderScaffold(
                title = "Edit Class",
                readinessStep = "06",
                routeParameters = listOf("classId" to route.classId.toString())
            )
        }

        composable<AppRoute.ManageStudents> {
            ConfigureActionBar(
                route = AppRoute.ManageStudents,
                onActionBarChanged = onActionBarChanged
            )
            PlaceholderScaffold(
                title = "Manage Students",
                readinessStep = "07"
            )
        }

        composable<AppRoute.TakeAttendance> { backStackEntry ->
            val route: AppRoute.TakeAttendance = backStackEntry.toRoute()
            ConfigureActionBar(
                route = route,
                onActionBarChanged = onActionBarChanged
            )
            PlaceholderScaffold(
                title = "Take Attendance",
                readinessStep = "08",
                routeParameters = listOf(
                    "classId" to route.classId.toString(),
                    "scheduleId" to route.scheduleId.toString(),
                    "date" to route.date
                )
            )
        }

        composable<AppRoute.AddNote> { backStackEntry ->
            val route: AppRoute.AddNote = backStackEntry.toRoute()
            ConfigureActionBar(
                route = route,
                onActionBarChanged = onActionBarChanged
            )
            PlaceholderScaffold(
                title = "Add Note",
                readinessStep = "09",
                routeParameters = listOf(
                    "date" to route.date,
                    "noteId" to route.noteId.toString()
                )
            )
        }

        composable<AppRoute.Settings> {
            ConfigureActionBar(
                route = AppRoute.Settings,
                onActionBarChanged = onActionBarChanged
            )
            PlaceholderScaffold(
                title = "Settings",
                readinessStep = "10"
            )
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
private fun SplashPlaceholder(
    onOpenSetup: () -> Unit,
    onOpenAuthGate: () -> Unit,
    onOpenDashboard: () -> Unit
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
                text = "Splash Placeholder",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Root route with no back navigation.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            AttenoteButton(text = "Go to Setup", onClick = onOpenSetup)
            AttenoteButton(text = "Go to Auth Gate", onClick = onOpenAuthGate)
            AttenoteButton(text = "Open Dashboard (Debug)", onClick = onOpenDashboard)
        }
    }
}

@Composable
private fun AuthGatePlaceholder(onAuthSuccess: () -> Unit) {
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
                text = "Auth Gate Placeholder",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Root route with no back navigation.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            AttenoteButton(text = "Authenticate", onClick = onAuthSuccess)
        }
    }
}

@Composable
private fun DashboardPlaceholder(
    onOpenCreateClass: () -> Unit,
    onOpenManageClasses: () -> Unit,
    onOpenEditClass: () -> Unit,
    onOpenManageStudents: () -> Unit,
    onOpenTakeAttendance: () -> Unit,
    onOpenAddNote: () -> Unit,
    onOpenSettings: () -> Unit
) {
    var validationResult by rememberSaveable {
        mutableStateOf("Not tested yet")
    }

    PlaceholderScaffold(
        title = "Dashboard",
        readinessStep = "04"
    ) {
        AttenoteButton(text = "Create Class", onClick = onOpenCreateClass)
        AttenoteButton(text = "Manage Classes", onClick = onOpenManageClasses)
        AttenoteButton(text = "Edit Class (classId = 1)", onClick = onOpenEditClass)
        AttenoteButton(text = "Manage Students", onClick = onOpenManageStudents)
        AttenoteButton(text = "Take Attendance", onClick = onOpenTakeAttendance)
        AttenoteButton(text = "Add Note", onClick = onOpenAddNote)
        AttenoteButton(text = "Settings", onClick = onOpenSettings)

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Date Validation Check:",
            style = MaterialTheme.typography.labelMedium
        )
        AttenoteButton(
            text = "Test Invalid Date Formats",
            onClick = {
                val firstError = runCatching { AppRoute.AddNote("2026/02/13") }
                    .exceptionOrNull()
                    ?.message
                    ?: "No exception for 2026/02/13"
                val secondError = runCatching { AppRoute.TakeAttendance(1L, 1L, "13-02-2026") }
                    .exceptionOrNull()
                    ?.message
                    ?: "No exception for 13-02-2026"
                validationResult = "$firstError | $secondError"
            }
        )
        Text(
            text = validationResult,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
