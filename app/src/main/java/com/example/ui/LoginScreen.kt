package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.WaTealGreen
import com.example.ui.theme.WaAccentGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: ChatViewModel,
    onLoginSuccess: () -> Unit
) {
    var phoneNumber by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var currentStep by remember { mutableIntStateOf(1) } // 1: Phone, 2: OTP, 3: Profile Setup
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Branding Logo
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(WaAccentGreen),
                contentAlignment = Alignment.Center
            ) {
                // Use a standard text representation of WhatsApp icon since we have vector/material icons
                Text(
                    text = "💬",
                    fontSize = 44.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Welcome to WhatsApp",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Read our Privacy Policy. Tap 'Agree & Continue' to accept the Terms of Service.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (currentStep) {
                        1 -> {
                            Text(
                                text = "Enter Your Phone Number",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            
                            OutlinedTextField(
                                value = phoneNumber,
                                onValueChange = { 
                                    if (it.length <= 15) {
                                        phoneNumber = it.filter { char -> char.isDigit() || char == '+' }
                                    }
                                },
                                label = { Text("Phone Number") },
                                placeholder = { Text("+1 (555) 010-0099") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (errorMessage.isNotEmpty()) {
                                Text(
                                    text = errorMessage,
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    if (phoneNumber.length < 8) {
                                        errorMessage = "Please enter a valid phone number"
                                    } else {
                                        errorMessage = ""
                                        isLoading = true
                                        // Simulate OTP sending
                                        scope.launch {
                                            delay(1500)
                                            isLoading = false
                                            currentStep = 2
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = WaTealGreen),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                                } else {
                                    Text("AGREE & CONTINUE", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        2 -> {
                            Text(
                                text = "Verify Your Number",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = "We've sent an SMS with a 6-digit verification code to $phoneNumber",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            OutlinedTextField(
                                value = otpCode,
                                onValueChange = { 
                                    if (it.length <= 6) {
                                        otpCode = it.filter { char -> char.isDigit() }
                                    }
                                },
                                label = { Text("6-Digit Code") },
                                placeholder = { Text("123456") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    isLoading = true
                                    scope.launch {
                                        delay(1200)
                                        isLoading = false
                                        currentStep = 3
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = WaTealGreen),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                                } else {
                                    Text("VERIFY OTP", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }

                            TextButton(onClick = { currentStep = 1 }) {
                                Text("Wrong number?", color = WaTealGreen)
                            }
                        }

                        3 -> {
                            Text(
                                text = "Profile Info",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = "Please provide your name and an optional profile status.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            OutlinedTextField(
                                value = userName,
                                onValueChange = { userName = it },
                                label = { Text("Your Name (Required)") },
                                placeholder = { Text("John Doe") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    if (userName.isBlank()) {
                                        userName = "User_" + phoneNumber.takeLast(4)
                                    }
                                    viewModel.registerUser(phoneNumber, userName)
                                    onLoginSuccess()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = WaAccentGreen),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("COMPLETE SETUP", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
