package com.example.campusassist.ui.screens

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.campusassist.domain.model.ServiceTicket
import com.example.campusassist.domain.model.TicketStatus
import com.example.campusassist.ui.theme.CampusColors
import com.example.campusassist.ui.viewmodel.ChatViewModel
import com.example.campusassist.ui.viewmodel.DepartmentViewModel
import com.example.campusassist.ui.viewmodel.TicketViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketDetailScreen(
    ticketId: Long,
    viewModel: TicketViewModel,
    departmentViewModel: DepartmentViewModel,
    onNavigateBack: () -> Unit,
    // Current logged-in user — passed from nav so we know who is sending messages
    currentUsername: String = "user",
    currentDisplayName: String = "User"
) {
    val chatViewModel: ChatViewModel = hiltViewModel()

    val ticket        by viewModel.selectedTicket.collectAsState()
    val deptState     by departmentViewModel.uiState.collectAsState()
    val chatMessages  by chatViewModel.messages.collectAsState()

    val departmentName = deptState.departments
        .firstOrNull { it.id == ticket?.departmentId }?.name

    var showDeleteDialog by remember { mutableStateOf(false) }
    var lightboxUri      by remember { mutableStateOf<String?>(null) }
    var showChat         by remember { mutableStateOf(false) }
    var chatInput        by remember { mutableStateOf("") }

    val chatListState = rememberLazyListState()

    LaunchedEffect(ticketId) {
        viewModel.loadTicketById(ticketId)
        chatViewModel.setTicket(ticketId)
    }

    // Auto-scroll to latest message
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            chatListState.animateScrollToItem(chatMessages.lastIndex)
        }
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
            ) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "Ticket Details",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = CampusColors.TextPrimary
                            )
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
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = CampusColors.TextPrimary
                                )
                            }
                        }
                    },
                    actions = {
                        // Chat toggle button
                        IconButton(onClick = { showChat = !showChat }) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (showChat) CampusColors.Amber.copy(alpha = 0.25f)
                                        else CampusColors.TextMuted.copy(alpha = 0.15f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "💬",
                                    fontSize = 16.sp
                                )
                            }
                        }
                        // Delete button
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(CampusColors.PriorityHigh.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = CampusColors.PriorityHigh
                                )
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
            ) {
                // ── Chat panel (slides in below the top bar) ──────────────────
                AnimatedVisibility(
                    visible = showChat,
                    enter   = expandVertically(),
                    exit    = shrinkVertically()
                ) {
                    ChatPanel(
                        messages         = chatMessages,
                        listState        = chatListState,
                        chatInput        = chatInput,
                        onInputChange    = { chatInput = it },
                        onSend           = {
                            chatViewModel.sendMessage(
                                ticketId          = ticketId,
                                senderUsername    = currentUsername,
                                senderDisplayName = currentDisplayName,
                                message           = chatInput
                            )
                            chatInput = ""
                        },
                        currentUsername  = currentUsername,
                        modifier         = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 220.dp, max = 320.dp)
                    )
                }

                // ── Scrollable detail content ─────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    HeroCard(ticket = t, departmentName = departmentName)

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

                    val attachments = t.attachmentUris
                        ?.split(",")
                        ?.map { it.trim() }
                        ?.filter { it.isNotBlank() }
                        .orEmpty()
                    if (attachments.isNotEmpty()) {
                        DetailCard(title = "ATTACHMENTS (${attachments.size})") {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                attachments.forEach { uriString ->
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(Uri.parse(uriString))
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Attachment",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(
                                                1.dp,
                                                MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                                                RoundedCornerShape(12.dp)
                                            )
                                            .clickable { lightboxUri = uriString }
                                    )
                                }
                            }
                        }
                    }

                    DetailCard(title = "TICKET INFO") {
                        InfoRow("Created", formatDetailDate(t.createdAt))
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 6.dp))
                        InfoRow("Last Updated", formatDetailDate(t.updatedAt))
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 6.dp))
                        InfoRow("Sync Status", if (t.isSynced) "✓ Synced to server" else "⏳ Pending sync")
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 6.dp))
                        InfoRow("Department", departmentName ?: "Not specified")
                    }

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
            }
        } ?: Box(
            Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = CampusColors.Amber)
        }
    }

    // ── Fullscreen image lightbox ─────────────────────────────────────────────
    lightboxUri?.let { uri ->
        Dialog(
            onDismissRequest = { lightboxUri = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable { lightboxUri = null },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(Uri.parse(uri))
                        .crossfade(true)
                        .build(),
                    contentDescription = "Full image",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .clickable { lightboxUri = null },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
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

// ── Chat Panel ────────────────────────────────────────────────────────────────

@Composable
private fun ChatPanel(
    messages: List<com.example.campusassist.ui.viewmodel.ChatMessage>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    chatInput: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    currentUsername: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = CampusColors.Amber.copy(alpha = 0.3f),
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
            )
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CampusColors.Amber.copy(alpha = 0.08f))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("💬", fontSize = 14.sp)
            Spacer(Modifier.width(6.dp))
            Text(
                "Ticket Chat",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = CampusColors.Amber,
                letterSpacing = 0.5.sp
            )
            if (messages.isNotEmpty()) {
                Spacer(Modifier.width(6.dp))
                Text(
                    "(${messages.size})",
                    fontSize = 11.sp,
                    color = CampusColors.TextSecondary
                )
            }
        }

        HorizontalDivider(color = CampusColors.Amber.copy(alpha = 0.2f))

        // Message list
        if (messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No messages yet. Start the conversation!",
                    color = CampusColors.TextMuted,
                    fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(
                state    = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    val isMe = msg.senderUsername == currentUsername
                    ChatBubble(message = msg, isMe = isMe)
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        // Input row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value         = chatInput,
                onValueChange = onInputChange,
                placeholder   = { Text("Type a message…", color = CampusColors.TextMuted, fontSize = 13.sp) },
                modifier      = Modifier.weight(1f),
                shape         = RoundedCornerShape(24.dp),
                maxLines      = 3,
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = CampusColors.Amber,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    cursorColor          = CampusColors.Amber,
                    focusedTextColor     = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor   = MaterialTheme.colorScheme.onSurface
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
            )
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (chatInput.isNotBlank()) CampusColors.Amber
                        else CampusColors.TextMuted.copy(alpha = 0.2f)
                    )
                    .clickable(enabled = chatInput.isNotBlank()) { onSend() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Default.Send,
                    contentDescription = "Send",
                    tint               = if (chatInput.isNotBlank()) CampusColors.NavyDeep else CampusColors.TextMuted,
                    modifier           = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ChatBubble(
    message: com.example.campusassist.ui.viewmodel.ChatMessage,
    isMe: Boolean
) {
    val bubbleColor  = if (isMe) CampusColors.Amber else MaterialTheme.colorScheme.surfaceVariant
    val textColor    = if (isMe) CampusColors.NavyDeep else MaterialTheme.colorScheme.onSurface
    val alignment    = if (isMe) Alignment.End else Alignment.Start
    val cornerShape  = if (isMe)
        RoundedCornerShape(topStart = 14.dp, topEnd = 4.dp, bottomStart = 14.dp, bottomEnd = 14.dp)
    else
        RoundedCornerShape(topStart = 4.dp, topEnd = 14.dp, bottomStart = 14.dp, bottomEnd = 14.dp)

    Column(
        modifier            = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        if (!isMe) {
            Text(
                text      = message.senderDisplayName,
                fontSize  = 10.sp,
                color     = CampusColors.TextSecondary,
                fontWeight = FontWeight.SemiBold,
                modifier  = Modifier.padding(start = 4.dp, bottom = 2.dp)
            )
        }
        Box(
            modifier = Modifier
                .clip(cornerShape)
                .background(bubbleColor)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .widthIn(max = 260.dp)
        ) {
            Text(
                text     = message.message,
                color    = textColor,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
        Text(
            text     = formatChatTime(message.sentAt),
            fontSize = 10.sp,
            color    = CampusColors.TextMuted,
            modifier = Modifier.padding(
                start = if (isMe) 0.dp else 4.dp,
                end   = if (isMe) 4.dp else 0.dp,
                top   = 2.dp
            )
        )
    }
}

// ── Shared composables from original file ─────────────────────────────────────

@Composable
fun HeroCard(ticket: ServiceTicket, departmentName: String?) {
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
                    colors = listOf(catColor.copy(alpha = 0.15f), MaterialTheme.colorScheme.surface),
                    start  = Offset(0f, 0f),
                    end    = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
            .border(1.dp, catColor.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(ticket.title, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 26.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(statusBg).border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp)).padding(horizontal = 10.dp, vertical = 5.dp)) {
                    Text(ticket.status.displayName, color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Box(modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(catColor.copy(alpha = 0.12f)).border(1.dp, catColor.copy(alpha = 0.35f), RoundedCornerShape(20.dp)).padding(horizontal = 10.dp, vertical = 5.dp)) {
                    Text(ticket.category.displayName, color = catColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                departmentName?.let {
                    Box(modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(CampusColors.TextMuted.copy(alpha = 0.1f)).border(1.dp, CampusColors.TextMuted.copy(alpha = 0.3f), RoundedCornerShape(20.dp)).padding(horizontal = 10.dp, vertical = 5.dp)) {
                        Text(it, color = CampusColors.TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
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
        Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CampusColors.TextSecondary, letterSpacing = 1.2.sp)
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

private fun formatChatTime(timestamp: Long): String =
    SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))
