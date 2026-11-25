package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CameraFront
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RemoveRedEye
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.ui.components.MedicalDisclaimerCard
import com.example.ui.i18n.LocalStrings
import com.example.ui.theme.JetBrainsMono
import com.example.vision.BlinkDetectorStateMachine
import com.example.vision.EyeVisionMetrics
import com.example.vision.FrontCameraPreview
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlinkDetectionScreen(
    cameraConsentGranted: Boolean,
    onGrantConsent: () -> Unit,
    onBack: () -> Unit,
    onSaveResult: (
        blinkCount: Int,
        durationSec: Int,
        blinksPerMinute: Float,
        avgEyeOpenProb: Float,
        observationLabel: String
    ) -> Unit
) {
    val strings = LocalStrings.current
    val context = LocalContext.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var permissionDeniedOnce by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (!granted) permissionDeniedOnce = true
    }

    var selectedDurationSec by remember { mutableIntStateOf(30) }
    var isMonitoring by remember { mutableStateOf(false) }
    var sessionCompleted by remember { mutableStateOf(false) }
    var elapsedSec by remember { mutableIntStateOf(0) }
    var blinkCount by remember { mutableIntStateOf(0) }
    var liveMetrics by remember { mutableStateOf(EyeVisionMetrics()) }
    var finalAvgOpenProb by remember { mutableFloatStateOf(0f) }

    val blinkDetector = remember { BlinkDetectorStateMachine() }
    val opennessHistory = remember { mutableStateListOf<Float>() }

    // Session countdown loop
    LaunchedEffect(isMonitoring, selectedDurationSec) {
        if (isMonitoring) {
            elapsedSec = 0
            while (isMonitoring && elapsedSec < selectedDurationSec) {
                delay(1_000L)
                elapsedSec += 1
            }
            if (isMonitoring) {
                isMonitoring = false
                finalAvgOpenProb = blinkDetector.averageOpenness
                sessionCompleted = true
            }
        }
    }

    val estimatedBpm = remember(blinkCount, elapsedSec) {
        if (elapsedSec <= 0) 0f else (blinkCount.toFloat() / elapsedSec.toFloat()) * 60f
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.blinkTitle, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("blink_back_button")) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 660.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Step 1: Privacy Consent Gate
                if (!cameraConsentGranted) {
                    CameraPrivacyConsentCard(
                        title = strings.cameraConsentTitle,
                        body = strings.cameraConsentBody,
                        buttonLabel = strings.agreeAndEnableCamera,
                        onAgree = {
                            onGrantConsent()
                            if (!hasCameraPermission) {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }
                    )
                } else if (!hasCameraPermission) {
                    // Step 2: Runtime Camera Permission Gate
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.CameraFront,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Text(
                                    text = strings.grantCameraPermission,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = strings.cameraPermissionDenied,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (permissionDeniedOnce) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(
                                onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("blink_request_camera_permission")
                            ) {
                                Text(strings.grantCameraPermission, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                } else if (sessionCompleted) {
                    // Step 4: Completed Summary
                    val safeDuration = elapsedSec.coerceAtLeast(1)
                    val finalBpm = (blinkCount.toFloat() / safeDuration) * 60f
                    val observationLabel =
                        "$blinkCount blinks observed in ${safeDuration}s (~${finalBpm.roundToInt()} blinks/min estimated)"

                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Text(
                                    text = strings.blinkResultTitle,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                BlinkStatCard(
                                    label = strings.blinksCounted,
                                    value = "$blinkCount",
                                    modifier = Modifier.weight(1f)
                                )
                                BlinkStatCard(
                                    label = strings.estimatedBpm,
                                    value = "${finalBpm.roundToInt()}",
                                    modifier = Modifier.weight(1f)
                                )
                                BlinkStatCard(
                                    label = strings.eyeOpennessRatio,
                                    value = "${(finalAvgOpenProb * 100).roundToInt()}%",
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Text(
                                text = observationLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    MedicalDisclaimerCard(text = strings.blinkNeutralNote)

                    Button(
                        onClick = {
                            onSaveResult(
                                blinkCount,
                                safeDuration,
                                finalBpm,
                                finalAvgOpenProb,
                                observationLabel
                            )
                            onBack()
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("blink_save_result_button")
                    ) {
                        Text(strings.saveAndReturn, style = MaterialTheme.typography.labelLarge)
                    }
                } else {
                    // Step 3: Live Front Camera Blink Monitoring Dashboard
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(290.dp)
                                .clip(RoundedCornerShape(24.dp))
                        ) {
                            FrontCameraPreview(
                                metrics = liveMetrics,
                                onMetricsUpdated = { metrics ->
                                    liveMetrics = metrics
                                    if (metrics.faceDetected) {
                                        opennessHistory.add(metrics.avgEyeOpenProb)
                                        if (opennessHistory.size > 48) {
                                            opennessHistory.removeAt(0)
                                        }
                                    }
                                    if (isMonitoring) {
                                        if (blinkDetector.onFrame(metrics)) {
                                            blinkCount = blinkDetector.blinkCount
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )

                            // Top live status pill
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (liveMetrics.faceDetected) Color(0xCC0F8A5F) else Color(0xCC37474F),
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (liveMetrics.faceDetected) Color(0xFF69F0AE) else Color(0xFFFFCA80))
                                    )
                                    Text(
                                        text = if (liveMetrics.faceDetected) strings.faceDetectedLabel else strings.noFaceDetectedLabel,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    // Real-time Telemetry Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        BlinkStatCard(
                            label = strings.blinksCounted,
                            value = "$blinkCount",
                            modifier = Modifier.weight(1f)
                        )
                        BlinkStatCard(
                            label = strings.estimatedBpm,
                            value = "${estimatedBpm.roundToInt()}",
                            modifier = Modifier.weight(1f)
                        )
                        BlinkStatCard(
                            label = strings.timeRemaining,
                            value = "${(selectedDurationSec - elapsedSec).coerceAtLeast(0)}s",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Live Eye Openness Ratio Bars & Waveform
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
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = strings.eyeOpennessRatio,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "L: ${(liveMetrics.leftEyeOpenProb * 100).roundToInt()}%  •  R: ${(liveMetrics.rightEyeOpenProb * 100).roundToInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            LinearProgressIndicator(
                                progress = { liveMetrics.avgEyeOpenProb.coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(50))
                            )

                            val waveColor = MaterialTheme.colorScheme.secondary
                            val thresholdColor = MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(68.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                            ) {
                                // Blink threshold guide line at 0.30
                                val threshY = size.height * (1f - 0.30f)
                                drawLine(
                                    color = thresholdColor,
                                    start = Offset(0f, threshY),
                                    end = Offset(size.width, threshY),
                                    strokeWidth = 2f
                                )
                                if (opennessHistory.size >= 2) {
                                    val path = Path()
                                    val stepX = size.width / (opennessHistory.size - 1).coerceAtLeast(1)
                                    opennessHistory.forEachIndexed { idx, value ->
                                        val x = idx * stepX
                                        val y = size.height * (1f - value.coerceIn(0f, 1f))
                                        if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
                                    }
                                    drawPath(
                                        path = path,
                                        color = waveColor,
                                        style = Stroke(width = 4f, cap = StrokeCap.Round)
                                    )
                                }
                            }
                        }
                    }

                    // Duration Selector (when idle)
                    if (!isMonitoring) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = strings.durationLabel,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            listOf(30, 60).forEach { sec ->
                                FilterChip(
                                    selected = selectedDurationSec == sec,
                                    onClick = { selectedDurationSec = sec },
                                    label = { Text("${sec}s") }
                                )
                            }
                        }
                    }

                    // Primary Start / Finish CTA
                    Button(
                        onClick = {
                            if (!isMonitoring) {
                                blinkDetector.reset()
                                blinkCount = 0
                                elapsedSec = 0
                                isMonitoring = true
                            } else {
                                isMonitoring = false
                                finalAvgOpenProb = blinkDetector.averageOpenness
                                sessionCompleted = true
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isMonitoring) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("blink_toggle_monitoring_button")
                    ) {
                        Icon(
                            imageVector = if (isMonitoring) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isMonitoring) strings.stopSession else strings.startBlinkSession,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    MedicalDisclaimerCard(text = strings.blinkNeutralNote)
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
internal fun CameraPrivacyConsentCard(
    title: String,
    body: String,
    buttonLabel: String,
    onAgree: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onAgree,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("camera_consent_agree_button")
            ) {
                Icon(Icons.Rounded.RemoveRedEye, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(buttonLabel, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun BlinkStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(fontFamily = JetBrainsMono),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}
