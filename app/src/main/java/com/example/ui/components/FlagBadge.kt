package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FlagColor
import com.example.ui.theme.*

@Composable
fun FlagBadge(
    flag: FlagColor,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val (bgColor, textColor, icon, label) = when (flag) {
        FlagColor.GREEN -> Quadruple(
            FlagGreenContainer,
            FlagGreen,
            Icons.Default.Security,
            if (compact) "Verde" else "Bandera Verde (Seguro)"
        )
        FlagColor.YELLOW -> Quadruple(
            FlagYellowContainer,
            FlagYellow,
            Icons.Default.Warning,
            if (compact) "Amarilla" else "Bandera Amarilla (Precaución)"
        )
        FlagColor.RED -> Quadruple(
            FlagRedContainer,
            FlagRed,
            Icons.Default.Flag,
            if (compact) "Roja" else "Bandera Roja (Peligro)"
        )
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(BorderStroke(1.dp, textColor.copy(alpha = 0.3f)), RoundedCornerShape(8.dp))
            .padding(horizontal = if (compact) 8.dp else 12.dp, vertical = if (compact) 4.dp else 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = textColor,
            modifier = Modifier.size(if (compact) 14.dp else 16.dp)
        )
        Text(
            text = label,
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = if (compact) 11.sp else 12.sp
        )
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
