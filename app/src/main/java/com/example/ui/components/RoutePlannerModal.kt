package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.model.GeoPoint
import com.example.model.SafeZone
import com.example.ui.theme.SpeedCameraRed
import com.example.ui.theme.TrafficGreen
import com.example.ui.theme.TrafficOrange
import com.example.ui.theme.WazeCyanPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutePlannerModal(
    familyMembers: List<FamilyMember>,
    safeZones: List<SafeZone>,
    onSelectDestination: (GeoPoint, String) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedOption by remember { mutableStateOf("Más rápida") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("route_planner_modal")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Crear Ruta de Viaje",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Con alertas de fotomultas y tráfico en tiempo real",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar")
                }
            }

            // Search input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                placeholder = { Text("Buscar destino o dirección...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Route options
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Más rápida", "Evitar peajes", "Ruta segura").forEach { opt ->
                    FilterChip(
                        selected = selectedOption == opt,
                        onClick = { selectedOption = opt },
                        label = { Text(opt, fontSize = 12.sp) }
                    )
                }
            }

            Text(
                text = "Rutas rápidas hacia tu familia:",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Family Destination Options
            familyMembers.filter { it.id != "user_me" }.forEach { member ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectDestination(member.location, "Ubicación de ${member.name}") },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(member.avatarBgColorHex)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(member.avatarEmoji, fontSize = 20.sp)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Hacia ${member.name} (${member.role.label})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "📍 ${member.location.streetName} • ${member.statusText}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = WazeCyanPrimary
                        )
                    }
                }
            }

            Text(
                text = "Destinos y Lugares Seguros:",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Safe Zone Options
            safeZones.forEach { zone ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectDestination(zone.location, zone.name) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(zone.emoji, fontSize = 24.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = zone.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "📍 ${zone.location.streetName}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            Icons.Default.Navigation,
                            contentDescription = null,
                            tint = TrafficGreen
                        )
                    }
                }
            }
        }
    }
}
