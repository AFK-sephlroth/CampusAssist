package com.example.campusassist.data.repository

import com.example.campusassist.data.local.dao.UserDao
import com.example.campusassist.data.mapper.toDomain
import com.example.campusassist.data.mapper.toEntity
import com.example.campusassist.data.remote.FirebaseAuthResult
import com.example.campusassist.data.remote.FirebaseAuthSource
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
    private val dao: UserDao,
    private val firebaseAuth: FirebaseAuthSource
) : UserRepository {

    // ── Login ─────────────────────────────────────────────────────────────────
    /**
     * Tries Firebase Auth first (online). On success, writes the profile into
     * Room so subsequent offline launches find the user. Falls back to the
     * local Room record if Firebase is unreachable.
     */
    override suspend fun login(id: String, password: String): User? {
        val firebaseResult = try {
            firebaseAuth.signIn(id, password)
        } catch (e: Exception) {
            FirebaseAuthResult.Error(e.message ?: "Network error")
        }

        return when (firebaseResult) {
            is FirebaseAuthResult.Success -> {
                // Fetch up-to-date profile from Firestore and cache it locally
                val profile = firebaseAuth.fetchCurrentUserProfile()
                if (profile != null) {
                    val user = profileToUser(profile)
                    dao.insertUser(user.toEntity("")) // empty hash — auth is Firebase-managed
                    user
                } else {
                    // Firestore fetch failed — try Room cache
                    dao.getUserById(id)?.toDomain()
                }
            }
            is FirebaseAuthResult.InvalidCredentials -> null
            else -> {
                // Firebase unreachable — fall back to local Room login
                dao.login(id, password.sha256())?.toDomain()
            }
        }
    }

    // ── Register ──────────────────────────────────────────────────────────────
    /**
     * Registers in Firebase Auth + Firestore, then mirrors the user into Room.
     * Throws on any failure so AuthViewModel can show a meaningful error.
     */
    override suspend fun register(user: User, password: String) {
        val result = firebaseAuth.register(
            username   = user.username,
            password   = password,
            fullname   = user.fullname,
            role       = user.role.name,
            department = user.department
        )
        when (result) {
            is FirebaseAuthResult.Success      -> {
                // Mirror into Room for offline access
                dao.insertUser(user.toEntity(password.sha256()))
            }
            is FirebaseAuthResult.UsernameTaken -> throw Exception("Username already taken")
            is FirebaseAuthResult.WeakPassword  -> throw Exception("Password must be at least 6 characters")
            is FirebaseAuthResult.Error         -> throw Exception(result.message)
            else                                -> throw Exception("Registration failed")
        }
    }

    // ── Profile operations ────────────────────────────────────────────────────

    override suspend fun getUserById(id: String): User? = dao.getUserById(id)?.toDomain()

    override suspend fun updateUser(user: User, password: String) =
        dao.updateUser(user.toEntity(password.sha256()))

    override suspend fun updateProfileImage(username: String, uri: String?) {
        dao.updateProfileImage(username, uri)
        firebaseAuth.updateProfileImageUri(uri)  // best-effort sync to Firestore
    }

    override fun getAllUsers(): Flow<List<User>> =
        dao.getAllUsers().map { it.map { e -> e.toDomain() } }

    override fun getUsersByRole(role: UserRole): Flow<List<User>> =
        dao.getUsersByRole(role.name).map { it.map { e -> e.toDomain() } }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun profileToUser(profile: Map<String, Any?>): User {
        val roleStr = profile["role"] as? String ?: "USER"
        return User(
            username   = profile["username"] as? String ?: "",
            fullname   = profile["fullname"] as? String ?: "",
            role       = runCatching { UserRole.valueOf(roleStr) }.getOrDefault(UserRole.USER),
            department = profile["department"] as? String,
            createdAt  = (profile["createdAt"] as? Long) ?: System.currentTimeMillis(),
            isActive   = profile["isActive"] as? Boolean ?: true
        )
    }

    private fun String.sha256(): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(toByteArray()).joinToString("") { "%02x".format(it) }
    }
}
