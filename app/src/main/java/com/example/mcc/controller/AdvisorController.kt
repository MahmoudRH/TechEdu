package com.example.mcc.controller

import android.util.Log
import com.example.mcc.controller.notifications.NotificationController
import com.example.mcc.model.Attachment
import com.example.mcc.model.Meeting
import com.example.mcc.model.MeetingRequest
import com.example.mcc.model.NotificationData
import com.example.mcc.model.PushNotification
import com.example.mcc.model.TrainingProgram
import com.example.mcc.model.User
import com.example.mcc.view.Utils
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object AdvisorController {
    const val COLLECTION_TRAINING_PROGRAMS = "trainingPrograms"
    const val COLLECTION_PROGRAM_SUBSCRIBERS = "programSubscribers"
    const val TAG = "AdvisorController"

    suspend fun addTrainingProgram(
        trainingProgram: TrainingProgram,
        attachment: Attachment,
    ): Boolean {
        val db = Firebase.firestore
        return withContext(Dispatchers.IO) {
            try {
                val url = Utils.uploadFile(attachment.uri, attachment.type)
                trainingProgram.coverPicture = url
                db.collection(COLLECTION_TRAINING_PROGRAMS).add(trainingProgram).await()
                true
            } catch (e: Exception) {
                Log.e(TAG, "addTrainingProgram: ${e.stackTrace}")
                false
            }
        }
    }

    suspend fun getProgramSubscribers(
        programId: String,
    ): Map<String, String> {
        val db = Firebase.firestore
        return withContext(Dispatchers.IO) {
            val document =
                db.collection(COLLECTION_TRAINING_PROGRAMS).document(programId).get().await()
            if (document != null && document.exists()) {
                val programSubscribersRef = document.getDocumentReference("subscribers")
                programSubscribersRef?.let { ref ->
                    val subscribersDoc = ref.get().await()
                    subscribersDoc.data as HashMap<String, String>
                } ?: mapOf()
            } else mapOf()
        }

    }

    suspend fun getTrainingProgramById(id: String): TrainingProgram {
        val db = Firebase.firestore
        return withContext(Dispatchers.IO) {
            db.collection(COLLECTION_TRAINING_PROGRAMS).get().await().documents
                .filter { documentSnapshot -> documentSnapshot.id == id }
                .map { documentSnapshot -> documentSnapshot.toObject(TrainingProgram::class.java)!! }
                .first()
        }
    }

    suspend fun addProgramSubscribersInFireStore(
        programId: String,
        studentId: String,
    ): Boolean {
        val db = Firebase.firestore
        val student = AuthController.getUserById(studentId)
        val userName = student?.name
        val programRef = db.collection(COLLECTION_TRAINING_PROGRAMS).document(programId)
        val programTask = programRef.get()
        val newSubscriber = hashMapOf(studentId to userName)
        programTask.await()
        if (programTask.isSuccessful) {
            val document = programTask.result
            if (document != null && document.exists()) {
                val programSubscribersRef = document.getDocumentReference("subscribers")
                return if (programSubscribersRef == null) {
                    val newSubscriberTask =
                        db.collection(COLLECTION_PROGRAM_SUBSCRIBERS).add(newSubscriber)
                    newSubscriberTask.await()
                    if (newSubscriberTask.isSuccessful) {
                        programRef.update("subscribers", newSubscriberTask.result)
                        true
                    } else
                        false
                } else {
                    programSubscribersRef.update(newSubscriber as Map<String, String>)
                    true
                }
            } else
                return false

        } else return false
    }


    fun removeSubscriberFromFireStore(
        programId: String,
        userId: String,
        callback: (Boolean) -> Unit,
    ) {
        val db = Firebase.firestore
        val programRef = db.collection(COLLECTION_TRAINING_PROGRAMS).document(programId)

        programRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document != null && document.exists()) {
                    val programSubscribersRef = document.getDocumentReference("subscribers")
                    programSubscribersRef?.let {
                        val updates = hashMapOf<String, Any>(userId to FieldValue.delete())
                        it.update(updates)
                        callback(true)
                    } ?: callback(false)
                }
            }
        }
    }

    suspend fun getAllTrainingPrograms(): List<Pair<String, TrainingProgram>> {
        val db = Firebase.firestore
        return withContext(Dispatchers.IO) {
            db.collection(COLLECTION_TRAINING_PROGRAMS).get()
                .await().documents.map { documentSnapshot ->
                    documentSnapshot.id to documentSnapshot.toObject(TrainingProgram::class.java)!!
                }
        }
    }

    suspend fun getTrainingProgramsOf(advisorId: String): List<Pair<TrainingProgram, Int>> {
        val db = Firebase.firestore
        return withContext(Dispatchers.IO) {

            db.collection(COLLECTION_TRAINING_PROGRAMS).get().await()
                .documents
                .filter { it.get("advisorId") == advisorId }
                .map { documentSnapshot ->
                    val numOfSubscribers = getProgramSubscribers(documentSnapshot.id).size
                    documentSnapshot.toObject(TrainingProgram::class.java)!! to numOfSubscribers
                }
        }
    }

    suspend fun getStudentsOf(advisorId: String): List<Pair<String,User>>  {
        val db = Firebase.firestore
        return withContext(Dispatchers.IO) {

            db.collection(COLLECTION_TRAINING_PROGRAMS).get().await()
                .documents
                .filter { it.get("advisorId") == advisorId }
                .map { documentSnapshot ->
                    val subscribersMap = getProgramSubscribers(documentSnapshot.id).mapNotNull {
                         AuthController.getUserById(it.key)?.let{
                             documentSnapshot.get("title") as String to it
                        }
                    }
                    subscribersMap
                }.flatten()
        }
    }

    suspend fun acceptMeeting(meet: MeetingRequest) {
        val db = Firebase.firestore
        withContext(Dispatchers.IO) {
            val meetingRequestsCollection = db.collection(StudentController.COLLECTION_MEETINGS)
            val querySnapshot = meetingRequestsCollection
                .whereEqualTo("studentId", meet.studentId)
                .whereEqualTo("programId", meet.programId)
                .whereEqualTo("advisorId", meet.advisorId)
                .get()
                .await()
            AuthController.getUserById(meet.studentId)?.let { user ->
                val program = getTrainingProgramById(meet.programId)
                notifyUser(
                    deviceToken = user.deviceToken,
                    title = "Meeting Accepted!",
                    message = "${user.name}, your meeting for program  \"${program.title}\" with \"${program.advisorName}\" got accepted. Congrats",
                    messageImage = ""
                )
            }
            for (document in querySnapshot.documents) {
                document.reference.update("status", "Accepted").await()
            }
        }
    }

    suspend fun denyMeeting(meet: MeetingRequest) {
        val db = Firebase.firestore
        withContext(Dispatchers.IO) {
            val meetingRequestsCollection = db.collection(StudentController.COLLECTION_MEETINGS)
            val querySnapshot = meetingRequestsCollection
                .whereEqualTo("studentId", meet.studentId)
                .whereEqualTo("programId", meet.programId)
                .whereEqualTo("advisorId", meet.advisorId)
                .get()
                .await()
            for (document in querySnapshot.documents) {
                document.reference.delete().await()
            }
        }
    }

    suspend fun getPendingMeetings(): List<Meeting> {

        val db = Firebase.firestore
        return withContext(Dispatchers.IO) {
            val query = db.collection(StudentController.COLLECTION_MEETINGS).get().await()
            val meetingRequests =
                query.documents.mapNotNull { it.toObject(MeetingRequest::class.java) }
                    .filter { it.status == "Pending" }
            meetingRequests.mapNotNull {
                AuthController.getUserById(it.studentId)?.let { user ->
                    val program = getTrainingProgramById(it.programId)

                    Meeting(
                        student = user,
                        program = program,
                        advisorId = it.advisorId,
                        requestedTime = it.requestedTime,
                        status = it.status,
                        programId = it.programId,
                        studentId = it.studentId
                    )
                }
            }

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


}