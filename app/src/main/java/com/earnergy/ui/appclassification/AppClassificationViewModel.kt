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

    private val _uiState = MutableStateFlow(AppClassificationUiState())
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
                            role = AppRole.INVESTED
                        ),
                        AppClassificationItem(
                            appName = "Social Feed",
                            packageName = "com.example.social",
                            role = AppRole.DRIFT
                        ),
                        AppClassificationItem(
                            appName = "Casual Game",
                            packageName = "com.example.game",
                            role = AppRole.IGNORED
                        )
                    )
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

data class AppClassificationUiState(
    val apps: List<AppClassificationItem> = emptyList()
)

data class AppClassificationItem(
    val appName: String,
    val packageName: String,
    val role: AppRole
)

enum class AppRole {
    INVESTED,
    DRIFT,
    IGNORED
}
