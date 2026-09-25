package com.projectgame.app.ui.missions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectgame.app.ui.components.squishyClickable
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
            Button(onClick = onBack) { Text("Volver") }
        }
        return
    }

    var showSuccess by remember { mutableStateOf(false) }

    // Wood Background (Similar to the board but focused)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF5D4037))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (!showSuccess) {
            // Mission Content (Paper)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(16.dp))
                    .background(Color(0xFFFFF8E1), RoundedCornerShape(16.dp))
                    .border(2.dp, Color(0xFFFFCC80), RoundedCornerShape(16.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    mission.title,
                    fontWeight = FontWeight.Black,
                    fontSize = 28.sp,
                    color = Color(0xFF3E2723),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    mission.description,
                    fontSize = 18.sp,
                    color = Color(0xFF5D4037),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))

                if (mission.missionType == "math") {
                    val jsonParser = Json { ignoreUnknownKeys = true }
                    val contentElement = try {
                        jsonParser.parseToJsonElement(mission.contentJson).jsonObject
                    } catch(e: Exception) { null }

                    val question = contentElement?.get("question")?.jsonPrimitive?.content ?: "¿Pregunta?"
                    val options = contentElement?.get("options")?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()
                    val correctIndex = contentElement?.get("correct_answer_index")?.jsonPrimitive?.content?.toIntOrNull() ?: 0

                    Text(
                        question,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        color = Color(0xFF1565C0)
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        options.forEachIndexed { index, optText ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .squishyClickable {
                                        if (index == correctIndex) {
                                            viewModel.completeDigitalMission(mission.id)
                                            showSuccess = true
                                        } else {
                                            // Future: Play wrong sound or shake animation
                                        }
                                    }
                                    .shadow(4.dp, RoundedCornerShape(16.dp))
                                    .background(Color(0xFF42A5F5), RoundedCornerShape(16.dp))
                                    .border(2.dp, Color(0xFF90CAF9), RoundedCornerShape(16.dp))
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(optText, color = Color.White, fontWeight = FontWeight.Black, fontSize = 24.sp)
                            }
                        }
                    }
                } else {
                    Text("Esta misión requiere que subas una foto.")
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .squishyClickable {
                                viewModel.completeDigitalMission(mission.id)
                                showSuccess = true
                            }
                            .shadow(4.dp, RoundedCornerShape(16.dp))
                            .background(Color(0xFFAB47BC), RoundedCornerShape(16.dp))
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Tomar Foto (Simulado)", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    "Cancelar",
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.squishyClickable { onBack() }.padding(8.dp)
                )
            }
        }

        // Reward Popup (Victory)
        AnimatedVisibility(
            visible = showSuccess,
            enter = fadeIn() + scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(24.dp))
                    .background(Color(0xFFFFF9C4), RoundedCornerShape(24.dp))
                    .border(4.dp, Color(0xFFFFB703), RoundedCornerShape(24.dp))
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🌟 ¡LO LOGRASTE! 🌟", fontWeight = FontWeight.Black, fontSize = 32.sp, color = Color(0xFFF57F17))
                Spacer(modifier = Modifier.height(24.dp))

                // Big Reward display
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("+${mission.rewardXp} XP", color = Color(0xFF2E7D32), fontWeight = FontWeight.Black, fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("+${mission.rewardCoins} 🪙", color = Color(0xFFF57F17), fontWeight = FontWeight.Black, fontSize = 28.sp)
                }

                Spacer(modifier = Modifier.height(32.dp))

                Box(
                    modifier = Modifier
                        .squishyClickable { onMissionCompleted() }
                        .shadow(4.dp, RoundedCornerShape(16.dp))
                        .background(Color(0xFF4CAF50), RoundedCornerShape(16.dp))
                        .border(2.dp, Color(0xFFA5D6A7), RoundedCornerShape(16.dp))
                        .padding(horizontal = 32.dp, vertical = 16.dp)
                ) {
                    Text("¡Reclamar!", color = Color.White, fontWeight = FontWeight.Black, fontSize = 24.sp)
                }
            }
        }
    }
}
