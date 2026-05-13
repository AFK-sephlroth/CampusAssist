package com.example.campusassist.data.local.dao

import androidx.room.*
import com.example.campusassist.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    // REPLACE (upsert) instead of ABORT so that:
    //  • re-login on the same device refreshes the cached Firestore profile without crashing
    //  • retrying a failed registration doesn't hit a UNIQUE constraint on username
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE username = :username AND passwordHash = :hash LIMIT 1")
    suspend fun login(username: String, hash: String): UserEntity?

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserById(username: String): UserEntity?

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET profileImageUri = :uri WHERE username = :username")
    suspend fun updateProfileImage(username: String, uri: String?)

    @Query("SELECT * FROM users WHERE role = :role")
    fun getUsersByRole(role: String): Flow<List<UserEntity>>
}