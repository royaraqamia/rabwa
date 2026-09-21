package com.example.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.presentation.ArchitectureViewModel
import com.example.presentation.ArchitectureViewModelFactory
import com.example.presentation.auth.AuthViewModel
import com.example.presentation.auth.state.AuthUiState
import com.example.presentation.notification.NotificationViewModel
import com.example.ui.icon.Bell
import com.example.ui.icon.Home
import com.example.ui.icon.LucideIcons
import com.example.ui.icon.User
import com.example.ui.theme.ThemeMode
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close

enum class AppNavigationDestination {
    HOME,
    PROFILE,
    LOGIN
}

@Composable
fun MainAppScreen(
    architectureViewModel: ArchitectureViewModel,
    authViewModel: AuthViewModel,
    notificationViewModel: NotificationViewModel? = null,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentDestination by rememberSaveable { mutableStateOf(AppNavigationDestination.HOME) }
    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val isAuthenticated = authUiState is AuthUiState.Authenticated

    var activeInAppNotification by remember { mutableStateOf<com.example.domain.model.notification.PushNotificationPayload?>(null) }

    LaunchedEffect(Unit) {
        try {
            val factory = ArchitectureViewModelFactory.getInstance(context)
            factory.notificationRepository.foregroundNotifications.collect { payload ->
                activeInAppNotification = payload
            }
        } catch (_: Exception) {}
    }

    LaunchedEffect(activeInAppNotification) {
        if (activeInAppNotification != null) {
            kotlinx.coroutines.delay(5000)
            activeInAppNotification = null
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            // Show bottom navigation bar on HOME and PROFILE screens
            if (currentDestination != AppNavigationDestination.LOGIN) {
                NavigationBar(
                    modifier = Modifier.testTag("app_bottom_navigation_bar"),
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    NavigationBarItem(
                        selected = currentDestination == AppNavigationDestination.HOME,
                        onClick = { currentDestination = AppNavigationDestination.HOME },
                        icon = {
                            Icon(
                                imageVector = LucideIcons.Home,
                                contentDescription = stringResource(R.string.nav_home)
                            )
                        },
                        label = { Text(stringResource(R.string.nav_home)) },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("nav_home_tab")
                    )

                    NavigationBarItem(
                        selected = currentDestination == AppNavigationDestination.PROFILE,
                        onClick = { currentDestination = AppNavigationDestination.PROFILE },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (isAuthenticated) {
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(6.dp)
                                        )
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = LucideIcons.User,
                                    contentDescription = stringResource(R.string.nav_profile)
                                )
                            }
                        },
                        label = { Text(stringResource(R.string.nav_profile)) },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("nav_profile_tab")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentDestination,
                transitionSpec = {
                    if (targetState == AppNavigationDestination.LOGIN) {
                        (slideInHorizontally { width -> width } + fadeIn())
                            .togetherWith(slideOutHorizontally { width -> -width } + fadeOut())
                    } else if (initialState == AppNavigationDestination.LOGIN) {
                        (slideInHorizontally { width -> -width } + fadeIn())
                            .togetherWith(slideOutHorizontally { width -> width } + fadeOut())
                    } else {
                        fadeIn().togetherWith(fadeOut())
                    }
                },
                label = "ScreenTransition"
            ) { destination ->
                when (destination) {
                    AppNavigationDestination.HOME -> {
                        ArchitectureDashboardScreen(
                            viewModel = architectureViewModel,
                            themeMode = themeMode,
                            onThemeModeChange = onThemeModeChange,
                            onNavigateToProfile = {
                                currentDestination = AppNavigationDestination.PROFILE
                            }
                        )
                    }
                    AppNavigationDestination.PROFILE -> {
                        ProfileScreen(
                            authViewModel = authViewModel,
                            notificationViewModel = notificationViewModel,
                            onNavigateToLogin = {
                                currentDestination = AppNavigationDestination.LOGIN
                            },
                            themeMode = themeMode,
                            onThemeModeChange = onThemeModeChange
                        )
                    }
                    AppNavigationDestination.LOGIN -> {
                        LoginScreen(
                            authViewModel = authViewModel,
                            onNavigateBack = {
                                currentDestination = AppNavigationDestination.HOME
                            }
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = activeInAppNotification != null,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .zIndex(99f)
            ) {
                activeInAppNotification?.let { notification ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 480.dp)
                            .testTag("in_app_notification_card")
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = LucideIcons.Bell,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = notification.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = notification.body,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                            IconButton(onClick = { activeInAppNotification = null }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
