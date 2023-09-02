package com.example.mcc.controller

import android.net.Uri
import android.util.Log
import com.example.mcc.model.Attachment
import com.example.mcc.model.AttachmentType
import com.example.mcc.model.User
import com.example.mcc.model.UserRegistrationRequest
import com.example.mcc.model.UserType
import com.example.mcc.view.Utils
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object AuthController {
    const val TAG = "AuthController"
    suspend fun login(token: String): UserType {
        val db = Firebase.firestore
        Log.e(TAG, "login: token ($token)", )
        return withContext(Dispatchers.IO) {
            val query = db.collection("user").get().await()
            val map =
                query.documents.associate { it.get("token") as String to it.get("userType") as String }
            map.forEach { (key, value) ->
                Log.e(TAG, "login: key [$key], value: $value", )
            }
            Log.e(TAG, "login: map[token] (${map[token]})")

            when (map[token]) {
                UserType.Manager.title -> UserType.Manager
                UserType.Advisor.title -> UserType.Advisor
                UserType.Student.title -> UserType.Student
                else -> UserType.Undefined
            }
        }
    }

    suspend fun register(userRegistrationRequest: UserRegistrationRequest, attachments: List<Attachment?>): Boolean {
        val db = Firebase.firestore
        val usersRef = db.collection("registrationRequests")
        return withContext(Dispatchers.IO) {
            try {
                attachments.forEach {attachment->
                    attachment?.let{
                        val url = Utils.uploadFile(it.uri, it.type)
                        when (it.type) {
                            AttachmentType.IMAGE -> userRegistrationRequest.image = url
                            else -> userRegistrationRequest.attachmentUrl = url
                        }
                    }
                }
                val docRef = usersRef.add(userRegistrationRequest).await()
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

    }

    suspend fun getUserByToken(token: String): Pair<String, User>? {
        return withContext(Dispatchers.IO){
            val docs = Firebase.firestore.collection("user").get().await()
             docs.documents.find { it.get("token")==token}?.let{ userDoc->
                 userDoc.id to userDoc.toObject(User::class.java)!!
             }
        }
    }

    suspend fun getUserById(id: String): User? {
        return withContext(Dispatchers.IO){
            val docs = Firebase.firestore.collection("user").get().await()
            docs.documents.find { it.id == id}?.let{ userDoc->
                userDoc.toObject(User::class.java)!!
            }
        }
    }


}