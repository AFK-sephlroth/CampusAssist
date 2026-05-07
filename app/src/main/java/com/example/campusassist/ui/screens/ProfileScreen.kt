package com.example.campusassist.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.campusassist.domain.model.User
import com.example.campusassist.domain.model.UserRole
import com.example.campusassist.ui.theme.CampusColors
import com.example.campusassist.ui.viewmodel.AuthViewModel
import com.example.campusassist.ui.viewmodel.ThemeViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: User,
    authViewModel: AuthViewModel,
    themeViewModel: ThemeViewModel,
    onNavigateBack: () -> Unit
) {
    // FIX: Collect the current theme string so the toggle reflects the
    // persisted preference immediately, even after process recreation.
    val theme by themeViewModel.theme.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Image picker launcher — saves the URI straight to the DB and refreshes
    // authState so the dashboard avatar updates immediately.
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { authViewModel.updateProfileImage(it.toString()) }
    }

    val roleColor = when (user.role) {
        UserRole.STAFF -> CampusColors.CatLibrary
        else           -> CampusColors.CatIT
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Profile",
                        fontWeight = FontWeight.Bold,
                        color = CampusColors.TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Box(
                            Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CampusColors.TextMuted.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ArrowBack, null, tint = CampusColors.TextPrimary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D1F3C))
            )
        }
    ) { pv ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pv)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // ── Avatar card ───────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                roleColor.copy(alpha = 0.15f),
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
                    .border(1.dp, roleColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment    = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (user.profileImageUri != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(Uri.parse(user.profileImageUri))
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Profile photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(roleColor, roleColor.copy(alpha = 0.6f))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    user.fullname.take(1).uppercase(Locale.getDefault()),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize   = 22.sp,
                                    color      = Color.White
                                )
                            }
                        }
                        // Camera overlay hint
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.25f), CircleShape),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Change photo",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(bottom = 2.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            user.fullname,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize   = 18.sp,
                            color      = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "@${user.username}",
                            fontSize = 13.sp,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(roleColor.copy(alpha = 0.15f))
                                .border(1.dp, roleColor.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                user.role.name,
                                color      = roleColor,
                                fontSize   = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // ── Account details ───────────────────────────────────────────────
            DetailCard(title = "ACCOUNT DETAILS") {
                InfoRow("Department", user.department?.ifBlank { "No Department" } ?: "Not Assigned")
                Divider(
                    color    = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    modifier = Modifier.padding(vertical = 6.dp)
                )
                InfoRow("Role", user.role.name)
            }

            // ── Appearance card ───────────────────────────────────────────────
            // FIX: Each option is now a proper ThemeOption data class, which
            // makes it trivial to add more modes later.  The selected button
            // uses MaterialTheme colours (primary / onPrimary) so it
            // automatically respects whichever theme is currently active —
            // you'll see the button update in real-time as you tap each option.
            DetailCard(title = "APPEARANCE") {
                data class ThemeOption(val key: String, val label: String, val icon: ImageVector)

                val options = listOf(
                    ThemeOption("LIGHT",  "Light",  Icons.Default.LightMode),
                    ThemeOption("DARK",   "Dark",   Icons.Default.DarkMode),
                    ThemeOption("SYSTEM", "System", Icons.Default.SettingsBrightness)
                )

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    options.forEach { opt ->
                        val isSelected = theme == opt.key
                        OutlinedButton(
                            onClick  = { themeViewModel.setTheme(opt.key) },
                            modifier = Modifier.weight(1f),
                            shape    = RoundedCornerShape(10.dp),
                            colors   = ButtonDefaults.outlinedButtonColors(
                                // FIX: Selected state uses MaterialTheme.colorScheme.primary
                                // (Amber in dark / Navy in light) so the toggle is visually
                                // consistent in both themes.  Unselected is transparent so
                                // the card background shows through.
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary
                                else Color.Transparent,
                                contentColor   = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(opt.icon, contentDescription = opt.label, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.height(2.dp))
                                Text(opt.label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // ── Sign out ──────────────────────────────────────────────────────
            Button(
                onClick  = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape  = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CampusColors.PriorityHigh.copy(alpha = 0.15f),
                    contentColor   = CampusColors.PriorityHigh
                )
            ) {
                Icon(Icons.Default.Logout, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Sign Out", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    // ── Logout dialog ─────────────────────────────────────────────────────────
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor   = MaterialTheme.colorScheme.surface,
            title   = { Text("Sign Out", fontWeight = FontWeight.Bold) },
            text    = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                Button(
                    onClick = { authViewModel.logout(); showLogoutDialog = false },
                    colors  = ButtonDefaults.buttonColors(containerColor = CampusColors.PriorityHigh),
                    shape   = RoundedCornerShape(10.dp)
                ) { Text("Sign Out", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showLogoutDialog = false },
                    shape   = RoundedCornerShape(10.dp)
                ) { Text("Cancel") }
            }
        )
    }
}