package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.WaAccentGreen
import com.example.ui.theme.WaCallMissed
import com.example.ui.theme.WaCallIncoming
import com.example.ui.theme.WaDarkGreen
import com.example.ui.theme.WaTealGreenLight

@Composable
fun CallScreen(
    viewModel: ChatViewModel,
    callState: CallState
) {
    if (callState is CallState.Idle) return

    val user = when (callState) {
        is CallState.Ringing -> callState.user
        is CallState.Active -> callState.user
        else -> null
    } ?: return

    val isVideo = when (callState) {
        is CallState.Ringing -> callState.isVideo
        is CallState.Active -> callState.isVideo
        else -> false
    }

    val isIncoming = (callState as? CallState.Ringing)?.isIncoming == true
    val isActive = callState is CallState.Active
    val durationSeconds = (callState as? CallState.Active)?.durationSeconds ?: 0

    var isMuted by remember { mutableStateOf(false) }
    var isSpeakerOn by remember { mutableStateOf(false) }
    var isCameraOff by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WaDarkGreen)
    ) {
        // Video Stream Background Mock
        if (isVideo && isActive && !isCameraOff) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.DarkGray)
            ) {
                // Large contact video placeholder
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("📷", fontSize = 72.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "WebRTC peer stream from ${user.name} active",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }

                // Small self camera preview box in corner
                Box(
                    modifier = Modifier
                        .padding(24.dp)
                        .size(110.dp, 160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Gray.copy(alpha = 0.4f))
                        .align(Alignment.TopEnd)
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Self", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // Voice Call Background or Disabled Video
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(36.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(CircleShape)
                        .background(WaTealGreenLight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.name.take(1).uppercase(),
                        color = Color.White,
                        fontSize = 54.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Top Status Header overlay (Caller Name, Call Type, Status)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 64.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = user.name,
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = when {
                    isActive -> "WhatsApp ${if (isVideo) "Video" else "Voice"} Call (${viewModel.formatDuration(durationSeconds)})"
                    isIncoming -> "Incoming WhatsApp ${if (isVideo) "Video" else "Voice"} Call..."
                    else -> "Ringing WhatsApp ${if (isVideo) "Video" else "Voice"} Call..."
                },
                color = if (isActive) WaCallIncoming else Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            if (!isActive) {
                Spacer(modifier = Modifier.height(16.dp))
                // End-to-End Encryption lock indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.4f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = "E2EE", tint = WaAccentGreen, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("End-to-End Encrypted", color = Color.White, fontSize = 11.sp)
                }
            }
        }

        // Bottom Call Action Controls row
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 48.dp, start = 24.dp, end = 24.dp)
                .align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isIncoming && !isActive) {
                // Ringing Accept or Decline Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FloatingActionButton(
                        onClick = { viewModel.endCall() },
                        containerColor = WaCallMissed,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.size(68.dp)
                    ) {
                        Icon(Icons.Default.CallEnd, contentDescription = "Decline Call", modifier = Modifier.size(28.dp))
                    }

                    FloatingActionButton(
                        onClick = { viewModel.acceptIncomingCall() },
                        containerColor = WaCallIncoming,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.size(68.dp)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Accept Call", modifier = Modifier.size(28.dp))
                    }
                }
            } else {
                // Active connected Call or Outgoing Ringing Controls
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(horizontal = 24.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Speaker toggle
                    IconButton(
                        onClick = { isSpeakerOn = !isSpeakerOn }
                    ) {
                        Icon(
                            imageVector = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                            contentDescription = "Speaker",
                            tint = if (isSpeakerOn) WaAccentGreen else Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Video camera toggle (if video)
                    if (isVideo) {
                        IconButton(
                            onClick = { isCameraOff = !isCameraOff }
                        ) {
                            Icon(
                                imageVector = if (isCameraOff) Icons.Default.VideocamOff else Icons.Default.Videocam,
                                contentDescription = "Camera Toggle",
                                tint = if (isCameraOff) WaCallMissed else Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    // Mute Microphone toggle
                    IconButton(
                        onClick = { isMuted = !isMuted }
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Mute Microphone",
                            tint = if (isMuted) WaCallMissed else Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Red End Call Button
                    FloatingActionButton(
                        onClick = { viewModel.endCall() },
                        containerColor = WaCallMissed,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(Icons.Default.CallEnd, contentDescription = "End Call", modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
    }
}
