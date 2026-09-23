package com.projectgame.app.ui.house

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.projectgame.app.ui.theme.SurfaceWood

@Composable
fun HouseScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceWood),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🏠 Mi Refugio", style = MaterialTheme.typography.headlineLarge, color = Color(0xFF8D6E63))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "¡Aquí podrás colocar tus muebles y trofeos pronto!",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.DarkGray
            )
        }
    }
}
