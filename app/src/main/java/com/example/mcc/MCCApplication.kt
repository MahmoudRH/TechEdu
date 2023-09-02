package com.example.mcc

import android.app.Application
import android.content.Context
import android.util.Log
import com.google.android.material.color.DynamicColors
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MCCApplication : Application() {

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
        Log.e("MCCApplication", "onCreate: AppCreated!", )
        val preferences = getSharedPreferences(APP_SHARED_PREFS, Context.MODE_PRIVATE)
        GlobalScope.launch {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { token ->
                preferences.edit().apply {
                    putString(DEVICE_TOKEN, token.result)
                    Log.e("MCCApplication", "onCreate: device token: ${token.result}", )
                }.apply()
            }.addOnFailureListener {
                Log.e("MCCApplication", "addOnFailureListener: ${it.stackTrace}", )
            }
        }
    }
    companion object{
        const val APP_SHARED_PREFS = "mcc_app"
        const val DEVICE_TOKEN = "deviceToken"
        const val CURRENT_USER_TOKEN = "userToken"

    }
}