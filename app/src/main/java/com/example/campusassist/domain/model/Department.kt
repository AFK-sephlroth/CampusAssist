package com.example.campusassist.domain.model

data class Department(
    val id: Long = 0,
    val name: String,
    val code: String,
    val createdAt: Long = System.currentTimeMillis()
)
