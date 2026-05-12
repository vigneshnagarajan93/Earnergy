package com.earnergy.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.earnergy.domain.model.Suggestion
import com.earnergy.domain.model.SuggestionType

@Composable
fun SuggestionsCard(
    suggestions: List<Suggestion>,
    onSuggestionClick: (Suggestion) -> Unit,
    onDismiss: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (suggestions.isEmpty()) return
    
    ElevatedCard(modifier = modifier) {
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
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Smart Suggestions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Show top 3 suggestions
            suggestions.take(3).forEach { suggestion ->
                SuggestionItem(
                    suggestion = suggestion,
                    onClick = { onSuggestionClick(suggestion) },
                    onDismiss = { onDismiss(suggestion.id) }
                )
            }
        }
    }
}

@Composable
private fun SuggestionItem(
    suggestion: Suggestion,
    onClick: () -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = getSuggestionBackgroundColor(suggestion.type),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
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
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = suggestion.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun getSuggestionBackgroundColor(type: SuggestionType): Color {
    return when (type) {
        SuggestionType.TAKE_BREAK -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        SuggestionType.IMPROVE_FOCUS -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        SuggestionType.REDUCE_DRIFT_TIME -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
        SuggestionType.ENABLE_GRAYSCALE -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
        SuggestionType.ENABLE_DND -> MaterialTheme.colorScheme.surfaceVariant
        SuggestionType.ENERGY_BOOST -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
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
