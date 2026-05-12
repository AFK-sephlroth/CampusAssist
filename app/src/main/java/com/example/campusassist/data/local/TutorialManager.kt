package com.example.campusassist.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tracks whether the onboarding tutorial has been shown for a given username.
 * Stored in SharedPreferences so it persists across app restarts.
 * Each username gets its own flag, so a new account always sees the tutorial.
 */
@Singleton
class TutorialManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("campus_tutorial", Context.MODE_PRIVATE)

    fun hasSeenTutorial(username: String): Boolean =
        prefs.getBoolean("seen_$username", false)

    fun markTutorialSeen(username: String) {
        prefs.edit().putBoolean("seen_$username", true).apply()
    }
}
