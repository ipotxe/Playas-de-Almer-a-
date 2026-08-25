package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.WarningAmber
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
import com.example.data.model.WindType
import com.example.ui.theme.*

@Composable
fun WindBadge(
    windType: WindType,
    isProtected: Boolean,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val (bgColor, textColor, label) = when {
        isProtected -> Triple(
            FlagGreenContainer,
            FlagGreen,
            if (compact) "🛡️ Protegida" else "🛡️ Protegida de $windType"
        )
        windType == WindType.LEVANTE -> Triple(
            WindLevanteContainer,
            WindLevante,
            if (compact) "⚠️ Expuesta" else "⚠️ Expuesta al Levante"
        )
        windType == WindType.PONIENTE -> Triple(
            WindPonienteContainer,
            WindPoniente,
            if (compact) "⚠️ Expuesta" else "⚠️ Expuesta al Poniente"
        )
        else -> Triple(
            WindCalmContainer,
            WindCalm,
            if (compact) "Calma" else "Calma / Sin oleaje"
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
            imageVector = if (isProtected) Icons.Default.CheckCircle else Icons.Default.Air,
            contentDescription = label,
            tint = textColor,
            modifier = Modifier.size(if (compact) 14.dp else 16.dp)
        )
        Text(
            text = label,
            color = textColor,
            fontWeight = FontWeight.SemiBold,
            fontSize = if (compact) 11.sp else 12.sp
        )
    }
}
