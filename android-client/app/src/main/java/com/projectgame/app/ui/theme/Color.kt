package com.projectgame.app.ui.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val PrimaryMagic = Color(0xFF6B4EE6)    // Magical Purple
val PrimaryMagicLight = Color(0xFF9E86FF)
val SecondaryAdventure = Color(0xFFFFB703) // Warm Orange/Gold
val SkyBlue = Color(0xFF87CEEB)
val ForestGreen = Color(0xFF81C784)
val SurfaceWood = Color(0xFFFFF3E0)      // Soft beige
val TextDark = Color(0xFF2C3E50)

val AventiColorScheme = lightColorScheme(
    primary = PrimaryMagic,
    secondary = SecondaryAdventure,
    tertiary = ForestGreen,
    background = SkyBlue,
    surface = SurfaceWood,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextDark,
    onSurface = TextDark
)
