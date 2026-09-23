package com.masum.fathom.core.common.result

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FathomResultTest {

    @Test
    fun givenSuccessResult_whenMap_thenTransformsData() {
        val result: FathomResult<Int, DataError.Local> = FathomResult.Success(42)
        val mapped = result.map { it * 2 }

        assertTrue(mapped is FathomResult.Success)
        assertEquals(84, mapped.data)
    }

    @Test
    fun givenFailureResult_whenMap_thenPreservesFailure() {
        val result: FathomResult<Int, DataError.Local> = FathomResult.Failure(DataError.Local.NOT_FOUND)
        val mapped = result.map { it * 2 }

        assertTrue(mapped is FathomResult.Failure)
        assertEquals(DataError.Local.NOT_FOUND, mapped.error)
    }

    @Test
    fun givenSuccessResult_whenFold_thenInvokesOnSuccess() {
        val result: FathomResult<String, ContentError.Pack> = FathomResult.Success("Passage Content")
        val folded = result.fold(
            onSuccess = { "Transformed: $it" },
            onError = { "Error: $it" }
        )

        assertEquals("Transformed: Passage Content", folded)
    }

    @Test
    fun givenFailureResult_whenFold_thenInvokesOnError() {
        val result: FathomResult<String, ContentError.Pack> = FathomResult.Failure(ContentError.Pack.INTEGRITY_CHECK_FAILED)
        val folded = result.fold(
            onSuccess = { "Transformed: $it" },
            onError = { "Failed with ${it.name}" }
        )

        assertEquals("Failed with INTEGRITY_CHECK_FAILED", folded)
    }

    @Test
    fun givenSuccessAndFailure_whenAsNullHelpersCalled_thenReturnsExpected() {
        val success: FathomResult<Int, DataError.Local> = FathomResult.Success(100)
        val failure: FathomResult<Int, DataError.Local> = FathomResult.Failure(DataError.Local.DISK_FULL)

        assertEquals(100, success.asSuccessOrNull())
        assertNull(success.asErrorOrNull())

        assertNull(failure.asSuccessOrNull())
        assertEquals(DataError.Local.DISK_FULL, failure.asErrorOrNull())
    }
}
