package com.earnergy.ui.dashboard

import androidx.compose.runtime.remember
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.earnergy.ui.common.GlassSurface
import com.earnergy.ui.common.ProgressRing

@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onOpenApps: () -> Unit,
    onOpenCharts: () -> Unit,
    onOpenSettings: () -> Unit,
    onRefresh: () -> Unit,
    onTakeBreak: () -> Unit,
    onDismissSuggestion: (String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val hasPermission = remember { com.earnergy.util.PermissionHelper.hasUsageStatsPermission(context) }
    
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0A0E1A), Color(0xFF1A1F2E))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Earnergy",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Track your time value",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
                IconButton(onClick = onRefresh) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = Color.White
                    )
                }
            }

            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage,
                    color = Color(0xFFF87171),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Permission Prompt
            if (!hasPermission) {
                PremiumCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "⚠️ Permission Required",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFBBF24)
                        )
                        Text(
                            text = "Earnergy needs Usage Access permission to track your app usage and calculate your time value.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        GradientActionButton(
                            text = "Grant Permission",
                            colors = listOf(Color(0xFFFBBF24), Color(0xFFF59E0B)),
                            onClick = { com.earnergy.util.PermissionHelper.openUsageAccessSettings(context) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "Tap the button above, then find \"Earnergy\" in the list and enable it.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            // Hero Card: Net Value
            PremiumCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = if (netValue >= 0) "Net Value Created" else "Net Value Lost",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = formatMoney(netValue.toDouble()),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (netValue >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                    )
                    Text(
                        text = when {
                            netValue > 0 -> "You're up ${formatMoney(netValue.toDouble())} today"
                            netValue < 0 -> "Drift is costing you ${formatMoney(-netValue.toDouble())}"
                            else -> if (hasPermission) "Start tracking your time" else "Grant permission to start tracking"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Normal,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }

            // Time Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PremiumCard(
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFF10B981), shape = androidx.compose.foundation.shape.CircleShape)
                            )
                            Text(
                                text = "Invested",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                        Text(
                            text = formatMinutes(investedMinutes),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
                
                PremiumCard(
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFFEF4444), shape = androidx.compose.foundation.shape.CircleShape)
                            )
                            Text(
                                text = "Drift",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                        Text(
                            text = formatMinutes(driftMinutes),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
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
                    onDismiss = onDismissSuggestion,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Action Button
            GradientActionButton(
                text = "Classify apps",
                colors = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)),
                onClick = onOpenApps,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
internal fun PremiumCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color.Black.copy(alpha = 0.3f),
                spotColor = Color.Black.copy(alpha = 0.3f)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E2433),
                        Color(0xFF181D2A)
                    )
                )
            )
    ) {
        content()
    }
}

@Composable
private fun LabelValueRow(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}

@Composable
private fun DividerLine() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
    )
}

@Composable
private fun GradientActionButton(
    text: String,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val gradient = Brush.horizontalGradient(colors)
    Box(
        modifier = modifier
            .background(gradient, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
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
