package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import com.example.model.AlertType
import com.example.ui.theme.SpeedCameraRed
import com.example.ui.theme.TrafficOrange
import com.example.ui.theme.WazeCyanPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportAlertModal(
    onDismiss: () -> Unit,
    onSubmitReport: (AlertType, String, Int?) -> Unit
) {
    var selectedType by remember { mutableStateOf(AlertType.SPEED_CAMERA) }
    var description by remember { mutableStateOf("") }
    var speedLimitText by remember { mutableStateOf("60") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("report_alert_modal")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
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
                        text = "Reportar Alerta Vial",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Avisa a tu familia y a otros conductores",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar")
                }
            }

            // Grid of Alert Types (like Waze's orange report bubble)
            val alertTypes = AlertType.values()
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.height(200.dp)
            ) {
                items(alertTypes) { type ->
                    val isSelected = selectedType == type
                    val bgColor = if (isSelected) {
                        when (type) {
                            AlertType.SPEED_CAMERA -> SpeedCameraRed
                            AlertType.HEAVY_TRAFFIC -> TrafficOrange
                            else -> WazeCyanPrimary
                        }
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedType = type },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = bgColor)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = type.iconEmoji, fontSize = 28.sp)
                            Text(
                                text = type.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Speed Limit Input if Speed Camera is selected
            if (selectedType == AlertType.SPEED_CAMERA) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Límite de velocidad:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    listOf("40", "50", "60", "80").forEach { limit ->
                        FilterChip(
                            selected = speedLimitText == limit,
                            onClick = { speedLimitText = limit },
                            label = { Text("$limit km/h") }
                        )
                    }
                }
            }

            // Description note
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Detalle o referencia del lugar") },
                placeholder = { Text("Ej: Sentido norte, carril derecho") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Button(
                onClick = {
                    val limit = if (selectedType == AlertType.SPEED_CAMERA) {
                        speedLimitText.toIntOrNull() ?: 60
                    } else null
                    onSubmitReport(selectedType, description, limit)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("submit_report_button")
            ) {
                Text(
                    text = "Publicar Alerta Inmediata",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
