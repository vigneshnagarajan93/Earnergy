package com.earnergy.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.util.Currency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onHourlyRateChanged: (String) -> Unit,
    onCurrencyChanged: (String) -> Unit,
    onCurrencySearchQueryChanged: (String) -> Unit,
    onHealthFeaturesToggled: (Boolean) -> Unit,
    onBrightnessWarningToggled: (Boolean) -> Unit,
    onSaveClicked: (Boolean) -> Unit,
    onDismissConfirmation: () -> Unit,
    onBack: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedCurrency = remember(uiState.currencyCode) {
        try {
            Currency.getInstance(uiState.currencyCode)
        } catch (e: Exception) {
            null
        }
    }

    val filteredCurrencies = remember(uiState.currencySearchQuery, uiState.availableCurrencies) {
        if (uiState.currencySearchQuery.isBlank()) {
            uiState.availableCurrencies
        } else {
            uiState.availableCurrencies.filter {
                it.currencyCode.contains(uiState.currencySearchQuery, ignoreCase = true) ||
                        it.displayName.contains(uiState.currencySearchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Sharp.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Economy",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )

            ElevatedCard {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = "${uiState.currencyCode} (${selectedCurrency?.symbol ?: ""})",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Currency") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = {
                                expanded = false
                                onCurrencySearchQueryChanged("")
                            }
                        ) {
                            OutlinedTextField(
                                value = uiState.currencySearchQuery,
                                onValueChange = onCurrencySearchQueryChanged,
                                label = { Text("Search currency") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                singleLine = true
                            )

                            filteredCurrencies.forEach { currency ->
                                ListItem(
                                    headlineContent = { Text(currency.currencyCode) },
                                    supportingContent = { Text(currency.displayName) },
                                    trailingContent = { Text(currency.symbol) },
                                    modifier = Modifier.fillMaxWidth().clickable {
                                        onCurrencyChanged(currency.currencyCode)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = uiState.hourlyRateInput,
                        onValueChange = onHourlyRateChanged,
                        label = { Text("Hourly rate") },
                        prefix = {
                            Text(
                                text = selectedCurrency?.symbol ?: "$",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        isError = uiState.errorMessage != null,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (uiState.errorMessage != null) {
                        Text(
                            text = uiState.errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    val hourlyRate = uiState.hourlyRateInput.toDoubleOrNull() ?: 0.0
                    val formattedRate = try {
                        val format = java.text.NumberFormat.getCurrencyInstance().apply {
                            currency = selectedCurrency
                        }
                        format.format(hourlyRate)
                    } catch (e: Exception) {
                        "${selectedCurrency?.symbol ?: "$"}${String.format("%.2f", hourlyRate)}"
                    }

                    Text(
                        text = "Live Preview: Your time is currently valued at $formattedRate/hr",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Used to estimate the cost of drift time and the value of invested time. Stored locally.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = "Health",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            ElevatedCard {
                Column {
                    ListItem(
                        headlineContent = { Text("20/20/20 Rule Reminders") },
                        supportingContent = { Text("Get overlays and notifications to take breaks every 20 minutes.") },
                        trailingContent = {
                            Switch(
                                checked = uiState.healthFeaturesEnabled,
                                onCheckedChange = onHealthFeaturesToggled
                            )
                        }
                    )
                    ListItem(
                        headlineContent = { Text("Dark Environment Warning") },
                        supportingContent = { Text("Warn if screen brightness is too high in dark environments.") },
                        trailingContent = {
                            Switch(
                                checked = uiState.brightnessWarningEnabled,
                                onCheckedChange = onBrightnessWarningToggled
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { onSaveClicked(false) },
                enabled = !uiState.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(vertical = 8.dp)
            ) {
                Text(if (uiState.isSaving) "Saving..." else "Save")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (uiState.showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = onDismissConfirmation,
            title = { Text("Update Rate/Currency?") },
            text = { Text("Changing your rate or currency will update today's current totals. Do you wish to proceed?") },
            confirmButton = {
                TextButton(onClick = { onSaveClicked(true) }) {
                    Text("Proceed")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissConfirmation) {
                    Text("Cancel")
                }
            }
        )
    }
}
