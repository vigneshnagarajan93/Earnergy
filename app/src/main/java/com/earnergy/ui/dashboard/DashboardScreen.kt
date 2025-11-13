package com.earnergy.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onOpenAppsClassification: () -> Unit,
    onOpenCharts: () -> Unit,
    onOpenSettings: () -> Unit,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Today's Focus",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        UsageSummaryCard(uiState)
        EarningsCard(uiState)
        Spacer(modifier = Modifier.weight(1f))
        if (uiState.isRefreshing) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onRefresh
        ) {
            Text("Refresh Usage")
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onOpenAppsClassification
        ) {
            Text("Manage App Roles")
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onOpenCharts
        ) {
            Text("View Charts")
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onOpenSettings
        ) {
            Text("Settings")
        }
    }
}

@Composable
private fun UsageSummaryCard(state: DashboardUiState) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "Productive", style = MaterialTheme.typography.titleMedium)
            Text(text = state.productiveTime, style = MaterialTheme.typography.headlineSmall)
            Text(text = "Passive", style = MaterialTheme.typography.titleMedium)
            Text(text = state.passiveTime, style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
private fun EarningsCard(state: DashboardUiState) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "Potential Earn", style = MaterialTheme.typography.titleMedium)
            Text(text = state.potentialEarnings, style = MaterialTheme.typography.headlineSmall)
            Text(text = "Potential Loss", style = MaterialTheme.typography.titleMedium)
            Text(text = state.potentialLoss, style = MaterialTheme.typography.headlineSmall)
            Text(
                text = "Hourly rate: ${state.hourlyRate}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
