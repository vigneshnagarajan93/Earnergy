package com.earnergy.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.earnergy.domain.model.FocusMetrics

@Composable
fun NotificationInsightsCard(
    metrics: FocusMetrics,
    modifier: Modifier = Modifier
) {
    PremiumCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Notification Insights",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InsightItem(
                    label = "Total Unlocks",
                    value = metrics.unlockCount.toString(),
                    modifier = Modifier.weight(1f)
                )
                InsightItem(
                    label = "Notif. Unlocks",
                    value = metrics.notificationLedUnlockCount.toString(),
                    modifier = Modifier.weight(1f)
                )
            }

            InsightItem(
                label = "Drift Notifications",
                value = metrics.driftNotificationCount.toString(),
                description = "Unlocks triggered by Drift apps",
                modifier = Modifier.fillMaxWidth()
            )

            if (metrics.appNotificationUnlocks.isNotEmpty()) {
                val biggestOffender = metrics.appNotificationUnlocks.maxByOrNull { it.value }
                if (biggestOffender != null) {
                    Text(
                        text = "Most distracting: ${biggestOffender.key} (${biggestOffender.value} unlocks)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun InsightItem(
    label: String,
    value: String,
    description: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        if (description != null) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}
