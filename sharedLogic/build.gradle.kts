import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.sqldelight)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "SharedLogic"
            isStatic = true
            linkerOpts("-lsqlite3")
        }
    }
    
    android {
       namespace = "com.masum.fathom.sharedLogic"
       compileSdk = 37
       minSdk = 26
    
       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
    }
    
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.sqldelight.coroutines.extensions)
            implementation(libs.sqldelight.primitive.adapters)
        }
        androidMain.dependencies {
            implementation(libs.sqldelight.android.driver)
        }
        iosMain.dependencies {
            implementation(libs.sqldelight.native.driver)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        val androidHostTest by getting {
            dependencies {
                implementation(libs.sqldelight.sqlite.driver)
            }
        }
    }
}

sqldelight {
    databases {
        create("FathomUserDatabase") {
            packageName.set("com.masum.fathom.core.database.user")
            srcDirs.setFrom("src/commonMain/userSqldelight")
            dialect(libs.sqldelight.sqlite324.dialect)
        }
        create("FathomContentDatabase") {
            packageName.set("com.masum.fathom.core.database.content")
            srcDirs.setFrom("src/commonMain/contentSqldelight")
            dialect(libs.sqldelight.sqlite324.dialect)
        }
    }
}