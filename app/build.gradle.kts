import java.net.URI
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.google.services)
    alias(libs.plugins.secrets.gradle.plugin)
    alias(libs.plugins.kotlin.serialization)
}

secrets {
    // Optionally specify a different file name or default values
    propertiesFileName = "secrets.properties"

    // A properties file at this path is expected to be present
    // defaultPropertiesFileName = "secrets.defaults.properties"

    // Configure which keys should be ignored by the plugin by providing regular expressions.
    // "sdk.dir" is ignored by default.
    ignoreList.add("keyToIgnore") // Ignore the key "keyToIgnore"
    ignoreList.add("sdk.*")       // Ignore all keys matching the regexp "sdk.*"
}

android {
    namespace = "io.github.koolunkle.yakallim"
    compileSdk = 37

    defaultConfig {
        applicationId = "io.github.koolunkle.yakallim"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
        debug {
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    lint {
        disable.add("MissingFirebaseInstanceTokenRefresh")
    }
}

// 디버그 빌드는 로컬 개발 서버에 평문 HTTP로 접속한다. 허용할 호스트를 저장소에 적어 두지 않고,
// secrets.properties의 BASE_URL(http일 때)에서 읽어 network_security_config를 빌드 때 만든다.
abstract class GenerateDebugNetworkSecurityConfig : DefaultTask() {
    @get:Input
    abstract val cleartextHosts: ListProperty<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val domains = cleartextHosts.get().joinToString("\n") { host ->
            "        <domain includeSubdomains=\"true\">$host</domain>"
        }
        val file = outputDir.file("xml/network_security_config.xml").get().asFile
        file.parentFile.mkdirs()
        file.writeText(
            """
            |<?xml version="1.0" encoding="utf-8"?>
            |<!-- 빌드 때 생성되는 파일입니다. 직접 수정하지 말고 secrets.properties의 BASE_URL을 바꾸세요. -->
            |<network-security-config>
            |    <base-config cleartextTrafficPermitted="false">
            |        <trust-anchors>
            |            <certificates src="system" />
            |        </trust-anchors>
            |    </base-config>
            |
            |    <domain-config cleartextTrafficPermitted="true">
            |$domains
            |    </domain-config>
            |
            |    <debug-overrides>
            |        <trust-anchors>
            |            <certificates src="system" />
            |            <certificates src="user" />
            |        </trust-anchors>
            |    </debug-overrides>
            |</network-security-config>
            |""".trimMargin()
        )
    }
}

// 에뮬레이터의 호스트 PC 별칭(10.0.2.2)과 localhost는 항상 허용하고, BASE_URL이 http면 그 호스트를 더한다.
val debugCleartextHosts: List<String> = run {
    val properties = Properties()
    val secretsFile = rootProject.file("secrets.properties").takeIf { it.exists() }
        ?: rootProject.file("secrets.defaults.properties")
    if (secretsFile.exists()) secretsFile.inputStream().use(properties::load)
    val baseUrl = runCatching { URI(properties.getProperty("BASE_URL").orEmpty()) }.getOrNull()
    val baseUrlHost = baseUrl?.takeIf { it.scheme == "http" }?.host
    (listOf("10.0.2.2", "localhost") + listOfNotNull(baseUrlHost)).distinct()
}

androidComponents {
    onVariants(selector().withBuildType("debug")) { variant ->
        val task = tasks.register<GenerateDebugNetworkSecurityConfig>(
            "generate${variant.name.replaceFirstChar { it.uppercase() }}NetworkSecurityConfig"
        ) {
            cleartextHosts.set(debugCleartextHosts)
        }
        variant.sources.res?.addGeneratedSourceDirectory(task, GenerateDebugNetworkSecurityConfig::outputDir)
    }
}

dependencies {
    // =================================================================
    // Android Core & Android KTX
    // =================================================================
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.exifinterface)

    // =================================================================
    // Jetpack Compose UI & Lifecycle
    // =================================================================
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // =================================================================
    // Design System (Material Components)
    // =================================================================
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.material)

    // =================================================================
    // Dependency Injection (Hilt)
    // =================================================================
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // =================================================================
    // Asynchronous & Network
    // =================================================================
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging)
    implementation(libs.okhttp.sse)

    // =================================================================
    // Media & Storage
    // =================================================================
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.extensions)

    implementation(libs.coil.compose)

    implementation(libs.lottie.compose)

    implementation(libs.androidx.datastore.preferences)

    // =================================================================
    // Firebase Services
    // =================================================================
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.messaging)
    implementation(libs.kotlinx.coroutines.play.services)

    // =================================================================
    // Testing & Debugging
    // =================================================================
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.mockito.kotlin)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    coreLibraryDesugaring(libs.desugar.jdk.libs)
}