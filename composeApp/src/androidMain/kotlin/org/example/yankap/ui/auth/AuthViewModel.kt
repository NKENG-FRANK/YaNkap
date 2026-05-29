package org.example.yankap.ui.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.example.yankap.data.AuthRepository
import org.example.yankap.data.AuthState

// The three steps of the auth journey
enum class AuthStep {
    PHONE_ENTRY,
    OTP_VERIFICATION,
    AUTHENTICATED
}

data class AuthUiState(
    val step: AuthStep = AuthStep.PHONE_ENTRY,
    val phoneNumber: String = "",
    val verificationId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _uiState = MutableStateFlow(
        // If a user is already signed in, jump straight to AUTHENTICATED
        AuthUiState(
            step = if (repository.isUserLoggedIn()) AuthStep.AUTHENTICATED
                   else AuthStep.PHONE_ENTRY
        )
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    /** Step 1 — Send OTP via Firebase PhoneAuthProvider */
    fun sendOtp(phoneNumber: String, activity: Activity) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                phoneNumber = phoneNumber,
                error = null
            )
            repository.sendOtp(phoneNumber, activity).collect { state ->
                when (state) {
                    is AuthState.Loading -> _uiState.value = _uiState.value.copy(
                        isLoading = true,
                        error = null
                    )
                    is AuthState.CodeSent -> _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        verificationId = state.verificationId,
                        step = AuthStep.OTP_VERIFICATION
                    )
                    is AuthState.Success -> _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        step = AuthStep.AUTHENTICATED
                    )
                    is AuthState.Error -> _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = state.message
                    )
                    else -> Unit
                }
            }
        }
    }

    /** Step 2 — Verify the code the user typed against Firebase */
    fun verifyOtp(code: String) {
        val verificationId = _uiState.value.verificationId ?: run {
            _uiState.value = _uiState.value.copy(
                error = "Session expired. Please go back and try again."
            )
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = repository.verifyOtp(verificationId, code)) {
                is AuthState.Success -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    step = AuthStep.AUTHENTICATED
                )
                is AuthState.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.message
                )
                else -> Unit
            }
        }
    }

    /** Go back from OTP screen to phone entry */
    fun goBack() {
        _uiState.value = _uiState.value.copy(
            step = AuthStep.PHONE_ENTRY,
            verificationId = null,
            error = null
        )
    }

    /** Called by UI when the user interacts after seeing an error */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
