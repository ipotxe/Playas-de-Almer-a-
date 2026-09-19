package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.*
import com.example.ui.components.BeachImageView
import com.example.ui.components.FlagBadge
import com.example.ui.components.ForecastDaySelector
import com.example.ui.components.SnorkelRatingBar
import com.example.ui.components.WindBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.BeachUiState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

// Geographic bounding box for Almería coast
private const val MIN_LAT = 36.65
private const val MAX_LAT = 37.45
private const val MIN_LNG = -2.75
private const val MAX_LNG = -1.55

@Composable
fun InteractiveMapScreen(
    uiState: BeachUiState,
    getBathingAlert: (Beach) -> BathingSafetyAlert,
    onDaySelected: (ForecastDay) -> Unit,
    onBeachSelected: (Beach) -> Unit,
    onFavoriteToggle: (String) -> Unit,
    onZoneFilterChanged: (Zone?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedMapBeach by remember { mutableStateOf<Beach?>(null) }

    // Google Maps & Map Mode State
    val almeriaCenter = remember { LatLng(36.85, -2.15) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(almeriaCenter, 9.8f)
    }
    val coroutineScope = rememberCoroutineScope()
    var useGoogleMaps by remember { mutableStateOf(true) }
    var mapType by remember { mutableStateOf(MapType.NORMAL) }
    var showLayersMenu by remember { mutableStateOf(false) }

    val handleZoneSelected: (Zone?) -> Unit = { zone ->
        onZoneFilterChanged(zone)
        if (useGoogleMaps) {
            val (targetPos, targetZoom) = when (zone) {
                Zone.CABO_DE_GATA -> LatLng(36.80, -2.12) to 11.2f
                Zone.PONIENTE_ALMERIENSE -> LatLng(36.72, -2.68) to 11.2f
                Zone.ALMERIA_CAPITAL -> LatLng(36.82, -2.43) to 12.0f
                Zone.LEVANTE_ALMERIENSE -> LatLng(37.15, -1.85) to 11.0f
                null -> almeriaCenter to 9.8f
            }
            coroutineScope.launch {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(targetPos, targetZoom),
                    durationMs = 700
                )
            }
        }
    }

    // Map Quick Filter State
    var onlyProtectedFilter by remember { mutableStateOf(false) }
    var onlyGreenFlagFilter by remember { mutableStateOf(false) }
    var onlyTopSnorkelFilter by remember { mutableStateOf(false) }

    // Nautical Vector Chart Pan & Zoom state (for vector mode)
    var scale by remember { mutableStateOf(1.0f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    // Active zone forecast for wind direction
    val currentZone = uiState.filterState.selectedZone ?: Zone.CABO_DE_GATA
    val currentForecast = uiState.marineForecasts[currentZone]

    // Filter beaches displayed on map based on zone & quick toggles
    val displayedBeaches = remember(
        uiState.allBeaches,
        uiState.filterState.selectedZone,
        onlyProtectedFilter,
        onlyGreenFlagFilter,
        onlyTopSnorkelFilter,
        uiState.marineForecasts
    ) {
        uiState.allBeaches.filter { beach ->
            val zoneMatch = uiState.filterState.selectedZone == null || beach.zone == uiState.filterState.selectedZone
            val alert = getBathingAlert(beach)
            val protectedMatch = !onlyProtectedFilter || alert.isProtectedFromCurrentWind
            val flagMatch = !onlyGreenFlagFilter || alert.flag == FlagColor.GREEN
            val snorkelMatch = !onlyTopSnorkelFilter || beach.snorkelRating >= 4.5f

            zoneMatch && protectedMatch && flagMatch && snorkelMatch
        }
    }

    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("interactive_map_screen")
    ) {
        if (useGoogleMaps) {
            // Google Maps View with Interactive Beach Markers
            GoogleMap(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("google_map_view"),
                cameraPositionState = cameraPositionState,
                properties = remember(mapType) {
                    MapProperties(
                        mapType = mapType,
                        isMyLocationEnabled = false
                    )
                },
                uiSettings = remember {
                    MapUiSettings(
                        zoomControlsEnabled = false,
                        compassEnabled = true,
                        myLocationButtonEnabled = false,
                        mapToolbarEnabled = true
                    )
                },
                onMapClick = {
                    selectedMapBeach = null
                }
            ) {
                for (beach in displayedBeaches) {
                    val alert = getBathingAlert(beach)
                    val markerHue = when (alert.flag) {
                        FlagColor.GREEN -> BitmapDescriptorFactory.HUE_GREEN
                        FlagColor.YELLOW -> BitmapDescriptorFactory.HUE_YELLOW
                        FlagColor.RED -> BitmapDescriptorFactory.HUE_RED
                    }
                    val beachLatLng = remember(beach.latitude, beach.longitude) {
                        LatLng(beach.latitude, beach.longitude)
                    }

                    Marker(
                        state = rememberMarkerState(key = beach.id, position = beachLatLng),
                        title = beach.name,
                        snippet = "${beach.municipality} • ${alert.flag.label} • ${if (alert.isProtectedFromCurrentWind) "🛡️ Protegida" else "💨 Expuesta"} • Snorkel: ${beach.snorkelRating}★",
                        icon = BitmapDescriptorFactory.defaultMarker(markerHue),
                        onClick = {
                            selectedMapBeach = beach
                            coroutineScope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngZoom(beachLatLng, 13.5f),
                                    durationMs = 500
                                )
                            }
                            false
                        },
                        onInfoWindowClick = {
                            onBeachSelected(beach)
                        }
                    )
                }
            }
        } else {
            // Interactive Canvas Nautical Chart Map with Pins
            Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.75f, 4.5f)
                        offsetX += pan.x
                        offsetY += pan.y
                    }
                }
                .pointerInput(displayedBeaches, scale, offsetX, offsetY) {
                    detectTapGestures { tapOffset ->
                        val canvasWidth = size.width
                        val canvasHeight = size.height

                        var clickedBeach: Beach? = null
                        var minDistance = Float.MAX_VALUE

                        // Find closest beach marker within touch target
                        for (beach in displayedBeaches) {
                            val markerPos = geoToScreen(
                                beach.latitude,
                                beach.longitude,
                                canvasWidth.toFloat(),
                                canvasHeight.toFloat(),
                                scale,
                                offsetX,
                                offsetY
                            )
                            val dist = (markerPos - tapOffset).getDistance()
                            if (dist < 44.dp.toPx() && dist < minDistance) {
                                minDistance = dist
                                clickedBeach = beach
                            }
                        }

                        selectedMapBeach = clickedBeach
                    }
                }
        ) {
            val width = size.width
            val height = size.height

            // 1. Mediterranean Sea Background (Deep Oceanic Gradient)
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF00293B),
                        Color(0xFF004866),
                        Color(0xFF006484),
                        Color(0xFF0284C7).copy(alpha = 0.85f)
                    )
                )
            )

            // 2. Almería Continental Land Mass
            drawCoastlineShape(width, height, scale, offsetX, offsetY)

            // 3. Wind Flow Vector Arrows across the sea
            if (currentForecast != null) {
                drawWindVectors(
                    width,
                    height,
                    currentForecast.windDirectionDegrees,
                    currentForecast.currentWindType,
                    currentForecast.windSpeedKmh
                )
            }

            // 4. Draw Beach Pins on the Map
            for (beach in displayedBeaches) {
                val alert = getBathingAlert(beach)
                val pos = geoToScreen(beach.latitude, beach.longitude, width, height, scale, offsetX, offsetY)

                if (pos.x in -60f..width + 60f && pos.y in -60f..height + 60f) {
                    val isSelected = selectedMapBeach?.id == beach.id
                    drawBeachPin(
                        pos = pos,
                        beach = beach,
                        alert = alert,
                        isSelected = isSelected
                    )

                    // Draw beach name label when selected or when zoomed in
                    if (isSelected || scale >= 2.2f) {
                        val textLayoutResult = textMeasurer.measure(
                            text = beach.name,
                            style = TextStyle(
                                fontSize = if (isSelected) 12.sp else 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = Color.White
                            )
                        )
                        val textWidth = textLayoutResult.size.width.toFloat()
                        val textHeight = textLayoutResult.size.height.toFloat()

                        val labelOffset = Offset(
                            pos.x - textWidth / 2f,
                            pos.y - textHeight - (if (isSelected) 22.dp.toPx() else 16.dp.toPx())
                        )

                        // Label Background Pill
                        drawRoundRect(
                            color = if (isSelected) Color(0xFF0284C7) else Color(0xCC001E2B),
                            topLeft = Offset(labelOffset.x - 6.dp.toPx(), labelOffset.y - 3.dp.toPx()),
                            size = Size(textWidth + 12.dp.toPx(), textHeight + 6.dp.toPx()),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )

                        drawText(
                            textLayoutResult = textLayoutResult,
                            topLeft = labelOffset
                        )
                    }
                }
            }
        }
        }

        // Top Control Overlay: Zone Chips + Quick Filters + Wind Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 3-Day Forecast Day Selector
            ForecastDaySelector(
                selectedDay = uiState.selectedForecastDay,
                onDaySelected = onDaySelected,
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
            )

            // Zone Selector Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = uiState.filterState.selectedZone == null,
                        onClick = { handleZoneSelected(null) },
                        label = { Text("Toda la costa (${displayedBeaches.size})", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                        )
                    )
                }
                items(Zone.values()) { zone ->
                    FilterChip(
                        selected = uiState.filterState.selectedZone == zone,
                        onClick = { handleZoneSelected(zone) },
                        label = { Text(zone.displayName, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                        )
                    )
                }
            }

            // Quick Filters Row (Protected / Green / Snorkel)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = onlyProtectedFilter,
                        onClick = { onlyProtectedFilter = !onlyProtectedFilter },
                        label = { Text("🛡️ Protegidas hoy", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FlagGreen,
                            selectedLabelColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = onlyGreenFlagFilter,
                        onClick = { onlyGreenFlagFilter = !onlyGreenFlagFilter },
                        label = { Text("🚩 Bandera Verde", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FlagGreen,
                            selectedLabelColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = onlyTopSnorkelFilter,
                        onClick = { onlyTopSnorkelFilter = !onlyTopSnorkelFilter },
                        label = { Text("🤿 Snorkel 4.5+", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TurquoiseSecondary,
                            selectedLabelColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
                        )
                    )
                }
            }

            // Active Map Engine & Layer Indicator Bar
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (useGoogleMaps) Icons.Default.Map else Icons.Default.Explore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = if (useGoogleMaps) {
                                when (mapType) {
                                    MapType.NORMAL -> "Google Maps • Estándar"
                                    MapType.SATELLITE -> "Google Maps • Vista Satélite"
                                    MapType.TERRAIN -> "Google Maps • Relieve"
                                    else -> "Google Maps • Híbrido"
                                }
                            } else "Carta Náutica (Flujo de viento)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (useGoogleMaps) {
                            TextButton(
                                onClick = {
                                    mapType = if (mapType == MapType.NORMAL) MapType.SATELLITE else MapType.NORMAL
                                },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                modifier = Modifier.height(26.dp)
                            ) {
                                Text(
                                    text = if (mapType == MapType.NORMAL) "Satélite" else "Mapa",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        TextButton(
                            onClick = { useGoogleMaps = !useGoogleMaps },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Text(
                                text = if (useGoogleMaps) "Carta Viento" else "Google Maps",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }

            // Live Wind & Safety Legend Bar
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                shadowElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentForecast != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (currentForecast.currentWindType == WindType.LEVANTE) WindLevanteContainer
                                        else WindPonienteContainer
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Navigation,
                                    contentDescription = null,
                                    tint = if (currentForecast.currentWindType == WindType.LEVANTE) WindLevante else WindPoniente,
                                    modifier = Modifier
                                        .size(12.dp)
                                        .rotate(currentForecast.windDirectionDegrees)
                                )
                            }
                            Text(
                                text = "Viento: ${currentForecast.currentWindType.label} (${currentForecast.windSpeedKmh} km/h)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Flag dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LegendDot(color = FlagGreen, label = "Seguro")
                        LegendDot(color = FlagYellow, label = "Precaución")
                        LegendDot(color = FlagRed, label = "Peligro")
                    }
                }
            }
        }

        // Map Zoom Controls, Layer Menu & Reset Button
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Map Layers Dropdown Button
            Box {
                FloatingActionButton(
                    onClick = { showLayersMenu = !showLayersMenu },
                    modifier = Modifier.size(40.dp).testTag("fab_map_layers"),
                    containerColor = if (useGoogleMaps && mapType == MapType.SATELLITE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    contentColor = if (useGoogleMaps && mapType == MapType.SATELLITE) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 3.dp)
                ) {
                    Icon(Icons.Default.Layers, contentDescription = "Capas de mapa", modifier = Modifier.size(20.dp))
                }

                DropdownMenu(
                    expanded = showLayersMenu,
                    onDismissRequest = { showLayersMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Google Maps (Estándar)") },
                        leadingIcon = { Icon(Icons.Default.Map, contentDescription = null) },
                        onClick = {
                            useGoogleMaps = true
                            mapType = MapType.NORMAL
                            showLayersMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Google Maps (Satélite)") },
                        leadingIcon = { Icon(Icons.Default.Satellite, contentDescription = null) },
                        onClick = {
                            useGoogleMaps = true
                            mapType = MapType.SATELLITE
                            showLayersMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Google Maps (Relieve/Terreno)") },
                        leadingIcon = { Icon(Icons.Default.Terrain, contentDescription = null) },
                        onClick = {
                            useGoogleMaps = true
                            mapType = MapType.TERRAIN
                            showLayersMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Carta Náutica (Flujo Viento)") },
                        leadingIcon = { Icon(Icons.Default.Air, contentDescription = null) },
                        onClick = {
                            useGoogleMaps = false
                            showLayersMenu = false
                        }
                    )
                }
            }

            FloatingActionButton(
                onClick = {
                    if (useGoogleMaps) {
                        coroutineScope.launch {
                            cameraPositionState.animate(CameraUpdateFactory.zoomIn())
                        }
                    } else {
                        scale = (scale * 1.3f).coerceAtMost(4.5f)
                    }
                },
                modifier = Modifier.size(40.dp).testTag("fab_zoom_in"),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 3.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Acercar mapa", modifier = Modifier.size(20.dp))
            }
            FloatingActionButton(
                onClick = {
                    if (useGoogleMaps) {
                        coroutineScope.launch {
                            cameraPositionState.animate(CameraUpdateFactory.zoomOut())
                        }
                    } else {
                        scale = (scale / 1.3f).coerceAtLeast(0.75f)
                    }
                },
                modifier = Modifier.size(40.dp).testTag("fab_zoom_out"),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 3.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Alejar mapa", modifier = Modifier.size(20.dp))
            }
            FloatingActionButton(
                onClick = {
                    if (useGoogleMaps) {
                        coroutineScope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(almeriaCenter, 9.8f)
                            )
                        }
                    } else {
                        scale = 1.0f
                        offsetX = 0f
                        offsetY = 0f
                    }
                },
                modifier = Modifier.size(40.dp).testTag("fab_center_map"),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 3.dp)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Centrar mapa", modifier = Modifier.size(20.dp))
            }
        }

        // Bottom Selected Beach Preview Card (Weather/Wind Summary when pin is tapped)
        AnimatedVisibility(
            visible = selectedMapBeach != null,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            selectedMapBeach?.let { beach ->
                val alert = getBathingAlert(beach)
                val isFav = uiState.favorites.any { it.id == beach.id }
                val zoneForecast = uiState.marineForecasts[beach.zone]

                MapBeachPreviewCard(
                    beach = beach,
                    alert = alert,
                    forecast = zoneForecast,
                    isFavorite = isFav,
                    onViewDetails = { onBeachSelected(beach) },
                    onNavigate = { openGoogleMapsNavigation(context, beach.latitude, beach.longitude, beach.name) },
                    onClose = { selectedMapBeach = null },
                    onFavoriteToggle = { onFavoriteToggle(beach.id) },
                    modifier = Modifier
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                        .navigationBarsPadding()
                )
            }
        }
    }
}

@Composable
fun LegendDot(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Summary Preview Card shown when a beach pin is tapped on the interactive map.
 * Displays real-time wind/weather status, shelter evaluation, wave metrics, and bathing safety flag.
 */
@Composable
fun MapBeachPreviewCard(
    beach: Beach,
    alert: BathingSafetyAlert,
    forecast: MarineForecast?,
    isFavorite: Boolean,
    onViewDetails: () -> Unit,
    onNavigate: () -> Unit,
    onClose: () -> Unit,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("map_beach_preview_card")
            .shadow(12.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Beach thumbnail, Name, Zone, Close & Fav buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Photo Thumbnail
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    BeachImageView(
                        beach = beach,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Name & Location
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = beach.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${beach.municipality} • ${beach.zone.displayName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Action icons (Favorite + Close)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onFavoriteToggle,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("map_preview_fav_btn")
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorito",
                            tint = if (isFavorite) Color(0xFFFF5252) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("map_preview_close_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Weather & Wind Snapshot Row
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Wind Status Pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Air,
                            contentDescription = null,
                            tint = if (alert.isProtectedFromCurrentWind) FlagGreen else WindPoniente,
                            modifier = Modifier.size(16.dp)
                        )
                        Column {
                            Text(
                                text = if (alert.isProtectedFromCurrentWind) "🛡️ Protegida hoy" else "⚠️ Expuesta al viento",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (alert.isProtectedFromCurrentWind) FlagGreen else FlagRed
                            )
                            Text(
                                text = "Orientación ${beach.orientation.label}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Marine Metrics: Waves & Water Temp
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (forecast != null) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "🌊 ${String.format("%.1f", forecast.waveHeightMeters)}m • 🌡️ ${forecast.waterTemperatureCelsius.toInt()}°C",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${forecast.currentWindType.label} (${forecast.windSpeedKmh}km/h)",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Badges Row: Flag + Wind + Snorkel
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FlagBadge(flag = alert.flag, compact = true)
                WindBadge(
                    windType = forecast?.currentWindType ?: WindType.LEVANTE,
                    isProtected = alert.isProtectedFromCurrentWind,
                    compact = true
                )
                SnorkelRatingBar(rating = beach.snorkelRating, showLabel = false)
            }

            // Bathing Safety Summary Text
            Text(
                text = alert.summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Action Buttons: Cómo Llegar (GPS) + Ver Ficha Completa
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigate,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cómo llegar", fontSize = 12.sp)
                }

                Button(
                    onClick = onViewDetails,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Text("Ver Ficha", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

// Convert Geo coordinates (lat, lng) to canvas screen pixels
fun geoToScreen(
    lat: Double,
    lng: Double,
    width: Float,
    height: Float,
    scale: Float,
    offsetX: Float,
    offsetY: Float
): Offset {
    val normX = (lng - MIN_LNG) / (MAX_LNG - MIN_LNG)
    // Latitude inverted because screen Y goes downwards
    val normY = 1.0 - ((lat - MIN_LAT) / (MAX_LAT - MIN_LAT))

    val basePadding = 40f
    val effectiveWidth = width - basePadding * 2
    val effectiveHeight = height - basePadding * 2

    val centerX = width / 2f
    val centerY = height / 2f

    val rawX = basePadding + (normX * effectiveWidth).toFloat()
    val rawY = basePadding + (normY * effectiveHeight).toFloat()

    val scaledX = (rawX - centerX) * scale + centerX + offsetX
    val scaledY = (rawY - centerY) * scale + centerY + offsetY

    return Offset(scaledX, scaledY)
}

// Draw coastline of Almería province
fun DrawScope.drawCoastlineShape(
    width: Float,
    height: Float,
    scale: Float,
    offsetX: Float,
    offsetY: Float
) {
    val landColor = Color(0xFF1E293B) // Slate dark continent
    val coastBorderColor = GoldenSand.copy(alpha = 0.85f)

    // Key coastal geo-points defining Almería province coastline
    val coastGeoPoints = listOf(
        Pair(37.45, -2.75), // Northwest inland
        Pair(37.45, -1.55), // Northeast inland (Águilas border)
        Pair(37.38, -1.63), // Pulpí / San Juan de los Terreros
        Pair(37.28, -1.75), // Villaricos
        Pair(37.20, -1.81), // Vera Playa
        Pair(37.14, -1.82), // Garrucha
        Pair(37.08, -1.84), // Mojácar
        Pair(36.98, -1.89), // Carboneras
        Pair(36.93, -1.93), // Agua Amarga
        Pair(36.87, -1.99), // Las Negras
        Pair(36.85, -2.00), // Rodalquilar
        Pair(36.75, -2.10), // San José
        Pair(36.71, -2.19), // Punta de Cabo de Gata (Faro)
        Pair(36.76, -2.25), // Las Salinas
        Pair(36.83, -2.45), // Almería Capital (Bahía)
        Pair(36.80, -2.57), // Aguadulce
        Pair(36.75, -2.62), // Roquetas de Mar
        Pair(36.68, -2.75)  // Poniente / Adra
    )

    val path = Path()
    coastGeoPoints.forEachIndexed { index, (lat, lng) ->
        val pt = geoToScreen(lat, lng, width, height, scale, offsetX, offsetY)
        if (index == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
    }
    path.close()

    drawPath(path, color = landColor, style = Fill)
    drawPath(path, color = coastBorderColor, style = Stroke(width = 3.dp.toPx()))
}

// Draw wind flow vector arrows across the sea
fun DrawScope.drawWindVectors(
    width: Float,
    height: Float,
    windDegrees: Float,
    windType: WindType,
    windSpeedKmh: Int
) {
    val arrowColor = when (windType) {
        WindType.LEVANTE -> WindLevante.copy(alpha = 0.4f)
        WindType.PONIENTE -> WindPoniente.copy(alpha = 0.4f)
        else -> Color.White.copy(alpha = 0.25f)
    }

    val arrowLength = (20 + (windSpeedKmh / 3)).coerceIn(20, 42).toFloat()
    val rad = Math.toRadians((windDegrees - 90).toDouble())
    val dx = (cos(rad) * arrowLength).toFloat()
    val dy = (sin(rad) * arrowLength).toFloat()

    val gridCols = 4
    val gridRows = 5
    for (i in 1..gridCols) {
        for (j in 2..gridRows) {
            val start = Offset(width * (i / (gridCols + 1f)), height * (j / (gridRows + 1f)))
            val end = start + Offset(dx, dy)
            drawLine(
                color = arrowColor,
                start = start,
                end = end,
                strokeWidth = 2.dp.toPx()
            )
            drawCircle(color = arrowColor, radius = 3.dp.toPx(), center = end)
        }
    }
}

// Draw Beach Pin on Map with flag color and protection status
fun DrawScope.drawBeachPin(
    pos: Offset,
    beach: Beach,
    alert: BathingSafetyAlert,
    isSelected: Boolean
) {
    val pinColor = when (alert.flag) {
        FlagColor.GREEN -> FlagGreen
        FlagColor.YELLOW -> FlagYellow
        FlagColor.RED -> FlagRed
    }

    val pinRadius = if (isSelected) 14.dp.toPx() else 9.dp.toPx()

    // Outer glow / halo ring if protected from current wind
    if (alert.isProtectedFromCurrentWind) {
        drawCircle(
            color = FlagGreen.copy(alpha = 0.45f),
            radius = pinRadius + (if (isSelected) 8.dp.toPx() else 5.dp.toPx()),
            center = pos
        )
    }

    // Outer Selection Ring
    if (isSelected) {
        drawCircle(
            color = Color.White,
            radius = pinRadius + 3.dp.toPx(),
            center = pos,
            style = Stroke(width = 2.5.dp.toPx())
        )
    }

    // Main pin circle
    drawCircle(
        color = pinColor,
        radius = pinRadius,
        center = pos
    )

    // Inner core dot
    drawCircle(
        color = Color.White,
        radius = pinRadius * 0.42f,
        center = pos
    )
}

fun openGoogleMapsNavigation(context: Context, lat: Double, lng: Double, name: String) {
    val gmmIntentUri = Uri.parse("geo:$lat,$lng?q=$lat,$lng($name)")
    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
    mapIntent.setPackage("com.google.android.apps.maps")
    try {
        context.startActivity(mapIntent)
    } catch (e: Exception) {
        val browserIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng")
        )
        context.startActivity(browserIntent)
    }
}
