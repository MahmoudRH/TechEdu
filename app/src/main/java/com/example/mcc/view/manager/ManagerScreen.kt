package com.example.mcc.view.manager

import android.content.SharedPreferences
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController


private data class ManagerScreens(val route:String, val title:String, val icon:ImageVector)
private val navItems = listOf(
    ManagerScreens(route = "Manager/AllUsers", title = "All Users", icon = Icons.Default.Face),
    ManagerScreens(route = "Manager/RegistrationRequests", title = "Registration Requests", icon =Icons.Default.HowToReg),
    ManagerScreens(route = "Manager/TrainingRequests", title = "Training Requests", icon =Icons.Default.Checklist),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagerScreen(navHostController: NavHostController) {
    val navController = rememberNavController()
    val current = remember { mutableStateOf(0) }
    val selectedItem = remember { mutableStateOf("Manager/AllUsers") }

    Scaffold(
        bottomBar = {
            BottomAppBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                navItems.forEachIndexed { index, screen ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title
                            )
                        },
                        label = { Text(text = screen.title, fontSize = 12.sp) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                            current.value = index
                            selectedItem.value = screen.route
                        }
                    )
                }
            }
        }
    ){ paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .imePadding()
        ) {
            HomeNavHost(
                navHostController = navController,
                mainNavHostController = navHostController,
            )
        }
    }
}

@Composable
private fun HomeNavHost(
    navHostController: NavHostController,
    mainNavHostController: NavController,
) {
    NavHost(
        navController = navHostController,
        startDestination = navItems.first().route,
    ) {
        composable("Manager/AllUsers") { AllUsersScreen(mainNavHostController) }
        composable("Manager/RegistrationRequests") { RegistrationRequests(mainNavHostController) }
        composable("Manager/TrainingRequests") { TrainingRequests(mainNavHostController,) }
    }
}
