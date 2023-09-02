package com.example.mcc.model

import android.net.Uri

data class Attachment(val uri: Uri, val type:AttachmentType)

enum class AttachmentType(val typeName: String) {
    IMAGE("image"),
    CERTIFICATE("certificate"),
    CV("cv"),
    STUDENT_ATTACHMENT("student_course_attachment")
}