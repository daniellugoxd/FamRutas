package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TrafficAlert
import com.example.ui.theme.SpeedCameraRed
import com.example.ui.theme.TrafficGreen
import com.example.ui.theme.TrafficOrange
import com.example.ui.theme.WazeCyanPrimary

@Composable
fun SpeedometerHud(
    currentSpeedKmH: Int,
    speedLimitKmH: Int,
    isSpeeding: Boolean,
    approachingCameraAlert: TrafficAlert?,
    onDismissCameraAlert: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Approaching Speed Camera Warning Card (Like Waze!)
        AnimatedVisibility(
            visible = approachingCameraAlert != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            approachingCameraAlert?.let { alert ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SpeedCameraRed),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("camera_alert_card")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📸",
                            fontSize = 28.sp,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "¡FOTOMULTA EN 250 METROS!",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Límite: ${alert.speedLimitKmH ?: 60} km/h • ${alert.description}",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 12.sp
                            )
                        }
                        IconButton(onClick = onDismissCameraAlert) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Cerrar alerta",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Circular Speedometer + Speed Limit badge
        Row(
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Live Speed Indicator
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .shadow(6.dp, CircleShape)
                    .clip(CircleShape)
                    .background(
                        if (isSpeeding) SpeedCameraRed else MaterialTheme.colorScheme.surface
                    )
                    .border(
                        width = 3.dp,
                        color = if (isSpeeding) Color.White else WazeCyanPrimary,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$currentSpeedKmH",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isSpeeding) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "km/h",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSpeeding) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Speed Limit Sign (Standard European / LatAm circular sign: white background with red border)
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(width = 4.dp, color = SpeedCameraRed, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$speedLimitKmH",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Black
                )
            }
        }
    }
}
