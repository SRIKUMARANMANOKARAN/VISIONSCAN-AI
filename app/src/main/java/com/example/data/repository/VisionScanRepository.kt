package com.example.data.repository

import com.example.data.ai.AiReportRepository
import com.example.data.ai.AiReportResult
import com.example.data.local.ScreeningSessionDao
import com.example.data.local.ScreeningSessionEntity
import com.example.data.local.UserProfileDao
import com.example.data.local.UserProfileEntity
import com.example.data.preferences.PreferencesManager
import com.example.data.preferences.UserPreferences
import com.example.ui.i18n.AppLanguage
import kotlinx.coroutines.flow.Flow

class VisionScanRepository(
    private val userProfileDao: UserProfileDao,
    private val sessionDao: ScreeningSessionDao,
    private val preferencesManager: PreferencesManager,
    private val aiReportRepository: AiReportRepository
) {
    val preferencesFlow: Flow<UserPreferences> = preferencesManager.preferencesFlow
    val allProfilesFlow: Flow<List<UserProfileEntity>> = userProfileDao.observeAllProfiles()

    fun observeProfile(userId: Long): Flow<UserProfileEntity?> = userProfileDao.observeProfile(userId)

    fun observeSessionsForUser(userId: Long): Flow<List<ScreeningSessionEntity>> =
        sessionDao.observeSessionsForUser(userId)

    fun observeSession(sessionId: Long): Flow<ScreeningSessionEntity?> =
        sessionDao.observeSessionById(sessionId)

    suspend fun registerProfile(
        name: String,
        age: Int,
        email: String,
        wearsGlasses: Boolean
    ): Long {
        val profile = UserProfileEntity(
            name = name.trim(),
            age = age,
            email = email.trim(),
            wearsGlasses = wearsGlasses,
            isDemoMode = false
        )
        val userId = userProfileDao.insertProfile(profile)
        preferencesManager.setActiveUserId(userId)
        ensureActiveSession(userId)
        return userId
    }

    suspend fun enterDemoMode(): Long {
        val existingDemo = userProfileDao.getDemoProfile()
        val userId = if (existingDemo != null) {
            existingDemo.id
        } else {
            userProfileDao.insertProfile(
                UserProfileEntity(
                    name = "Demo User",
                    age = 28,
                    email = "demo@visionscan.local",
                    wearsGlasses = false,
                    isDemoMode = true
                )
            )
        }
        preferencesManager.setActiveUserId(userId)
        ensureActiveSession(userId)
        return userId
    }

    suspend fun selectExistingProfile(userId: Long) {
        preferencesManager.setActiveUserId(userId)
        ensureActiveSession(userId)
    }

    suspend fun updateProfile(profile: UserProfileEntity) {
        userProfileDao.updateProfile(profile)
    }

    suspend fun logout() {
        preferencesManager.setActiveUserId(-1L)
    }

    suspend fun ensureActiveSession(userId: Long): ScreeningSessionEntity {
        val latest = sessionDao.getLatestSession(userId)
        if (latest != null) return latest
        val newId = sessionDao.insertSession(ScreeningSessionEntity(userId = userId))
        return sessionDao.getSessionById(newId) ?: ScreeningSessionEntity(id = newId, userId = userId)
    }

    suspend fun startNewSession(userId: Long): Long {
        return sessionDao.insertSession(ScreeningSessionEntity(userId = userId))
    }

    suspend fun updateSession(session: ScreeningSessionEntity) {
        sessionDao.updateSession(session.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteSession(sessionId: Long) {
        sessionDao.deleteSessionById(sessionId)
    }

    suspend fun clearAllSessionsForUser(userId: Long) {
        sessionDao.deleteAllSessionsForUser(userId)
        sessionDao.insertSession(ScreeningSessionEntity(userId = userId))
    }

    suspend fun generateAiReport(
        profile: UserProfileEntity,
        session: ScreeningSessionEntity,
        language: AppLanguage
    ): Result<AiReportResult> {
        val result = aiReportRepository.generateScreeningReport(profile, session, language)
        result.onSuccess { report ->
            sessionDao.updateSession(
                session.copy(
                    aiReportGenerated = true,
                    aiSummary = report.summary,
                    aiObservations = report.observations,
                    aiRecommendations = report.recommendations,
                    aiIsPlaceholder = report.isPlaceholder,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
        return result
    }

    suspend fun setDarkMode(enabled: Boolean) = preferencesManager.setDarkMode(enabled)
    suspend fun setLanguage(language: AppLanguage) = preferencesManager.setLanguage(language)
    suspend fun setLargeTextMode(enabled: Boolean) = preferencesManager.setLargeTextMode(enabled)
    suspend fun setCameraConsent(granted: Boolean) = preferencesManager.setCameraConsent(granted)
    suspend fun setScreenBreakReminder(enabled: Boolean) = preferencesManager.setScreenBreakReminder(enabled)
}
