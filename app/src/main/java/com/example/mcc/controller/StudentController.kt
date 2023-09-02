package com.example.mcc.controller

import android.util.Log
import com.example.mcc.model.Attachment
import com.example.mcc.model.MeetingRequest
import com.example.mcc.model.TrainingProgram
import com.example.mcc.view.Utils
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object StudentController {
    const val COLLECTION_SUBSCRIPTION_REQUESTS = "subscriptionRequests"
    const val COLLECTION_MEETINGS = "meetings"
    const val COLLECTION_ATTENDANCE_FORMS = "attendanceForms"
    const val COLLECTION_STUDENT_ATTACHMENTS = "studentAttachments"


    suspend fun submitAttachment(
        studentId: String,
        programId: String,
        advisorId: String,
        attachment: Attachment
    ){
        val db = Firebase.firestore
        withContext(Dispatchers.IO){
          val url =  Utils.uploadFile(attachment.uri,attachment.type)
            db.collection(COLLECTION_STUDENT_ATTACHMENTS).add(
                mapOf(
                    "studentId" to studentId,
                    "programId" to programId,
                    "advisorId" to advisorId,
                    "fileType" to attachment.type.typeName,
                    "fileUrl" to url
                )
            ).await()

        }
    }

    // upload file function
    suspend fun submitAttendanceForm(
        studentId: String,
        programId: String,
        attendanceData: Map<String, Boolean>,
    ) {
        val db = Firebase.firestore
        val attendanceFormData = hashMapOf(
            "studentId" to studentId,
            "programId" to programId,
            "attendanceData" to attendanceData
        )

        withContext(Dispatchers.IO) {
            db.collection(COLLECTION_ATTENDANCE_FORMS).add(attendanceFormData)
        }
    }

    suspend fun requestMeeting(request: MeetingRequest): Boolean {
        val db = Firebase.firestore

        return withContext(Dispatchers.IO) {
            val x = db.collection(COLLECTION_MEETINGS)
                .whereEqualTo("requestedTime", request.requestedTime).get().await()
            if (x.isEmpty) {
                db.collection(COLLECTION_MEETINGS).add(request)
                true
            } else {
                false
            }
        }
    }

    suspend fun cancelMeetingRequest(studentId: String, programId: String, advisorId: String) {
        val db = Firebase.firestore
        withContext(Dispatchers.IO) {
            val querySnapshot = db.collection(COLLECTION_MEETINGS)
                .whereEqualTo("studentId", studentId)
                .whereEqualTo("programId", programId)
                .whereEqualTo("advisorId", advisorId)
                .get()
                .await()

            for (document in querySnapshot.documents) {
                document.reference.delete()
            }
        }
    }

    suspend fun applyForProgramInFireStore(
        programId: String,
        studentId: String,
        callback: (Boolean) -> Unit,
    ) {
        val db = Firebase.firestore
        val programRef =
            db.collection(AdvisorController.COLLECTION_TRAINING_PROGRAMS).document(programId)
        val subscriberRequest = hashMapOf(studentId to programId)
        programRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document != null && document.exists()) {
                    val programSubscribersRef =
                        document.getDocumentReference("subscriptionRequests")
                    if (programSubscribersRef == null) {
                        db.collection(COLLECTION_SUBSCRIPTION_REQUESTS).add(subscriberRequest)
                            .addOnSuccessListener { newDocument ->
                                programRef.update("subscriptionRequests", newDocument)
                                callback(true)
                            }.addOnFailureListener {
                                callback(false)
                            }
                    } else {
                        programSubscribersRef.update(subscriberRequest as Map<String, String>)
                        callback(true)
                    }
                }
            }
        }
    }

    fun removeSubscriptionFromFireStore(
        programId: String,
        userId: String,
        callback: (Boolean) -> Unit,
    ) {
        val db = Firebase.firestore
        val programRef =
            db.collection(AdvisorController.COLLECTION_TRAINING_PROGRAMS).document(programId)

        programRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document != null && document.exists()) {
                    val programSubscribersRef =
                        document.getDocumentReference("subscriptionRequests")
                    programSubscribersRef?.let {
                        val updates = hashMapOf<String, Any>(userId to FieldValue.delete())
                        it.update(updates)
                        callback(true)
                    } ?: callback(false)
                }
            }
        }
    }

    fun getSubscriptionRequests(programId: String, callback: (HashMap<String, String>) -> Unit) {
        val db = Firebase.firestore
        db.collection(AdvisorController.COLLECTION_TRAINING_PROGRAMS).document(programId).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document != null && document.exists()) {
                        val programSubscribersRef =
                            document.getDocumentReference("subscriptionRequests")
                        programSubscribersRef?.let { ref ->
                            ref.get().addOnSuccessListener {
                                it.data?.let { map -> callback(map as HashMap<String, String>) }
                            }
                        }
                    }
                }
            }
    }

    suspend fun getMeetingRequests(programId: String, callback: (List<MeetingRequest>) -> Unit) {
        val db = Firebase.firestore
        withContext(Dispatchers.IO) {
            val query = db.collection(COLLECTION_MEETINGS).get().await()
            val result = query.documents.filter {
                it.get("programId") == programId
            }.mapNotNull { it.toObject(MeetingRequest::class.java) }
            callback(result)
        }

    }

    suspend fun getTrainingProgramsOf(studentId: String): List<Pair<String, TrainingProgram>> {
        val db = Firebase.firestore
        return withContext(Dispatchers.IO) {
            db.collection(AdvisorController.COLLECTION_TRAINING_PROGRAMS).get().await()
                .documents
                .filter { documentSnapshot ->
                    isStudentSubscribed(documentSnapshot.id, studentId)
                }
                .map { documentSnapshot ->
                    documentSnapshot.id to documentSnapshot.toObject(TrainingProgram::class.java)!!
                }
        }
    }

    private suspend fun isStudentSubscribed(
        programId: String,
        studentId: String,
    ): Boolean {
        Log.e(
            "StudentController1",
            "isStudentSubscribed: programId: $programId, studentId: $studentId",
        )
        val db = Firebase.firestore
        val document =
            db.collection(AdvisorController.COLLECTION_TRAINING_PROGRAMS).document(programId).get()
                .await()
        return withContext(Dispatchers.IO) {
            if (document != null && document.exists()) {
                document.data?.forEach { (key, value) ->
                    Log.e("StudentController2", "isStudentSubscribed: [$key]:$value,")
                }
                val programSubscribersRef = document.getDocumentReference("subscribers")
                programSubscribersRef?.let { ref ->
                    val x = ref.get().await()
                    !(x.data?.get(studentId) as? String).isNullOrBlank()
                } ?: false
            } else false
        }

    }
}