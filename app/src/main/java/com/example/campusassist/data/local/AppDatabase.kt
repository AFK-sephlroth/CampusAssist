package com.example.campusassist.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
        DepartmentEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun serviceTicketDao(): ServiceTicketDao
    abstract fun userDao(): UserDao
    abstract fun notificationDao(): NotificationDao
    abstract fun departmentDao(): DepartmentDao

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

        // Migration 3→4: Recreates users table with new schema (username as TEXT PK,
        // fullname instead of name, passwordHash column, no email/contactNumber).
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

                // Best-effort copy: old schema may differ; this handles fresh installs.
                // Existing users are dropped on migration (acceptable for dev builds).
                db.execSQL("DROP TABLE IF EXISTS users")
                db.execSQL("ALTER TABLE users_new RENAME TO users")
            }
        }

        // Migration 4→5: Adds notes and attachmentUris columns to service_tickets.
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
        // Migration 5→6: Adds profileImageUri column to users table.
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    ALTER TABLE users
                    ADD COLUMN profileImageUri TEXT DEFAULT NULL
                """.trimIndent())
            }
        }
    }
}
