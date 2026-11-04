package app.slovami.ai.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import app.slovami.ai.data.SessionStore
import app.slovami.ai.ui.theme.ElectricViolet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Idle-only dashboard; recording, processing and transcript states remain in InterviewScreen. */
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

    HomeDashboardContent(
        recent = recent,
        onStartRecording = onStartRecording,
        onImportAudio = onImportAudio,
        onOpenHistory = onOpenHistory,
        onOpenSession = onOpenSession,
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
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()).padding(start = 18.dp, end = 18.dp, top = 12.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
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
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text(
                    buildAnnotatedString {
                        append("От ")
                        withStyle(
                            SpanStyle(brush = Brush.horizontalGradient(
                                listOf(ElectricViolet.accent, ElectricViolet.violet),
                            )),
                        ) { append("записи") }
                        append("\nдо ")
                        withStyle(
                            SpanStyle(brush = Brush.horizontalGradient(
                                listOf(ElectricViolet.neonBlue, ElectricViolet.accent),
                            )),
                        ) { append("готового текста") }
                        append(".")
                    },
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 27.sp,
                        lineHeight = 32.sp,
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
            verticalAlignment = Alignment.CenterVertically,
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
                icon = Icons.Default.FileUpload,
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
            Text("Недавние записи", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            TextButton(onClick = onOpenHistory) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("См. все", color = ElectricViolet.text)
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = ElectricViolet.text, modifier = Modifier.size(18.dp))
                }
            }
        }
        if (recent.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(ElectricViolet.card, RoundedCornerShape(16.dp))
                    .padding(18.dp),
            ) {
                Text("Здесь появятся сохранённые записи.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            recent.forEach { session ->
                RecentRecordingCard(
                    session = session,
                    onOpen = { onOpenSession(session.id) },
                    onOpenHistory = onOpenHistory,
                )
            }
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun RecentRecordingCard(
    session: SessionStore.SessionSummary,
    onOpen: () -> Unit,
    onOpenHistory: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(ElectricViolet.card, RoundedCornerShape(16.dp))
            .clickable(role = Role.Button, onClick = onOpen)
            .padding(start = 9.dp, top = 10.dp, bottom = 10.dp, end = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Box(
            modifier = Modifier.size(39.dp)
                .background(Color(0xFF302047), RoundedCornerShape(50)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = ElectricViolet.text)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                session.title.ifBlank { "Без названия" },
                color = ElectricViolet.text,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
            )
            val whenRecorded = remember(session.createdAt) {
                SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(session.createdAt))
            }
            val duration = "${session.durationMs / 60_000}:${(session.durationMs / 1000 % 60).toString().padStart(2, '0')}"
            Text(
                "$whenRecorded  ·  $duration",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                style = MaterialTheme.typography.labelSmall,
            )
        }
        Box(
            modifier = Modifier.border(1.dp, ElectricViolet.accent, RoundedCornerShape(50))
                .padding(horizontal = 8.dp, vertical = 5.dp),
        ) {
            Text(
                "${session.totalWords} слов",
                color = ElectricViolet.text,
                maxLines = 1,
                style = MaterialTheme.typography.labelSmall,
            )
        }
        Box {
            IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(30.dp)) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "Меню записи",
                    tint = ElectricViolet.text,
                    modifier = Modifier.size(19.dp),
                )
            }
            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                DropdownMenuItem(text = { Text("Открыть") }, onClick = {
                    menuExpanded = false
                    onOpen()
                })
                DropdownMenuItem(text = { Text("Вся история") }, onClick = {
                    menuExpanded = false
                    onOpenHistory()
                })
            }
        }
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
    val shape = RoundedCornerShape(28.dp)
    val gradient = Brush.horizontalGradient(
        listOf(ElectricViolet.accent, ElectricViolet.violet, Color(0xFF6517EF)),
    )
    Box(
        modifier = modifier
            .height(60.dp)
            .then(
                if (primary) Modifier.background(gradient, shape)
                else Modifier.border(
                    1.5.dp,
                    Brush.horizontalGradient(listOf(ElectricViolet.accent, ElectricViolet.neonBlue)),
                    shape,
                ).background(ElectricViolet.background, shape)
            )
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = ElectricViolet.text, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(10.dp))
            Text(label, color = ElectricViolet.text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
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
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
