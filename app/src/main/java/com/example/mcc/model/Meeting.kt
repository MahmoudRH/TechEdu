package com.example.mcc.model

data class Meeting(
    val student: User,
    val program: TrainingProgram,
    val advisorId: String = "",
    val studentId: String = "",
    val programId: String = "",
    val requestedTime: Long = 0L,
    val status: String = "",
)