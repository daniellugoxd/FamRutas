package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.FamRutaViewModel
import com.example.viewmodel.MapFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMapScreen(
    viewModel: FamRutaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // Request Location Permission and start GPS tracking
    val locationPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            viewModel.startGpsTracking(context)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.attachLocationService(context)
        locationPermissionLauncher.launch(
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // Filtered data based on activeMapFilter
    val displayedAlerts = remember(uiState.trafficAlerts, uiState.activeMapFilter) {
        when (uiState.activeMapFilter) {
            MapFilter.ALL -> uiState.trafficAlerts
            MapFilter.CAMERAS -> uiState.trafficAlerts.filter { it.type == AlertType.SPEED_CAMERA }
            MapFilter.TRAFFIC -> uiState.trafficAlerts.filter {
                it.type == AlertType.HEAVY_TRAFFIC || it.type == AlertType.POLICE || it.type == AlertType.SPEED_CAMERA
            }
            MapFilter.FAMILY, MapFilter.ZONES -> emptyList()
        }
    }

    val displayedSafeZones = remember(uiState.safeZones, uiState.activeMapFilter) {
        if (uiState.activeMapFilter == MapFilter.ALL || uiState.activeMapFilter == MapFilter.ZONES) {
            uiState.safeZones
        } else {
            emptyList()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // 1. Vector Map Canvas
        MapCanvas(
            familyMembers = uiState.familyMembers,
            trafficAlerts = displayedAlerts,
            safeZones = displayedSafeZones,
            activeRoute = uiState.activeRoute,
            selectedMember = uiState.selectedMember,
            isDayMode = uiState.isDayMode,
            trafficSegments = uiState.trafficSegments,
            isTrafficOverlayEnabled = uiState.isTrafficOverlayEnabled,
            onSelectMember = { viewModel.selectMember(it) },
            onSelectAlert = { viewModel.selectAlert(it) },
            modifier = Modifier.fillMaxSize()
        )

        // 2. Top Navigation Banner (Active Turn-by-Turn when navigating)
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Celebratory Reunion Event Banner (Abrazo y Salto de Alegría)
            AnimatedVisibility(
                visible = uiState.reunionEvent != null,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                uiState.reunionEvent?.let { event ->
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFBE185D)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, Color(0xFFFFB6C1), RoundedCornerShape(18.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🤗✨", fontSize = 32.sp, modifier = Modifier.padding(end = 12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "¡ENCUENTRO FAMILIAR! ❤️",
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "${event.member1Name} y ${event.member2Name} se han reunido en ${event.locationDescription}. ¡Saltando de alegría y abrazándose!",
                                    color = Color.White.copy(alpha = 0.95f),
                                    fontSize = 12.sp
                                )
                            }
                            IconButton(onClick = { viewModel.dismissReunionEvent() }) {
                                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                            }
                        }
                    }
                }
            }

            // If currently Navigating a Route
            AnimatedVisibility(
                visible = uiState.isNavigating && uiState.activeRoute != null,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                uiState.activeRoute?.let { route ->
                    val currentStep = route.steps.getOrNull(uiState.currentNavStepIndex) ?: route.steps.first()
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = RoadDark),
                        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, WazeCyanPrimary, RoundedCornerShape(18.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Turn direction icon
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(if (uiState.isRouteDelayed) SpeedCameraRed else WazeCyanPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                val icon = when (currentStep.turnType) {
                                    TurnType.LEFT, TurnType.SLIGHT_LEFT -> Icons.Default.TurnLeft
                                    TurnType.RIGHT, TurnType.SLIGHT_RIGHT -> Icons.Default.TurnRight
                                    TurnType.ARRIVAL -> Icons.Default.Flag
                                    else -> Icons.Default.Straight
                                }
                                Icon(icon, contentDescription = null, tint = Color.White)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "En ${currentStep.distanceMeters} m",
                                    fontSize = 12.sp,
                                    color = if (uiState.isRouteDelayed) TrafficOrange else WazeCyanLight,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = currentStep.instruction,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1
                                )
                                Text(
                                    text = if (uiState.isRouteDelayed) {
                                        "⚠️ Retraso por tráfico: +18 min • ¡Familiar esperando con desespero ⌚😤!"
                                    } else {
                                        "Destino: ${route.destinationName} • ETA: ${route.durationMinutes} min • ¡Alegría en camino! 🥰"
                                    },
                                    fontSize = 11.sp,
                                    color = if (uiState.isRouteDelayed) SpeedCameraRed else TextSecondaryDark,
                                    fontWeight = if (uiState.isRouteDelayed) FontWeight.Bold else FontWeight.Normal
                                )
                            }

                            if (uiState.activeRouteTargetMemberId != null) {
                                IconButton(
                                    onClick = { viewModel.toggleRouteDelaySimulation() },
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = if (uiState.isRouteDelayed) SpeedCameraRed.copy(alpha = 0.3f) else TrafficOrange.copy(alpha = 0.3f)
                                    )
                                ) {
                                    Text(if (uiState.isRouteDelayed) "😤" else "⌚", fontSize = 16.sp)
                                }
                            }

                            IconButton(
                                onClick = { viewModel.stopNavigation() },
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = RoadCard)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Detener viaje", tint = Color.White)
                            }
                        }
                    }
                }
            }

            // Top Header Bar
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(WazeCyanPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Navigation, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            Column {
                                
                               Text(
                                   text = "FamRuta",
                                   fontSize = 18.sp,
                                   fontWeight = FontWeight.Black,
                                   color = MaterialTheme.colorScheme.onSurface
                            )

                               Text(
                                   text = "${uiState.familyMembers.size} miembros protegidos",
                                   fontSize = 11.sp,
                                   color = VpnSecureGreen,
                                   fontWeight = FontWeight.Bold
                            )

                               Text(
                                    text = "Protección familiar en tiempo real",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            }

                      

                            // VPN status badge
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (uiState.vpnState.isConnected) VpnSecureGreen.copy(alpha = 0.15f) else TrafficOrange.copy(alpha = 0.15f),
                                modifier = Modifier.clickable { viewModel.openVpnModal() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(if (uiState.vpnState.isConnected) VpnSecureGreen else TrafficOrange)
                                    )
                                    Text(
                                        text = if (uiState.vpnState.isConnected) "VPN Activo" else "Offline Mesh",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (uiState.vpnState.isConnected) VpnSecureGreen else TrafficOrange
                                    )
                                }
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Surface(
    shape = RoundedCornerShape(10.dp),
    color = Color(0xFF16A34A).copy(alpha = 0.15f)
) {
    Text(
        text = "🟢 En línea",
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold
    )
                            }
                            
                            // Traffic API Layer Toggle
                            IconButton(
                                onClick = { viewModel.toggleTrafficOverlay() },
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = if (uiState.isTrafficOverlayEnabled) TrafficOrange.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Icon(
                                    Icons.Default.Traffic,
                                    contentDescription = "Capa de Tráfico",
                                    tint = if (uiState.isTrafficOverlayEnabled) TrafficOrange else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Day / Night Toggle
                            IconButton(onClick = { viewModel.toggleDayNightMode() }) {
                                Icon(
                                    if (uiState.isDayMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                                    contentDescription = "Modo de mapa",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Google Sync & Cloud Button
                            IconButton(
                                onClick = { viewModel.openGoogleSyncModal() },
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(Icons.Default.CloudSync, contentDescription = "Conectar con Google", tint = MaterialTheme.colorScheme.primary)
                            }

                            // Family Manage Button
                            IconButton(
                                onClick = { viewModel.openFamilyManageModal() },
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(Icons.Default.Group, contentDescription = "Familia", tint = MaterialTheme.colorScheme.primary)
                            }

                            // Settings Button (Versión, Creador, Ajustes)
                            IconButton(
    onClick = { viewModel.navigateTo(com.example.viewmodel.AppScreen.PROFILE) },
    colors = IconButtonDefaults.filledIconButtonColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer
    )
) {
    Icon(
        Icons.Default.AccountCircle,
        contentDescription = "Perfil"
    )
                            }
                            
                            
                            IconButton(
                                onClick = { viewModel.navigateTo(com.example.viewmodel.AppScreen.SETTINGS) },
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(Icons.Default.Settings, contentDescription = "Configuración", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    // Map Filter Pills
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(MapFilter.values()) { filter ->
                            FilterChip(
                                selected = uiState.activeMapFilter == filter,
                                onClick = { viewModel.setMapFilter(filter) },
                                label = { Text(filter.label, fontSize = 11.sp) },
                                leadingIcon = {
                                    when (filter) {
                                        MapFilter.ALL -> Icon(Icons.Default.Layers, contentDescription = null, modifier = Modifier.size(14.dp))
                                        MapFilter.FAMILY -> Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(14.dp))
                                        MapFilter.CAMERAS -> Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                                        MapFilter.TRAFFIC -> Icon(Icons.Default.Traffic, contentDescription = null, modifier = Modifier.size(14.dp))
                                        MapFilter.ZONES -> Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(14.dp))
                                    }
                                }
                            )
                        }
                    }

                    // Interactive Simulation Actions for testing animations
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFEC4899).copy(alpha = 0.18f),
                            modifier = Modifier.clickable { viewModel.triggerFamilyReunionSimulation() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("🤗", fontSize = 13.sp)
                                Text("Probar Abrazo y Salto", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEC4899))
                            }
                        }

                        if (uiState.activeRouteTargetMemberId != null) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (uiState.isRouteDelayed) SpeedCameraRed.copy(alpha = 0.18f) else TrafficOrange.copy(alpha = 0.18f),
                                modifier = Modifier.clickable { viewModel.toggleRouteDelaySimulation() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(if (uiState.isRouteDelayed) "😤" else "⌚", fontSize = 13.sp)
                                    Text(
                                        text = if (uiState.isRouteDelayed) "Quitar Retraso" else "Simular Desespero",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (uiState.isRouteDelayed) SpeedCameraRed else TrafficOrange
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Bottom Controls Area (Speedometer, Family Avatars Quick Strip, and Floating Actions)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Speedometer HUD on bottom left + Camera approaching banner
            SpeedometerHud(
                currentSpeedKmH = uiState.currentSpeedKmH,
                speedLimitKmH = uiState.currentSpeedLimitKmH,
                isSpeeding = uiState.isSpeeding,
                approachingCameraAlert = uiState.approachingCameraAlert,
                proximityEvent = uiState.cameraProximityEvent,
                onDismissCameraAlert = { viewModel.dismissCameraAlert() }
            )

            // Family Live Member Quick Carousel (Tap any to see location & Call button!)
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Mi Familia en Tiempo Real",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Toca para llamar o ver ruta",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(uiState.familyMembers) { member ->
                            val memberColor = Color(member.avatarBgColorHex)
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable { viewModel.selectMember(member) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(memberColor)
                                            .border(1.5.dp, Color.White, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(member.avatarEmoji, fontSize = 16.sp)
                                    }

                                    Column {
                                        Text(
                                            text = member.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${member.currentSpeedKmH} km/h • 🔋${member.batteryLevel}%",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Navigation Action Bar (Route Planner, Report Fotomulta, Chat, VPN, SOS)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Route button
                Button(
                    onClick = { viewModel.openRoutePlanner() },
                    colors = ButtonDefaults.buttonColors(containerColor = WazeCyanPrimary),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("open_route_planner_button")
                ) {
                    Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                    Text("Crear Ruta", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                // Report Fotomulta / Hazard FAB (Like Waze's orange report button)
                FloatingActionButton(
                    onClick = { viewModel.openReportModal() },
                    containerColor = TrafficOrange,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(52.dp)
                        .testTag("report_alert_fab")
                ) {
                    Icon(Icons.Default.AddAlert, contentDescription = "Reportar fotomulta o tráfico", modifier = Modifier.size(26.dp))
                }

                // Chat button
                IconButton(
                    onClick = { viewModel.openChatModal() },
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.size(48.dp)
                ) {
                    BadgedBox(badge = {
                        Badge { Text("${uiState.messages.size}") }
                    }) {
                        Icon(Icons.Default.Chat, contentDescription = "Chat familiar", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }

                // Share my location
                IconButton(
                    onClick = { viewModel.shareLocation(context) },
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.ShareLocation, contentDescription = "Compartir ubicación", tint = MaterialTheme.colorScheme.onSurface)
                }

                // Emergency SOS Button
                FloatingActionButton(
                    onClick = { viewModel.openSosModal() },
                    containerColor = SpeedCameraRed,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("sos_fab")
                ) {
                    Text("SOS", fontWeight = FontWeight.Black, fontSize = 14.sp)
                }
            }
        }

        // 4. Modals & Sheets
        if (uiState.showFamilySheet && uiState.selectedMember != null) {
            FamilyMemberSheet(
                member = uiState.selectedMember!!,
                onCallClick = { viewModel.callMember(context, it) },
                onSendMessage = { member, msg -> viewModel.sendQuickMessage(context, member, msg) },
                onCreateRoute = { viewModel.createRouteToMember(it) },
                onShareLocation = { viewModel.shareLocation(context, it) },
                onDismiss = { viewModel.selectMember(null) }
            )
        }

        if (uiState.showReportAlertModal) {
            ReportAlertModal(
                onDismiss = { viewModel.closeReportModal() },
                onSubmitReport = { type, desc, limit -> viewModel.reportAlert(type, desc, limit) }
            )
        }

        if (uiState.showRoutePlannerModal) {
            RoutePlannerModal(
                familyMembers = uiState.familyMembers,
                safeZones = uiState.safeZones,
                onSelectDestination = { dest, name -> viewModel.createRouteToDestination(dest, name) },
                onDismiss = { viewModel.closeRoutePlanner() }
            )
        }

        if (uiState.showVpnModal) {
            VpnStatusModal(
                vpnState = uiState.vpnState,
                onToggleAutoVpn = { viewModel.toggleAutoVpn(it) },
                onToggleOfflineMode = { viewModel.toggleOfflineMode(it) },
                onDismiss = { viewModel.closeVpnModal() }
            )
        }

        if (uiState.showChatModal) {
            FamilyChatModal(
                messages = uiState.messages,
                onSendMessage = { viewModel.sendChatMessage(it) },
                onDismiss = { viewModel.closeChatModal() }
            )
        }

        if (uiState.showFamilyManageModal) {
            FamilyManagementModal(
                familyMembers = uiState.familyMembers,
                onSelectMember = { viewModel.selectMember(it) },
                onAddMember = { name, role, emoji, color, phone, vehicle -> viewModel.addFamilyMember(name, role, emoji, color, phone, vehicle) },
                onDismiss = { viewModel.closeFamilyManageModal() }
            )
        }

        if (uiState.showSosModal) {
            EmergencySosModal(
                onConfirmSos = { viewModel.confirmSos(context) },
                onDismiss = { viewModel.closeSosModal() }
            )
        }

        if (uiState.showGoogleSyncModal) {
            GoogleSyncSheet(
                onDismiss = { viewModel.closeGoogleSyncModal() }
            )
        }

        // Alert popup when tapping on any traffic alert
        uiState.selectedAlert?.let { alert ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(alert.type.iconEmoji, fontSize = 28.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = alert.type.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            if (alert.speedLimitKmH != null) {
                                Text(
                                    text = "Límite: ${alert.speedLimitKmH} km/h",
                                    fontWeight = FontWeight.Bold,
                                    color = SpeedCameraRed,
                                    fontSize = 13.sp
                                )
                            }
                        }
                        IconButton(onClick = { viewModel.selectAlert(null) }) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar")
                        }
                    }

                    Text(
                        text = alert.description,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "📍 ${alert.location.streetName} • Reportado por: ${alert.reportedBy}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = { viewModel.selectAlert(null) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Entendido", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Smart In-App Notifications (Auto-disappearing alerts at the top)
        NotificationHost(
            notifications = uiState.activeNotifications,
            onDismissNotification = { viewModel.dismissNotification(it) },
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}
