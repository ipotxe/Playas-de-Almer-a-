package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.BeachCard
import com.example.ui.components.ForecastDaySelector
import com.example.ui.components.WindAlertBanner
import com.example.ui.components.WindAlertDetailSheet
import com.example.ui.theme.*
import com.example.ui.viewmodel.BeachSortOrder
import com.example.ui.viewmodel.BeachUiState
import com.example.util.WindAlertSeverity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    uiState: BeachUiState,
    getBathingAlert: (Beach) -> BathingSafetyAlert,
    onDaySelected: (ForecastDay) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onZoneSelected: (Zone?) -> Unit,
    onToggleTopSnorkel: () -> Unit,
    onToggleGreenFlag: () -> Unit,
    onToggleWindProtected: () -> Unit,
    onToggleVirginCoves: () -> Unit,
    onToggleLifeguard: () -> Unit,
    onToggleNudist: () -> Unit,
    onToggleFamily: () -> Unit,
    onSportSelected: (WaterSport?) -> Unit,
    onSortOrderChanged: (BeachSortOrder) -> Unit,
    onResetFilters: () -> Unit,
    onBeachClick: (Beach) -> Unit,
    onFavoriteToggle: (String) -> Unit,
    onRefreshForecast: () -> Unit,
    onDismissWindAlert: () -> Unit,
    onOpenWindAlertSheet: () -> Unit,
    onCloseWindAlertSheet: () -> Unit,
    onFilterShelteredOnly: () -> Unit,
    onSimulateWindScenario: (WindType, WindAlertSeverity) -> Unit,
    onResetRealAemet: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSortSheet by remember { mutableStateOf(false) }
    var showFiltersSheet by remember { mutableStateOf(false) }

    val activeFiltersCount = listOf(
        uiState.filterState.onlyTopSnorkel,
        uiState.filterState.onlyGreenFlagToday,
        uiState.filterState.onlyWindProtectedToday,
        uiState.filterState.onlyVirginCoves,
        uiState.filterState.onlyWithLifeguard,
        uiState.filterState.onlyNudist,
        uiState.filterState.onlyFamily,
        uiState.filterState.selectedSport != null
    ).count { it }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Playas de Almería",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${uiState.filteredBeaches.size} calas y playas encontradas",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showSortSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "Ordenar resultados"
                        )
                    }
                    IconButton(onClick = onRefreshForecast) {
                        if (uiState.isLoadingForecast) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MarineCyan
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Actualizar previsión AEMET"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.testTag("explore_screen")
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Active AEMET Wind / Swimming Safety Alert Banner
            item {
                WindAlertBanner(
                    alert = uiState.activeWindAlert,
                    isDismissed = uiState.isWindAlertDismissed,
                    onDismiss = onDismissWindAlert,
                    onViewDetails = onOpenWindAlertSheet,
                    onFilterShelteredOnly = onFilterShelteredOnly
                )
            }

            // Search Input Field
            item {
                OutlinedTextField(
                    value = uiState.filterState.searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("search_beach_input"),
                    placeholder = { Text("Buscar cala, pueblo, snorkel, kayak...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (uiState.filterState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpiar búsqueda")
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    singleLine = true
                )
            }

            // 3-Day Forecast Day Selector (Hoy / Mañana / Pasado mañana)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PREDICCIÓN DEL ESTADO DEL MAR",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = uiState.selectedForecastDay.getDisplayDate(),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    ForecastDaySelector(
                        selectedDay = uiState.selectedForecastDay,
                        onDaySelected = onDaySelected
                    )
                }
            }

            // Zone Selection Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = uiState.filterState.selectedZone == null,
                            onClick = { onZoneSelected(null) },
                            label = { Text("Todas (${uiState.allBeaches.size})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                    items(Zone.values()) { zone ->
                        FilterChip(
                            selected = uiState.filterState.selectedZone == zone,
                            onClick = { onZoneSelected(zone) },
                            label = { Text(zone.displayName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }

            // Quick Filter Chips (Wind protected today, Green flag, Snorkel 4.5+, Virgin, Filters button)
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = uiState.filterState.onlyWindProtectedToday,
                            onClick = onToggleWindProtected,
                            label = { Text("🛡️ Protegida hoy") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FlagGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = uiState.filterState.onlyGreenFlagToday,
                            onClick = onToggleGreenFlag,
                            label = { Text("🟢 Bandera verde") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FlagGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = uiState.filterState.onlyTopSnorkel,
                            onClick = onToggleTopSnorkel,
                            label = { Text("🤿 Top Snorkel (4.5+)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TurquoiseSecondary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = uiState.filterState.onlyVirginCoves,
                            onClick = onToggleVirginCoves,
                            label = { Text("🏖️ Vírgenes") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = uiState.filterState.onlyFamily,
                            onClick = onToggleFamily,
                            label = { Text("👨‍👩‍👦 Familiar") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = uiState.filterState.onlyNudist,
                            onClick = onToggleNudist,
                            label = { Text("☀️ Naturista") }
                        )
                    }
                    item {
                        IconButton(
                            onClick = { showFiltersSheet = true },
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (activeFiltersCount > 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                .size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Más filtros",
                                tint = if (activeFiltersCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Active Sort & Results Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Orden: ${uiState.filterState.sortOrder.label}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { showSortSheet = true }
                    )

                    if (activeFiltersCount > 0 || uiState.filterState.selectedZone != null || uiState.filterState.searchQuery.isNotEmpty()) {
                        TextButton(
                            onClick = onResetFilters,
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Restablecer filtros", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Beaches List
            if (uiState.filteredBeaches.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "No se encontraron playas con estos filtros",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Prueba a relajar los filtros de viento, zona o término de búsqueda.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = onResetFilters,
                            colors = ButtonDefaults.buttonColors(containerColor = MarineCyan)
                        ) {
                            Text("Ver todas las playas")
                        }
                    }
                }
            } else {
                items(uiState.filteredBeaches, key = { it.id }) { beach ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        BeachCard(
                            beach = beach,
                            bathingAlert = getBathingAlert(beach),
                            isFavorite = uiState.favorites.any { it.id == beach.id },
                            onBeachClick = onBeachClick,
                            onFavoriteToggle = onFavoriteToggle
                        )
                    }
                }
            }
        }
    }

    // Sort Order Bottom Sheet
    if (showSortSheet) {
        ModalBottomSheet(onDismissRequest = { showSortSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Ordenar Playas por",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                BeachSortOrder.values().forEach { order ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                onSortOrderChanged(order)
                                showSortSheet = false
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = order.label,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (uiState.filterState.sortOrder == order) FontWeight.Bold else FontWeight.Normal
                        )
                        if (uiState.filterState.sortOrder == order) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = MarineCyan)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Advanced Filters Sheet
    if (showFiltersSheet) {
        ModalBottomSheet(onDismissRequest = { showFiltersSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filtros de Actividades y Servicios",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = {
                        onResetFilters()
                        showFiltersSheet = false
                    }) {
                        Text("Limpiar")
                    }
                }

                Text(
                    text = "Filtrar por Deporte Acuático:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(WaterSport.values()) { sport ->
                        FilterChip(
                            selected = uiState.filterState.selectedSport == sport,
                            onClick = { onSportSelected(sport) },
                            label = { Text(sport.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MarineCyan,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Divider()

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterSwitchRow(
                        title = "Socorrismo y vigilancia",
                        checked = uiState.filterState.onlyWithLifeguard,
                        onCheckedChange = { onToggleLifeguard() }
                    )
                    FilterSwitchRow(
                        title = "Playa naturista / nudista",
                        checked = uiState.filterState.onlyNudist,
                        onCheckedChange = { onToggleNudist() }
                    )
                    FilterSwitchRow(
                        title = "Recomendada para familias",
                        checked = uiState.filterState.onlyFamily,
                        onCheckedChange = { onToggleFamily() }
                    )
                }

                Button(
                    onClick = { showFiltersSheet = false },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MarineCyan)
                ) {
                    Text("Aplicar Filtros (${uiState.filteredBeaches.size} playas)")
                }
                Spacer(modifier = Modifier.height(16.dp))
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
fun FilterSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
