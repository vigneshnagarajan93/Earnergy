package com.earnergy.ui.dashboard

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    onRefresh: () -> Unit
) {
    val scrollState = rememberScrollState()
    val investedMinutes by animateIntAsState(
        targetValue = uiState.investedMinutes,
        animationSpec = tween(durationMillis = 600, easing = LinearEasing)
    )
    val driftMinutes by animateIntAsState(
        targetValue = uiState.driftMinutes,
        animationSpec = tween(durationMillis = 600, easing = LinearEasing)
    )
    val progressTarget = (investedMinutes + driftMinutes).let { total ->
        if (total == 0) 0f else investedMinutes / total.toFloat()
    }
    val progress by animateFloatAsState(
        targetValue = progressTarget,
        animationSpec = tween(durationMillis = 800, easing = LinearEasing)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF08090F), Color(0xFF14182C))
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
                Text(
                    text = "Earnergy",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White//MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onRefresh) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = Color.White//MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            GlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ProgressRing(
                        progress = progress,
                        size = 160.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Invested time today",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White//MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatMinutes(investedMinutes),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White//MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            GlassSurface(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LabelValueRow(
                        label = "Drift time today",
                        value = formatMinutes(driftMinutes),
                        valueColor = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    DividerLine()
                    Spacer(modifier = Modifier.height(8.dp))
                    LabelValueRow(
                        label = "Cost of drift time",
                        value = formatMoney(uiState.costOfDriftTime),
                        valueColor = if (uiState.costOfDriftTime > 0.0) Color.Red else Color.Green
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GradientActionButton(
                    text = "Classify apps",
                    colors = listOf(Color(0xFFF59E0B), Color(0xFFF97316)),
                    onClick = onOpenApps
                )
                GradientActionButton(
                    text = "View charts",
                    colors = listOf(Color(0xFF22C55E), Color(0xFF14B8A6)),
                    onClick = onOpenCharts
                )
                GradientActionButton(
                    text = "Settings",
                    colors = listOf(Color(0xFF8B5CF6), Color(0xFFEC4899)),
                    onClick = onOpenSettings
                )
            }
        }
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
private fun RowScope.GradientActionButton(
    text: String,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val gradient = Brush.horizontalGradient(colors)
    Box(
        modifier = modifier
            .weight(1f)
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
