package com.example.mcc.view.student

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.mcc.controller.AuthController
import com.example.mcc.controller.StudentController
import com.example.mcc.model.Attachment
import com.example.mcc.model.AttachmentType
import com.example.mcc.model.MeetingRequest
import com.example.mcc.model.User
import com.example.mcc.view.Utils
import com.example.mcc.view.common.DropdownMenu
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscribedProgramDetailsScreen(
    navController: NavController,
    programId: String,
    advisorId: String,
    coverPicture: String,
    title: String,
    description: String,
    price: String,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val advisor = remember { mutableStateOf<User?>(null) }
    val student = remember { mutableStateOf<Pair<String, User>?>(null) }
    var time by remember { mutableStateOf(Date().time + (24 * 60 * 60 * 1000)) }
    val sessionsList = listOf("Session 1", "Session 2", "Session 3", "Session 4", "Session 5")
    var isRequested by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var isDialogVisible by remember { mutableStateOf(false) }
    val attachment = remember { mutableStateOf<Attachment?>(null) }
    val attachmentFileName = remember { mutableStateOf("") }
    val getContentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                attachmentFileName.value = Utils.getFileNameFromUri(context, uri)
                attachment.value = Attachment(uri, AttachmentType.STUDENT_ATTACHMENT)
            }
        })
    val inputTypesMap = remember {
        mutableStateMapOf<String, String>(
            "Pdf" to "application/pdf",
            "Document" to "application/msword",
            "Image" to "image/*",
            "Video" to "video/*"
        )
    }
    var selectedType by remember {
        mutableStateOf("Pdf")
    }
    // on click 'select' button
    // getContentLauncher.launch(inputTypesMap[selectedType]?:"application/pdf")

    @Composable
    fun AdvisorInfo(
        advisor: AdvisorDetails,
        isSubscribed: Boolean,
        onClickCancel: () -> Unit,
        onClickApply: () -> Unit,
    ) {
        fun trackMeetingRequestEvent(status: Boolean) {
            val firebaseAnalytics = FirebaseAnalytics.getInstance(context)

            val eventName = if (status) "Meeting Request" else "Cancel Meeting"

            val params = Bundle().apply {
                putString("Button", eventName)
            }

            firebaseAnalytics.logEvent("Meeting_Request_Event", params)
        }
        Row(
            modifier = Modifier.fillMaxWidth(.95f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = advisor.image),
                contentDescription = "User Image",
                modifier = Modifier
                    .size(60.dp)
                    .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .padding(2.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(
                    text = advisor.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = advisor.phone,
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.secondary)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        if (isSubscribed) {
                            onClickCancel()
                            trackMeetingRequestEvent(false)
                        } else {
                            onClickApply()
                            trackMeetingRequestEvent(true)
                        }
                    }
                ) {
                    Text(if (isSubscribed) "Cancel" else "Request Meet", fontSize = 14.sp)
                }
                TextButton(enabled = !isRequested,onClick = {
                    Utils.showTimePicker(
                        label = "Select Meeting Time",
                        time = time,
                        activity = context as AppCompatActivity,
                        onTimeSelected = { hour, minute ->
                            val calender = Calendar.getInstance()
                            calender.timeInMillis = time


                            calender.set(Calendar.HOUR_OF_DAY, hour)
                            calender.set(Calendar.MINUTE, minute)
                            calender.set(Calendar.SECOND, 0)

                            time = calender.timeInMillis
                        }
                    )
                }) {
                    Text(
                        text = "Meet At: ${
                            SimpleDateFormat(
                                "dd MMM, hh:mm a",
                                Locale.getDefault()
                            ).format(time)
                        }",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.SemiBold,
                        fontStyle = if (isRequested) FontStyle.Italic else FontStyle.Normal,
                    )
                }
            }
        }
    }
    LaunchedEffect(key1 = Unit, block = {
        if (advisor.value == null) {
            isLoading = true
            advisor.value = AuthController.getUserById(advisorId)
            val currentUserToken = Utils.getCurrentUserToken(context)
            student.value =
                AuthController.getUserByToken(currentUserToken)// current user must be student
            Log.e("SubscribedProgramDetailsScreen", "getCurrentUserToken = $currentUserToken")
            Log.e("SubscribedProgramDetailsScreen", "advisorId = $advisorId")
            Log.e("SubscribedProgramDetailsScreen", "programId = $programId")
            Log.e(
                "SubscribedProgramDetailsScreen",
                "student.value.first = ${student.value?.first}",
            )
            Log.e(
                "SubscribedProgramDetailsScreen",
                "student.value.second = ${student.value?.second}",
            )

            StudentController.getMeetingRequests(programId) {
                val request = it.find { req -> req.studentId == student.value?.first }
                isRequested = request != null
            }
            isLoading = false
        }
    })

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        advisor.value?.let {

            AsyncImage(
                model = coverPicture,
                contentDescription = "Cover picture",
                modifier = Modifier
                    .fillMaxHeight(0.3f)
                    .fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
            Text(
                text = title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth(.9f)
                    .padding(top = 16.dp)
            )

            AdvisorInfo(
                advisor = AdvisorDetails(
                    image = it.image,
                    name = it.name,
                    phone = it.phone,
                    id = advisorId,
                ),
                isSubscribed = isRequested,
                onClickCancel = {
                    scope.launch {
                        StudentController.cancelMeetingRequest(
                            studentId = student.value?.first ?: "",
                            programId = programId,
                            advisorId = advisorId,
                        )
                        isRequested = false
                    }
                }
            ) {
                scope.launch {
                    val success = StudentController.requestMeeting(
                        MeetingRequest(
                            studentId = student.value?.first ?: "",
                            programId = programId,
                            advisorId = advisorId,
                            requestedTime = time,
                            status = "Pending"
                        )
                    )
                    if (success) {
                        isRequested = true
                    }
                }
            }
            Divider(
                modifier = Modifier.padding(top = 4.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(.9f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = {
                    isDialogVisible = true
                }) {
                    Icon(imageVector = Icons.Default.UploadFile, contentDescription = "upload file")
                    Text(text = "Submit A File")
                }
            }
            Text(
                text = "Attendance Form",
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                modifier = Modifier
                    .fillMaxWidth(.9f)
                    .padding(top = 12.dp, start = 8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .fillMaxWidth(0.9f)
                    .border(
                        1.dp, MaterialTheme.colorScheme.onBackground,
                        RoundedCornerShape(10.dp)
                    )
                    .padding(bottom = 8.dp), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val checkedList = remember {
                    mutableStateListOf(false, false, false, false, false)
                }
                sessionsList.forEachIndexed { index, s ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(.9f),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = s)
                        Checkbox(
                            checked = checkedList[index],
                            onCheckedChange = { isChecked -> checkedList[index] = isChecked })
                    }
                    Divider(
                        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                }
                Button(onClick = {
                    scope.launch {
                        StudentController.submitAttendanceForm(
                            student.value?.first ?: "",
                            programId,
                            sessionsList.zip(checkedList).toMap()
                        )
                    }
                }) {
                    Text(text = "Submit Form")
                }
            }

        }
    }

    if (isDialogVisible) {
        AlertDialog(
            title = {
                Text(text = "Upload a file")
            },
            text = {
                Column {
                    DropdownMenu(
                        selectedItem = selectedType,
                        items = inputTypesMap.keys.toList(),
                        title = "File Type",
                        onSelectedItem = {
                            selectedType = it
                        },
                        onSelectedItem2 = {
                            selectedType = it
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .pointerInput(Unit) {
                                detectTapGestures(onTap = {
                                    getContentLauncher.launch(
                                        inputTypesMap[selectedType] ?: "application/pdf"
                                    )
                                })
                            }
                    ) {
                        OutlinedTextField(
                            value = attachmentFileName.value,
                            onValueChange = { attachmentFileName.value = it },
                            label = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text("File")
                                    Spacer(Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Outlined.AttachFile,
                                        contentDescription = "attach file"
                                    )
                                }
                            },
                            enabled = false,
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                disabledBorderColor = MaterialTheme.colorScheme.onBackground,
                                disabledLabelColor = MaterialTheme.colorScheme.onBackground,
                                disabledTextColor = MaterialTheme.colorScheme.onBackground
                            )
                        )
                    }

                }
            },
            onDismissRequest = {
                isDialogVisible = false
                attachment.value = null
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            attachment.value?.let {
                                isLoading = true
                                StudentController.submitAttachment(
                                    student.value?.first ?: "",
                                    programId,
                                    advisorId,
                                    it
                                )
                                isLoading = false
                                isDialogVisible = false
                            }
                        }
                    }
                ) {
                    Text(
                        "Done",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        isDialogVisible = false
                        attachment.value = null
                    }
                ) {
                    Text(
                        "Cancel",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        )
    }
}



