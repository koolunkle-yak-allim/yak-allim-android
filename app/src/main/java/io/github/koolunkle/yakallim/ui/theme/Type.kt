package io.github.koolunkle.yakallim.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import io.github.koolunkle.yakallim.R

/** 화면 전체에 쓰는 본문 글꼴. 굵기는 400·600·700만 넣었고, 그 사이 값은 가장 가까운 굵기로 그려진다. */
val PretendardFamily = FontFamily(
    Font(R.font.pretendard_regular, FontWeight.Normal),
    Font(R.font.pretendard_semibold, FontWeight.SemiBold),
    Font(R.font.pretendard_bold, FontWeight.Bold),
)

/** 앱 이름(워드마크)에만 쓰는 장식 글꼴. 본문·숫자에는 쓰지 않는다. */
val KaturiFamily = FontFamily(Font(R.font.katuri, FontWeight.Bold))

private fun style(size: Int, lineHeight: Int, weight: FontWeight) = TextStyle(
    fontFamily = PretendardFamily,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = 0.sp,
)

// DESIGN.md의 타입 토큰과 Material 3 슬롯의 대응:
// display=headlineLarge, title=titleLarge, subtitle=titleMedium, body=bodyLarge,
// bodySmall=bodyMedium, label=labelLarge, caption=labelSmall.
// 모든 슬롯을 정의해, 정의되지 않은 슬롯이 기기 기본 글꼴로 그려지지 않게 한다.
val Typography = Typography(
    displayLarge = style(32, 40, FontWeight.Bold),
    displayMedium = style(28, 36, FontWeight.Bold),
    displaySmall = style(24, 32, FontWeight.Bold),
    headlineLarge = style(28, 36, FontWeight.Bold),
    headlineMedium = style(24, 32, FontWeight.Bold),
    headlineSmall = style(22, 30, FontWeight.Bold),
    titleLarge = style(20, 28, FontWeight.Bold),
    titleMedium = style(17, 24, FontWeight.Bold),
    titleSmall = style(15, 22, FontWeight.SemiBold),
    bodyLarge = style(16, 24, FontWeight.Normal),
    bodyMedium = style(14, 20, FontWeight.Normal),
    bodySmall = style(13, 18, FontWeight.Normal),
    labelLarge = style(15, 20, FontWeight.SemiBold),
    labelMedium = style(13, 18, FontWeight.SemiBold),
    labelSmall = style(12, 16, FontWeight.SemiBold),
)

/** 알람 시각·복용량처럼 세로로 줄을 맞춰야 하는 숫자. 고정폭 숫자(tnum)를 쓴다. */
val NumeralTextStyle = style(20, 28, FontWeight.Bold).copy(fontFeatureSettings = "tnum")

/** 상단바의 앱 이름. */
val WordmarkTextStyle = TextStyle(
    fontFamily = KaturiFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 22.sp,
    lineHeight = 28.sp,
)
