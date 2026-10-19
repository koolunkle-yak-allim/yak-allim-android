package io.github.koolunkle.yakallim.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 60-30-10: background/surface(60) · onSurface/onSurfaceVariant/outline(30) · primary(10).
// secondary·tertiary는 쓰지 않으므로 강조색이 늘어나지 않게 primary 계열로 묶어 둔다.
private val LightColorScheme = lightColorScheme(
    primary = YakPalette.Teal,
    onPrimary = YakPalette.OnTeal,
    primaryContainer = YakPalette.TealSoft,
    onPrimaryContainer = YakPalette.Teal,
    secondary = YakPalette.Teal,
    onSecondary = YakPalette.OnTeal,
    secondaryContainer = YakPalette.TealSoft,
    onSecondaryContainer = YakPalette.Teal,
    tertiary = YakPalette.Teal,
    onTertiary = YakPalette.OnTeal,
    background = YakPalette.Canvas,
    onBackground = YakPalette.Ink,
    surface = YakPalette.Surface,
    onSurface = YakPalette.Ink,
    surfaceVariant = YakPalette.Sunken,
    onSurfaceVariant = YakPalette.InkMuted,
    surfaceContainerLowest = YakPalette.Surface,
    surfaceContainerLow = YakPalette.Surface,
    surfaceContainer = YakPalette.Surface,
    surfaceContainerHigh = YakPalette.Surface,
    surfaceContainerHighest = YakPalette.Sunken,
    outline = YakPalette.Line,
    outlineVariant = YakPalette.Line,
    error = YakPalette.Danger,
    onError = YakPalette.OnTeal,
    errorContainer = YakPalette.DangerSoft,
    onErrorContainer = YakPalette.Danger,
)

private val DarkColorScheme = darkColorScheme(
    primary = YakPalette.TealDark,
    onPrimary = YakPalette.OnTealDark,
    primaryContainer = YakPalette.TealSoftDark,
    onPrimaryContainer = YakPalette.TealDark,
    secondary = YakPalette.TealDark,
    onSecondary = YakPalette.OnTealDark,
    secondaryContainer = YakPalette.TealSoftDark,
    onSecondaryContainer = YakPalette.TealDark,
    tertiary = YakPalette.TealDark,
    onTertiary = YakPalette.OnTealDark,
    background = YakPalette.CanvasDark,
    onBackground = YakPalette.InkDark,
    surface = YakPalette.SurfaceDark,
    onSurface = YakPalette.InkDark,
    surfaceVariant = YakPalette.SunkenDark,
    onSurfaceVariant = YakPalette.InkMutedDark,
    surfaceContainerLowest = YakPalette.SurfaceDark,
    surfaceContainerLow = YakPalette.SurfaceDark,
    surfaceContainer = YakPalette.SurfaceDark,
    surfaceContainerHigh = YakPalette.SurfaceDark,
    surfaceContainerHighest = YakPalette.SunkenDark,
    outline = YakPalette.LineDark,
    outlineVariant = YakPalette.LineDark,
    error = YakPalette.DangerDark,
    onError = YakPalette.CanvasDark,
    errorContainer = YakPalette.DangerSoftDark,
    onErrorContainer = YakPalette.DangerDark,
)

@Composable
fun YakAllimTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val statusColors = if (darkTheme) DarkStatusColors else LightStatusColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalYakStatusColors provides statusColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}

/** MaterialTheme에 없는 디자인 토큰에 접근하는 진입점. */
object YakTheme {
    val status: YakStatusColors
        @Composable @ReadOnlyComposable
        get() = LocalYakStatusColors.current
}
