package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SpeedCameraRed

@Composable
fun EmergencySosModal(
    onConfirmSos: () -> Unit,
    onDismiss: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sosPulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sosScale"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = SpeedCameraRed)
                Text(
                    text = "🚨 SOS Familiar de Emergencia",
                    color = SpeedCameraRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Al presionar el botón se enviará inmediatamente una alerta de socorro con tu ubicación exacta y se activará la sirena a toda tu familia.",
                    Card(
    colors = CardDefaults.cardColors(
        containerColor = SpeedCameraRed.copy(alpha = 0.08f)
    )
) {
    Column(
        modifier = Modifier.padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text("📍 Compartir ubicación exacta")
        Text("🔋 Compartir nivel de batería")
        Text("👨‍👩‍👧‍👦 Notificar a la familia")
        Text("📞 Contactar emergencia")
    }
                    }
                    
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Box(
                    modifier = Modifier
                        .size((110 * scale).dp)
                        .clip(CircleShape)
                        .background(SpeedCameraRed)
                        .border(4.dp, Color.White, CircleShape)
                        .clickable {
                            onConfirmSos()
                            onDismiss()
                        }
                        .testTag("confirm_sos_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("SOS", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
                        Text("ENVIAR", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
