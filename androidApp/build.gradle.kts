import java.io.FileInputStream
import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

composeCompiler {
    stabilityConfigurationFiles.add(layout.projectDirectory.file("compose-stability.conf"))
}
dependencies {
    implementation(project(":sharedLogic"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.runtime)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.phosphor.icon)
    implementation(libs.compose.ui)
    implementation(libs.androidx.lifecycle.viewmodelCompose)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.sqldelight.android.driver)
    implementation(libs.play.app.update)
    implementation(libs.androidx.profileinstaller)
    implementation(libs.backdrop)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.graphics.shapes)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.media)
    implementation(libs.androidx.security.crypto)
    implementation(libs.play.billing)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.json)
}

android {
    namespace = "com.masum.fathom"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.masum.fathom"
        minSdk = 26
        targetSdk = 37
        versionCode = 15
        versionName = "1.7.0-rc01"
        manifestPlaceholders["appLabel"] = "Fathom"

        val keystorePropertiesFile = rootProject.file("keystore.properties")
        val playBillingPublicKey = if (keystorePropertiesFile.exists()) {
            val properties = Properties()
            FileInputStream(keystorePropertiesFile).use { properties.load(it) }
            properties.getProperty("playBillingPublicKey", "") ?: ""
        } else {
            ""
        }
        buildConfigField("String", "PLAY_LICENSE_PUBLIC_KEY", "\"$playBillingPublicKey\"")
    }

    signingConfigs {
        val keystorePropertiesFile = rootProject.file("keystore.properties")
        if (keystorePropertiesFile.exists()) {
            val properties = Properties()
            FileInputStream(keystorePropertiesFile).use { properties.load(it) }
            val storeFilePath = properties.getProperty("storeFile")
            if (!storeFilePath.isNullOrEmpty() && file(storeFilePath).exists()) {
                create("releaseConfig") {
                    storeFile = file(storeFilePath)
                    storePassword = properties.getProperty("storePassword")
                    keyAlias = properties.getProperty("keyAlias")
                    keyPassword = properties.getProperty("keyPassword")
                }
            }
        }
    }

    androidResources {
        localeFilters += listOf("en")
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    packaging {
        resources {
            excludes += listOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "/META-INF/*.version",
                "/META-INF/*.kotlin_module",
                "/META-INF/DEPENDENCIES",
                "DebugProbesKt.bin"
            )
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isDebuggable = true
            isMinifyEnabled = false
            manifestPlaceholders["appLabel"] = "Fathom (Debug)"
        }
        release {
            val releaseSigning = signingConfigs.findByName("releaseConfig")
            if (releaseSigning != null) {
                signingConfig = releaseSigning
            }
            isMinifyEnabled = true
            isShrinkResources = true
            manifestPlaceholders["appLabel"] = "Fathom"
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}