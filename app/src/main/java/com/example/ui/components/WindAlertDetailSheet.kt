package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Beach
import com.example.data.model.WindType
import com.example.ui.theme.*
import com.example.util.WindAlertSeverity
import com.example.util.WindSafetyAlert
import com.example.util.WindSafetyAlertHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WindAlertDetailSheet(
    alert: WindSafetyAlert,
    allBeaches: List<Beach>,
    onBeachClick: (Beach) -> Unit,
    onDismissRequest: () -> Unit,
    onSimulateScenario: (WindType, WindAlertSeverity) -> Unit,
    onResetRealAemet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var notificationSentText by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableStateOf(0) } // 0: Refugio Seguro, 1: Expuestas/Peligro, 2: Consejos y Test

    val shelteredBeaches = remember(alert, allBeaches) {
        allBeaches.filter { it.id in alert.shelteredBeachIds }
    }
    val exposedBeaches = remember(alert, allBeaches) {
        allBeaches.filter { it.id in alert.exposedBeachIds }
    }

    val isDanger = alert.severity == WindAlertSeverity.STRONG_DANGER

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = modifier.testTag("wind_alert_detail_sheet")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Warning Title and Severity
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isDanger) FlagRedContainer else WindPonienteContainer,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isDanger) Icons.Default.Warning else Icons.Default.Air,
                                    contentDescription = null,
                                    tint = if (isDanger) FlagRed else WindPoniente,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = alert.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = alert.severity.displayName,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDanger) FlagRed else FlagYellow
                            )
                        }
                    }
                }
            }

            // Key Metrics Card (Wind Speed, Direction, Waves, Zone)
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MetricItem(
                            label = "Velocidad",
                            value = "${alert.windSpeedKmh} km/h",
                            subtext = "Viento medio",
                            icon = Icons.Default.Speed
                        )
                        MetricItem(
                            label = "Oleaje Est.",
                            value = "${String.format("%.1f", alert.waveHeightMeters)} m",
                            subtext = "Costa abierta",
                            icon = Icons.Default.Waves
                        )
                        MetricItem(
                            label = "Dirección",
                            value = alert.windType.label,
                            subtext = alert.cardinalDirection,
                            icon = Icons.Default.Navigation
                        )
                    }
                }
            }

            // Swimming Impact Description Card
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isDanger) FlagRedContainer.copy(alpha = 0.5f) else AmberContainerLight.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, if (isDanger) FlagRed.copy(alpha = 0.3f) else AmberTertiary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Pool,
                                contentDescription = null,
                                tint = if (isDanger) FlagRed else AmberTertiary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Impacto Directo en el Baño",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isDanger) FlagRed else AmberOnContainer
                            )
                        }

                        Text(
                            text = alert.swimmingImpactDescription,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            // Tabs Selector: Refugio vs Expuestas vs Configuración/Test
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                text = "🛡️ Refugio (${shelteredBeaches.size})",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                text = "⚠️ Peligro (${exposedBeaches.size})",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            Text(
                                text = "⚙️ Consejos/Test",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Content based on selected Tab
            when (selectedTab) {
                0 -> {
                    item {
                        Text(
                            text = "Playas resguardadas del viento actual donde el mar está en calma y el baño es seguro:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    items(shelteredBeaches) { beach ->
                        ShelteredBeachRow(
                            beach = beach,
                            onClick = {
                                onDismissRequest()
                                onBeachClick(beach)
                            }
                        )
                    }
                }
                1 -> {
                    item {
                        Text(
                            text = "Playas orientadas de frente al viento. Alto riesgo de resaca, oleaje rompiente y banderas amarillas/rojas:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    items(exposedBeaches) { beach ->
                        ExposedBeachRow(
                            beach = beach,
                            onClick = {
                                onDismissRequest()
                                onBeachClick(beach)
                            }
                        )
                    }
                }
                2 -> {
                    // Safety Tips List
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Recomendaciones de Seguridad:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            alert.safetyRecommendations.forEach { tip ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text("•", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = tip,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    // Native Notification Trigger & Test Controls
                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Text(
                            text = "Herramientas de Notificación y Simulación:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    item {
                        Button(
                            onClick = {
                                WindSafetyAlertHelper.sendWindAlertNotification(context, alert)
                                notificationSentText = "¡Notificación de alerta enviada al sistema!"
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("trigger_notification_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Outlined.Notifications, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Enviar Notificación de Prueba al Móvil")
                        }
                    }

                    if (notificationSentText != null) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = FlagGreenContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = notificationSentText!!,
                                    color = FlagGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Simular escenarios de viento para comprobar alertas:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onSimulateScenario(WindType.LEVANTE, WindAlertSeverity.STRONG_DANGER) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Levante 38 km/h", fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = { onSimulateScenario(WindType.PONIENTE, WindAlertSeverity.STRONG_DANGER) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Poniente 34 km/h", fontSize = 11.sp)
                            }
                        }
                    }

                    item {
                        FilledTonalButton(
                            onClick = {
                                onResetRealAemet()
                                notificationSentText = "Restablecido a datos AEMET reales"
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Restablecer a Datos Reales AEMET")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricItem(
    label: String,
    value: String,
    subtext: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = subtext,
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun ShelteredBeachRow(
    beach: Beach,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = FlagGreenContainer.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, FlagGreen.copy(alpha = 0.3f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = beach.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = FlagGreenContainer
                    ) {
                        Text(
                            text = "🛡️ Protegida",
                            color = FlagGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = "${beach.municipality} • Orientación: ${beach.orientation.label}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Ver playa",
                tint = FlagGreen
            )
        }
    }
}

@Composable
private fun ExposedBeachRow(
    beach: Beach,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = FlagRedContainer.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, FlagRed.copy(alpha = 0.25f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = beach.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = FlagRedContainer
                    ) {
                        Text(
                            text = "⚠️ Oleaje / Resaca",
                            color = FlagRed,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = "${beach.municipality} • Expuesta al viento (${beach.orientation.label})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Ver playa",
                tint = FlagRed
            )
        }
    }
}
