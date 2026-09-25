package com.projectgame.app.ui.world

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.projectgame.app.ui.theme.ForestGreen
import com.projectgame.app.ui.theme.SkyBlue

@Composable
fun WorldScreen(
    currentAssetUrl: String?,
    isLoading: Boolean
) {
    // A simple bouncing animation for the pet to feel "alive"
    val infiniteTransition = rememberInfiniteTransition()
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(SkyBlue, Color(0xFFB3E5FC), ForestGreen),
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading && currentAssetUrl == null) {
            Text("Llamando a tu mascota...", color = Color.White)
        } else {
            // Pet rendering
            Box(
                modifier = Modifier
                    .offset(y = offsetY.dp)
                    .size(250.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        // Future: Trigger explicit happy animation or sound
                    },
                contentAlignment = Alignment.Center
            ) {
                if (currentAssetUrl != null) {
                    SubcomposeAsyncImage(
                        model = currentAssetUrl,
                        contentDescription = "Mascota",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val state = painter.state
                        if (state is AsyncImagePainter.State.Loading) {
                            CircularProgressIndicator(modifier = Modifier.size(50.dp).align(Alignment.Center))
                        } else if (state is AsyncImagePainter.State.Error) {
                            Box(
                                modifier = Modifier
                                    .size(150.dp)
                                    .background(Color.White.copy(alpha = 0.5f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Mascota (Placeholder)", color = Color.DarkGray)
                            }
                        } else {
                            SubcomposeAsyncImageContent()
                        }
                    }
                } else {
                    // Fallback visual if no asset url is defined yet
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .background(Color.White.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Mascota Mágica", color = Color.DarkGray)
                    }
                }
            }
        }
    }
}
