package com.example.core.result

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppResultTest {

    @Test
    fun `map transforms Success value`() {
        val success: AppResult<Int> = AppResult.Success(10)
        val mapped = success.map { it * 2 }
        assertTrue(mapped is AppResult.Success)
        assertEquals(20, (mapped as AppResult.Success).data)
    }

    @Test
    fun `map propagates Failure without transformation`() {
        val error = AppError.Database("disk full")
        val failure: AppResult<Int> = AppResult.Failure(error)
        val mapped = failure.map { it * 2 }
        assertTrue(mapped is AppResult.Failure)
        assertEquals(error, (mapped as AppResult.Failure).error)
    }

    @Test
    fun `fold executes onSuccess branch for Success`() {
        val success: AppResult<String> = AppResult.Success("Clean")
        val result = success.fold(
            onSuccess = { "Handled: $it" },
            onFailure = { "Error: ${it.message}" }
        )
        assertEquals("Handled: Clean", result)
    }

    @Test
    fun `fold executes onFailure branch for Failure`() {
        val failure: AppResult<String> = AppResult.Failure(AppError.Network(500, "Server down"))
        val result = failure.fold(
            onSuccess = { "Handled: $it" },
            onFailure = { "Error: ${it.message}" }
        )
        assertEquals("Error: Server down", result)
    }

    @Test
    fun `runCatching captures exception and converts to AppError Unknown`() {
        val result = AppResult.runCatching {
            throw IllegalStateException("Fatal crash simulated")
        }
        assertTrue(result is AppResult.Failure)
        val error = (result as AppResult.Failure).error
        assertTrue(error is AppError.Unknown)
        assertEquals("Fatal crash simulated", error.message)
        assertNull(result.getOrNull())
    }
}
