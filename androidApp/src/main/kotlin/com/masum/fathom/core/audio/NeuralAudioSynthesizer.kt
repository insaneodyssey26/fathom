package com.masum.fathom.core.audio

import android.content.Context
import com.masum.fathom.core.model.TraditionId
import com.masum.fathom.core.sanctuary.ByokProvider
import com.masum.fathom.core.sanctuary.SanctuaryPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

class NeuralAudioSynthesizer(
    private val context: Context,
    private val preferences: SanctuaryPreferences
) {

    private val cacheDir: File by lazy {
        File(context.cacheDir, "neural_audio").apply {
            if (!exists()) mkdirs()
        }
    }

    suspend fun synthesize(
        text: String,
        traditionId: TraditionId,
        isOriginal: Boolean
    ): File? = withContext(Dispatchers.IO) {
        val settings = preferences.settings.value
        val apiKey = settings.byokApiKey.trim()
        val provider = settings.byokProvider

        if (apiKey.isBlank() || provider == ByokProvider.NONE) {
            return@withContext null
        }

        val cacheKey = hashText("${provider.name}_${settings.byokVoiceId}_${text.trim()}")
        val cachedFile = File(cacheDir, "$cacheKey.mp3")
        if (cachedFile.exists() && cachedFile.length() > 1024L) {
            return@withContext cachedFile
        }

        return@withContext when (provider) {
            ByokProvider.ELEVENLABS -> synthesizeElevenLabs(text, apiKey, settings.byokVoiceId, cachedFile)
            ByokProvider.OPENAI -> synthesizeOpenAi(text, apiKey, settings.byokVoiceId, cachedFile)
            ByokProvider.NONE -> null
        }
    }

    suspend fun testConnection(provider: ByokProvider, apiKey: String, voiceId: String): Result<String> = withContext(Dispatchers.IO) {
        val cleanKey = apiKey.trim()
        if (cleanKey.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("API key cannot be empty"))
        }

        val testText = "The sacred light within."
        val testFile = File(cacheDir, "test_${provider.name}.mp3")

        val file = when (provider) {
            ByokProvider.ELEVENLABS -> synthesizeElevenLabs(testText, cleanKey, voiceId, testFile)
            ByokProvider.OPENAI -> synthesizeOpenAi(testText, cleanKey, voiceId, testFile)
            ByokProvider.NONE -> null
        }

        if (file != null && file.exists() && file.length() > 500L) {
            Result.success("Connection successful! Neural audio ready.")
        } else {
            Result.failure(IllegalStateException("Failed to synthesize audio. Check your API key and quotas."))
        }
    }

    private fun synthesizeElevenLabs(
        text: String,
        apiKey: String,
        customVoiceId: String,
        targetFile: File
    ): File? {
        val voiceId = if (customVoiceId.isNotBlank()) customVoiceId.trim() else DEFAULT_ELEVENLABS_VOICE
        val endpoint = "https://api.elevenlabs.io/v1/text-to-speech/$voiceId"

        var connection: HttpURLConnection? = null
        return try {
            val url = URL(endpoint)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15000
                readTimeout = 30000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "audio/mpeg")
                setRequestProperty("xi-api-key", apiKey)
            }

            val payload = JSONObject().apply {
                put("text", text)
                put("model_id", "eleven_multilingual_v2")
                put("voice_settings", JSONObject().apply {
                    put("stability", 0.5)
                    put("similarity_boost", 0.75)
                })
            }

            connection.outputStream.use { os ->
                os.write(payload.toString().toByteArray(Charsets.UTF_8))
                os.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream.use { input ->
                    FileOutputStream(targetFile).use { output ->
                        input.copyTo(output)
                    }
                }
                targetFile
            } else {
                null
            }
        } catch (_: Exception) {
            null
        } finally {
            connection?.disconnect()
        }
    }

    private fun synthesizeOpenAi(
        text: String,
        apiKey: String,
        customVoice: String,
        targetFile: File
    ): File? {
        val voice = if (customVoice.isNotBlank()) customVoice.trim().lowercase() else DEFAULT_OPENAI_VOICE
        val endpoint = "https://api.openai.com/v1/audio/speech"

        var connection: HttpURLConnection? = null
        return try {
            val url = URL(endpoint)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15000
                readTimeout = 30000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Authorization", "Bearer $apiKey")
            }

            val payload = JSONObject().apply {
                put("model", "tts-1")
                put("input", text)
                put("voice", voice)
            }

            connection.outputStream.use { os ->
                os.write(payload.toString().toByteArray(Charsets.UTF_8))
                os.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream.use { input ->
                    FileOutputStream(targetFile).use { output ->
                        input.copyTo(output)
                    }
                }
                targetFile
            } else {
                null
            }
        } catch (_: Exception) {
            null
        } finally {
            connection?.disconnect()
        }
    }

    private fun hashText(input: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(input.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    companion object {
        const val DEFAULT_ELEVENLABS_VOICE = "21m00Tcm4TlvDq8ikWAM"
        const val DEFAULT_OPENAI_VOICE = "onyx"
    }
}
