package com.example.campusassist.data.repository

import com.example.campusassist.data.local.dao.DepartmentDao
import com.example.campusassist.data.local.entity.DepartmentEntity
import com.example.campusassist.data.mapper.toDomain
import com.example.campusassist.data.mapper.toEntity
import com.example.campusassist.domain.model.Department
import com.example.campusassist.domain.repository.DepartmentRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DepartmentRepositoryImpl @Inject constructor(
    private val dao: DepartmentDao,
    private val firestore: FirebaseFirestore
) : DepartmentRepository {

    companion object {
        private const val DEPARTMENTS_COLLECTION = "departments"
    }

    override fun getAllDepartments(): Flow<List<Department>> =
        dao.getAllDepartments().map { list -> list.map { it.toDomain() } }

    override suspend fun getDepartmentById(id: Long): Department? =
        dao.getDepartmentById(id)?.toDomain()

    override suspend fun addDepartment(department: Department) {
        dao.insertDepartment(department.toEntity()).let { Unit }
        pushDepartmentToFirestore(department)
    }

    override suspend fun updateDepartment(department: Department) {
        dao.updateDepartment(department.toEntity())
        pushDepartmentToFirestore(department)
    }

    override suspend fun deleteDepartment(department: Department) {
        dao.deleteDepartment(department.toEntity())
        try {
            firestore.collection(DEPARTMENTS_COLLECTION)
                .document(department.name.trim().lowercase())
                .delete()
                .await()
        } catch (_: Exception) { /* best-effort */ }
    }

    override suspend fun getOrCreateByName(name: String): Department {
        val trimmed = name.trim()

        // 1. Check local Room first (fast path)
        val existing = dao.getByName(trimmed)
        if (existing != null) return existing.toDomain()

        // 2. Check Firestore — another device may have already created it
        val firestoreDoc = try {
            firestore.collection(DEPARTMENTS_COLLECTION)
                .document(trimmed.lowercase())
                .get()
                .await()
        } catch (_: Exception) { null }

        if (firestoreDoc != null && firestoreDoc.exists()) {
            val code      = firestoreDoc.getString("code") ?: trimmed.take(4).uppercase()
            val createdAt = firestoreDoc.getLong("createdAt") ?: System.currentTimeMillis()
            val entity    = DepartmentEntity(name = trimmed, code = code, createdAt = createdAt)
            dao.insertDepartment(entity)
            return entity.toDomain()
        }

        // 3. Truly new — create locally and push to Firestore
        val newEntity = DepartmentEntity(
            name      = trimmed,
            code      = trimmed.take(4).uppercase(),
            createdAt = System.currentTimeMillis()
        )
        val insertedId = dao.insertDepartment(newEntity)
        val created    = newEntity.copy(id = insertedId).toDomain()
        pushDepartmentToFirestore(created)
        return created
    }

    /**
     * Pull all departments from Firestore into Room.
     * Call on login / app startup so every device stays in sync.
     */
    override suspend fun syncFromFirestore() {
        try {
            val snapshot = firestore.collection(DEPARTMENTS_COLLECTION).get().await()
            for (doc in snapshot.documents) {
                val name      = doc.getString("name") ?: continue
                val code      = doc.getString("code") ?: name.take(4).uppercase()
                val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                val entity    = DepartmentEntity(name = name, code = code, createdAt = createdAt)
                dao.insertDepartment(entity) // IGNORE on conflict — preserves local IDs
            }
        } catch (_: Exception) { /* offline — Room data stays as-is */ }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private suspend fun pushDepartmentToFirestore(department: Department) {
        try {
            val doc = mapOf(
                "name"      to department.name,
                "code"      to department.code,
                "createdAt" to System.currentTimeMillis()
            )
            // Lowercase name as document ID for idempotent upserts
            firestore.collection(DEPARTMENTS_COLLECTION)
                .document(department.name.trim().lowercase())
                .set(doc)
                .await()
        } catch (_: Exception) { /* best-effort; Room already has it */ }
    }
}