package com.example.mcc.view.advisor

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.mcc.controller.AdvisorController
import com.example.mcc.model.Meeting
import com.example.mcc.model.MeetingRequest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingRequestsScreen(navHostController: NavController) {
    val requests = remember {
        mutableStateListOf<Meeting>()
    }
    val scope = rememberCoroutineScope()
    var isLoading by remember {
        mutableStateOf(true)
    }
    LaunchedEffect(key1 = Unit, block = {
        if (requests.isEmpty())
            requests.addAll(AdvisorController.getPendingMeetings())

        isLoading = false
    })

    LazyColumn(Modifier.fillMaxSize()) {
        item {
            CenterAlignedTopAppBar(title = { Text(text = "Meeting Requests") })
        }
        items(requests) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 8.dp)
                    .padding(vertical = 4.dp, horizontal = 12.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp))
                    .padding(vertical = 8.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(model = it.student.image),
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
                        text = it.student.name,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = it.program.title,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        modifier = Modifier
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "At: ${
                            SimpleDateFormat(
                                "dd MMM, hh:mm a",
                                Locale.getDefault()
                            ).format(it.requestedTime)
                        }",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = {
                        scope.launch {
                            isLoading = true
                            AdvisorController.denyMeeting(
                                meet = MeetingRequest(
                                    studentId = it.studentId,
                                    programId = it.programId,
                                    advisorId = it.advisorId,
                                    requestedTime = it.requestedTime,
                                    status = it.status
                                )
                            )
                            isLoading = false
                            requests.remove(it)
                        }
                    }) {
                        Icon(imageVector = Icons.Outlined.Cancel, contentDescription = "deny")
//                        Text(text = "Deny")
                    }
                    IconButton(onClick = {
                        scope.launch {
                            isLoading = true
                            AdvisorController.acceptMeeting(
                                MeetingRequest(
                                    studentId = it.studentId,
                                    programId = it.programId,
                                    advisorId = it.advisorId,
                                    requestedTime = it.requestedTime,
                                    status = it.status
                                )
                            )
                            isLoading = false
                            requests.remove(it)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = "accept"
                        )
//                        Text(text = "Accept")
                    }
                }
            }
        }

    }
}