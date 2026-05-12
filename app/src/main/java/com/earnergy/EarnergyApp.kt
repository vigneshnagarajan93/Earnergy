package com.earnergy

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.InsertChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.InsertChart
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.earnergy.core.ui.theme.EarnergyTheme
import com.earnergy.ui.appclassification.AppRolesScreen
import com.earnergy.ui.dashboard.DashboardScreen
import com.earnergy.ui.dashboard.DashboardViewModel
import com.earnergy.ui.charts.InsightsScreen
import com.earnergy.ui.settings.SettingsEvent
import com.earnergy.ui.settings.SettingsScreen
import com.earnergy.ui.settings.SettingsViewModel

@Composable
fun EarnergyApp() {
    val navController = rememberNavController()
    EarnergyTheme {
        Scaffold(
            bottomBar = { EarnergyBottomBar(navController) }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                NavHost(
                    navController = navController,
                    startDestination = "dashboard"
                ) {
                    composable("dashboard") {
                        val viewModel: DashboardViewModel = hiltViewModel()
                        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                        val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

                        androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
                            val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                                if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                                    viewModel.refresh()
                                }
                            }
                            lifecycleOwner.lifecycle.addObserver(observer)
                            onDispose {
                                lifecycleOwner.lifecycle.removeObserver(observer)
                            }
                        }

                        DashboardScreen(
                            uiState = uiState,
                            onOpenApps = { navController.navigate("apps") },
                            onOpenCharts = { navController.navigate("insights") },
                            onOpenSettings = { navController.navigate("settings") },
                            onRefresh = viewModel::refresh,
                            onTakeBreak = { viewModel.logBreak() },
                            onSuggestionClick = { suggestion ->
                                val context = navController.context
                                if (suggestion.type == com.earnergy.domain.model.SuggestionType.ENABLE_GRAYSCALE) {
                                    val intent = android.content.Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                    context.startActivity(intent)
                                }
                                viewModel.onSuggestionClicked(suggestion)
                            },
                            onDismissSuggestion = viewModel::dismissSuggestion
                        )
                    }
                    composable("apps") {
                        AppRolesScreen(onBack = { navController.popBackStack() })
                    }
                    composable("insights") {
                        InsightsScreen(onBack = { navController.popBackStack() })
                    }
                    composable("settings") {
                        val viewModel: SettingsViewModel = hiltViewModel()
                        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                        LaunchedEffect(viewModel.events) {
                            viewModel.events.collect { event ->
                                if (event is SettingsEvent.SaveSuccess) {
                                    navController.popBackStack()
                                }
                            }
                        }
                        SettingsScreen(
                            uiState = uiState,
                            onHourlyRateChanged = viewModel::onHourlyRateChanged,
                            onHealthFeaturesToggled = viewModel::onHealthFeaturesToggled,
                            onBrightnessWarningToggled = viewModel::onBrightnessWarningToggled,
                            onSaveClicked = viewModel::onSaveClicked,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EarnergyBottomBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    NavigationBar {
        navItems.forEach { item ->
            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) },
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

private data class NavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val navItems = listOf(
    NavItem("dashboard", "Home", Icons.Filled.Home, Icons.Outlined.Home),
    NavItem("apps", "Apps", Icons.Filled.Apps, Icons.Outlined.Apps),
    NavItem("insights", "Insights", Icons.Filled.InsertChart, Icons.Outlined.InsertChart),
    NavItem("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
)
