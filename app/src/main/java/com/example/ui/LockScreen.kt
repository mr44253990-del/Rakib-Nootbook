package com.example.ui

import android.content.Context
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

@Composable
fun LockScreen(
    onUnlock: () -> Unit
) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("secure_vault_prefs", Context.MODE_PRIVATE) }
    
    // Check if passcode is already set up
    var savedPin by remember { mutableStateOf(sharedPrefs.getString("master_pinkey", null)) }
    var isSetupMode by remember { mutableStateOf(savedPin == null) }
    
    var setupStep by remember { mutableStateOf(1) } // 1: Enter new PIN, 2: Confirm new PIN
    var tempPin by remember { mutableStateOf("") }
    
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var enteredPin by remember { mutableStateOf("") }

    // Proactively try biometric on start (only if already set up)
    LaunchedEffect(savedPin) {
        if (savedPin != null) {
            try {
                authenticate(context as FragmentActivity, onUnlock) { err ->
                    errorMessage = err
                }
            } catch (e: Exception) {
                errorMessage = "Biometrics unavailable. Enter PIN."
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFF1E1E2C) // Sleek space theme
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 40.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(PrimaryPurple.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSetupMode) Icons.Default.VpnKey else Icons.Default.Lock,
                        contentDescription = "Lock",
                        modifier = Modifier.size(36.dp),
                        tint = PrimaryPurple
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Rakib Notebook",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    if (isSetupMode) "INITIALIZE SECURITY LOCKED VAULT" else "SECURE PERSONAL BRAIN",
                    style = MaterialTheme.typography.labelSmall,
                    color = PrimaryPurple,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // PIN Dot Indicators
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                val instructionText = if (errorMessage != null) {
                    errorMessage!!
                } else if (isSetupMode) {
                    if (setupStep == 1) "নতুন সিকিউরিটি পিন সেট করুন\n(Create a 4-Digit Security PIN)" else "পিনটি নিশ্চিত করতে পুনরায় লিখুন\n(Re-enter to Confirm Security PIN)"
                } else {
                    "সিকিউরিটি পিন লিখুন\n(Enter Security PIN to Unlock)"
                }

                Text(
                    instructionText,
                    color = if (errorMessage != null) MaterialTheme.colorScheme.error else Color.LightGray,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 4) {
                        val isFilled = i < enteredPin.length
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(if (isFilled) PrimaryPurple else Color.Transparent)
                                .border(2.dp, if (isFilled) PrimaryPurple else Color.Gray.copy(alpha = 0.7f), CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                if (!isSetupMode) {
                    Text(
                        "পাসওয়ার্ড ভুলে গেছেন? অ্যাপ রি-ইনস্টল করুন।",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        "আপনার নোট সুরক্ষিত রাখতে একটি গোপন পিন তৈরি করুন",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Standard PIN Keyboard
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val keys = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("FINGERPRINT", "0", "DELETE")
                )

                for (row in keys) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (key in row) {
                            when (key) {
                                "FINGERPRINT" -> {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .clickable {
                                                if (isSetupMode) {
                                                    errorMessage = "Set up a PIN first before using fingerprint"
                                                } else {
                                                    try {
                                                        authenticate(context as FragmentActivity, onUnlock) { err ->
                                                            errorMessage = err
                                                        }
                                                    } catch (e: Exception) {
                                                        errorMessage = "Biometrics unavailable"
                                                    }
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Fingerprint,
                                            contentDescription = "Fingerprint",
                                            tint = if (isSetupMode) Color.DarkGray else PrimaryPurple,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                                "DELETE" -> {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .clickable {
                                                if (enteredPin.isNotEmpty()) {
                                                    enteredPin = enteredPin.dropLast(1)
                                                    errorMessage = null
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Backspace,
                                            contentDescription = "Backspace",
                                            tint = Color.LightGray,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                else -> {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.05f))
                                            .clickable {
                                                if (enteredPin.length < 4) {
                                                    enteredPin += key
                                                    errorMessage = null
                                                    if (enteredPin.length == 4) {
                                                        if (isSetupMode) {
                                                            if (setupStep == 1) {
                                                                // First step of setup complete
                                                                tempPin = enteredPin
                                                                enteredPin = ""
                                                                setupStep = 2
                                                            } else {
                                                                // Second step of setup: confirm
                                                                if (enteredPin == tempPin) {
                                                                    // Saved successfully
                                                                    sharedPrefs.edit().putString("master_pinkey", enteredPin).apply()
                                                                    savedPin = enteredPin
                                                                    isSetupMode = false
                                                                    onUnlock()
                                                                } else {
                                                                    errorMessage = "পিন মেলেনি! পুনরায় চেষ্টা করুন"
                                                                    enteredPin = ""
                                                                    setupStep = 1
                                                                    tempPin = ""
                                                                }
                                                            }
                                                        } else {
                                                            // Verify mode
                                                            if (enteredPin == savedPin) {
                                                                onUnlock()
                                                            } else {
                                                                errorMessage = "ভুল সিকিউরিটি পিন! সঠিক পিন দিন"
                                                                enteredPin = ""
                                                            }
                                                        }
                                                    }
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = key,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun authenticate(
    activity: FragmentActivity,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val executor = ContextCompat.getMainExecutor(activity)
    val biometricPrompt = BiometricPrompt(activity, executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError("Error: $errString")
            }
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }
            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onError("Authentication failed")
            }
        })

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Unlock Rakib Note")
        .setSubtitle("Use your fingerprint to continue")
        .setAllowedAuthenticators(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL)
        .build()

    try {
        biometricPrompt.authenticate(promptInfo)
    } catch (e: Exception) {
        onError("Fingerprint not configured")
    }
}
