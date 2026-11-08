package com.example.model

enum class TrafficCongestionLevel(val label: String, val speedFactor: Float) {
    FREE_FLOW("Tráfico fluido", 1.0f),
    MODERATE("Tráfico moderado", 0.65f),
    HEAVY("Tráfico lento", 0.35f),
    SEVERE("Congestión severa", 0.15f)
}

data class TrafficSegment(
    val id: String,
    val streetName: String,
    val startLat: Double,
    val startLng: Double,
    val endLat: Double,
    val endLng: Double,
    val congestionLevel: TrafficCongestionLevel,
    val speedKmh: Int,
    val delaySeconds: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

data class TrafficApiResponse(
    val status: String,
    val source: String = "Google Maps Traffic Network",
    val queryTimestamp: Long = System.currentTimeMillis(),
    val segments: List<TrafficSegment>,
    val generalAlert: String? = null
)
