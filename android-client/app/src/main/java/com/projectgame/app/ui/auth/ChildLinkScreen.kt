package com.projectgame.app.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ChildLinkScreen(
    onConfirmClick: (String) -> Unit
) {
    var code by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Conecta con tu Adulto", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Pídele a tu padre o tutor el código secreto para conectar sus aplicaciones.",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = code,
            onValueChange = { code = it },
            label = { Text("Código de vinculación") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onConfirmClick(code) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Confirmar")
        }
    }
}
