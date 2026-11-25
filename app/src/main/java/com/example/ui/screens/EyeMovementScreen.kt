package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.ui.components.MedicalDisclaimerCard
import com.example.ui.i18n.LocalStrings
import com.example.ui.theme.JetBrainsMono
import com.example.vision.EyeVisionMetrics
import com.example.vision.FrontCameraPreview
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.roundToInt

private enum class TargetDirection(
    val targetX: Float,
    val targetY: Float
) {
    CENTER(0.50f, 0.50f),
    LEFT(0.16f, 0.50f),
    RIGHT(0.84f, 0.50f),
    UP(0.50f, 0.18f),
    DOWN(0.50f, 0.82f),
    SWEEP_RIGHT(0.82f, 0.50f)
}

private data class CheckpointOutcome(
    val direction: TargetDirection,
    val tracked: Boolean,
    val faceVisible: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EyeMovementScreen(
    cameraConsentGranted: Boolean,
    onGrantConsent: () -> Unit,
    onBack: () -> Unit,
    onSaveResult: (
        targetsTracked: Int,
        targetsTotal: Int,
        trackingRatio: Float,
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

    var liveMetrics by remember { mutableStateOf(EyeVisionMetrics()) }
    var isRunning by remember { mutableStateOf(false) }
    var isCompleted by remember { mutableStateOf(false) }
    var stepIndex by remember { mutableIntStateOf(0) }

    // Calibrated baseline center (recorded during CENTER checkpoint)
    var baselineYaw by remember { mutableFloatStateOf(0f) }
    var baselinePitch by remember { mutableFloatStateOf(0f) }
    var baselineEyeMidX by remember { mutableFloatStateOf(0.5f) }
    var baselineEyeMidY by remember { mutableFloatStateOf(0.5f) }

    val checkpoints = remember { TargetDirection.entries }
    val checkpointOutcomes = remember { mutableStateListOf<CheckpointOutcome>() }

    // Current checkpoint tracking accumulator
    var currentStepFaceFrames by remember { mutableIntStateOf(0) }
    var currentStepMatchedFrames by remember { mutableIntStateOf(0) }

    val currentDirection = checkpoints[stepIndex.coerceIn(0, checkpoints.lastIndex)]
    val animatedTargetX by animateFloatAsState(
        targetValue = currentDirection.targetX,
        animationSpec = tween(durationMillis = 650),
        label = "target_x"
    )
    val animatedTargetY by animateFloatAsState(
        targetValue = currentDirection.targetY,
        animationSpec = tween(durationMillis = 650),
        label = "target_y"
    )

    // Evaluate live eye/face landmark alignment on each incoming camera frame
    LaunchedEffect(liveMetrics.timestampMs, isRunning, stepIndex) {
        if (!isRunning || !liveMetrics.faceDetected) return@LaunchedEffect
        currentStepFaceFrames += 1

        val left = liveMetrics.leftEyeNormalized
        val right = liveMetrics.rightEyeNormalized
        val midX = if (left != null && right != null) (left.x + right.x) / 2f else 0.5f
        val midY = if (left != null && right != null) (left.y + right.y) / 2f else 0.5f

        val deltaYaw = liveMetrics.headYawDegrees - baselineYaw
        val deltaPitch = liveMetrics.headPitchDegrees - baselinePitch
        val deltaX = midX - baselineEyeMidX
        val deltaY = midY - baselineEyeMidY

        val matched = when (currentDirection) {
            TargetDirection.CENTER -> {
                // Establish rolling center calibration
                baselineYaw = liveMetrics.headYawDegrees
                baselinePitch = liveMetrics.headPitchDegrees
                baselineEyeMidX = midX
                baselineEyeMidY = midY
                true
            }
            TargetDirection.LEFT -> deltaYaw > 4.5f || deltaX < -0.025f
            TargetDirection.RIGHT, TargetDirection.SWEEP_RIGHT -> deltaYaw < -4.5f || deltaX > 0.025f
            TargetDirection.UP -> deltaPitch > 3.5f || deltaY < -0.020f
            TargetDirection.DOWN -> deltaPitch < -3.5f || deltaY > 0.020f
        }
        if (matched) currentStepMatchedFrames += 1
    }

    // Step progression loop (3.5s per target checkpoint)
    LaunchedEffect(isRunning) {
        if (!isRunning) return@LaunchedEffect
        checkpointOutcomes.clear()
        for (idx in checkpoints.indices) {
            stepIndex = idx
            currentStepFaceFrames = 0
            currentStepMatchedFrames = 0
            delay(3_500L)
            if (!isRunning) return@LaunchedEffect
            val faceSeen = currentStepFaceFrames >= 3
            val tracked = faceSeen && (currentStepMatchedFrames.toFloat() / currentStepFaceFrames.coerceAtLeast(1) >= 0.25f)
            checkpointOutcomes += CheckpointOutcome(
                direction = checkpoints[idx],
                tracked = tracked,
                faceVisible = faceSeen
            )
        }
        isRunning = false
        isCompleted = true
    }

    // Visual indicator of estimated gaze/head point on the tracking stage
    val estimatedGazeX = remember(liveMetrics, baselineYaw, baselineEyeMidX) {
        if (!liveMetrics.faceDetected) 0.5f
        else {
            val yawOffset = -(liveMetrics.headYawDegrees - baselineYaw) / 35f
            val leftEye = liveMetrics.leftEyeNormalized
            val rightEye = liveMetrics.rightEyeNormalized
            val midX = if (leftEye != null && rightEye != null) {
                (leftEye.x + rightEye.x) / 2f
            } else 0.5f
            val posOffset = (midX - baselineEyeMidX) * 2.4f
            (0.5f + yawOffset + posOffset).coerceIn(0.08f, 0.92f)
        }
    }
    val estimatedGazeY = remember(liveMetrics, baselinePitch, baselineEyeMidY) {
        if (!liveMetrics.faceDetected) 0.5f
        else {
            val pitchOffset = -(liveMetrics.headPitchDegrees - baselinePitch) / 28f
            val leftEye = liveMetrics.leftEyeNormalized
            val rightEye = liveMetrics.rightEyeNormalized
            val midY = if (leftEye != null && rightEye != null) {
                (leftEye.y + rightEye.y) / 2f
            } else 0.5f
            val posOffset = (midY - baselineEyeMidY) * 2.4f
            (0.5f + pitchOffset + posOffset).coerceIn(0.08f, 0.92f)
        }
    }

    fun directionLabel(dir: TargetDirection): String = when (dir) {
        TargetDirection.CENTER -> strings.targetCenter
        TargetDirection.LEFT -> strings.targetLeft
        TargetDirection.RIGHT -> strings.targetRight
        TargetDirection.UP -> strings.targetUp
        TargetDirection.DOWN -> strings.targetDown
        TargetDirection.SWEEP_RIGHT -> strings.targetSweep
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.movementTitle, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("movement_back_button")) {
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
                                Icon(Icons.Rounded.CameraFront, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
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
                                    .testTag("movement_request_camera_permission")
                            ) {
                                Text(strings.grantCameraPermission, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                } else if (isCompleted) {
                    val trackedCount = checkpointOutcomes.count { it.tracked }
                    val total = checkpoints.size
                    val ratio = trackedCount.toFloat() / total.coerceAtLeast(1)
                    val anyFaceVisible = checkpointOutcomes.any { it.faceVisible }
                    val observationLabel = when {
                        !anyFaceVisible -> "No face detected during tracking session — retry in brighter lighting"
                        ratio >= 0.75f -> "Consistent target-following movement observed ($trackedCount/$total checkpoints)"
                        ratio >= 0.45f -> "Partial target-following observed ($trackedCount/$total checkpoints) — ensure steady lighting"
                        else -> "Limited tracking alignment observed ($trackedCount/$total checkpoints)"
                    }

                    Card(
                        shape = RoundedCornerShape(24.dp),
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
                                    Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Text(
                                    text = strings.movementResultTitle,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = "$trackedCount / $total (${(ratio * 100).roundToInt()}%)",
                                style = MaterialTheme.typography.displaySmall.copy(fontFamily = JetBrainsMono),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = observationLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                checkpointOutcomes.forEach { item ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = directionLabel(item.direction),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Text(
                                            text = when {
                                                !item.faceVisible -> "No face detected"
                                                item.tracked -> "Tracked"
                                                else -> "Not aligned"
                                            },
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (item.tracked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    MedicalDisclaimerCard(text = strings.movementDisclaimer)

                    Button(
                        onClick = {
                            onSaveResult(trackedCount, total, ratio, observationLabel)
                            onBack()
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("movement_save_result_button")
                    ) {
                        Text(strings.saveAndReturn, style = MaterialTheme.typography.labelLarge)
                    }
                } else {
                    // Compact Front Camera Feed + Landmark Telemetry
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .width(126.dp)
                                .height(136.dp)
                        ) {
                            FrontCameraPreview(
                                metrics = liveMetrics,
                                onMetricsUpdated = { liveMetrics = it },
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (liveMetrics.faceDetected) MaterialTheme.colorScheme.secondaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = if (liveMetrics.faceDetected) strings.faceDetectedLabel else strings.noFaceDetectedLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (liveMetrics.faceDetected) MaterialTheme.colorScheme.onSecondaryContainer
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                            Text(
                                text = if (isRunning) directionLabel(currentDirection) else strings.followTargetPrompt,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Yaw: ${liveMetrics.headYawDegrees.roundToInt()}°  •  Pitch: ${liveMetrics.headPitchDegrees.roundToInt()}°",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (isRunning) {
                        LinearProgressIndicator(
                            progress = { (stepIndex + 1).toFloat() / checkpoints.size },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(50))
                        )
                    }

                    // Smooth Pursuit Target Stage Canvas
                    val primaryColor = MaterialTheme.colorScheme.primary
                    val tealColor = MaterialTheme.colorScheme.secondary
                    val gridColor = MaterialTheme.colorScheme.outlineVariant
                    val isNearTarget = abs(estimatedGazeX - animatedTargetX) < 0.22f &&
                        abs(estimatedGazeY - animatedTargetY) < 0.22f &&
                        liveMetrics.faceDetected

                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(265.dp)
                                .background(MaterialTheme.colorScheme.surface)
                                .testTag("eye_movement_stage")
                        ) {
                            // Subtle optical crosshair guides
                            drawLine(
                                color = gridColor,
                                start = Offset(size.width / 2f, 24f),
                                end = Offset(size.width / 2f, size.height - 24f),
                                strokeWidth = 2f
                            )
                            drawLine(
                                color = gridColor,
                                start = Offset(24f, size.height / 2f),
                                end = Offset(size.width - 24f, size.height / 2f),
                                strokeWidth = 2f
                            )

                            // Moving On-Screen Target
                            val targetCenter = Offset(animatedTargetX * size.width, animatedTargetY * size.height)
                            drawCircle(
                                color = primaryColor.copy(alpha = 0.16f),
                                radius = 34.dp.toPx(),
                                center = targetCenter
                            )
                            drawCircle(
                                color = primaryColor,
                                radius = 16.dp.toPx(),
                                center = targetCenter
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 5.dp.toPx(),
                                center = targetCenter
                            )

                            // Live Eye/Gaze Tracking Reticle
                            if (liveMetrics.faceDetected) {
                                val gazeCenter = Offset(estimatedGazeX * size.width, estimatedGazeY * size.height)
                                val reticleColor = if (isNearTarget) tealColor else Color(0xFFFFA726)
                                drawCircle(
                                    color = reticleColor,
                                    radius = 22.dp.toPx(),
                                    center = gazeCenter,
                                    style = Stroke(width = 4f)
                                )
                                drawCircle(
                                    color = reticleColor,
                                    radius = 5.dp.toPx(),
                                    center = gazeCenter
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (!isRunning) {
                                stepIndex = 0
                                isRunning = true
                            } else {
                                isRunning = false
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("movement_start_button")
                    ) {
                        Icon(
                            imageVector = if (isRunning) Icons.Rounded.TrackChanges else Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isRunning) "${strings.targetsTracked}: ${stepIndex + 1}/${checkpoints.size}" else strings.startTrackingTest,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    MedicalDisclaimerCard(text = strings.movementDisclaimer)
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
