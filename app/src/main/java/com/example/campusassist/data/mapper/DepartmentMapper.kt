package com.example.campusassist.data.mapper

import com.example.campusassist.data.local.entity.DepartmentEntity
import com.example.campusassist.domain.model.Department

fun DepartmentEntity.toDomain(): Department = Department(
    id = id,
    name = name,
    code = code,
    createdAt = createdAt
)

fun Department.toEntity(): DepartmentEntity = DepartmentEntity(
    id = id,
    name = name,
    code = code,
    createdAt = createdAt
)