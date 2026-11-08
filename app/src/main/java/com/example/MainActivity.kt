package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.InAppNotification
import com.example.model.NotificationType
import com.example.ui.components.NotificationHost
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MainMapScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.RoadDark
import com.example.ui.theme.SpeedCameraRed
import com.example.ui.theme.WazeCyanLight
import com.example.ui.theme.WazeCyanPrimary
import com.example.viewmodel.AppScreen
import com.example.viewmodel.FamRutaViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: FamRutaViewModel = viewModel()
                val uiState by viewModel.uiState.collectAsState()
                val snackbarHostState = remember { SnackbarHostState() }

                // Collect snackbarAlert StateFlow and display auto-dismissing Material 3 Snackbars
                LaunchedEffect(viewModel) {
                    viewModel.snackbarAlert.collectLatest { alert ->
                        if (alert != null) {
                            val prefix = if (alert.iconEmoji != null) "${alert.iconEmoji} " else ""
                            val result = snackbarHostState.showSnackbar(
                                message = "$prefix${alert.message}",
                                actionLabel = alert.actionLabel,
                                duration = SnackbarDuration.Short
                            )
                            viewModel.dismissSnackbarAlert()
                        }
                    }
                }

                Scaffold(
                    snackbarHost = {
                        SnackbarHost(hostState = snackbarHostState) { data ->
                            Snackbar(
                                snackbarData = data,
                                shape = RoundedCornerShape(14.dp),
                                containerColor = RoadDark.copy(alpha = 0.95f),
                                contentColor = Color.White,
                                actionColor = WazeCyanLight,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AnimatedContent(
                            targetState = uiState.currentScreen,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(350)) togetherWith
                                        fadeOut(animationSpec = tween(350))
                            },
                            label = "ScreenTransition",
                            modifier = Modifier.fillMaxSize()
                        ) { screen ->
                            when (screen) {
                                AppScreen.SPLASH -> {
                                    SplashScreen(
                                        onSplashFinished = {
                                            // Check if user is already signed in
                                            val currentUser = Firebase.auth.currentUser
                                            if (currentUser != null) {
                                                viewModel.navigateTo(AppScreen.MAIN_MAP)
                                                viewModel.showSnackbarAlert(
                                                    message = "Conectado como ${currentUser.displayName ?: currentUser.email}",
                                                    iconEmoji = "👋"
                                                )
                                            } else {
                                                viewModel.navigateTo(AppScreen.LOGIN)
                                            }
                                        }
                                    )
                                }

                                AppScreen.LOGIN -> {
                                    LoginScreen(
                                        onLoginSuccess = { user ->
                                            viewModel.navigateTo(AppScreen.MAIN_MAP)
                                            viewModel.showSnackbarAlert(
                                                message = "Sesión iniciada con éxito. GPS familiar en la nube.",
                                                iconEmoji = "✅"
                                            )
                                        },
                                        onContinueAsGuest = {
                                            viewModel.navigateTo(AppScreen.MAIN_MAP)
                                            viewModel.showSnackbarAlert(
                                                message = "Modo local activo",
                                                iconEmoji = "🚀"
                                            )
                                        }
                                    )
                                }

                                AppScreen.MAIN_MAP -> {
                                    MainMapScreen(viewModel = viewModel)
                                }

                                AppScreen.SETTINGS -> {
                                    BackHandler {
                                        viewModel.navigateTo(AppScreen.MAIN_MAP)
                                    }
                                    SettingsScreen(
                                        viewModel = viewModel,
                                        onBack = { viewModel.navigateTo(AppScreen.MAIN_MAP) },
                                        onNavigateToLogin = { viewModel.navigateTo(AppScreen.LOGIN) }
                                    )
                                }

                                AppScreen.PROFILE -> {
                                    BackHandler {
                                        viewModel.navigateTo(AppScreen.SETTINGS)
                                    }
                                    com.example.ui.screens.UserProfileScreen(
                                        viewModel = viewModel,
                                        onBack = { viewModel.navigateTo(AppScreen.SETTINGS) }
                                    )
                                }
                            }
                        }

                        // Top notifications banner overlay
                        if (uiState.currentScreen != AppScreen.MAIN_MAP) {
                            NotificationHost(
                                notifications = uiState.activeNotifications,
                                onDismissNotification = { viewModel.dismissNotification(it) },
                                modifier = Modifier.align(Alignment.TopCenter)
                            )
                        }
                    }
                }
            }
        }
    }
}
