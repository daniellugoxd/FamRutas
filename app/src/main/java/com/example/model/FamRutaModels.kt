package com.example.model

data class GeoPoint(
    val lat: Double,
    val lng: Double,
    val streetName: String = ""
)

enum class FamilyRole(val label: String) {
    ESPOSA("Esposa"),
    HIJO("Hijo"),
    HIJA("Hija"),
    PAPA("Papá"),
    MAMA("Mamá"),
    OTRO("Familiar")
}

enum class VehicleType(val label: String, val iconEmoji: String) {
    AUTO("Auto", "🚗"),
    SUV("Camioneta", "🚙"),
    BUS("Autobús / Bus", "🚌"),
    MOTO("Motocicleta", "🏍️"),
    BICICLETA("Bicicleta", "🚲"),
    A_PIE("A pie", "🚶")
}

enum class AvatarAnimationState {
    NORMAL,
    MOVING_AUTO,
    MOVING_BUS,
    MOVING_MOTO,
    MOVING_BIKE,
    MOVING_WALK,
    JOY_MEETING,          // Abrazo o salto de alegría al juntarse con otro familiar
    JOY_ROUTE_EN_CAMINO,  // Alegría al ir en camino hacia un familiar
    WAITING_HAPPY,        // Esperando alegremente
    WAITING_IMPATIENT     // Espera con desespero o retraso por tráfico
}

data class ReunionEvent(
    val member1Name: String,
    val member2Name: String,
    val member1Emoji: String,
    val member2Emoji: String,
    val locationDescription: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class FamilyMember(
    val id: String,
    val name: String,
    val role: FamilyRole,
    val avatarEmoji: String,
    val avatarBgColorHex: Long,
    val vehicleType: VehicleType,
    val phoneNumber: String,
    val location: GeoPoint,
    val heading: Float = 0f,
    val currentSpeedKmH: Int = 0,
    val batteryLevel: Int = 100,
    val isMoving: Boolean = false,
    val statusText: String = "En casa",
    val destination: String? = null,
    val etaMinutes: Int? = null,
    val lastSeenMinutesAgo: Int = 0,
    val isOnline: Boolean = true,
    val animationState: AvatarAnimationState = AvatarAnimationState.NORMAL,
    val moodBubble: String? = null,
    val meetingWithMemberName: String? = null
)

enum class AlertType(
    val title: String,
    val iconEmoji: String,
    val defaultDescription: String
) {
    SPEED_CAMERA("Fotomulta / Radar", "📷", "Cámara de velocidad fija"),
    HEAVY_TRAFFIC("Tráfico pesado", "🚗", "Congestión vehicular moderada a alta"),
    POLICE("Control policial", "👮", "Puesto de control de tránsito"),
    ACCIDENT("Accidente", "💥", "Vehículo detenido en la vía"),
    HAZARD("Peligro en la vía", "⚠️", "Bache u obstáculo en el carril"),
    ROAD_WORK("Obras viales", "🚧", "Mantenimiento y reducción de calzada")
}

data class TrafficAlert(
    val id: String,
    val type: AlertType,
    val location: GeoPoint,
    val speedLimitKmH: Int? = null,
    val description: String,
    val reportedBy: String,
    val timestamp: Long = System.currentTimeMillis(),
    val upvotes: Int = 3
)

enum class TurnType {
    STRAIGHT,
    LEFT,
    RIGHT,
    SLIGHT_LEFT,
    SLIGHT_RIGHT,
    ROUNDABOUT,
    ARRIVAL
}

data class RouteInstruction(
    val distanceMeters: Int,
    val instruction: String,
    val street: String,
    val turnType: TurnType
)

data class TripRoute(
    val id: String,
    val title: String,
    val destinationName: String,
    val destinationLocation: GeoPoint,
    val distanceKm: Double,
    val durationMinutes: Int,
    val delayMinutes: Int = 0,
    val points: List<GeoPoint>,
    val steps: List<RouteInstruction>
)

data class FamilyMessage(
    val id: String,
    val senderId: String,
    val senderName: String,
    val senderEmoji: String,
    val messageText: String,
    val timeFormatted: String,
    val isEmergency: Boolean = false
)

data class SafeZone(
    val id: String,
    val name: String,
    val emoji: String,
    val location: GeoPoint,
    val radiusMeters: Int
)

data class VpnState(
    val isConnected: Boolean = true,
    val isAutoVpnEnabled: Boolean = true,
    val ipAddress: String = "10.8.42.17 (Túnel Familiar Seguro)",
    val serverLocation: String = "Mesh P2P Privado Encriptado",
    val pingMs: Int = 24,
    val encryption: String = "WireGuard / ChaCha20-Poly1305",
    val bytesTransferred: String = "14.2 MB",
    val offlineCachedTilesMb: Double = 64.5,
    val isOfflineModeActive: Boolean = false
)
