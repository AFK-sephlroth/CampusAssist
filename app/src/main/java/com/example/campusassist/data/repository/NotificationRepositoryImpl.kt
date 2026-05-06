package com.example.campusassist.data.repository

import com.example.campusassist.data.local.dao.NotificationDao
import com.example.campusassist.data.mapper.toDomain
import com.example.campusassist.data.mapper.toEntity
import com.example.campusassist.domain.model.AppNotification
import com.example.campusassist.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val dao: NotificationDao
) : NotificationRepository {

    override fun getNotifications(userId: String): Flow<List<AppNotification>> =
        dao.getNotificationsForUser(userId).map { list -> list.map { it.toDomain() } }

    override fun getUnreadCount(userId: String): Flow<Int> =
        dao.getUnreadCount(userId)

    override suspend fun addNotification(notification: AppNotification) =
        dao.insertNotification(notification.toEntity())

    override suspend fun markAsRead(id: Long) =
        dao.markAsRead(id)

    override suspend fun markAllAsRead(userId: String) =
        dao.markAllAsRead(userId)

    override suspend fun clearAll(userId: String) =
        dao.clearAll(userId)
}