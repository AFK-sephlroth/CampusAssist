package com.example.campusassist.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusassist.ui.theme.CampusColors
import com.example.campusassist.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit
) {
    val authState by viewModel.authState.collectAsState()
    var studentId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF080E1D), Color(0xFF0D1F3C), Color(0xFF080E1D)),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
    ) {
        // Decorative glows
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-60).dp, y = (-60).dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(CampusColors.Amber.copy(alpha = 0.08f), Color.Transparent)
                    ), shape = CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(250.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 60.dp, y = 60.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(CampusColors.CatIT.copy(alpha = 0.06f), Color.Transparent)
                    ), shape = CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(CampusColors.Amber, Color(0xFFFF8F00))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("CA+", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = CampusColors.NavyDeep)
            }

            Spacer(Modifier.height(20.dp))

            Text(
                "CampusAssist+",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 28.sp,
                color = CampusColors.TextPrimary,
                letterSpacing = (-0.5).sp
            )
            Text(
                "Smart Campus Service System",
                fontSize = 13.sp,
                color = CampusColors.TextSecondary,
                letterSpacing = 0.3.sp
            )

            Spacer(Modifier.height(40.dp))

            // Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(CampusColors.NavyCard)
                    .border(1.dp, CampusColors.TextMuted.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = CampusColors.TextPrimary)

                OutlinedTextField(
                    value = studentId,
                    onValueChange = { studentId = it },
                    label = { Text("Student / Staff ID") },
                    leadingIcon = { Icon(Icons.Default.Person, null, tint = CampusColors.TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = loginFieldColors(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = CampusColors.TextSecondary) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                null, tint = CampusColors.TextSecondary
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = loginFieldColors(),
                    singleLine = true
                )

                authState.errorMessage?.let { error ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(CampusColors.PriorityHigh.copy(alpha = 0.1f))
                            .border(1.dp, CampusColors.PriorityHigh.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("⚠", fontSize = 13.sp)
                        Text(error, color = CampusColors.PriorityHigh, fontSize = 13.sp)
                    }
                }

                Button(
                    onClick = { viewModel.login(studentId, password) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !authState.isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CampusColors.Amber,
                        contentColor = CampusColors.NavyDeep
                    )
                ) {
                    if (authState.isLoading) {
                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = CampusColors.NavyDeep)
                    } else {
                        Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            TextButton(onClick = onNavigateToRegister) {
                Text("Don't have an account? Register", color = CampusColors.Amber, fontSize = 14.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun loginFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = CampusColors.Amber,
    unfocusedBorderColor = CampusColors.TextMuted,
    focusedLabelColor = CampusColors.Amber,
    unfocusedLabelColor = CampusColors.TextSecondary,
    cursorColor = CampusColors.Amber,
    focusedTextColor = CampusColors.TextPrimary,
    unfocusedTextColor = CampusColors.TextPrimary
)