package app.slovami.ai.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()).padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.height(12.dp))
        Text(
            buildAnnotatedString {
                append("От ")
                withStyle(SpanStyle(color = ElectricViolet.violet)) { append("записи") }
                append(" до ")
                withStyle(SpanStyle(color = ElectricViolet.neonBlue)) { append("готового текста") }
                append(".")
            },
            style = MaterialTheme.typography.headlineMedium,
            color = ElectricViolet.text,
            fontWeight = FontWeight.Bold,
        )
        Text(
            "Запишите разговор или загрузите аудиофайл. Slovami AI распознает речь, разделит её по участникам и поможет выделить главное.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        ElectricVioletCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Spacer(Modifier.height(6.dp))
                // The microphone and its label are a single, accessible recording action.
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(role = Role.Button, onClick = onStartRecording)
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(168.dp)
                            .border(
                                width = 1.dp,
                                color = ElectricViolet.violet.copy(alpha = 0.25f),
                                shape = CircleShape,
                            )
                            .padding(10.dp)
                            .border(
                                width = 1.dp,
                                color = ElectricViolet.accent.copy(alpha = 0.45f),
                                shape = CircleShape,
                            )
                            .padding(10.dp)
                            .background(ElectricViolet.primaryGradient, CircleShape)
                            .padding(6.dp)
                            .background(
                                Brush.radialGradient(
                                    listOf(Color(0xFF39105C), ElectricViolet.card),
                                ),
                                CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.Mic,
                            contentDescription = null,
                            tint = ElectricViolet.text,
                            modifier = Modifier.size(64.dp),
                        )
                    }
                    Text(
                        "Начать запись",
                        style = MaterialTheme.typography.titleLarge,
                        color = ElectricViolet.text,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .background(ElectricViolet.card, RoundedCornerShape(14.dp))
                        .clickable(onClick = onImportAudio)
                        .padding(vertical = 15.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.AudioFile, contentDescription = null, tint = ElectricViolet.accent)
                    Spacer(Modifier.width(10.dp))
                    Text("Импортировать аудио", color = ElectricViolet.text, fontWeight = FontWeight.Medium)
                }
            }
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
