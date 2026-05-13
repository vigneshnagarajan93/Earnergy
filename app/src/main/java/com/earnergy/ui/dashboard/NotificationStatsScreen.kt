package com.earnergy.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.earnergy.domain.model.AppRole
import com.earnergy.ui.common.PieChart
import com.earnergy.ui.common.PieChartData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationStatsScreen(
    uiState: NotificationStatsUiState,
    onSearchQueryChanged: (String) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notification Statistics") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
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
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                placeholder = { Text("Search apps...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    item {
                        NotificationChartsSection(uiState)
                    }

                    item {
                        Text(
                            text = "App Breakdown",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(uiState.appNotificationStats) { stat ->
                        AppNotificationItem(stat)
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationChartsSection(uiState: NotificationStatsUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        // Total Notifications Chart
        if (uiState.appNotificationStats.isNotEmpty()) {
            val chartData = uiState.appNotificationStats
                .take(5)
                .map { stat ->
                    PieChartData(
                        label = stat.displayName,
                        value = stat.count.toFloat(),
                        color = getRoleColor(stat.role)
                    )
                }.toMutableList()

            val otherCount = uiState.appNotificationStats.drop(5).sumOf { it.count }
            if (otherCount > 0) {
                chartData.add(
                    PieChartData(
                        label = "Others",
                        value = otherCount.toFloat(),
                        color = Color.Gray
                    )
                )
            }

            ChartCard(
                title = "All Notifications (${uiState.totalNotifications})",
                data = chartData
            )
        }

        // Drift Notifications Chart
        if (uiState.driftNotificationStats.isNotEmpty()) {
            val driftTotal = uiState.driftNotificationStats.sumOf { it.count }
            val driftChartData = uiState.driftNotificationStats
                .take(5)
                .map { stat ->
                    PieChartData(
                        label = stat.displayName,
                        value = stat.count.toFloat(),
                        color = MaterialTheme.colorScheme.error
                    )
                }.toMutableList()

            val otherDriftCount = uiState.driftNotificationStats.drop(5).sumOf { it.count }
            if (otherDriftCount > 0) {
                driftChartData.add(
                    PieChartData(
                        label = "Other Drift",
                        value = otherDriftCount.toFloat(),
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                    )
                )
            }

            ChartCard(
                title = "Drift Notifications ($driftTotal)",
                data = driftChartData
            )
        }
    }
}

@Composable
private fun ChartCard(title: String, data: List<PieChartData>) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(16.dp))
            PieChart(
                data = data,
                modifier = Modifier.size(200.dp)
            )
        }
    }
}

@Composable
private fun AppNotificationItem(stat: AppNotificationStat) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stat.displayName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = stat.packageName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = stat.count.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = getRoleColor(stat.role)
        )
    }
}

@Composable
private fun getRoleColor(role: AppRole): Color {
    return when (role) {
        AppRole.INVESTED -> MaterialTheme.colorScheme.primary
        AppRole.DRIFT -> MaterialTheme.colorScheme.error
        AppRole.IGNORED -> MaterialTheme.colorScheme.outline
    }
}
