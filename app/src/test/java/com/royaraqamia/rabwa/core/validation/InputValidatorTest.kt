package com.royaraqamia.rabwa.core.validation

import com.royaraqamia.rabwa.core.result.AppError
import com.royaraqamia.rabwa.core.result.AppResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InputValidatorTest {

    @Test
    fun `validateNotEmpty - happy path trims and returns success`() {
        val result = InputValidator.validateNotEmpty("Title", "  Valid Input  ")
        assertTrue(result is AppResult.Success)
        assertEquals("Valid Input", (result as AppResult.Success).data)
    }

    @Test
    fun `validateNotEmpty - failure path empty string returns validation error`() {
        val result = InputValidator.validateNotEmpty("Title", "")
        assertTrue(result is AppResult.Failure)
        val error = (result as AppResult.Failure).error
        assertTrue(error is AppError.Validation)
        assertEquals("Title", (error as AppError.Validation).field)
    }

    @Test
    fun `validateNotEmpty - edge case whitespace only returns validation error`() {
        val result = InputValidator.validateNotEmpty("Title", "    ")
        assertTrue(result is AppResult.Failure)
    }

    @Test
    fun `validateLength - happy path within range succeeds`() {
        val result = InputValidator.validateLength("Code", "TEST", minLength = 2, maxLength = 10)
        assertTrue(result is AppResult.Success)
        assertEquals("TEST", (result as AppResult.Success).data)
    }

    @Test
    fun `validateLength - edge case exact min and max bounds succeed`() {
        val minResult = InputValidator.validateLength("Code", "AB", minLength = 2, maxLength = 4)
        assertTrue(minResult is AppResult.Success)

        val maxResult = InputValidator.validateLength("Code", "ABCD", minLength = 2, maxLength = 4)
        assertTrue(maxResult is AppResult.Success)
    }

    @Test
    fun `validateLength - failure path too short returns error`() {
        val result = InputValidator.validateLength("Code", "A", minLength = 2, maxLength = 10)
        assertTrue(result is AppResult.Failure)
        val error = (result as AppResult.Failure).error as AppError.Validation
        assertTrue(error.message.contains("at least 2 characters"))
    }

    @Test
    fun `validateLength - failure path too long returns error`() {
        val result = InputValidator.validateLength("Code", "TOOLONGSTRING", minLength = 2, maxLength = 5)
        assertTrue(result is AppResult.Failure)
        val error = (result as AppResult.Failure).error as AppError.Validation
        assertTrue(error.message.contains("cannot exceed 5 characters"))
    }

    @Test
    fun `validateEmail - happy path standard email succeeds`() {
        val result = InputValidator.validateEmail("UserEmail", "developer@enterprise.org")
        assertTrue(result is AppResult.Success)
        assertEquals("developer@enterprise.org", (result as AppResult.Success).data)
    }

    @Test
    fun `validateEmail - failure path invalid formats return validation error`() {
        val invalidEmails = listOf("plainaddress", "missing@domain", "@missingusername.com", "user@.com")
        for (email in invalidEmails) {
            val result = InputValidator.validateEmail("Email", email)
            assertTrue("Failed for $email", result is AppResult.Failure)
        }
    }

    @Test
    fun `validateIdentifier - happy path alphanumeric succeeds`() {
        val result = InputValidator.validateIdentifier("Slug", "service-auth_01")
        assertTrue(result is AppResult.Success)
    }

    @Test
    fun `validateIdentifier - failure path special symbols rejected`() {
        val result = InputValidator.validateIdentifier("Slug", "service/auth?id=1")
        assertTrue(result is AppResult.Failure)
    }
}
