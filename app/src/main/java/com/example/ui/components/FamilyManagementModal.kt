package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.model.FamilyRole
import com.example.model.VehicleType
import com.example.ui.theme.WazeCyanPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyManagementModal(
    familyMembers: List<FamilyMember>,
    onSelectMember: (FamilyMember) -> Unit,
    onAddMember: (String, FamilyRole, String, Long, String, VehicleType) -> Unit,
    onDismiss: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newPhone by remember { mutableStateOf("+34 600 000 000") }
    var selectedRole by remember { mutableStateOf(FamilyRole.HIJO) }
    var selectedVehicle by remember { mutableStateOf(VehicleType.AUTO) }
    var selectedEmoji by remember { mutableStateOf("👦") }
    var selectedColorHex by remember { mutableLongStateOf(0xFF10B981) }

    val availableEmojis = listOf("👨‍💼", "👩‍🦰", "👦", "👧", "👨‍🦱", "👩‍🦱", "👴", "👵", "👶", "🚗", "🚙", "🚌", "🏎️", "🏍️", "🚲", "🚶")
    val availableColors = listOf(
        0xFF0284C7, // Blue
        0xFFEC4899, // Pink
        0xFF10B981, // Emerald
        0xFFA855F7, // Purple
        0xFFF59E0B, // Amber
        0xFFE11D48, // Rose
        0xFF06B6D4  // Cyan
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("family_management_modal")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Círculo Familiar & Avatares",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Personaliza avatares, roles y miembros de tu familia",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar")
                }
            }

            // List of family members
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(familyMembers) { member ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectMember(member)
                                onDismiss()
                            },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color(member.avatarBgColorHex)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(member.avatarEmoji, fontSize = 24.sp)
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = member.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "• ${member.role.label}",
                                        fontSize = 12.sp,
                                        color = Color(member.avatarBgColorHex),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Text(
                                    text = "📍 ${member.location.streetName}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "📞 ${member.phoneNumber} • ${member.currentSpeedKmH} km/h • 🔋 ${member.batteryLevel}%",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = "Ver detalles",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Button to Add Member
            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("add_family_member_button")
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text("Añadir Nuevo Familiar", fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Añadir Familiar al Círculo") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Nombre del familiar") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newPhone,
                        onValueChange = { newPhone = it },
                        label = { Text("Teléfono de contacto") },
                        singleLine = true
                    )

                    Text("Selecciona un Avatar personalizado:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(availableEmojis) { emoji ->
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (selectedEmoji == emoji) WazeCyanPrimary.copy(alpha = 0.3f) else Color.Transparent)
                                    .border(
                                        width = if (selectedEmoji == emoji) 2.dp else 0.dp,
                                        color = WazeCyanPrimary,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedEmoji = emoji },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(emoji, fontSize = 22.sp)
                            }
                        }
                    }

                    Text("Medio de Transporte / Desplazamiento:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(VehicleType.values()) { v ->
                            FilterChip(
                                selected = selectedVehicle == v,
                                onClick = { selectedVehicle = v },
                                label = { Text("${v.iconEmoji} ${v.label}", fontSize = 11.sp) }
                            )
                        }
                    }

                    Text("Color del Pin:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(availableColors) { col ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(col))
                                    .border(
                                        width = if (selectedColorHex == col) 3.dp else 0.dp,
                                        color = Color.White,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColorHex = col }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            onAddMember(newName, selectedRole, selectedEmoji, selectedColorHex, newPhone, selectedVehicle)
                            showAddDialog = false
                            newName = ""
                        }
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
