package com.masum.fathom.core.subscription

import android.os.Build
import com.masum.fathom.core.model.user.SubscriptionTier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import android.util.Base64

data class ActiveDeviceInfo(
    val deviceId: String,
    val deviceName: String,
    val activatedAtEpochMs: Long = 0L,
    val isCurrentDevice: Boolean = false
)

data class LicenseValidationResult(
    val isValid: Boolean,
    val tier: SubscriptionTier = SubscriptionTier.FREE,
    val expiresAtEpochMs: Long = 0L,
    val isExpired: Boolean = false,
    val deviceCount: Int = 1,
    val maxDevices: Int = 2,
    val activeDevices: List<ActiveDeviceInfo> = emptyList(),
    val signedToken: String? = null,
    val tokenSignature: String? = null,
    val errorMessage: String? = null
)

class LicenseEngine(
    private val apiBaseUrl: String = "https://fathom-license-api.skmasumali-main.workers.dev",
    private val clientSigningSecret: String = "fathom_sanctuary_master_secret_2026"
) {

    private fun parseTier(identifier: String?): SubscriptionTier {
        return when (identifier?.uppercase()) {
            "MONTHLY" -> SubscriptionTier.MONTHLY
            "ANNUAL" -> SubscriptionTier.ANNUAL
            "LIFETIME" -> SubscriptionTier.LIFETIME
            else -> SubscriptionTier.LIFETIME
        }
    }

    suspend fun activateLicenseRemote(
        licenseToken: String,
        email: String?,
        deviceId: String,
        deviceName: String = "${Build.MANUFACTURER} ${Build.MODEL}"
    ): LicenseValidationResult = withContext(Dispatchers.IO) {
        val sanitized = licenseToken.trim().replace("\n", "").replace("\r", "")
        if (sanitized.isBlank()) {
            return@withContext LicenseValidationResult(isValid = false, errorMessage = "License key is empty")
        }

        try {
            val endpoint = URL("$apiBaseUrl/api/activate")
            val conn = (endpoint.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 10000
                readTimeout = 10000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
            }

            val jsonBody = JSONObject().apply {
                put("licenseKey", sanitized)
                put("deviceId", deviceId)
                put("deviceName", deviceName)
                if (!email.isNullOrBlank()) {
                    put("email", email.trim().lowercase())
                }
            }

            OutputStreamWriter(conn.outputStream, StandardCharsets.UTF_8).use { writer ->
                writer.write(jsonBody.toString())
                writer.flush()
            }

            val responseCode = conn.responseCode
            val isSuccess = responseCode in 200..299
            val stream = if (isSuccess) conn.inputStream else conn.errorStream ?: conn.inputStream

            val responseText = BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8)).use { reader ->
                reader.readText()
            }

            val rootJson = JSONObject(responseText)

            if (!isSuccess || !rootJson.optBoolean("success", false)) {
                val errorMsg = rootJson.optString("error", "Activation failed ($responseCode)")
                return@withContext LicenseValidationResult(isValid = false, errorMessage = errorMsg)
            }

            val tier = parseTier(rootJson.optString("tier"))
            val expiresAt = rootJson.optLong("expiresAt", 0L)
            val deviceCount = rootJson.optInt("deviceCount", 1)
            val maxDevices = rootJson.optInt("maxDevices", 2)
            val signedToken = rootJson.optString("signedToken", "")
            val tokenSignature = rootJson.optString("tokenSignature", "")

            val activeDevicesList = mutableListOf<ActiveDeviceInfo>()
            val devicesArray = rootJson.optJSONArray("devices")
            if (devicesArray != null) {
                for (i in 0 until devicesArray.length()) {
                    val dObj = devicesArray.getJSONObject(i)
                    activeDevicesList.add(
                        ActiveDeviceInfo(
                            deviceId = dObj.optString("deviceId"),
                            deviceName = dObj.optString("deviceName", "Device"),
                            activatedAtEpochMs = dObj.optLong("activatedAt", 0L),
                            isCurrentDevice = dObj.optBoolean("isCurrent", false)
                        )
                    )
                }
            }

            LicenseValidationResult(
                isValid = true,
                tier = tier,
                expiresAtEpochMs = expiresAt,
                deviceCount = deviceCount,
                maxDevices = maxDevices,
                activeDevices = activeDevicesList,
                signedToken = signedToken,
                tokenSignature = tokenSignature
            )
        } catch (e: Exception) {
            LicenseValidationResult(
                isValid = false,
                errorMessage = e.localizedMessage ?: "Network error connecting to license server"
            )
        }
    }

    suspend fun fetchActiveDevicesRemote(
        licenseKey: String,
        currentDeviceId: String
    ): List<ActiveDeviceInfo> = withContext(Dispatchers.IO) {
        val sanitized = licenseKey.trim().uppercase()
        if (sanitized.isBlank()) return@withContext emptyList()

        try {
            val encodedKey = URLEncoder.encode(sanitized, "UTF-8")
            val encodedDev = URLEncoder.encode(currentDeviceId, "UTF-8")
            val endpoint = URL("$apiBaseUrl/api/devices?licenseKey=$encodedKey&deviceId=$encodedDev")

            val conn = (endpoint.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("Accept", "application/json")
            }

            if (conn.responseCode !in 200..299) return@withContext emptyList()

            val text = BufferedReader(InputStreamReader(conn.inputStream, StandardCharsets.UTF_8)).use { it.readText() }
            val root = JSONObject(text)
            val devicesArray = root.optJSONArray("devices") ?: return@withContext emptyList()

            val list = mutableListOf<ActiveDeviceInfo>()
            for (i in 0 until devicesArray.length()) {
                val obj = devicesArray.getJSONObject(i)
                list.add(
                    ActiveDeviceInfo(
                        deviceId = obj.optString("deviceId"),
                        deviceName = obj.optString("deviceName", "Device"),
                        activatedAtEpochMs = obj.optLong("activatedAt", 0L),
                        isCurrentDevice = obj.optBoolean("isCurrent", false)
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun revokeDeviceRemote(
        licenseKey: String,
        deviceId: String
    ): Boolean = withContext(Dispatchers.IO) {
        val sanitized = licenseKey.trim().uppercase()
        if (sanitized.isBlank() || deviceId.isBlank()) return@withContext false

        try {
            val endpoint = URL("$apiBaseUrl/api/revoke")
            val conn = (endpoint.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 8000
                readTimeout = 8000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
            }

            val body = JSONObject().apply {
                put("licenseKey", sanitized)
                put("deviceId", deviceId)
            }

            OutputStreamWriter(conn.outputStream, StandardCharsets.UTF_8).use { it.write(body.toString()); it.flush() }
            val isSuccess = conn.responseCode in 200..299
            if (isSuccess) {
                val text = BufferedReader(InputStreamReader(conn.inputStream, StandardCharsets.UTF_8)).use { it.readText() }
                JSONObject(text).optBoolean("success", false)
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    fun verifyTokenLocally(signedToken: String, signature: String): Boolean {
        if (signedToken.isBlank() || signature.isBlank()) return false
        return try {
            val mac = Mac.getInstance("HmacSHA256")
            val keySpec = SecretKeySpec(clientSigningSecret.toByteArray(StandardCharsets.UTF_8), "HmacSHA256")
            mac.init(keySpec)
            val computed = mac.doFinal(signedToken.toByteArray(StandardCharsets.UTF_8))
            val computedBase64 = Base64.encodeToString(computed, Base64.NO_WRAP)
            computedBase64 == signature
        } catch (_: Exception) {
            false
        }
    }
}
