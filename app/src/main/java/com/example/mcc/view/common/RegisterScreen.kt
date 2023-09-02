package com.example.mcc.view.common

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.mcc.MCCApplication
import com.example.mcc.R
import com.example.mcc.controller.AuthController
import com.example.mcc.model.Attachment
import com.example.mcc.model.AttachmentType
import com.example.mcc.model.UserRegistrationRequest
import com.example.mcc.view.Utils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val image = remember { mutableStateOf<Uri?>(null) }
    val name = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }
    val phoneNumber = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val attachment = remember { mutableStateOf<Attachment?>(null) }
    val attachmentFileName = remember { mutableStateOf("") }
    val selectedUserType = remember { mutableStateOf("Student") }
    val selectedDiscipline = remember { mutableStateOf("programming") }
    val userTypes = remember { mutableStateListOf<String>("Student", "Advisor") }
    val disciplines = remember { mutableStateListOf<String>("programming", "graphic design", "video editing", "Data base specialist", "data analyst") }
    var isLoading by remember { mutableStateOf(false) }
    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                image.value = uri
            }
        }
    )
    val getContentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                attachmentFileName.value = Utils.getFileNameFromUri(context, uri)
                attachment.value = Attachment(
                    uri,
                    if (selectedUserType.value == "Student") AttachmentType.CERTIFICATE else AttachmentType.CV
                )
            }
        })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AsyncImage(
            model = image.value ?: R.drawable.ic_profile_placeholder,
            contentDescription = "Profile picture",
            modifier = Modifier
                .size(150.dp)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .padding(4.dp)
                .clip(CircleShape)
                .clickable {
                    singlePhotoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
            contentScale = ContentScale.Crop
        )

        OutlinedTextField(
            value = name.value,
            onValueChange = { name.value = it },
            label = { Text("Full name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = email.value,
            onValueChange = { email.value = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = phoneNumber.value,
            onValueChange = { phoneNumber.value = it },
            label = { Text("Phone number") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone
            ),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password.value,
            onValueChange = { password.value = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            modifier = Modifier.fillMaxWidth()
        )

        DropdownMenu(
            selectedItem = selectedUserType.value,
            items = userTypes,
            onSelectedItem = {
                selectedUserType.value = it
            },
            onSelectedItem2 = {
                selectedUserType.value = it
                attachment.value = null
                attachmentFileName.value = ""
            }
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        getContentLauncher.launch("application/pdf")
                    })
                }
        ) {
            OutlinedTextField(
                value = attachmentFileName.value,
                onValueChange = { attachmentFileName.value = it },
                label = { Text(if (selectedUserType.value == "Student") "Certificate" else "CV") },
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    disabledBorderColor = MaterialTheme.colorScheme.onBackground,
                    disabledLabelColor = MaterialTheme.colorScheme.onBackground,
                    disabledTextColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
        AnimatedVisibility(visible = selectedUserType.value == "Advisor") {
            DropdownMenu(
                selectedItem = selectedDiscipline.value,
                items = disciplines,
                title = "Discipline",
                onSelectedItem = {
                    selectedDiscipline.value = it
                },
                onSelectedItem2 = {
                    selectedDiscipline.value = it
                }
            )
        }

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            onClick = {
               val deviceToken:String = Utils.getDeviceToken(context)
                scope.launch {
                    isLoading = true
                    val isRegisterSuccess = AuthController.register(
                        userRegistrationRequest = UserRegistrationRequest(
                            userType = selectedUserType.value,
                            discipline = if(selectedUserType.value == "Advisor") selectedDiscipline.value else "",
                            name = name.value,
                            email = email.value,
                            phone = phoneNumber.value,
                            deviceToken = deviceToken,
                        ),
                        attachments = listOf(
                            attachment.value,
                            image.value?.let { Attachment(uri = it, type = AttachmentType.IMAGE) },
                        )
                    )
                    if (isRegisterSuccess) {
                        navController.popBackStack()
                        navController.navigate("register_pending_screen")
                    }else {
                        Toast.makeText(context, "Registration Failed", Toast.LENGTH_SHORT).show()
                    }
                    isLoading = false

                }
            }
        ) {
            Text("Sign Up")
        }

        Spacer(Modifier.weight(1f))

        TextButton(
            onClick = { navController.navigate("login_screen") },
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text("Have an account? Sign In")
        }
    }
    
    LoadingScreen(visibility = isLoading)
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenu(
    selectedItem: String,
    items: List<String>,
    title:String = "User Type",
    onSelectedItem: (String) -> Unit,
    onSelectedItem2: (String) -> Unit,
) {
    val expanded = remember { mutableStateOf(false) }

    Column {
        TextField(
            modifier = Modifier
                .fillMaxWidth(),
            readOnly = true,
            value = selectedItem,
            onValueChange = { onSelectedItem(it) },
            label = { Text(title) },
            trailingIcon = {
                IconButton(onClick = { expanded.value = !expanded.value }) {
                    Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "")
                }
            }
        )

        DropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = { Text(text = item) },
                    onClick = {
                        onSelectedItem2(item)
                        expanded.value = false
                    })
            }
        }
    }
}