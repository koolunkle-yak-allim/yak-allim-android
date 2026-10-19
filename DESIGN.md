---
name: yak-allim
version: 1
colors:
  light:
    canvas: "#F6F8F9"        # colorScheme.background
    surface: "#FFFFFF"       # colorScheme.surface
    surfaceSunken: "#EEF3F5" # colorScheme.surfaceVariant
    ink: "#1B2328"           # colorScheme.onSurface
    inkMuted: "#56626B"      # colorScheme.onSurfaceVariant
    line: "#DDE4E8"          # colorScheme.outline
    accent: "#0A6F73"        # colorScheme.primary
    onAccent: "#FFFFFF"      # colorScheme.onPrimary
    accentSoft: "#E8F5F5"    # colorScheme.primaryContainer
    success: "#1E7A34"       # YakTheme.status.success
    successSoft: "#E6F4EA"   # YakTheme.status.successContainer
    warning: "#9A5B00"       # YakTheme.status.warning
    warningSoft: "#FFF3DC"   # YakTheme.status.warningContainer
    danger: "#B42318"        # colorScheme.error
    dangerSoft: "#FDECEA"    # colorScheme.errorContainer
  dark:
    canvas: "#11181C"
    surface: "#1A2329"
    surfaceSunken: "#222D34"
    ink: "#E6ECEF"
    inkMuted: "#9AA7AF"
    line: "#2C3840"
    accent: "#4FC3C0"
    onAccent: "#06302F"
    accentSoft: "#16302F"
    success: "#7DD3A0"
    successSoft: "#14261C"
    warning: "#FFD08A"
    warningSoft: "#2A2214"
    danger: "#F2A99A"
    dangerSoft: "#2E1715"
typography:
  family: { body: "Pretendard", wordmark: "Katuri" }
  display:   { slot: headlineLarge, size: 28, lineHeight: 36, weight: 700 }
  title:     { slot: titleLarge,    size: 20, lineHeight: 28, weight: 700 }
  subtitle:  { slot: titleMedium,   size: 17, lineHeight: 24, weight: 700 }
  body:      { slot: bodyLarge,     size: 16, lineHeight: 24, weight: 400 }
  bodySmall: { slot: bodyMedium,    size: 14, lineHeight: 20, weight: 400 }
  label:     { slot: labelLarge,    size: 15, lineHeight: 20, weight: 600 }
  caption:   { slot: labelSmall,    size: 12, lineHeight: 16, weight: 600 }
  numeral:   { token: NumeralTextStyle, size: 20, lineHeight: 28, weight: 700, features: tnum }
  wordmark:  { token: WordmarkTextStyle, size: 20, lineHeight: 28, weight: 700 }
spacing: [4, 8, 12, 16, 20, 24, 32]
rounded: { input: 8, button: 12, card: 16, sheet: 24, chip: full }
touchTarget: { min: 48, primaryButton: 52 }
---

# 약올림 디자인 시스템

코드의 기준은 `app/src/main/java/io/github/koolunkle/yakallim/ui/theme/`입니다.
이 문서와 코드가 다르면 코드를 고치고 이 문서도 함께 고칩니다.

## 1. 컨셉

**약 봉투처럼 정확하고 또렷하게.** 흰 바탕과 진한 숫자, 청록 한 가지로 복용법을 틀림없이 전달합니다.

약 봉투의 "1회 1정 · 1일 3회 · 5일분" 표기를 화면의 기본 문법으로 삼습니다. 가장 먼저 읽혀야 하는 것은 숫자이고, 색은 상태와 다음 행동을 알리는 데만 씁니다.

## 2. 무드 키워드

| 키워드 | 화면에서의 의미 |
| --- | --- |
| 클리니컬 클린 | 약국 조제실처럼 정돈된 흰 면. 장식 요소 없음 |
| 소프트 미니멀 | 둥근 모서리, 그림자 대신 1dp 선 |
| 큰 숫자 가독성 | 용량·횟수·시각이 화면에서 가장 큰 글자 |
| 차분한 신뢰 | 저채도 청록 한 가지만 강조색으로 사용 |

## 3. 컬러

### 60-30-10

| 비율 | 역할 | 토큰 |
| --- | --- | --- |
| 60% | 바탕 | `background`(canvas), `surface` |
| 30% | 글자·구조 | `onSurface`(ink), `onSurfaceVariant`(inkMuted), `outline`(line) |
| 10% | 강조 | `primary`(accent): 주 버튼, 선택, 포커스 |

- 의미 색(`YakTheme.status.*`, `error`)은 상태 표시에만 쓰고 위 비율에 넣지 않습니다.
- `secondary`·`tertiary`는 쓰지 않습니다. Material 기본 컴포넌트가 다른 강조색을 쓰지 않도록 `primary` 계열로 맞춰 두었습니다.

### 대비 (WCAG, 글자 4.5:1 이상)

| 조합 | 대비 |
| --- | --- |
| `onPrimary` / `primary` (라이트) | 5.94:1 |
| `primary` / `primaryContainer` (라이트) | 5.32:1 |
| `success` / `successContainer` (라이트) | 4.76:1 |
| `warning` / `warningContainer` (라이트) | 4.94:1 |
| `error` / `surface` (라이트) | 6.57:1 |
| `onSurfaceVariant` / `surface` (라이트) | 6.26:1 |
| `primary` / `surface` (다크) | 7.52:1 |
| `onSurface` / `surface` (다크) | 13.38:1 |
| `inversePrimary` / `inverseSurface` (스낵바 동작 글자, 라이트) | 7.51:1 |
| `inversePrimary` / `inverseSurface` (스낵바 동작 글자, 다크) | 4.98:1 |

이전 색은 흰 글자 / `#00ACC1` 2.74:1, 흰 글자 / `#FF8A65` 2.31:1로 기준에 못 미쳤습니다.

### 쓰는 법

```kotlin
MaterialTheme.colorScheme.primary          // 강조
MaterialTheme.colorScheme.onSurfaceVariant // 보조 설명
YakTheme.status.successContainer           // 알람 켜짐 바탕
```

컴포넌트 안에서 `Color(0xFF…)`, `Color.White`를 쓰지 않습니다. 예외는 카메라 화면 하나입니다. 카메라 영상 위에는 테마와 관계없이 어두운 덮개와 흰 아이콘을 씁니다.

## 4. 타이포

- 글꼴은 두 개입니다. 본문은 **Pretendard**(400·600·700, `res/font/pretendard_*.ttf`), 앱 이름(워드마크)은 **카투리체**입니다. 카투리체는 본문과 숫자에 쓰지 않습니다.
- Material 3의 모든 글자 슬롯을 `Typography`에서 정의합니다. 정의하지 않은 슬롯은 기기 기본 글꼴로 그려지기 때문입니다.
- 숫자를 세로로 맞춰야 하는 곳(알람 시각, 스테퍼)은 `NumeralTextStyle`(고정폭 숫자 `tnum`)을 씁니다.
- 본문은 16sp 이상입니다. 가장 작은 글자는 caption 12sp입니다.
- 글자를 감싸는 요소에 고정 높이를 주지 않습니다. 글자 크기를 200%까지 키워도 잘리지 않도록 최소 높이만 지정합니다.

| 토큰 | Material 슬롯 | 크기 / 줄높이 | 굵기 | 용도 |
| --- | --- | --- | --- | --- |
| display | `headlineLarge` | 28 / 36sp | 700 | 결과 요약 숫자 |
| title | `titleLarge` | 20 / 28sp | 700 | 시트·화면 제목 |
| subtitle | `titleMedium` | 17 / 24sp | 700 | 약 이름 |
| body | `bodyLarge` | 16 / 24sp | 400 | 본문, 복용 정보 |
| bodySmall | `bodyMedium` | 14 / 20sp | 400 | 보조 설명, 안내 배너 |
| label | `labelLarge` | 15 / 20sp | 600 | 버튼, 칩 |
| caption | `labelSmall` | 12 / 16sp | 600 | 메타 정보 |
| numeral | `NumeralTextStyle` | 20 / 28sp | 700 | 시각, 스테퍼 숫자 |
| wordmark | `WordmarkTextStyle` | 20 / 28sp | 700 | 상단바 앱 이름. 왼쪽 정렬, `onSurface` |

### 여백·모양

| 구분 | 값 | 규칙 |
| --- | --- | --- |
| 간격 | 4 · 8 · 12 · 16 · 20 · 24 · 32dp | 4dp 배수만 사용. 화면 좌우 20, 카드 안쪽 16, 카드 사이 12, 섹션 사이 24 |
| 모서리 | `Shapes` | `small` 8(입력칸), `medium` 12(버튼), `large` 16(카드), `extraLarge` 24(시트), 칩은 `CircleShape` |
| 깊이 | 선 1dp | 카드는 `outline` 테두리로 구분하고 그림자를 쓰지 않음. 그림자는 하단 고정 버튼 영역에만 |
| 터치 영역 | 최소 48dp | 주 버튼 높이 52dp |

## 5. 섹션 구성

| 화면 | 구성 (위에서 아래) |
| --- | --- |
| 홈 (분석 전) | 상단바(워드마크·도움말) → 사진 영역 4:3(빈 상태: 무엇을 어떻게 찍는지 안내) → [카메라로 찍기] [사진 고르기] 테두리 버튼 → 하단 고정 [분석 시작], 사진이 있을 때만 활성 |
| 분석 중 | 사진 썸네일 → 진행 카드(애니메이션, 단계 문구, 분석 단계는 무한 진행 막대) → [분석 취소] 글자 버튼 |
| 결과 | 요약("약 N개를 찾았어요", "알람 n/N개 켜짐 · 확인 필요 n개") → 확인 안내 배너 1개 → 원본 사진(접힌 한 줄, 누르면 전체 화면) → 약 카드 목록 → [다른 사진으로 분석] 테두리 버튼 |
| 약 카드 | 약 이름 + 상태 칩(꺼짐/켜짐/확인 필요) → 복용 정보 "1회 n정 · 1일 n회 · n일분" → (켜짐) 알람 시각 · 알람음 → 할 일 버튼 1개(오른쪽 정렬) |
| 알람 설정 시트 | 약 이름 → 복용 정보 스테퍼 3개(1회 양, 1일 횟수, 기간 + 날짜) → 복용 시각 칩(횟수만큼) → 알람음 한 줄 + 미리 듣기 → 하단 고정 주 버튼, 켜진 약이면 "알람 끄기" 글자 버튼 |
| 오류 | 제목 "분석하지 못했어요"와 원인·해결 방법을 한 카드에 → 하단 고정 [다시 시도] |
| 카메라 | 전체 화면, 세로 3:4 문서 가이드 테두리와 안내 문구, 셔터 72dp, 닫기 48dp |

- 복용 정보의 순서는 약 봉투 표기(1회 → 1일 → 일분)를 따르고, 카드와 입력 화면에서 같은 순서를 씁니다.
- 값이 비어 있으면(사진에 숫자가 없어 서버가 `null`을 보낸 경우) 그 자리에 "?"와 `warning` 색을 쓰고, 버튼은 "복용량 입력"처럼 할 일을 말합니다.

## 6. 인터랙션

| 상황 | 동작 | 시간·곡선 |
| --- | --- | --- |
| 버튼·카드 누름 | Material ripple, 상태층 8% | 기본값 |
| 카드 → 알람 설정 | `ModalBottomSheet`, 배경 어둡게 32% | 300ms · emphasized decelerate |
| 알람 켜짐/꺼짐 전환 | 상태 칩 crossfade, 다음 알람 줄 펼침 | 150ms · standard |
| 알람 켜기 완료 | 가벼운 진동(CONFIRM) + 스낵바 "○○ 알람을 켰어요" | `SnackbarDuration.Short`(4초) |
| 알람 끄기 | 즉시 끄고 스낵바에 [실행 취소]. 누르면 끄기 전 시각·알람음으로 다시 켬 | `SnackbarDuration.Long`(10초) |
| 다른 사진으로 분석 | 켜 둔 알람이 있으면 "켜 둔 알람 n개도 함께 꺼져요" 확인 후 진행. 없으면 바로 진행 | 기본값 |
| 스테퍼 길게 누름 | 반복 증감, 범위 밖이면 버튼 비활성 | 400ms 뒤 100ms 간격 |
| 복용 시각 칩 누름 | Material3 `TimePicker` | 기본값 |
| 분석 진행 | 분석 단계는 무한 진행 막대. 시스템의 "애니메이션 줄이기"가 켜져 있으면 정지 아이콘을 쓰고 무한 진행 막대도 숨김 | 시스템 설정 |

## 7. 레퍼런스

| 출처 | 가져온 것 |
| --- | --- |
| 약국 약 봉투 표기 | "1회 1정 · 1일 3회 · 5일분" 순서와 단위 |
| Apple 건강 앱 · 약물 | 약마다 일정과 시각을 칩으로, 편집은 시트에서 |
| 토스(TDS) | 큰 숫자, 화면당 주 버튼 하나, 하단 고정 버튼, 여러 값 입력은 바텀시트 |
| Material 3 | tonal 버튼, `ModalBottomSheet`, `TimePicker`, 상태층 규칙 |

## 8. 금지사항

- 본문·숫자에 장식 글꼴(카투리체) 사용. 워드마크만 허용
- 밝은 색 위에 흰 글자처럼 대비 4.5:1 미만인 글자
- 한 화면에 채운(primary) 버튼 두 개 이상
- 위험 동작(알람 끄기)을 채운 버튼이나 강조색으로 표시하는 것, 실행 취소 없는 즉시 삭제
- 상태를 색만으로 구분하는 것. 반드시 글자(칩)와 함께
- 컴포넌트 안에서 `Color(0xFF…)`, `Color.White` 직접 사용 (카메라 화면 제외)
- 그라데이션, 글래스모피즘, 카드 그림자
- UI 문구 안의 이모지 (⏰ 등)
- 여러 값을 입력받는 폼을 `AlertDialog`에 넣는 것. 바텀시트로
- 글자를 감싸는 고정 높이
- 4dp 배수가 아닌 간격
- 사진에 없는 값을 화면에서 기본값으로 채우는 것. 빈 값은 "확인 필요"로 보여 줌

## 라이선스

Pretendard는 SIL Open Font License 1.1로 배포됩니다. 원문은 [`licenses/Pretendard-OFL.txt`](licenses/Pretendard-OFL.txt)에 있습니다.
