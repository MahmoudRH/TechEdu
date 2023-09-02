package com.example.mcc.view.student

import android.os.Bundle
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.mcc.controller.AdvisorController
import com.example.mcc.controller.AuthController
import com.example.mcc.controller.StudentController
import com.example.mcc.model.User
import com.example.mcc.view.Utils
import com.example.mcc.view.common.LoadingScreen
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.launch

@Composable
fun TrainingDetailsScreen(
    navController: NavController,
    programId:String,
    advisorId:String,
    coverPicture:String,
    title:String,
    description:String,
    price:String,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val advisor = remember { mutableStateOf<User?>(null) }
    val student = remember {
        mutableStateOf<Pair<String, User>?>(null)
    }
    var isLoading by remember { mutableStateOf(false) }
    
    val isUserSubscriber = remember { mutableStateOf(false) }

    LaunchedEffect(key1 = Unit, block = {
        if (advisor.value == null) {
            isLoading = true
            advisor.value = AuthController.getUserById(advisorId)
            val currentUserToken = Utils.getCurrentUserToken(context)
            student.value = AuthController.getUserByToken(currentUserToken)// current user must be student

            Log.e("TrainingDetailsScreen", "TrainingDetailsScreen: getCurrentUserToken = $currentUserToken", )
            Log.e("TrainingDetailsScreen", "TrainingDetailsScreen: advisorId = $advisorId", )
            Log.e("TrainingDetailsScreen", "TrainingDetailsScreen: programId = $programId", )
            Log.e("TrainingDetailsScreen", "TrainingDetailsScreen: student.value.first = ${student.value?.first}", )
            Log.e("TrainingDetailsScreen", "TrainingDetailsScreen: student.value.second = ${student.value?.second}", )


            StudentController.getSubscriptionRequests(programId){
                isUserSubscriber.value = it.keys.contains(student.value?.first)
            }
            isLoading = false
        }
    })
    Box(modifier = Modifier.fillMaxSize()) {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            advisor.value?.let {

                AsyncImage(
                    model = coverPicture,
                    contentDescription = "Cover picture",
                    modifier = Modifier
                        .fillMaxHeight(0.3f)
                        .fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )
                AdvisorInfo(
                    advisor = AdvisorDetails(
                        image = it.image,
                        name = it.name,
                        phone = it.phone,
                        id = advisorId,
                    ),
                    programPrice= price,
                    isSubscribed = isUserSubscriber,
                    onClickUnCancel = {
                        StudentController.removeSubscriptionFromFireStore(
                            programId = programId,
                            userId = student.value?.first?:"",
                        ) { success ->
                            if (success) {
                                isUserSubscriber.value = false
                            }
                        }
                    }
                ){
                    scope.launch {
                        StudentController.applyForProgramInFireStore(
                            programId = programId,
                            studentId = student.value?.first?:""
                        ) { success ->
                            if (success) {
                                isUserSubscriber.value = true
                            }
                        }
                    }
                }
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = description,
                    fontSize = 14.sp
                )
            }

        }
        LoadingScreen(visibility = isLoading)
    }
}

@Composable
private fun AdvisorInfo(
    advisor: AdvisorDetails,
    programPrice:String,
    isSubscribed: MutableState<Boolean>,
    onClickUnCancel: () -> Unit,
    onClickApply: () -> Unit,
) {
    val context = LocalContext.current

    fun trackSubscriptionEvent(isSubscribed: Boolean) {
        val firebaseAnalytics = FirebaseAnalytics.getInstance(context)

        val eventName = if (isSubscribed) "Apply" else "Cancel Application"

        val params = Bundle().apply {
            putString("Button", eventName)
        }

        firebaseAnalytics.logEvent("Subscription_Event", params)
    }
    Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberAsyncImagePainter(model = advisor.image),
            contentDescription = "User Image",
            modifier = Modifier
                .size(50.dp)
                .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .padding(2.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Column(modifier = Modifier.padding(start = 16.dp)) {
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
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Button(
                onClick = {
                    if (isSubscribed.value) {
                        onClickUnCancel()
                        trackSubscriptionEvent(false) // Unsubscribe event tracked
                    } else {
                        onClickApply()
                        trackSubscriptionEvent(true) // Subscribe event tracked
                    }
                }
            ) {
                Text(if (isSubscribed.value) "Cancel" else "Apply", fontSize = 14.sp)
            }
            Text(text = if(programPrice.startsWith("$")) programPrice else "$$programPrice", color = MaterialTheme.colorScheme.primary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

data class AdvisorDetails(val image: String, val name: String, val phone: String, val id: String)
