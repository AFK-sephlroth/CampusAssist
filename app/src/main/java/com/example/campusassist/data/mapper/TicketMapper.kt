package com.example.campusassist.data.mapper

import com.example.campusassist.data.local.entity.ServiceTicketEntity
import com.example.campusassist.domain.model.ServiceCategory
import com.example.campusassist.domain.model.ServiceTicket
import com.example.campusassist.domain.model.TicketPriority
import com.example.campusassist.domain.model.TicketStatus

fun ServiceTicketEntity.toDomain(): ServiceTicket = ServiceTicket(
    id             = id,
    title          = title,
    description    = description,
    category       = ServiceCategory.valueOf(category),
    priority       = TicketPriority.valueOf(priority),
    status         = TicketStatus.entries.firstOrNull { it.displayName == status } ?: TicketStatus.PENDING,
    isSynced       = isSynced,
    createdAt      = createdAt,
    updatedAt      = updatedAt,
    departmentId   = departmentId,
    notes          = notes,
    attachmentUris = attachmentUris,
    firestoreId    = firestoreId,
    createdBy      = createdBy
)

fun ServiceTicket.toEntity(): ServiceTicketEntity = ServiceTicketEntity(
    id             = id,
    title          = title,
    description    = description,
    category       = category.name,
    priority       = priority.name,
    status         = status.displayName,
    isSynced       = isSynced,
    createdAt      = createdAt,
    updatedAt      = updatedAt,
    departmentId   = departmentId,
    notes          = notes,
    attachmentUris = attachmentUris,
    firestoreId    = firestoreId,
    createdBy      = createdBy
)

// ── Firestore map ↔ domain ────────────────────────────────────────────────────

fun ServiceTicket.toFirestoreMap(): Map<String, Any?> = mapOf(
    "localId"        to id,
    "title"          to title,
    "description"    to description,
    "category"       to category.name,
    "priority"       to priority.name,
    "status"         to status.displayName,
    "createdAt"      to createdAt,
    "updatedAt"      to updatedAt,
    "departmentId"   to departmentId,
    "notes"          to notes,
    "attachmentUris" to attachmentUris,
    "createdBy"      to createdBy
)

fun Map<String, Any?>.toServiceTicketEntity(): ServiceTicketEntity {
    val statusStr = this["status"] as? String ?: "Pending"
    return ServiceTicketEntity(
        id             = (this["localId"] as? Long) ?: 0L,
        title          = this["title"] as? String ?: "",
        description    = this["description"] as? String ?: "",
        category       = this["category"] as? String ?: "IT",
        priority       = this["priority"] as? String ?: "MEDIUM",
        status         = statusStr,
        isSynced       = true,
        createdAt      = (this["createdAt"] as? Long) ?: System.currentTimeMillis(),
        updatedAt      = (this["updatedAt"] as? Long) ?: System.currentTimeMillis(),
        departmentId   = (this["departmentId"] as? Long),
        notes          = this["notes"] as? String,
        attachmentUris = this["attachmentUris"] as? String,
        firestoreId    = this["firestoreId"] as? String,
        createdBy      = this["createdBy"] as? String
    )
}
