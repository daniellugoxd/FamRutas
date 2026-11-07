package com.example.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MockDataProvider
import com.example.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.*

data class FamRutaUiState(
    val familyMembers: List<FamilyMember> = MockDataProvider.initialFamilyMembers,
    val selectedMember: FamilyMember? = null,
    val trafficAlerts: List<TrafficAlert> = MockDataProvider.initialAlerts,
    val selectedAlert: TrafficAlert? = null,
    val safeZones: List<SafeZone> = MockDataProvider.safeZones,
    val activeRoute: TripRoute? = null,
    val currentNavStepIndex: Int = 0,
    val isNavigating: Boolean = false,
    val currentSpeedKmH: Int = 54,
    val currentSpeedLimitKmH: Int = 60,
    val isSpeeding: Boolean = false,
    val vpnState: VpnState = VpnState(),
    val messages: List<FamilyMessage> = MockDataProvider.sampleMessages,
    val isSosActive: Boolean = false,
    val sosCountdownSeconds: Int = 0,
    val approachingCameraAlert: TrafficAlert? = null,
    val isDayMode: Boolean = false,
    val activeMapFilter: MapFilter = MapFilter.ALL,
    val showFamilySheet: Boolean = false,
    val showReportAlertModal: Boolean = false,
    val showRoutePlannerModal: Boolean = false,
    val showVpnModal: Boolean = false,
    val showChatModal: Boolean = false,
    val showFamilyManageModal: Boolean = false,
    val showSosModal: Boolean = false,
    val showGoogleSyncModal: Boolean = false,
    val activeRouteTargetMemberId: String? = null,
    val isRouteDelayed: Boolean = false,
    val reunionEvent: ReunionEvent? = null,
    val userHeading: Float = 45f
)

enum class MapFilter(val label: String) {
    ALL("Todo"),
    FAMILY("Familia"),
    CAMERAS("Fotomultas"),
    TRAFFIC("Tráfico y Radar"),
    ZONES("Zonas Seguras")
}

class FamRutaViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FamRutaUiState())
    val uiState: StateFlow<FamRutaUiState> = _uiState.asStateFlow()

    init {
        startLiveMovementSimulation()
        startSpeedometerSimulation()
    }

    private fun startLiveMovementSimulation() {
        viewModelScope.launch {
            while (true) {
                delay(2500)
                _uiState.update { state ->
                    // Update locations of moving family members slightly to simulate live movement on streets
                    val updatedMembers = state.familyMembers.map { member ->
                        if (member.isMoving) {
                            val headingRad = Math.toRadians(member.heading.toDouble())
                            val deltaLat = (sin(headingRad) * 0.00035 * (member.currentSpeedKmH / 50.0))
                            val deltaLng = (cos(headingRad) * 0.00035 * (member.currentSpeedKmH / 50.0))
                            val newLat = member.location.lat + deltaLat
                            val newLng = member.location.lng + deltaLng

                            // Slight speed fluctuation for realism
                            val speedChange = (-2..2).random()
                            val newSpeed = (member.currentSpeedKmH + speedChange).coerceIn(20, 85)

                            member.copy(
                                location = member.location.copy(lat = newLat, lng = newLng),
                                currentSpeedKmH = newSpeed
                            )
                        } else {
                            member
                        }
                    }

                    // Detect if any family members are together (distance < 75 meters)
                    var detectedReunion: ReunionEvent? = state.reunionEvent
                    val membersMeeting = mutableSetOf<String>()

                    for (i in 0 until updatedMembers.size) {
                        for (j in i + 1 until updatedMembers.size) {
                            val m1 = updatedMembers[i]
                            val m2 = updatedMembers[j]
                            val dist = calculateDistanceMeters(m1.location, m2.location)
                            if (dist < 75.0) {
                                membersMeeting.add(m1.id)
                                membersMeeting.add(m2.id)
                                if (detectedReunion == null) {
                                    detectedReunion = ReunionEvent(
                                        member1Name = m1.name,
                                        member2Name = m2.name,
                                        member1Emoji = m1.avatarEmoji,
                                        member2Emoji = m2.avatarEmoji,
                                        locationDescription = m1.location.streetName
                                    )
                                }
                            }
                        }
                    }

                    // Assign animated states and mood bubbles
                    val animatedMembers = updatedMembers.map { member ->
                        val isMeeting = membersMeeting.contains(member.id)
                        val isNavigatingToMe = state.activeRouteTargetMemberId == member.id
                        val isUserHeadingToTarget = member.id == "user_me" && state.activeRouteTargetMemberId != null

                        val animState = when {
                            isMeeting -> AvatarAnimationState.JOY_MEETING
                            isUserHeadingToTarget -> AvatarAnimationState.JOY_ROUTE_EN_CAMINO
                            isNavigatingToMe && state.isRouteDelayed -> AvatarAnimationState.WAITING_IMPATIENT
                            isNavigatingToMe -> AvatarAnimationState.WAITING_HAPPY
                            member.isMoving -> when (member.vehicleType) {
                                VehicleType.AUTO, VehicleType.SUV -> AvatarAnimationState.MOVING_AUTO
                                VehicleType.BUS -> AvatarAnimationState.MOVING_BUS
                                VehicleType.MOTO -> AvatarAnimationState.MOVING_MOTO
                                VehicleType.BICICLETA -> AvatarAnimationState.MOVING_BIKE
                                VehicleType.A_PIE -> AvatarAnimationState.MOVING_WALK
                            }
                            else -> AvatarAnimationState.NORMAL
                        }

                        val bubble = when (animState) {
                            AvatarAnimationState.JOY_MEETING -> "¡Abrazo familiar! 🤗❤️"
                            AvatarAnimationState.JOY_ROUTE_EN_CAMINO -> "¡Voy por ti! 🥰🎉"
                            AvatarAnimationState.WAITING_IMPATIENT -> "¡Apúrate! ¿Por qué tardas? ⌚😤"
                            AvatarAnimationState.WAITING_HAPPY -> "¡Esperando con calma! 👋❤️"
                            AvatarAnimationState.MOVING_AUTO -> "En auto 🚗💨"
                            AvatarAnimationState.MOVING_BUS -> "En autobús 🚌💨"
                            AvatarAnimationState.MOVING_MOTO -> "En moto 🏍️⚡"
                            AvatarAnimationState.MOVING_BIKE -> "En bicicleta 🚲💨"
                            AvatarAnimationState.MOVING_WALK -> "Caminando 🚶✨"
                            AvatarAnimationState.NORMAL -> member.statusText
                        }

                        member.copy(
                            animationState = animState,
                            moodBubble = bubble
                        )
                    }

                    // Check if approaching any speed camera
                    val myLocation = animatedMembers.firstOrNull { it.id == "user_me" }?.location
                    val nearbyCamera = if (myLocation != null) {
                        state.trafficAlerts.firstOrNull { alert ->
                            alert.type == AlertType.SPEED_CAMERA &&
                                    calculateDistanceMeters(myLocation, alert.location) < 350
                        }
                    } else null

                    state.copy(
                        familyMembers = animatedMembers,
                        approachingCameraAlert = nearbyCamera,
                        reunionEvent = detectedReunion
                    )
                }
            }
        }
    }

    private fun startSpeedometerSimulation() {
        viewModelScope.launch {
            while (true) {
                delay(1200)
                _uiState.update { state ->
                    if (state.isNavigating) {
                        val delta = (-3..4).random()
                        val newSpeed = (state.currentSpeedKmH + delta).coerceIn(40, 78)
                        val speeding = newSpeed > state.currentSpeedLimitKmH
                        state.copy(
                            currentSpeedKmH = newSpeed,
                            isSpeeding = speeding
                        )
                    } else {
                        state
                    }
                }
            }
        }
    }

    fun selectMember(member: FamilyMember?) {
        _uiState.update { it.copy(selectedMember = member, showFamilySheet = member != null) }
    }

    fun selectAlert(alert: TrafficAlert?) {
        _uiState.update { it.copy(selectedAlert = alert) }
    }

    fun setMapFilter(filter: MapFilter) {
        _uiState.update { it.copy(activeMapFilter = filter) }
    }

    fun toggleDayNightMode() {
        _uiState.update { it.copy(isDayMode = !it.isDayMode) }
    }

    fun dismissCameraAlert() {
        _uiState.update { it.copy(approachingCameraAlert = null) }
    }

    fun openReportModal() {
        _uiState.update { it.copy(showReportAlertModal = true) }
    }

    fun closeReportModal() {
        _uiState.update { it.copy(showReportAlertModal = false) }
    }

    fun openRoutePlanner() {
        _uiState.update { it.copy(showRoutePlannerModal = true) }
    }

    fun closeRoutePlanner() {
        _uiState.update { it.copy(showRoutePlannerModal = false) }
    }

    fun openVpnModal() {
        _uiState.update { it.copy(showVpnModal = true) }
    }

    fun closeVpnModal() {
        _uiState.update { it.copy(showVpnModal = false) }
    }

    fun openChatModal() {
        _uiState.update { it.copy(showChatModal = true) }
    }

    fun closeChatModal() {
        _uiState.update { it.copy(showChatModal = false) }
    }

    fun openFamilyManageModal() {
        _uiState.update { it.copy(showFamilyManageModal = true) }
    }

    fun closeFamilyManageModal() {
        _uiState.update { it.copy(showFamilyManageModal = false) }
    }

    fun openSosModal() {
        _uiState.update { it.copy(showSosModal = true, sosCountdownSeconds = 3) }
    }

    fun closeSosModal() {
        _uiState.update { it.copy(showSosModal = false, isSosActive = false, sosCountdownSeconds = 0) }
    }

    fun openGoogleSyncModal() {
        _uiState.update { it.copy(showGoogleSyncModal = true) }
    }

    fun closeGoogleSyncModal() {
        _uiState.update { it.copy(showGoogleSyncModal = false) }
    }

    fun confirmSos(context: Context) {
        _uiState.update { it.copy(isSosActive = true) }
        vibratePhone(context, 1000)
        Toast.makeText(context, "🚨 ALERTA SOS ENVIADA A TODA LA FAMILIA", Toast.LENGTH_LONG).show()

        // Add SOS message to chat automatically
        val myUser = _uiState.value.familyMembers.firstOrNull { it.id == "user_me" }
        val sosMsg = FamilyMessage(
            id = "sos_${System.currentTimeMillis()}",
            senderId = myUser?.id ?: "user_me",
            senderName = myUser?.name ?: "Daniel",
            senderEmoji = "🚨",
            messageText = "¡EMERGENCIA! He activado el SOS Familiar desde mi ubicación actual (${myUser?.location?.streetName ?: "En ruta"}). Por favor contáctenme.",
            timeFormatted = "Ahora mismo",
            isEmergency = true
        )
        _uiState.update { it.copy(messages = it.messages + sosMsg) }
    }

    fun callMember(context: Context, member: FamilyMember) {
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${member.phoneNumber}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "No se pudo abrir el marcador telefónico", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareLocation(context: Context, member: FamilyMember? = null) {
        val target = member ?: _uiState.value.familyMembers.firstOrNull { it.id == "user_me" }
        val text = if (target != null) {
            "FamRuta GPS: Ubicación en tiempo real de ${target.name}: ${target.location.streetName} (${target.location.lat}, ${target.location.lng}) - Velocidad: ${target.currentSpeedKmH} km/h, Batería: ${target.batteryLevel}%."
        } else {
            "FamRuta GPS: Compartiendo mi ubicación familiar en tiempo real."
        }
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Compartir ubicación familiar")
        shareIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(shareIntent)
    }

    fun sendQuickMessage(context: Context, member: FamilyMember, quickText: String) {
        val newMsg = FamilyMessage(
            id = "msg_${System.currentTimeMillis()}",
            senderId = "user_me",
            senderName = "Daniel",
            senderEmoji = "👨‍💼",
            messageText = "@${member.name}: $quickText",
            timeFormatted = "Ahora mismo"
        )
        _uiState.update { it.copy(messages = it.messages + newMsg) }
        Toast.makeText(context, "Mensaje enviado a ${member.name}", Toast.LENGTH_SHORT).show()
    }

    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        val newMsg = FamilyMessage(
            id = "msg_${System.currentTimeMillis()}",
            senderId = "user_me",
            senderName = "Daniel",
            senderEmoji = "👨‍💼",
            messageText = text.trim(),
            timeFormatted = "Ahora mismo"
        )
        _uiState.update { it.copy(messages = it.messages + newMsg) }
    }

    fun createRouteToMember(member: FamilyMember) {
        val route = MockDataProvider.createSampleRouteTo(member.location, "Ubicación de ${member.name}")
        _uiState.update {
            it.copy(
                activeRoute = route,
                activeRouteTargetMemberId = member.id,
                isRouteDelayed = false,
                isNavigating = true,
                currentNavStepIndex = 0,
                selectedMember = null,
                showFamilySheet = false,
                currentSpeedLimitKmH = 60
            )
        }
    }

    fun createRouteToDestination(destination: GeoPoint, name: String) {
        val route = MockDataProvider.createSampleRouteTo(destination, name)
        _uiState.update {
            it.copy(
                activeRoute = route,
                activeRouteTargetMemberId = null,
                isRouteDelayed = false,
                isNavigating = true,
                currentNavStepIndex = 0,
                showRoutePlannerModal = false,
                currentSpeedLimitKmH = 60
            )
        }
    }

    fun stopNavigation() {
        _uiState.update {
            it.copy(
                isNavigating = false,
                activeRoute = null,
                activeRouteTargetMemberId = null,
                isRouteDelayed = false
            )
        }
    }

    fun triggerFamilyReunionSimulation() {
        val mariaLoc = _uiState.value.familyMembers.firstOrNull { it.role == FamilyRole.ESPOSA }?.location
            ?: GeoPoint(4.6680, -74.0690, "Casa (Hogar)")

        _uiState.update { state ->
            val updated = state.familyMembers.map { member ->
                if (member.role == FamilyRole.HIJO) {
                    member.copy(
                        location = mariaLoc.copy(lat = mariaLoc.lat + 0.0001, lng = mariaLoc.lng + 0.0001),
                        isMoving = false,
                        currentSpeedKmH = 0,
                        animationState = AvatarAnimationState.JOY_MEETING,
                        moodBubble = "¡Abrazo familiar! 🤗❤️"
                    )
                } else if (member.role == FamilyRole.ESPOSA) {
                    member.copy(
                        isMoving = false,
                        currentSpeedKmH = 0,
                        animationState = AvatarAnimationState.JOY_MEETING,
                        moodBubble = "¡Salto de alegría! 🎉❤️"
                    )
                } else member
            }
            state.copy(
                familyMembers = updated,
                reunionEvent = ReunionEvent(
                    member1Name = "María (Esposa)",
                    member2Name = "Lucas (Hijo)",
                    member1Emoji = "👩‍🦰",
                    member2Emoji = "👦",
                    locationDescription = "Casa (Hogar)"
                )
            )
        }
    }

    fun toggleRouteDelaySimulation() {
        _uiState.update { state ->
            val nextDelayed = !state.isRouteDelayed
            state.copy(
                isRouteDelayed = nextDelayed,
                currentSpeedLimitKmH = if (nextDelayed) 25 else 60,
                currentSpeedKmH = if (nextDelayed) 12 else 54
            )
        }
    }

    fun dismissReunionEvent() {
        _uiState.update { it.copy(reunionEvent = null) }
    }

    fun nextNavStep() {
        _uiState.update { state ->
            val route = state.activeRoute
            if (route != null && state.currentNavStepIndex < route.steps.lastIndex) {
                state.copy(currentNavStepIndex = state.currentNavStepIndex + 1)
            } else {
                state.copy(isNavigating = false, activeRoute = null)
            }
        }
    }

    fun reportAlert(type: AlertType, description: String, speedLimit: Int?) {
        val me = _uiState.value.familyMembers.firstOrNull { it.id == "user_me" }
        val location = me?.location ?: GeoPoint(4.6550, -74.0810, "En tu ubicación actual")
        val newAlert = TrafficAlert(
            id = "alert_${System.currentTimeMillis()}",
            type = type,
            location = location,
            speedLimitKmH = speedLimit,
            description = description.ifBlank { type.defaultDescription },
            reportedBy = "Daniel (Tú)"
        )
        _uiState.update {
            it.copy(
                trafficAlerts = listOf(newAlert) + it.trafficAlerts,
                showReportAlertModal = false
            )
        }
    }

    fun toggleAutoVpn(enabled: Boolean) {
        _uiState.update {
            it.copy(
                vpnState = it.vpnState.copy(
                    isAutoVpnEnabled = enabled,
                    isConnected = enabled || !it.vpnState.isOfflineModeActive
                )
            )
        }
    }

    fun toggleOfflineMode(enabled: Boolean) {
        _uiState.update {
            val vpnConnected = if (enabled && it.vpnState.isAutoVpnEnabled) true else it.vpnState.isConnected
            it.copy(
                vpnState = it.vpnState.copy(
                    isOfflineModeActive = enabled,
                    isConnected = vpnConnected
                )
            )
        }
    }

    fun updateFamilyMember(updated: FamilyMember) {
        _uiState.update { state ->
            state.copy(
                familyMembers = state.familyMembers.map { if (it.id == updated.id) updated else it }
            )
        }
    }

    fun addFamilyMember(
        name: String,
        role: FamilyRole,
        emoji: String,
        colorHex: Long,
        phone: String,
        vehicleType: VehicleType = VehicleType.AUTO
    ) {
        val newMember = FamilyMember(
            id = "member_${System.currentTimeMillis()}",
            name = name,
            role = role,
            avatarEmoji = emoji,
            avatarBgColorHex = colorHex,
            vehicleType = vehicleType,
            phoneNumber = phone,
            location = GeoPoint(4.6500 + Math.random() * 0.02, -74.0800 + Math.random() * 0.02, "En la ciudad"),
            currentSpeedKmH = if (vehicleType == VehicleType.A_PIE) 5 else 38,
            batteryLevel = 100,
            isMoving = true,
            statusText = "Recién añadido",
            isOnline = true
        )
        _uiState.update { it.copy(familyMembers = it.familyMembers + newMember) }
    }

    private fun vibratePhone(context: Context, durationMs: Long) {
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    private fun calculateDistanceMeters(p1: GeoPoint, p2: GeoPoint): Double {
        val dLat = (p1.lat - p2.lat) * 111000.0
        val dLng = (p1.lng - p2.lng) * 111000.0 * cos(Math.toRadians(p1.lat))
        return sqrt(dLat * dLat + dLng * dLng)
    }
}
