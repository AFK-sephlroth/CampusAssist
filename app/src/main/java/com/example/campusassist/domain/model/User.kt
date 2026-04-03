package com.example.campusassist.domain.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val department: String,
    val contactNumber: String,
    val role: UserRole,
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)

enum class UserRole(val displayName: String) {
    STUDENT("Student"),
    STAFF("Staff"),
    ADMIN("Admin")
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