package com.example.campusassist.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusassist.domain.model.User
import com.example.campusassist.domain.model.UserRole
import com.example.campusassist.ui.theme.CampusColors
import com.example.campusassist.ui.viewmodel.AuthViewModel
import com.example.campusassist.ui.viewmodel.ThemeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: User,
    authViewModel: AuthViewModel,
    themeViewModel: ThemeViewModel,
    onNavigateBack: () -> Unit
) {
    val theme by themeViewModel.theme.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    val roleColor = when (user.role) {
        UserRole.STUDENT -> CampusColors.CatIT
        UserRole.STAFF   -> CampusColors.CatLibrary
        UserRole.ADMIN   -> CampusColors.Amber
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold, color = CampusColors.TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Box(
                            Modifier.size(36.dp).clip(CircleShape)
                                .background(CampusColors.TextMuted.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Default.ArrowBack, null, tint = CampusColors.TextPrimary) }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D1F3C))
            )
        }
    ) { pv ->
        Column(
            modifier = Modifier.fillMaxSize().padding(pv)
                .verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Avatar card
            Box(
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(roleColor.copy(alpha = 0.15f), MaterialTheme.colorScheme.surface)
                        )
                    )
                    .border(1.dp, roleColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(
                        modifier = Modifier.size(64.dp).clip(CircleShape)
                            .background(brush = Brush.linearGradient(colors = listOf(roleColor, roleColor.copy(alpha = 0.6f)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(user.name.take(2).uppercase(), fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = CampusColors.NavyDeep)
                    }
                    Column {
                        Text(user.name, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text(user.id, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(20.dp))
                                .background(roleColor.copy(alpha = 0.15f))
                                .border(1.dp, roleColor.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(user.role.displayName, color = roleColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Contact info
            DetailCard(title = "CONTACT INFO") {
                InfoRow("Email", user.email.ifBlank { "—" })
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 6.dp))
                InfoRow("Department", user.department.ifBlank { "—" })
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 6.dp))
                InfoRow("Contact", user.contactNumber.ifBlank { "—" })
            }

            // Theme toggle
            DetailCard(title = "APPEARANCE") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("LIGHT", "DARK", "SYSTEM").forEach { t ->
                        val isSelected = theme == t
                        val icon = if (t == "DARK") Icons.Default.DarkMode else Icons.Default.LightMode
                        OutlinedButton(
                            onClick = { themeViewModel.setTheme(t) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) CampusColors.Amber else Color.Transparent,
                                contentColor = if (isSelected) CampusColors.NavyDeep else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(icon, null, modifier = Modifier.size(16.dp))
                                Text(t.lowercase().replaceFirstChar { it.uppercase() }, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // Sign out button
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CampusColors.PriorityHigh.copy(alpha = 0.15f),
                    contentColor = CampusColors.PriorityHigh
                )
            ) {
                Icon(Icons.Default.Logout, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Sign Out", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Sign Out", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                Button(
                    onClick = { authViewModel.logout(); showLogoutDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CampusColors.PriorityHigh),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Sign Out", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showLogoutDialog = false }, shape = RoundedCornerShape(10.dp)) {
                    Text("Cancel")
                }
            }
        )
    }
}