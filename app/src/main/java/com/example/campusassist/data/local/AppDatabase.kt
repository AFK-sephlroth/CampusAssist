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
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun serviceTicketDao(): ServiceTicketDao
    abstract fun userDao(): UserDao
    abstract fun notificationDao(): NotificationDao
    abstract fun departmentDao(): DepartmentDao

    companion object {
        const val DATABASE_NAME = "campus_assist_db"

        // Migration 2 to 3: Just creates the table and adds the column
        // without pre-filling it with departments.
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

                // Hard-coded department insertion removed to follow your new logic
            }
        }

        // Migration 3 to 4: Cleans up the Users table
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE users_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        username TEXT NOT NULL,
                        fullname TEXT NOT NULL,
                        password TEXT NOT NULL,
                        department TEXT, 
                        role TEXT NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                """.trimIndent())

                db.execSQL("""
                    INSERT INTO users_new (id, username, fullname, password, department, role, createdAt)
                    SELECT id, username, fullname, password, department, role, createdAt FROM users
                """.trimIndent())

                db.execSQL("DROP TABLE users")
                db.execSQL("ALTER TABLE users_new RENAME TO users")
            }
        }

        // Removed SEED_CALLBACK entirely so the app starts with 0 departments.
    }
}