package com.example.model

data class InAppNotification(
    val id: String = "notif_${System.currentTimeMillis()}_${(1000..9999).random()}",
    val title: String,
    val message: String,
    val iconEmoji: String = "🔔",
    val type: NotificationType = NotificationType.INFO,
    val timestamp: Long = System.currentTimeMillis(),
    val durationMs: Long = 4000L,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null
)

enum class NotificationType {
    INFO,
    SUCCESS,
    WARNING,
    ALERT,
    TRAFFIC
}
