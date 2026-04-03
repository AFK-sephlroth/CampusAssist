package com.example.campusassist.domain.model

data class ServiceTicket(
    val id: Long = 0,
    val title: String,
    val description: String,
    val category: ServiceCategory,
    val priority: TicketPriority,
    val status: TicketStatus = TicketStatus.PENDING,
    val isSynced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val departmentId: Long? = null
)

enum class ServiceCategory(val displayName: String) {
    IT("IT Support"),
    FACILITIES("Facilities"),
    LIBRARY("Library")
}

enum class TicketPriority(val displayName: String) {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High")
}

enum class TicketStatus(val displayName: String) {
    PENDING("Pending"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed")
}