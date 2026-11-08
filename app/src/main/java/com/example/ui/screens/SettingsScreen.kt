package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.auth.AuthManager
import com.example.model.InAppNotification
import com.example.model.NotificationType
import com.example.ui.theme.*
import com.example.viewmodel.FamRutaViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: FamRutaViewModel,
    onBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val uiState by viewModel.uiState.collectAsState()

    var currentUser by remember { mutableStateOf<FirebaseUser?>(Firebase.auth.currentUser) }

    // Settings States
    var voiceSpeedCameraAlerts by remember { mutableStateOf(true) }
    var voiceTrafficAlerts by remember { mutableStateOf(true) }
    var soundEffectsEnabled by remember { mutableStateOf(true) }
    var autoReunionReactions by remember { mutableStateOf(true) }
    var lowBatteryAlerts by remember { mutableStateOf(true) }
    var offlineMapCacheEnabled by remember { mutableStateOf(true) }
    var speedLimitUnitKmH by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Configuración",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar al mapa")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Account Section
            Text(
                text = "Cuenta y Sincronización",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = WazeCyanPrimary
            )

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (currentUser != null) {
                        val user = currentUser!!
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(WazeCyanPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = user.displayName?.take(1) ?: "U",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = user.displayName ?: "Usuario de FamRuta",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = user.email ?: "",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "🟢 Conectado a Google Cloud & Firebase",
                                    fontSize = 11.sp,
                                    color = TrafficGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                        Button(
                            onClick = { viewModel.navigateTo(com.example.viewmodel.AppScreen.PROFILE) },
                            colors = ButtonDefaults.buttonColors(containerColor = WazeCyanPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Editar Perfil & Foto con Cámara", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                AuthManager.signOut(context, credentialManager, coroutineScope) {
                                    currentUser = null
                                    viewModel.showNotification(
                                        InAppNotification(
                                            title = "Sesión cerrada",
                                            message = "Se cerró la sesión de Google exitosamente.",
                                            iconEmoji = "👋",
                                            type = NotificationType.INFO
                                        )
                                    )
                                    Toast.makeText(context, "Sesión cerrada", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cerrar Sesión")
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(TrafficOrange.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PersonOutline, contentDescription = null, tint = TrafficOrange)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Modo Local / Demo",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Inicia sesión con Google para sincronizar tus miembros y rutas.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = onNavigateToLogin,
                            colors = ButtonDefaults.buttonColors(containerColor = WazeCyanPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Iniciar Sesión con Google", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 2. Alert & Sound Settings
            Text(
                text = "Alertas de Navegación y Radares",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = WazeCyanPrimary
            )

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SettingToggleRow(
                        icon = "📷",
                        title = "Aviso de Fotomultas fijas",
                        subtitle = "Alertar con voz o sonido a 350m de distancia",
                        checked = voiceSpeedCameraAlerts,
                        onCheckedChange = {
                            voiceSpeedCameraAlerts = it
                            viewModel.showNotification(
                                InAppNotification(
                                    title = "Alertas de Fotomultas",
                                    message = if (it) "Avisos de fotomultas activados" else "Avisos de fotomultas silenciados",
                                    iconEmoji = "📷",
                                    type = NotificationType.INFO
                                )
                            )
                        }
                    )

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    SettingToggleRow(
                        icon = "🚗",
                        title = "Alertas de Tráfico y Retenes",
                        subtitle = "Notificar congestiones y controles policiales",
                        checked = voiceTrafficAlerts,
                        onCheckedChange = { voiceTrafficAlerts = it }
                    )

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    SettingToggleRow(
                        icon = "🔊",
                        title = "Sonidos y Vibración",
                        subtitle = "Vibrar ante límites de velocidad y alertas SOS",
                        checked = soundEffectsEnabled,
                        onCheckedChange = { soundEffectsEnabled = it }
                    )

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    SettingToggleRow(
                        icon = "🤗",
                        title = "Celebración de Encuentro Familiar",
                        subtitle = "Animación y aviso cuando 2 familiares se junten",
                        checked = autoReunionReactions,
                        onCheckedChange = { autoReunionReactions = it }
                    )

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    SettingToggleRow(
                        icon = "🔋",
                        title = "Alerta de Batería Baja Familiar",
                        subtitle = "Avisar cuando un familiar tenga menos del 15% de batería",
                        checked = lowBatteryAlerts,
                        onCheckedChange = { lowBatteryAlerts = it }
                    )
                }
            }

            // 3. Network & Security Settings
            Text(
                text = "Red Segura & Mapas Offline",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = WazeCyanPrimary
            )

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SettingToggleRow(
                        icon = "🔒",
                        title = "Túnel VPN Encriptado Privado",
                        subtitle = "Protege las rutas GPS mediante WireGuard P2P",
                        checked = uiState.vpnState.isAutoVpnEnabled,
                        onCheckedChange = { viewModel.toggleAutoVpn(it) }
                    )

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    SettingToggleRow(
                        icon = "🗺️",
                        title = "Caché de Mapas Offline",
                        subtitle = "Almacena 64.5 MB de mapa para zonas sin cobertura",
                        checked = offlineMapCacheEnabled,
                        onCheckedChange = { offlineMapCacheEnabled = it }
                    )
                }
            }

            // 4. Test Notification Button (to see notifications appear and disappear)
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "🔔 Notificaciones Inteligentes",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Las notificaciones de FamRuta aparecen en la parte superior cuando surge un evento y desaparecen solas tras unos segundos o al tocarlas.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = {
                            viewModel.showSnackbarAlert(
                                message = "Ruta actualizada: Destino seguro recalculado (+12 min)",
                                actionLabel = "Ver",
                                iconEmoji = "🚗"
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WazeCyanPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Probar Snackbar: 'Ruta actualizada'", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            viewModel.showSnackbarAlert(
                                message = "¡Camila se unió al círculo familiar!",
                                actionLabel = "Saludar",
                                iconEmoji = "👋"
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TrafficGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Probar Snackbar: 'Friend joined'", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 5. App Info, Version and Creator Branding
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = RoadSurface),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.fam_nav_icon_1793930319361),
                                contentDescription = "FamRuta",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(16.dp))
                            )
                        }

                        Column {
                            Text(
                                text = "FamRuta",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Versión: 1.0.0 (Build 2026.11)",
                                fontSize = 12.sp,
                                color = WazeCyanPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Estado: 100% Funcional y Verificado",
                                fontSize = 11.sp,
                                color = TrafficGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // Creator info
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Code, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                        Column {
                            Text(
                                text = "Creador y Desarrollador:",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Daniel Lugo",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Tech Stack Summary
                    Text(
                        text = "Tecnologías: Kotlin, Jetpack Compose, Material Design 3, Google Sign-In con Credential Manager y Firebase Firestore.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp
                    )

                    Text(
                        text = "FamRuta • Todos los derechos reservados © 2026",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SettingToggleRow(
    icon: String,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text(icon, fontSize = 20.sp)
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
