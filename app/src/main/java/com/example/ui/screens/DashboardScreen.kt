package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material.icons.rounded.RemoveRedEye
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.ScreeningSessionEntity
import com.example.data.local.UserProfileEntity
import com.example.ui.components.ClinicalMetricTile
import com.example.ui.components.CompletionStatusChip
import com.example.ui.components.HeaderQuickControls
import com.example.ui.components.MedicalDisclaimerCard
import com.example.ui.components.ModuleCompletionRingChart
import com.example.ui.components.ScreeningHistoryBarChart
import com.example.ui.components.SdgThreePill
import com.example.ui.components.formatScreeningDate
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalStrings
import kotlin.math.roundToInt

@Composable
fun DashboardScreen(
    profile: UserProfileEntity,
    activeSession: ScreeningSessionEntity?,
    history: List<ScreeningSessionEntity>,
    language: AppLanguage,
    isDarkMode: Boolean,
    onToggleLanguage: () -> Unit,
    onToggleDarkMode: () -> Unit,
    onStartNewSession: () -> Unit,
    onOpenAcuityTest: () -> Unit,
    onOpenColorTest: () -> Unit,
    onOpenBlinkTest: () -> Unit,
    onOpenMovementTest: () -> Unit,
    onOpenReport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val totalTestsCompletedAllTime = history.sumOf { it.completedModuleCount }
    val latestCompletedSession = history.firstOrNull { it.hasAnyResult }
    val lastDateText = latestCompletedSession?.let { formatScreeningDate(it.updatedAt) } ?: strings.noScreeningYet

    val currentCompletedCount = activeSession?.completedModuleCount ?: 0

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 720.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Greeting Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${strings.goodMorningGreeting} ${profile.name}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = strings.tagline,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    HeaderQuickControls(
                        language = language,
                        isDarkMode = isDarkMode,
                        onToggleLanguage = onToggleLanguage,
                        onToggleDarkMode = onToggleDarkMode
                    )
                }
            }

            // Hero Clinical Banner Card
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(166.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_hero_vision_banner_1795579126763),
                            contentDescription = "VisionScan AI screening hero illustration",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                                            MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(18.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                SdgThreePill()
                                Text(
                                    text = strings.activeSessionTitle,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                OutlinedButton(
                                    onClick = onStartNewSession,
                                    shape = RoundedCornerShape(50),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("start_new_session_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.AddCircleOutline,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(strings.startNewSession, style = MaterialTheme.typography.labelMedium)
                                }
                            }
                            ModuleCompletionRingChart(completedCount = currentCompletedCount)
                        }
                    }
                }
            }

            // Medical Disclaimer Banner
            item {
                MedicalDisclaimerCard(text = strings.medicalDisclaimerShort)
            }

            // Analytics Section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = strings.analyticsTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ClinicalMetricTile(
                            icon = Icons.Rounded.Assessment,
                            label = strings.totalTestsCompleted,
                            value = "$totalTestsCompletedAllTime",
                            accentColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        ClinicalMetricTile(
                            icon = Icons.Rounded.EventAvailable,
                            label = strings.lastScreeningDate,
                            value = lastDateText,
                            accentColor = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val acuityVal = if (activeSession?.acuityCompleted == true) {
                            "R:${activeSession.rightEyeLevel}/6 L:${activeSession.leftEyeLevel}/6"
                        } else strings.statusPending
                        val colorVal = if (activeSession?.colorCompleted == true) {
                            "${activeSession.colorCorrect}/${activeSession.colorTotal}"
                        } else strings.statusPending
                        val blinkVal = if (activeSession?.blinkCompleted == true) {
                            "${activeSession.blinksPerMinute.roundToInt()} /min"
                        } else strings.statusPending

                        ClinicalMetricTile(
                            icon = Icons.Rounded.FormatSize,
                            label = strings.acuityCardTitle,
                            value = acuityVal,
                            accentColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        ClinicalMetricTile(
                            icon = Icons.Rounded.ColorLens,
                            label = strings.colorCardTitle,
                            value = colorVal,
                            accentColor = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.weight(1f)
                        )
                        ClinicalMetricTile(
                            icon = Icons.Rounded.RemoveRedEye,
                            label = strings.blinkCardTitle,
                            value = blinkVal,
                            accentColor = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // History Completion Trend Chart
                    if (history.any { it.hasAnyResult }) {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = strings.historyTitle,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                ScreeningHistoryBarChart(sessions = history.filter { it.hasAnyResult })
                                Text(
                                    text = strings.progressOverviewNote,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // 5 Screening Modules Header
            item {
                Text(
                    text = strings.screeningModulesTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // Module 1: Visual Acuity Test
            item {
                ScreeningModuleCard(
                    icon = Icons.Rounded.FormatSize,
                    title = strings.acuityCardTitle,
                    description = strings.acuityCardDesc,
                    completed = activeSession?.acuityCompleted == true,
                    resultSummary = if (activeSession?.acuityCompleted == true) {
                        "${strings.rightEyeLabel}: L${activeSession.rightEyeLevel}/6 • ${strings.leftEyeLabel}: L${activeSession.leftEyeLevel}/6"
                    } else null,
                    actionLabel = if (activeSession?.acuityCompleted == true) strings.retakeTest else strings.startTest,
                    accentColor = MaterialTheme.colorScheme.primary,
                    testTag = "card_visual_acuity_start",
                    onClick = onOpenAcuityTest
                )
            }

            // Module 2: Color Vision Test
            item {
                ScreeningModuleCard(
                    icon = Icons.Rounded.ColorLens,
                    title = strings.colorCardTitle,
                    description = strings.colorCardDesc,
                    completed = activeSession?.colorCompleted == true,
                    resultSummary = if (activeSession?.colorCompleted == true) {
                        "${activeSession.colorCorrect}/${activeSession.colorTotal} plates • ${activeSession.colorOutcomeLabel}"
                    } else null,
                    actionLabel = if (activeSession?.colorCompleted == true) strings.retakeTest else strings.startTest,
                    accentColor = MaterialTheme.colorScheme.tertiary,
                    testTag = "card_color_vision_start",
                    onClick = onOpenColorTest
                )
            }

            // Module 3: AI Eye Blink Detection
            item {
                ScreeningModuleCard(
                    icon = Icons.Rounded.RemoveRedEye,
                    title = strings.blinkCardTitle,
                    description = strings.blinkCardDesc,
                    completed = activeSession?.blinkCompleted == true,
                    resultSummary = if (activeSession?.blinkCompleted == true) {
                        "${activeSession.blinkCount} blinks (${activeSession.blinkDurationSec}s) • ~${activeSession.blinksPerMinute.roundToInt()} /min"
                    } else null,
                    actionLabel = if (activeSession?.blinkCompleted == true) strings.retakeTest else strings.startTest,
                    accentColor = MaterialTheme.colorScheme.secondary,
                    testTag = "card_blink_detection_start",
                    onClick = onOpenBlinkTest
                )
            }

            // Module 4: Eye Movement Tracking
            item {
                ScreeningModuleCard(
                    icon = Icons.Rounded.TrackChanges,
                    title = strings.movementCardTitle,
                    description = strings.movementCardDesc,
                    completed = activeSession?.movementCompleted == true,
                    resultSummary = if (activeSession?.movementCompleted == true) {
                        "${activeSession.movementTargetsTracked}/${activeSession.movementTargetsTotal} checkpoints tracked"
                    } else null,
                    actionLabel = if (activeSession?.movementCompleted == true) strings.retakeTest else strings.startTest,
                    accentColor = MaterialTheme.colorScheme.primary,
                    testTag = "card_eye_movement_start",
                    onClick = onOpenMovementTest
                )
            }

            // Module 5: AI Eye Screening Report
            item {
                ScreeningModuleCard(
                    icon = Icons.Rounded.Description,
                    title = strings.reportCardTitle,
                    description = strings.reportCardDesc,
                    completed = activeSession?.aiReportGenerated == true,
                    resultSummary = if (activeSession != null) {
                        "${activeSession.completedModuleCount}/4 ${strings.completedModulesLabel}"
                    } else null,
                    actionLabel = strings.viewReport,
                    accentColor = MaterialTheme.colorScheme.secondary,
                    highlighted = true,
                    testTag = "card_screening_report_open",
                    onClick = onOpenReport
                )
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun ScreeningModuleCard(
    icon: ImageVector,
    title: String,
    description: String,
    completed: Boolean,
    resultSummary: String?,
    actionLabel: String,
    accentColor: Color,
    testTag: String,
    highlighted: Boolean = false,
    onClick: () -> Unit
) {
    val containerColor = if (highlighted) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(
            width = if (highlighted) 1.5.dp else 1.dp,
            color = if (highlighted) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(Modifier.width(8.dp))
                CompletionStatusChip(completed = completed)
            }

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (resultSummary != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = resultSummary,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onClick,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (highlighted) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag(testTag)
                ) {
                    Text(actionLabel, style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
