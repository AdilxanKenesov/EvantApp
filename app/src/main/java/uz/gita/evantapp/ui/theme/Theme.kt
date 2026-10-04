package uz.gita.evantapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Background = Color(0xFF0B0D10)
val Surface = Color(0xFF15181D)
val SurfaceActive = Color(0xFF0F2A22)
val Accent = Color(0xFF00E5A0)
val TextPrimary = Color(0xFFE8EAED)
val TextMuted = Color(0xFF6B7280)

// Faqat night mode: light scheme va dynamic color ataylab yo'q
private val DarkColors = darkColorScheme(
    primary = Accent,
    onPrimary = Background,
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
)

@Composable
fun EvantAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = Typography,
        content = content
    )
}
