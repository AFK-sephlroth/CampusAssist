package com.example.campusassist.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.campusassist.ui.theme.CampusColors

// ── Tutorial step data ────────────────────────────────────────────────────────

private data class TutorialStep(
    val emoji: String,
    val title: String,
    val description: String,
    val tip: String? = null
)

private val tutorialSteps = listOf(
    TutorialStep(
        emoji       = "👋",
        title       = "Welcome to CampusAssist!",
        description = "Your one-stop place for submitting and tracking campus service requests — IT, Facilities, Library, and more.",
        tip         = "This quick tour will show you around in just a few steps."
    ),
    TutorialStep(
        emoji       = "➕",
        title       = "Submit a Request",
        description = "Tap the gold \"+\" button at the bottom-right to create a new service ticket. Fill in the title, description, priority, and category.",
        tip         = "You can also attach up to 3 photos as evidence."
    ),
    TutorialStep(
        emoji       = "🎫",
        title       = "Track Your Tickets",
        description = "All your submitted requests appear on the home screen. Use the filter chips to view Pending, In Progress, or Completed tickets at a glance.",
        tip         = "Tap any ticket to see its full details."
    ),
    TutorialStep(
        emoji       = "🔄",
        title       = "Update Status",
        description = "Inside a ticket's detail page, you can change its status between Pending, In Progress, and Completed as work progresses.",
        tip         = "Staff members can also leave notes on your tickets."
    ),
    TutorialStep(
        emoji       = "💬",
        title       = "Chat on a Ticket",
        description = "Hit the 💬 button in the top-right of any ticket detail to open a live chat thread. Talk directly to staff or collaborators about the issue.",
        tip         = "Messages are saved locally and persist across sessions."
    ),
    TutorialStep(
        emoji       = "🔔",
        title       = "Notifications & Profile",
        description = "The bell icon shows your latest updates. Tap your avatar to view your profile, switch dark/light mode, or sign out.",
        tip         = "You're all set — let's get started! 🎉"
    )
)

// ── Main overlay composable ───────────────────────────────────────────────────

@Composable
fun OnboardingOverlay(
    onFinish: () -> Unit,
    onSkip: () -> Unit
) {
    var currentStep by remember { mutableIntStateOf(0) }
    val step = tutorialSteps[currentStep]
    val isLast = currentStep == tutorialSteps.lastIndex

    // Pulsing emoji animation
    val pulse = rememberInfiniteTransition(label = "pulse")
    val emojiScale by pulse.animateFloat(
        initialValue = 1f,
        targetValue  = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "emojiScale"
    )

    Dialog(
        onDismissRequest = { /* block back-button dismissal so user must tap Skip */ },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress      = false,
            dismissOnClickOutside   = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.72f)),
            contentAlignment = Alignment.Center
        ) {
            // Card
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { it } + fadeIn()) togetherWith
                        (slideOutHorizontally { -it } + fadeOut())
                    } else {
                        (slideInHorizontally { -it } + fadeIn()) togetherWith
                        (slideOutHorizontally { it } + fadeOut())
                    }
                },
                label = "stepTransition"
            ) { stepIndex ->
                val s = tutorialSteps[stepIndex]
                TutorialCard(
                    step       = s,
                    stepIndex  = stepIndex,
                    totalSteps = tutorialSteps.size,
                    emojiScale = emojiScale,
                    isLast     = stepIndex == tutorialSteps.lastIndex,
                    onNext     = {
                        if (stepIndex < tutorialSteps.lastIndex) currentStep++
                        else onFinish()
                    },
                    onPrev     = { if (stepIndex > 0) currentStep-- },
                    onSkip     = onSkip
                )
            }
        }
    }
}

@Composable
private fun TutorialCard(
    step: TutorialStep,
    stepIndex: Int,
    totalSteps: Int,
    emojiScale: Float,
    isLast: Boolean,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 28.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF0D1F3C), Color(0xFF162A48)),
                    start  = Offset(0f, 0f),
                    end    = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
            .border(1.dp, CampusColors.Amber.copy(alpha = 0.35f), RoundedCornerShape(28.dp))
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Skip link — top right
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Text(
                text     = "Skip tutorial",
                color    = CampusColors.TextMuted,
                fontSize = 12.sp,
                modifier = Modifier.clickable { onSkip() }
            )
        }

        // Emoji
        Box(
            modifier = Modifier
                .size(88.dp)
                .scale(emojiScale)
                .clip(CircleShape)
                .background(CampusColors.Amber.copy(alpha = 0.12f))
                .border(1.5.dp, CampusColors.Amber.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(step.emoji, fontSize = 38.sp)
        }

        // Title
        Text(
            text       = step.title,
            fontWeight = FontWeight.ExtraBold,
            fontSize   = 20.sp,
            color      = CampusColors.TextPrimary,
            textAlign  = TextAlign.Center,
            lineHeight = 26.sp
        )

        // Description
        Text(
            text      = step.description,
            fontSize  = 14.sp,
            color     = CampusColors.TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 21.sp
        )

        // Tip pill
        step.tip?.let { tip ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CampusColors.Amber.copy(alpha = 0.10f))
                    .border(1.dp, CampusColors.Amber.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text      = "💡 $tip",
                    fontSize  = 12.sp,
                    color     = CampusColors.Amber,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                    modifier  = Modifier.fillMaxWidth()
                )
            }
        }

        // Step dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            repeat(totalSteps) { idx ->
                val isActive = idx == stepIndex
                Box(
                    modifier = Modifier
                        .animateContentSize()
                        .height(6.dp)
                        .width(if (isActive) 22.dp else 6.dp)
                        .clip(CircleShape)
                        .background(
                            if (isActive) CampusColors.Amber
                            else CampusColors.TextMuted.copy(alpha = 0.4f)
                        )
                )
            }
        }

        // Navigation buttons
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Back button (hidden on step 0)
            if (stepIndex > 0) {
                OutlinedButton(
                    onClick  = onPrev,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape    = RoundedCornerShape(14.dp),
                    border   = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.linearGradient(listOf(CampusColors.TextMuted, CampusColors.TextMuted))
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CampusColors.TextSecondary)
                ) {
                    Text("← Back", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }

            // Next / Finish button
            Button(
                onClick  = onNext,
                modifier = Modifier
                    .weight(if (stepIndex > 0) 1.6f else 1f)
                    .height(48.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = CampusColors.Amber,
                    contentColor   = CampusColors.NavyDeep
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Text(
                    text       = if (isLast) "Get Started 🚀" else "Next →",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize   = 14.sp
                )
            }
        }

        // Step counter text
        Text(
            text     = "Step ${stepIndex + 1} of $totalSteps",
            fontSize = 11.sp,
            color    = CampusColors.TextMuted
        )
    }
}
