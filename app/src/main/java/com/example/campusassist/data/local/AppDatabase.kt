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
    version = 3,
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

                //CCIS, COM, CEA, CAT, CON, CCJS, and  COED
                val now = System.currentTimeMillis()
                listOf(
                    Pair("MIS Department", "MIS"),
                    Pair("CCIS Department", "CCIS"),
                    Pair("COM Department", "COM"),
                    Pair("CEA Department", "CEA"),
                    Pair("CAT Department", "CAT"),
                    Pair("CON Department", "CON"),
                    Pair("CCJS Department", "CCJS"),
                    Pair("COED Department", "COED"),
                    Pair("Registrar", "REGISTRAR"),
                    Pair("Library", "LIBRARY"),
                    Pair("Cashier", "CASHIER"),
                    Pair("Maintenance", "MAINTENANCE")
                ).forEach { (name, code) ->
                    db.execSQL("""
                        INSERT INTO departments (name, code, createdAt)
                        VALUES ('$name', '$code', $now)
                    """.trimIndent())
                }
            }
        }

        // SEED_CALLBACK is inside companion object so AppModule can access it as AppDatabase.SEED_CALLBACK
        val SEED_CALLBACK = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                val now = System.currentTimeMillis()
                listOf(
                    Pair("MIS Department", "MIS"),
                    Pair("CCIS Department", "CCIS"),
                    Pair("COM Department", "COM"),
                    Pair("CEA Department", "CEA"),
                    Pair("CAT Department", "CAT"),
                    Pair("CON Department", "CON"),
                    Pair("CCJS Department", "CCJS"),
                    Pair("COED Department", "COED"),
                    Pair("Registrar", "REGISTRAR"),
                    Pair("Library", "LIBRARY"),
                    Pair("Cashier", "CASHIER"),
                    Pair("Maintenance", "MAINTENANCE")
                ).forEach { (name, code) ->
                    db.execSQL("""
                        INSERT INTO departments (name, code, createdAt)
                        VALUES ('$name', '$code', $now)
                    """.trimIndent())
                }
            }
        }
    }   // <-- companion object closes here
}       // <-- class closes here