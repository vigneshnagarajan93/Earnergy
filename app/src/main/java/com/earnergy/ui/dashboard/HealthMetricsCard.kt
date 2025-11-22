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
import androidx.compose.material3.MaterialTheme
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
    PremiumCard(
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
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "Eye Health",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
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
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = getStrainLabel(metrics.eyeStrainScore),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
                Text(
                    text = "${metrics.eyeStrainScore.toInt()}/100",
                    style = MaterialTheme.typography.headlineMedium,
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
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${metrics.breaksTaken} of ${metrics.breaksRecommended} recommended",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
                Text(
                    text = "${(metrics.breakComplianceRate * 100).toInt()}%",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = getComplianceColor(metrics.breakComplianceRate)
                )
            }
            
            // Time Since Last Break
            if (metrics.continuousScreenTimeMinutes > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (metrics.continuousScreenTimeMinutes >= 20) {
                                Color(0xFFEF4444).copy(alpha = 0.2f)
                            } else {
                                Color(0xFF252B3A)
                            }
                        )
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                            Text(
                                text = "${metrics.continuousScreenTimeMinutes} minutes",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
            
            // Take Break Button
            if (metrics.continuousScreenTimeMinutes >= 20) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            androidx.compose.ui.graphics.Brush.horizontalGradient(
                                listOf(Color(0xFF10B981), Color(0xFF059669))
                            )
                        )
                        .clickable(onClick = onTakeBreak)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Take a Break",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun getStrainColor(score: Double): Color {
    return when {
        score < 30 -> Color(0xFF10B981) // Green - Low strain
        score < 60 -> Color(0xFFFBBF24) // Yellow - Moderate strain
        else -> Color(0xFFEF4444) // Red - High strain
    }
}

@Composable
private fun getComplianceColor(rate: Double): Color {
    return when {
        rate >= 0.8 -> Color(0xFF10B981) // Green - Good compliance
        rate >= 0.5 -> Color(0xFFFBBF24) // Yellow - Moderate compliance
        else -> Color(0xFFEF4444) // Red - Poor compliance
    }
}

private fun getStrainLabel(score: Double): String {
    return when {
        score < 30 -> "Low strain - Keep it up!"
        score < 60 -> "Moderate strain"
        else -> "High strain - Take a break!"
    }
}
