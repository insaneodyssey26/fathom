package com.masum.fathom.core.common.result

sealed interface FathomResult<out D, out E : RootError> {
    data class Success<out D>(val data: D) : FathomResult<D, Nothing>
    data class Failure<out E : RootError>(val error: E) : FathomResult<Nothing, E>
}

inline fun <D, E : RootError, R> FathomResult<D, E>.map(transform: (D) -> R): FathomResult<R, E> {
    return when (this) {
        is FathomResult.Success -> FathomResult.Success(transform(data))
        is FathomResult.Failure -> this
    }
}

inline fun <D, E : RootError> FathomResult<D, E>.onSuccess(action: (D) -> Unit): FathomResult<D, E> {
    if (this is FathomResult.Success) {
        action(data)
    }
    return this
}

inline fun <D, E : RootError> FathomResult<D, E>.onError(action: (E) -> Unit): FathomResult<D, E> {
    if (this is FathomResult.Failure) {
        action(error)
    }
    return this
}

inline fun <D, E : RootError, R> FathomResult<D, E>.fold(
    onSuccess: (D) -> R,
    onError: (E) -> R
): R {
    return when (this) {
        is FathomResult.Success -> onSuccess(data)
        is FathomResult.Failure -> onError(error)
    }
}

fun <D> FathomResult<D, *>.asSuccessOrNull(): D? {
    return when (this) {
        is FathomResult.Success -> data
        is FathomResult.Failure -> null
    }
}

fun <E : RootError> FathomResult<*, E>.asErrorOrNull(): E? {
    return when (this) {
        is FathomResult.Success -> null
        is FathomResult.Failure -> error
    }
}
