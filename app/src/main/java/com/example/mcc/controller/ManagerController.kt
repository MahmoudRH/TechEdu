package com.example.mcc.controller

import android.util.Log
import com.example.mcc.BuildConfig
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import com.example.mcc.BuildConfig.EMAIL_PASSWORD
import com.example.mcc.controller.notifications.NotificationController
import com.example.mcc.model.NotificationData
import com.example.mcc.model.PushNotification
import com.example.mcc.model.TrainingProgram
import com.example.mcc.model.TrainingRequest
import com.example.mcc.model.User
import com.example.mcc.model.UserRegistrationRequest
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import kotlin.random.Random
import kotlin.text.StringBuilder

object ManagerController {
    suspend fun getRegistrationRequests(): List<UserRegistrationRequest> {
        val db = Firebase.firestore
        return withContext(Dispatchers.IO) {
            val query = db.collection("registrationRequests").get().await()
            Log.e("ManagerController", "documents.size: ${query.documents.size}")
            query.documents.mapNotNull { it.toObject(UserRegistrationRequest::class.java) }
        }
    }

    suspend fun acceptRegistrationRequest(request: UserRegistrationRequest) {
        val newUserToken = generateToken()
        val user = User(
            token = newUserToken,
            userType = request.userType,
            image = request.image,
            name = request.name,
            email = request.email,
            phone = request.phone,
            deviceToken = request.deviceToken,
            discipline = request.discipline,
            certificate = if (request.userType == "Student") request.attachmentUrl else "",
            cv = if (request.userType == "Advisor") request.attachmentUrl else ""
        )
        addNewUserToFirebase(user)
        notifyUser(user.deviceToken, "Registration Success", "Registration Request Got Accepted")
        sendEmailWithToken(user.name, user.email, user.token)
        removeRegistrationRequestFromFirebase(request)
    }


    private suspend fun removeRegistrationRequestFromFirebase(request: UserRegistrationRequest) {
        val db = Firebase.firestore
        val query = db.collection("registrationRequests").get().await()
        val refMap = query.documents.associateBy(
            { it.toObject(UserRegistrationRequest::class.java)?.deviceToken },
            { it.reference }
        )
        refMap[request.deviceToken]?.let {
            it.delete().await()
        }
    }

    private suspend fun notifyUser(
        deviceToken: String,
        title: String,
        message: String,
        messageImage: String = "",
    ) {
        NotificationController.sendNotification(
            PushNotification(
                notification = NotificationData(
                    title = title,
                    body = message,
                    image = messageImage
                ),
                to = deviceToken,
            )
        )
    }

    private suspend fun sendEmailWithToken(username: String, email: String, token: String) {
        val properties = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
            put("mail.smtp.host", "smtp.gmail.com")
            put("mail.smtp.socketFactory.port", "465")
        }

        val session = Session.getInstance(properties, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication("mhmoud.r.h1@gmail.com", EMAIL_PASSWORD)
            }
        })
        withContext(Dispatchers.IO) {
            try {
                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress("mhmoud.r.h1@gmail.com")) // Replace with your email
                    setRecipient(Message.RecipientType.TO, InternetAddress(email))
                    subject = "Tech Edu"
                    setContent(
                        """<div style = "font-family: Arial, sans-serif; font-size: 14px; color: #333;">
                        Dear $username,<br/><br/>
                       
                       Congratulations, <br/>
                       Your registration request was accepted!<br/>
                       Your token is: <b>$token</b><br/><br/>
                       
                       You can use this token to login to our application<br/>
                       Please don't share this with anyone.<br/><div\>
                       """.trimMargin(),
                        "text/html"
                    )

                }

                Transport.send(message)
            } catch (e: MessagingException) {
                e.printStackTrace()
            }
        }

    }

    private suspend fun sendEmailForProgramRegistration(
        username: String,
        email: String,
        programName: String,
        price: String,
    ) {
        val properties = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
            put("mail.smtp.host", "smtp.gmail.com")
            put("mail.smtp.socketFactory.port", "465")
        }

        val session = Session.getInstance(properties, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication("mhmoud.r.h1@gmail.com", EMAIL_PASSWORD)
            }
        })
        withContext(Dispatchers.IO) {
            try {
                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress("mhmoud.r.h1@gmail.com")) // Replace with your email
                    setRecipient(Message.RecipientType.TO, InternetAddress(email))
                    subject = "Tech Edu"
                    setContent(
                        """<div style = "font-family: Arial, sans-serif; font-size: 14px; color: #333;">
                        Dear $username,<br/><br/>
                       
                       Congratulations, <br/>
                       Your registration request for the training program <b>$programName</b> got accepted.<br/>
                       Your bill is: <b>$$price</b><br/><br/>
                       
                       Thank you for using our program<br/><br/>
                       Best Regards.<br/> 
                       Tech Edu Team.<br/><div\>
                       """.trimMargin(),
                        "text/html"
                    )

                }

                Transport.send(message)
            } catch (e: MessagingException) {
                e.printStackTrace()
            }
        }

    }

    private suspend fun addNewUserToFirebase(user: User): Boolean {
        val db = Firebase.firestore
        return withContext(Dispatchers.IO) {
            try {
                db.collection("user").add(user).await()
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    private fun generateToken(): String {
        val letters = "abcdefghijklmnopqrstuvwxyz"
        val numbers = "0123456789"
        val chars = StringBuilder().append(letters).append(numbers)
            .append(letters.toUpperCase(Locale.current)).toList()
        val token = StringBuilder()
        repeat(5) {
            val nextChar = chars.shuffled()[Random.nextInt(0, chars.lastIndex)]
            token.append(nextChar)
        }
        return token.toString()
    }

    suspend fun denyRegistrationRequest(request: UserRegistrationRequest) {
        notifyUser(request.deviceToken, "Registration Failed", "Registration Request Got Denied")
        removeRegistrationRequestFromFirebase(request)
    }

    suspend fun getAllRegisteredUsers(): List<User> {
        val db = Firebase.firestore
        return withContext(Dispatchers.IO) {
            db.collection("user").get().await()
                .mapNotNull { if (it.get("userType") != "0") it.toObject(User::class.java) else null }
        }
    }


    suspend fun getTrainingRequests(): List<TrainingRequest> {
        Log.e("ManagerController", "getTrainingRequests...")
        val db = Firebase.firestore
        return withContext(Dispatchers.IO) {
            val query =
                db.collection(StudentController.COLLECTION_SUBSCRIPTION_REQUESTS).get().await()
            Log.e("ManagerController", "getTrainingRequests, size: ${query.documents.size}")
            query.documents.mapNotNull {
                val programId = it.data?.values?.firstOrNull() as String? ?: ""
                val studentIds = it.data?.keys
//                Log.e("ManagerController", "getTrainingRequests mapping: studentId = $studentId ")
//                studentId?.let { stdId ->
//                    val programId = it.data?.get(stdId) as String
                Log.e(
                    "ManagerController",
                    "getTrainingRequests mapping: programId = $programId ",
                )
                studentIds?.let { stdIds ->
                    stdIds.mapNotNull { stdId ->
                        AuthController.getUserById(stdId)?.let { user ->
                            Log.e(
                                "ManagerController",
                                "getTrainingRequests mapping: student = $user "
                            )
                            val program = AdvisorController.getTrainingProgramById(programId)
                            Log.e(
                                "ManagerController",
                                "getTrainingRequests mapping: program = $program ",
                            )
                            TrainingRequest(
                                id = it.id,
                                student = user,
                                program = program,
                                programId = programId,
                                studentId = stdId
                            )

                        }
                    }
                }
                /* AuthController.getUserById(stdId)?.let { user ->
                     Log.e("ManagerController", "getTrainingRequests mapping: student = $user ")
                     val program = AdvisorController.getTrainingProgramById(programId)
                     Log.e(
                         "ManagerController",
                         "getTrainingRequests mapping: program = $program ",
                     )
                     TrainingRequest(
                         id = it.id,
                         student = user,
                         program = program,
                         programId = programId,
                         studentId = studentId
                     )*/

            }.flatten()
        }
    }

    suspend fun acceptTrainingRequest(item: TrainingRequest): Boolean {
        val db = Firebase.firestore
        return withContext(Dispatchers.IO) {
            val subscribeSuccess =
                AdvisorController.addProgramSubscribersInFireStore(item.programId, item.studentId)
            if (subscribeSuccess) {
                val deleteTask = db.collection(StudentController.COLLECTION_SUBSCRIPTION_REQUESTS)
                    .document(item.id).delete()
                sendEmailForProgramRegistration(
                    username = item.student.name,
                    email = item.student.email,
                    programName = item.program.title,
                    price = item.program.price
                )
                deleteTask.isSuccessful
            } else false

        }
    }

    suspend fun denyTrainingRequest(item: TrainingRequest): Boolean {
        val db = Firebase.firestore
        return withContext(Dispatchers.IO) {
            val deleteTask = db.collection(StudentController.COLLECTION_SUBSCRIPTION_REQUESTS)
                .document(item.id).delete()
            deleteTask.isSuccessful
        }
    }
}