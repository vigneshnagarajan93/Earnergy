package com.earnergy.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Button
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import com.earnergy.ui.common.GlassSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onHourlyRateChanged: (String) -> Unit,
    onHealthFeaturesToggled: (Boolean) -> Unit,
    onBrightnessWarningToggled: (Boolean) -> Unit,
    onSaveClicked: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0D1116), Color(0xFF14182C))
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(text = "Settings", color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Sharp.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            content = { innerPadding ->
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .padding(24.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    GlassSurface {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Economy",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Hourly rate",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            GlassInputField(
                                value = uiState.hourlyRateInput,
                                onValueChange = onHourlyRateChanged
                            )
                            Text(
                                text = "Used to estimate the cost of drift time and the value of invested time. Stored only on this device.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                            uiState.errorMessage?.let { error ->
                                Text(text = error, color = Color(0xFFEF4444))
                            }
                        }
                    }

                    GlassSurface {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Health",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )

                            ToggleRow(
                                label = "20/20/20 Rule Reminders",
                                description = "Get overlays and notifications to take breaks every 20 minutes.",
                                checked = uiState.healthFeaturesEnabled,
                                onCheckedChange = onHealthFeaturesToggled
                            )

                            ToggleRow(
                                label = "Dark Environment Warning",
                                description = "Warn if screen brightness is too high in dark environments.",
                                checked = uiState.brightnessWarningEnabled,
                                onCheckedChange = onBrightnessWarningToggled
                            )
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    GradientButton(
                        text = if (uiState.isSaving) "Saving..." else "Save",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        onClick = {
                            if (!uiState.isSaving) {
                                onSaveClicked()
                            }
                        }
                    )
                }
            }
        )
    }
}

@Composable
private fun GlassInputField(value: String, onValueChange: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.20f)
                    )
                ),
                shape = RoundedCornerShape(18.dp)
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.AttachMoney,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    color = Color.White,//MaterialTheme.colorScheme.onSurface,
                    fontSize = MaterialTheme.typography.titleLarge.fontSize
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ToggleRow(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF4C6FFF),
                uncheckedThumbColor = Color.White.copy(alpha = 0.6f),
                uncheckedTrackColor = Color.White.copy(alpha = 0.2f)
            )
        )
    }
}

@Composable
private fun GradientButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val gradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFF4C6FFF), Color(0xFF8B5CF6))
    )
    Button(
        onClick = onClick,
        modifier = modifier.clip(RoundedCornerShape(16.dp)),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = text, color = Color.White)
        }
    }
}
