import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.serialization)
}

// GITHUB_RUN_NUMBER starts at 1 and has no relation to what's already live on the
// Play Store; this offset keeps the release workflow's first versionCode safely
// above the current live versionCode (3 as of 2026-07-19).
val versionCodeOffset = 100
val releaseVersionName: String? = System.getenv("RELEASE_VERSION_NAME")
val releaseRunNumber: String? = System.getenv("GITHUB_RUN_NUMBER")

if (releaseVersionName != null) {
    require(Regex("""^\d+\.\d+\.\d+$""").matches(releaseVersionName)) {
        "RELEASE_VERSION_NAME must be in X.Y.Z format, got: $releaseVersionName"
    }
}

android {
    namespace = "dev.akexorcist.flipfoldcounter"
    compileSdk = 36

    defaultConfig {
        applicationId = "dev.akexorcist.flipfoldcounter"
        minSdk = 29
        targetSdk = 36
        versionCode = releaseRunNumber?.let { it.toInt() + versionCodeOffset } ?: 3
        versionName = releaseVersionName ?: "1.2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    val keystoreProperties = Properties()
    val keystorePropertiesFile = rootProject.file("local.properties")
    if (keystorePropertiesFile.exists()) {
        keystoreProperties.load(keystorePropertiesFile.inputStream())
    }

    signingConfigs {
        create("release") {
            storeFile = file(keystoreProperties.getProperty("keystore_path", "release.keystore"))
            storePassword = keystoreProperties.getProperty("keystore_password", "")
            keyAlias = keystoreProperties.getProperty("keystore_key_alias", "")
            keyPassword = keystoreProperties.getProperty("keystore_key_password", "")
            enableV3Signing = true
            enableV4Signing = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
    testOptions {
        unitTests.all {
            it.useJUnitPlatform()
        }
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.serialization.json)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.navigation3.runtime)
    implementation(libs.navigation3.ui)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.room.testing)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.koin.android)
    implementation(libs.koin.compose)

    implementation(libs.pagerIndicator)
    implementation(libs.vico)
    implementation(libs.splashscreen)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs = listOf("-XXLanguage:+PropertyParamAnnotationDefaultTargetMode")
    }
}
