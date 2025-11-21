package com.earnergy.ui.appclassification

import androidx.lifecycle.ViewModel
import com.earnergy.domain.model.AppRole
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.earnergy.core.data.repository.UsageRepository
import java.time.LocalDate


@HiltViewModel
class AppClassificationViewModel @Inject constructor(
    private val usageRepository: UsageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppClassificationUiState(isLoading = true))
    val uiState: StateFlow<AppClassificationUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val today = LocalDate.now().toEpochDay()
            usageRepository.observeDaySummary(today).collect { summary ->
                _uiState.update { current ->
                    current.copy(
                        apps = summary.usages.map { usage ->
                            AppClassificationItem(
                                appName = usage.displayName,
                                packageName = usage.packageName,
                                todayMinutes = usage.totalForeground.inWholeMinutes.toInt(),
                                role = usage.role,
                                isSystemApp = usage.isSystemApp
                            )
                        },
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onRoleChanged(packageName: String, newRole: AppRole) {
        viewModelScope.launch {
            usageRepository.updateAppRole(packageName, newRole)
        }
    }
    
    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }
}

