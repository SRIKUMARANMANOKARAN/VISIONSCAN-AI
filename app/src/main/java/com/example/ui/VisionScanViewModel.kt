package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.ScreeningSessionEntity
import com.example.data.local.UserProfileEntity
import com.example.data.preferences.UserPreferences
import com.example.data.repository.VisionScanRepository
import com.example.ui.i18n.AppLanguage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface AiReportUiState {
    data object Idle : AiReportUiState
    data object Loading : AiReportUiState
    data object Success : AiReportUiState
    data class Error(val message: String) : AiReportUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
class VisionScanViewModel(
    private val repository: VisionScanRepository
) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = repository.preferencesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UserPreferences()
    )

    val allProfiles: StateFlow<List<UserProfileEntity>> = repository.allProfilesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    val activeProfile: StateFlow<UserProfileEntity?> = preferences
        .flatMapLatest { prefs ->
            if (prefs.activeUserId > 0L) repository.observeProfile(prefs.activeUserId)
            else flowOf(null)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    val screeningHistory: StateFlow<List<ScreeningSessionEntity>> = preferences
        .flatMapLatest { prefs ->
            if (prefs.activeUserId > 0L) repository.observeSessionsForUser(prefs.activeUserId)
            else flowOf(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _selectedReportSessionId = MutableStateFlow<Long?>(null)
    val selectedReportSessionId: StateFlow<Long?> = _selectedReportSessionId.asStateFlow()

    private val _aiReportState = MutableStateFlow<AiReportUiState>(AiReportUiState.Idle)
    val aiReportState: StateFlow<AiReportUiState> = _aiReportState.asStateFlow()

    fun currentSessionOrNull(): ScreeningSessionEntity? = screeningHistory.value.firstOrNull()

    fun registerProfile(name: String, age: Int, email: String, wearsGlasses: Boolean) {
        viewModelScope.launch {
            repository.registerProfile(name, age, email, wearsGlasses)
        }
    }

    fun enterDemoMode() {
        viewModelScope.launch {
            repository.enterDemoMode()
        }
    }

    fun selectProfile(userId: Long) {
        viewModelScope.launch {
            repository.selectExistingProfile(userId)
        }
    }

    fun updateProfile(name: String, age: Int, email: String, wearsGlasses: Boolean) {
        val current = activeProfile.value ?: return
        viewModelScope.launch {
            repository.updateProfile(
                current.copy(
                    name = name.trim(),
                    age = age,
                    email = email.trim(),
                    wearsGlasses = wearsGlasses
                )
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            _selectedReportSessionId.value = null
            _aiReportState.value = AiReportUiState.Idle
            repository.logout()
        }
    }

    fun startNewSession() {
        val uid = activeProfile.value?.id ?: return
        viewModelScope.launch {
            val newId = repository.startNewSession(uid)
            _selectedReportSessionId.value = newId
            _aiReportState.value = AiReportUiState.Idle
        }
    }

    fun openReportForSession(sessionId: Long?) {
        _selectedReportSessionId.value = sessionId
        _aiReportState.value = AiReportUiState.Idle
    }

    fun saveVisualAcuityResult(
        rightLevel: Int,
        rightCorrect: Int,
        rightTotal: Int,
        leftLevel: Int,
        leftCorrect: Int,
        leftTotal: Int,
        calibrationScale: Float,
        outcomeLabel: String
    ) {
        val uid = activeProfile.value?.id ?: return
        viewModelScope.launch {
            val session = repository.ensureActiveSession(uid)
            repository.updateSession(
                session.copy(
                    acuityCompleted = true,
                    rightEyeLevel = rightLevel,
                    rightEyeCorrect = rightCorrect,
                    rightEyeTotal = rightTotal,
                    leftEyeLevel = leftLevel,
                    leftEyeCorrect = leftCorrect,
                    leftEyeTotal = leftTotal,
                    calibrationScale = calibrationScale,
                    acuityOutcomeLabel = outcomeLabel,
                    aiReportGenerated = false
                )
            )
        }
    }

    fun saveColorVisionResult(
        correctCount: Int,
        totalPlates: Int,
        plateDetails: String,
        outcomeLabel: String
    ) {
        val uid = activeProfile.value?.id ?: return
        viewModelScope.launch {
            val session = repository.ensureActiveSession(uid)
            repository.updateSession(
                session.copy(
                    colorCompleted = true,
                    colorCorrect = correctCount,
                    colorTotal = totalPlates,
                    colorPlateDetails = plateDetails,
                    colorOutcomeLabel = outcomeLabel,
                    aiReportGenerated = false
                )
            )
        }
    }

    fun saveBlinkResult(
        blinkCount: Int,
        durationSec: Int,
        blinksPerMinute: Float,
        avgEyeOpenProb: Float,
        observationLabel: String
    ) {
        val uid = activeProfile.value?.id ?: return
        viewModelScope.launch {
            val session = repository.ensureActiveSession(uid)
            repository.updateSession(
                session.copy(
                    blinkCompleted = true,
                    blinkCount = blinkCount,
                    blinkDurationSec = durationSec,
                    blinksPerMinute = blinksPerMinute,
                    avgEyeOpenProb = avgEyeOpenProb,
                    blinkObservationLabel = observationLabel,
                    aiReportGenerated = false
                )
            )
        }
    }

    fun saveEyeMovementResult(
        targetsTracked: Int,
        targetsTotal: Int,
        trackingRatio: Float,
        observationLabel: String
    ) {
        val uid = activeProfile.value?.id ?: return
        viewModelScope.launch {
            val session = repository.ensureActiveSession(uid)
            repository.updateSession(
                session.copy(
                    movementCompleted = true,
                    movementTargetsTracked = targetsTracked,
                    movementTargetsTotal = targetsTotal,
                    movementTrackingRatio = trackingRatio,
                    movementObservationLabel = observationLabel,
                    aiReportGenerated = false
                )
            )
        }
    }

    fun generateAiReport(session: ScreeningSessionEntity) {
        val profile = activeProfile.value ?: return
        val lang = preferences.value.language
        viewModelScope.launch {
            _aiReportState.value = AiReportUiState.Loading
            val result = repository.generateAiReport(profile, session, lang)
            _aiReportState.value = result.fold(
                onSuccess = { AiReportUiState.Success },
                onFailure = { AiReportUiState.Error(it.localizedMessage ?: "Failed to generate report.") }
            )
        }
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            val uid = activeProfile.value?.id
            if (uid != null && screeningHistory.value.size <= 1) {
                repository.ensureActiveSession(uid)
            }
        }
    }

    fun clearAllHistory() {
        val uid = activeProfile.value?.id ?: return
        viewModelScope.launch {
            repository.clearAllSessionsForUser(uid)
            _selectedReportSessionId.value = null
            _aiReportState.value = AiReportUiState.Idle
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch { repository.setDarkMode(enabled) }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch { repository.setLanguage(language) }
    }

    fun toggleLargeText(enabled: Boolean) {
        viewModelScope.launch { repository.setLargeTextMode(enabled) }
    }

    fun setCameraConsent(granted: Boolean) {
        viewModelScope.launch { repository.setCameraConsent(granted) }
    }

    fun setScreenBreakReminder(enabled: Boolean) {
        viewModelScope.launch { repository.setScreenBreakReminder(enabled) }
    }

    class Factory(private val repository: VisionScanRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return VisionScanViewModel(repository) as T
        }
    }
}
