package com.example.mcc.model

data class User(
    val token: String = "",
    val userType: String = "",
    var image: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val deviceToken: String = "",
    var certificate: String = "",
    val discipline: String = "",
    var cv: String = "",
)

data class UserRegistrationRequest(
    val userType: String = "",
    val discipline: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val deviceToken: String = "",
    var image: String = "",
    var attachmentUrl: String = "",
)

enum class UserType(val title: String) {
    Manager("Manager"),
    Advisor("Advisor"),
    Student("Student"),
    Undefined("Else")
}