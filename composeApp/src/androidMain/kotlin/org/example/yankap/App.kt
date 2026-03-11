package org.example.yankap

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import org.example.yankap.ui.features.history.HistoryScreen
import org.example.yankap.ui.features.settings.SettingsScreen
import org.example.yankap.ui.features.transactions.TransactionScreen

// Simple manual navigation state matching the defined screens
enum class Screen {
    TRANSACTION,
    HISTORY,
    SETTINGS
}

@Suppress("FunctionName")
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