package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AccessibilityNew
import androidx.compose.material.icons.rounded.CameraFront
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.UserProfileEntity
import com.example.data.preferences.UserPreferences
import com.example.ui.components.MedicalDisclaimerCard
import com.example.ui.components.PrivacyTrustBadge
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalStrings

@Composable
fun ProfileScreen(
    profile: UserProfileEntity,
    preferences: UserPreferences,
    onUpdateProfile: (name: String, age: Int, email: String, wearsGlasses: Boolean) -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    onSelectLanguage: (AppLanguage) -> Unit,
    onToggleLargeText: (Boolean) -> Unit,
    onToggleCameraConsent: (Boolean) -> Unit,
    onToggleScreenBreakReminder: (Boolean) -> Unit,
    onClearAllHistory: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current

    var name by remember(profile.id) { mutableStateOf(profile.name) }
    var ageText by remember(profile.id) { mutableStateOf(profile.age.toString()) }
    var email by remember(profile.id) { mutableStateOf(profile.email) }
    var wearsGlasses by remember(profile.id) { mutableStateOf(profile.wearsGlasses) }
    var savedBanner by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(profile) {
        name = profile.name
        ageText = profile.age.toString()
        email = profile.email
        wearsGlasses = profile.wearsGlasses
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(strings.confirmDeleteTitle, fontWeight = FontWeight.Bold) },
            text = { Text(strings.confirmDeleteAllBody) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onClearAllHistory()
                    }
                ) {
                    Text(strings.delete, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }

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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.profileTitle,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    PrivacyTrustBadge()
                }
            }

            // Personal Details Editor Card
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = strings.editProfileTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                savedBanner = false
                            },
                            label = { Text(strings.fullNameLabel) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("profile_name_input")
                        )

                        OutlinedTextField(
                            value = ageText,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() } && input.length <= 3) {
                                    ageText = input
                                    savedBanner = false
                                }
                            },
                            label = { Text(strings.ageLabel) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("profile_age_input")
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                savedBanner = false
                            },
                            label = { Text(strings.emailOptionalLabel) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = wearsGlasses,
                                onCheckedChange = {
                                    wearsGlasses = it
                                    savedBanner = false
                                }
                            )
                            Text(
                                text = strings.wearsGlassesLabel,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Button(
                            onClick = {
                                val parsedAge = ageText.toIntOrNull()?.coerceIn(5, 120) ?: profile.age
                                if (name.isNotBlank()) {
                                    onUpdateProfile(name, parsedAge, email, wearsGlasses)
                                    savedBanner = true
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("profile_save_button")
                        ) {
                            Icon(Icons.Rounded.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(strings.saveChanges, style = MaterialTheme.typography.labelLarge)
                        }

                        if (savedBanner) {
                            Text(
                                text = "✓ ${strings.saveChanges}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Display, Language & Accessibility Preferences
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = strings.preferencesTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )

                        // Language Picker
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Rounded.Translate, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text(strings.languageLabel, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = preferences.language == AppLanguage.ENGLISH,
                                    onClick = { onSelectLanguage(AppLanguage.ENGLISH) },
                                    label = { Text("English") },
                                    modifier = Modifier.testTag("lang_chip_en")
                                )
                                FilterChip(
                                    selected = preferences.language == AppLanguage.TAMIL,
                                    onClick = { onSelectLanguage(AppLanguage.TAMIL) },
                                    label = { Text("தமிழ்") },
                                    modifier = Modifier.testTag("lang_chip_ta")
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        PreferenceSwitchRow(
                            icon = Icons.Rounded.DarkMode,
                            title = strings.darkModeLabel,
                            subtitle = null,
                            checked = preferences.isDarkMode,
                            testTag = "pref_dark_mode_switch",
                            onCheckedChange = onToggleDarkMode
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        PreferenceSwitchRow(
                            icon = Icons.Rounded.AccessibilityNew,
                            title = strings.largeTextLabel,
                            subtitle = strings.largeTextDesc,
                            checked = preferences.largeTextMode,
                            testTag = "pref_large_text_switch",
                            onCheckedChange = onToggleLargeText
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        PreferenceSwitchRow(
                            icon = Icons.Rounded.CameraFront,
                            title = strings.cameraConsentToggleLabel,
                            subtitle = null,
                            checked = preferences.cameraConsentGranted,
                            testTag = "pref_camera_consent_switch",
                            onCheckedChange = onToggleCameraConsent
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        PreferenceSwitchRow(
                            icon = Icons.Rounded.NotificationsActive,
                            title = strings.screenBreakReminderLabel,
                            subtitle = null,
                            checked = preferences.screenBreakReminderEnabled,
                            testTag = "pref_screen_break_switch",
                            onCheckedChange = onToggleScreenBreakReminder
                        )
                    }
                }
            }

            // Data Privacy & Logout
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = strings.dataPrivacyTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedButton(
                            onClick = { showDeleteDialog = true },
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.6f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("profile_clear_history_button")
                        ) {
                            Icon(Icons.Rounded.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(strings.clearAllHistory, style = MaterialTheme.typography.labelLarge)
                        }

                        Button(
                            onClick = onLogout,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("profile_logout_button")
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(strings.logoutButton, style = MaterialTheme.typography.labelLarge)
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

@Composable
private fun PreferenceSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    checked: Boolean,
    testTag: String,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Spacer(Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag)
        )
    }
}
