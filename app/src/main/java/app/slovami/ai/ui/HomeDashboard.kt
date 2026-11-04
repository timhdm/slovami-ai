package app.slovami.ai.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.slovami.ai.data.SessionStore
import app.slovami.ai.ui.theme.ElectricViolet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Brand mark shared by the dashboard and the app bar. */
@Composable
internal fun SlovamiBrandLogo(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        val heights = listOf(14, 29, 43, 28, 15)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            heights.forEachIndexed { index, height ->
                Box(
                    Modifier.width(4.dp).height(height.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.verticalGradient(
                                if (index % 2 == 0)
                                    listOf(ElectricViolet.accent, ElectricViolet.violet)
                                else listOf(ElectricViolet.violet, ElectricViolet.neonBlue)
                            )
                        )
                )
            }
        }
        Text(
            buildAnnotatedString {
                withStyle(SpanStyle(color = ElectricViolet.text)) { append("Slovami ") }
                withStyle(SpanStyle(color = ElectricViolet.accent)) { append("AI") }
            },
            fontWeight = FontWeight.Bold,
            fontSize = 21.sp,
            maxLines = 1,
        )
    }
}

/** Home view for the IDLE phase; no recognition or recording logic is modified. */
@Composable
internal fun HomeDashboard(
    onStartRecording: () -> Unit,
    onImportAudio: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSession: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var recent by remember { mutableStateOf<List<SessionStore.SessionSummary>>(emptyList()) }
    LaunchedEffect(context) {
        recent = withContext(Dispatchers.IO) {
            SessionStore.listSummaries(context).take(3)
        }
    }

    Column(
        modifier = modifier.verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Spacer(Modifier.height(12.dp))
        Text(
            buildAnnotatedString {
                append("От записи до ")
                withStyle(SpanStyle(color = ElectricViolet.accent)) {
                    append("готового текста.")
                }
            },
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = ElectricViolet.text,
        )
        Text(
            "Запишите разговор или загрузите аудиофайл. Slovami AI распознает речь, разделит её по участникам и поможет выделить главное.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier.fillMaxWidth().height(224.dp),
            contentAlignment = Alignment.Center,
        ) {
            WaveformDecoration(modifier = Modifier.fillMaxWidth().height(104.dp))
            Box(
                modifier = Modifier.size(205.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(ElectricViolet.accent, ElectricViolet.violet, ElectricViolet.neonBlue)
                        ),
                        CircleShape,
                    )
                    .padding(4.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF5D16B5), Color(0xFF24103F), Color(0xFF15112F))
                        ),
                    )
                    .clickable(onClick = onStartRecording),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(9.dp),
                ) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = null,
                        tint = ElectricViolet.text,
                        modifier = Modifier.size(56.dp),
                    )
                    Text(
                        "Новая запись",
                        color = ElectricViolet.text,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth()
                .border(
                    1.dp,
                    Brush.horizontalGradient(
                        listOf(ElectricViolet.accent, ElectricViolet.violet)
                    ),
                    RoundedCornerShape(28.dp),
                )
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF311653), Color(0xFF201634))
                    ),
                    RoundedCornerShape(28.dp),
                )
                .clickable(onClick = onImportAudio)
                .padding(vertical = 17.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.Upload, contentDescription = null, tint = ElectricViolet.text)
            Spacer(Modifier.width(12.dp))
            Text(
                "Импортировать аудио",
                color = ElectricViolet.text,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium,
            )
        }

        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                "Последние записи",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ElectricViolet.text,
            )
            TextButton(onClick = onOpenHistory) {
                Text("Вся история ›", color = ElectricViolet.accent)
            }
        }
        if (recent.isEmpty()) {
            Box(
                Modifier.fillMaxWidth()
                    .background(ElectricViolet.card, RoundedCornerShape(16.dp))
                    .padding(18.dp),
            ) {
                Text(
                    "Здесь появятся сохранённые записи.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            recent.forEach { session ->
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .border(1.dp, ElectricViolet.cardBorder, RoundedCornerShape(16.dp))
                        .background(ElectricViolet.card, RoundedCornerShape(16.dp))
                        .clickable { onOpenSession(session.id) }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier.size(38.dp)
                            .background(Color(0xFF302047), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = ElectricViolet.text,
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            session.title.ifBlank { "Без названия" },
                            color = ElectricViolet.text,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                        )
                        Text(
                            "${SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(session.createdAt))} · ${formatHomeDuration(session.durationMs)}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(20.dp))
    }
}

private fun formatHomeDuration(ms: Long): String {
    val minutes = ms / 60_000
    val seconds = ms / 1_000 % 60
    return "%02d:%02d".format(Locale.ROOT, minutes, seconds)
}

/** Static waveform: a decorative brand element, not a fake live microphone level. */
@Composable
private fun WaveformDecoration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val bars = listOf(
            0.08f, 0.17f, 0.35f, 0.55f, 0.76f, 0.48f, 0.27f, 0.59f,
            0.38f, 0.16f, 0.24f, 0.48f, 0.72f, 0.92f, 0.67f, 0.33f,
            0.18f, 0.29f, 0.55f, 0.78f, 0.43f, 0.20f, 0.36f, 0.64f,
            0.88f, 0.56f, 0.33f, 0.17f,
        )
        val widthStep = size.width / (bars.size + 1)
        bars.forEachIndexed { index, fraction ->
            val x = widthStep * (index + 1)
            val halfHeight = size.height * fraction * 0.45f
            drawLine(
                color = if (index < bars.size / 2) ElectricViolet.accent
                    else ElectricViolet.neonBlue,
                start = Offset(x, size.center.y - halfHeight),
                end = Offset(x, size.center.y + halfHeight),
                strokeWidth = widthStep * 0.46f,
                cap = StrokeCap.Round,
            )
        }
    }
}
