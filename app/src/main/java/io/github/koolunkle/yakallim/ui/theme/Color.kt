package io.github.koolunkle.yakallim.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// 팔레트 원본 값. 컴포넌트는 이 값을 직접 쓰지 않고 MaterialTheme.colorScheme 또는 YakTheme.status를 쓴다.
// 대비는 DESIGN.md의 표 참고(글자 4.5:1 이상).
internal object YakPalette {
    // Light
    val Canvas = Color(0xFFF6F8F9)
    val Surface = Color(0xFFFFFFFF)
    val Sunken = Color(0xFFEEF3F5)
    val Ink = Color(0xFF1B2328)
    val InkMuted = Color(0xFF56626B)
    val Line = Color(0xFFDDE4E8)
    val Teal = Color(0xFF0A6F73)
    val OnTeal = Color(0xFFFFFFFF)
    val TealSoft = Color(0xFFE8F5F5)
    val Success = Color(0xFF1E7A34)
    val SuccessSoft = Color(0xFFE6F4EA)
    val Warning = Color(0xFF9A5B00)
    val WarningSoft = Color(0xFFFFF3DC)
    val Danger = Color(0xFFB42318)
    val DangerSoft = Color(0xFFFDECEA)

    // Dark
    val CanvasDark = Color(0xFF11181C)
    val SurfaceDark = Color(0xFF1A2329)
    val SunkenDark = Color(0xFF222D34)
    val InkDark = Color(0xFFE6ECEF)
    val InkMutedDark = Color(0xFF9AA7AF)
    val LineDark = Color(0xFF2C3840)
    val TealDark = Color(0xFF4FC3C0)
    val OnTealDark = Color(0xFF06302F)
    val TealSoftDark = Color(0xFF16302F)
    val SuccessDark = Color(0xFF7DD3A0)
    val SuccessSoftDark = Color(0xFF14261C)
    val WarningDark = Color(0xFFFFD08A)
    val WarningSoftDark = Color(0xFF2A2214)
    val DangerDark = Color(0xFFF2A99A)
    val DangerSoftDark = Color(0xFF2E1715)
}

/** Material colorScheme에 없는 상태 색. 상태 표시(칩·배너·켜진 카드)에만 쓰고 강조색으로 쓰지 않는다. */
@Immutable
data class YakStatusColors(
    val success: Color,
    val successContainer: Color,
    val warning: Color,
    val warningContainer: Color,
)

internal val LightStatusColors = YakStatusColors(
    success = YakPalette.Success,
    successContainer = YakPalette.SuccessSoft,
    warning = YakPalette.Warning,
    warningContainer = YakPalette.WarningSoft,
)

internal val DarkStatusColors = YakStatusColors(
    success = YakPalette.SuccessDark,
    successContainer = YakPalette.SuccessSoftDark,
    warning = YakPalette.WarningDark,
    warningContainer = YakPalette.WarningSoftDark,
)

internal val LocalYakStatusColors = staticCompositionLocalOf { LightStatusColors }
