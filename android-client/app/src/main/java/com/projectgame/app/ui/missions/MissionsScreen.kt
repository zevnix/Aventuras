package com.projectgame.app.ui.missions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.projectgame.app.data.local.entity.MissionConfigEntity
import com.projectgame.app.ui.components.squishyClickable

@Composable
fun MissionsScreen(
    viewModel: MissionsViewModel,
    onMissionSelected: (String) -> Unit,
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    val boardColor = try { Color(android.graphics.Color.parseColor(uiState.themeConfig.board_color)) } catch(e:Exception) { Color(0xFF5D4037) }
    val boardAccent = try { Color(android.graphics.Color.parseColor(uiState.themeConfig.board_accent)) } catch(e:Exception) { Color(0xFF8D6E63) }
    val buttonPrimary = try { Color(android.graphics.Color.parseColor(uiState.themeConfig.button_primary)) } catch(e:Exception) { Color(0xFF43A047) }
    val buttonAccent = try { Color(android.graphics.Color.parseColor(uiState.themeConfig.button_primary_accent)) } catch(e:Exception) { Color(0xFFA5D6A7) }

    // Wood Board Background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(boardColor) // Dark Wood
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Board Header
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .background(boardAccent, RoundedCornerShape(12.dp))
                    .border(4.dp, Color(0xFF4E342E), RoundedCornerShape(12.dp))
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Tablero de Aventuras",
                    color = Color(0xFFFFECB3), // Golden Paper
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.isLoading && uiState.missions.isEmpty()) {
                CircularProgressIndicator(color = Color(0xFFFFB703))
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(uiState.missions.size) { index ->
                        MissionCard(uiState.missions[index], buttonPrimary, buttonAccent, onMissionSelected)
                    }
                }
            }
        }

        // Return button at the bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
                .squishyClickable { onBack() }
                .shadow(8.dp, RoundedCornerShape(24.dp))
                .background(Color(0xFFE53935), RoundedCornerShape(24.dp))
                .border(2.dp, Color.White, RoundedCornerShape(24.dp))
                .padding(horizontal = 32.dp, vertical = 12.dp)
        ) {
            Text("Volver al Mundo", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}

@Composable
fun MissionCard(
    mission: MissionConfigEntity,
    buttonPrimary: Color,
    buttonAccent: Color,
    onMissionSelected: (String) -> Unit
) {
    // Looks like a piece of paper pinned to the board
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(0.dp)) // Flat paper shadow
            .background(Color(0xFFFFF8E1)) // Old paper color
            .padding(16.dp)
    ) {
        // Mock Pin
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-8).dp)
                .size(12.dp)
                .background(Color.Red, RoundedCornerShape(6.dp))
        )

        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    mission.title,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = Color(0xFF3E2723),
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .background(Color(0xFFFFCC80), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        getCategoryLabel(mission.category),
                        color = Color(0xFFE65100),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                mission.description,
                color = Color(0xFF5D4037),
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    Text("+${mission.rewardXp} XP", color = Color(0xFF2E7D32), fontWeight = FontWeight.Black, fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("+${mission.rewardCoins} 🪙", color = Color(0xFFF57F17), fontWeight = FontWeight.Black, fontSize = 18.sp)
                }

                // Squishy game button
                Box(
                    modifier = Modifier
                        .squishyClickable { onMissionSelected(mission.id) }
                        .shadow(4.dp, RoundedCornerShape(12.dp))
                        .background(buttonPrimary, RoundedCornerShape(12.dp))
                        .border(2.dp, buttonAccent, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("¡Empezar!", color = Color.White, fontWeight = FontWeight.Black)
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
        else -> category.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}
