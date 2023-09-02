package com.example.mcc.view.advisor

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.mcc.controller.AdvisorController
import com.example.mcc.controller.AuthController
import com.example.mcc.model.Meeting
import com.example.mcc.model.User
import com.example.mcc.view.Utils
import com.example.mcc.view.common.LoadingScreen
import java.text.SimpleDateFormat
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyStudentsScreen(navHostController: NavController) {
    val students = remember {
        mutableStateListOf<Pair<String, User>>()
    }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember {
        mutableStateOf(true)
    }
    LaunchedEffect(key1 = Unit, block = {
        if (students.isEmpty()) {
            val currentUserToken = Utils.getCurrentUserToken(context)
            val (advisorId, advisor) = AuthController.getUserByToken(currentUserToken)
                ?: ("" to null)
            students.addAll(AdvisorController.getStudentsOf(advisorId))
        }
        isLoading = false
    })
    LazyColumn(){
        item {
            CenterAlignedTopAppBar(title = { Text(text = "My Students") })
            Divider()
            Spacer(modifier = Modifier.height(20.dp))
        }
        items(students) {(programTitle,it) ->
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
                    painter = rememberAsyncImagePainter(model = it.image),
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
                        text = it.name,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = it.phone,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        modifier = Modifier
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = it.email,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = programTitle,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    modifier = Modifier,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
    LoadingScreen(visibility = isLoading)
}

