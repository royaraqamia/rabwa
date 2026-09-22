package com.royaraqamia.rabwa

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.royaraqamia.rabwa.core.network.SupabaseClientProvider
import com.royaraqamia.rabwa.data.local.theme.SharedPreferencesThemePreferencesManager
import com.royaraqamia.rabwa.data.local.session.SharedPreferencesSessionManager
import com.royaraqamia.rabwa.core.notification.NotificationChannelManager
import com.royaraqamia.rabwa.presentation.ArchitectureViewModel
import com.royaraqamia.rabwa.presentation.ArchitectureViewModelFactory
import com.royaraqamia.rabwa.presentation.auth.AuthViewModel
import com.royaraqamia.rabwa.presentation.notification.NotificationViewModel
import com.royaraqamia.rabwa.ui.screen.MainAppScreen
import com.royaraqamia.rabwa.ui.theme.RabwaTheme
import com.royaraqamia.rabwa.ui.theme.ThemeMode
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.jan.supabase.auth.handleDeeplinks
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val themePreferencesManager by lazy {
        SharedPreferencesThemePreferencesManager.getInstance(applicationContext)
    }

    private val viewModel: ArchitectureViewModel by viewModels {
        ArchitectureViewModelFactory.getInstance(applicationContext)
    }

    private val authViewModel: AuthViewModel by viewModels {
        ArchitectureViewModelFactory.getInstance(applicationContext)
    }

    private val notificationViewModel: NotificationViewModel by viewModels {
        ArchitectureViewModelFactory.getInstance(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        NotificationChannelManager.ensureDefaultChannel(applicationContext)
        SupabaseClientProvider.setSessionManager(SharedPreferencesSessionManager(applicationContext))
        handleAuthIntent(intent)
        handleNotificationIntent(intent)

        setContent {
            val themeMode by themePreferencesManager.themeModeState.collectAsStateWithLifecycle()

            RabwaTheme(themeMode = themeMode) {
                MainAppScreen(
                    architectureViewModel = viewModel,
                    authViewModel = authViewModel,
                    notificationViewModel = notificationViewModel,
                    themeMode = themeMode,
                    onThemeModeChange = { newMode ->
                        themePreferencesManager.setThemeMode(newMode)
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleAuthIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleAuthIntent(intent: Intent?) {
        if (intent == null) return
        lifecycleScope.launch {
            try {
                SupabaseClientProvider.getClient().handleDeeplinks(intent)
            } catch (_: Exception) {
                // Ignore non-auth deep link intents
            }
        }
    }

    private fun handleNotificationIntent(intent: Intent?) {
        if (intent == null) return
        val title = intent.getStringExtra(com.royaraqamia.rabwa.data.service.AppFirebaseMessagingService.EXTRA_NOTIFICATION_TITLE)
            ?: intent.getStringExtra("title")
            ?: intent.getStringExtra("gcm.notification.title")
        val body = intent.getStringExtra(com.royaraqamia.rabwa.data.service.AppFirebaseMessagingService.EXTRA_NOTIFICATION_BODY)
            ?: intent.getStringExtra("body")
            ?: intent.getStringExtra("gcm.notification.body")
        if (title != null && body != null) {
            // Securely handle any payload details or update UI status if clicked from notification tray
            android.util.Log.d("MainActivity", "Notification tapped: $title - $body")
        }
    }
}
