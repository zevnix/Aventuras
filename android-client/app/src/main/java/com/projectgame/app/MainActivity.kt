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

import androidx.compose.runtime.collectAsState
import com.projectgame.app.ui.viewmodel.AuthState
import com.projectgame.app.ui.viewmodel.LinkState

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
                    val linkViewModel: ChildLinkViewModel = viewModel()

                    val authState by authViewModel.uiState.collectAsState()
                    val linkState by linkViewModel.uiState.collectAsState()

                    if (authState is AuthState.Success) {
                        ChildLinkScreen(
                            onConfirmClick = { code ->
                                linkViewModel.confirmLinkCode(code)
                            }
                        )
                        if (linkState is LinkState.Success) {
                            // Link confirmed! Navigate to game map or home
                        }
                    } else {
                        ChildAuthScreen(
                            onLoginClick = { email, pass ->
                                authViewModel.login(email, pass)
                            },
                            onRegisterClick = { email, pass ->
                                authViewModel.register(email, pass)
                            }
                        )
                    }
                }
            }
        }
    }
}
