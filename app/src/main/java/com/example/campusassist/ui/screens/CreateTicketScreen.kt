package com.example.campusassist.ui.screens

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusassist.domain.model.ServiceCategory
import com.example.campusassist.domain.model.TicketPriority
import com.example.campusassist.ui.theme.CampusColors
import com.example.campusassist.ui.viewmodel.TicketViewModel
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import com.example.campusassist.domain.model.Department
import com.example.campusassist.ui.viewmodel.DepartmentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTicketScreen(
    viewModel: TicketViewModel,
    departmentViewModel: DepartmentViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.createUiState.collectAsState()
    val deptState by departmentViewModel.uiState.collectAsState()


    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onNavigateBack()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF0D1F3C), Color(0xFF1A2E50)),
                            start = Offset(0f, 0f),
                            end = Offset(Float.POSITIVE_INFINITY, 0f)
                        )
                    )
                    .padding(horizontal = 4.dp)
            ) {
                TopAppBar(
                    title = {
                        Column {
                            Text("New Request", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = CampusColors.TextPrimary)
                            Text("Fill in the details below", fontSize = 11.sp, color = CampusColors.TextSecondary)
                        }
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
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = CampusColors.TextPrimary)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Title field
            FormSection(label = "TICKET TITLE") {
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = viewModel::onTitleChange,
                    placeholder = { Text("e.g., Projector not working in Room 301", color = CampusColors.TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    isError = uiState.errorMessage != null && uiState.title.isBlank(),
                    colors = outlinedTextFieldColors(),
                    singleLine = true
                )
            }

            // Description field
            FormSection(label = "DESCRIPTION") {
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = viewModel::onDescriptionChange,
                    placeholder = { Text("Describe the issue in detail...", color = CampusColors.TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 4,
                    maxLines = 6,
                    colors = outlinedTextFieldColors()
                )
            }

            // Category selector
            FormSection(label = "SERVICE CATEGORY") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ServiceCategory.entries.forEach { category ->
                        val catColor = when (category.name) {
                            "IT"         -> CampusColors.CatIT
                            "FACILITIES" -> CampusColors.CatFacilities
                            else         -> CampusColors.CatLibrary
                        }
                        val isSelected = uiState.category == category
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) catColor else catColor.copy(alpha = 0.08f))
                                .border(
                                    1.5.dp,
                                    if (isSelected) catColor else catColor.copy(alpha = 0.25f),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.onCategoryChange(category) }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = category.displayName,
                                color = if (isSelected) CampusColors.NavyDeep else catColor,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Priority selector
            FormSection(label = "PRIORITY LEVEL") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TicketPriority.entries.forEach { priority ->
                        val priColor = when (priority.name) {
                            "HIGH"   -> CampusColors.PriorityHigh
                            "MEDIUM" -> CampusColors.PriorityMed
                            else     -> CampusColors.PriorityLow
                        }
                        val isSelected = uiState.priority == priority
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) priColor else priColor.copy(alpha = 0.08f))
                                .border(
                                    1.5.dp,
                                    if (isSelected) priColor else priColor.copy(alpha = 0.25f),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.onPriorityChange(priority) }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            if (isSelected) CampusColors.NavyDeep else priColor,
                                            CircleShape
                                        )
                                )
                                Text(
                                    text = priority.displayName,
                                    color = if (isSelected) CampusColors.NavyDeep else priColor,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // Department selector
            FormSection(label = "DEPARTMENT") {
                DepartmentDropdown(
                    departments = deptState.departments,
                    selectedId = uiState.departmentId,
                    onSelect = viewModel::onDepartmentChange
                )
            }

            // Error message
            uiState.errorMessage?.let { error ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(CampusColors.PriorityHigh.copy(alpha = 0.1f))
                        .border(1.dp, CampusColors.PriorityHigh.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("⚠", fontSize = 14.sp)
                    Text(error, color = CampusColors.PriorityHigh, fontSize = 13.sp)
                }
            }

            Spacer(Modifier.height(4.dp))

            // Submit button
            Button(
                onClick = viewModel::submitTicket,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !uiState.isLoading,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CampusColors.Amber,
                    contentColor = CampusColors.NavyDeep
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.5.dp,
                        color = CampusColors.NavyDeep
                    )
                } else {
                    Text(
                        "Submit Request",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        letterSpacing = 0.3.sp
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun FormSection(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = CampusColors.TextSecondary,
            letterSpacing = 1.2.sp
        )
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun outlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = CampusColors.Amber,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedLabelColor = CampusColors.Amber,
    cursorColor = CampusColors.Amber,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
)

@Composable
fun DepartmentDropdown(
    departments: List<Department>,
    selectedId: Long?,
    onSelect: (Long?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = departments.firstOrNull { it.id == selectedId }

    Box(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(
                    1.5.dp,
                    if (selectedId != null) CampusColors.Amber else MaterialTheme.colorScheme.outline,
                    RoundedCornerShape(12.dp)
                )
                .clickable { expanded = true }
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selected?.name ?: "Select a department",
                    color = if (selected != null) MaterialTheme.colorScheme.onSurface
                    else CampusColors.TextMuted,
                    fontSize = 14.sp,
                    fontWeight = if (selected != null) FontWeight.Medium else FontWeight.Normal
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ArrowDropUp
                    else Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = CampusColors.Amber
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            // None option
            DropdownMenuItem(
                text = {
                    Text(
                        "None / Not sure",
                        color = CampusColors.TextSecondary,
                        fontSize = 13.sp
                    )
                },
                onClick = {
                    onSelect(null)
                    expanded = false
                }
            )
            HorizontalDivider()
            departments.forEach { dept ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(
                                text = dept.name,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = dept.code,
                                color = CampusColors.TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    },
                    onClick = {
                        onSelect(dept.id)
                        expanded = false
                    },
                    modifier = if (dept.id == selectedId)
                        Modifier.background(CampusColors.Amber.copy(alpha = 0.08f))
                    else Modifier
                )
            }
        }
    }
}