package com.royaraqamia.rabwa.domain.usecase

import com.royaraqamia.rabwa.domain.model.NetworkStatus
import com.royaraqamia.rabwa.domain.network.ConnectivityMonitor
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
