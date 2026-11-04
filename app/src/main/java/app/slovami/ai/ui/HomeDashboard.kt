package app.slovami.ai.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import app.slovami.ai.data.SessionStore
import app.slovami.ai.ui.theme.ElectricViolet
import app.slovami.ai.ui.theme.ElectricVioletCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Idle-only dashboard; recording, processing and transcript states remain in InterviewScreen. */
@Composable
internal fun HomeDashboard(
    onStartRecording: () -> Unit,
    onImportAudio: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSession: (String) -> Unit,
    onEditMetadata: () -> Unit,
    onEditParticipants: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var recent by remember { mutableStateOf<List<SessionStore.SessionSummary>>(emptyList()) }
    LaunchedEffect(context) {
        recent = withContext(Dispatchers.IO) {
            SessionStore.listSummaries(context).take(3)
        }
    }

    HomeDashboardContent(
        recent = recent,
        onStartRecording = onStartRecording,
        onImportAudio = onImportAudio,
        onOpenHistory = onOpenHistory,
        onOpenSession = onOpenSession,
        onEditMetadata = onEditMetadata,
        onEditParticipants = onEditParticipants,
        modifier = modifier,
    )
}

/** Pure UI content: Preview can render this without reading the session store. */
@Composable
private fun HomeDashboardContent(
    recent: List<SessionStore.SessionSummary>,
    onStartRecording: () -> Unit,
    onImportAudio: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSession: (String) -> Unit,
    onEditMetadata: () -> Unit,
    onEditParticipants: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()).padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Subtle violet-blue ribbons behind the right side of the introduction.
            Canvas(modifier = Modifier.matchParentSize()) {
                val w = size.width
                val h = size.height
                val ribbon = Brush.horizontalGradient(
                    listOf(ElectricViolet.violet, ElectricViolet.accent, ElectricViolet.neonBlue),
                    startX = w * 0.40f,
                    endX = w,
                )
                for (i in 0..3) {
                    val y = h * (0.42f + i * 0.075f)
                    val path = Path().apply {
                        moveTo(w * 0.40f, y)
                        cubicTo(w * 0.59f, y - h * 0.22f, w * 0.72f, y + h * 0.35f, w, y - h * 0.16f)
                    }
                    drawPath(
                        path = path,
                        brush = ribbon,
                        alpha = 0.10f + i * 0.035f,
                        style = Stroke(width = 2.dp.toPx() + i * 1.5.dp.toPx()),
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    buildAnnotatedString {
                        append("От ")
                        withStyle(
                            SpanStyle(brush = Brush.horizontalGradient(
                                listOf(ElectricViolet.accent, ElectricViolet.violet),
                            )),
                        ) { append("записи") }
                        append(" до\n")
                        withStyle(
                            SpanStyle(brush = Brush.horizontalGradient(
                                listOf(ElectricViolet.accent, ElectricViolet.neonBlue),
                            )),
                        ) { append("готового текста") }
                        append(".")
                    },
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 23.sp,
                        lineHeight = 27.sp,
                    ),
                    color = ElectricViolet.text,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "Запишите разговор или загрузите аудиофайл. Slovami AI распознает речь, разделит её по участникам и поможет выделить главное.",
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            HomeActionTile(
                label = "Запись",
                icon = Icons.Default.Mic,
                primary = true,
                onClick = onStartRecording,
                modifier = Modifier.weight(1f),
            )
            HomeActionTile(
                label = "Импорт",
                icon = Icons.Default.AudioFile,
                primary = false,
                onClick = onImportAudio,
                modifier = Modifier.weight(1f),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Последние записи", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            TextButton(onClick = onOpenHistory) {
                Text("Вся история", color = ElectricViolet.accent)
            }
        }
        if (recent.isEmpty()) {
            ElectricVioletCard(modifier = Modifier.fillMaxWidth()) {
                Text("Здесь появятся сохранённые записи.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            recent.forEach { session ->
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .background(ElectricViolet.card, RoundedCornerShape(16.dp))
                        .clickable { onOpenSession(session.id) }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = ElectricViolet.accent)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            session.title.ifBlank { "Без названия" },
                            color = ElectricViolet.text,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                        )
                        Text(
                            "${session.durationMs / 60_000} мин · ${session.numSpeakers} участников",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
        Text("Параметры новой записи", style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.weight(1f)
                    .background(ElectricViolet.card, RoundedCornerShape(14.dp))
                    .clickable(onClick = onEditMetadata)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Default.Description, contentDescription = null, tint = ElectricViolet.accent)
                Spacer(Modifier.width(8.dp))
                Text("Название", style = MaterialTheme.typography.labelMedium)
            }
            Row(
                modifier = Modifier.weight(1f)
                    .background(ElectricViolet.card, RoundedCornerShape(14.dp))
                    .clickable(onClick = onEditParticipants)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Default.Group, contentDescription = null, tint = ElectricViolet.accent)
                Spacer(Modifier.width(8.dp))
                Text("Участники", style = MaterialTheme.typography.labelMedium)
            }
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun HomeActionTile(
    label: String,
    icon: ImageVector,
    primary: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(17.dp)
    Box(
        modifier = modifier
            .height(94.dp)
            .background(
                if (primary) Brush.linearGradient(
                    listOf(Color(0xFF431568), Color(0xFF25143F)),
                ) else Brush.linearGradient(
                    listOf(ElectricViolet.card, Color(0xFF12101F)),
                ),
                shape,
            )
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (primary) ElectricViolet.accent else ElectricViolet.text,
                modifier = Modifier.size(27.dp),
            )
            Text(
                label,
                color = ElectricViolet.text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Preview(name = "Home dashboard", showBackground = true, widthDp = 393, heightDp = 852)
@Preview(name = "Home dashboard compact", showBackground = true, widthDp = 360, heightDp = 740)
@Composable
private fun HomeDashboardPreview() {
    app.slovami.ai.ui.theme.InterviewTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ElectricViolet.background),
        ) {
            HomeDashboardContent(
                recent = emptyList(),
                onStartRecording = {},
                onImportAudio = {},
                onOpenHistory = {},
                onOpenSession = {},
                onEditMetadata = {},
                onEditParticipants = {},
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
