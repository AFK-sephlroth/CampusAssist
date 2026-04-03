package com.example.campusassist.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusassist.domain.model.AppNotification
import com.example.campusassist.domain.model.NotificationType
import com.example.campusassist.ui.theme.CampusColors
import com.example.campusassist.ui.viewmodel.NotificationViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    viewModel: NotificationViewModel,
    onNavigateBack: () -> Unit
) {
    val notifications by viewModel.notifications.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.Bold, color = CampusColors.TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Box(
                            Modifier.size(36.dp).clip(CircleShape).background(CampusColors.TextMuted.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Default.ArrowBack, null, tint = CampusColors.TextPrimary) }
                    }
                },
                actions = {
                    if (notifications.any { !it.isRead }) {
                        IconButton(onClick = viewModel::markAllAsRead) {
                            Icon(Icons.Default.DoneAll, "Mark all read", tint = CampusColors.Amber)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D1F3C))
            )
        }
    ) { pv ->
        if (notifications.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(pv), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.NotificationsNone, null, modifier = Modifier.size(48.dp), tint = CampusColors.TextMuted)
                    Text("No notifications yet", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.titleMedium)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(pv),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(notifications, key = { it.id }) { notif ->
                    NotificationCard(notif = notif, onRead = { viewModel.markAsRead(notif.id) })
                }
            }
        }
    }
}

@Composable
fun NotificationCard(notif: AppNotification, onRead: () -> Unit) {
    val (typeColor, typeLabel, typeEmoji) = when (notif.type) {
        NotificationType.STATUS_CHANGE  -> Triple(CampusColors.StatusProgress, "Status Update", "🔄")
        NotificationType.ASSIGNED       -> Triple(CampusColors.Amber, "Assigned", "👤")
        NotificationType.SYNC_COMPLETE  -> Triple(CampusColors.StatusDone, "Synced", "✓")
        NotificationType.CONFLICT       -> Triple(CampusColors.PriorityHigh, "Conflict", "⚠")
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (!notif.isRead) MaterialTheme.colorScheme.surface
                else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
            )
            .border(
                1.dp,
                if (!notif.isRead) typeColor.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                RoundedCornerShape(14.dp)
            )
            .clickable { if (!notif.isRead) onRead() }
            .padding(14.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
            // Icon bubble
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(typeColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) { Text(typeEmoji, fontSize = 18.sp) }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(notif.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                    if (!notif.isRead) {
                        Box(modifier = Modifier.size(8.dp).background(typeColor, CircleShape))
                    }
                }
                Text(notif.message, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    SimpleDateFormat("MMM d · h:mm a", Locale.getDefault()).format(Date(notif.createdAt)),
                    fontSize = 11.sp, color = CampusColors.TextMuted
                )
            }
        }
    }
}