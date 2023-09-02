package com.example.mcc.model

data class MeetingRequest(
    val studentId: String = "",
    val programId: String = "",
    val advisorId: String = "",
    val requestedTime: Long = 0L,
    val status: String = "",
)