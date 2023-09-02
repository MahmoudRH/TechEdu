package com.example.mcc.controller.notifications

import android.content.SharedPreferences
import com.google.firebase.messaging.FirebaseMessagingService


class FirebaseService : FirebaseMessagingService() {

    val CHANNEL_ID = "my_notification_channel"

    companion object {
        var sharedPref: SharedPreferences? = null

        var token: String?
            get() {
                return sharedPref?.getString("token", "")
            }
            set(value) {
                sharedPref?.edit()?.putString("token", value)?.apply()
            }
    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        token = p0
    }
}