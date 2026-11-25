package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.components.MedicalDisclaimerCard
import com.example.ui.components.SdgThreePill
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalStrings
import com.example.ui.theme.JetBrainsMono
import kotlinx.coroutines.delay
import java.util.Locale

private data class EyeCareGuide(
    val icon: ImageVector,
    val titleEn: String,
    val bodyEn: String,
    val titleTa: String,
    val bodyTa: String
)

private val EYE_CARE_GUIDES = listOf(
    EyeCareGuide(
        icon = Icons.Rounded.Timer,
        titleEn = "Reduce Digital Eye Strain",
        bodyEn = "Keep screens 50–65 cm away with the top edge slightly below eye level. Conscious full blinks help keep the tear film moist during reading.",
        titleTa = "திரை கண் சோர்வைக் குறைக்கவும்",
        bodyTa = "திரையை 50–65 செ.மீ தூரத்தில் கண் மட்டத்திற்குச் சற்று கீழே வைக்கவும். படிக்கும்போது முழுமையாகக் கண் சிமிட்டுவது கண்களை ஈரப்பதமாக வைத்திருக்கும்."
    ),
    EyeCareGuide(
        icon = Icons.Rounded.Lightbulb,
        titleEn = "Balanced Room Lighting",
        bodyEn = "Avoid viewing bright screens in pitch-dark rooms. Soft ambient light reduces glare and pupillary fatigue.",
        titleTa = "சீரான அறை வெளிச்சம்",
        bodyTa = "முற்றிலும் இருட்டான அறையில் பிரகாசமான திரையைப் பார்ப்பதைத் தவிர்க்கவும். மென்மையான அறை வெளிச்சம் கண் சோர்வைக் குறைக்கும்."
    ),
    EyeCareGuide(
        icon = Icons.Rounded.WbSunny,
        titleEn = "UV Protection & Nutrition",
        bodyEn = "Wear UV400 sunglasses outdoors and include leafy greens, carrots, citrus, and omega-3 foods to support retinal health.",
        titleTa = "UV பாதுகாப்பு & சத்தான உணவு",
        bodyTa = "வெயிலில் செல்லும்போது UV400 கண்ணாடி அணியவும்; கீரைகள், கேரட் மற்றும் ஒமேகா-3 உணவுகள் கண் நலத்திற்கு உதவும்."
    ),
    EyeCareGuide(
        icon = Icons.Rounded.LocalHospital,
        titleEn = "When to See an Eye Specialist",
        bodyEn = "Schedule an exam every 1–2 years—or promptly if you notice sudden blurry vision, halos around lights, eye pain, flashes, or persistent dryness.",
        titleTa = "கண் மருத்துவரை எப்போது அணுக வேண்டும்",
        bodyTa = "ஒவ்வொரு 1–2 வருடங்களுக்கும் கண் பரிசோதனை செய்துகொள்ளவும்; திடீர் மங்கல், கண் வலி அல்லது சிவப்பு இருந்தால் உடனடியாக மருத்துவரை அணுகவும்."
    )
)

@Composable
fun EyeCareScreen(
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current

    // Interactive 20-20-20 timer: toggles between 20-min work countdown and 20-sec eye rest
    var isRestMode by remember { mutableStateOf(false) }
    var isRunning by remember { mutableStateOf(false) }
    var secondsLeft by remember { mutableIntStateOf(20 * 60) }

    LaunchedEffect(isRunning, isRestMode) {
        while (isRunning && secondsLeft > 0) {
            delay(1_000L)
            secondsLeft -= 1
        }
        if (isRunning && secondsLeft == 0) {
            if (!isRestMode) {
                isRestMode = true
                secondsLeft = 20
            } else {
                isRestMode = false
                isRunning = false
                secondsLeft = 20 * 60
            }
        }
    }

    val totalDuration = if (isRestMode) 20 else 20 * 60
    val progress = 1f - (secondsLeft.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f)
    val mins = secondsLeft / 60
    val secs = secondsLeft % 60
    val formattedClock = String.format(Locale.US, "%02d:%02d", mins, secs)

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 700.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = strings.eyeCareTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            // Wellness Illustration Hero Card
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column {
                        Image(
                            painter = painterResource(id = R.drawable.img_eye_care_banner_1795579127352),
                            contentDescription = "Eye care wellness illustration",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                        )
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SdgThreePill()
                            Text(
                                text = strings.screenBreakTimerTitle,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = strings.screenBreakRuleDesc,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Interactive 20-20-20 Screen-Break Timer Card
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isRestMode) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                        else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isRestMode) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (isRestMode) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.SelfImprovement,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = strings.restingEyesPrompt,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = strings.lookAwayDistanceHint,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }

                        Text(
                            text = formattedClock,
                            style = MaterialTheme.typography.displayLarge.copy(fontFamily = JetBrainsMono),
                            fontWeight = FontWeight.Bold,
                            color = if (isRestMode) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.testTag("screen_break_timer_text")
                        )

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(50))
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { isRunning = !isRunning },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("timer_play_pause_button")
                            ) {
                                Icon(
                                    imageVector = if (isRunning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(if (isRunning) strings.pauseTimer else "20:00", style = MaterialTheme.typography.labelLarge)
                            }

                            Button(
                                onClick = {
                                    isRestMode = true
                                    secondsLeft = 20
                                    isRunning = true
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(48.dp)
                                    .testTag("timer_start_20s_rest_button")
                            ) {
                                Text(strings.startRestNow, style = MaterialTheme.typography.labelMedium)
                            }

                            OutlinedButton(
                                onClick = {
                                    isRunning = false
                                    isRestMode = false
                                    secondsLeft = 20 * 60
                                },
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("timer_reset_button")
                            ) {
                                Icon(Icons.Rounded.Refresh, contentDescription = strings.resetTimer)
                            }
                        }
                    }
                }
            }

            // Awareness Guides
            item {
                Text(
                    text = strings.awarenessGuidesTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            items(EYE_CARE_GUIDES.size) { index ->
                val guide = EYE_CARE_GUIDES[index]
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = guide.icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = if (language == AppLanguage.TAMIL) guide.titleTa else guide.titleEn,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (language == AppLanguage.TAMIL) guide.bodyTa else guide.bodyEn,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Privacy & Consent Center
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Shield,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = strings.privacyCenterTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        strings.privacyBullets.forEach { bullet ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "• $bullet",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                MedicalDisclaimerCard(text = strings.medicalDisclaimerFull)
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}
