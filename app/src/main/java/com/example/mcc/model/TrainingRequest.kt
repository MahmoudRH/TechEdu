package com.example.mcc.model

data class TrainingRequest(
    val id:String,
    val studentId:String,
    val programId:String,
    val student:User,
    val program:TrainingProgram,
)
