package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.model.FamilyMember
import com.example.ui.theme.SpeedCameraRed
import com.example.ui.theme.TrafficGreen
import com.example.ui.theme.TrafficOrange
import com.example.ui.theme.WazeCyanPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyMemberSheet(
    member: FamilyMember,
    onCallClick: (FamilyMember) -> Unit,
    onSendMessage: (FamilyMember, String) -> Unit,
    onCreateRoute: (FamilyMember) -> Unit,
    onShareLocation: (FamilyMember) -> Unit,
    onDismiss: () -> Unit
) {
    var customMessageText by remember { mutableStateOf("") }
    val memberColor = Color(member.avatarBgColorHex)

    val quickMessages = listOf(
        "¿Dónde estás?",
        "Ya voy en camino",
        "¿Todo bien?",
        "Te llamo en un minuto",
        "Conduce con cuidado"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.testTag("family_member_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with Avatar and Basic Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Large Avatar Circle
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(memberColor)
                        .border(3.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = member.avatarEmoji,
                        fontSize = 32.sp
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = member.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = memberColor.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = member.role.label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = memberColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = member.statusText,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "📍 ${member.location.streetName}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Real-time Status Badges (Speed, Battery, Vehicle, Connection)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Speed Pill
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${member.currentSpeedKmH} km/h",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (member.currentSpeedKmH > 0) TrafficGreen else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (member.isMoving) "En marcha" else "Detenido",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Battery Pill
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val batteryColor = when {
                            member.batteryLevel > 50 -> TrafficGreen
                            member.batteryLevel > 20 -> TrafficOrange
                            else -> SpeedCameraRed
                        }
                        Text(
                            text = "🔋 ${member.batteryLevel}%",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = batteryColor
                        )
                        Text(
                            text = "Batería",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Vehicle Pill
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${member.vehicleType.iconEmoji} ${member.vehicleType.label}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Vehículo",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // PRIMARY CALL BUTTON (Explicit user requirement: "que tenga un botón donde diga llamar a ...a sería la persona")
            Button(
                onClick = { onCallClick(member) },
                colors = ButtonDefaults.buttonColors(containerColor = TrafficGreen),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("call_member_button")
            ) {
                Icon(
                    Icons.Default.Phone,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "Llamar a ${member.name}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Action Buttons: Create Route & Share Location
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onCreateRoute(member) },
                    colors = ButtonDefaults.buttonColors(containerColor = WazeCyanPrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("create_route_to_member_button")
                ) {
                    Icon(
                        Icons.Default.Directions,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(
                        text = "Crear Ruta",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                OutlinedButton(
                    onClick = { onShareLocation(member) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("share_member_location_button")
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(
                        text = "Compartir",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            // Quick Message Section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Enviar mensaje rápido a ${member.name}:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 4.dp)
                ) {
                    items(quickMessages) { msg ->
                        SuggestionChip(
                            onClick = { onSendMessage(member, msg) },
                            label = { Text(msg, fontSize = 12.sp) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = customMessageText,
                        onValueChange = { customMessageText = it },
                        placeholder = { Text("Escribir mensaje personalizado...", fontSize = 13.sp) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    IconButton(
                        onClick = {
                            if (customMessageText.isNotBlank()) {
                                onSendMessage(member, customMessageText)
                                customMessageText = ""
                            }
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Enviar mensaje", tint = Color.White)
                    }
                }
            }
        }
    }
}
