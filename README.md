# Yak-Allim-Android

> **복약 안내서 OCR 분석 기반 복약 관리 솔루션**

**Yak-Allim-Android**는 복약 안내서를 촬영하거나 이미지를 업로드하여 복용 방법(용법, 용량, 기간 등)을 분석하고, 이를 기반으로 복약 알람을 제공하는 안드로이드 애플리케이션입니다.

---

## Features

- **OCR Analysis**: 카메라 촬영 또는 갤러리 이미지 선택을 통해 복약 안내서의 텍스트를 인식하고 복용 정보를 추출합니다.
- **Medication Alarm**: 분석된 정보를 바탕으로 복용 횟수와 기간에 맞춰 알람을 등록합니다.
- **Real-time Status Updates**: Firebase Cloud Messaging(FCM)을 활용하여 분석 진행 상황을 수신합니다.
- **User Guide**: 앱의 주요 기능과 사용법을 이해할 수 있는 사용자 가이드를 제공합니다.

## Tech Stack

- **UI**: Jetpack Compose
- **Language**: Kotlin
- **Architecture**: Clean Architecture & MVVM
- **Asynchronous**: Coroutines & Flow
- **Dependency Injection**: Hilt
- **Network**: Retrofit2, OkHttp3, kotlinx.serialization
- **Image/Camera**: CameraX, Coil, ExifInterface
- **Local Storage**: DataStore Preferences
- **Notification**: Firebase Cloud Messaging (FCM)
- **Security & Build**: Secrets Gradle Plugin, Network Security Config, R8/ProGuard

## Project Structure

**Clean Architecture** 원칙에 따라 레이어를 분리하여 관심사 분리(SoC)와 확장성을 확보했습니다.

- **Domain Layer**: 비즈니스 로직의 모델, Repository/Infrastructure 인터페이스 및 UseCase 정의
- **Data Layer**: 데이터 소스(Local/Remote) 접근 로직과 인프라(Alarm, FCM, Image) 구현 및 Repository 구현체
- **UI Layer**: Jetpack Compose를 활용한 선언적 UI와 상태 관리를 위한 ViewModel

### Package Structure

```text
com.example.yakallim
├── data            # 데이터 및 인프라 구현체
│   ├── datasource  # 로컬(DataStore) 및 원격(API) 데이터 소스
│   ├── infrastructure # 알람, FCM, 이미지 처리 구현체
│   ├── mapper      # DTO와 도메인 모델 간 매핑
│   └── repository  # 레포지토리 인터페이스 구현체
├── di              # 의존성 주입을 위한 Hilt 모듈
├── domain          # 비즈니스 로직 및 모델
│   ├── model       # 도메인 모델
│   ├── repository  # 도메인 레포지토리 인터페이스
│   └── usecase     # 기능적 비즈니스 로직 단위
└── ui              # UI 컴포넌트 및 뷰모델
    ├── camera      # 카메라 및 이미지 선택 기능
    ├── ocr         # OCR 진행 및 결과 UI
    └── theme       # 디자인 시스템 및 테마 정의
```

## Getting Started

### Prerequisites

- **Android Studio**: Ladybug (2024.2.1) or higher
- **JDK**: Java 11
- **SDK**: Compile SDK 37, Min SDK 24

### Installation & Build

1. 저장소를 복제합니다:

    ```bash
    git clone https://github.com/koolunkle-yak-allim/yak-allim-android.git
    ```

2. 프로젝트 루트에 `secrets.properties` 파일을 생성하고 필요한 환경 변수를 설정합니다.
3. Firebase 프로젝트 설정 후 `app/` 디렉토리에 `google-services.json` 파일을 추가합니다.
4. Android Studio에서 프로젝트를 빌드하고 실행합니다.
