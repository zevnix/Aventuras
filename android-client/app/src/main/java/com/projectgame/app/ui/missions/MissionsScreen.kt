package com.projectgame.app.ui.missions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.projectgame.app.data.local.entity.MissionConfigEntity
import com.projectgame.app.ui.theme.PrimaryMagic

@Composable
fun MissionsScreen(
    viewModel: MissionsViewModel,
    onMissionSelected: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDFBF7))
            .padding(16.dp)
    ) {
        Text("Tus Aventuras de Hoy", style = MaterialTheme.typography.headlineMedium, color = PrimaryMagic)
        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isLoading && uiState.missions.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(uiState.missions.size) { index ->
                    MissionCard(uiState.missions[index], onMissionSelected)
                }
            }
        }
    }
}

@Composable
fun MissionCard(mission: MissionConfigEntity, onMissionSelected: (String) -> Unit) {
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
                    Text(getCategoryLabel(mission.category), color = PrimaryMagic, style = MaterialTheme.typography.labelSmall)
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
                    Text("+${mission.rewardXp} XP", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("+${mission.rewardCoins} 🪙", color = Color(0xFFFFB703), fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { onMissionSelected(mission.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryMagic)
                ) {
                    Text("¡Empezar!")
                }
            }
        }
    }
}

fun getCategoryLabel(category: String): String {
    return when(category) {
        "mind" -> "Mente"
        "creativity" -> "Creatividad"
        "real_world" -> "Mundo Real"
        "family" -> "Familia"
        else -> category.capitalize()
    }
}
