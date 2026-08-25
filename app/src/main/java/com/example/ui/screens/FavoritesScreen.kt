package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.model.BathingSafetyAlert
import com.example.data.model.Beach
import com.example.ui.components.BeachCard
import com.example.ui.theme.GoldenSand
import com.example.ui.theme.MarineCyan
import com.example.ui.viewmodel.BeachUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    uiState: BeachUiState,
    getBathingAlert: (Beach) -> BathingSafetyAlert,
    onBeachClick: (Beach) -> Unit,
    onFavoriteToggle: (String) -> Unit,
    onToggleVisited: (String) -> Unit,
    onSaveNotes: (String, String) -> Unit,
    onExploreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var editingNotesBeachId by remember { mutableStateOf<String?>(null) }
    var currentNoteText by remember { mutableStateOf("") }

    val favCount = uiState.favorites.size
    val visitedCount = uiState.favorites.count { beach ->
        uiState.userBeachData[beach.id]?.isVisited == true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Mis Playas Favoritas",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$favCount guardadas • $visitedCount visitadas",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.testTag("favorites_screen")
    ) { padding ->
        if (uiState.favorites.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = MarineCyan.copy(alpha = 0.5f),
                        modifier = Modifier.size(72.dp)
                    )
                    Text(
                        text = "Aún no tienes playas favoritas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Pulsa en el corazón de cualquier cala para guardarla aquí y planificar tus escapadas a Cabo de Gata y Levante.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onExploreClick,
                        colors = ButtonDefaults.buttonColors(containerColor = MarineCyan)
                    ) {
                        Text("Explorar Playas")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Banner
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.BeachAccess,
                                contentDescription = null,
                                tint = MarineCyan,
                                modifier = Modifier.size(36.dp)
                            )
                            Column {
                                Text(
                                    text = "Tu Colección de Almería",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Guarda notas personales sobre accesos, restaurantes y marca las calas que ya has visitado.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                // Favorites List
                items(uiState.favorites, key = { it.id }) { beach ->
                    val userEntry = uiState.userBeachData[beach.id]
                    val isVisited = userEntry?.isVisited ?: false
                    val notes = userEntry?.userNotes ?: ""

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        BeachCard(
                            beach = beach,
                            bathingAlert = getBathingAlert(beach),
                            isFavorite = true,
                            onBeachClick = onBeachClick,
                            onFavoriteToggle = onFavoriteToggle
                        )

                        // Visited toggle & Notes bar
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Mark as visited checkbox
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable { onToggleVisited(beach.id) }
                                    ) {
                                        Checkbox(
                                            checked = isVisited,
                                            onCheckedChange = { onToggleVisited(beach.id) },
                                            colors = CheckboxDefaults.colors(checkedColor = MarineCyan)
                                        )
                                        Text(
                                            text = if (isVisited) "✅ Ya la he visitado" else "Pendiente de visitar",
                                            fontSize = 12.sp,
                                            fontWeight = if (isVisited) FontWeight.Bold else FontWeight.Normal,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    // Add/edit note button
                                    TextButton(
                                        onClick = {
                                            editingNotesBeachId = beach.id
                                            currentNoteText = notes
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.EditNote,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (notes.isEmpty()) "Añadir nota" else "Editar nota",
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                if (notes.isNotEmpty()) {
                                    Text(
                                        text = "📝 \"$notes\"",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(start = 12.dp, bottom = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Notes Edit Dialog
    if (editingNotesBeachId != null) {
        AlertDialog(
            onDismissRequest = { editingNotesBeachId = null },
            title = { Text("Nota sobre la playa") },
            text = {
                OutlinedTextField(
                    value = currentNoteText,
                    onValueChange = { currentNoteText = it },
                    placeholder = { Text("Ej: Llevar escarpines, llegar antes de las 10:00 por el parking, buen arroz en el chiringuito...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        editingNotesBeachId?.let { onSaveNotes(it, currentNoteText) }
                        editingNotesBeachId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MarineCyan)
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingNotesBeachId = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
