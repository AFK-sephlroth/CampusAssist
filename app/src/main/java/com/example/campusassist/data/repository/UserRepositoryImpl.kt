package com.example.campusassist.data.repository

import com.example.campusassist.data.local.dao.UserDao
import com.example.campusassist.data.mapper.toDomain
import com.example.campusassist.data.mapper.toEntity
import com.example.campusassist.domain.model.User
import com.example.campusassist.domain.model.UserRole
import com.example.campusassist.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val dao: UserDao
) : UserRepository {

    override suspend fun login(id: String, password: String): User? =
        dao.login(id, password.sha256())?.toDomain()

    override suspend fun register(user: User, password: String) =
        dao.insertUser(user.toEntity(password.sha256()))

    override suspend fun getUserById(id: String): User? =
        dao.getUserById(id)?.toDomain()

    override suspend fun updateUser(user: User, password: String) =
        dao.updateUser(user.toEntity(password.sha256()))

    override fun getAllUsers(): Flow<List<User>> =
        dao.getAllUsers().map { list -> list.map { it.toDomain() } }

    override fun getUsersByRole(role: UserRole): Flow<List<User>> =
        dao.getUsersByRole(role.name).map { list -> list.map { it.toDomain() } }

    private fun String.sha256(): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(toByteArray()).joinToString("") { "%02x".format(it) }
    }
}