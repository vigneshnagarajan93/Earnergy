package com.earnergy.ui.appclassification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AppClassificationViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(AppClassificationUiState(isLoading = true))
    val uiState: StateFlow<AppClassificationUiState> = _uiState.asStateFlow()

    init {
        // Seed with mock data until repositories are wired.
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    apps = listOf(
                        AppClassificationItem(
                            appName = "Earnergy",
                            packageName = "com.earnergy",
                            todayMinutes = 95,
                            role = AppRole.INVESTED
                        ),
                        AppClassificationItem(
                            appName = "Social Feed",
                            packageName = "com.example.social",
                            todayMinutes = 45,
                            role = AppRole.DRIFT
                        ),
                        AppClassificationItem(
                            appName = "Casual Game",
                            packageName = "com.example.game",
                            todayMinutes = 20,
                            role = AppRole.IGNORED
                        )
                    ),
                    isLoading = false
                )
            }
        }
    }

    fun onRoleChanged(packageName: String, newRole: AppRole) {
        _uiState.update { current ->
            current.copy(
                apps = current.apps.map { app ->
                    if (app.packageName == packageName) {
                        app.copy(role = newRole)
                    } else {
                        app
                    }
                }
            )
        }
    }
}

