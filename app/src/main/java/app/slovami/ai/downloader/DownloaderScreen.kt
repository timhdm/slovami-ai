package app.slovami.ai.downloader

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import app.slovami.ai.ui.theme.ElectricViolet
import app.slovami.ai.ui.theme.ElectricVioletCard
import app.slovami.ai.ui.theme.ElectricVioletPrimaryButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.slovami.ai.R
import app.slovami.ai.ui.theme.BrandBlue
import app.slovami.ai.ui.theme.BrandPurple
import app.slovami.ai.ui.theme.RecordingRed

/**
 * Экран скачивания моделей при первом запуске.
 *
 * Состояния (соответствуют DownloaderViewModel.Phase):
 *  - CHECKING        : показываем спиннер "Проверяем модели..."
 *  - NEEDS_DOWNLOAD  : welcome-экран с описанием и кнопкой "Скачать"
 *  - DOWNLOADING     : прогресс-бар + текущая скорость + кнопка "Отмена"
 *  - SUCCESS         : кратко "Готово!" + onComplete()
 *  - ERROR           : описание ошибки + кнопки "Повторить" / "Закрыть"
 *
 * @param onComplete — вызывается когда все модели скачаны (переход на главный экран).
 */
@Composable
fun DownloaderScreen(
    vm: DownloaderViewModel = viewModel(),
    onComplete: () -> Unit,
    onExit: () -> Unit = {},
) {
    val phase by vm.phase.collectAsStateWithLifecycle()
    val progress by vm.progress.collectAsStateWithLifecycle()
    val bytesDownloaded by vm.bytesDownloaded.collectAsStateWithLifecycle()
    val bytesTotal by vm.bytesTotal.collectAsStateWithLifecycle()
    val speedBps by vm.speedBps.collectAsStateWithLifecycle()
    val currentModelId by vm.currentModelId.collectAsStateWithLifecycle()
    val currentModelBytes by vm.currentModelBytes.collectAsStateWithLifecycle()
    val errorMessage by vm.errorMessage.collectAsStateWithLifecycle()

    // Состояние диалога подтверждения выхода
    var showExitDialog by remember { mutableStateOf(false) }

    // Состояние menu выбора языка
    var showLanguageMenu by remember { mutableStateOf(false) }
    val ctx = androidx.compose.ui.platform.LocalContext.current

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text(stringResource(R.string.downloader_exit_dialog_title)) },
            text = { Text(stringResource(R.string.downloader_exit_dialog_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showExitDialog = false
                    onExit()
                }) {
                    Text(stringResource(R.string.downloader_exit_dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text(stringResource(R.string.downloader_exit_dialog_dismiss))
                }
            },
        )
    }

    // Авто-переход когда модели уже скачаны при старте
    LaunchedEffect(phase) {
        if (phase == DownloaderViewModel.Phase.SUCCESS) {
            onComplete()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = ElectricViolet.background,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Основной контент (по центру)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = if (phase == DownloaderViewModel.Phase.DOWNLOADING) 20.dp else 76.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                BrandHeader()
                Spacer(Modifier.height(if (phase == DownloaderViewModel.Phase.DOWNLOADING) 26.dp else 42.dp))
                when (phase) {
                    DownloaderViewModel.Phase.CHECKING -> CheckingState()
                    DownloaderViewModel.Phase.NEEDS_DOWNLOAD -> {
                        // Читаем выбранный язык из prefs чтобы показать правильный размер скачивания.
                        // Обновляется при смене языка через LaunchedEffect (Activity пересоздаётся).
                        val selectedLang = remember {
                            val prefs = ctx.getSharedPreferences("interview_prefs", android.content.Context.MODE_PRIVATE)
                            prefs.getString("asr_language", "RU") ?: "RU"
                        }
                        WelcomeState(
                            totalBytes = ModelRegistry.firstRunTotalBytesFor(selectedLang),
                            onStart = { vm.startFirstRunDownload() },
                        )
                    }
                    DownloaderViewModel.Phase.DOWNLOADING -> DownloadingState(
                        progress = progress,
                        bytesDownloaded = bytesDownloaded,
                        bytesTotal = bytesTotal,
                        speedBps = speedBps,
                        currentModelId = currentModelId,
                        currentModelBytes = currentModelBytes,
                        onCancel = { vm.cancel() },
                    )
                    DownloaderViewModel.Phase.SUCCESS -> SuccessState()
                    DownloaderViewModel.Phase.ERROR -> ErrorState(
                        message = errorMessage,
                        onRetry = { vm.retry() },
                    )
                }
            }

            // Кнопки в правом верхнем углу: язык + выход
            if (phase != DownloaderViewModel.Phase.SUCCESS && phase != DownloaderViewModel.Phase.DOWNLOADING) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                ) {
                    // Кнопка смены языка (иконка глобуса)
                    Box {
                        IconButton(onClick = { showLanguageMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Language",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        DropdownMenu(
                            expanded = showLanguageMenu,
                            onDismissRequest = { showLanguageMenu = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("Русский") },
                                onClick = {
                                    showLanguageMenu = false
                                    setDownloaderLanguage(ctx, "RU", "ru")
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("English") },
                                onClick = {
                                    showLanguageMenu = false
                                    setDownloaderLanguage(ctx, "EN", "en")
                                },
                            )
                        }
                    }
                    // Кнопка выхода
                    IconButton(onClick = { showExitDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.downloader_exit),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun BrandHeader() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.GraphicEq, contentDescription = null, tint = ElectricViolet.accent, modifier = Modifier.size(34.dp))
        Spacer(Modifier.width(10.dp))
        Text("Slovami ", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Text("AI", color = ElectricViolet.accent, fontSize = 23.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CheckingState() {
    CircularProgressIndicator(color = ElectricViolet.accent)
    Spacer(Modifier.height(18.dp))
    Text(stringResource(R.string.downloader_checking), color = ElectricViolet.text)
}


@Composable
private fun ModelDownloadList(
    models: List<ModelRegistry.Model>,
    currentModelId: String? = null,
    currentModelBytes: Long = 0L,
    showProgress: Boolean = false,
) {
    ElectricVioletCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            models.forEachIndexed { index, model ->
                val label = when (model.id) {
                    "asr_gigaam_v3" -> "GigaAM v3 · распознавание речи"
                    "embedding_camplus" -> "CAM++ · разделение голосов"
                    "asr_en_encoder" -> "Whisper · кодировщик"
                    "asr_en_decoder" -> "Whisper · декодер"
                    "asr_en_tokens" -> "Whisper · словарь"
                    else -> model.filename
                }
                val active = showProgress && currentModelId == model.id
                val done = showProgress && currentModelId != null &&
                    models.indexOfFirst { it.id == currentModelId } > index
                val amount = when {
                    done -> model.sizeBytes
                    active -> currentModelBytes.coerceIn(0L, model.sizeBytes)
                    else -> 0L
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(label, color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1f))
                    Spacer(Modifier.width(8.dp))
                    Text(formatBytes(model.sizeBytes), color = ElectricViolet.text, fontSize = 13.sp)
                }
                if (showProgress) {
                    LinearProgressIndicator(
                        progress = { (amount.toFloat() / model.sizeBytes.coerceAtLeast(1)).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(5.dp)),
                        color = ElectricViolet.accent,
                        trackColor = ElectricViolet.cardBorder,
                    )
                    if (active) Text("${formatBytes(amount)} / ${formatBytes(model.sizeBytes)}", color = ElectricViolet.text, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun WelcomeState(totalBytes: Long, onStart: () -> Unit) {
    Icon(Icons.Default.CloudDownload, contentDescription = null, tint = ElectricViolet.accent, modifier = Modifier.size(130.dp))
    Spacer(Modifier.height(28.dp))
    Text(stringResource(R.string.downloader_welcome_title), color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    Spacer(Modifier.height(16.dp))
    Text(stringResource(R.string.downloader_welcome_message), color = ElectricViolet.text, fontSize = 15.sp, lineHeight = 22.sp, textAlign = TextAlign.Center)
    Spacer(Modifier.height(26.dp))
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val language = remember(ctx) {
        ctx.getSharedPreferences("interview_prefs", android.content.Context.MODE_PRIVATE)
            .getString("asr_language", "RU")
    }
    Text("Модели для загрузки", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(12.dp))
    ModelDownloadList(models = ModelRegistry.firstRunModelsFor(language))
    Spacer(Modifier.height(12.dp))
    Text("Всего: ${formatBytes(totalBytes)}", color = ElectricViolet.text, fontSize = 14.sp)
    Spacer(Modifier.height(26.dp))
    ElectricVioletPrimaryButton(text = stringResource(R.string.downloader_start_action), onClick = onStart, modifier = Modifier.fillMaxWidth())
}

@Composable
private fun DownloadingState(
    progress: Float,
    bytesDownloaded: Long,
    bytesTotal: Long,
    speedBps: Long,
    currentModelId: String?,
    currentModelBytes: Long,
    onCancel: () -> Unit,
) {
    Text("Загрузка моделей", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    Spacer(Modifier.height(22.dp))
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val language = remember(ctx) {
        ctx.getSharedPreferences("interview_prefs", android.content.Context.MODE_PRIVATE)
            .getString("asr_language", "RU")
    }
    ModelDownloadList(
        models = ModelRegistry.firstRunModelsFor(language),
        currentModelId = currentModelId,
        currentModelBytes = currentModelBytes,
        showProgress = true,
    )
    Spacer(Modifier.height(20.dp))
    Text("Загружено: ${(progress * 100).toInt()} %", color = ElectricViolet.text, fontSize = 15.sp)
    Spacer(Modifier.height(24.dp))
    OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(22.dp), border = androidx.compose.foundation.BorderStroke(1.dp, ElectricViolet.violet)) {
        Text(stringResource(R.string.downloader_cancel), color = ElectricViolet.text)
    }
}

@Composable
private fun SuccessState() {
    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ElectricViolet.accent, modifier = Modifier.size(80.dp))
    Spacer(Modifier.height(16.dp))
    Text(stringResource(R.string.downloader_success_title), color = Color.White, fontWeight = FontWeight.Bold)
}

@Composable
private fun ErrorState(message: String?, onRetry: () -> Unit) {
    Icon(Icons.Default.CloudOff, contentDescription = null, tint = ElectricViolet.accent, modifier = Modifier.size(130.dp))
    Spacer(Modifier.height(30.dp))
    Text(stringResource(R.string.downloader_error_title), color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    Spacer(Modifier.height(18.dp))
    Text(message ?: stringResource(R.string.downloader_error_generic), color = ElectricViolet.text, textAlign = TextAlign.Center, lineHeight = 24.sp)
    Spacer(Modifier.height(28.dp))
    ElectricVioletCard(modifier = Modifier.fillMaxWidth()) {
        Text("Проверьте подключение к интернету и повторите попытку", color = ElectricViolet.text)
    }
    Spacer(Modifier.height(28.dp))
    ElectricVioletPrimaryButton(text = stringResource(R.string.downloader_retry), onClick = onRetry, modifier = Modifier.fillMaxWidth())
}

/**
 * Смена языка интерфейса на Downloader экране.
 *
 * Делает две вещи:
 *  1. Сохраняет выбор в SharedPreferences (как InterviewViewModel.setAsrLanguage)
 *  2. Вызывает AppCompatDelegate.setApplicationLocales — перезапуск Activity
 *     и перерисовка UI на новом языке.
 *
 * @param ctx Андроид Context
 * @param asrLang Имя варианта в enum AsrLanguage ("RU" / "EN")
 * @param localeTag IETF tag ("ru" / "en")
 */
private fun setDownloaderLanguage(
    ctx: android.content.Context,
    asrLang: String,
    localeTag: String,
) {
    // Сохраняем в те же prefs что использует InterviewViewModel
    val prefs = ctx.getSharedPreferences("interview_prefs", android.content.Context.MODE_PRIVATE)
    prefs.edit().putString("asr_language", asrLang).apply()

    // Переключаем язык UI — Activity будет пересоздана
    val locales = androidx.core.os.LocaleListCompat.forLanguageTags(localeTag)
    androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(locales)
}

/**
 * Форматирование байт в человекочитаемый размер.
 * 1.4 МБ / 305 МБ / 1.2 ГБ
 */
private fun formatBytes(bytes: Long): String {
    val mb = bytes / (1024.0 * 1024.0)
    val gb = mb / 1024.0
    return when {
        gb >= 1.0 -> "%.2f ГБ".format(gb)
        mb >= 10.0 -> "%.0f МБ".format(mb)
        mb >= 1.0 -> "%.1f МБ".format(mb)
        else -> "%.0f КБ".format(bytes / 1024.0)
    }
}
