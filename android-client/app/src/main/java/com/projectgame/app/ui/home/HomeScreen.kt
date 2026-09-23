package com.projectgame.app.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import com.projectgame.app.ui.components.TopHUD
import com.projectgame.app.ui.house.HouseScreen
import com.projectgame.app.ui.missions.MissionsScreen
import com.projectgame.app.ui.navigation.AventiBottomNav
import com.projectgame.app.ui.navigation.Screen
import com.projectgame.app.ui.profile.ProfileScreen
import com.projectgame.app.ui.world.WorldScreen

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { AventiBottomNav(navController) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. Core Navigation Host (The Worlds)
            NavHost(
                navController = navController,
                startDestination = Screen.World.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Screen.World.route) {
                    WorldScreen(
                        currentAssetUrl = uiState.currentAssetUrl,
                        isLoading = uiState.isLoading
                    )
                }
                composable(Screen.Missions.route) {
                    MissionsScreen()
                }
                composable(Screen.House.route) {
                    HouseScreen()
                }
                composable(Screen.Profile.route) {
                    ProfileScreen()
                }
            }

            // 2. Persistent Universal HUD overlaid on top of World and House
            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
            if (currentRoute == Screen.World.route || currentRoute == Screen.House.route) {
                Box(modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp)) {
                    TopHUD(
                        level = uiState.profile?.level ?: 1,
                        xpProgress = uiState.visualProgressPercentage,
                        coins = uiState.profile?.coinsBalance ?: 0,
                        gems = uiState.profile?.gemsBalance ?: 0,
                        isOffline = uiState.isOffline
                    )
                }
            }
        }
    }
}
