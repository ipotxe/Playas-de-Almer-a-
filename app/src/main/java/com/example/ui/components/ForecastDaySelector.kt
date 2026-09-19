package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ForecastDay
import com.example.ui.theme.MarineOceanPrimary

@Composable
fun ForecastDaySelector(
    selectedDay: ForecastDay,
    onDaySelected: (ForecastDay) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        modifier = modifier
            .fillMaxWidth()
            .testTag("forecast_day_selector")
    ) {
        Row(
            modifier = Modifier
                .padding(4.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ForecastDay.values().forEach { day ->
                val isSelected = day == selectedDay
                val animatedBg by animateColorAsState(
                    targetValue = if (isSelected) MarineOceanPrimary else Color.Transparent,
                    label = "DaySelectorBg"
                )
                val animatedTextColor by animateColorAsState(
                    targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    label = "DaySelectorText"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(animatedBg)
                        .clickable { onDaySelected(day) }
                        .padding(vertical = 10.dp, horizontal = 6.dp)
                        .testTag("day_tab_${day.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = when (day) {
                                    ForecastDay.TODAY -> Icons.Default.WbSunny
                                    ForecastDay.TOMORROW -> Icons.Default.CalendarToday
                                    ForecastDay.DAY_AFTER_TOMORROW -> Icons.Default.Event
                                },
                                contentDescription = null,
                                tint = animatedTextColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = day.title,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = animatedTextColor,
                                maxLines = 1
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = getShortDate(day.dayOffset),
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = if (isSelected) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

private fun getShortDate(dayOffset: Int): String {
    val cal = java.util.Calendar.getInstance()
    cal.add(java.util.Calendar.DAY_OF_YEAR, dayOffset)
    val day = cal.get(java.util.Calendar.DAY_OF_MONTH)
    val month = when (cal.get(java.util.Calendar.MONTH)) {
        0 -> "Ene"
        1 -> "Feb"
        2 -> "Mar"
        3 -> "Abr"
        4 -> "May"
        5 -> "Jun"
        6 -> "Jul"
        7 -> "Ago"
        8 -> "Sep"
        9 -> "Oct"
        10 -> "Nov"
        else -> "Dic"
    }
    return "$day $month"
}
