package com.example.campusassist.domain.repository

import com.example.campusassist.domain.model.AppNotification
import com.example.campusassist.domain.model.User
import com.example.campusassist.domain.model.UserRole
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun login(id: String, password: String): User?
    suspend fun register(user: User, password: String)
    suspend fun getUserById(id: String): User?
    suspend fun updateUser(user: User, password: String)
    suspend fun updateProfileImage(username: String, uri: String?)
    fun getAllUsers(): Flow<List<User>>
    fun getUsersByRole(role: UserRole): Flow<List<User>>
}

interface NotificationRepository {
    fun getNotifications(userId: String): Flow<List<AppNotification>>
    fun getUnreadCount(userId: String): Flow<Int>
    suspend fun addNotification(notification: AppNotification)
    suspend fun markAsRead(id: Long)
    suspend fun markAllAsRead(userId: String)
    suspend fun clearAll(userId: String)
}