package com.example.mcc.view.manager

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.mcc.R
import com.example.mcc.controller.ManagerController
import com.example.mcc.model.AttachmentType
import com.example.mcc.model.User
import com.example.mcc.model.UserRegistrationRequest
import com.example.mcc.view.Utils
import kotlinx.coroutines.launch

@Composable
fun RegistrationRequests(navController: NavController) {
    val scope = rememberCoroutineScope()
    val studentRequests = remember { mutableStateListOf<UserRegistrationRequest>() }
    val advisorRequests = remember { mutableStateListOf<UserRegistrationRequest>() }

    LaunchedEffect(key1 = Unit, block = {
        val allRequests = ManagerController.getRegistrationRequests().groupBy { it.userType }
        studentRequests.clear()
        advisorRequests.clear()
        allRequests["Student"]?.let { studentRequests.addAll(it) }
        allRequests["Advisor"]?.let { advisorRequests.addAll(it) }
    })

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            AnimatedVisibility(visible = !advisorRequests.isEmpty()) {
                Text(
                    text = "Advisors Requests",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(top = 32.dp)
                )
            }
        }
        items(advisorRequests) {
            AnimatedVisibility(visible = !advisorRequests.isEmpty()) {
                RequestItem(
                    item = it,
                    onAccept = {
                        scope.launch {
                            ManagerController.acceptRegistrationRequest(it)
                            advisorRequests.remove(it)
                        }
                    }, onDeny = {
                        scope.launch {
                            ManagerController.denyRegistrationRequest(it)
                            advisorRequests.remove(it)
                        }
                    })
                Divider(color = MaterialTheme.colorScheme.primary.copy(0.5f))
            }
        }
        item {
            AnimatedVisibility(visible = !studentRequests.isEmpty()) {
                Text(
                    text = "Students Requests",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
        items(studentRequests) {
            AnimatedVisibility(visible = !studentRequests.isEmpty()) {
                RequestItem(
                    item = it,
                    onAccept = {
                        scope.launch {
                            ManagerController.acceptRegistrationRequest(it)
                            studentRequests.remove(it)
                        }
                    }, onDeny = {
                        scope.launch {
                            ManagerController.denyRegistrationRequest(it)
                            studentRequests.remove(it)
                        }
                    })
                Divider(color = MaterialTheme.colorScheme.primary.copy(0.5f))
            }
        }

    }
}


@Composable
private fun RequestItem(
    item: UserRegistrationRequest,
    onAccept: () -> Unit = {},
    onDeny: () -> Unit = {},
) {
    val context = LocalContext.current
    val attachmentName = if (item.userType == "Student") "Certificate" else "CV"

    Column(
        Modifier
            .padding(bottom = 24.dp)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp))
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = item.image,
                contentDescription = "Profile picture",
                placeholder = painterResource(id = R.drawable.ic_profile_placeholder),
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(80.dp)
                    .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .padding(4.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.height(80.dp)) {
                Text(text = item.name, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.weight(1f))
                Text(text = item.phone, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.weight(1f))
                Text(text = item.email, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if(item.userType == "Advisor"){
                    Text(text = item.discipline, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                OutlinedButton(onClick = {
                    Utils.downloadFile(
                        context,
                        item.attachmentUrl,
                        item.name,
                        attachmentName
                    )
                }) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = attachmentName)
                        Spacer(modifier = Modifier.height(8.dp))
                        Icon(
                            imageVector = Icons.Outlined.FileDownload,
                            contentDescription = "download"
                        )
                    }
                }
            }
        }
        Button(
            onClick = onAccept, modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(text = "Accept")
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = onDeny, modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(text = "Deny")
        }
        Spacer(modifier = Modifier.height(12.dp))

    }
}

