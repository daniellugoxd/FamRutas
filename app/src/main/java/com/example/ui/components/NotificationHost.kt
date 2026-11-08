package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.InAppNotification
import com.example.model.NotificationType
import com.example.ui.theme.*

@Composable
fun NotificationHost(
    notifications: List<InAppNotification>,
    onDismissNotification: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        notifications.forEach { notif ->
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier.fillMaxWidth()
            ) {
                NotificationBanner(
                    notification = notif,
                    onDismiss = { onDismissNotification(notif.id) }
                )
            }
        }
    }
}

@Composable
fun NotificationBanner(
    notification: InAppNotification,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (borderColor, containerColor) = when (notification.type) {
        NotificationType.INFO -> WazeCyanPrimary to RoadDark.copy(alpha = 0.95f)
        NotificationType.SUCCESS -> TrafficGreen to Color(0xFF064E3B).copy(alpha = 0.95f)
        NotificationType.WARNING -> TrafficOrange to Color(0xFF78350F).copy(alpha = 0.95f)
        NotificationType.ALERT -> SpeedCameraRed to Color(0xFF881337).copy(alpha = 0.95f)
        NotificationType.TRAFFIC -> TrafficOrange to RoadDark.copy(alpha = 0.95f)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
            .shadow(12.dp, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon / Emoji
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(borderColor.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Text(notification.iconEmoji, fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )
                Text(
                    text = notification.message,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    lineHeight = 16.sp
                )

                if (notification.actionLabel != null && notification.onAction != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = notification.actionLabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = WazeCyanLight,
                        modifier = Modifier.clickable {
                            notification.onAction.invoke()
                            onDismiss()
                        }
                    )
                }
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Cerrar notificación",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
