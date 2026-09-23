# Fathom Production ProGuard & R8 Optimization Rules

# 1. SQLDelight & SQLite Driver Preservation
-keep class app.cash.sqldelight.** { *; }
-keep class com.masum.fathom.core.database.** { *; }
-dontwarn app.cash.sqldelight.**

# 2. Kotlin Coroutines & Flow
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# 3. Jetpack Compose Runtime Optimization
-assumenosideeffects class androidx.compose.runtime.ComposerKt {
    void sourceInformation(androidx.compose.runtime.Composer, java.lang.String);
    void sourceInformationMarkerStart(androidx.compose.runtime.Composer, int, java.lang.String);
    void sourceInformationMarkerEnd(androidx.compose.runtime.Composer);
}

# 4. Preserve Line Numbers for Crash Reporting
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile