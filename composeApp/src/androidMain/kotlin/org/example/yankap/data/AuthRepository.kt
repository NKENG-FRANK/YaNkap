package org.example.yankap.data

import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

// Sealed hierarchy representing every possible auth state
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class CodeSent(val verificationId: String) : AuthState()
    data class Success(val uid: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()

    // Stored so that "Resend OTP" reuses the token and avoids reCAPTCHA
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    /**
     * Sends an OTP to [phoneNumber] (Cameroonian +237 prefix is added automatically).
     * Emits Loading → CodeSent | Error (or Success on auto-verification).
     */
    fun sendOtp(phoneNumber: String, activity: Activity): Flow<AuthState> = callbackFlow {

        trySend(AuthState.Loading)

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            // Called on some Pixel/emulator devices — Firebase verifies automatically
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                auth.signInWithCredential(credential)
                    .addOnSuccessListener { result ->
                        trySend(AuthState.Success(result.user?.uid ?: ""))
                        close()
                    }
                    .addOnFailureListener { e ->
                        trySend(AuthState.Error(e.message ?: "Auto sign-in failed"))
                        close()
                    }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                trySend(AuthState.Error(e.message ?: "Verification failed"))
                close()
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                resendToken = token
                trySend(AuthState.CodeSent(verificationId))
                close()
            }
        }

        val optionsBuilder = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber("+237$phoneNumber")
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)

        // Attach token on resend to skip reCAPTCHA
        resendToken?.let { optionsBuilder.setForceResendingToken(it) }

        PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())

        awaitClose() // Keeps the flow alive until callbacks close it
    }

    /**
     * Verifies the 6-digit [code] against [verificationId] returned by Firebase.
     * Returns Success or Error — never Loading (the ViewModel sets that before calling).
     */
    suspend fun verifyOtp(verificationId: String, code: String): AuthState {
        return try {
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            val result = auth.signInWithCredential(credential).await()
            AuthState.Success(result.user?.uid ?: "")
        } catch (e: Exception) {
            AuthState.Error(e.message ?: "Invalid OTP code. Please try again.")
        }
    }

    /** Returns true if a user is already signed in (used to skip auth on launch). */
    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    fun signOut() = auth.signOut()
}
