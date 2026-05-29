package org.example.yankap

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.example.yankap.ui.auth.AuthStep
import org.example.yankap.ui.auth.AuthViewModel
import org.example.yankap.ui.components.CameraScanner
import org.example.yankap.ui.screens.*

enum class AppScreen(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    OPERATIONS("Operations", Icons.Default.GridView),
    TRANSFER("Transfer", Icons.Default.Refresh),
    HISTORY("History", Icons.Default.History),
    PROFILE("Profile", Icons.Default.AccountCircle)
}

@Suppress("FunctionName")
@Composable
fun App() {
    // Resolve Activity — needed by PhoneAuthProvider for SMS verification
    val activity = LocalActivity.current ?: return

    val authViewModel: AuthViewModel = viewModel()
    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()

    var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
    var scannedNumber by remember { mutableStateOf<String?>(null) }
    var isCameraOpen by remember { mutableStateOf(false) }

    val green = Color(0xFF1A7A4A)
    val lightGreen = Color(0xFFE8F5EE)

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.White
        ) {
            when (uiState.step) {

                AuthStep.PHONE_ENTRY -> {
                    PhoneEntryScreen(
                        isLoading = uiState.isLoading,
                        error = uiState.error,
                        onSendOtp = { number ->
                            authViewModel.sendOtp(number, activity)
                        },
                        onErrorDismissed = { authViewModel.clearError() }
                    )
                }

                AuthStep.OTP_VERIFICATION -> {
                    OtpVerificationScreen(
                        phoneNumber = uiState.phoneNumber,
                        isLoading = uiState.isLoading,
                        error = uiState.error,
                        onVerify = { code -> authViewModel.verifyOtp(code) },
                        onBack = { authViewModel.goBack() },
                        onResend = { authViewModel.sendOtp(uiState.phoneNumber, activity) },
                        onErrorDismissed = { authViewModel.clearError() }
                    )
                }

                AuthStep.AUTHENTICATED -> {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            NavigationBar(containerColor = Color.White) {
                                AppScreen.values().forEach { screen ->
                                    NavigationBarItem(
                                        selected = currentScreen == screen,
                                        onClick = { currentScreen = screen },
                                        icon = {
                                            Icon(
                                                imageVector = screen.icon,
                                                contentDescription = screen.label,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        },
                                        label = {
                                            Text(screen.label, fontSize = 11.sp)
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = green,
                                            selectedTextColor = green,
                                            indicatorColor = lightGreen,
                                            unselectedIconColor = Color.Gray,
                                            unselectedTextColor = Color.Gray
                                        )
                                    )
                                }
                            }
                        },
                        floatingActionButton = {
                            FloatingActionButton(
                                onClick = { isCameraOpen = true },
                                containerColor = green,
                                contentColor = Color.White,
                                shape = CircleShape
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = "Scan Number"
                                )
                            }
                        },
                        floatingActionButtonPosition = FabPosition.End
                    ) { paddingValues ->
                        Box(modifier = Modifier.padding(paddingValues)) {
                            when (currentScreen) {
                                AppScreen.HOME -> HomeScreenContent()
                                AppScreen.OPERATIONS -> OperationsScreen()
                                AppScreen.TRANSFER -> TransferScreen(initialRecipient = scannedNumber)
                                AppScreen.HISTORY -> HistoryScreen()
                                AppScreen.PROFILE -> ProfileScreen()
                            }
                        }
                    }

                    if (isCameraOpen) {
                        CameraScanner(
                            onNumberScanned = { number ->
                                scannedNumber = number
                                isCameraOpen = false
                                currentScreen = AppScreen.TRANSFER
                            },
                            onClose = { isCameraOpen = false }
                        )
                    }
                }
            }
        }
    }
}