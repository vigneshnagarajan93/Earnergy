package com.earnergy.ui.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.earnergy.domain.model.HealthMetrics
import com.earnergy.domain.model.EyeHealthStatus

@Composable
fun HealthMetricsCard(
    metrics: HealthMetrics,
    onTakeBreak: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "EYE HEALTH STATUS",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = metrics.status.label,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = getStatusColor(metrics.status)
                    )
                }
                Text(
                    text = "👁️",
                    style = MaterialTheme.typography.displaySmall
                )
            }

            // Insight Text
            Text(
                text = metrics.status.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Strain Progress Bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Current Session",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = "${metrics.currentSessionMinutes}m",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                val progress by animateFloatAsState(
                    targetValue = (metrics.currentSessionMinutes / 60f).coerceIn(0f, 1f),
                    label = "strainProgress"
                )

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    color = getStrainProgressColor(metrics.currentSessionMinutes),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("0m", style = MaterialTheme.typography.labelSmall)
                    Text("20m", style = MaterialTheme.typography.labelSmall)
                    Text("40m", style = MaterialTheme.typography.labelSmall)
                    Text("60m+", style = MaterialTheme.typography.labelSmall)
                }
            }

            // Secondary Metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MetricSmall(
                    label = "Daily Strain",
                    value = "${metrics.dailyStrainMinutes}m",
                    modifier = Modifier.weight(1f)
                )
                MetricSmall(
                    label = "Max Session",
                    value = "${metrics.maxContinuousSessionMinutes}m",
                    modifier = Modifier.weight(1f)
                )
            }

            // Action Button
            if (metrics.currentSessionMinutes >= 20) {
                Button(
                    onClick = onTakeBreak,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Take a 20s Break")
                }
            }
        }
    }
}

@Composable
private fun MetricSmall(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun getStatusColor(status: EyeHealthStatus): Color {
    return when (status) {
        EyeHealthStatus.EXCELLENT -> Color(0xFF4CAF50) // Green
        EyeHealthStatus.GOOD -> Color(0xFF8BC34A)      // Light Green
        EyeHealthStatus.FAIR -> Color(0xFFFF9800)      // Orange
        EyeHealthStatus.POOR -> Color(0xFFF44336)      // Red
    }
}

@Composable
private fun getStrainProgressColor(minutes: Int): Color {
    return when {
        minutes <= 20 -> Color(0xFF4CAF50) // Green
        minutes <= 40 -> Color(0xFFFFEB3B) // Yellow
        else -> Color(0xFFF44336)          // Red
    }
}
