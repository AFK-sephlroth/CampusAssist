package com.example.campusassist.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusassist.domain.model.Department
import com.example.campusassist.domain.model.ServiceTicket
import com.example.campusassist.domain.model.TicketStatus
import com.example.campusassist.ui.theme.CampusColors
import com.example.campusassist.ui.viewmodel.DepartmentViewModel
import com.example.campusassist.ui.viewmodel.TicketViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketListScreen(
    viewModel: TicketViewModel,
    onCreateTicket: () -> Unit,
    onTicketClick: (Long) -> Unit,
    onProfileClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    notifViewModel: com.example.campusassist.ui.viewmodel.NotificationViewModel? = null,
    syncViewModel: com.example.campusassist.ui.viewmodel.SyncViewModel? = null,
    currentUser: com.example.campusassist.domain.model.User? = null,
    departmentViewModel: DepartmentViewModel
) {
    val uiState by viewModel.listUiState.collectAsState()
    val deptState by departmentViewModel.uiState.collectAsState()
    val syncState by syncViewModel?.syncState?.collectAsState()
        ?: remember { mutableStateOf(com.example.campusassist.ui.viewmodel.SyncUiState()) }
    var selectedFilter by remember { mutableStateOf<TicketStatus?>(null) }

    val filteredTickets = remember(uiState.tickets, selectedFilter) {
        if (selectedFilter == null) uiState.tickets
        else uiState.tickets.filter { it.status == selectedFilter }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateTicket,
                containerColor = CampusColors.Amber,
                contentColor = CampusColors.NavyDeep,
                shape = CircleShape,
                modifier = Modifier
                    .size(60.dp)
                    .shadow(16.dp, CircleShape)
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Ticket", modifier = Modifier.size(28.dp))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ── Gradient Header ───────────────────────────────────────────────
            GradientHeader(
                isOffline = uiState.isOffline,
                onProfileClick = onProfileClick,
                onNotificationsClick = onNotificationsClick,
                unreadCount = notifViewModel?.unreadCount?.collectAsState()?.value ?: 0,
                currentUser = currentUser
            )

            // ── Sync Status Bar ───────────────────────────────────────────────
            com.example.campusassist.ui.components.SyncStatusBar(
                syncState = syncState,
                onSyncNow = { syncViewModel?.syncNow() },
                onDismiss = { syncViewModel?.dismissSyncBanner() }
            )

            // ── Stats Row ────────────────────────────────────────────────────
            StatsRow(tickets = uiState.tickets)

            // ── Filter Chips ─────────────────────────────────────────────────
            FilterChipRow(
                selected = selectedFilter,
                onSelect = { selectedFilter = if (selectedFilter == it) null else it },
                counts = mapOf(
                    TicketStatus.PENDING to uiState.tickets.count { it.status == TicketStatus.PENDING },
                    TicketStatus.IN_PROGRESS to uiState.tickets.count { it.status == TicketStatus.IN_PROGRESS },
                    TicketStatus.COMPLETED to uiState.tickets.count { it.status == TicketStatus.COMPLETED }
                )
            )

            // ── Ticket List ───────────────────────────────────────────────────
            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CampusColors.Amber)
                }
            } else if (filteredTickets.isEmpty()) {
                EmptyState(hasFilter = selectedFilter != null)
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredTickets, key = { it.id }) { ticket ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + slideInVertically()
                        ) {
                            TicketCard(
                                ticket = ticket,
                                departments = deptState.departments,
                                onClick = { onTicketClick(ticket.id) })
                        }
                    }
                    item { Spacer(Modifier.height(72.dp)) }
                }
            }
        }
    }
}

@Composable
fun GradientHeader(
    isOffline: Boolean,
    onProfileClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    unreadCount: Int = 0,
    currentUser: com.example.campusassist.domain.model.User? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF0D1F3C), Color(0xFF1A2E50), Color(0xFF0D1F3C)),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
    ) {
        // Decorative glow blob
        Box(
            modifier = Modifier
                .size(180.dp)
                .offset(x = (-30).dp, y = (-40).dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(CampusColors.Amber.copy(alpha = 0.12f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .padding(top = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CampusAssist+",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        color = CampusColors.TextPrimary,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Smart Campus Services",
                        fontSize = 12.sp,
                        color = CampusColors.TextSecondary,
                        letterSpacing = 0.5.sp
                    )
                }

                if (isOffline) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(CampusColors.PriorityHigh.copy(alpha = 0.15f))
                            .border(1.dp, CampusColors.PriorityHigh.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(modifier = Modifier.size(6.dp).background(CampusColors.PriorityHigh, CircleShape))
                        Text("Offline", color = CampusColors.PriorityHigh, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Notification bell
                        Box {
                            IconButton(
                                onClick = onNotificationsClick,
                                modifier = Modifier.size(38.dp).clip(CircleShape).background(CampusColors.TextMuted.copy(alpha = 0.2f))
                            ) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = CampusColors.TextPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            if (unreadCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(CampusColors.PriorityHigh, CircleShape)
                                        .align(Alignment.TopEnd),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (unreadCount > 9) "9+" else unreadCount.toString(),
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        // Profile avatar
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(CampusColors.Amber)
                                .clickable { onProfileClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (currentUser?.name?.take(2) ?: "?").uppercase(),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = CampusColors.NavyDeep
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatsRow(tickets: List<ServiceTicket>) {
    val pending = tickets.count { it.status == TicketStatus.PENDING }
    val inProgress = tickets.count { it.status == TicketStatus.IN_PROGRESS }
    val done = tickets.count { it.status == TicketStatus.COMPLETED }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatCard(label = "Pending", count = pending, color = CampusColors.StatusPending, modifier = Modifier.weight(1f))
        StatCard(label = "In Progress", count = inProgress, color = CampusColors.StatusProgress, modifier = Modifier.weight(1f))
        StatCard(label = "Done", count = done, color = CampusColors.StatusDone, modifier = Modifier.weight(1f))
    }
}

@Composable
fun StatCard(label: String, count: Int, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.1f))
            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = count.toString(),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                color = color
            )
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = color.copy(alpha = 0.7f),
                letterSpacing = 0.3.sp
            )
        }
    }
}

@Composable
fun FilterChipRow(
    selected: TicketStatus?,
    onSelect: (TicketStatus) -> Unit,
    counts: Map<TicketStatus, Int>
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(TicketStatus.entries) { status ->
            val isSelected = selected == status
            val (color, bg) = when (status) {
                TicketStatus.PENDING -> CampusColors.StatusPending to CampusColors.StatusPendingBg
                TicketStatus.IN_PROGRESS -> CampusColors.StatusProgress to CampusColors.StatusProgressBg
                TicketStatus.COMPLETED -> CampusColors.StatusDone to CampusColors.StatusDoneBg
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) color else bg)
                    .border(
                        1.dp,
                        if (isSelected) color else color.copy(alpha = 0.3f),
                        RoundedCornerShape(20.dp)
                    )
                    .clickable { onSelect(status) }
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Text(
                    text = "${status.displayName} (${counts[status] ?: 0})",
                    color = if (isSelected) CampusColors.NavyDeep else color,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun EmptyState(hasFilter: Boolean) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("✦", fontSize = 36.sp, color = CampusColors.TextMuted)
            Text(
                text = if (hasFilter) "No tickets match this filter" else "No tickets yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (hasFilter) "Try clearing the filter" else "Tap + to submit a service request",
                style = MaterialTheme.typography.bodyMedium,
                color = CampusColors.TextMuted
            )
        }
    }
}

@Composable
fun TicketCard(
    ticket: ServiceTicket,
    departments: List<Department>,
    onClick: () -> Unit
) {
    val (statusColor, statusBg) = when (ticket.status) {
        TicketStatus.PENDING    -> CampusColors.StatusPending to CampusColors.StatusPendingBg
        TicketStatus.IN_PROGRESS -> CampusColors.StatusProgress to CampusColors.StatusProgressBg
        TicketStatus.COMPLETED  -> CampusColors.StatusDone to CampusColors.StatusDoneBg
    }
    val departmentName = departments.firstOrNull { it.id == ticket.departmentId }?.name ?: "No department"
    val catColor = when (ticket.category.name) {
        "IT"         -> CampusColors.CatIT
        "FACILITIES" -> CampusColors.CatFacilities
        else         -> CampusColors.CatLibrary
    }
    val priorityColor = when (ticket.priority.name) {
        "HIGH"   -> CampusColors.PriorityHigh
        "MEDIUM" -> CampusColors.PriorityMed
        else     -> CampusColors.PriorityLow
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        catColor.copy(alpha = 0.4f),
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
    ) {
        // Left accent bar
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(catColor, catColor.copy(alpha = 0.3f))
                    ),
                    shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                )
                .align(Alignment.CenterStart)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 14.dp, top = 14.dp, bottom = 12.dp)
        ) {
            // Title + Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = ticket.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                )
                // Status pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(statusBg)
                        .border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = ticket.status.displayName,
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(5.dp))

            // Description
            if (ticket.description.isNotBlank()) {
                Text(
                    text = ticket.description,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
            }

            // Bottom row: category + priority + sync + date
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Category
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(catColor.copy(alpha = 0.12f))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(ticket.category.displayName, color = catColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                // Add after the category Box, before the priority Row
                ticket.departmentId?.let {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CampusColors.TextMuted.copy(alpha = 0.1f))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = departmentName,
                            color = CampusColors.TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Priority dot + label
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Box(modifier = Modifier.size(7.dp).background(priorityColor, CircleShape))
                    Text(ticket.priority.displayName, color = priorityColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }

                if (!ticket.isSynced) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CampusColors.PriorityHigh.copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text("Unsynced", color = CampusColors.PriorityHigh, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(Modifier.weight(1f))

                Text(
                    text = formatDate(ticket.createdAt),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

fun formatDate(timestamp: Long): String =
    SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp))