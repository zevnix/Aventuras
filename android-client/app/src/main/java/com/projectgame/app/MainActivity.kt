package com.projectgame.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.projectgame.app.ui.auth.ChildAuthScreen
import com.projectgame.app.ui.auth.ChildLinkScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.projectgame.app.ui.viewmodel.ChildAuthViewModel
import com.projectgame.app.ui.viewmodel.ChildLinkViewModel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import com.projectgame.app.ui.viewmodel.AuthState
import com.projectgame.app.ui.viewmodel.LinkState

// Mocking Home ViewModel for the Play-First flow test
import com.projectgame.app.ui.home.HomeScreen
import com.projectgame.app.ui.home.HomeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val authViewModel: ChildAuthViewModel = viewModel()
                    val authState by authViewModel.uiState.collectAsState()

                    when (val state = authState) {
                        is AuthState.Initializing -> {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Comprobando sesión mágica...")
                            }
                        }
                        is AuthState.CreatingAnonymousSession -> {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Creando tu nuevo mundo...")
                            }
                        }
                        is AuthState.Success -> {
                            // 100% Play-First: Bypass login screens completely and load the game
                            val homeViewModel: HomeViewModel = viewModel()
                            HomeScreen(viewModel = homeViewModel)
                        }
                        is AuthState.Error -> {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("¡Oh no! Algo falló.", color = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(state.message)
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = { authViewModel.checkOrCreateSession() }) {
                                    Text("Reintentar")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
