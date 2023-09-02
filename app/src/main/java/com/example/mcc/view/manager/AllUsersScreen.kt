package com.example.mcc.view.manager

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier 
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.mcc.R
import com.example.mcc.controller.ManagerController
import com.example.mcc.model.User
import com.example.mcc.view.Utils

@Composable
fun AllUsersScreen(navController: NavController) {
    val students = remember { mutableStateListOf<User>() }
    val advisors = remember { mutableStateListOf<User>() }
    LaunchedEffect(key1 = Unit, block = {
        val allUsers = ManagerController.getAllRegisteredUsers().groupBy { it.userType }
        students.clear()
        advisors.clear()
        allUsers["Student"]?.let { students.addAll(it) }
        allUsers["Advisor"]?.let { advisors.addAll(it) }
    })

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            AnimatedVisibility(visible = !advisors.isEmpty()) {
                Text(
                    text = "Advisors",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(top = 32.dp)
                )
            }
        }
        items(advisors) {
            UserItem(it) {
                //TODO: navigate to userDetails
            }
            Divider(color = MaterialTheme.colorScheme.primary.copy(0.5f))
        }
        item {
            AnimatedVisibility(visible = !students.isEmpty()) {
                Text(
                    text = "Students",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
        items(students) {
            UserItem(it) {
                //TODO: navigate to userDetails
            }
            Divider(color = MaterialTheme.colorScheme.primary.copy(0.5f))
        }

    }
}

@Composable
private fun UserItem(item: User, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        val context = LocalContext.current
        val attachmentName = if (item.userType == "Student") "Certificate" else "CV"
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
                Spacer(modifier = Modifier.weight(1f))
            }
            OutlinedButton(onClick = {
                Utils.downloadFile(
                    context,
                    if(attachmentName == "CV") item.cv else item.certificate,
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
}