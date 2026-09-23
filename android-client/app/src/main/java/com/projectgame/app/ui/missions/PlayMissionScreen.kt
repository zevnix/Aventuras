package com.projectgame.app.ui.missions

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.projectgame.app.ui.theme.PrimaryMagic
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun PlayMissionScreen(
    missionId: String,
    viewModel: MissionsViewModel,
    onBack: () -> Unit,
    onMissionCompleted: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val mission = uiState.missions.find { it.id == missionId }

    if (mission == null) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Misión no encontrada")
            Button(onClick = onBack) {
                Text("Volver")
            }
        }
        return
    }

    var showSuccess by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(mission.title, style = MaterialTheme.typography.headlineMedium, color = PrimaryMagic)
        Spacer(modifier = Modifier.height(16.dp))
        Text(mission.description, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(32.dp))

        if (showSuccess) {
            Text("¡Misión Completada!", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("+${mission.rewardXp} XP", color = Color(0xFF4CAF50))
            Text("+${mission.rewardCoins} 🪙", color = Color(0xFFFFB703))
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onMissionCompleted) {
                Text("Volver a Aventuras")
            }
        } else {
            if (mission.missionType == "math") {
                val jsonParser = Json { ignoreUnknownKeys = true }
                val contentElement = try {
                    jsonParser.parseToJsonElement(mission.contentJson).jsonObject
                } catch(e: Exception) { null }

                val question = contentElement?.get("question")?.jsonPrimitive?.content ?: "¿Pregunta?"
                val options = contentElement?.get("options")?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()
                val correctIndex = contentElement?.get("correct_answer_index")?.jsonPrimitive?.content?.toIntOrNull() ?: 0

                Text(question, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    options.forEachIndexed { index, optText ->
                        Button(onClick = {
                            if (index == correctIndex) {
                                viewModel.completeDigitalMission(mission.id)
                                showSuccess = true
                            }
                        }) {
                            Text(optText)
                        }
                    }
                }
            } else {
                Text("Esta misión requiere que subas una foto.")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    // For now, just simulate completing it since we don't have camera intent wired up in this vertical slice
                    viewModel.completeDigitalMission(mission.id)
                    showSuccess = true
                }) {
                    Text("Tomar Foto (Simulado)")
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) {
                Text("Cancelar")
            }
        }
    }
}
