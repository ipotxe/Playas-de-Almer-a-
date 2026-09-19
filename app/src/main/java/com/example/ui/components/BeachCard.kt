package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.BathingSafetyAlert
import com.example.data.model.Beach
import com.example.ui.theme.*

@Composable
fun BeachCard(
    beach: Beach,
    bathingAlert: BathingSafetyAlert,
    isFavorite: Boolean,
    onBeachClick: (Beach) -> Unit,
    onFavoriteToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onBeachClick(beach) }
            .testTag("beach_card_${beach.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp, pressedElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Beach Photo Container with Overlaid Badges
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                BeachImageView(
                    beach = beach,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Smooth gradient scrim for optimal text contrast
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.35f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.8f)
                                ),
                                startY = 0f,
                                endY = Float.POSITIVE_INFINITY
                            )
                        )
                )

                // Top badges: Flag Alert & Favorite Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FlagBadge(flag = bathingAlert.flag, compact = true)

                    IconButton(
                        onClick = { onFavoriteToggle(beach.id) },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.45f))
                            .testTag("fav_btn_${beach.id}")
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = if (isFavorite) "Quitar de favoritos" else "Guardar en favoritos",
                            tint = if (isFavorite) Color(0xFFEF4444) else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Bottom Overlay: Beach Name and Municipality
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = beach.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = AmberTertiary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${beach.municipality} • ${beach.zone.displayName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Card Body: Wind protection, Snorkel Rating, Summary
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Status Badges Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Wind Protection Pill
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (bathingAlert.isProtectedFromCurrentWind) FlagGreenContainer else WindPonienteContainer
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (bathingAlert.isProtectedFromCurrentWind) "🛡️ Protegida hoy" else "⚠️ Viento directo",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (bathingAlert.isProtectedFromCurrentWind) FlagGreen else WindPoniente
                        )
                    }

                    // Snorkel Score
                    SnorkelRatingBar(rating = beach.snorkelRating, showLabel = true)
                }

                // Summary text
                Text(
                    text = beach.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )

                // Tags row (Natural Park, Sand, Services)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (beach.isNaturalPark) {
                        TagChip(text = "🌿 Parque Natural", bgColor = FlagGreenContainer, textColor = FlagGreen)
                    }
                    if (beach.services.isVirginCove) {
                        TagChip(text = "🏖️ Cala Virgen", bgColor = MarineOceanContainerLight, textColor = MarineOceanPrimary)
                    }
                    if (beach.isNudistFriendly) {
                        TagChip(text = "☀️ Naturista", bgColor = AmberContainerLight, textColor = AmberTertiary)
                    }
                }
            }
        }
    }
}

@Composable
fun TagChip(
    text: String,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}
