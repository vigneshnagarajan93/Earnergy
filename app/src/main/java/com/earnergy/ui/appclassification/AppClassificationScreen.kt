package com.earnergy.ui.appclassification

import androidx.compose.foundation.Image
import com.earnergy.domain.model.AppRole
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.ArrowBack
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.earnergy.ui.common.GlassChip
import com.earnergy.ui.common.GlassSurface
import android.graphics.drawable.Drawable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext



@Composable
fun AppClassificationScreen(
    uiState: AppClassificationUiState,
    onRoleChanged: (packageName: String, newRole: AppRole) -> Unit,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(AppClassificationTab.All) }
    val filteredApps = remember(uiState.apps, selectedTab) {
        filterApps(uiState.apps, selectedTab)
    }
    
    val (userApps, systemApps) = remember(filteredApps) {
        filteredApps.partition { !it.isSystemApp }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF03040A), Color(0xFF10162A))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GlassSurface {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Sharp.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Text(
                        text = "App roles",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppClassificationTab.values().forEach { tab ->
                    GlassChip(
                        label = tab.title,
                        selected = tab == selectedTab,
                        selectedColor = when (tab) {
                            AppClassificationTab.Invested -> Color(0xFFFCD34D)
                            AppClassificationTab.Drift -> Color(0xFFF87171)
                            AppClassificationTab.All -> Color.White
                        },
                        onClick = { selectedTab = tab }
                    )
                }
            }

            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(text = "Loading…", color = Color.White)
                    }
                }
                uiState.errorMessage != null -> {
                    Text(
                        text = uiState.errorMessage,
                        color = Color(0xFFF87171),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(userApps, key = { it.packageName }) { app ->
                            AppItem(app, onRoleChanged)
                        }
                        
                        if (systemApps.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "System Apps",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            items(systemApps, key = { it.packageName }) { app ->
                                AppItem(app, onRoleChanged)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppItem(
    app: AppClassificationItem,
    onRoleChanged: (packageName: String, newRole: AppRole) -> Unit
) {
    GlassSurface(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AppIcon(packageName = app.packageName, appName = app.appName)
                Column {
                    Text(
                        text = app.appName,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Text(
                        text = formatMinutes(app.todayMinutes),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GlassChip(
                    label = "Invested",
                    selected = app.role == AppRole.INVESTED,
                    selectedColor = Color(0xFFFCD34D),
                    onClick = {
                        if (app.role != AppRole.INVESTED) {
                            onRoleChanged(app.packageName, AppRole.INVESTED)
                        }
                    }
                )
                GlassChip(
                    label = "Drift",
                    selected = app.role == AppRole.DRIFT,
                    selectedColor = Color(0xFFF87171),
                    onClick = {
                        if (app.role != AppRole.DRIFT) {
                            onRoleChanged(app.packageName, AppRole.DRIFT)
                        }
                    }
                )
                GlassChip(
                    label = "Ignored",
                    selected = app.role == AppRole.IGNORED,
                    selectedColor = Color.White,
                    onClick = {
                        if (app.role != AppRole.IGNORED) {
                            onRoleChanged(app.packageName, AppRole.IGNORED)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun AppIcon(packageName: String, appName: String) {
    val context = LocalContext.current
    var icon by remember(packageName) { mutableStateOf<Drawable?>(null) }
    
    LaunchedEffect(packageName) {
        withContext(Dispatchers.IO) {
            try {
                icon = context.packageManager.getApplicationIcon(packageName)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    if (icon != null) {
        Image(
            painter = rememberAsyncImagePainter(icon),
            contentDescription = "$appName icon",
            modifier = Modifier.size(40.dp)
        )
    } else {
        Icon(
            imageVector = Icons.Default.Apps,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(40.dp)
        )
    }
}

private fun filterApps(apps: List<AppClassificationItem>, tab: AppClassificationTab): List<AppClassificationItem> {
    return when (tab) {
        AppClassificationTab.All -> apps
        AppClassificationTab.Invested -> apps.filter { it.role == AppRole.INVESTED }
        AppClassificationTab.Drift -> apps.filter { it.role == AppRole.DRIFT }
    }
}

private fun formatMinutes(minutes: Int): String {
    val hours = minutes / 60
    val remainingMinutes = minutes % 60
    return if (hours > 0) "${hours}h ${remainingMinutes}m" else "${remainingMinutes}m"
}

enum class AppClassificationTab(val title: String) {
    All("All"),
    Invested("Invested"),
    Drift("Drift")
}
