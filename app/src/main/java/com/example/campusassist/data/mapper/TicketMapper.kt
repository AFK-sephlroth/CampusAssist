package com.example.campusassist.data.mapper

import com.example.campusassist.data.local.entity.ServiceTicketEntity
import com.example.campusassist.domain.model.ServiceCategory
import com.example.campusassist.domain.model.ServiceTicket
import com.example.campusassist.domain.model.TicketPriority
import com.example.campusassist.domain.model.TicketStatus

fun ServiceTicketEntity.toDomain(): ServiceTicket = ServiceTicket(
    id = id,
    title = title,
    description = description,
    category = ServiceCategory.valueOf(category),
    priority = TicketPriority.valueOf(priority),
    status = TicketStatus.entries.firstOrNull { it.displayName == status } ?: TicketStatus.PENDING,
    isSynced = isSynced,
    createdAt = createdAt,
    updatedAt = updatedAt,
    departmentId = departmentId
)

fun ServiceTicket.toEntity(): ServiceTicketEntity = ServiceTicketEntity(
    id = id,
    title = title,
    description = description,
    category = category.name,
    priority = priority.name,
    status = status.displayName,
    isSynced = isSynced,
    createdAt = createdAt,
    updatedAt = updatedAt,
    departmentId = departmentId
)