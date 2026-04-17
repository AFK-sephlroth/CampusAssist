package com.example.campusassist.data.mapper

import com.example.campusassist.data.local.entity.NotificationEntity
import com.example.campusassist.data.local.entity.UserEntity
import com.example.campusassist.domain.model.AppNotification
import com.example.campusassist.domain.model.NotificationType
import com.example.campusassist.domain.model.User
import com.example.campusassist.domain.model.UserRole


fun UserEntity.toDomain(): User {
    return User(
        username = this.username,
        fullname = this.fullname,
        role = UserRole.valueOf(this.role),
        department = this.department
    )
}

fun User.toEntity(passwordHash: String) = UserEntity(
    username     = username,      // Changed from id
    fullname     = fullname,      // Changed from name
    department   = department,
    role         = role.name,
    passwordHash = passwordHash,
    createdAt    = System.currentTimeMillis() // Or use a property from User if you have one
)

fun NotificationEntity.toDomain() = AppNotification(
    id        = id,
    userId    = userId,
    ticketId  = ticketId,
    title     = title,
    message   = message,
    type      = NotificationType.valueOf(type),
    isRead    = isRead,
    createdAt = createdAt
)

fun AppNotification.toEntity() = NotificationEntity(
    id        = id,
    userId    = userId,
    ticketId  = ticketId,
    title     = title,
    message   = message,
    type      = type.name,
    isRead    = isRead,
    createdAt = createdAt
)