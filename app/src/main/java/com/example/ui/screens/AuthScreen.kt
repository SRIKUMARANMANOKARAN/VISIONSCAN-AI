package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.UserProfileEntity
import com.example.ui.components.HeaderQuickControls
import com.example.ui.components.MedicalDisclaimerCard
import com.example.ui.components.PrivacyTrustBadge
import com.example.ui.components.SdgThreePill
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalStrings

@Composable
fun AuthScreen(
    profiles: List<UserProfileEntity>,
    language: AppLanguage,
    isDarkMode: Boolean,
    onToggleLanguage: () -> Unit,
    onToggleDarkMode: () -> Unit,
    onRegisterProfile: (name: String, age: Int, email: String, wearsGlasses: Boolean) -> Unit,
    onSelectProfile: (Long) -> Unit,
    onEnterDemoMode: () -> Unit
) {
    val strings = LocalStrings.current
    var selectedTab by remember { mutableIntStateOf(if (profiles.isNotEmpty()) 1 else 0) }
    var fullName by remember { mutableStateOf("") }
    var ageText by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var wearsGlasses by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top bar with Brand + Quick Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_visionscan_fg_1795579124739),
                        contentDescription = "VisionScan AI logo",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Column {
                        Text(
                            text = strings.appName,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = strings.tagline,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                HeaderQuickControls(
                    language = language,
                    isDarkMode = isDarkMode,
                    onToggleLanguage = onToggleLanguage,
                    onToggleDarkMode = onToggleDarkMode
                )
            }

            // Hero Header Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(145.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_hero_vision_banner_1795579126763),
                            contentDescription = "VisionScan AI clinical eye screening banner",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.25f)
                                )
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SdgThreePill()
                        }
                    }

                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = strings.welcomeTitle,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = strings.welcomeSubtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        PrivacyTrustBadge()
                    }
                }
            }

            // Auth Card (Register / Saved Profiles)
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.clip(RoundedCornerShape(14.dp))
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text(strings.registerTab, fontWeight = FontWeight.SemiBold) },
                            modifier = Modifier.testTag("auth_register_tab")
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = {
                                Text(
                                    "${strings.savedProfilesTab} (${profiles.size})",
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            modifier = Modifier.testTag("auth_profiles_tab")
                        )
                    }

                    if (selectedTab == 0) {
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = {
                                fullName = it
                                errorMessage = null
                            },
                            label = { Text(strings.fullNameLabel) },
                            leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_name_input")
                        )

                        OutlinedTextField(
                            value = ageText,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() } && input.length <= 3) {
                                    ageText = input
                                    errorMessage = null
                                }
                            },
                            label = { Text(strings.ageLabel) },
                            leadingIcon = { Icon(Icons.Rounded.Cake, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_age_input")
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text(strings.emailOptionalLabel) },
                            leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_email_input")
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { wearsGlasses = !wearsGlasses }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = wearsGlasses,
                                onCheckedChange = { wearsGlasses = it },
                                modifier = Modifier.testTag("auth_glasses_checkbox")
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = strings.wearsGlassesLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (errorMessage != null) {
                            Text(
                                text = errorMessage.orEmpty(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Button(
                            onClick = {
                                val parsedAge = ageText.toIntOrNull()
                                when {
                                    fullName.isBlank() -> errorMessage = strings.nameRequiredError
                                    parsedAge == null || parsedAge !in 5..120 -> errorMessage = strings.validAgeError
                                    else -> onRegisterProfile(fullName, parsedAge, email, wearsGlasses)
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("register_continue_button")
                        ) {
                            Icon(Icons.Rounded.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(strings.createProfileAndContinue, style = MaterialTheme.typography.labelLarge)
                        }
                    } else {
                        if (profiles.isEmpty()) {
                            Text(
                                text = strings.noSavedProfiles,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                profiles.forEach { profile ->
                                    Surface(
                                        onClick = { onSelectProfile(profile.id) },
                                        shape = RoundedCornerShape(16.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("select_profile_${profile.id}")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(42.dp)
                                                        .clip(CircleShape)
                                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Badge,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                                Column {
                                                    Text(
                                                        text = profile.name,
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                    Text(
                                                        text = "${profile.age} yrs" + if (profile.isDemoMode) " • ${strings.demoBadge}" else "",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                                contentDescription = "Open profile",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Demo Mode CTA
                    OutlinedButton(
                        onClick = onEnterDemoMode,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.secondary),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.secondary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("enter_demo_mode_button")
                    ) {
                        Icon(Icons.Rounded.PlayCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(strings.enterDemoMode, style = MaterialTheme.typography.labelLarge)
                    }

                    Text(
                        text = strings.demoModeBanner,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            MedicalDisclaimerCard(text = strings.medicalDisclaimerFull)
            Spacer(Modifier.height(16.dp))
        }
    }
}
