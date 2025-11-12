package com.earnergy.navigation

import android.content.Intent
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.earnergy.ui.dashboard.DashboardScreen
import com.earnergy.ui.dashboard.DashboardViewModel
import com.earnergy.ui.onboarding.OnboardingScreen
import com.earnergy.ui.settings.SettingsScreen
import com.earnergy.ui.settings.SettingsViewModel

object Destinations {
    const val Onboarding = "onboarding"
    const val Dashboard = "dashboard"
    const val Settings = "settings"
}

@Composable
fun EarnergyNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Destinations.Onboarding) {
        composable(Destinations.Onboarding) {
            val context = LocalContext.current
            OnboardingScreen(
                onRequestUsageAccess = {
                    context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                },
                onContinue = {
                    navController.navigate(Destinations.Dashboard) {
                        popUpTo(Destinations.Onboarding) { inclusive = true }
                    }
                }
            )
        }
        composable(Destinations.Dashboard) {
            val viewModel: DashboardViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            DashboardScreen(
                state = state,
                onRefresh = { viewModel.refreshNow() },
                onOpenSettings = { navController.navigate(Destinations.Settings) }
            )
        }
        composable(Destinations.Settings) {
            val viewModel: SettingsViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            SettingsScreen(
                state = state,
                onRateChange = viewModel::onHourlyRateChange,
                onSave = viewModel::saveRate
            )
        }
    }
}
