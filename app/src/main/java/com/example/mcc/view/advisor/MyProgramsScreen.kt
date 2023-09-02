package com.example.mcc.view.advisor

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.mcc.controller.AdvisorController
import com.example.mcc.controller.AuthController
import com.example.mcc.controller.ManagerController
import com.example.mcc.model.TrainingProgram
import com.example.mcc.model.TrainingRequest
import com.example.mcc.view.Utils
import com.example.mcc.view.common.LoadingScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyProgramsScreen(navHostController: NavController) {
    val scope = rememberCoroutineScope()
    val programs = remember { mutableStateListOf<Pair<TrainingProgram,Int>>() }
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current
    LaunchedEffect(key1 = Unit, block = {
        if (programs.isEmpty()) {
            val currentUserToken = Utils.getCurrentUserToken(context)

            val (advisorId, advisor) = AuthController.getUserByToken(currentUserToken)
                ?: ("" to null)
            programs.addAll(AdvisorController.getTrainingProgramsOf(advisorId))
            Log.e("TrainingRequests", "programs.size = ${programs.size} ")
        }
        isLoading = false
    })

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            CenterAlignedTopAppBar(title = { Text(text = "My Programs") })
            Spacer(modifier = Modifier.height(16.dp))
        }

        items(programs) {(program,subscribersCount)->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AsyncImage(
                        model = program.coverPicture,
                        contentDescription = "Cover picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .background(color = Color.Black.copy(alpha = 0.4f))
                            .fillMaxSize()
                    )
                    Text(
                        text = program.category,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 18.sp
                        ),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 4.dp)
                    )
                    Text(
                        text = program.title,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        modifier = Modifier.align(Alignment.Center)
                    )
                    Text(
                        text = "(${subscribersCount}) student(s)",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        ),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
    LoadingScreen(visibility = isLoading)

}