package com.earnergy

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.InsertChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
import com.earnergy.ui.common.GlassSurface
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF101219), Color(0xFF1B1F2C))
                    )
                )
        ) {
            Scaffold(
                containerColor = Color.White,
                contentColor = MaterialTheme.colorScheme.onSurface,
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
                                onRefresh = viewModel::refresh
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
                                onSaveClicked = viewModel::onSaveClicked,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EarnergyBottomBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.hierarchy?.firstOrNull()?.route ?: "dashboard"
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0B0F19).copy(alpha = 0.95f))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEach { item ->
                val selected = currentRoute == item.route
                val gradient = if (selected) {
                    Brush.horizontalGradient(listOf(Color(0xFF4C6FFF), Color(0xFF8B5CF6)))
                } else {
                    Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                }
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(gradient)
                        .clickable {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = if (selected) Color.White else Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                        if (selected) {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class NavItem(val route: String, val label: String, val icon: ImageVector)

private val navItems = listOf(
    NavItem("dashboard", "Home", Icons.Default.Home),
    NavItem("apps", "Apps", Icons.Default.Apps),
    NavItem("insights", "Insights", Icons.Default.InsertChart),
    NavItem("settings", "Settings", Icons.Default.Settings)
)
