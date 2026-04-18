package com.example.campusassist.data.repository

import com.example.campusassist.data.local.dao.DepartmentDao
import com.example.campusassist.data.local.entity.DepartmentEntity
import com.example.campusassist.data.mapper.toDomain
import com.example.campusassist.data.mapper.toEntity
import com.example.campusassist.domain.model.Department
import com.example.campusassist.domain.repository.DepartmentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DepartmentRepositoryImpl @Inject constructor(
    private val dao: DepartmentDao
) : DepartmentRepository {

    override fun getAllDepartments(): Flow<List<Department>> =
        dao.getAllDepartments().map { list -> list.map { it.toDomain() } }

    override suspend fun getDepartmentById(id: Long): Department? =
        dao.getDepartmentById(id)?.toDomain()

    override suspend fun addDepartment(department: Department) =
        dao.insertDepartment(department.toEntity()).let { Unit }

    override suspend fun updateDepartment(department: Department) =
        dao.updateDepartment(department.toEntity())

    override suspend fun deleteDepartment(department: Department) =
        dao.deleteDepartment(department.toEntity())

    override suspend fun getOrCreateByName(name: String): Department {
        val existing = dao.getByName(name)
        if (existing != null) return existing.toDomain()

        val trimmed = name.trim()
        val newEntity = DepartmentEntity(
            name      = trimmed,
            code      = trimmed.take(4).uppercase(),
            createdAt = System.currentTimeMillis()
        )
        val insertedId = dao.insertDepartment(newEntity)
        return newEntity.copy(id = insertedId).toDomain()
    }
}
