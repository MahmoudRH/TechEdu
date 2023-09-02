package com.example.mcc.view

import android.app.DownloadManager
import android.app.TimePickerDialog
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.mcc.MCCApplication
import com.example.mcc.R
import com.example.mcc.model.AttachmentType
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object Utils {
    fun getFileNameFromUri(context: Context, uri: Uri): String {
        var fileName = ""
        val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)

        cursor?.use {
            if (it.moveToFirst()) {
                val displayNameColumnIndex: Int = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (displayNameColumnIndex != -1) {
                    fileName = it.getString(displayNameColumnIndex)
                }
            }
        }

        cursor?.close()
        return fileName
    }

    fun getDeviceToken(context: Context): String {
        val prefs =
            context.getSharedPreferences(MCCApplication.APP_SHARED_PREFS, Context.MODE_PRIVATE)
        return prefs.getString(MCCApplication.DEVICE_TOKEN, "") ?: ""
    }

    fun downloadFile(
        context: Context,
        attachmentUrl: String,
        username: String,
        attachmentName: String,
    ): Long {
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri = Uri.parse(attachmentUrl)
        val request = DownloadManager.Request(uri)
            .setTitle("${username}_${attachmentName}")
            .setDescription("File is being downloaded")
            .setMimeType("application/pdf")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(
                context,
                Environment.DIRECTORY_DOWNLOADS,
                "${username}_${attachmentName}.pdf"
            )
        return dm.enqueue(request)
    }

    suspend fun uploadFile(uri: Uri, type: AttachmentType): String {
        val storageRef =
            Firebase.storage.reference.child("${type.typeName}/${System.currentTimeMillis()}_${uri.lastPathSegment}")
        return storageRef.putFile(uri)
            .await() // Wait for the upload to complete
            .storage
            .downloadUrl
            .await() // Get the download URL for the uploaded file
            .toString()
    }

    fun getCurrentUserToken(context: Context): String {
        val prefs =
            context.getSharedPreferences(MCCApplication.APP_SHARED_PREFS, Context.MODE_PRIVATE)
        return prefs.getString(MCCApplication.CURRENT_USER_TOKEN, "") ?: ""
    }

    fun saveUserToken(context: Context, token: String) {
        val preferences =
            context.getSharedPreferences(MCCApplication.APP_SHARED_PREFS, Context.MODE_PRIVATE)
        preferences.edit().apply {
            putString(MCCApplication.CURRENT_USER_TOKEN, token)
            Log.e("Utils", "saveUserToken: current user token: $token")
        }.apply()
    }

    fun getAvailableCategories(): List<String> {
        return listOf("Programming", "Graphic design", "Video editing", "Database", "Data Analysis")
    }

    fun showDatePicker(
        label: String,
        value: Long,
        activity: AppCompatActivity?,
        onDateSelected: (String) -> Unit,
    ) {
        val picker = MaterialDatePicker.Builder.datePicker().setTitleText(label)
            .setSelection(value).build()
        activity?.let {
            picker.show(it.supportFragmentManager, picker.toString())
            picker.addOnPositiveButtonClickListener { timestamp ->
                onDateSelected(timestamp.toString())
            }
        }
    }

    fun showTimePicker(
        label: String,
        time: Long,
        activity: AppCompatActivity,
        onTimeSelected: (Int,Int) -> Unit,
    ) {
        val hour = SimpleDateFormat("hh", Locale("en")).format(time).toIntOrNull()?:0
        val minute = SimpleDateFormat("mm", Locale("en")).format(time).toIntOrNull()?:0
        MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(hour)
            .setMinute(minute)
            .setTitleText(label)
            .build().apply {
                addOnPositiveButtonClickListener {
                    onTimeSelected(this.hour,this.minute)
                }
            }
            .show(activity.supportFragmentManager, "activityXpicker")
    }

}

object Temporary {
    const val ManagerToken = "q6XuB"
    const val AdvisorToken = "zF112"
    const val StudentToken = "v3G8G"
}

