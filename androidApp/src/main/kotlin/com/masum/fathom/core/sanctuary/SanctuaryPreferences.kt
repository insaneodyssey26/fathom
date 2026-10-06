package com.masum.fathom.core.sanctuary

import android.content.Context
import android.content.SharedPreferences
import com.masum.fathom.core.model.TraditionId
import com.masum.fathom.core.model.user.FeedMode
import com.masum.fathom.ui.theme.ReadingTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class VerseSource { ALL_TRADITIONS, MY_TRADITION }

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
    val verseSource: VerseSource = VerseSource.ALL_TRADITIONS,
    val showVerseOnLockScreen: Boolean = false,
    val updateAlertsEnabled: Boolean = true,
    val byokProvider: ByokProvider = ByokProvider.NONE,
    val byokApiKey: String = "",
    val byokVoiceId: String = "",
    val lastSeenWhatsNewVersion: Int = 0,
    val feedSelectedTraditions: Set<TraditionId> = TraditionId.entries.toSet(),
    val feedSelectedBookId: String? = null,
    val feedMode: FeedMode = FeedMode.EXPLORATION_DISCOVERY
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

        val sourceName = prefs.getString("notification_verse_source", VerseSource.ALL_TRADITIONS.name) ?: VerseSource.ALL_TRADITIONS.name
        val verseSource = runCatching { VerseSource.valueOf(sourceName) }.getOrDefault(VerseSource.ALL_TRADITIONS)
        val lockScreen = prefs.getBoolean("notification_lock_screen", false)
        val updateAlerts = prefs.getBoolean("update_alerts_enabled", true)

        val providerName = prefs.getString("byok_provider", ByokProvider.NONE.name) ?: ByokProvider.NONE.name
        val provider = try {
            ByokProvider.valueOf(providerName)
        } catch (_: Exception) {
            ByokProvider.NONE
        }

        val apiKey = prefs.getString("byok_api_key", "") ?: ""
        val voiceId = prefs.getString("byok_voice_id", "") ?: ""
        val lastSeenWhatsNew = prefs.getInt("last_seen_whats_new_version", 0)

        val savedTraditionKeys = prefs.getStringSet("feed_selected_traditions", null)
        val feedTraditions = if (savedTraditionKeys != null && savedTraditionKeys.isNotEmpty()) {
            savedTraditionKeys.mapNotNull { TraditionId.fromKeyOrNull(it) }.toSet().ifEmpty { TraditionId.entries.toSet() }
        } else {
            TraditionId.entries.toSet()
        }
        val feedBookId = prefs.getString("feed_selected_book_id", null)
        val feedModeName = prefs.getString("feed_mode", FeedMode.EXPLORATION_DISCOVERY.name) ?: FeedMode.EXPLORATION_DISCOVERY.name
        val feedMode = runCatching { FeedMode.valueOf(feedModeName) }.getOrDefault(FeedMode.EXPLORATION_DISCOVERY)

        return SanctuarySettings(
            userName = name,
            userTradition = tradition,
            feedPersonalizedToTradition = feedPersonalized,
            hasCompletedOnboarding = completedOnboarding,
            theme = theme,
            dailyNotificationEnabled = notifEnabled,
            notificationHour = hour,
            notificationMinute = minute,
            verseSource = verseSource,
            showVerseOnLockScreen = lockScreen,
            updateAlertsEnabled = updateAlerts,
            byokProvider = provider,
            byokApiKey = apiKey,
            byokVoiceId = voiceId,
            lastSeenWhatsNewVersion = lastSeenWhatsNew,
            feedSelectedTraditions = feedTraditions,
            feedSelectedBookId = feedBookId,
            feedMode = feedMode
        )
    }

    fun completeOnboarding(name: String, tradition: String?, personalizeFeed: Boolean) {
        val cleanName = name.trim().ifBlank { "Sanctuary Reader" }
        val cleanTradition = tradition?.trim()?.ifBlank { null }
        val traditions = if (personalizeFeed && cleanTradition != null) {
            val trad = TraditionId.entries.firstOrNull {
                it.key.equals(cleanTradition, ignoreCase = true) || it.displayName.equals(cleanTradition, ignoreCase = true)
            } ?: TraditionId.HINDUISM
            setOf(trad)
        } else {
            TraditionId.entries.toSet()
        }
        val traditionKeys = traditions.map { it.key }.toSet()

        prefs.edit()
            .putString("user_name", cleanName)
            .putString("user_tradition", cleanTradition)
            .putBoolean("feed_personalized", personalizeFeed)
            .putBoolean("has_completed_onboarding", true)
            .putInt("completed_onboarding_version", CURRENT_ONBOARDING_VERSION)
            .putStringSet("feed_selected_traditions", traditionKeys)
            .apply()
        _settings.value = _settings.value.copy(
            userName = cleanName,
            userTradition = cleanTradition,
            feedPersonalizedToTradition = personalizeFeed,
            hasCompletedOnboarding = true,
            feedSelectedTraditions = traditions
        )
    }

    fun updateFeedTraditions(traditions: Set<TraditionId>) {
        val keys = traditions.map { it.key }.toSet()
        prefs.edit().putStringSet("feed_selected_traditions", keys).apply()
        _settings.value = _settings.value.copy(feedSelectedTraditions = traditions)
    }

    fun updateFeedBook(bookId: String?) {
        prefs.edit().putString("feed_selected_book_id", bookId).apply()
        _settings.value = _settings.value.copy(feedSelectedBookId = bookId)
    }

    fun updateFeedMode(mode: FeedMode) {
        prefs.edit().putString("feed_mode", mode.name).apply()
        _settings.value = _settings.value.copy(feedMode = mode)
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

    fun updateVerseSource(source: VerseSource) {
        prefs.edit().putString("notification_verse_source", source.name).apply()
        _settings.value = _settings.value.copy(verseSource = source)
    }

    fun updateLockScreenVerse(show: Boolean) {
        prefs.edit().putBoolean("notification_lock_screen", show).apply()
        _settings.value = _settings.value.copy(showVerseOnLockScreen = show)
    }

    fun updateUpdateAlerts(enabled: Boolean) {
        prefs.edit().putBoolean("update_alerts_enabled", enabled).apply()
        _settings.value = _settings.value.copy(updateAlertsEnabled = enabled)
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
