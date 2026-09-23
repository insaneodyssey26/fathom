package com.masum.fathom.core.common.result

sealed interface DataError : RootError {
    enum class Local : DataError {
        DISK_FULL,
        CORRUPTED_DATABASE,
        NOT_FOUND,
        CONSTRAINT_VIOLATION,
        ATTACH_FAILED,
        UNKNOWN
    }

    enum class Network : DataError {
        REQUEST_TIMEOUT,
        NO_INTERNET,
        SERVER_ERROR,
        PAYLOAD_TOO_LARGE,
        SERIALIZATION_ERROR,
        UNKNOWN
    }
}

sealed interface ContentError : RootError {
    enum class Pack : ContentError {
        INTEGRITY_CHECK_FAILED,
        VERSION_INCOMPATIBLE,
        DOWNLOAD_FAILED,
        MANIFEST_INVALID,
        NOT_INSTALLED
    }
}
