package com.example.mcc.controller.notifications

import android.util.Log
import com.example.mcc.model.PushNotification
import java.lang.Exception

object NotificationController {
    suspend fun sendNotification(notification: PushNotification) {
        try {
            Log.e("Mah ", "sendNotification: [$notification]", )
            RetrofitInstance.api.postNotification(notification)
        } catch (e: Exception) {
            Log.e("Mah ", e.toString())
        }
    }
}