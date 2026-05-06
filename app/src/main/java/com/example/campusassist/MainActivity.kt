package com.example.campusassist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.campusassist.data.local.SessionManager
import com.example.campusassist.ui.navigation.AppNavigation
import com.example.campusassist.ui.theme.CampusAssistTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // FIX: Inject SessionManager directly here so we can read the persisted
    // theme preference BEFORE the first Compose frame.  Reading it inside
    // AppNavigation (via ThemeViewModel) would work too, but doing it here
    // means CampusAssistTheme is already correct on the very first composition
    // — no flicker from a wrong-theme frame.
    @Inject lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Collect the persisted theme string ("LIGHT" | "DARK" | "SYSTEM")
            // as Compose state so any change automatically triggers recomposition.
            val themePref by sessionManager.theme.collectAsState()
            val systemDark = isSystemInDarkTheme()

            val useDark = when (themePref) {
                "DARK"  -> true
                "LIGHT" -> false
                else    -> systemDark   // "SYSTEM" or any unknown value
            }

            CampusAssistTheme(darkTheme = useDark) {
                AppNavigation()
            }
        }
    }
}