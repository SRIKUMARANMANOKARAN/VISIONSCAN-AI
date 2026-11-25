package com.example.data.ai

import com.example.data.local.ScreeningSessionEntity
import com.example.data.local.UserProfileEntity
import com.example.ui.i18n.AppLanguage
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

data class AiReportResult(
    val summary: String,
    val observations: String,
    val recommendations: String,
    val isPlaceholder: Boolean = true
)

/**
 * Single AI interface for screening report synthesis.
 * Currently backed by [PlaceholderAiReportRepository] because Firebase AI Logic
 * is not enabled. Can be swapped with a Firebase.ai Gemini implementation once
 * Firebase is provisioned.
 */
interface AiReportRepository {
    suspend fun generateScreeningReport(
        profile: UserProfileEntity,
        session: ScreeningSessionEntity,
        language: AppLanguage
    ): Result<AiReportResult>
}

class PlaceholderAiReportRepository : AiReportRepository {

    override suspend fun generateScreeningReport(
        profile: UserProfileEntity,
        session: ScreeningSessionEntity,
        language: AppLanguage
    ): Result<AiReportResult> {
        if (!session.hasAnyResult) {
            return Result.failure(
                IllegalStateException(
                    if (language == AppLanguage.TAMIL) {
                        "அறிக்கை உருவாக்க குறைந்தது ஒரு சோதனையையாவது முடிக்கவும்."
                    } else {
                        "Complete at least one screening test before generating a report."
                    }
                )
            )
        }

        // Brief realistic synthesis pause
        delay(650L)

        val observations = mutableListOf<String>()
        val recommendations = mutableListOf<String>()
        var suggestSpecialistFollowUp = false

        if (language == AppLanguage.TAMIL) {
            if (session.acuityCompleted) {
                observations += "• பார்வைத் திறன் (முதற்கட்ட): வலது கண் நிலை ${session.rightEyeLevel}/6 (${session.rightEyeCorrect}/${session.rightEyeTotal} சரி), இடது கண் நிலை ${session.leftEyeLevel}/6 (${session.leftEyeCorrect}/${session.leftEyeTotal} சரி). இது மருத்துவ Snellen அளவீடு அல்ல."
                if (session.rightEyeLevel <= 3 || session.leftEyeLevel <= 3 || kotlin.math.abs(session.rightEyeLevel - session.leftEyeLevel) >= 2) {
                    suggestSpecialistFollowUp = true
                }
            } else {
                observations += "• பார்வைத் திறன் சோதனை: இந்த அமர்வில் முடிக்கப்படவில்லை."
            }

            if (session.colorCompleted) {
                observations += "• நிறப் பார்வை (முதற்கட்ட): ${session.colorCorrect}/${session.colorTotal} நிறப் புள்ளி எண் தட்டுகள் சரியாக அடையாளம் காணப்பட்டன. திரை அமைப்புகள் நிறத் துல்லியத்தைப் பாதிக்கலாம்."
                if (session.colorCorrect < session.colorTotal - 1) {
                    suggestSpecialistFollowUp = true
                }
            } else {
                observations += "• நிறப் பார்வை சோதனை: இந்த அமர்வில் முடிக்கப்படவில்லை."
            }

            if (session.blinkCompleted) {
                observations += "• கண் சிமிட்டல் கண்காணிப்பு: ${session.blinkDurationSec} வினாடிகளில் ${session.blinkCount} சிமிட்டல்கள் பதிவாயின (மதிப்பீடு ~${session.blinksPerMinute.roundToInt()} / நிமிடம்). சராசரி கண் திறப்பு ${(session.avgEyeOpenProb * 100).roundToInt()}%."
            } else {
                observations += "• கண் சிமிட்டல் கண்காணிப்பு: இந்த அமர்வில் முடிக்கப்படவில்லை."
            }

            if (session.movementCompleted) {
                val pct = (session.movementTrackingRatio * 100).roundToInt()
                observations += "• கண் அசைவு கவனிப்பு: ${session.movementTargetsTracked}/${session.movementTargetsTotal} இலக்கு நிலைகளில் ($pct%) நிலையான பின்தொடர்வு காணப்பட்டது."
                if (session.movementTargetsTracked < (session.movementTargetsTotal / 2)) {
                    suggestSpecialistFollowUp = true
                }
            } else {
                observations += "• கண் அசைவு கண்காணிப்பு: இந்த அமர்வில் முடிக்கப்படவில்லை."
            }

            if (suggestSpecialistFollowUp) {
                recommendations += "• உங்கள் முதற்கட்ட முடிவுகளில் சில மாறுபாடுகள் உள்ளதால், தகுதியான கண் மருத்துவரிடம் (Optometrist / Ophthalmologist) முழுப் பரிசோதனை செய்துகொள்ளப் பரிந்துரைக்கப்படுகிறது."
            } else {
                recommendations += "• முடிந்த சோதனைகளில் முதற்கட்ட பதிவுகள் சீராக உள்ளன; இருப்பினும் வழக்கமான வருடாந்திர கண் பரிசோதனை அவசியம்."
            }
            recommendations += "• திரை பயன்படுத்தும்போது 20-20-20 விதியைப் பின்பற்றுங்கள் (ஒவ்வொரு 20 நிமிடத்திற்கும் 20 அடி தூரத்தை 20 வினாடிகள் பார்க்கவும்)."
            if (profile.age >= 40) {
                recommendations += "• 40 வயதிற்கு மேற்பட்டவர்களுக்கு வழக்கமான விரிவான கண் அழுத்த மற்றும் விழித்திரை பரிசோதனை முக்கியம்."
            }
            recommendations += "• மங்கலான பார்வை, கண் வலி, சிவப்பு அல்லது திடீர் பார்வை மாற்றம் இருந்தால் உடனடியாக மருத்துவரை அணுகவும்."

            val summary = "[மாதிரி அறிக்கை] ${profile.name} (வயது ${profile.age}) அவர்களுக்கான இந்த அமர்வில் ${session.completedModuleCount}/4 முதற்கட்ட கண் பரிசோதனைகள் பதிவு செய்யப்பட்டுள்ளன. இந்த சுருக்கம் உண்மையான சோதனை பதில்களின் அடிப்படையில் உருவாக்கப்பட்ட விழிப்புணர்வு குறிப்பு மட்டுமே; மருத்துவ நோயறிதல் அல்ல."

            return Result.success(
                AiReportResult(
                    summary = summary,
                    observations = observations.joinToString("\n"),
                    recommendations = recommendations.joinToString("\n"),
                    isPlaceholder = true
                )
            )
        }

        // English synthesis based strictly on completed modules
        if (session.acuityCompleted) {
            observations += "• Visual Acuity (Preliminary): Right eye reached Level ${session.rightEyeLevel}/6 (${session.rightEyeCorrect}/${session.rightEyeTotal} correct); Left eye reached Level ${session.leftEyeLevel}/6 (${session.leftEyeCorrect}/${session.leftEyeTotal} correct) at scale ${String.format("%.2f", session.calibrationScale)}x. This is a mobile awareness check, not a clinical Snellen reading."
            if (session.rightEyeLevel <= 3 || session.leftEyeLevel <= 3 || kotlin.math.abs(session.rightEyeLevel - session.leftEyeLevel) >= 2) {
                suggestSpecialistFollowUp = true
            }
        } else {
            observations += "• Visual Acuity Screening: Not completed in this session."
        }

        if (session.colorCompleted) {
            observations += "• Color Vision (Preliminary): Correctly identified ${session.colorCorrect} of ${session.colorTotal} pseudo-isochromatic screening plates (${session.colorOutcomeLabel}). Display brightness and color filters can influence results."
            if (session.colorCorrect < session.colorTotal - 1) {
                suggestSpecialistFollowUp = true
            }
        } else {
            observations += "• Color Vision Screening: Not completed in this session."
        }

        if (session.blinkCompleted) {
            observations += "• Blink Monitoring (Camera Observation): Recorded ${session.blinkCount} blinks over ${session.blinkDurationSec}s (~${session.blinksPerMinute.roundToInt()} blinks/min estimated; average eye openness ${(session.avgEyeOpenProb * 100).roundToInt()}%). Blink rates naturally fluctuate with focus and environment."
        } else {
            observations += "• Blink Monitoring: Not completed in this session."
        }

        if (session.movementCompleted) {
            val pct = (session.movementTrackingRatio * 100).roundToInt()
            observations += "• Eye Movement Tracking (Interactive Observation): Consistent target tracking detected across ${session.movementTargetsTracked} of ${session.movementTargetsTotal} checkpoints ($pct%). Not a clinical ocular motility exam."
            if (session.movementTargetsTracked < (session.movementTargetsTotal / 2)) {
                suggestSpecialistFollowUp = true
            }
        } else {
            observations += "• Eye Movement Tracking: Not completed in this session."
        }

        if (suggestSpecialistFollowUp) {
            recommendations += "• Consider scheduling a comprehensive in-person exam with an optometrist or ophthalmologist to review the preliminary differences observed in this session."
        } else {
            recommendations += "• Recorded preliminary responses appear consistent across completed modules; continue routine preventive eye checkups with an eye care professional."
        }
        recommendations += "• Follow the 20-20-20 rule during extended screen sessions to help minimize digital eye fatigue and maintain comfortable blinking."
        if (profile.wearsGlasses) {
            recommendations += "• Keep your prescription eyewear up to date and verify lens clarity regularly during scheduled optometry visits."
        }
        if (profile.age >= 40) {
            recommendations += "• Adults aged 40+ benefit from periodic dilated eye examinations and intraocular pressure checks as recommended by eye care guidelines."
        }
        recommendations += "• Seek prompt professional medical attention if you ever experience sudden vision changes, eye pain, flashes, floaters, or persistent redness."

        val summary = "[Sample AI Report] Screening summary for ${profile.name} (Age ${profile.age}) covering ${session.completedModuleCount} of 4 completed preliminary modules. Only actually recorded test responses are summarized below; no clinical diagnosis is made or implied."

        return Result.success(
            AiReportResult(
                summary = summary,
                observations = observations.joinToString("\n"),
                recommendations = recommendations.joinToString("\n"),
                isPlaceholder = true
            )
        )
    }
}
