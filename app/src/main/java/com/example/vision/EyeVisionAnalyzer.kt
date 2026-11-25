package com.example.vision

import android.graphics.PointF
import android.graphics.RectF
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark

data class EyeVisionMetrics(
    val faceDetected: Boolean = false,
    val leftEyeOpenProb: Float = 0f,
    val rightEyeOpenProb: Float = 0f,
    val avgEyeOpenProb: Float = 0f,
    val leftEyeNormalized: PointF? = null,
    val rightEyeNormalized: PointF? = null,
    val faceBoxNormalized: RectF? = null,
    val headYawDegrees: Float = 0f,
    val headPitchDegrees: Float = 0f,
    val timestampMs: Long = System.currentTimeMillis()
)

/**
 * Tracks real blink transitions using eye openness probabilities from ML Kit Face Detection.
 * A blink is recorded when average eye openness drops below [closeThreshold] and then recovers
 * above [reopenThreshold] within a physiological blink window (50ms - 1200ms).
 */
class BlinkDetectorStateMachine(
    private val closeThreshold: Float = 0.30f,
    private val reopenThreshold: Float = 0.52f,
    private val minBlinkMs: Long = 50L,
    private val maxBlinkMs: Long = 1200L,
    private val minInterBlinkMs: Long = 180L
) {
    private var eyesClosed = false
    private var closeStartedAtMs = 0L
    private var lastBlinkAtMs = 0L

    var blinkCount: Int = 0
        private set

    private var opennessSum = 0f
    private var opennessSamples = 0

    val averageOpenness: Float
        get() = if (opennessSamples == 0) 0f else opennessSum / opennessSamples

    fun reset() {
        eyesClosed = false
        closeStartedAtMs = 0L
        lastBlinkAtMs = 0L
        blinkCount = 0
        opennessSum = 0f
        opennessSamples = 0
    }

    /**
     * Feeds a new frame's eye openness reading and returns true if a complete blink just occurred.
     */
    fun onFrame(metrics: EyeVisionMetrics): Boolean {
        if (!metrics.faceDetected) {
            eyesClosed = false
            return false
        }

        val openness = metrics.avgEyeOpenProb
        opennessSum += openness
        opennessSamples += 1

        val now = metrics.timestampMs
        if (!eyesClosed && openness < closeThreshold) {
            eyesClosed = true
            closeStartedAtMs = now
            return false
        }

        if (eyesClosed && openness > reopenThreshold) {
            eyesClosed = false
            val duration = now - closeStartedAtMs
            if (duration in minBlinkMs..maxBlinkMs && (now - lastBlinkAtMs) >= minInterBlinkMs) {
                blinkCount += 1
                lastBlinkAtMs = now
                return true
            }
        }
        return false
    }
}

class EyeVisionAnalyzer(
    private val onMetrics: (EyeVisionMetrics) -> Unit,
    private val onError: (String) -> Unit = {}
) : ImageAnalysis.Analyzer {

    private val detector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .enableTracking()
            .build()
    )

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val rotation = imageProxy.imageInfo.rotationDegrees
        val inputImage = InputImage.fromMediaImage(mediaImage, rotation)
        val rotatedWidth = if (rotation == 90 || rotation == 270) imageProxy.height else imageProxy.width
        val rotatedHeight = if (rotation == 90 || rotation == 270) imageProxy.width else imageProxy.height

        detector.process(inputImage)
            .addOnSuccessListener { faces ->
                val face = faces.maxByOrNull { it.boundingBox.width() * it.boundingBox.height() }
                if (face == null) {
                    onMetrics(EyeVisionMetrics(faceDetected = false))
                } else {
                    onMetrics(extractMetrics(face, rotatedWidth.toFloat(), rotatedHeight.toFloat()))
                }
            }
            .addOnFailureListener { exc ->
                onError(exc.localizedMessage ?: "Face landmark analysis error")
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    private fun extractMetrics(face: Face, frameW: Float, frameH: Float): EyeVisionMetrics {
        val safeW = frameW.coerceAtLeast(1f)
        val safeH = frameH.coerceAtLeast(1f)

        val leftProb = face.leftEyeOpenProbability ?: 0.5f
        val rightProb = face.rightEyeOpenProbability ?: 0.5f
        val avgProb = ((leftProb + rightProb) / 2f).coerceIn(0f, 1f)

        // Front camera preview is mirrored horizontally -> mirror X
        fun mirrorPoint(pt: PointF?): PointF? = pt?.let {
            PointF((1f - (it.x / safeW)).coerceIn(0f, 1f), (it.y / safeH).coerceIn(0f, 1f))
        }

        val leftEyePt = mirrorPoint(face.getLandmark(FaceLandmark.LEFT_EYE)?.position)
        val rightEyePt = mirrorPoint(face.getLandmark(FaceLandmark.RIGHT_EYE)?.position)

        val box = face.boundingBox
        val mirroredLeft = (1f - (box.right / safeW)).coerceIn(0f, 1f)
        val mirroredRight = (1f - (box.left / safeW)).coerceIn(0f, 1f)
        val normBox = RectF(
            minOf(mirroredLeft, mirroredRight),
            (box.top / safeH).coerceIn(0f, 1f),
            maxOf(mirroredLeft, mirroredRight),
            (box.bottom / safeH).coerceIn(0f, 1f)
        )

        return EyeVisionMetrics(
            faceDetected = true,
            leftEyeOpenProb = leftProb.coerceIn(0f, 1f),
            rightEyeOpenProb = rightProb.coerceIn(0f, 1f),
            avgEyeOpenProb = avgProb,
            leftEyeNormalized = leftEyePt,
            rightEyeNormalized = rightEyePt,
            faceBoxNormalized = normBox,
            headYawDegrees = face.headEulerAngleY,
            headPitchDegrees = face.headEulerAngleX,
            timestampMs = System.currentTimeMillis()
        )
    }

    fun close() {
        detector.close()
    }
}
