package com.example.campusassist.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "service_categories")
data class ServiceCategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,       // IT, Facilities, Library
    val description: String,
    val iconRes: String     // icon identifier
)