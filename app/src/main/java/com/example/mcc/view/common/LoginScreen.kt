package com.example.mcc.view.common

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mcc.R
import com.example.mcc.controller.AuthController
import com.example.mcc.model.UserType
import com.example.mcc.model.UserType.*
import com.example.mcc.view.Temporary
import com.example.mcc.view.Utils
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
) {
    var token by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.4f)
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = null,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        OutlinedTextField(
            value = token,
            onValueChange = { token = it },
            label = { Text("Token") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))
        val context = LocalContext.current

        Button(
            onClick = {
/*                navController.popBackStack()
                navController.navigate("advisor_screen")
                Utils.saveUserToken(context,Temporary.AdvisorToken) // student
                isLoading = false
//                navController.navigate("advisor_screen")*/
                scope.launch {
                    isLoading = true
                    val userType = AuthController.login(token)
                    Log.e("LOGIN", "LoginScreen: userType=[ \" ${userType.name} \" ]")
                    if (userType != Undefined) {
                        Utils.saveUserToken(context, token)
                    }
                    when (userType) {
                        Manager -> navController.navigate("manager_screen")
                        Advisor -> navController.navigate("advisor_screen")
                        Student -> navController.navigate("student_screen")
                        Undefined -> {
                            Toast.makeText(context, "Error: Invalid Token", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                    isLoading = false
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text("Sign In")
        }

        TextButton(
            onClick = {
                navController.navigate("register_screen")
            },
            modifier = Modifier
                .padding(bottom = 16.dp)
        ) {
            Text("Dont have account? Sign Up")
        }

    }
    LoadingScreen(visibility = isLoading)
}