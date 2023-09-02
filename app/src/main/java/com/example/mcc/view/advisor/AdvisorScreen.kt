package com.example.mcc.view.advisor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.outlined.VideoCall
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mcc.controller.AuthController
import com.example.mcc.model.User
import com.example.mcc.view.Utils


private data class AdvisorScreens(val route: String, val title: String, val icon: ImageVector)

private val navItems = listOf(
    AdvisorScreens(route = "Advisor/MyStudents", title = "My Students", icon = Icons.Default.Face),
    AdvisorScreens(route = "Advisor/MyPrograms", title = "My Programs", icon = Icons.Default.Apps),
    AdvisorScreens(
        route = "Advisor/MeetingRequests",
        title = "Meeting Requests",
        icon = Icons.Outlined.VideoCall
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvisorScreen(navHostController: NavHostController) {
    val navController = rememberNavController()
    val current = remember { mutableStateOf(0) }
    val selectedItem = remember { mutableStateOf("Advisor/MyStudents") }
    val advisor = remember { mutableStateOf<Pair<String, User>?>(null) }
    val context = LocalContext.current
    LaunchedEffect(key1 = Unit, block = {
        if (advisor.value == null) {
            val currentUserToken: String = Utils.getCurrentUserToken(context)
            advisor.value = AuthController.getUserByToken(currentUserToken)
        }
    })
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
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate("Advisor/AddNewProgram") {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            }) {

                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "",
                )
            }
        }
    ) { paddingValues ->
        advisor.value?.let { (advisorId, advisorObj) ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .imePadding()
            ) {
                HomeNavHost(
                    navHostController = navController,
                    mainNavHostController = navHostController,
                    advisorName = advisorObj.name,
                    advisorId = advisorId
                )
            }
        }
    }
}

@Composable
private fun HomeNavHost(
    navHostController: NavHostController,
    mainNavHostController: NavController,
    advisorName: String,
    advisorId: String,
) {
    NavHost(
        navController = navHostController,
        startDestination = navItems.first().route,
    ) {
        composable("Advisor/MyStudents") { MyStudentsScreen(mainNavHostController) }
        composable("Advisor/MyPrograms") { MyProgramsScreen(mainNavHostController) }
        composable("Advisor/MeetingRequests") { MeetingRequestsScreen(mainNavHostController) }
        composable("Advisor/AddNewProgram") {
            AddProgramScreen(
                mainNavHostController,
                advisorName,
                advisorId
            )
        }
    }
}