package com.example.domain.network

import com.example.domain.model.NetworkStatus
import kotlinx.coroutines.flow.Flow

/**
 * Enterprise domain contract for observing real-time network connectivity changes.
 * Pure interface strictly decoupled from the Android framework and SDK specifics.
 */
interface ConnectivityMonitor {
    /**
     * Cold flow emitting real-time [NetworkStatus] updates whenever connectivity changes.
     */
    val networkStatus: Flow<NetworkStatus>

    /**
     * Synchronous check for instantaneous network connectivity.
     */
    val isCurrentlyConnected: Boolean
}
