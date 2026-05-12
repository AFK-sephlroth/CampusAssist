package com.example.campusassist.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Wraps Firebase Auth + Firestore for user credential management.
 *
 * Email convention: because Firebase Auth requires an email address but the
 * app uses a username, we map username → "<username>@campusassist.app" so
 * every username becomes a unique, stable email address. This lets us keep
 * the username-based UI intact.
 *
 * User profile data (fullname, role, department) is stored in the Firestore
 * "users" collection, keyed by Firebase UID, and mirrored into Room for
 * offline access.
 */
class FirebaseAuthSource(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val USERS_COLLECTION = "users"

        /** Converts a username to the internal Firebase email address. */
        fun usernameToEmail(username: String) = "${username.trim().lowercase()}@campusassist.app"
    }

    // ── Current state ─────────────────────────────────────────────────────────

    val currentFirebaseUser: FirebaseUser? get() = auth.currentUser

    val isSignedIn: Boolean get() = auth.currentUser != null

    // ── Sign in ───────────────────────────────────────────────────────────────

    /**
     * Signs the user in with Firebase. Returns a [FirebaseAuthResult] describing
     * success or the specific failure reason.
     */
    suspend fun signIn(username: String, password: String): FirebaseAuthResult {
        return try {
            val email = usernameToEmail(username)
            auth.signInWithEmailAndPassword(email, password).await()
            FirebaseAuthResult.Success
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            FirebaseAuthResult.InvalidCredentials
        } catch (e: Exception) {
            FirebaseAuthResult.Error(e.message ?: "Sign-in failed")
        }
    }

    // ── Register ──────────────────────────────────────────────────────────────

    /**
     * Creates a new Firebase Auth account and writes the profile document to
     * Firestore. Returns [FirebaseAuthResult] describing success/failure.
     */
    suspend fun register(
        username: String,
        password: String,
        fullname: String,
        role: String,
        department: String?
    ): FirebaseAuthResult {
        return try {
            val email = usernameToEmail(username)

            // 1. Create the Auth account
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return FirebaseAuthResult.Error("No UID returned")

            // 2. Write the profile to Firestore
            val profile = mapOf(
                "uid"        to uid,
                "username"   to username.trim(),
                "fullname"   to fullname.trim(),
                "role"       to role,
                "department" to department,
                "createdAt"  to System.currentTimeMillis(),
                "isActive"   to true
            )
            firestore.collection(USERS_COLLECTION).document(uid).set(profile).await()

            FirebaseAuthResult.Success
        } catch (e: FirebaseAuthUserCollisionException) {
            FirebaseAuthResult.UsernameTaken
        } catch (e: FirebaseAuthWeakPasswordException) {
            FirebaseAuthResult.WeakPassword
        } catch (e: Exception) {
            FirebaseAuthResult.Error(e.message ?: "Registration failed")
        }
    }

    // ── Fetch profile ─────────────────────────────────────────────────────────

    /**
     * Fetches the Firestore profile for the currently signed-in Firebase user.
     * Returns null if not signed in or the document doesn't exist.
     */
    suspend fun fetchCurrentUserProfile(): Map<String, Any?>? {
        val uid = auth.currentUser?.uid ?: return null
        return try {
            val doc = firestore.collection(USERS_COLLECTION).document(uid).get().await()
            if (doc.exists()) doc.data else null
        } catch (e: Exception) {
            null
        }
    }

    // ── Update profile image ──────────────────────────────────────────────────

    suspend fun updateProfileImageUri(uri: String?) {
        val uid = auth.currentUser?.uid ?: return
        try {
            firestore.collection(USERS_COLLECTION).document(uid)
                .update("profileImageUri", uri).await()
        } catch (_: Exception) { /* best-effort */ }
    }

    // ── Sign out ──────────────────────────────────────────────────────────────

    fun signOut() = auth.signOut()
}

// ── Result type ───────────────────────────────────────────────────────────────

sealed class FirebaseAuthResult {
    data object Success            : FirebaseAuthResult()
    data object InvalidCredentials : FirebaseAuthResult()
    data object UsernameTaken      : FirebaseAuthResult()
    data object WeakPassword       : FirebaseAuthResult()
    data class  Error(val message: String) : FirebaseAuthResult()
}
