package com.example.campusassist.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * `name` carries a UNIQUE index so that [insertOrReplaceDepartment] (REPLACE strategy)
 * deduplicates rows by name, not just by the auto-generated [id].
 * Without this, every syncFromFirestore() call would insert a new row for the same
 * department because `id = 0` always looks like a new record to SQLite.
 */
@Entity(
    tableName = "departments",
    indices = [Index(value = ["name"], unique = true)]
)
data class DepartmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val code: String,
    val createdAt: Long = System.currentTimeMillis()
)