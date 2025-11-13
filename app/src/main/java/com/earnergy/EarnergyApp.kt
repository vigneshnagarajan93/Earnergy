package com.earnergy

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.graphics.vector.ImageVector
import com.earnergy.core.ui.theme.EarnergyTheme
import com.earnergy.ui.appclassification.AppClassificationScreen
import com.earnergy.ui.appclassification.AppClassificationViewModel
import com.earnergy.ui.charts.ChartsScreen
import com.earnergy.ui.charts.ChartsViewModel
import com.earnergy.ui.dashboard.DashboardScreen
import com.earnergy.ui.dashboard.DashboardViewModel
import com.earnergy.ui.settings.SettingsScreen
import com.earnergy.ui.settings.SettingsViewModel

@Composable
fun EarnergyApp() {
    EarnergyTheme {
        val navController = rememberNavController()
        val destinations = remember {
            listOf(
                EarnergyDestination(
                    route = EarnergyRoutes.Dashboard,
                    label = "Dashboard",
                    icon = Icons.Filled.Home
                ),
                EarnergyDestination(
                    route = EarnergyRoutes.AppsClassification,
                    label = "Apps",
                    icon = Icons.Filled.Apps
                ),
                EarnergyDestination(
                    route = EarnergyRoutes.Charts,
                    label = "Charts",
                    icon = Icons.Filled.InsertChart
                ),
                EarnergyDestination(
                    route = EarnergyRoutes.Settings,
                    label = "Settings",
                    icon = Icons.Filled.Settings
                )
            )
        }
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        Scaffold(
            bottomBar = {
                NavigationBar {
                    destinations.forEach { destination ->
                        val selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(imageVector = destination.icon, contentDescription = destination.label) },
                            label = { Text(text = destination.label) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = EarnergyRoutes.Dashboard,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(EarnergyRoutes.Dashboard) {
                    val viewModel: DashboardViewModel = hiltViewModel()
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                    DashboardScreen(
                        uiState = uiState,
                        onOpenAppsClassification = { navController.navigate(EarnergyRoutes.AppsClassification) },
                        onOpenCharts = { navController.navigate(EarnergyRoutes.Charts) },
                        onOpenSettings = { navController.navigate(EarnergyRoutes.Settings) },
                        onRefresh = { viewModel.refresh() }
                    )
                }
                composable(EarnergyRoutes.AppsClassification) {
                    val viewModel: AppClassificationViewModel = hiltViewModel()
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                    AppClassificationScreen(
                        uiState = uiState,
                        onRoleChanged = viewModel::onRoleChanged,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(EarnergyRoutes.Charts) {
                    val viewModel: ChartsViewModel = hiltViewModel()
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                    ChartsScreen(
                        uiState = uiState,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(EarnergyRoutes.Settings) {
                    val viewModel: SettingsViewModel = hiltViewModel()
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                    SettingsScreen(
                        state = uiState,
                        onHourlyRateChanged = viewModel::onHourlyRateChanged,
                        onSaveClicked = viewModel::onSaveClicked,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

private data class EarnergyDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private object EarnergyRoutes {
    const val Dashboard = "dashboard"
    const val AppsClassification = "apps_classification"
    const val Charts = "charts"
    const val Settings = "settings"
}
