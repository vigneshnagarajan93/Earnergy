package com.earnergy.ui.appclassification

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AppRolesScreen(onBack: () -> Unit) {
    val viewModel: AppClassificationViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    AppClassificationScreen(
        uiState = uiState,
        onRoleChanged = viewModel::onRoleChanged,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onBack = onBack
    )
}
