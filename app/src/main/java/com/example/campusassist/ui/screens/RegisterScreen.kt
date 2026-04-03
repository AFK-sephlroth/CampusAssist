package com.example.campusassist.ui.screens

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
import androidx.compose.ui.graphics.Color
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
                title = { Text("Create Account", fontWeight = FontWeight.Bold, color = CampusColors.TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Box(
                            modifier = Modifier.size(36.dp).clip(CircleShape)
                                .background(CampusColors.TextMuted.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ArrowBack, null, tint = CampusColors.TextPrimary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { pv ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pv)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            RegisterField("Full Name *", state.name, viewModel::onNameChange)
            RegisterField("Student / Staff ID *", state.studentId, viewModel::onStudentIdChange)
            RegisterField("Email", state.email, viewModel::onEmailChange, KeyboardType.Email)
            RegisterField("Department", state.department, viewModel::onDepartmentChange)
            RegisterField("Contact Number", state.contactNumber, viewModel::onContactChange, KeyboardType.Phone)

            // Role selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("ROLE", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                    color = CampusColors.TextSecondary, letterSpacing = 1.2.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    UserRole.entries.forEach { role ->
                        val isSelected = state.role == role
                        val color = when (role) {
                            UserRole.STUDENT -> CampusColors.CatIT
                            UserRole.STAFF   -> CampusColors.CatLibrary
                            UserRole.ADMIN   -> CampusColors.Amber
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) color else color.copy(alpha = 0.08f))
                                .border(1.5.dp, if (isSelected) color else color.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                                .clickable { viewModel.onRoleChange(role) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                role.displayName,
                                color = if (isSelected) CampusColors.NavyDeep else color,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            RegisterField("Password *", state.password, viewModel::onPasswordChange,
                KeyboardType.Password, isPassword = true)
            RegisterField("Confirm Password *", state.confirmPassword, viewModel::onConfirmPasswordChange,
                KeyboardType.Password, isPassword = true)

            state.errorMessage?.let {
                Text(
                    it, color = CampusColors.PriorityHigh, fontSize = 13.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CampusColors.PriorityHigh.copy(alpha = 0.1f))
                        .padding(10.dp)
                )
            }

            Button(
                onClick = viewModel::register,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !state.isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CampusColors.Amber,
                    contentColor = CampusColors.NavyDeep
                )
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = CampusColors.NavyDeep)
                } else {
                    Text("Create Account", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = loginFieldColors(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None
    )
}