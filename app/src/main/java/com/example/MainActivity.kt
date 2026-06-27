package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme

enum class Screen {
    LOGIN,
    MAIN,
    CHAT_DETAIL,
    ADMIN_PANEL
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: ChatViewModel = viewModel()
            val themeSetting by viewModel.appTheme.collectAsState()
            val isDarkTheme = when (themeSetting) {
                AppThemeSetting.LIGHT -> false
                AppThemeSetting.DARK -> true
                AppThemeSetting.SYSTEM -> isSystemInDarkTheme()
            }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    var currentScreen by remember { mutableStateOf(Screen.MAIN) }
                    var activeChatId by remember { mutableStateOf<Long?>(null) }

                    // Global overlays
                    val callState by viewModel.callState.collectAsState()
                    val activeStories by viewModel.activeStatusViewer.collectAsState()

                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                            // Primary Screen router
                            when (currentScreen) {
                                Screen.LOGIN -> {
                                    LoginScreen(
                                        viewModel = viewModel,
                                        onLoginSuccess = {
                                            currentScreen = Screen.MAIN
                                        }
                                    )
                                }
                                Screen.MAIN -> {
                                    MainScreen(
                                        viewModel = viewModel,
                                        onChatClick = { id ->
                                            activeChatId = id
                                            viewModel.selectChat(id)
                                            currentScreen = Screen.CHAT_DETAIL
                                        },
                                        onAdminClick = {
                                            currentScreen = Screen.ADMIN_PANEL
                                        }
                                    )
                                }
                                Screen.CHAT_DETAIL -> {
                                    activeChatId?.let { id ->
                                        ChatDetailScreen(
                                            viewModel = viewModel,
                                            chatId = id,
                                            onBackClick = {
                                                currentScreen = Screen.MAIN
                                                activeChatId = null
                                            }
                                        )
                                    } ?: run {
                                        currentScreen = Screen.MAIN
                                    }
                                }
                                Screen.ADMIN_PANEL -> {
                                    AdminPanelScreen(
                                        viewModel = viewModel,
                                        onBackClick = {
                                            currentScreen = Screen.MAIN
                                        }
                                    )
                                }
                            }

                            // --- Global overlay 1: Timed Stories Slideshow ---
                            activeStories?.let { list ->
                                StatusViewerScreen(
                                    viewModel = viewModel,
                                    updates = list,
                                    onClose = { viewModel.closeStatusViewer() }
                                )
                            }

                            // --- Global overlay 2: Fullscreen WebRTC Calling overlay ---
                            if (callState !is CallState.Idle) {
                                CallScreen(
                                    viewModel = viewModel,
                                    callState = callState
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
