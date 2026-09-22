package com.royaraqamia.rabwa.presentation.error

import com.royaraqamia.rabwa.R
import com.royaraqamia.rabwa.core.result.AppError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ErrorMessageResolverTest {

    @Test
    fun `resolve network error maps to safe localized string res and is retryable`() {
        val error = AppError.Network(statusCode = 503, message = "Raw 503 Service Unavailable SQL error")
        val resolved = ErrorMessageResolver.resolve(error)

        assertEquals(R.string.error_network_generic, resolved.messageResId)
        assertTrue(resolved.isRetryable)
        // Ensure raw exception string is NOT in the fallback
        assertFalse(resolved.fallbackMessage.contains("SQL"))
        assertFalse(resolved.fallbackMessage.contains("503"))
    }

    @Test
    fun `resolve no internet error maps to dedicated no internet string res and is retryable`() {
        val error = AppError.Network.noInternet()
        val resolved = ErrorMessageResolver.resolve(error)

        assertEquals(R.string.error_no_internet, resolved.messageResId)
        assertTrue(resolved.isRetryable)
        assertTrue(resolved.fallbackMessage.contains("No Internet"))
    }

    @Test
    fun `resolve network error with no internet flag maps to dedicated no internet string res`() {
        val error = AppError.Network(isNoInternet = true, message = "Network down")
        val resolved = ErrorMessageResolver.resolve(error)

        assertEquals(R.string.error_no_internet, resolved.messageResId)
        assertTrue(resolved.isRetryable)
    }

    @Test
    fun `resolve database error maps to safe localized string res and is retryable`() {
        val error = AppError.Database(message = "table 'records' does not exist in SQLite schema")
        val resolved = ErrorMessageResolver.resolve(error)

        assertEquals(R.string.error_database_generic, resolved.messageResId)
        assertTrue(resolved.isRetryable)
        assertFalse(resolved.fallbackMessage.contains("SQLite"))
        assertFalse(resolved.fallbackMessage.contains("table"))
    }

    @Test
    fun `resolve validation error maps to safe localized string res and is non-retryable`() {
        val error = AppError.Validation(field = "title", message = "Input rejected by regex ^[a-zA-Z0-9]$")
        val resolved = ErrorMessageResolver.resolve(error)

        assertEquals(R.string.error_validation_generic, resolved.messageResId)
        assertFalse(resolved.isRetryable)
        assertFalse(resolved.fallbackMessage.contains("regex"))
    }

    @Test
    fun `resolve unauthorized error maps to safe localized string res and is non-retryable`() {
        val error = AppError.Unauthorized(message = "JWT token expired: secret key abc123xyz")
        val resolved = ErrorMessageResolver.resolve(error)

        assertEquals(R.string.error_unauthorized_generic, resolved.messageResId)
        assertFalse(resolved.isRetryable)
        assertFalse(resolved.fallbackMessage.contains("JWT"))
        assertFalse(resolved.fallbackMessage.contains("abc123xyz"))
    }

    @Test
    fun `resolve not found error maps to safe localized string res and is non-retryable`() {
        val error = AppError.NotFound(resource = "users_table_id_9999", message = "ID not found")
        val resolved = ErrorMessageResolver.resolve(error)

        assertEquals(R.string.error_not_found_generic, resolved.messageResId)
        assertFalse(resolved.isRetryable)
        assertFalse(resolved.fallbackMessage.contains("users_table"))
    }

    @Test
    fun `resolve unknown error never leaks raw throwable message or stacktrace`() {
        val sensitiveThrowable = RuntimeException("NullPointerException at com.royaraqamia.rabwa.internal.SecretAuthManager.kt:142")
        val error = AppError.Unknown(cause = sensitiveThrowable)
        val resolved = ErrorMessageResolver.resolve(error)

        assertEquals(R.string.error_unknown_generic, resolved.messageResId)
        assertTrue(resolved.isRetryable)
        assertFalse(resolved.fallbackMessage.contains("NullPointerException"))
        assertFalse(resolved.fallbackMessage.contains("SecretAuthManager"))
    }
}
