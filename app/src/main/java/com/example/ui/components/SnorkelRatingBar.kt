package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Water
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CoralAccent
import com.example.ui.theme.TurquoiseContainerLight
import com.example.ui.theme.TurquoiseSecondary

@Composable
fun SnorkelRatingBar(
    rating: Float,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(TurquoiseContainerLight)
            .border(BorderStroke(1.dp, TurquoiseSecondary.copy(alpha = 0.25f)), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Water,
            contentDescription = "Snorkel",
            tint = TurquoiseSecondary,
            modifier = Modifier.size(15.dp)
        )
        if (showLabel) {
            Text(
                text = "Snorkel",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = TurquoiseSecondary
            )
        }
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = "Puntuación",
            tint = CoralAccent,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = String.format("%.1f", rating),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TurquoiseSecondary
        )
    }
}
