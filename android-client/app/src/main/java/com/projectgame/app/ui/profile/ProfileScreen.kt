package com.projectgame.app.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.projectgame.app.ui.theme.PrimaryMagic

@Composable
fun ProfileScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Mi Perfil", style = MaterialTheme.typography.headlineMedium, color = PrimaryMagic)
        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Progreso Local (Cuenta Invitado)", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Tu mundo se guarda en este dispositivo. Para no perderlo nunca y jugar en otras tablets, conecta una cuenta real.")
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { /* Future: Call auth.linkIdentity() */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Guardar Progreso (Google / Email)")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Familia", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Conecta tu mundo con la app de tus papás para que aprueben tus misiones del mundo real.")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { /* Future: Navigate to ChildLinkScreen */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Vincular con mis Padres")
                }
            }
        }
    }
}
