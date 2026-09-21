package com.example.ui.component

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.R
import com.example.core.result.AppError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class GlobalErrorBannerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `when error is null banner is not displayed`() {
        composeTestRule.setContent {
            MaterialTheme {
                GlobalErrorBanner(
                    error = null,
                    onDismiss = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("global_error_banner").assertDoesNotExist()
    }

    @Test
    fun `when database error occurs banner displays localized message without leaking db details`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val expectedMessage = context.getString(R.string.error_database_generic)
        var dismissed = false

        composeTestRule.setContent {
            MaterialTheme {
                GlobalErrorBanner(
                    error = AppError.Database(message = "Corrupted SQLite master table at /data/user/0/databases/app.db"),
                    onDismiss = { dismissed = true }
                )
            }
        }

        composeTestRule.onNodeWithTag("global_error_banner").assertIsDisplayed()
        composeTestRule.onNodeWithTag("global_error_icon").assertIsDisplayed()
        composeTestRule.onNodeWithText(expectedMessage).assertIsDisplayed()

        // Raw internal DB file path must NEVER exist anywhere in the component tree
        composeTestRule.onNodeWithText("Corrupted SQLite master table", substring = true).assertDoesNotExist()

        // Test dismiss button
        composeTestRule.onNodeWithTag("global_error_dismiss_button").performClick()
        assertTrue(dismissed)
    }

    @Test
    fun `when retryable error with retry callback is passed, retry button is clickable`() {
        var retried = false

        composeTestRule.setContent {
            MaterialTheme {
                GlobalErrorBanner(
                    error = AppError.Network(statusCode = 500, message = "Internal 500 downstream failure"),
                    onDismiss = {},
                    onRetry = { retried = true }
                )
            }
        }

        composeTestRule.onNodeWithTag("global_error_retry_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("global_error_retry_button").performClick()
        assertTrue(retried)
    }

    @Test
    fun `when non-retryable error is passed, retry button is not displayed`() {
        composeTestRule.setContent {
            MaterialTheme {
                GlobalErrorBanner(
                    error = AppError.Validation(field = "title", message = "Field empty"),
                    onDismiss = {},
                    onRetry = { /* should not show retry */ }
                )
            }
        }

        composeTestRule.onNodeWithTag("global_error_retry_button").assertDoesNotExist()
    }

    @Test
    fun `when no internet error occurs banner displays localized No Internet message`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val expectedMessage = context.getString(R.string.error_no_internet)
        var retried = false

        composeTestRule.setContent {
            MaterialTheme {
                GlobalErrorBanner(
                    error = AppError.Network.noInternet(),
                    onDismiss = {},
                    onRetry = { retried = true }
                )
            }
        }

        composeTestRule.onNodeWithTag("global_error_banner").assertIsDisplayed()
        composeTestRule.onNodeWithTag("global_error_icon").assertIsDisplayed()
        composeTestRule.onNodeWithText(expectedMessage).assertIsDisplayed()
        composeTestRule.onNodeWithTag("global_error_retry_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("global_error_retry_button").performClick()
        assertTrue(retried)
    }
}
