package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF38BDF8),
    onPrimary = Color(0xFF00354E),
    primaryContainer = MarineOceanDark,
    onPrimaryContainer = MarineOceanContainerLight,
    secondary = Color(0xFF2DD4BF),
    onSecondary = Color(0xFF003731),
    secondaryContainer = TurquoiseOnContainer,
    onSecondaryContainer = TurquoiseContainerLight,
    tertiary = Color(0xFFFBBF24),
    onTertiary = Color(0xFF451A03),
    tertiaryContainer = AmberOnContainer,
    onTertiaryContainer = AmberContainerLight,
    background = BackgroundDark,
    surface = SurfaceDark,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceContainerHighDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark
)

private val LightColorScheme = lightColorScheme(
    primary = MarineOceanPrimary,
    onPrimary = Color.White,
    primaryContainer = MarineOceanContainerLight,
    onPrimaryContainer = MarineOceanOnContainer,
    secondary = TurquoiseSecondary,
    onSecondary = Color.White,
    secondaryContainer = TurquoiseContainerLight,
    onSecondaryContainer = TurquoiseOnContainer,
    tertiary = AmberTertiary,
    onTertiary = Color.White,
    tertiaryContainer = AmberContainerLight,
    onTertiaryContainer = AmberOnContainer,
    background = BackgroundLight,
    surface = SurfaceLight,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceContainerLowLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep tailored brand palette
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
