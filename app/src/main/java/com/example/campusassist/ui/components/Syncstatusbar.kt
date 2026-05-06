package com.example.campusassist.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusassist.ui.theme.CampusColors
import com.example.campusassist.ui.viewmodel.SyncUiState

/**
 * Shown at the top of TicketListScreen.
 * - Red bar when offline
 * - Amber spinning bar when syncing
 * - Green banner when sync completes
 */
@Composable
fun SyncStatusBar(
    syncState: SyncUiState,
    onSyncNow: () -> Unit,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = !syncState.isOnline || syncState.isSyncing || syncState.showSyncBanner,
        enter = slideInVertically() + fadeIn(),
        exit  = slideOutVertically() + fadeOut()
    ) {
        when {
            !syncState.isOnline  -> OfflineBar(onSyncNow)
            syncState.isSyncing  -> SyncingBar()
            syncState.showSyncBanner -> SyncSuccessBanner(
                message = syncState.lastSyncMessage ?: "Sync complete",
                onDismiss = onDismiss
            )
        }
    }
}

@Composable
private fun OfflineBar(onRetry: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CampusColors.PriorityHigh.copy(alpha = 0.15f))
            .border(
                width = 0.dp,
                color = Color.Transparent,
                shape = RoundedCornerShape(0.dp)
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(modifier = Modifier.size(8.dp).background(CampusColors.PriorityHigh, CircleShape))

        Text(
            text = "You're offline — tickets saved locally",
            color = CampusColors.PriorityHigh,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(CampusColors.PriorityHigh.copy(alpha = 0.15f))
                .border(1.dp, CampusColors.PriorityHigh.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                .clickable { onRetry() }
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Text("Retry", color = CampusColors.PriorityHigh, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SyncingBar() {
    val rotation by rememberInfiniteTransition(label = "sync_spin").animateFloat(
        initialValue = 0f,
        targetValue  = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CampusColors.Amber.copy(alpha = 0.1f))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Syncing",
            tint = CampusColors.Amber,
            modifier = Modifier.size(16.dp).rotate(rotation)
        )
        Text(
            text = "Syncing tickets to server...",
            color = CampusColors.Amber,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.weight(1f))
        CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            strokeWidth = 2.dp,
            color = CampusColors.Amber
        )
    }
}

@Composable
private fun SyncSuccessBanner(message: String, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CampusColors.StatusDone.copy(alpha = 0.1f))
            .border(
                width = 0.dp,
                color = Color.Transparent,
                shape = RoundedCornerShape(0.dp)
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("✓", color = CampusColors.StatusDone, fontSize = 14.sp, fontWeight = FontWeight.Bold)

        Text(
            text = message,
            color = CampusColors.StatusDone,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )

        IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Dismiss",
                tint = CampusColors.StatusDone,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}