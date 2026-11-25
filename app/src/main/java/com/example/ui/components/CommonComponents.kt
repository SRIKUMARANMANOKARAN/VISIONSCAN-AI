package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.HealthAndSafety
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.ScreeningSessionEntity
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalStrings
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.StatusCaution
import com.example.ui.theme.StatusCautionBg
import com.example.ui.theme.StatusComplete
import com.example.ui.theme.StatusCompleteBg
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatScreeningDate(timestampMs: Long): String {
    val fmt = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    return fmt.format(Date(timestampMs))
}

@Composable
fun HeaderQuickControls(
    language: AppLanguage,
    isDarkMode: Boolean,
    onToggleLanguage: () -> Unit,
    onToggleDarkMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Surface(
            onClick = onToggleLanguage,
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .testTag("language_toggle_button")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Translate,
                    contentDescription = "Switch language",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = if (language == AppLanguage.ENGLISH) "தமிழ்" else "EN",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        IconButton(
            onClick = onToggleDarkMode,
            modifier = Modifier.testTag("dark_mode_toggle_button")
        ) {
            Icon(
                imageVector = if (isDarkMode) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                contentDescription = if (isDarkMode) "Switch to light mode" else "Switch to dark mode",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun SdgThreePill(modifier: Modifier = Modifier) {
    val strings = LocalStrings.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.secondaryContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.HealthAndSafety,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = strings.sdgBadge,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun MedicalDisclaimerCard(
    text: String,
    modifier: Modifier = Modifier,
    title: String? = null
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.2f
    val bgColor = if (isDark) Color(0xFF2B2212) else StatusCautionBg
    val fgColor = if (isDark) Color(0xFFFFCA80) else StatusCaution
    val borderColor = fgColor.copy(alpha = 0.35f)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Info,
                contentDescription = "Medical notice",
                tint = fgColor,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(20.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (title != null) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = fgColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) Color(0xFFF3DEC0) else Color(0xFF5D3A09)
                )
            }
        }
    }
}

@Composable
fun CompletionStatusChip(
    completed: Boolean,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val isDark = MaterialTheme.colorScheme.background.red < 0.2f
    val bg = when {
        completed && isDark -> Color(0xFF0E3325)
        completed -> StatusCompleteBg
        isDark -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val fg = when {
        completed && isDark -> Color(0xFF69F0AE)
        completed -> StatusComplete
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = bg
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = if (completed) Icons.Rounded.CheckCircle else Icons.Rounded.Schedule,
                contentDescription = null,
                tint = fg,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = if (completed) strings.statusCompleted else strings.statusPending,
                style = MaterialTheme.typography.labelMedium,
                color = fg,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ClinicalMetricTile(
    icon: ImageVector,
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMono),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
    }
}

@Composable
fun ModuleCompletionRingChart(
    completedCount: Int,
    totalCount: Int = 4,
    modifier: Modifier = Modifier
) {
    val progress = (completedCount.toFloat() / totalCount.coerceAtLeast(1)).coerceIn(0f, 1f)
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary
    val tealColor = MaterialTheme.colorScheme.secondary

    Box(
        modifier = modifier.size(92.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(88.dp)) {
            val strokeWidth = 10.dp.toPx()
            val inset = strokeWidth / 2f
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)

            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            if (progress > 0f) {
                drawArc(
                    color = if (progress >= 1f) tealColor else primaryColor,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$completedCount/$totalCount",
                style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMono),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ScreeningHistoryBarChart(
    sessions: List<ScreeningSessionEntity>,
    modifier: Modifier = Modifier
) {
    val recent = sessions.take(6).reversed()
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val track = MaterialTheme.colorScheme.surfaceVariant

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
        ) {
            if (recent.isEmpty()) return@Canvas
            val barGroupWidth = size.width / recent.size
            val barWidth = (barGroupWidth * 0.42f).coerceAtMost(28.dp.toPx())
            val maxH = size.height - 10.dp.toPx()

            recent.forEachIndexed { idx, session ->
                val centerX = idx * barGroupWidth + barGroupWidth / 2f
                val fraction = (session.completedModuleCount / 4f).coerceIn(0.08f, 1f)
                val barH = maxH * fraction

                // Background rail
                drawRoundRect(
                    color = track,
                    topLeft = Offset(centerX - barWidth / 2f, 4.dp.toPx()),
                    size = Size(barWidth, maxH),
                    cornerRadius = CornerRadius(10f, 10f)
                )
                // Value bar
                drawRoundRect(
                    color = if (session.completedModuleCount == 4) secondary else primary,
                    topLeft = Offset(centerX - barWidth / 2f, 4.dp.toPx() + (maxH - barH)),
                    size = Size(barWidth, barH),
                    cornerRadius = CornerRadius(10f, 10f)
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            recent.forEachIndexed { idx, session ->
                Text(
                    text = "S${idx + 1} (${session.completedModuleCount}/4)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun PrivacyTrustBadge(modifier: Modifier = Modifier) {
    val strings = LocalStrings.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.VerifiedUser,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = strings.localModeBadge,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
