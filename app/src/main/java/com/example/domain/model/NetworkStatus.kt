package com.example.domain.model

/**
 * Domain representation of real-time network connectivity status.
 * Clean, immutable, and strictly typed.
 */
sealed interface NetworkStatus {
    val isConnected: Boolean

    /**
     * Active network connection with internet access is established.
     */
    data object Available : NetworkStatus {
        override val isConnected: Boolean = true
    }

    /**
     * No active network connection is available.
     */
    data object Unavailable : NetworkStatus {
        override val isConnected: Boolean = false
    }

    /**
     * Network connection was lost or disconnected.
     */
    data object Lost : NetworkStatus {
        override val isConnected: Boolean = false
    }

    companion object {
        val Connected: NetworkStatus get() = Available
        val Disconnected: NetworkStatus get() = Unavailable
    }
}
