package com.example.ui.component

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.example.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AppProgressIndicatorTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `when standalone indicator is rendered, it is displayed with default tag`() {
        composeTestRule.setContent {
            MaterialTheme {
                AppCircularProgressIndicator(
                    testTag = "custom_progress_indicator"
                )
            }
        }

        composeTestRule.onNodeWithTag("custom_progress_indicator").assertIsDisplayed()
        composeTestRule.onNodeWithTag("custom_progress_indicator_label").assertDoesNotExist()
    }

    @Test
    fun `when inline indicator is rendered with label, both spinner and label text are displayed`() {
        val labelText = "جارٍ المعالجة..."

        composeTestRule.setContent {
            MaterialTheme {
                AppCircularProgressIndicator(
                    label = labelText,
                    testTag = "test_indicator"
                )
            }
        }

        composeTestRule.onNodeWithTag("test_indicator").assertIsDisplayed()
        composeTestRule.onNodeWithTag("test_indicator_spinner").assertIsDisplayed()
        composeTestRule.onNodeWithTag("test_indicator_label").assertIsDisplayed()
        composeTestRule.onNodeWithText(labelText).assertIsDisplayed()
    }

    @Test
    fun `when custom size and colors are specified, indicator renders without exception`() {
        composeTestRule.setContent {
            MaterialTheme {
                AppCircularProgressIndicator(
                    size = 48.dp,
                    strokeWidth = 4.dp,
                    color = Color.Green,
                    trackColor = Color.LightGray,
                    label = "جارٍ الحفظ...",
                    testTag = "styled_indicator"
                )
            }
        }

        composeTestRule.onNodeWithTag("styled_indicator").assertIsDisplayed()
        composeTestRule.onNodeWithTag("styled_indicator_spinner").assertIsDisplayed()
        composeTestRule.onNodeWithTag("styled_indicator_label").assertIsDisplayed()
    }

    @Test
    fun `when LoadingOverlay rendered with isLoading false, it does not exist in hierarchy`() {
        composeTestRule.setContent {
            MaterialTheme {
                LoadingOverlay(
                    isLoading = false,
                    message = "جارٍ التحميل...",
                    testTag = "test_loading_overlay"
                )
            }
        }

        composeTestRule.onNodeWithTag("test_loading_overlay").assertDoesNotExist()
        composeTestRule.onNodeWithTag("test_loading_overlay_card").assertDoesNotExist()
    }

    @Test
    fun `when LoadingOverlay rendered with isLoading true, overlay card, spinner, and message are displayed`() {
        val messageText = "جارٍ الاتصال بقاعدة البيانات..."

        composeTestRule.setContent {
            MaterialTheme {
                LoadingOverlay(
                    isLoading = true,
                    message = messageText,
                    testTag = "test_loading_overlay"
                )
            }
        }

        composeTestRule.onNodeWithTag("test_loading_overlay").assertIsDisplayed()
        composeTestRule.onNodeWithTag("test_loading_overlay_card", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("test_loading_overlay_spinner", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithTag("test_loading_overlay_message", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText(messageText).assertIsDisplayed()
    }

    @Test
    fun `when LoadingOverlay uses default string resource, it displays localized message`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val defaultMessage = context.getString(R.string.loading_generic)

        composeTestRule.setContent {
            MaterialTheme {
                LoadingOverlay(
                    isLoading = true,
                    testTag = "default_loading_overlay"
                )
            }
        }

        composeTestRule.onNodeWithTag("default_loading_overlay").assertIsDisplayed()
        composeTestRule.onNodeWithText(defaultMessage).assertIsDisplayed()
    }

    @Test
    fun `when LoadingOverlay is displayed, it prevents clicks on background elements`() {
        var backgroundButtonClicked = false

        composeTestRule.setContent {
            MaterialTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    Button(
                        onClick = { backgroundButtonClicked = true },
                        modifier = Modifier.testTag("background_button")
                    ) {
                        Text("زر الخلفية")
                    }

                    LoadingOverlay(
                        isLoading = true,
                        message = "جارٍ المعالجة في الخلفية...",
                        testTag = "blocking_overlay"
                    )
                }
            }
        }

        composeTestRule.onNodeWithTag("blocking_overlay").assertIsDisplayed()
        // Click on the overlay area above the button
        composeTestRule.onNodeWithTag("blocking_overlay").performClick()
        assertFalse("Background button should not be clicked through overlay", backgroundButtonClicked)
    }

    @Test
    fun `when toggle isLoading state dynamically, overlay transitions seamlessly`() {
        var loadingState by mutableStateOf(false)

        composeTestRule.setContent {
            MaterialTheme {
                LoadingOverlay(
                    isLoading = loadingState,
                    message = "جارٍ التحديث...",
                    testTag = "dynamic_overlay"
                )
            }
        }

        composeTestRule.onNodeWithTag("dynamic_overlay").assertDoesNotExist()

        loadingState = true
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("dynamic_overlay").assertIsDisplayed()

        loadingState = false
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("dynamic_overlay").assertDoesNotExist()
    }
}
