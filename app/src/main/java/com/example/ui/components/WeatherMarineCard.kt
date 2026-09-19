package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MarineForecast
import com.example.data.model.WindType
import com.example.ui.theme.*

@Composable
fun WeatherMarineCard(
    forecast: MarineForecast?,
    isLoading: Boolean,
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (forecast == null) return

    val isLevante = forecast.currentWindType == WindType.LEVANTE
    val isPoniente = forecast.currentWindType == WindType.PONIENTE

    val gradientColors = when {
        isLevante -> listOf(Color(0xFF0369A1), Color(0xFF0284C7), Color(0xFF0D9488))
        isPoniente -> listOf(Color(0xFFC2410C), Color(0xFFEA580C), Color(0xFFD97706))
        else -> listOf(Color(0xFF0F766E), Color(0xFF0D9488), Color(0xFF0284C7))
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(gradientColors))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header: Zone Title + AEMET indicator + Refresh
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "AEMET • ${forecast.day.title.uppercase()}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AmberTertiary.copy(alpha = 0.9f),
                                letterSpacing = 1.sp
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.White.copy(alpha = 0.25f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (forecast.day == com.example.data.model.ForecastDay.TODAY) "EN DIRECTO" else forecast.day.getDisplayDate(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                        Text(
                            text = forecast.zone.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    IconButton(
                        onClick = onRefreshClick,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Actualizar previsión AEMET",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Main Wind Banner: LEVANTE vs PONIENTE prominent badge
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.Black.copy(alpha = 0.25f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Air,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "VIENTO PREDOMINANTE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                                Text(
                                    text = forecast.currentWindType.shortName,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }

                        // Wind Speed & Cardinal Arrow
                        Column(horizontalAlignment = Alignment.End) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Navigation,
                                    contentDescription = null,
                                    tint = AmberTertiary,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .rotate(forecast.windDirectionDegrees)
                                )
                                Text(
                                    text = "${forecast.windSpeedKmh} km/h",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = forecast.windDirectionCardinal,
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }

                // Marine Metrics Grid: Waves, Sea Temp, Air Temp, UV
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricBox(
                        icon = Icons.Default.Waves,
                        value = "${String.format("%.1f", forecast.waveHeightMeters)} m",
                        label = "Oleaje",
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        icon = Icons.Default.Water,
                        value = "${String.format("%.1f", forecast.waterTemperatureCelsius)}°C",
                        label = "Agua del mar",
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        icon = Icons.Default.Thermostat,
                        value = "${String.format("%.0f", forecast.airTemperatureCelsius)}°C",
                        label = "Ambiente",
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        icon = Icons.Default.WbSunny,
                        value = "UV ${forecast.uvIndex}",
                        label = "Índice UV",
                        modifier = Modifier.weight(1f)
                    )
                }

                // Today's Bathing & Snorkel Advice
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = AmberTertiary,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(top = 2.dp)
                        )
                        Text(
                            text = forecast.generalAdvice,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White,
                            lineHeight = 16.sp
                        )
                    }
                }

                Text(
                    text = forecast.lastUpdatedText,
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.75f),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
fun MetricBox(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.Black.copy(alpha = 0.22f))
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)), RoundedCornerShape(10.dp))
            .padding(vertical = 8.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AmberTertiary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = label,
                fontSize = 9.sp,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
    }
}
