package com.example.mcc.view.manager

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.mcc.controller.ManagerController
import com.example.mcc.model.TrainingProgram
import com.example.mcc.model.TrainingRequest
import com.example.mcc.model.User
import com.example.mcc.view.common.LoadingScreen
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun TrainingRequests(navController: NavController) {
    val scope = rememberCoroutineScope()
    val trainingRequests = remember { mutableStateListOf<TrainingRequest>() }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(key1 = Unit, block = {
        if (trainingRequests.isEmpty()) {
            trainingRequests.addAll(ManagerController.getTrainingRequests())
            Log.e("TrainingRequests", "trainingRequests.size = ${trainingRequests.size} ")
//            Log.e("TrainingRequests", "trainingRequests.first = ${trainingRequests.first()} ", )

        }

        isLoading = false
    })

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        trainingRequests.groupBy { it.program }.forEach { (program, trainingRequest) ->
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))
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
                            text = program.advisorName,
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
                            text = "(${trainingRequest.size}) request(s)",
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
            }
            items(trainingRequest) {
                TrainingRequestItem(item = it, onAccept = {
                    scope.launch {
                        isLoading = true
                        ManagerController.acceptTrainingRequest(it)
                        trainingRequests.remove(it)
                        isLoading = false
                    }
                }, onDeny = {
                    scope.launch {
                        isLoading = true
                        ManagerController.denyTrainingRequest(it)
                        trainingRequests.remove(it)
                        isLoading = false
                    }
                })
            }

        }
    }
    LoadingScreen(visibility = isLoading)

}

@Composable
fun TrainingRequestItem(
    item: TrainingRequest,
    onAccept: () -> Unit,
    onDeny: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 12.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp))
            .padding(vertical = 8.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Image(
            painter = rememberAsyncImagePainter(model = item.student.image),
            contentDescription = "User Image",
            modifier = Modifier
                .size(60.dp)
                .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .padding(2.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                text = item.student.name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$${item.program.price}",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                modifier = Modifier
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            OutlinedButton(onClick = { onDeny() }) {
                Icon(imageVector = Icons.Outlined.Cancel, contentDescription = "deny")
                Text(text = "Deny")
            }
            Button(onClick = { onAccept() }) {
                Icon(imageVector = Icons.Outlined.CheckCircle, contentDescription = "accept")
                Text(text = "Accept")
            }
        }
    }
}
