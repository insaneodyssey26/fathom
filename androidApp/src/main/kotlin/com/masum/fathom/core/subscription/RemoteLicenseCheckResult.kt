package com.masum.fathom.core.subscription

sealed interface RemoteLicenseCheckResult {
    data class Valid(val expiresAtEpochMs: Long) : RemoteLicenseCheckResult
    data object Revoked : RemoteLicenseCheckResult
    data object Expired : RemoteLicenseCheckResult
    data object NotFound : RemoteLicenseCheckResult
    data class NetworkError(val message: String) : RemoteLicenseCheckResult
}
