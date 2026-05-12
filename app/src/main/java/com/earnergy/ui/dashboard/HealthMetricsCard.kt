package com.earnergy.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.earnergy.domain.model.HealthMetrics

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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "👁️",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Eye Health",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Eye Strain Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Eye Strain",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = getStrainLabel(metrics.eyeStrainScore),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "${metrics.eyeStrainScore.toInt()}/100",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = getStrainColor(metrics.eyeStrainScore)
                )
            }
            
            // Break Compliance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Breaks Taken",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${metrics.breaksTaken} of ${metrics.breaksRecommended} recommended",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "${(metrics.breakComplianceRate * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = getComplianceColor(metrics.breakComplianceRate)
                )
            }
            
            // Time Since Last Break
            if (metrics.continuousScreenTimeMinutes > 0) {
                Surface(
                    color = if (metrics.continuousScreenTimeMinutes >= 20) {
                        MaterialTheme.colorScheme.errorContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (metrics.continuousScreenTimeMinutes >= 20) {
                                    "⚠️ Time for a break!"
                                } else {
                                    "Time since last break"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${metrics.continuousScreenTimeMinutes} minutes",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // Take Break Button
            if (metrics.continuousScreenTimeMinutes >= 20) {
                Button(
                    onClick = onTakeBreak,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Take a Break")
                }
            }
        }
    }
}

@Composable
private fun getStrainColor(score: Double): Color {
    return when {
        score < 30 -> MaterialTheme.colorScheme.primary
        score < 60 -> Color(0xFFFF9800) // Amber
        else -> MaterialTheme.colorScheme.error
    }
}

@Composable
private fun getComplianceColor(rate: Double): Color {
    return when {
        rate >= 0.8 -> MaterialTheme.colorScheme.primary
        rate >= 0.5 -> Color(0xFFFF9800) // Amber
        else -> MaterialTheme.colorScheme.error
    }
}

private fun getStrainLabel(score: Double): String {
    return when {
        score < 30 -> "Low strain - Keep it up!"
        score < 60 -> "Moderate strain"
        else -> "High strain - Take a break!"
    }
}
