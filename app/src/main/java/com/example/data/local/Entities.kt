package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val age: Int,
    val email: String = "",
    val wearsGlasses: Boolean = false,
    val isDemoMode: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "screening_sessions")
data class ScreeningSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val userId: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),

    // Visual Acuity Module
    val acuityCompleted: Boolean = false,
    val rightEyeLevel: Int = 0,
    val rightEyeCorrect: Int = 0,
    val rightEyeTotal: Int = 0,
    val leftEyeLevel: Int = 0,
    val leftEyeCorrect: Int = 0,
    val leftEyeTotal: Int = 0,
    val calibrationScale: Float = 1.0f,
    val acuityOutcomeLabel: String = "",

    // Color Vision Module
    val colorCompleted: Boolean = false,
    val colorCorrect: Int = 0,
    val colorTotal: Int = 0,
    val colorPlateDetails: String = "",
    val colorOutcomeLabel: String = "",

    // Blink Detection Module
    val blinkCompleted: Boolean = false,
    val blinkCount: Int = 0,
    val blinkDurationSec: Int = 0,
    val blinksPerMinute: Float = 0f,
    val avgEyeOpenProb: Float = 0f,
    val blinkObservationLabel: String = "",

    // Eye Movement Tracking Module
    val movementCompleted: Boolean = false,
    val movementTargetsTracked: Int = 0,
    val movementTargetsTotal: Int = 0,
    val movementTrackingRatio: Float = 0f,
    val movementObservationLabel: String = "",

    // Consolidated AI Report
    val aiReportGenerated: Boolean = false,
    val aiSummary: String = "",
    val aiObservations: String = "",
    val aiRecommendations: String = "",
    val aiIsPlaceholder: Boolean = true
) {
    val completedModuleCount: Int
        get() = listOf(acuityCompleted, colorCompleted, blinkCompleted, movementCompleted).count { it }

    val hasAnyResult: Boolean
        get() = completedModuleCount > 0
}
