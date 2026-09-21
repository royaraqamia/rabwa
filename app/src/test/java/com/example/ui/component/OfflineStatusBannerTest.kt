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
import com.example.domain.model.SyncSummary
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class OfflineStatusBannerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `when online and all items synced banner is not displayed`() {
        composeTestRule.setContent {
            MaterialTheme {
                OfflineStatusBanner(
                    isOffline = false,
                    syncSummary = SyncSummary(pendingCount = 0, syncedCount = 5),
                    isSyncing = false,
                    onSyncNow = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("offline_status_banner").assertDoesNotExist()
    }

    @Test
    fun `when offline banner is displayed with offline mode message`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val expectedMessage = context.getString(R.string.offline_mode_banner)

        composeTestRule.setContent {
            MaterialTheme {
                OfflineStatusBanner(
                    isOffline = true,
                    syncSummary = SyncSummary(pendingCount = 0),
                    isSyncing = false,
                    onSyncNow = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("offline_status_banner").assertIsDisplayed()
        composeTestRule.onNodeWithText(expectedMessage).assertIsDisplayed()
        composeTestRule.onNodeWithTag("sync_now_button").assertDoesNotExist()
    }

    @Test
    fun `when online with pending sync items banner displays count and clickable sync now button`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val expectedMessage = context.getString(R.string.sync_status_pending, 3)
        var syncTriggered = false

        composeTestRule.setContent {
            MaterialTheme {
                OfflineStatusBanner(
                    isOffline = false,
                    syncSummary = SyncSummary(pendingCount = 3),
                    isSyncing = false,
                    onSyncNow = { syncTriggered = true }
                )
            }
        }

        composeTestRule.onNodeWithTag("offline_status_banner").assertIsDisplayed()
        composeTestRule.onNodeWithText(expectedMessage).assertIsDisplayed()
        composeTestRule.onNodeWithTag("sync_now_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("sync_now_button").performClick()
        assertTrue(syncTriggered)
    }

    @Test
    fun `when syncing banner displays syncing indicator`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val expectedMessage = context.getString(R.string.sync_status_syncing)

        composeTestRule.setContent {
            MaterialTheme {
                OfflineStatusBanner(
                    isOffline = false,
                    syncSummary = SyncSummary(pendingCount = 3),
                    isSyncing = true,
                    onSyncNow = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("offline_status_banner").assertIsDisplayed()
        composeTestRule.onNodeWithText(expectedMessage).assertIsDisplayed()
        composeTestRule.onNodeWithTag("sync_now_button").assertDoesNotExist()
    }
}
