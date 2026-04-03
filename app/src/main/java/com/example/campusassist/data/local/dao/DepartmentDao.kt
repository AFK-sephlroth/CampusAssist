package com.example.campusassist.data.local.dao

import androidx.room.*
import androidx.room.Dao
import com.example.campusassist.data.local.entity.DepartmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DepartmentDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDepartment(department: DepartmentEntity): Long

    @Update
    suspend fun updateDepartment(department: DepartmentEntity)

    @Delete
    suspend fun deleteDepartment(department: DepartmentEntity)

    @Query("SELECT * FROM departments ORDER BY name ASC")
    fun getAllDepartments(): Flow<List<DepartmentEntity>>

    @Query("SELECT * FROM departments WHERE id = :id LIMIT 1")
    suspend fun getDepartmentById(id: Long): DepartmentEntity?


}