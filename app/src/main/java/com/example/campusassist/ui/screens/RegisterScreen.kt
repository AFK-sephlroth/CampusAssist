package com.example.campusassist.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusassist.domain.model.UserRole
import com.example.campusassist.ui.theme.CampusColors
import com.example.campusassist.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.registerState.collectAsState()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) onNavigateBack()
    }

    Scaffold(
        containerColor = CampusColors.NavyDeep,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Create Account",
                        fontWeight = FontWeight.Bold,
                        color = CampusColors.TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CampusColors.TextMuted.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ArrowBack, null, tint = CampusColors.TextPrimary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
            )
        }
    ) { pv ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pv)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // ── Role toggle ───────────────────────────────────────────────────
            RoleToggle(
                selected = state.role,
                onSelect = viewModel::onRoleChange
            )

            // ── Shared fields ─────────────────────────────────────────────────
            RegisterField(
                label = "Username *",
                value = state.username,
                onValueChange = viewModel::onUsernameChange
            )
            RegisterField(
                label = "Full Name *",
                value = state.fullName,
                onValueChange = viewModel::onFullNameChange
            )

            // ── Staff-only: department free-text ──────────────────────────────
            AnimatedVisibility(
                visible = state.role == UserRole.STAFF,
                enter = fadeIn(tween(200)) + expandVertically(tween(200)),
                exit  = fadeOut(tween(150)) + shrinkVertically(tween(150))
            ) {
                RegisterField(
                    label         = "Department *",
                    value         = state.departmentText,
                    onValueChange = viewModel::onDepartmentTextChange
                )
            }

            // ── Password fields ───────────────────────────────────────────────
            RegisterField(
                label         = "Password *",
                value         = state.password,
                onValueChange = viewModel::onPasswordChange,
                keyboardType  = KeyboardType.Password,
                isPassword    = true
            )
            RegisterField(
                label         = "Confirm Password *",
                value         = state.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChange,
                keyboardType  = KeyboardType.Password,
                isPassword    = true
            )

            // ── Error banner ──────────────────────────────────────────────────
            AnimatedVisibility(visible = state.errorMessage != null) {
                state.errorMessage?.let { msg ->
                    Text(
                        text     = msg,
                        color    = CampusColors.PriorityHigh,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CampusColors.PriorityHigh.copy(alpha = 0.1f))
                            .border(1.dp, CampusColors.PriorityHigh.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    )
                }
            }

            // ── Submit button ─────────────────────────────────────────────────
            Button(
                onClick  = viewModel::register,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled  = !state.isLoading,
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = CampusColors.Amber,
                    contentColor   = CampusColors.NavyDeep
                )
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color       = CampusColors.NavyDeep
                    )
                } else {
                    Text("Create Account", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Role Toggle ───────────────────────────────────────────────────────────────

@Composable
private fun RoleToggle(
    selected: UserRole,
    onSelect: (UserRole) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "ACCOUNT TYPE",
            fontSize      = 11.sp,
            fontWeight    = FontWeight.Bold,
            color         = CampusColors.TextSecondary,
            letterSpacing = 1.2.sp
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CampusColors.NavyCard)
                .border(1.dp, CampusColors.TextMuted.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            UserRole.entries.forEach { role ->
                val isSelected = selected == role
                val roleColor  = if (role == UserRole.STAFF) CampusColors.CatLibrary else CampusColors.CatIT
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (isSelected) roleColor else androidx.compose.ui.graphics.Color.Transparent)
                        .clickable { onSelect(role) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text       = role.displayName,
                        color      = if (isSelected) CampusColors.NavyDeep else CampusColors.TextSecondary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize   = 14.sp
                    )
                }
            }
        }
    }
}

// ── Reusable text field ───────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterField(
    label:         String,
    value:         String,
    onValueChange: (String) -> Unit,
    keyboardType:  KeyboardType = KeyboardType.Text,
    isPassword:    Boolean = false
) {
    OutlinedTextField(
        value             = value,
        onValueChange     = onValueChange,
        label             = { Text(label) },
        modifier          = Modifier.fillMaxWidth(),
        shape             = RoundedCornerShape(12.dp),
        colors            = loginFieldColors(),
        singleLine        = true,
        keyboardOptions   = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None
    )
}
