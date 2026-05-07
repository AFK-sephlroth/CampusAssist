package com.example.campusassist.domain.model

data class User(
    val username: String,
    val fullname: String,
    val role: UserRole,
    val department: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val profileImageUri: String? = null
)

enum class UserRole(val displayName: String) {
    USER("User"),
    STAFF("Staff")
}

data class AppNotification(
    val id: Long = 0,
    val userId: String,
    val ticketId: Long,
    val title: String,
    val message: String,
    val type: NotificationType,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

enum class NotificationType(val displayName: String) {
    STATUS_CHANGE("Status Changed"),
    ASSIGNED("Ticket Assigned"),
    SYNC_COMPLETE("Sync Complete"),
    CONFLICT("Sync Conflict")
}
