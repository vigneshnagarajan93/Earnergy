package com.earnergy.ui.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.earnergy.ui.common.GlassSurface

@Composable
fun ChartsScreen(
    uiState: ChartsUiState,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF03040A), Color(0xFF0E1427))
                )
            )
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GlassSurface {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Sharp.ArrowBack,
                            contentDescription = "Back",
                            tint= Color.White
                        )
                    }
                    Text(
                        text = "Insights",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            Text(
                text = "This week",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White
            )
            GlassSurface {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ChartRow(
                        title = "Invested",
                        hours = uiState.investedHoursLabel,
                        progress = uiState.investedRatio,
                        color = Color(0xFF22C55E),

                    )
                    ChartRow(
                        title = "Drift",
                        hours = uiState.driftHoursLabel,
                        progress = uiState.driftRatio,
                        color = Color(0xFFF87171)
                    )
                    if (uiState.isLoading) {
                        Text(
                            text = "Loading insights…",
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    } else {
                        Text(
                            text = uiState.summary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White//MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartRow(title: String, hours: String, progress: Float, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White//MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = hours,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .height(6.dp)
                    .background(color, RoundedCornerShape(12.dp))
            )
        }
    }
}
