package app.slovami.ai.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Shared visual tokens for the Electric Violet A1 redesign. */
object ElectricViolet {
    val background = Color(0xFF0B0B12)
    val violet = Color(0xFF7B2FF7)
    val neonBlue = Color(0xFF4C6EFF)
    val accent = Color(0xFFD66BFF)
    val text = Color(0xFFE9E6FF)
    val card = Color(0xFF171226)
    val cardBorder = Color(0xFF3C2859)

    val primaryGradient: Brush
        get() = Brush.horizontalGradient(listOf(accent, violet, neonBlue))

    val cardGradient: Brush
        get() = Brush.linearGradient(listOf(Color(0xFF24113D), card))
}

@Composable
fun ElectricVioletCard(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .background(ElectricViolet.cardGradient, RoundedCornerShape(18.dp))
            .border(1.dp, ElectricViolet.cardBorder, RoundedCornerShape(18.dp))
            .padding(16.dp),
        content = content,
    )
}

@Composable
fun ElectricVioletPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val shape = RoundedCornerShape(18.dp)
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .defaultMinSize(minHeight = 54.dp)
            .background(
                if (enabled) ElectricViolet.primaryGradient
                else Brush.horizontalGradient(listOf(Color.DarkGray, Color.Gray)),
                shape,
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
        ),
        shape = shape,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = ElectricViolet.text,
        )
    }
}
