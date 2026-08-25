package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WindType
import com.example.ui.theme.*
import com.example.util.WindAlertSeverity
import com.example.util.WindSafetyAlert

@Composable
fun WindAlertBanner(
    alert: WindSafetyAlert?,
    isDismissed: Boolean,
    onDismiss: () -> Unit,
    onViewDetails: () -> Unit,
    onFilterShelteredOnly: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = alert != null && !isDismissed,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        if (alert == null) return@AnimatedVisibility

        val isDanger = alert.severity == WindAlertSeverity.STRONG_DANGER
        val isLevante = alert.windType == WindType.LEVANTE

        val containerGradient = when {
            isDanger -> listOf(Color(0xFFB91C1C), Color(0xFFDC2626), Color(0xFFEA580C))
            isLevante -> listOf(Color(0xFF0369A1), Color(0xFF0284C7), Color(0xFF0D9488))
            else -> listOf(Color(0xFFC2410C), Color(0xFFD97706), Color(0xFFEAB308))
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clickable { onViewDetails() }
                .testTag("wind_safety_alert_banner"),
            shape = RoundedCornerShape(16.dp),
            color = Color.Transparent,
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
            shadowElevation = 4.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(containerGradient))
                    .padding(14.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Header Row: Icon + Badge + Close Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isDanger) Icons.Default.Warning else Icons.Default.Air,
                                    contentDescription = "Alerta de Viento",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Column {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = alert.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    if (alert.isSimulated) {
                                        Surface(
                                            color = Color.Black.copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "TEST",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.2f))
                                .testTag("dismiss_wind_alert_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Descartar aviso",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Headline text
                    Text(
                        text = alert.headline,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.95f),
                        lineHeight = 16.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Quick Stats & Action Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Protected count badge
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.Black.copy(alpha = 0.25f),
                            border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "🛡️ ${alert.shelteredBeachIds.size} Playas Refugio",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Direct action: Safe beaches
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White,
                                modifier = Modifier
                                    .clickable { onFilterShelteredOnly() }
                                    .testTag("filter_sheltered_quick_btn")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Pool,
                                        contentDescription = null,
                                        tint = if (isDanger) Color(0xFFB91C1C) else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "Ver seguras",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDanger) Color(0xFFB91C1C) else MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            // Details button
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.25f),
                                modifier = Modifier.clickable { onViewDetails() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Info,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "Aviso",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
