package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.BeachCard
import com.example.ui.components.FlagBadge
import com.example.ui.components.WeatherMarineCard
import com.example.ui.components.WindAlertBanner
import com.example.ui.components.WindAlertDetailSheet
import com.example.ui.components.WindBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.BeachUiState
import com.example.util.WindAlertSeverity
import com.example.util.WindSafetyAlertHelper
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarineForecastScreen(
    uiState: BeachUiState,
    getBathingAlert: (Beach) -> BathingSafetyAlert,
    onBeachClick: (Beach) -> Unit,
    onFavoriteToggle: (String) -> Unit,
    onRefresh: () -> Unit,
    onDismissWindAlert: () -> Unit,
    onOpenWindAlertSheet: () -> Unit,
    onCloseWindAlertSheet: () -> Unit,
    onFilterShelteredOnly: () -> Unit,
    onSimulateWindScenario: (WindType, WindAlertSeverity) -> Unit,
    onResetRealAemet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedZone by remember { mutableStateOf(Zone.CABO_DE_GATA) }
    val forecast = uiState.marineForecasts[selectedZone]

    val zoneBeaches = uiState.allBeaches.filter { it.zone == selectedZone }
    val protectedBeaches = zoneBeaches.filter { getBathingAlert(it).isProtectedFromCurrentWind }
    val exposedBeaches = zoneBeaches.filter { !getBathingAlert(it).isProtectedFromCurrentWind }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Previsión Marítima AEMET",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Viento de Levante / Poniente y Estado del Mar",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar AEMET")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.testTag("marine_forecast_screen")
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Active Wind Safety Alert Banner if triggered
            item {
                WindAlertBanner(
                    alert = uiState.activeWindAlert,
                    isDismissed = uiState.isWindAlertDismissed,
                    onDismiss = onDismissWindAlert,
                    onViewDetails = onOpenWindAlertSheet,
                    onFilterShelteredOnly = onFilterShelteredOnly
                )
            }

            // Zone Selector Tabs
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(Zone.values()) { zone ->
                        FilterChip(
                            selected = selectedZone == zone,
                            onClick = { selectedZone = zone },
                            label = { Text(zone.displayName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }

            // Main AEMET Weather Marine Card
            item {
                WeatherMarineCard(
                    forecast = forecast,
                    isLoading = uiState.isLoadingForecast,
                    onRefreshClick = onRefresh
                )
            }

            // Dedicated Wind Alert & Swimming Safety Controller Card
            item {
                WindAlertControllerCard(
                    alert = uiState.activeWindAlert,
                    onOpenDetails = onOpenWindAlertSheet,
                    onSendNotification = {
                        uiState.activeWindAlert?.let {
                            WindSafetyAlertHelper.sendWindAlertNotification(context, it)
                        }
                    },
                    onSimulateLevante = {
                        onSimulateWindScenario(WindType.LEVANTE, WindAlertSeverity.STRONG_DANGER)
                    },
                    onSimulatePoniente = {
                        onSimulateWindScenario(WindType.PONIENTE, WindAlertSeverity.STRONG_DANGER)
                    },
                    onResetAemet = onResetRealAemet
                )
            }

            // Wind Explanation Section (Levante vs Poniente in Almería)
            item {
                WindExplainerCard(windType = forecast?.currentWindType ?: WindType.LEVANTE)
            }

            // Section: Best Beaches Today (Protected from current wind)
            item {
                SectionHeader(
                    title = "🛡️ Playas Recomendadas Hoy en ${selectedZone.displayName}",
                    subtitle = "Resguardadas del viento actual con mar en calma",
                    count = protectedBeaches.size,
                    accentColor = FlagGreen
                )
            }

            if (protectedBeaches.isEmpty()) {
                item {
                    Text(
                        text = "No se encontraron playas resguardadas en esta zona para las condiciones actuales.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(protectedBeaches) { beach ->
                    BeachCard(
                        beach = beach,
                        bathingAlert = getBathingAlert(beach),
                        isFavorite = uiState.favorites.any { it.id == beach.id },
                        onBeachClick = onBeachClick,
                        onFavoriteToggle = onFavoriteToggle
                    )
                }
            }

            // Section: Beaches with swell or direct wind
            if (exposedBeaches.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    SectionHeader(
                        title = "⚠️ Playas con Oleaje o Viento Directo",
                        subtitle = "Bañarse con precaución o evitar deportes como snorkel",
                        count = exposedBeaches.size,
                        accentColor = FlagYellow
                    )
                }

                items(exposedBeaches) { beach ->
                    BeachCard(
                        beach = beach,
                        bathingAlert = getBathingAlert(beach),
                        isFavorite = uiState.favorites.any { it.id == beach.id },
                        onBeachClick = onBeachClick,
                        onFavoriteToggle = onFavoriteToggle
                    )
                }
            }

            // Snorkel & Coastal Safety Tips Card
            item {
                CoastalTipsCard()
            }
        }
    }

    if (uiState.isWindAlertSheetOpen && uiState.activeWindAlert != null) {
        WindAlertDetailSheet(
            alert = uiState.activeWindAlert,
            allBeaches = uiState.allBeaches,
            onBeachClick = onBeachClick,
            onDismissRequest = onCloseWindAlertSheet,
            onSimulateScenario = onSimulateWindScenario,
            onResetRealAemet = onResetRealAemet
        )
    }
}

@Composable
fun WindAlertControllerCard(
    alert: com.example.util.WindSafetyAlert?,
    onOpenDetails: () -> Unit,
    onSendNotification: () -> Unit,
    onSimulateLevante: () -> Unit,
    onSimulatePoniente: () -> Unit,
    onResetAemet: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Alertas de Viento para el Baño",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (alert != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (alert.severity == WindAlertSeverity.STRONG_DANGER) FlagRedContainer else FlagYellowContainer
                    ) {
                        Text(
                            text = if (alert.severity == WindAlertSeverity.STRONG_DANGER) "Alerta Roja" else "Aviso Amarillo",
                            color = if (alert.severity == WindAlertSeverity.STRONG_DANGER) FlagRed else FlagYellow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Text(
                text = if (alert != null) {
                    "Se ha detectado viento de ${alert.windType.label} (${alert.windSpeedKmh} km/h). Hay ${alert.exposedBeachIds.size} playas con oleaje y ${alert.shelteredBeachIds.size} playas seguras."
                } else {
                    "El viento actual no genera riesgo para el baño. Puedes enviar notificaciones al dispositivo o simular temporales de Levante y Poniente."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (alert != null) {
                    Button(
                        onClick = onOpenDetails,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Ver Análisis", fontSize = 12.sp)
                    }
                }

                FilledTonalButton(
                    onClick = onSendNotification,
                    modifier = Modifier.weight(1f),
                    enabled = alert != null
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Notificar Móvil", fontSize = 12.sp)
                }
            }

            // Quick simulation chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Test:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FilterChip(
                    selected = alert?.windType == WindType.LEVANTE && alert.isSimulated,
                    onClick = onSimulateLevante,
                    label = { Text("Levante 38 km/h", fontSize = 10.sp) }
                )
                FilterChip(
                    selected = alert?.windType == WindType.PONIENTE && alert.isSimulated,
                    onClick = onSimulatePoniente,
                    label = { Text("Poniente 34 km/h", fontSize = 10.sp) }
                )
                if (alert?.isSimulated == true) {
                    IconButton(
                        onClick = onResetAemet,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Restablecer AEMET", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String,
    count: Int,
    accentColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "$count playas",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }
        }
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun WindExplainerCard(windType: WindType) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Explore,
                    contentDescription = null,
                    tint = MarineCyan
                )
                Text(
                    text = "¿Cómo afecta el viento en Almería?",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = when (windType) {
                    WindType.LEVANTE -> "🌬️ Con LEVANTE (viento del Este): Las calas orientadas al Este (Los Muertos, Las Negras, Agua Amarga, Mojácar) reciben fuerte oleaje y corrientes. Sin embargo, las playas del lado Oeste del Cabo (Mónsul, Genoveses, Cala Rajá, El Zapillo) disfrutan de aguas completamente calmas y cristalinas."
                    WindType.PONIENTE -> "🌬️ Con PONIENTE (viento del Oeste): Las playas de Cabo de Gata y Levante que miran al Este (Cala de Enmedio, Los Muertos, Genoveses, Agua Amarga) tienen el mar liso como un espejo porque el viento sopla de tierra hacia el mar. ¡Es el día perfecto para snorkel en el Levante!"
                    WindType.CALM -> "🌊 Con CALMA: Cualquier playa de Almería y Cabo de Gata es ideal hoy para el baño y snorkel con visibilidad inmejorable."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun CoastalTipsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = CoralAccent
                )
                Text(
                    text = "Consejos de Seguridad y Snorkel en Almería",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            val tips = listOf(
                "🤿 Snorkel con Posidonia: Las praderas de Posidonia son Patrimonio protegido. No arranques plantas ni toques la fauna.",
                "🚩 Respeta siempre las banderas de la playa: En Playa de los Muertos el desnivel es muy brusco y la resaca es fuerte con oleaje.",
                "🧴 Protección solar ecológica: Utiliza protectores solares 'Reef Safe' (sin oxibenzona) para preservar los fondos marinos de Cabo de Gata.",
                "💧 Lleva agua y calzado escarpines: En las calas vírgenes no hay chiringuitos y muchas tienen accesos rocosos o grava fina."
            )

            tips.forEach { tip ->
                Text(
                    text = tip,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 17.sp
                )
            }
        }
    }
}
