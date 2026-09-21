package com.example.domain.usecase

import com.example.domain.model.NetworkStatus
import com.example.domain.network.ConnectivityMonitor
import kotlinx.coroutines.flow.Flow

/**
 * Use case encapsulating business rules for observing real-time network connectivity.
 */
class ObserveNetworkStatusUseCase(
    private val connectivityMonitor: ConnectivityMonitor
) {
    operator fun invoke(): Flow<NetworkStatus> {
        return connectivityMonitor.networkStatus
    }
}
