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
import androidx.compose.material.icons.filled.Delete
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
import com.example.campusassist.domain.model.ServiceTicket
import com.example.campusassist.domain.model.TicketStatus
import com.example.campusassist.ui.theme.CampusColors
import com.example.campusassist.ui.viewmodel.TicketViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketDetailScreen(
    ticketId: Long,
    viewModel: TicketViewModel,
    onNavigateBack: () -> Unit
) {
    val ticket by viewModel.selectedTicket.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(ticketId) { viewModel.loadTicketById(ticketId) }

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
            ) {
                TopAppBar(
                    title = {
                        Column {
                            Text("Ticket Details", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = CampusColors.TextPrimary)
                            ticket?.let {
                                Text("#${it.id}", fontSize = 11.sp, color = CampusColors.TextSecondary)
                            }
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
                    actions = {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(CampusColors.PriorityHigh.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CampusColors.PriorityHigh)
                            }
                        }
                        Spacer(Modifier.width(4.dp))
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        }
    ) { paddingValues ->
        ticket?.let { t ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ── Hero Card ─────────────────────────────────────────────────
                HeroCard(ticket = t)

                // ── Description ───────────────────────────────────────────────
                if (t.description.isNotBlank()) {
                    DetailCard(title = "DESCRIPTION") {
                        Text(
                            t.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 22.sp
                        )
                    }
                }

                // ── Ticket Info ───────────────────────────────────────────────
                DetailCard(title = "TICKET INFO") {
                    InfoRow("Created", formatDetailDate(t.createdAt))
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 6.dp))
                    InfoRow("Last Updated", formatDetailDate(t.updatedAt))
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 6.dp))
                    InfoRow("Sync Status", if (t.isSynced) "✓ Synced to server" else "⏳ Pending sync")
                }

                // ── Update Status ─────────────────────────────────────────────
                DetailCard(title = "UPDATE STATUS") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        TicketStatus.entries.forEach { status ->
                            val isSelected = t.status == status
                            val (color, bg) = when (status) {
                                TicketStatus.PENDING     -> CampusColors.StatusPending to CampusColors.StatusPendingBg
                                TicketStatus.IN_PROGRESS -> CampusColors.StatusProgress to CampusColors.StatusProgressBg
                                TicketStatus.COMPLETED   -> CampusColors.StatusDone to CampusColors.StatusDoneBg
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) color.copy(alpha = 0.15f) else bg.copy(alpha = 0.5f))
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) color else color.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { viewModel.updateTicketStatus(t, status) }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(color, CircleShape)
                                )
                                Text(
                                    text = status.displayName,
                                    color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 14.sp
                                )
                                if (isSelected) {
                                    Spacer(Modifier.weight(1f))
                                    Text("✓", color = color, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        } ?: Box(
            Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = CampusColors.Amber)
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text("Delete Ticket", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            },
            text = {
                Text(
                    "This ticket will be permanently removed. This action cannot be undone.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        ticket?.let { viewModel.deleteTicket(it) }
                        showDeleteDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CampusColors.PriorityHigh),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Delete", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteDialog = false },
                    shape = RoundedCornerShape(10.dp),
                    border = ButtonDefaults.outlinedButtonBorder
                ) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun HeroCard(ticket: ServiceTicket) {
    val catColor = when (ticket.category.name) {
        "IT"         -> CampusColors.CatIT
        "FACILITIES" -> CampusColors.CatFacilities
        else         -> CampusColors.CatLibrary
    }
    val (statusColor, statusBg) = when (ticket.status) {
        TicketStatus.PENDING     -> CampusColors.StatusPending to CampusColors.StatusPendingBg
        TicketStatus.IN_PROGRESS -> CampusColors.StatusProgress to CampusColors.StatusProgressBg
        TicketStatus.COMPLETED   -> CampusColors.StatusDone to CampusColors.StatusDoneBg
    }
    val priorityColor = when (ticket.priority.name) {
        "HIGH"   -> CampusColors.PriorityHigh
        "MEDIUM" -> CampusColors.PriorityMed
        else     -> CampusColors.PriorityLow
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        catColor.copy(alpha = 0.15f),
                        MaterialTheme.colorScheme.surface
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
            .border(1.dp, catColor.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = ticket.title,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 26.sp
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                // Status
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(statusBg)
                        .border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(ticket.status.displayName, color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                // Category
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(catColor.copy(alpha = 0.12f))
                        .border(1.dp, catColor.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(ticket.category.displayName, color = catColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                // Priority
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(modifier = Modifier.size(8.dp).background(priorityColor, CircleShape))
                    Text(ticket.priority.displayName, color = priorityColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun DetailCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = CampusColors.TextSecondary,
            letterSpacing = 1.2.sp
        )
        content()
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        Text(value, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

fun formatDetailDate(timestamp: Long): String =
    SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault()).format(Date(timestamp))