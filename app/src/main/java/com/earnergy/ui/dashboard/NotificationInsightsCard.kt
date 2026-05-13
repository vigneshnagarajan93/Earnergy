package com.earnergy.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.earnergy.domain.model.FocusMetrics

@Composable
fun NotificationInsightsCard(
    metrics: FocusMetrics,
    onOpenNotificationStats: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showCompulsiveExplanation by remember { mutableStateOf(false) }

    if (showCompulsiveExplanation) {
        AlertDialog(
            onDismissRequest = { showCompulsiveExplanation = false },
            title = { Text("Compulsive Unlocks") },
            text = {
                Text("A compulsive unlock is recorded when the entire 'unlock -> lock -> unlock' cycle is completed within 60 seconds. This metric helps identify habit-based phone checking.")
            },
            confirmButton = {
                TextButton(onClick = { showCompulsiveExplanation = false }) {
                    Text("Got it")
                }
            }
        )
    }

    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Device Interaction Insights",
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
                    label = "Compulsive Unlocks",
                    value = metrics.compulsiveUnlockCount.toString(),
                    onClick = { showCompulsiveExplanation = true },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InsightItem(
                    label = "Drift Notifications",
                    value = metrics.driftNotificationCount.toString(),
                    modifier = Modifier.weight(1f)
                )
                InsightItem(
                    label = "Total Notifications",
                    value = metrics.totalNotificationCount.toString(),
                    onClick = onOpenNotificationStats,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun InsightItem(
    label: String,
    value: String,
    description: String? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
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
