package com.royaraqamia.rabwa.core.validation

import com.royaraqamia.rabwa.core.result.AppResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthValidatorTest {

    @Test
    fun `validateEmail returns Success for valid emails`() {
        val validEmails = listOf(
            "test@example.com",
            "user.name+tag@sub.domain.org",
            "admin@company.io"
        )
        for (email in validEmails) {
            val result = AuthValidator.validateEmail(email)
            assertTrue("Expected valid email for $email", result is AppResult.Success)
            assertEquals(email.trim(), (result as AppResult.Success).data)
        }
    }

    @Test
    fun `validateEmail returns Failure for invalid emails`() {
        val invalidEmails = listOf(
            "",
            "   ",
            "plainaddress",
            "@missingusername.com",
            "username@.com",
            "username@domain..com"
        )
        for (email in invalidEmails) {
            val result = AuthValidator.validateEmail(email)
            assertTrue("Expected failure for '$email'", result is AppResult.Failure)
        }
    }

    @Test
    fun `validatePassword checks minimum length`() {
        val shortResult = AuthValidator.validatePassword("12345", minLength = 6)
        assertTrue(shortResult is AppResult.Failure)

        val validResult = AuthValidator.validatePassword("secret123", minLength = 6)
        assertTrue(validResult is AppResult.Success)
    }

    @Test
    fun `validateIdToken checks three part JWT format`() {
        val invalidToken = "invalid_token_without_dots"
        val failResult = AuthValidator.validateIdToken(invalidToken)
        assertTrue(failResult is AppResult.Failure)

        val validToken = "header.payload.signature"
        val successResult = AuthValidator.validateIdToken(validToken)
        assertTrue(successResult is AppResult.Success)
    }

    @Test
    fun `validateBucketName enforces lowercase alphanumeric and length`() {
        val validBuckets = listOf("avatars", "user-docs", "backup.files.2026")
        for (bucket in validBuckets) {
            val result = AuthValidator.validateBucketName(bucket)
            assertTrue("Expected valid bucket for $bucket", result is AppResult.Success)
        }

        val invalidBuckets = listOf("ab", "", "UPPERCASE", "invalid_bucket_with_underscores_!@#")
        for (bucket in invalidBuckets) {
            val result = AuthValidator.validateBucketName(bucket)
            assertTrue("Expected failure for bucket '$bucket'", result is AppResult.Failure)
        }
    }

    @Test
    fun `sanitizeStoragePath removes directory traversal and extra slashes`() {
        val traversalPath = "documents/../secret.txt"
        val failResult = AuthValidator.sanitizeStoragePath(traversalPath)
        assertTrue(failResult is AppResult.Failure)

        val validPath = "  //user/123//avatar.png  "
        val successResult = AuthValidator.sanitizeStoragePath(validPath)
        assertTrue(successResult is AppResult.Success)
        assertEquals("user/123/avatar.png", (successResult as AppResult.Success).data)
    }
}
