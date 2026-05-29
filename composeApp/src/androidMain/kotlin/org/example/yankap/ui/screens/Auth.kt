package org.example.yankap.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneEntryScreen(
    isLoading: Boolean = false,
    error: String? = null,
    onSendOtp: (String) -> Unit,
    onErrorDismissed: () -> Unit = {}
) {
    var phoneNumber by remember { mutableStateOf("") }
    val green = Color(0xFF1A7A4A)

    // Auto-dismiss error as soon as the user edits the field
    LaunchedEffect(phoneNumber) {
        if (error != null) onErrorDismissed()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFE8F5EE)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = null,
                tint = green,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Welcome to YaNkap",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Enter your phone number to continue",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { if (it.length <= 9) phoneNumber = it },
            label = { Text("Phone Number") },
            placeholder = { Text("670 123 456") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            enabled = !isLoading,
            isError = error != null,
            leadingIcon = {
                Row(
                    modifier = Modifier.padding(start = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("+237", fontWeight = FontWeight.Bold, color = green)
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(24.dp)
                            .background(Color.LightGray)
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = green,
                focusedLabelColor = green,
                errorBorderColor = Color(0xFFD32F2F),
                errorLabelColor = Color(0xFFD32F2F)
            )
        )

        // Error message (animated in/out)
        AnimatedVisibility(
            visible = error != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            error?.let {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = it,
                    color = Color(0xFFD32F2F),
                    fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { if (phoneNumber.isNotEmpty()) onSendOtp(phoneNumber) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = green),
            enabled = phoneNumber.length >= 8 && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.5.dp
                )
            } else {
                Text("Send OTP", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "By continuing, you agree to our Terms and Conditions",
            fontSize = 12.sp,
            color = Color.LightGray,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpVerificationScreen(
    phoneNumber: String,
    isLoading: Boolean = false,
    error: String? = null,
    onVerify: (String) -> Unit,
    onBack: () -> Unit,
    onResend: () -> Unit = {},
    onErrorDismissed: () -> Unit = {}
) {
    var otpCode by remember { mutableStateOf("") }
    val green = Color(0xFF1A7A4A)
    val focusRequester = remember { FocusRequester() }

    // Auto-dismiss error as soon as the user edits the OTP
    LaunchedEffect(otpCode) {
        if (error != null) onErrorDismissed()
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
    ) {
        IconButton(onClick = onBack, enabled = !isLoading) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Verify Phone",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Enter the 6-digit code sent to +237 $phoneNumber",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(48.dp))

        // 6-digit OTP boxes
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
            ) {
                repeat(6) { index ->
                    val char = otpCode.getOrNull(index)?.toString() ?: ""
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                when {
                                    error != null -> Color(0xFFFFF0F0)
                                    char.isNotEmpty() -> Color(0xFFE8F5EE)
                                    else -> Color(0xFFF5F5F5)
                                }
                            )
                            .border(
                                width = 1.5.dp,
                                color = when {
                                    error != null -> Color(0xFFD32F2F)
                                    char.isNotEmpty() -> green
                                    else -> Color.Transparent
                                },
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (error != null) Color(0xFFD32F2F) else green
                        )
                    }
                }
            }

            // Hidden TextField to capture keyboard input
            TextField(
                value = otpCode,
                onValueChange = { input ->
                    if (input.length <= 6 && input.all { it.isDigit() }) otpCode = input
                },
                modifier = Modifier
                    .size(336.dp, 60.dp)
                    .alpha(0f)
                    .focusRequester(focusRequester),
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        // Error message
        AnimatedVisibility(
            visible = error != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            error?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = it,
                    color = Color(0xFFD32F2F),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { if (otpCode.length == 6) onVerify(otpCode) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = green),
            enabled = otpCode.length == 6 && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.5.dp
                )
            } else {
                Text("Verify & Continue", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Resend countdown timer
        var timer by remember { mutableStateOf(60) }
        LaunchedEffect(Unit) {
            while (timer > 0) {
                delay(1000L)
                timer -= 1
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Didn't receive code? ", fontSize = 14.sp, color = Color.Gray)
            if (timer > 0) {
                Text(
                    "Resend in ${timer}s",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
            } else {
                TextButton(
                    onClick = {
                        timer = 60
                        onResend()
                    },
                    contentPadding = PaddingValues(0.dp),
                    enabled = !isLoading
                ) {
                    Text(
                        text = "Resend",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = green
                    )
                }
            }
        }
    }
}
