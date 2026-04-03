package com.example.campusassist.domain.repository

import com.example.campusassist.domain.model.Department
import kotlinx.coroutines.flow.Flow

interface DepartmentRepository {
    fun getAllDepartments(): Flow<List<Department>>
    suspend fun getDepartmentById(id: Long): Department?
    suspend fun addDepartment(department: Department)
    suspend fun updateDepartment(department: Department)
    suspend fun deleteDepartment(department: Department)
}