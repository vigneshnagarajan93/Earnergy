package com.earnergy.ui.charts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.earnergy.ui.charts.ChartsViewModel

@Composable
fun InsightsScreen(onBack: () -> Unit) {
    val viewModel: ChartsViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ChartsScreen(
        uiState = uiState,
        onBack = onBack
    )
}
