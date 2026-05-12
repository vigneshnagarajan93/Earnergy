package com.earnergy.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.earnergy.domain.model.FocusMetrics

@Composable
fun NotificationInsightsCard(
    metrics: FocusMetrics,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Notification Insights",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
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
                description = "Total notifications from Drift apps",
                modifier = Modifier.fillMaxWidth()
            )

            if (metrics.appNotificationUnlocks.isNotEmpty()) {
                val biggestOffender = metrics.appNotificationUnlocks.maxByOrNull { it.value }
                if (biggestOffender != null) {
                    Text(
                        text = "Most distracting: ${biggestOffender.key} (${biggestOffender.value} unlocks)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        if (description != null) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
