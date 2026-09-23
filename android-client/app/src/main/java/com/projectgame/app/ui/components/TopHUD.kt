package com.projectgame.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectgame.app.ui.theme.PrimaryMagic

@Composable
fun TopHUD(
    level: Int,
    xpProgress: Float,
    coins: Int,
    gems: Int,
    isOffline: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.85f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Level & Progress
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(PrimaryMagic),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$level",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            // Minimal XP Bar
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.LightGray)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(xpProgress)
                        .fillMaxHeight()
                        .background(PrimaryMagic)
                )
            }
        }

        // Right: Currencies & Status
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isOffline) {
                Text("☁️", modifier = Modifier.padding(end = 8.dp))
            }
            Text("$coins 🪙", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text("$gems 💎", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}
