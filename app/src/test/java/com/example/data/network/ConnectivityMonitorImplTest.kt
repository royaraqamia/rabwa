package com.example.data.network

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.domain.model.NetworkStatus
import com.example.testutil.TestCoroutineDispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ConnectivityMonitorImplTest {

    @Test
    fun `connectivity monitor initializes cleanly without exception`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dispatchers = TestCoroutineDispatchers()
        val monitor = ConnectivityMonitorImpl(context, dispatchers)

        // isCurrentlyConnected should evaluate without crashing
        val isConnected = monitor.isCurrentlyConnected
        assertNotNull(isConnected)
    }

    @Test
    fun `networkStatus flow emits initial status without throwing`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dispatchers = TestCoroutineDispatchers()
        val monitor = ConnectivityMonitorImpl(context, dispatchers)

        val initialStatus = monitor.networkStatus.first()
        assertNotNull(initialStatus)
    }
}
