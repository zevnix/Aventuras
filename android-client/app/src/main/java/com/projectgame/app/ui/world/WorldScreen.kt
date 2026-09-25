package com.projectgame.app.ui.world

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.projectgame.app.ui.components.squishyClickable
import com.projectgame.app.ui.home.ThemeConfig

@Composable
fun WorldScreen(
    currentAssetUrl: String?,
    isLoading: Boolean,
    themeConfig: ThemeConfig,
    onNavigateToMissions: () -> Unit,
    onNavigateToHouse: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val skyTop = try { Color(android.graphics.Color.parseColor(themeConfig.parallax_sky_top)) } catch(e:Exception) { Color(0xFF81D4FA) }
    val skyBottom = try { Color(android.graphics.Color.parseColor(themeConfig.parallax_sky_bottom)) } catch(e:Exception) { Color(0xFFB3E5FC) }
    val groundTop = try { Color(android.graphics.Color.parseColor(themeConfig.parallax_ground_top)) } catch(e:Exception) { Color(0xFF4CAF50) }
    val groundBottom = try { Color(android.graphics.Color.parseColor(themeConfig.parallax_ground_bottom)) } catch(e:Exception) { Color(0xFF1B5E20) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(skyTop, skyBottom, groundTop),
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            )
    ) {
        // --- LAYER 1: Background World Elements (Clouds, Mountains, etc) ---
        // Placeholder for future dynamic backgrounds from Supabase

        // --- LAYER 2: The Ground / Floor ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.3f)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(groundTop, groundBottom)
                    ),
                    shape = RoundedCornerShape(topStart = 100.dp, topEnd = 100.dp)
                )
        )

        // --- LAYER 3: Interactive World Objects (Navigation) ---

        // Object 1: The Missions Board (Tablón)
        WorldObject(
            label = "Aventuras",
            color = Color(0xFF8D6E63), // Wood color
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = 32.dp, y = 80.dp),
            onClick = onNavigateToMissions
        )

        // Object 2: The House (Casa)
        WorldObject(
            label = "Mi Casa",
            color = Color(0xFFE57373), // Red roof color
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = (-32).dp, y = 40.dp),
            onClick = onNavigateToHouse
        )

        // Object 3: Magic Mirror / Profile
        WorldObject(
            label = "Perfil",
            color = Color(0xFFCE93D8), // Magic purple
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-40).dp, y = (-80).dp),
            onClick = onNavigateToProfile
        )

        // --- LAYER 4: The Pet (Protagonist) ---
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 40.dp), // Resting on the ground line
            contentAlignment = Alignment.Center
        ) {
            if (isLoading && currentAssetUrl == null) {
                CircularProgressIndicator(color = Color.White)
            } else {
                AnimatedPet(currentAssetUrl)
            }
        }
    }
}

@Composable
fun AnimatedPet(assetUrl: String?) {
    // Breathing animation (Scale Y)
    val infiniteTransition = rememberInfiniteTransition()
    val scaleY by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Floating animation (Offset Y)
    val offsetY by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        )
    )

    var isHappy by remember { mutableStateOf(false) }

    // Click reaction scale
    val clickScale by animateFloatAsState(
        targetValue = if (isHappy) 1.2f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        finishedListener = { isHappy = false }
    )

    Box(
        modifier = Modifier
            .offset(y = offsetY.dp)
            .size(200.dp)
            .squishyClickable { isHappy = true },
        contentAlignment = Alignment.Center
    ) {
        if (assetUrl != null) {
            SubcomposeAsyncImage(
                model = assetUrl,
                contentDescription = "Mascota",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        this.scaleY = scaleY * clickScale
                        this.scaleX = clickScale
                    }
            ) {
                val state = painter.state
                if (state is AsyncImagePainter.State.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(50.dp).align(Alignment.Center))
                } else if (state is AsyncImagePainter.State.Error) {
                    PlaceholderPet()
                } else {
                    SubcomposeAsyncImageContent()
                }
            }
        } else {
            PlaceholderPet()
        }

        if (isHappy) {
            Text(
                "❤️",
                fontSize = 40.sp,
                modifier = Modifier.offset(y = (-80).dp)
            )
        }
    }
}

@Composable
fun PlaceholderPet() {
    Box(
        modifier = Modifier
            .size(120.dp)
            .background(Color.White.copy(alpha = 0.8f), CircleShape)
            .shadow(8.dp, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text("🥚", fontSize = 60.sp)
    }
}

@Composable
fun WorldObject(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    // Represents a physical object in the world that acts as a navigation portal
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.squishyClickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .shadow(8.dp, RoundedCornerShape(16.dp))
                .background(color, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Future: Replace this colored box with actual downloaded asset URL (e.g. wooden board)
            Text(label.first().toString(), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}
