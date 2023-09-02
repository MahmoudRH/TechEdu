package com.example.mcc.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mcc.view.advisor.AdvisorScreen
import com.example.mcc.view.student.TrainingDetailsScreen
import com.example.mcc.view.common.LoginScreen
import com.example.mcc.view.common.RegisterScreen
import com.example.mcc.view.common.RegistrationPendingScreen
import com.example.mcc.view.manager.ManagerScreen
import com.example.mcc.view.student.StudentScreen
import com.example.mcc.view.student.SubscribedProgramDetailsScreen
import com.example.mcc.view.ui.theme.MCCTheme
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class MainActivity :  AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MCCTheme {
                val navController = rememberNavController()
                Surface(modifier = Modifier.fillMaxSize()) {
                    MyNavHost(navHostController = navController)
                }
            }
        }
    }

    @Composable
    private fun MyNavHost(navHostController: NavHostController) {
        NavHost(
            navController = navHostController,
            startDestination = "login_screen"
        ) {
            composable(route = "register_screen") {
                RegisterScreen(navHostController)
            }
            composable(route = "login_screen") {
                LoginScreen(navHostController)
            }
            composable(route = "student_screen") {
                StudentScreen(navHostController)
            }
            composable(
                route = "training_details_screen/{coverImage}/{title}/{description}/{price}/{programId}/{advisorId}",
                arguments = listOf(
                    navArgument("coverImage") { type = NavType.StringType },
                    navArgument("title") { type = NavType.StringType },
                    navArgument("description") { type = NavType.StringType },
                    navArgument("price") { type = NavType.StringType },
                    navArgument("programId") { type = NavType.StringType },
                    navArgument("advisorId") { type = NavType.StringType },
                )
            ) {
                val coverImageEncoded = it.arguments?.getString("coverImage") ?: ""
                val title = it.arguments?.getString("title") ?: ""
                val description = it.arguments?.getString("description") ?: ""
                val price = it.arguments?.getString("price") ?: ""
                val programId: String = it.arguments?.getString("programId") ?: ""
                val advisorId: String = it.arguments?.getString("advisorId") ?: ""

                val coverImage =
                    URLDecoder.decode(coverImageEncoded, StandardCharsets.UTF_8.toString())
                TrainingDetailsScreen(
                    navController = navHostController,
                    programId = programId,
                    advisorId = advisorId,
                    coverPicture = coverImage,
                    title = title,
                    description = description,
                    price = price,
                )
            }

            composable(
                route = "subscribed_program_details_screen/{coverImage}/{title}/{description}/{price}/{programId}/{advisorId}",
                arguments = listOf(
                    navArgument("coverImage") { type = NavType.StringType },
                    navArgument("title") { type = NavType.StringType },
                    navArgument("description") { type = NavType.StringType },
                    navArgument("price") { type = NavType.StringType },
                    navArgument("programId") { type = NavType.StringType },
                    navArgument("advisorId") { type = NavType.StringType },
                )
            ) {
                val coverImageEncoded = it.arguments?.getString("coverImage") ?: ""
                val title = it.arguments?.getString("title") ?: ""
                val description = it.arguments?.getString("description") ?: ""
                val price = it.arguments?.getString("price") ?: ""
                val programId: String = it.arguments?.getString("programId") ?: ""
                val advisorId: String = it.arguments?.getString("advisorId") ?: ""

                val coverImage =
                    URLDecoder.decode(coverImageEncoded, StandardCharsets.UTF_8.toString())
                SubscribedProgramDetailsScreen(
                    navController = navHostController,
                    programId = programId,
                    advisorId = advisorId,
                    coverPicture = coverImage,
                    title = title,
                    description = description,
                    price = price,
                )
            }
            composable(route = "manager_screen") {
                ManagerScreen(navHostController)
            }
            composable(route = "advisor_screen") {
                AdvisorScreen(navHostController)
            }
            composable(route = "register_pending_screen") {
                RegistrationPendingScreen()
            }
        }
    }
}

