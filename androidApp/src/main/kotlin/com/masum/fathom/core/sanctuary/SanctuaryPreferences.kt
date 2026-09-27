package com.masum.fathom.core.sanctuary

import android.content.Context
import android.content.SharedPreferences
import com.masum.fathom.ui.theme.ReadingTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ByokProvider(val displayName: String) {
    NONE("Offline System TTS (Free)"),
    ELEVENLABS("ElevenLabs Neural API"),
    OPENAI("OpenAI Audio API")
}

data class SanctuarySettings(
    val userName: String = "Sanctuary Reader",
    val userTradition: String? = null,
    val feedPersonalizedToTradition: Boolean = false,
    val hasCompletedOnboarding: Boolean = false,
    val theme: ReadingTheme = ReadingTheme.OBSIDIAN,
    val dailyNotificationEnabled: Boolean = false,
    val notificationHour: Int = 7,
    val notificationMinute: Int = 0,
    val byokProvider: ByokProvider = ByokProvider.NONE,
    val byokApiKey: String = "",
    val byokVoiceId: String = "",
    val lastSeenWhatsNewVersion: Int = 0
)

class SanctuaryPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("fathom_sanctuary_prefs", Context.MODE_PRIVATE)

    companion object {
        const val CURRENT_ONBOARDING_VERSION = 1
    }

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<SanctuarySettings> = _settings.asStateFlow()

    private fun loadSettings(): SanctuarySettings {
        val name = prefs.getString("user_name", "Sanctuary Reader") ?: "Sanctuary Reader"
        val tradition = prefs.getString("user_tradition", null)
        val feedPersonalized = prefs.getBoolean("feed_personalized", false)
        val completedVersion = prefs.getInt("completed_onboarding_version", 0)
        val completedOnboarding = completedVersion >= CURRENT_ONBOARDING_VERSION

        val themeName = prefs.getString("reading_theme", ReadingTheme.OBSIDIAN.name) ?: ReadingTheme.OBSIDIAN.name
        val theme = try {
            ReadingTheme.valueOf(themeName)
        } catch (_: Exception) {
            ReadingTheme.OBSIDIAN
        }

        val notifEnabled = prefs.getBoolean("daily_notification_enabled", false)
        val hour = prefs.getInt("notification_hour", 7)
        val minute = prefs.getInt("notification_minute", 0)

        val providerName = prefs.getString("byok_provider", ByokProvider.NONE.name) ?: ByokProvider.NONE.name
        val provider = try {
            ByokProvider.valueOf(providerName)
        } catch (_: Exception) {
            ByokProvider.NONE
        }

        val apiKey = prefs.getString("byok_api_key", "") ?: ""
        val voiceId = prefs.getString("byok_voice_id", "") ?: ""
        val lastSeenWhatsNew = prefs.getInt("last_seen_whats_new_version", 0)

        return SanctuarySettings(
            userName = name,
            userTradition = tradition,
            feedPersonalizedToTradition = feedPersonalized,
            hasCompletedOnboarding = completedOnboarding,
            theme = theme,
            dailyNotificationEnabled = notifEnabled,
            notificationHour = hour,
            notificationMinute = minute,
            byokProvider = provider,
            byokApiKey = apiKey,
            byokVoiceId = voiceId,
            lastSeenWhatsNewVersion = lastSeenWhatsNew
        )
    }

    fun completeOnboarding(name: String, tradition: String?, personalizeFeed: Boolean) {
        val cleanName = name.trim().ifBlank { "Sanctuary Reader" }
        val cleanTradition = tradition?.trim()?.ifBlank { null }
        prefs.edit()
            .putString("user_name", cleanName)
            .putString("user_tradition", cleanTradition)
            .putBoolean("feed_personalized", personalizeFeed)
            .putBoolean("has_completed_onboarding", true)
            .putInt("completed_onboarding_version", CURRENT_ONBOARDING_VERSION)
            .apply()
        _settings.value = _settings.value.copy(
            userName = cleanName,
            userTradition = cleanTradition,
            feedPersonalizedToTradition = personalizeFeed,
            hasCompletedOnboarding = true
        )
    }

    fun updateProfile(name: String, tradition: String?) {
        val cleanName = name.trim().ifBlank { "Sanctuary Reader" }
        val cleanTradition = tradition?.trim()?.ifBlank { null }
        prefs.edit()
            .putString("user_name", cleanName)
            .putString("user_tradition", cleanTradition)
            .apply()
        _settings.value = _settings.value.copy(
            userName = cleanName,
            userTradition = cleanTradition
        )
    }

    fun updateTheme(theme: ReadingTheme) {
        prefs.edit().putString("reading_theme", theme.name).apply()
        _settings.value = _settings.value.copy(theme = theme)
    }

    fun updateNotificationTime(enabled: Boolean, hour: Int, minute: Int) {
        prefs.edit()
            .putBoolean("daily_notification_enabled", enabled)
            .putInt("notification_hour", hour)
            .putInt("notification_minute", minute)
            .apply()
        _settings.value = _settings.value.copy(
            dailyNotificationEnabled = enabled,
            notificationHour = hour,
            notificationMinute = minute
        )
    }

    fun updateByokSettings(provider: ByokProvider, apiKey: String, voiceId: String) {
        prefs.edit()
            .putString("byok_provider", provider.name)
            .putString("byok_api_key", apiKey)
            .putString("byok_voice_id", voiceId)
            .apply()
        _settings.value = _settings.value.copy(
            byokProvider = provider,
            byokApiKey = apiKey,
            byokVoiceId = voiceId
        )
    }

    fun shouldShowWhatsNew(currentVersionCode: Int): Boolean {
        return _settings.value.lastSeenWhatsNewVersion < currentVersionCode
    }

    fun markWhatsNewSeen(currentVersionCode: Int) {
        prefs.edit().putInt("last_seen_whats_new_version", currentVersionCode).apply()
        _settings.value = _settings.value.copy(lastSeenWhatsNewVersion = currentVersionCode)
    }
}
