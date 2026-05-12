package com.earnergy.ui.dashboard

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.earnergy.ui.common.GlassSurface
import com.earnergy.ui.common.ProgressRing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onOpenApps: () -> Unit,
    onOpenCharts: () -> Unit,
    onOpenSettings: () -> Unit,
    onRefresh: () -> Unit,
    onTakeBreak: () -> Unit,
    onSuggestionClick: (com.earnergy.domain.model.Suggestion) -> Unit,
    onDismissSuggestion: (String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val hasPermission = remember { com.earnergy.util.PermissionHelper.hasUsageStatsPermission(context) }
    val isIgnoringBatteryOptimizations = remember { androidx.compose.runtime.mutableStateOf(com.earnergy.util.PermissionHelper.isIgnoringBatteryOptimizations(context)) }
    
    val scrollState = rememberScrollState()
    val netValue by animateFloatAsState(
        targetValue = uiState.netValue.toFloat(),
        animationSpec = tween(durationMillis = 800, easing = LinearEasing)
    )
    val investedMinutes by animateIntAsState(
        targetValue = uiState.investedMinutes,
        animationSpec = tween(durationMillis = 600, easing = LinearEasing)
    )
    val driftMinutes by animateIntAsState(
        targetValue = uiState.driftMinutes,
        animationSpec = tween(durationMillis = 600, easing = LinearEasing)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Earnergy",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Track your time value",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Battery Optimization Prompt
            if (!isIgnoringBatteryOptimizations.value) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "⚠️ Battery Optimization",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Please disable battery optimizations for Earnergy to ensure background tracking works reliably.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Button(
                            onClick = { com.earnergy.util.PermissionHelper.requestIgnoreBatteryOptimizations(context) },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Disable Optimization")
                        }
                    }
                }
            }

            // Permission Prompt
            if (!hasPermission) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "⚠️ Permission Required",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Earnergy needs Usage Access permission to track your app usage and calculate your time value.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Button(
                            onClick = { com.earnergy.util.PermissionHelper.openUsageAccessSettings(context) },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Grant Permission")
                        }
                    }
                }
            }

            // Hero Card: Net Value
            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    val isPositive = netValue >= 0
                    Text(
                        text = if (isPositive) "Net Value Created" else "Net Value Lost",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatMoney(netValue.toDouble()),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isPositive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = when {
                            netValue > 0 -> "You're up ${formatMoney(netValue.toDouble())} today"
                            netValue < 0 -> "Drift is costing you ${formatMoney(-netValue.toDouble())}"
                            else -> if (hasPermission) "Start tracking your time" else "Grant permission to start"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Time Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ElevatedCard(
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Invested",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = formatMinutes(investedMinutes),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                ElevatedCard(
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Drift",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = formatMinutes(driftMinutes),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Focus Score Card
            if (uiState.focusMetrics != null) {
                FocusScoreCard(
                    metrics = uiState.focusMetrics,
                    modifier = Modifier.fillMaxWidth()
                )

                NotificationInsightsCard(
                    metrics = uiState.focusMetrics,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Health Metrics Card
            if (uiState.healthMetrics != null) {
                HealthMetricsCard(
                    metrics = uiState.healthMetrics,
                    onTakeBreak = onTakeBreak,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Suggestions Card
            if (uiState.suggestions.isNotEmpty()) {
                SuggestionsCard(
                    suggestions = uiState.suggestions,
                    onSuggestionClick = onSuggestionClick,
                    onDismiss = onDismissSuggestion,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Action Button
            Button(
                onClick = onOpenApps,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .height(56.dp)
            ) {
                Text("Classify apps")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun formatMinutes(minutes: Int): String {
    val hours = minutes / 60
    val remaining = minutes % 60
    return if (hours > 0) {
        "${hours}h ${remaining}m"
    } else {
        "${remaining}m"
    }
}

private fun formatMoney(value: Double): String =
    "$" + String.format("%.2f", value)
