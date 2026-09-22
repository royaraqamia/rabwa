package com.royaraqamia.rabwa.domain.usecase

import com.royaraqamia.rabwa.domain.model.NetworkStatus
import com.royaraqamia.rabwa.domain.network.ConnectivityMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ObserveNetworkStatusUseCaseTest {

    @Test
    fun `use case delegates networkStatus flow directly from monitor`() = runTest {
        val fakeMonitor = object : ConnectivityMonitor {
            override val networkStatus: Flow<NetworkStatus> = flowOf(NetworkStatus.Available)
            override val isCurrentlyConnected: Boolean = true
        }

        val useCase = ObserveNetworkStatusUseCase(fakeMonitor)
        val result = useCase().first()

        assertEquals(NetworkStatus.Available, result)
        assertTrue(result.isConnected)
    }

    @Test
    fun `use case emits unavailable status when network is disconnected`() = runTest {
        val fakeMonitor = object : ConnectivityMonitor {
            override val networkStatus: Flow<NetworkStatus> = flowOf(NetworkStatus.Unavailable)
            override val isCurrentlyConnected: Boolean = false
        }

        val useCase = ObserveNetworkStatusUseCase(fakeMonitor)
        val result = useCase().first()

        assertEquals(NetworkStatus.Unavailable, result)
        org.junit.Assert.assertFalse(result.isConnected)
    }
}
