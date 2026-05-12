package com.example.campusassist.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.campusassist.data.local.dao.ChatMessageDao
import com.example.campusassist.data.local.dao.DepartmentDao
import com.example.campusassist.data.local.dao.NotificationDao
import com.example.campusassist.data.local.dao.ServiceTicketDao
import com.example.campusassist.data.local.dao.UserDao
import com.example.campusassist.data.local.entity.*

@Database(
    entities = [
        ServiceTicketEntity::class,
        ServiceCategoryEntity::class,
        TicketStatusEntity::class,
        UserEntity::class,
        NotificationEntity::class,
        DepartmentEntity::class,
        ChatMessageEntity::class
    ],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun serviceTicketDao(): ServiceTicketDao
    abstract fun userDao(): UserDao
    abstract fun notificationDao(): NotificationDao
    abstract fun departmentDao(): DepartmentDao
    abstract fun chatMessageDao(): ChatMessageDao

    companion object {
        const val DATABASE_NAME = "campus_assist_db"

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS departments (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        code TEXT NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    ALTER TABLE service_tickets
                    ADD COLUMN departmentId INTEGER DEFAULT NULL REFERENCES departments(id) ON DELETE SET NULL
                """.trimIndent())
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE users_new (
                        username TEXT PRIMARY KEY NOT NULL,
                        fullname TEXT NOT NULL,
                        department TEXT,
                        role TEXT NOT NULL,
                        passwordHash TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        isActive INTEGER NOT NULL DEFAULT 1
                    )
                """.trimIndent())
                db.execSQL("DROP TABLE IF EXISTS users")
                db.execSQL("ALTER TABLE users_new RENAME TO users")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    ALTER TABLE service_tickets
                    ADD COLUMN notes TEXT DEFAULT NULL
                """.trimIndent())
                db.execSQL("""
                    ALTER TABLE service_tickets
                    ADD COLUMN attachmentUris TEXT DEFAULT NULL
                """.trimIndent())
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    ALTER TABLE users
                    ADD COLUMN profileImageUri TEXT DEFAULT NULL
                """.trimIndent())
            }
        }

        // Migration 6→7: Adds chat_messages table for per-ticket messaging.
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS chat_messages (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        ticketId INTEGER NOT NULL,
                        senderUsername TEXT NOT NULL,
                        senderDisplayName TEXT NOT NULL,
                        message TEXT NOT NULL,
                        sentAt INTEGER NOT NULL,
                        FOREIGN KEY(ticketId) REFERENCES service_tickets(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_chat_messages_ticketId ON chat_messages(ticketId)")
            }
        }

        // Migration 7→8: adds firestoreId and createdBy to service_tickets.
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE service_tickets ADD COLUMN firestoreId TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE service_tickets ADD COLUMN createdBy TEXT DEFAULT NULL")
            }
        }
    }
}
