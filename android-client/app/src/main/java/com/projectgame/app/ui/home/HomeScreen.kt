package com.projectgame.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.profile?.displayName ?: "Aventurero") },
                actions = {
                    if (uiState.isOffline) {
                        Text("Offline", color = Color.Red, modifier = Modifier.padding(end = 8.dp))
                    }
                    Text("Nvl: ${uiState.profile?.level ?: 1}", fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 16.dp))
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = { /* Navigate to Missions */ }) {
                        Text("¡Aventuras!")
                    }
                    Button(onClick = { /* Navigate to House (Locked in Slice) */ }, enabled = false) {
                        Text("Mi Casa (Próximamente)")
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFE8F5E9)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // Header: Balances and Progress
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Monedas: ${uiState.profile?.coinsBalance ?: 0} 🪙", fontSize = 18.sp)
                    Text("Gemas: ${uiState.profile?.gemsBalance ?: 0} 💎", fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("XP Total: ${uiState.profile?.xpBalance ?: 0}")
                LinearProgressIndicator(
                    progress = uiState.visualProgressPercentage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .clip(CircleShape)
                )
                Text(
                    text = "Próximo nivel en: ${uiState.targetXpForNextLevel} XP",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.End)
                )
            }

            // Center: Mascot Rendering
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.currentAssetUrl != null) {
                    AsyncImage(
                        model = uiState.currentAssetUrl,
                        contentDescription = "Mascota Actual",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(250.dp)
                    )
                } else {
                    // Fallback visual if no asset loaded yet
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .background(Color.Gray, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Kiro\n(Descargando...)", color = Color.White)
                    }
                }
            }

            // Footer of content area
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(bottom = 16.dp))
            } else {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}
