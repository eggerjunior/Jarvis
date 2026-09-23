package br.app.egger.jarvis.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val CyanPrimary = Color(0xFF00E5FF)
val CyanSecondary = Color(0xFF00B0FF)
val DarkBackground = Color(0xFF000000)
val DarkGradientMid = Color(0xFF051721)
val CardBackground = Color(0x0Fffffff) // white.opacity(0.06)
val CardBorder = Color(0x3300E5FF) // cyan.opacity(0.2)

private val DarkColorScheme = darkColorScheme(
    primary = CyanPrimary,
    secondary = CyanSecondary,
    background = DarkBackground,
    surface = DarkBackground,
    onPrimary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun JarvisTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
