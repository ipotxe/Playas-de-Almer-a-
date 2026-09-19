package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.Beach
import com.example.ui.theme.MarineOceanDark
import com.example.ui.theme.MarineOceanPrimary

@Composable
fun BeachImageView(
    beach: Beach,
    modifier: Modifier = Modifier,
    contentDescription: String? = beach.name,
    contentScale: ContentScale = ContentScale.Crop,
    overridePhotoUrl: String? = null,
    overrideResId: Int? = null
) {
    val context = LocalContext.current
    val photoUrl = overridePhotoUrl ?: beach.mainPhotoUrl
    val fallbackResId = overrideResId ?: beach.mainPhotoResId ?: R.drawable.almeria_cabo_gata_1787596760956

    if (photoUrl.isNotBlank()) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(context)
                .data(photoUrl)
                .crossfade(true)
                .build(),
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier,
            loading = {
                // Fallback drawable while network photo loads
                Image(
                    painter = painterResource(id = fallbackResId),
                    contentDescription = contentDescription,
                    contentScale = contentScale,
                    modifier = Modifier.fillMaxSize()
                )
            },
            error = {
                // High-quality local drawable fallback if network unavailable
                Image(
                    painter = painterResource(id = fallbackResId),
                    contentDescription = contentDescription,
                    contentScale = contentScale,
                    modifier = Modifier.fillMaxSize()
                )
            }
        )
    } else {
        Image(
            painter = painterResource(id = fallbackResId),
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
    }
}
