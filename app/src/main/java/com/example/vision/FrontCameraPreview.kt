package com.example.vision

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.VideocamOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.util.concurrent.Executors

@Composable
fun FrontCameraPreview(
    metrics: EyeVisionMetrics,
    onMetricsUpdated: (EyeVisionMetrics) -> Unit,
    modifier: Modifier = Modifier,
    showOverlay: Boolean = true
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var cameraError by remember { mutableStateOf<String?>(null) }

    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
    val analyzer = remember {
        EyeVisionAnalyzer(
            onMetrics = { newMetrics -> onMetricsUpdated(newMetrics) },
            onError = { err -> Log.w("EyeVisionAnalyzer", err) }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            analyzer.close()
            analysisExecutor.shutdown()
        }
    }

    Box(
        modifier = modifier.background(Color(0xFF0B1622)),
        contentAlignment = Alignment.Center
    ) {
        if (cameraError != null) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Rounded.VideocamOff,
                    contentDescription = "Camera unavailable",
                    tint = Color(0xFFFF8A80),
                    modifier = Modifier.size(40.dp)
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = cameraError ?: "Camera unavailable",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    }

                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener(
                        {
                            try {
                                val cameraProvider = cameraProviderFuture.get()
                                val selector = when {
                                    cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) ->
                                        CameraSelector.DEFAULT_FRONT_CAMERA
                                    cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) ->
                                        CameraSelector.DEFAULT_BACK_CAMERA
                                    else -> {
                                        cameraError = "No front or rear camera was found on this device."
                                        return@addListener
                                    }
                                }

                                val preview = Preview.Builder().build().also {
                                    it.surfaceProvider = previewView.surfaceProvider
                                }

                                val imageAnalysis = ImageAnalysis.Builder()
                                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                    .build()
                                    .also { analysis ->
                                        analysis.setAnalyzer(analysisExecutor, analyzer)
                                    }

                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    selector,
                                    preview,
                                    imageAnalysis
                                )
                            } catch (e: Exception) {
                                Log.e("FrontCameraPreview", "Camera binding failed", e)
                                cameraError = "Unable to start front camera: ${e.localizedMessage ?: "hardware unavailable"}"
                            }
                        },
                        ContextCompat.getMainExecutor(ctx)
                    )
                    previewView
                }
            )

            if (showOverlay) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val box = metrics.faceBoxNormalized
                    if (metrics.faceDetected && box != null) {
                        val left = box.left * size.width
                        val top = box.top * size.height
                        val width = (box.right - box.left) * size.width
                        val height = (box.bottom - box.top) * size.height
                        drawRoundRect(
                            color = Color(0xFF4DD0E1).copy(alpha = 0.85f),
                            topLeft = Offset(left, top),
                            size = Size(width, height),
                            cornerRadius = CornerRadius(24f, 24f),
                            style = Stroke(width = 4f)
                        )
                    }

                    metrics.leftEyeNormalized?.let { pt ->
                        val center = Offset(pt.x * size.width, pt.y * size.height)
                        val eyeColor = if (metrics.leftEyeOpenProb < 0.30f) Color(0xFFFFB74D) else Color(0xFF69F0AE)
                        drawCircle(color = eyeColor, radius = 14f, center = center, style = Stroke(width = 4f))
                        drawCircle(color = eyeColor, radius = 5f, center = center)
                    }
                    metrics.rightEyeNormalized?.let { pt ->
                        val center = Offset(pt.x * size.width, pt.y * size.height)
                        val eyeColor = if (metrics.rightEyeOpenProb < 0.30f) Color(0xFFFFB74D) else Color(0xFF69F0AE)
                        drawCircle(color = eyeColor, radius = 14f, center = center, style = Stroke(width = 4f))
                        drawCircle(color = eyeColor, radius = 5f, center = center)
                    }
                }
            }
        }
    }
}
