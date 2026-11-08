package com.example.model

data class SnackbarAlert(
    val id: String = "snack_${System.currentTimeMillis()}_${(100..999).random()}",
    val message: String,
    val actionLabel: String? = null,
    val iconEmoji: String? = null,
    val isEmergency: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
