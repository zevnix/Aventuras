package com.projectgame.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.projectgame.app.ui.theme.PrimaryMagic
import com.projectgame.app.ui.theme.SecondaryAdventure

sealed class Screen(val route: String, val title: String, val icon: @Composable () -> Unit) {
    object World : Screen("world", "Mundo", { Icon(Icons.Filled.Place, contentDescription = "Mundo") })
    object Missions : Screen("missions", "Aventuras", { Icon(Icons.Filled.List, contentDescription = "Aventuras") })
    object House : Screen("house", "Casa", { Icon(Icons.Filled.Home, contentDescription = "Casa") })
    object Profile : Screen("profile", "Perfil", { Icon(Icons.Filled.Person, contentDescription = "Perfil") })
}

val bottomNavItems = listOf(
    Screen.World,
    Screen.Missions,
    Screen.House,
    Screen.Profile
)

@Composable
fun AventiBottomNav(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = Color.White.copy(alpha = 0.9f),
        contentColor = PrimaryMagic
    ) {
        bottomNavItems.forEach { screen ->
            NavigationBarItem(
                icon = screen.icon,
                label = { Text(screen.title) },
                selected = currentRoute == screen.route,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = PrimaryMagic,
                    indicatorColor = SecondaryAdventure,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray
                ),
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}
