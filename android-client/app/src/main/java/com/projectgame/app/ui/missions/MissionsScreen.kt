package com.projectgame.app.ui.missions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.projectgame.app.ui.theme.PrimaryMagic

@Composable
fun MissionsScreen() {
    // In Phase 3, this is a visual mock of the board.
    // Future Phase: Fetch from `missions_config` and `mission_instances`.

    val mockMissions = listOf(
        MissionMock("Detective de colores", "Encuentra 3 cosas rojas en casa y tómales una foto.", "Mundo Real", 50, 20),
        MissionMock("Suma de Manzanas", "Resuelve este pequeño puzzle matemático.", "Mente", 30, 10),
        MissionMock("Dibuja a Kiro", "Usa tus lápices para hacer un retrato de tu dragón.", "Creatividad", 60, 25)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDFBF7))
            .padding(16.dp)
    ) {
        Text("Tus Aventuras de Hoy", style = MaterialTheme.typography.headlineMedium, color = PrimaryMagic)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(mockMissions.size) { index ->
                MissionCard(mockMissions[index])
            }
        }
    }
}

@Composable
fun MissionCard(mission: MissionMock) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(mission.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(PrimaryMagic.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(mission.category, color = PrimaryMagic, style = MaterialTheme.typography.labelSmall)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(mission.description, color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    Text("+${mission.xp} XP", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("+${mission.coins} 🪙", color = Color(0xFFFFB703), fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { /* Future: Start mission */ },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryMagic)
                ) {
                    Text("¡Empezar!")
                }
            }
        }
    }
}

data class MissionMock(
    val title: String,
    val description: String,
    val category: String,
    val xp: Int,
    val coins: Int
)
