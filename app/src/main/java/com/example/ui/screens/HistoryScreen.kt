package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CompareArrows
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.ScreeningSessionEntity
import com.example.ui.components.ScreeningHistoryBarChart
import com.example.ui.components.formatScreeningDate
import com.example.ui.i18n.LocalStrings
import com.example.ui.theme.JetBrainsMono
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HistoryScreen(
    sessions: List<ScreeningSessionEntity>,
    onOpenReport: (Long) -> Unit,
    onDeleteSession: (Long) -> Unit,
    onClearAllHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val recordedSessions = remember(sessions) { sessions.filter { it.hasAnyResult } }
    var showClearAllDialog by remember { mutableStateOf(false) }

    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            title = { Text(strings.confirmDeleteTitle, fontWeight = FontWeight.Bold) },
            text = { Text(strings.confirmDeleteAllBody) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearAllDialog = false
                        onClearAllHistory()
                    },
                    modifier = Modifier.testTag("confirm_clear_history_button")
                ) {
                    Text(strings.delete, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 700.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.historyTitle,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (recordedSessions.isNotEmpty()) {
                        OutlinedButton(
                            onClick = { showClearAllDialog = true },
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.testTag("clear_all_history_button")
                        ) {
                            Icon(Icons.Rounded.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(strings.clearAllHistory, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            if (recordedSessions.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(44.dp)
                            )
                            Text(
                                text = strings.emptyHistoryTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = strings.emptyHistoryBody,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                // Progress Comparison Card (Latest vs Previous Session)
                if (recordedSessions.size >= 2) {
                    item {
                        val latest = recordedSessions[0]
                        val previous = recordedSessions[1]
                        Card(
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f)
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.CompareArrows,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Column {
                                        Text(
                                            text = strings.progressComparisonTitle,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = strings.latestVsPrevious,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                ComparisonRow(
                                    label = strings.acuityCardTitle,
                                    previousVal = if (previous.acuityCompleted) "R:L${previous.rightEyeLevel} L:L${previous.leftEyeLevel}" else "—",
                                    latestVal = if (latest.acuityCompleted) "R:L${latest.rightEyeLevel} L:L${latest.leftEyeLevel}" else "—"
                                )
                                ComparisonRow(
                                    label = strings.colorCardTitle,
                                    previousVal = if (previous.colorCompleted) "${previous.colorCorrect}/${previous.colorTotal}" else "—",
                                    latestVal = if (latest.colorCompleted) "${latest.colorCorrect}/${latest.colorTotal}" else "—"
                                )
                                ComparisonRow(
                                    label = strings.blinkCardTitle,
                                    previousVal = if (previous.blinkCompleted) "${previous.blinksPerMinute.roundToInt()}/min" else "—",
                                    latestVal = if (latest.blinkCompleted) "${latest.blinksPerMinute.roundToInt()}/min" else "—"
                                )
                                ComparisonRow(
                                    label = strings.movementCardTitle,
                                    previousVal = if (previous.movementCompleted) "${previous.movementTargetsTracked}/${previous.movementTargetsTotal}" else "—",
                                    latestVal = if (latest.movementCompleted) "${latest.movementTargetsTracked}/${latest.movementTargetsTotal}" else "—"
                                )
                            }
                        }
                    }
                }

                // Chart Overview
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = strings.completedModulesLabel,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            ScreeningHistoryBarChart(sessions = recordedSessions)
                        }
                    }
                }

                // Session Timeline Cards
                items(recordedSessions, key = { it.id }) { session ->
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = formatScreeningDate(session.updatedAt),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${session.completedModuleCount} / 4 ${strings.completedModulesLabel}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = { onDeleteSession(session.id) },
                                    modifier = Modifier.testTag("delete_session_${session.id}")
                                ) {
                                    Icon(
                                        Icons.Rounded.DeleteOutline,
                                        contentDescription = strings.deleteSession,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (session.acuityCompleted) {
                                    HistoryChip("Acuity R:L${session.rightEyeLevel} L:L${session.leftEyeLevel}")
                                }
                                if (session.colorCompleted) {
                                    HistoryChip("Color ${session.colorCorrect}/${session.colorTotal}")
                                }
                                if (session.blinkCompleted) {
                                    HistoryChip("Blink ~${session.blinksPerMinute.roundToInt()}/m")
                                }
                                if (session.movementCompleted) {
                                    HistoryChip("Tracking ${session.movementTargetsTracked}/${session.movementTargetsTotal}")
                                }
                            }

                            Button(
                                onClick = { onOpenReport(session.id) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .testTag("open_history_report_${session.id}")
                            ) {
                                Icon(Icons.Rounded.Description, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(strings.viewReport, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun ComparisonRow(
    label: String,
    previousVal: String,
    latestVal: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "$previousVal  →  $latestVal",
            style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMono),
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun HistoryChip(text: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}
