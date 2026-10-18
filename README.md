# Yak-Allim-Android

> **복약 안내서 OCR 분석 기반 복약 관리 솔루션**

**Yak-Allim-Android**는 복약 안내서를 촬영하거나 이미지를 업로드하여 복용 방법(용법, 용량, 기간 등)을 분석하고, 이를 기반으로 복약 알람을 제공하는 안드로이드 애플리케이션입니다.

---

## Features

- **OCR Analysis**: 카메라 촬영 또는 갤러리 이미지 선택을 통해 복약 안내서의 텍스트를 인식하고 복용 정보를 추출합니다. 결과는 원본 사진과 함께 보여 주어 사용자가 직접 확인할 수 있습니다.
- **Medication Alarm**: 분석된 정보를 바탕으로 복용 횟수와 기간에 맞춰 알람을 등록합니다. 약마다 알람음을 고를 수 있고, 재부팅 후에도 알람을 다시 등록합니다.
- **Real-time Status Updates**: 분석 진행 상황은 SSE로 실시간 표시하고, 분석이 끝나거나 실패하면 Firebase Cloud Messaging(FCM)으로 알립니다. 앱이 백그라운드에 있어도 알림을 눌러 결과 화면으로 이동할 수 있고, 분석 중에 취소할 수 있습니다.
- **User Guide**: 앱의 주요 기능과 사용법을 이해할 수 있는 사용자 가이드를 제공합니다.

## Tech Stack

- **UI**: Jetpack Compose
- **Language**: Kotlin
- **Architecture**: Clean Architecture & MVVM
- **Asynchronous**: Coroutines & Flow
- **Dependency Injection**: Hilt
- **Network**: Retrofit2, OkHttp3(SSE 포함), kotlinx.serialization
- **Image/Camera**: CameraX, Coil, ExifInterface, Lottie(로딩 애니메이션)
- **Local Storage**: DataStore Preferences
- **Notification**: Firebase Cloud Messaging (FCM)
- **Security & Build**: Secrets Gradle Plugin, Network Security Config (릴리스 빌드는 아직 코드 축소·난독화(`isMinifyEnabled`)를 켜지 않았습니다)
- **Testing**: JUnit, kotlinx-coroutines-test(가상 시간), Mockito-Kotlin, Compose UI Test

## Project Structure

**Clean Architecture** 원칙에 따라 레이어를 분리하여 관심사 분리(SoC)와 확장성을 확보했습니다.

- **Domain Layer**: 비즈니스 로직의 모델, Repository/Infrastructure 인터페이스 및 UseCase 정의
- **Data Layer**: 데이터 소스(Local/Remote) 접근 로직과 인프라(Alarm, FCM, Image) 구현 및 Repository 구현체
- **UI Layer**: Jetpack Compose를 활용한 선언적 UI와 상태 관리를 위한 ViewModel

### Package Structure

```text
io.github.koolunkle.yakallim
├── data            # 데이터 및 인프라 구현체
│   ├── datasource  # 로컬(DataStore) 및 원격(API) 데이터 소스
│   ├── infrastructure # 알람, FCM, 이미지 처리 구현체
│   ├── mapper      # DTO와 도메인 모델 간 매핑
│   └── repository  # 레포지토리 인터페이스 구현체
├── di              # 의존성 주입을 위한 Hilt 모듈
├── domain          # 비즈니스 로직 및 모델
│   ├── alarm       # 알람 시각 계산 등 순수 도메인 로직(AlarmTimeCalculator)
│   ├── image       # 이미지 처리 인터페이스
│   ├── model       # 도메인 모델
│   ├── notification # 푸시 알림 옵저버 인터페이스
│   ├── repository  # 도메인 레포지토리 인터페이스
│   └── usecase     # 기능적 비즈니스 로직 단위
├── ui              # UI 컴포넌트 및 뷰모델
│   ├── alarm       # 알람 등록/취소/복구 화면 상태(AlarmViewModel)
│   ├── camera      # 카메라 및 이미지 선택 기능
│   ├── ocr         # OCR 진행 및 결과 UI (components: 화면 구성 요소, 파일당 하나)
│   └── theme       # 디자인 시스템 및 테마 정의
└── util            # Context 확장 함수(ContextExt), 권한 확인(Permission)
```

## Getting Started

### Prerequisites

- **Android Studio**: Rabbit (2026.2.1) 이상. 프로젝트가 AGP 9.4.1을 쓰기 때문에, AGP 9.2.1까지만 지원하는 이전 버전(예: 2025.3.4)에서는 Gradle Sync가 실패합니다.
- **Build**: AGP 9.4.1, Gradle 9.8.1(Wrapper), Kotlin 2.4.21
- **JDK**: Gradle 실행에는 JDK 17이 필요합니다(`gradle/gradle-daemon-jvm.properties`, 없으면 Gradle이 자동으로 내려받음). 앱 코드는 Java 11 바이트코드로 컴파일됩니다.
- **SDK**: Compile SDK 37, Target SDK 37, Min SDK 24

### Installation & Build

1. 저장소를 복제합니다:

    ```bash
    git clone https://github.com/koolunkle-yak-allim/yak-allim-android.git
    ```

2. 프로젝트 루트에 `secrets.properties` 파일을 생성하고 필요한 환경 변수를 설정합니다(`secrets.defaults.properties` 참고).
3. Firebase 프로젝트 설정 후 `app/` 디렉토리에 `google-services.json` 파일을 추가합니다.
4. `secrets.properties`의 `BASE_URL`을 백엔드(`yak-allim-server` + `yak-allim-infra`) 주소로 지정합니다.
   - 같은 PC에서 Android 에뮬레이터로 테스트: `BASE_URL=http://10.0.2.2/` (에뮬레이터가 호스트 PC의 localhost를 가리키는 특수 별칭)
   - 같은 Wi-Fi의 실제 기기로 테스트: `BASE_URL=http://<백엔드가 떠 있는 PC의 LAN IP>/`
   - 디버그 빌드는 `10.0.2.2`, `localhost`, 그리고 `BASE_URL`이 `http://`일 때 그 호스트에 한해서만 평문 HTTP를 허용합니다. 허용 목록은 빌드 때 `BASE_URL`에서 읽어 `network_security_config.xml`을 만들기 때문에, PC의 IP가 바뀌어도 `BASE_URL`만 고치면 됩니다. (릴리스 빌드는 평문 HTTP를 허용하지 않습니다.)
5. Android Studio에서 프로젝트를 빌드하고 실행합니다.
