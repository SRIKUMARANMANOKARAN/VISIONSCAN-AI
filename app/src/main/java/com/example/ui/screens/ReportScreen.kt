package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.export.ExportedPdfInfo
import com.example.data.export.PdfReportExporter
import com.example.data.local.ScreeningSessionEntity
import com.example.data.local.UserProfileEntity
import com.example.ui.AiReportUiState
import com.example.ui.components.MedicalDisclaimerCard
import com.example.ui.components.formatScreeningDate
import com.example.ui.i18n.LocalStrings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    profile: UserProfileEntity,
    session: ScreeningSessionEntity?,
    aiState: AiReportUiState,
    onGenerateAiReport: (ScreeningSessionEntity) -> Unit,
    onBack: () -> Unit
) {
    val strings = LocalStrings.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var isExportingPdf by remember { mutableStateOf(false) }
    var lastExportedPdf by remember { mutableStateOf<ExportedPdfInfo?>(null) }

    fun exportPdf(shareImmediately: Boolean) {
        val active = session ?: return
        scope.launch {
            isExportingPdf = true
            val result = withContext(Dispatchers.IO) {
                PdfReportExporter.generateAndSavePdf(context, profile, active)
            }
            isExportingPdf = false
            result.onSuccess { pdfInfo ->
                lastExportedPdf = pdfInfo
                if (shareImmediately) {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, pdfInfo.shareUri)
                        putExtra(Intent.EXTRA_SUBJECT, "VisionScan AI Screening Report")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, strings.sharePdfButton))
                } else {
                    val locNote = if (pdfInfo.savedToDownloads) "Downloads/VisionScanAI/${pdfInfo.fileName}" else pdfInfo.fileName
                    snackbarHostState.showSnackbar("${strings.pdfSavedMessage} ($locNote)")
                }
            }.onFailure { err ->
                snackbarHostState.showSnackbar(err.localizedMessage ?: "PDF export failed.")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.reportTitle, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("report_back_button")) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 680.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (session == null || !session.hasAnyResult) {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(22.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = strings.reportTitle,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = strings.completeOneTestHint,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    // Patient & Session Overview Card
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = strings.patientDetails,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${profile.name} • ${profile.age} yrs" + if (profile.wearsGlasses) " • Eyewear" else "",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${strings.screeningDateLabel}: ${formatScreeningDate(session.updatedAt)}  •  ${session.completedModuleCount}/4 ${strings.completedModulesLabel}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Recorded Test Results Breakdown (Never fabricates missing modules)
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = strings.keyObservationsTitle,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )

                            ReportModuleRow(
                                title = strings.acuityCardTitle,
                                completed = session.acuityCompleted,
                                detail = if (session.acuityCompleted) {
                                    "OD (Right): Level ${session.rightEyeLevel}/6 (${session.rightEyeCorrect}/${session.rightEyeTotal}) • OS (Left): Level ${session.leftEyeLevel}/6 (${session.leftEyeCorrect}/${session.leftEyeTotal})"
                                } else strings.notCompletedLabel
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                            ReportModuleRow(
                                title = strings.colorCardTitle,
                                completed = session.colorCompleted,
                                detail = if (session.colorCompleted) {
                                    "${session.colorCorrect}/${session.colorTotal} plates correct • ${session.colorOutcomeLabel}"
                                } else strings.notCompletedLabel
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                            ReportModuleRow(
                                title = strings.blinkCardTitle,
                                completed = session.blinkCompleted,
                                detail = if (session.blinkCompleted) {
                                    "${session.blinkCount} blinks in ${session.blinkDurationSec}s (~${session.blinksPerMinute.roundToInt()} blinks/min est.)"
                                } else strings.notCompletedLabel
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                            ReportModuleRow(
                                title = strings.movementCardTitle,
                                completed = session.movementCompleted,
                                detail = if (session.movementCompleted) {
                                    "${session.movementTargetsTracked}/${session.movementTargetsTotal} checkpoints tracked (${(session.movementTrackingRatio * 100).roundToInt()}%)"
                                } else strings.notCompletedLabel
                            )
                        }
                    }

                    // Generate AI Explanation CTA
                    Button(
                        onClick = { onGenerateAiReport(session) },
                        enabled = aiState !is AiReportUiState.Loading,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("generate_ai_report_button")
                    ) {
                        if (aiState is AiReportUiState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(strings.generatingAiReport, style = MaterialTheme.typography.labelLarge)
                        } else {
                            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (session.aiReportGenerated) strings.regenerateAiReport else strings.generateAiReport,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }

                    if (aiState is AiReportUiState.Error) {
                        Text(
                            text = aiState.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    // AI Screening Explanation Output + Required Firebase Notice
                    if (session.aiReportGenerated) {
                        if (session.aiIsPlaceholder) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("ai_firebase_required_notice")
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Info,
                                        contentDescription = "Firebase AI Logic notice",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Text(
                                            text = strings.aiPlaceholderBannerTitle,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = strings.aiPlaceholderBannerBody,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }

                        Card(
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = strings.overallSummaryTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = session.aiSummary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                                Text(
                                    text = strings.keyObservationsTitle,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = session.aiObservations,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                                Text(
                                    text = strings.recommendationsTitle,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = session.aiRecommendations,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Download & Share PDF Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { exportPdf(shareImmediately = false) },
                            enabled = !isExportingPdf,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("export_pdf_button")
                        ) {
                            Icon(Icons.Rounded.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(strings.exportPdfButton, style = MaterialTheme.typography.labelLarge)
                        }

                        OutlinedButton(
                            onClick = { exportPdf(shareImmediately = true) },
                            enabled = !isExportingPdf,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("share_pdf_button")
                        ) {
                            Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(strings.sharePdfButton, style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    if (lastExportedPdf != null) {
                        Text(
                            text = "${strings.pdfSavedMessage} (${lastExportedPdf?.fileName})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                MedicalDisclaimerCard(text = strings.medicalDisclaimerFull)
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ReportModuleRow(
    title: String,
    completed: Boolean,
    detail: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = if (completed) Icons.Rounded.CheckCircle else Icons.Rounded.Schedule,
            contentDescription = null,
            tint = if (completed) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(20.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
