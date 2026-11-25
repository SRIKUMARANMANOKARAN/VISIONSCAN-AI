package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.ErrorOutline
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.components.MedicalDisclaimerCard
import com.example.ui.i18n.LocalStrings
import com.example.ui.theme.JetBrainsMono
import kotlin.math.hypot
import kotlin.random.Random

private enum class ColorTestPhase { INTRO, TESTING, SUMMARY }

private data class ColorPlateSpec(
    val id: Int,
    val number: String,
    val category: String,
    val options: List<String>,
    val bgPalette: List<Color>,
    val fgPalette: List<Color>,
    val seed: Int
)

private val SCREENING_COLOR_PLATES = listOf(
    ColorPlateSpec(
        id = 1,
        number = "12",
        category = "Control Plate",
        options = listOf("12", "17", "21", "42"),
        bgPalette = listOf(Color(0xFF4F7CAC), Color(0xFF6B93BA), Color(0xFF5D85AE)),
        fgPalette = listOf(Color(0xFFE67E22), Color(0xFFD96B27), Color(0xFFF09436)),
        seed = 101
    ),
    ColorPlateSpec(
        id = 2,
        number = "8",
        category = "Red-Green Contrast",
        options = listOf("3", "8", "6", "9"),
        bgPalette = listOf(Color(0xFF7DA35A), Color(0xFF8CB069), Color(0xFF6D944C), Color(0xFF9BB97A)),
        fgPalette = listOf(Color(0xFFD96953), Color(0xFFC95A46), Color(0xFFE37E68)),
        seed = 202
    ),
    ColorPlateSpec(
        id = 3,
        number = "6",
        category = "Red-Green Contrast",
        options = listOf("5", "6", "8", "9"),
        bgPalette = listOf(Color(0xFFC9785D), Color(0xFFD4876E), Color(0xFFBA6D54), Color(0xFFDF9780)),
        fgPalette = listOf(Color(0xFF6B9A62), Color(0xFF5B8C52), Color(0xFF7CAB73)),
        seed = 303
    ),
    ColorPlateSpec(
        id = 4,
        number = "29",
        category = "Red-Green Contrast",
        options = listOf("29", "70", "20", "79"),
        bgPalette = listOf(Color(0xFF93A864), Color(0xFFA1B472), Color(0xFF819655)),
        fgPalette = listOf(Color(0xFFD66E49), Color(0xFFC45F3B), Color(0xFFE2815D)),
        seed = 404
    ),
    ColorPlateSpec(
        id = 5,
        number = "74",
        category = "Red-Green Contrast",
        options = listOf("21", "74", "71", "24"),
        bgPalette = listOf(Color(0xFFCC8562), Color(0xFFD69473), Color(0xFFBC7656)),
        fgPalette = listOf(Color(0xFF669868), Color(0xFF568959), Color(0xFF7AA97B)),
        seed = 505
    ),
    ColorPlateSpec(
        id = 6,
        number = "45",
        category = "Blue-Yellow Contrast",
        options = listOf("45", "46", "15", "48"),
        bgPalette = listOf(Color(0xFF8E90B8), Color(0xFF9D9FC5), Color(0xFF7D7FA8)),
        fgPalette = listOf(Color(0xFFD9A641), Color(0xFFC89532), Color(0xFFE3B658)),
        seed = 606
    )
)

// Clean 5x7 pixel matrices for digits 0-9 used to paint original pseudo-isochromatic dot plates
private val DIGIT_BITMAPS: Map<Char, List<String>> = mapOf(
    '1' to listOf("..#..", ".##..", "..#..", "..#..", "..#..", "..#..", ".###."),
    '2' to listOf(".###.", "#...#", "....#", "..##.", ".#...", "#....", "#####"),
    '4' to listOf("#...#", "#...#", "#...#", "#####", "....#", "....#", "....#"),
    '5' to listOf("#####", "#....", "####.", "....#", "....#", "#...#", ".###."),
    '6' to listOf(".###.", "#....", "####.", "#...#", "#...#", "#...#", ".###."),
    '7' to listOf("#####", "....#", "...#.", "..#..", ".#...", ".#...", ".#..."),
    '8' to listOf(".###.", "#...#", "#...#", ".###.", "#...#", "#...#", ".###."),
    '9' to listOf(".###.", "#...#", "#...#", ".####", "....#", "...#.", ".##..")
)

private data class DotSample(
    val nx: Float,
    val ny: Float,
    val nRadius: Float,
    val color: Color
)

private fun isDigitPixel(number: String, nx: Float, ny: Float): Boolean {
    // Map normalized circle coordinates (-1..1) into the digit grid
    val chars = number.toList()
    val colsPerDigit = 5
    val gapCols = if (chars.size > 1) 1 else 0
    val totalCols = chars.size * colsPerDigit + (chars.size - 1) * gapCols
    val totalRows = 7

    val scaleX = if (chars.size == 1) 0.50f else 0.72f
    val scaleY = 0.62f

    if (nx < -scaleX || nx > scaleX || ny < -scaleY || ny > scaleY) return false

    val u = ((nx + scaleX) / (2f * scaleX)) * totalCols
    val v = ((ny + scaleY) / (2f * scaleY)) * totalRows

    val col = u.toInt().coerceIn(0, totalCols - 1)
    val row = v.toInt().coerceIn(0, totalRows - 1)

    val stride = colsPerDigit + gapCols
    val charIdx = col / stride
    val localCol = col % stride
    if (charIdx >= chars.size || localCol >= colsPerDigit) return false

    val grid = DIGIT_BITMAPS[chars[charIdx]] ?: return false
    return grid[row][localCol] == '#'
}

private fun buildPlateDots(spec: ColorPlateSpec): List<DotSample> {
    val rng = Random(spec.seed)
    val dots = mutableListOf<DotSample>()
    val radiiOptions = floatArrayOf(0.052f, 0.042f, 0.034f, 0.026f)

    // Deterministic non-overlapping circle packing inside unit disk
    for (r in radiiOptions) {
        val attempts = when {
            r >= 0.05f -> 420
            r >= 0.04f -> 560
            r >= 0.03f -> 720
            else -> 980
        }
        repeat(attempts) {
            val x = rng.nextFloat() * 2f - 1f
            val y = rng.nextFloat() * 2f - 1f
            if (hypot(x, y) + r <= 0.96f) {
                var overlaps = false
                for (existing in dots) {
                    val minDist = existing.nRadius + r + 0.008f
                    val dx = existing.nx - x
                    val dy = existing.ny - y
                    if (dx * dx + dy * dy < minDist * minDist) {
                        overlaps = true
                        break
                    }
                }
                if (!overlaps) {
                    val insideNum = isDigitPixel(spec.number, x, y)
                    val palette = if (insideNum) spec.fgPalette else spec.bgPalette
                    val color = palette[rng.nextInt(palette.size)]
                    dots += DotSample(x, y, r, color)
                }
            }
        }
    }
    return dots
}

private data class PlateResponse(
    val plate: ColorPlateSpec,
    val selectedAnswer: String?,
    val isCorrect: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorVisionScreen(
    onBack: () -> Unit,
    onSaveResult: (
        correctCount: Int,
        totalPlates: Int,
        plateDetails: String,
        outcomeLabel: String
    ) -> Unit
) {
    val strings = LocalStrings.current
    var phase by remember { mutableStateOf(ColorTestPhase.INTRO) }
    var currentIndex by remember { mutableIntStateOf(0) }
    val responses = remember { mutableStateListOf<PlateResponse>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.colorTitle, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("color_back_button")) {
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
                    ColorTestPhase.INTRO -> {
                        Card(
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.ColorLens,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Text(
                                        text = strings.colorTitle,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = strings.colorCardDesc,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        MedicalDisclaimerCard(
                            title = strings.colorDisplayWarningTitle,
                            text = strings.colorDisplayWarningBody
                        )

                        Button(
                            onClick = {
                                responses.clear()
                                currentIndex = 0
                                phase = ColorTestPhase.TESTING
                            },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("color_start_button")
                        ) {
                            Text(strings.colorStartButton, style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    ColorTestPhase.TESTING -> {
                        val plate = SCREENING_COLOR_PLATES[currentIndex]
                        val dots = remember(plate.id) { buildPlateDots(plate) }
                        val progress = currentIndex.toFloat() / SCREENING_COLOR_PLATES.size

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${strings.plateLabel} ${currentIndex + 1} / ${SCREENING_COLOR_PLATES.size}",
                                style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMono),
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = plate.category,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                )
                            }
                        }

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(50))
                        )

                        // Original Pseudo-Isochromatic Dot Matrix Plate
                        Surface(
                            shape = RoundedCornerShape(26.dp),
                            color = Color(0xFFFAFBFD),
                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(
                                    modifier = Modifier
                                        .widthIn(max = 290.dp)
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                        .testTag("color_plate_canvas_${plate.id}")
                                ) {
                                    val radius = size.minDimension / 2f
                                    val cx = size.width / 2f
                                    val cy = size.height / 2f
                                    drawCircle(
                                        color = Color(0xFFF0F3F6),
                                        radius = radius,
                                        center = Offset(cx, cy)
                                    )
                                    dots.forEach { d ->
                                        drawCircle(
                                            color = d.color,
                                            radius = d.nRadius * radius,
                                            center = Offset(cx + d.nx * radius, cy + d.ny * radius)
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = strings.colorQuestionPrompt,
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        fun recordChoice(answer: String?) {
                            responses += PlateResponse(
                                plate = plate,
                                selectedAnswer = answer,
                                isCorrect = answer == plate.number
                            )
                            if (currentIndex + 1 >= SCREENING_COLOR_PLATES.size) {
                                phase = ColorTestPhase.SUMMARY
                            } else {
                                currentIndex += 1
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            plate.options.chunked(2).forEach { pair ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    pair.forEach { opt ->
                                        Button(
                                            onClick = { recordChoice(opt) },
                                            shape = RoundedCornerShape(16.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(56.dp)
                                                .testTag("color_option_$opt")
                                        ) {
                                            Text(
                                                text = opt,
                                                style = MaterialTheme.typography.titleLarge.copy(fontFamily = JetBrainsMono),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = { recordChoice(null) },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("color_no_number_button")
                        ) {
                            Text(strings.noNumberVisible, style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    ColorTestPhase.SUMMARY -> {
                        val correctCount = responses.count { it.isCorrect }
                        val total = SCREENING_COLOR_PLATES.size
                        val outcomeLabel = when {
                            correctCount == total -> "All screening plates clearly identified"
                            correctCount >= total - 1 -> "High plate recognition (${correctCount}/${total})"
                            else -> "Some color contrast plates missed (${correctCount}/${total}) — consider professional color vision testing"
                        }

                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
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
                                        text = strings.colorResultTitle,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = "$correctCount / $total",
                                    style = MaterialTheme.typography.displaySmall.copy(fontFamily = JetBrainsMono),
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = outcomeLabel,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                // Plate-by-plate recap
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    responses.forEach { r ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${strings.plateLabel} ${r.plate.id} (${r.plate.category}): Expected ${r.plate.number}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = r.selectedAnswer ?: "None",
                                                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMono),
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Icon(
                                                    imageVector = if (r.isCorrect) Icons.Rounded.CheckCircle else Icons.Rounded.ErrorOutline,
                                                    contentDescription = null,
                                                    tint = if (r.isCorrect) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        MedicalDisclaimerCard(text = strings.colorDisplayWarningBody)

                        Button(
                            onClick = {
                                val details = responses.joinToString("; ") {
                                    "P${it.plate.id}:${it.selectedAnswer ?: "None"}/${it.plate.number}"
                                }
                                onSaveResult(correctCount, total, details, outcomeLabel)
                                onBack()
                            },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("color_save_result_button")
                        ) {
                            Text(strings.saveAndReturn, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
