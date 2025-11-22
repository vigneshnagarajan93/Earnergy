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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.earnergy.domain.model.Suggestion
import com.earnergy.domain.model.SuggestionType

@Composable
fun SuggestionsCard(
    suggestions: List<Suggestion>,
    onDismiss: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (suggestions.isEmpty()) return
    
    PremiumCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "💡",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "Smart Suggestions",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Show top 3 suggestions
            suggestions.take(3).forEach { suggestion ->
                SuggestionItem(
                    suggestion = suggestion,
                    onDismiss = { onDismiss(suggestion.id) }
                )
            }
        }
    }
}

@Composable
private fun SuggestionItem(
    suggestion: Suggestion,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(getSuggestionBackgroundColor(suggestion.type))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = getSuggestionIcon(suggestion.type),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = suggestion.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
                Text(
                    text = suggestion.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
            
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun getSuggestionBackgroundColor(type: SuggestionType): Color {
    return when (type) {
        SuggestionType.TAKE_BREAK -> Color(0xFF10B981).copy(alpha = 0.15f)
        SuggestionType.IMPROVE_FOCUS -> Color(0xFF6366F1).copy(alpha = 0.15f)
        SuggestionType.REDUCE_DRIFT_TIME -> Color(0xFFEF4444).copy(alpha = 0.15f)
        SuggestionType.ENABLE_GRAYSCALE -> Color(0xFF8B5CF6).copy(alpha = 0.15f)
        SuggestionType.ENABLE_DND -> Color(0xFFFBBF24).copy(alpha = 0.15f)
        SuggestionType.ENERGY_BOOST -> Color(0xFF14B8A6).copy(alpha = 0.15f)
    }
}

private fun getSuggestionIcon(type: SuggestionType): String {
    return when (type) {
        SuggestionType.TAKE_BREAK -> "👁️"
        SuggestionType.IMPROVE_FOCUS -> "🎯"
        SuggestionType.REDUCE_DRIFT_TIME -> "⏰"
        SuggestionType.ENABLE_GRAYSCALE -> "🎨"
        SuggestionType.ENABLE_DND -> "🔕"
        SuggestionType.ENERGY_BOOST -> "⚡"
    }
}
