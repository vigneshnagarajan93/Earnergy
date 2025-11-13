package <your.package.name>

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.InsertChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.earnergy.ui.appsclassification.AppClassificationScreen
import com.earnergy.ui.charts.ChartsScreen
import com.earnergy.ui.dashboard.DashboardScreen
import com.earnergy.ui.settings.SettingsScreen

private sealed class TimeWorthDestination(
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String
) {
    object Dashboard : TimeWorthDestination("dashboard", Icons.Filled.Home, "Dashboard")
    object AppsClassification :
        TimeWorthDestination("apps_classification", Icons.Filled.Apps, "Apps")

    object Charts : TimeWorthDestination("charts", Icons.Filled.InsertChart, "Charts")
    object Settings : TimeWorthDestination("settings", Icons.Filled.Settings, "Settings")
}

@Composable
fun TimeWorthRoot() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: TimeWorthDestination.Dashboard.route
    Scaffold(
        bottomBar = {
            NavigationBar {
                val destinations = listOf(
                    TimeWorthDestination.Dashboard,
                    TimeWorthDestination.AppsClassification,
                    TimeWorthDestination.Charts,
                    TimeWorthDestination.Settings
                )
                destinations.forEach { destination ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.label
                            )
                        },
                        label = { Text(destination.label) },
                        selected = currentRoute == destination.route,
                        onClick = {
                            if (currentRoute != destination.route) {
                                navController.navigate(destination.route) {
                                    launchSingleTop = true
                                    restoreState = true
                                    val graph = navController.graph
                                    popUpTo(graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = TimeWorthDestination.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(TimeWorthDestination.Dashboard.route) {
                DashboardScreen(
                    onOpenAppsClassification = {
                        navController.navigate(TimeWorthDestination.AppsClassification.route)
                    },
                    onOpenSettings = {
                        navController.navigate(TimeWorthDestination.Settings.route)
                    },
                    onOpenCharts = {
                        navController.navigate(TimeWorthDestination.Charts.route)
                    }
                )
            }
            composable(TimeWorthDestination.AppsClassification.route) {
                AppClassificationScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(TimeWorthDestination.Charts.route) {
                ChartsScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(TimeWorthDestination.Settings.route) {
                SettingsScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
