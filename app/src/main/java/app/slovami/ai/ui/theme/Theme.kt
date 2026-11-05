package app.slovami.ai.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import android.app.Activity

/**
 * Постоянная тёмная тема Slovami AI; системная светлая тема не применяется.
 */


/** Brand blue — основной для primary actions, активных элементов. */
val BrandBlue = Color(0xFF4C6EFF)
/** Brand purple — для accent / secondary. */
val BrandPurple = Color(0xFF7B2FF7)
/** Brand cyan — для подсветки live-элементов (recording dot, waveform). */
val BrandCyan = Color(0xFF4C6EFF)
/** Recording red — только для recording state. */
val RecordingRed = Color(0xFFEF4444)


/** Самый тёмный — background всего приложения. */
private val Surface0 = Color(0xFF0B0B12)
/** Surface — контейнеры, карточки нижнего уровня. */
private val Surface1 = Color(0xFF171226)
/** SurfaceContainer — карточки сегментов. */
private val Surface2 = Color(0xFF231A36)
/** SurfaceContainerHigh — поднятые элементы (modal sheets, dialogs). */
private val Surface3 = Color(0xFF322344)
/** SurfaceContainerHighest — кнопки на тёмном, вспомогательные. */
private val Surface4 = Color(0xFF43315D)


/** Основной текст — почти-белый. */
private val TextPrimary = Color(0xFFE9E6FF)
/** Вторичный текст — для подсказок, меток. */
private val TextSecondary = Color(0xFFB5ADC9)
/** Третичный — отключённое, edge cases. */
private val TextTertiary = Color(0xFF84799F)


private val SlovamiAIColorScheme = darkColorScheme(
    primary             = BrandPurple,
    onPrimary           = Color.White,
    primaryContainer    = Color(0xFF4B168B),    // тёмно-синий для container'ов
    onPrimaryContainer  = Color(0xFFF3E8FF),

    secondary           = BrandBlue,
    onSecondary         = Color.White,
    secondaryContainer  = Color(0xFF253F95),
    onSecondaryContainer= Color(0xFFE1E8FF),

    tertiary            = BrandCyan,
    onTertiary          = Color.White,
    tertiaryContainer   = Color(0xFF2840B7),
    onTertiaryContainer = Color(0xFFE1E8FF),

    error               = RecordingRed,
    onError             = Color.White,
    errorContainer      = Color(0xFF991B1B),
    onErrorContainer    = Color(0xFFFEE2E2),

    background          = Surface0,
    onBackground        = TextPrimary,
    surface             = Surface1,
    onSurface           = TextPrimary,
    surfaceVariant      = Surface2,
    onSurfaceVariant    = TextSecondary,
    surfaceContainer    = Surface2,
    surfaceContainerLow = Surface1,
    surfaceContainerHigh= Surface3,
    surfaceContainerHighest = Surface4,

    outline             = Color(0xFF334155),
    outlineVariant      = Color(0xFF1E293B),

    inverseSurface      = TextPrimary,
    inverseOnSurface    = Surface0,
    inversePrimary      = Color(0xFF1E40AF),

    scrim               = Color(0xFF000000),
)


/** Дефолтный шрифт Roboto — без custom-fonts (минимальный APK).
 *  Roboto на современных Android выглядит чисто и нейтрально. */
private val SlovamiAITypography = Typography(
    displayLarge = TextStyle(
        fontSize = 48.sp,
        lineHeight = 56.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.5).sp,
    ),
    displayMedium = TextStyle(
        fontSize = 36.sp,
        lineHeight = 44.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.25).sp,
    ),

    headlineLarge = TextStyle(
        fontSize = 28.sp,
        lineHeight = 36.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    headlineMedium = TextStyle(
        fontSize = 24.sp,
        lineHeight = 32.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    headlineSmall = TextStyle(
        fontSize = 20.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.SemiBold,
    ),

    titleLarge = TextStyle(
        fontSize = 18.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    titleMedium = TextStyle(
        fontSize = 16.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.1.sp,
    ),
    titleSmall = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.1.sp,
    ),

    bodyLarge = TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.15.sp,
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.15.sp,
    ),
    bodySmall = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.2.sp,
    ),

    labelLarge = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.5.sp,
    ),
    labelSmall = TextStyle(
        fontSize = 11.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.5.sp,
    ),
)


private val SlovamiAIShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small      = RoundedCornerShape(8.dp),
    medium     = RoundedCornerShape(12.dp),
    large      = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
)


@Composable
fun InterviewTheme(
    content: @Composable () -> Unit,
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Edge-to-edge: статус-бар прозрачный, иконки светлые (тёмная тема)
            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }
    MaterialTheme(
        colorScheme = SlovamiAIColorScheme,
        typography  = SlovamiAITypography,
        shapes      = SlovamiAIShapes,
        content     = content,
    )
}
