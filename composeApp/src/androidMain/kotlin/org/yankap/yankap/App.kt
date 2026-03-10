package org.yankap.yankap

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import org.yankap.yankap.ui.features.history.HistoryScreen
import org.yankap.yankap.ui.features.settings.SettingsScreen
import org.yankap.yankap.ui.features.transactions.TransactionScreen

// Simple manual navigation state matching the defined screens
enum class Screen {
    TRANSACTION,
    HISTORY,
    SETTINGS
}

@Composable
@Preview
fun App() {
    MaterialTheme {
        var currentScreen by remember { mutableStateOf(Screen.TRANSACTION) }

        when (currentScreen) {
            Screen.TRANSACTION -> {
                TransactionScreen(
                    onNavigateToHistory = { currentScreen = Screen.HISTORY },
                    onNavigateToSettings = { currentScreen = Screen.SETTINGS }
                )
            }
            Screen.HISTORY -> {
                HistoryScreen(
                    onNavigateBack = { currentScreen = Screen.TRANSACTION }
                )
            }
            Screen.SETTINGS -> {
                SettingsScreen(
                    onNavigateBack = { currentScreen = Screen.TRANSACTION }
                )
            }
        }
    }
}