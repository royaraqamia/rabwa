package com.royaraqamia.rabwa.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.royaraqamia.rabwa.core.dispatcher.CoroutineDispatchers
import com.royaraqamia.rabwa.domain.model.NetworkStatus
import com.royaraqamia.rabwa.domain.network.ConnectivityMonitor
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn

/**
 * Enterprise implementation of [ConnectivityMonitor] utilizing Android [ConnectivityManager].
 *
 * Adheres to:
 * 1. Zero resource leakage with structured lifecycle cleanup in [awaitClose].
 * 2. Real-time reactivity using [ConnectivityManager.NetworkCallback].
 * 3. Graceful fallback when permissions or connectivity services are restricted.
 * 4. Dispatcher isolation on [CoroutineDispatchers.io].
 */
class ConnectivityMonitorImpl(
    context: Context,
    private val dispatchers: CoroutineDispatchers
) : ConnectivityMonitor {

    private val connectivityManager: ConnectivityManager? =
        context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    override val isCurrentlyConnected: Boolean
        get() {
            val cm = connectivityManager ?: return false
            return try {
                val activeNetwork = cm.activeNetwork ?: return false
                val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            } catch (e: Exception) {
                false
            }
        }

    override val networkStatus: Flow<NetworkStatus> = callbackFlow {
        val cm = connectivityManager
        if (cm == null) {
            trySend(NetworkStatus.Unavailable)
            close()
            return@callbackFlow
        }

        // Emit initial instantaneous connection status
        val initialStatus = if (isCurrentlyConnected) NetworkStatus.Available else NetworkStatus.Unavailable
        trySend(initialStatus)

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(NetworkStatus.Available)
            }

            override fun onLost(network: Network) {
                trySend(NetworkStatus.Lost)
            }

            override fun onUnavailable() {
                trySend(NetworkStatus.Unavailable)
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                trySend(if (hasInternet) NetworkStatus.Available else NetworkStatus.Unavailable)
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        try {
            cm.registerNetworkCallback(request, callback)
        } catch (e: Exception) {
            trySend(NetworkStatus.Unavailable)
        }

        awaitClose {
            try {
                cm.unregisterNetworkCallback(callback)
            } catch (e: Exception) {
                // Ignore if unregister fails or already unregistered
            }
        }
    }
        .distinctUntilChanged()
        .flowOn(dispatchers.io)
}
