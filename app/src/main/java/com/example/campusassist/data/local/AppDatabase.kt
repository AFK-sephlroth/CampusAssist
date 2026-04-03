package com.example.campusassist.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.campusassist.data.local.dao.*
import com.example.campusassist.data.local.entity.*

@Database(
    entities = [
        ServiceTicketEntity::class,
        ServiceCategoryEntity::class,
        TicketStatusEntity::class,
        UserEntity::class,
        NotificationEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun serviceTicketDao(): ServiceTicketDao
    abstract fun userDao(): UserDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        const val DATABASE_NAME = "campus_assist_db"
    }
}