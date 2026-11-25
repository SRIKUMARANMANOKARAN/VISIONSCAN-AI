package com.example.ui.screens

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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.MedicalDisclaimerCard
import com.example.ui.i18n.LocalStrings
import com.example.ui.theme.JetBrainsMono
import java.util.Locale
import kotlin.random.Random

private enum class AcuityPhase {
    SETUP,
    COVER_LEFT_PROMPT,
    RIGHT_EYE_TEST,
    COVER_RIGHT_PROMPT,
    LEFT_EYE_TEST,
    SUMMARY
}

private val SLOAN_OPTOTYPES = listOf('C', 'D', 'E', 'F', 'H', 'K', 'N', 'O', 'P', 'R', 'T', 'V', 'Z')

// Optotype font sizes (in sp base at 1.0x calibration) descending from Level 1 (largest) to Level 6 (finest)
private val LEVEL_BASE_SIZES_SP = listOf(64f, 48f, 36f, 27f, 20f, 15f)
private const val TRIALS_PER_EYE = 6

private data class AcuityTrial(
    val targetLetter: Char,
    val choices: List<Char>
)

private fun generateTrial(previousLetter: Char?): AcuityTrial {
    val candidates = SLOAN_OPTOTYPES.filter { it != previousLetter }
    val target = candidates.random(Random(System.nanoTime()))
    val distractors = SLOAN_OPTOTYPES.filter { it != target }.shuffled(Random(System.nanoTime())).take(3)
    return AcuityTrial(
        targetLetter = target,
        choices = (distractors + target).shuffled(Random(System.nanoTime()))
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisualAcuityScreen(
    onBack: () -> Unit,
    onSaveResult: (
        rightLevel: Int,
        rightCorrect: Int,
        rightTotal: Int,
        leftLevel: Int,
        leftCorrect: Int,
        leftTotal: Int,
        calibrationScale: Float,
        outcomeLabel: String
    ) -> Unit
) {
    val strings = LocalStrings.current
    var phase by remember { mutableStateOf(AcuityPhase.SETUP) }
    var calibrationScale by remember { mutableFloatStateOf(1.0f) }

    // Adaptive staircase state
    var currentLevel by remember { mutableIntStateOf(2) } // 1..6
    var trialIndex by remember { mutableIntStateOf(0) }
    var currentTrial by remember { mutableStateOf(generateTrial(null)) }
    var highestCorrectLevel by remember { mutableIntStateOf(0) }
    var correctCount by remember { mutableIntStateOf(0) }

    // Recorded eye summaries
    var rightBestLevel by remember { mutableIntStateOf(0) }
    var rightCorrect by remember { mutableIntStateOf(0) }
    var leftBestLevel by remember { mutableIntStateOf(0) }
    var leftCorrect by remember { mutableIntStateOf(0) }

    fun resetEyeTrials() {
        currentLevel = 2
        trialIndex = 0
        highestCorrectLevel = 0
        correctCount = 0
        currentTrial = generateTrial(null)
    }

    fun submitAnswer(selected: Char?) {
        val isCorrect = selected == currentTrial.targetLetter
        if (isCorrect) {
            correctCount += 1
            if (currentLevel > highestCorrectLevel) {
                highestCorrectLevel = currentLevel
            }
            if (currentLevel < 6) currentLevel += 1
        } else {
            if (currentLevel > 1) currentLevel -= 1
        }

        if (trialIndex + 1 >= TRIALS_PER_EYE) {
            if (phase == AcuityPhase.RIGHT_EYE_TEST) {
                rightBestLevel = highestCorrectLevel.coerceAtLeast(1)
                rightCorrect = correctCount
                phase = AcuityPhase.COVER_RIGHT_PROMPT
            } else {
                leftBestLevel = highestCorrectLevel.coerceAtLeast(1)
                leftCorrect = correctCount
                phase = AcuityPhase.SUMMARY
            }
        } else {
            trialIndex += 1
            currentTrial = generateTrial(currentTrial.targetLetter)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.acuityTitle, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("acuity_back_button")) {
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
                    .widthIn(max = 640.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (phase) {
                    AcuityPhase.SETUP -> {
                        Text(
                            text = strings.acuitySetupSubtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.Visibility,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = strings.viewingDistanceTitle,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = strings.viewingDistanceBody,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.Straighten,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                    Text(
                                        text = strings.screenCalibrationTitle,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = strings.screenCalibrationBody,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                // Credit card calibration bar (~85.6mm reference)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width((210f * calibrationScale).dp)
                                            .height(44.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "85.6 mm • ${String.format(Locale.US, "%.2f", calibrationScale)}x",
                                            style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMono),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Slider(
                                    value = calibrationScale,
                                    onValueChange = { calibrationScale = it },
                                    valueRange = 0.75f..1.25f,
                                    modifier = Modifier.testTag("acuity_calibration_slider")
                                )
                            }
                        }

                        MedicalDisclaimerCard(text = strings.acuityDisclaimer)

                        Button(
                            onClick = { phase = AcuityPhase.COVER_LEFT_PROMPT },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("acuity_begin_right_eye")
                        ) {
                            Text(strings.beginRightEye, style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    AcuityPhase.COVER_LEFT_PROMPT, AcuityPhase.COVER_RIGHT_PROMPT -> {
                        val isRightEyeNext = phase == AcuityPhase.COVER_LEFT_PROMPT
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.VisibilityOff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(54.dp)
                                )
                                Text(
                                    text = if (isRightEyeNext) strings.rightEyeLabel else strings.leftEyeLabel,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isRightEyeNext) strings.coverLeftEyePrompt else strings.coverRightEyePrompt,
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.height(4.dp))
                                Button(
                                    onClick = {
                                        resetEyeTrials()
                                        phase = if (isRightEyeNext) AcuityPhase.RIGHT_EYE_TEST else AcuityPhase.LEFT_EYE_TEST
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("acuity_confirm_eye_ready")
                                ) {
                                    Text(strings.startTest, style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }

                    AcuityPhase.RIGHT_EYE_TEST, AcuityPhase.LEFT_EYE_TEST -> {
                        val eyeName = if (phase == AcuityPhase.RIGHT_EYE_TEST) strings.rightEyeLabel else strings.leftEyeLabel
                        val progress = (trialIndex.toFloat() / TRIALS_PER_EYE).coerceIn(0f, 1f)
                        val optotypeSizeSp = (LEVEL_BASE_SIZES_SP[(currentLevel - 1).coerceIn(0, 5)] * calibrationScale).sp

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = eyeName,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }
                            Text(
                                text = "${strings.levelLabel} $currentLevel/6 • ${trialIndex + 1}/$TRIALS_PER_EYE",
                                style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMono),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(50))
                        )

                        // High-contrast Clinical Optotype Chart Box (always crisp white background + dark ink)
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = androidx.compose.ui.graphics.Color.White,
                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = currentTrial.targetLetter.toString(),
                                    fontFamily = JetBrainsMono,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = optotypeSizeSp,
                                    color = androidx.compose.ui.graphics.Color(0xFF0A141E),
                                    modifier = Modifier.testTag("acuity_target_letter")
                                )
                            }
                        }

                        // 2x2 Multiple Choice Letter Buttons
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            currentTrial.choices.chunked(2).forEach { rowChoices ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    rowChoices.forEach { option ->
                                        Button(
                                            onClick = { submitAnswer(option) },
                                            shape = RoundedCornerShape(16.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(60.dp)
                                                .testTag("acuity_choice_$option")
                                        ) {
                                            Text(
                                                text = option.toString(),
                                                style = MaterialTheme.typography.headlineSmall.copy(fontFamily = JetBrainsMono),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = { submitAnswer(null) },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("acuity_cant_see_button")
                        ) {
                            Text(strings.cantSeeClearly, style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    AcuityPhase.SUMMARY -> {
                        val avgLevel = (rightBestLevel + leftBestLevel) / 2.0
                        val outcomeLabel = when {
                            avgLevel >= 5.0 -> "Consistent recognition down to finer screening optotypes"
                            avgLevel >= 3.5 -> "Moderate optotype clarity — consider routine optometry check"
                            else -> "Reduced optotype clarity observed — professional eye exam advised"
                        }

                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
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
                                        text = strings.acuityResultsTitle,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    EyeOutcomeCard(
                                        eyeLabel = strings.rightEyeLabel,
                                        level = rightBestLevel,
                                        correct = rightCorrect,
                                        total = TRIALS_PER_EYE,
                                        modifier = Modifier.weight(1f)
                                    )
                                    EyeOutcomeCard(
                                        eyeLabel = strings.leftEyeLabel,
                                        level = leftBestLevel,
                                        correct = leftCorrect,
                                        total = TRIALS_PER_EYE,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                                ) {
                                    Text(
                                        text = outcomeLabel,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(14.dp)
                                    )
                                }
                            }
                        }

                        MedicalDisclaimerCard(text = strings.acuityDisclaimer)

                        Button(
                            onClick = {
                                onSaveResult(
                                    rightBestLevel,
                                    rightCorrect,
                                    TRIALS_PER_EYE,
                                    leftBestLevel,
                                    leftCorrect,
                                    TRIALS_PER_EYE,
                                    calibrationScale,
                                    outcomeLabel
                                )
                                onBack()
                            },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("acuity_save_result_button")
                        ) {
                            Text(strings.saveAndReturn, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EyeOutcomeCard(
    eyeLabel: String,
    level: Int,
    correct: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = eyeLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Level $level / 6",
                style = MaterialTheme.typography.titleLarge.copy(fontFamily = JetBrainsMono),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "$correct / $total correct",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
