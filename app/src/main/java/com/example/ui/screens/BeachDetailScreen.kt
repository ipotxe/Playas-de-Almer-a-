package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.model.*
import com.example.ui.components.FlagBadge
import com.example.ui.components.SnorkelRatingBar
import com.example.ui.components.WindBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeachDetailScreen(
    beach: Beach,
    bathingAlert: BathingSafetyAlert,
    isFavorite: Boolean,
    isVisited: Boolean,
    userNotes: String,
    onBackClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onToggleVisited: () -> Unit,
    onSaveNotes: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedPhotoIndex by remember { mutableStateOf(0) }
    var isFullScreenPhotoOpen by remember { mutableStateOf(false) }
    var showNotesDialog by remember { mutableStateOf(false) }
    var noteInputText by remember { mutableStateOf(userNotes) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = beach.name,
                        maxLines = 1,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        shareBeachInfo(context, beach, bathingAlert)
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Compartir playa")
                    }
                    IconButton(onClick = onFavoriteToggle) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorito",
                            tint = if (isFavorite) Color(0xFFFF5252) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.testTag("beach_detail_screen")
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Photo Gallery Carousel Header
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clickable { isFullScreenPhotoOpen = true }
                    ) {
                        val currentPhoto = beach.photos.getOrNull(selectedPhotoIndex)
                        val resId = currentPhoto?.drawableResId ?: beach.mainPhotoResId ?: R.drawable.almeria_cabo_gata_1787596760956

                        Image(
                            painter = painterResource(id = resId),
                            contentDescription = currentPhoto?.title ?: beach.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Gradient overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                                    )
                                )
                        )

                        // Photo Caption & Fullscreen hint
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = currentPhoto?.title ?: beach.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            if (currentPhoto != null) {
                                Text(
                                    text = currentPhoto.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }

                        // Fullscreen icon badge
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                                .padding(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ZoomIn,
                                contentDescription = "Ver en pantalla completa",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Gallery Thumbnails Row
                    if (beach.photos.size > 1) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(beach.photos) { index, photo ->
                                val thumbRes = photo.drawableResId ?: beach.mainPhotoResId ?: R.drawable.almeria_cabo_gata_1787596760956
                                val isSelected = selectedPhotoIndex == index

                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) MarineCyan else Color.Transparent)
                                        .padding(if (isSelected) 2.dp else 0.dp)
                                        .clickable { selectedPhotoIndex = index }
                                ) {
                                    Image(
                                        painter = painterResource(id = thumbRes),
                                        contentDescription = photo.title,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(6.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Real-Time Marine & Bathing Safety Alert Card
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    BathingSafetyLiveCard(beach = beach, alert = bathingAlert)
                }
            }

            // General Information & Description
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    GeneralInfoCard(beach = beach)
                }
            }

            // Snorkel & Marine Life Guide
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    SnorkelGuideCard(beach = beach, alert = bathingAlert)
                }
            }

            // Water Sports Badges
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    WaterSportsCard(sports = beach.waterSports)
                }
            }

            // Access, Parking and Coordinates
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    AccessAndParkingCard(
                        beach = beach,
                        onOpenNavigation = {
                            openGoogleMapsNavigation(context, beach.latitude, beach.longitude, beach.name)
                        }
                    )
                }
            }

            // Services & Amenities Grid
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    ServicesGridCard(services = beach.services)
                }
            }

            // User Notes & Visit Status
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    UserNotesCard(
                        isVisited = isVisited,
                        notes = userNotes,
                        onToggleVisited = onToggleVisited,
                        onEditNotes = {
                            noteInputText = userNotes
                            showNotesDialog = true
                        }
                    )
                }
            }
        }
    }

    // Full Screen Photo Lightbox Dialog
    if (isFullScreenPhotoOpen) {
        val currentPhoto = beach.photos.getOrNull(selectedPhotoIndex)
        val resId = currentPhoto?.drawableResId ?: beach.mainPhotoResId ?: R.drawable.almeria_cabo_gata_1787596760956

        Dialog(
            onDismissRequest = { isFullScreenPhotoOpen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = currentPhoto?.title ?: beach.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                // Close Button
                IconButton(
                    onClick = { isFullScreenPhotoOpen = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(16.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                }

                // Photo Caption at bottom
                if (currentPhoto != null) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.7f))
                            .navigationBarsPadding()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = currentPhoto.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = currentPhoto.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }

    // Notes Editor Dialog
    if (showNotesDialog) {
        AlertDialog(
            onDismissRequest = { showNotesDialog = false },
            title = { Text("Nota personal sobre ${beach.name}") },
            text = {
                OutlinedTextField(
                    value = noteInputText,
                    onValueChange = { noteInputText = it },
                    placeholder = { Text("Añade consejos sobre el acceso, calzado, mejor hora para visitarla o dónde aparcar...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 5
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSaveNotes(noteInputText)
                        showNotesDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MarineCyan)
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNotesDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun BathingSafetyLiveCard(beach: Beach, alert: BathingSafetyAlert) {
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AEMET • Estado del Baño Hoy",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                FlagBadge(flag = alert.flag)
            }

            // Wind status alert
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (alert.isProtectedFromCurrentWind) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (alert.isProtectedFromCurrentWind) Icons.Default.CheckCircle else Icons.Default.Air,
                        contentDescription = null,
                        tint = if (alert.isProtectedFromCurrentWind) Color(0xFF2E7D32) else Color(0xFFE65100),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = alert.windAdvice,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = if (alert.isProtectedFromCurrentWind) Color(0xFF1B5E20) else Color(0xFFBF360C)
                    )
                }
            }

            Text(
                text = alert.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Metrics row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoPill(label = "Oleaje estimado", value = "~${String.format("%.1f", alert.waveHeightEstimated)} m")
                InfoPill(label = "Orientación costa", value = beach.orientation.label)
                InfoPill(label = "Snorkel hoy", value = if (alert.isProtectedFromCurrentWind) "Excelente" else "Reducido")
            }
        }
    }
}

@Composable
fun GeneralInfoCard(beach: Beach) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Información General",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = beach.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp
            )

            Divider(modifier = Modifier.padding(vertical = 4.dp))

            // Specs grid
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SpecRow(label = "Municipio", value = beach.municipality)
                SpecRow(label = "Zona costera", value = beach.zone.displayName)
                SpecRow(label = "Tipo de fondo / arena", value = beach.sandType)
                SpecRow(label = "Dimensiones", value = "${beach.lengthMeters} m longitud x ${beach.widthMeters} m anchura")
                SpecRow(label = "Entorno", value = if (beach.isNaturalPark) "🌿 Parque Natural Cabo de Gata-Níjar" else "🏖️ Litoral Almeriense")
                SpecRow(label = "Naturismo", value = if (beach.isNudistFriendly) "☀️ Tradición naturista / nudista" else "Textil tradicional")
            }
        }
    }
}

@Composable
fun SnorkelGuideCard(beach: Beach, alert: BathingSafetyAlert) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Water,
                        contentDescription = null,
                        tint = MarineCyan
                    )
                    Text(
                        text = "Guía de Snorkel y Buceo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                SnorkelRatingBar(rating = beach.snorkelRating)
            }

            Text(
                text = beach.snorkelDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )

            Text(
                text = "Especies y Biodiversidad Marina Marina:",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Marine life badges
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(beach.marineLifeHighlights.size) { index ->
                    val species = beach.marineLifeHighlights[index]
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE0F7FA))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "🐟 $species",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF006064)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WaterSportsCard(sports: List<WaterSport>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Deportes Acuáticos Recomendados",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                sports.forEach { sport ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = sport.label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AccessAndParkingCard(
    beach: Beach,
    onOpenNavigation: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Acceso y Aparcamiento",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsWalk,
                    contentDescription = null,
                    tint = MarineCyan
                )
                Text(
                    text = beach.accessDifficulty.label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = beach.parkingInfo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            Button(
                onClick = onOpenNavigation,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MarineCyan)
            ) {
                Icon(Icons.Default.Navigation, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cómo llegar con Google Maps (GPS)")
            }
        }
    }
}

@Composable
fun ServicesGridCard(services: BeachServices) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Servicios e Instalaciones",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (services.isVirginCove) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFE1F5FE),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🏝️ Cala 100% Virgen: Sin servicios ni chiringuitos. Recuerda llevar agua abundante, comida y llevarte tu basura para proteger el entorno.",
                        fontSize = 12.sp,
                        color = Color(0xFF0277BD),
                        modifier = Modifier.padding(10.dp)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ServiceItem(icon = Icons.Default.HealthAndSafety, name = "Socorrismo y primeros auxilios", has = services.hasLifeguard)
                    ServiceItem(icon = Icons.Default.Restaurant, name = "Chiringuito / Restaurante", has = services.hasChiringuito)
                    ServiceItem(icon = Icons.Default.Shower, name = "Duchas / Lavapiés", has = services.hasShowers)
                    ServiceItem(icon = Icons.Default.Wc, name = "Aseos públicos", has = services.hasToilets)
                    ServiceItem(icon = Icons.Default.Accessible, name = "Acceso adaptado PMR", has = services.hasDisabledAccess)
                    ServiceItem(icon = Icons.Default.BeachAccess, name = "Alquiler de hamacas / sombrillas", has = services.hasSunbedRental)
                }
            }
        }
    }
}

@Composable
fun ServiceItem(icon: ImageVector, name: String, has: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (has) MarineCyan else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = name,
                fontSize = 13.sp,
                color = if (has) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
        Text(
            text = if (has) "✅ Sí" else "❌ No",
            fontSize = 12.sp,
            color = if (has) FlagGreen else MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
fun UserNotesCard(
    isVisited: Boolean,
    notes: String,
    onToggleVisited: () -> Unit,
    onEditNotes: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mi Diario de Viaje",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onEditNotes) {
                    Text(if (notes.isEmpty()) "Añadir nota" else "Editar")
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onToggleVisited() }
            ) {
                Checkbox(checked = isVisited, onCheckedChange = { onToggleVisited() })
                Text(
                    text = if (isVisited) "✅ Ya he visitado esta playa" else "Marcar como visitada",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (notes.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "📝 \"$notes\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun InfoPill(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

fun shareBeachInfo(context: Context, beach: Beach, alert: BathingSafetyAlert) {
    val text = """
        🏖️ ${beach.name} (${beach.municipality}, Almería)
        🚩 Estado del mar hoy: ${alert.flag.label}
        🌬️ Viento y oleaje: ${alert.windAdvice}
        🤿 Snorkel: ${beach.snorkelRating}/5.0
        📍 Coordenadas: ${beach.latitude}, ${beach.longitude}
        
        Descubre más en la app Playas de Almería.
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, beach.name)
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Compartir ${beach.name}"))
}
