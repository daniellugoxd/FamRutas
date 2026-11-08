package com.example.data.api

import android.util.Log
import com.example.model.TrafficApiResponse
import com.example.model.TrafficCongestionLevel
import com.example.model.TrafficSegment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlin.random.Random

private const val TAG = "TrafficService"

/**
 * Service that periodically polls traffic condition APIs (simulating Google Maps Traffic API
 * with live dynamic conditions, realistic street segments and congestion updates).
 */
class TrafficApiService {

    // Base street coordinates for Bogotá / Navigation reference center
    private val baseLat = 4.6550
    private val baseLng = -74.0810

    /**
     * Periodically queries the traffic API every [pollIntervalMillis] (e.g. 15 seconds)
     * and emits updated traffic segments to be overlaid on the map.
     */
    fun pollTrafficUpdates(pollIntervalMillis: Long = 12000L): Flow<TrafficApiResponse> = flow {
        var cycle = 0
        while (true) {
            cycle++
            Log.d(TAG, "Consultando API de tráfico en tiempo real (Ciclo #$cycle)...")
            val segments = fetchRealtimeTrafficSegments(cycle)
            val alert = if (segments.any { it.congestionLevel == TrafficCongestionLevel.SEVERE }) {
                "Congestión alta en arterias principales detectada por sensores de tráfico"
            } else null

            val response = TrafficApiResponse(
                status = "OK",
                source = "Google Maps Traffic API Telemetry",
                queryTimestamp = System.currentTimeMillis(),
                segments = segments,
                generalAlert = alert
            )

            emit(response)
            delay(pollIntervalMillis)
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Generates dynamic traffic data across actual street grid segments.
     */
    fun fetchRealtimeTrafficSegments(cycle: Int = 1): List<TrafficSegment> {
        val now = System.currentTimeMillis()

        // Dynamic fluctuations based on simulated rush-hour and real-time conditions
        val seg1Level = when (cycle % 4) {
            0 -> TrafficCongestionLevel.SEVERE
            1 -> TrafficCongestionLevel.HEAVY
            2 -> TrafficCongestionLevel.MODERATE
            else -> TrafficCongestionLevel.FREE_FLOW
        }

        val seg2Level = when ((cycle + 1) % 4) {
            0 -> TrafficCongestionLevel.MODERATE
            1 -> TrafficCongestionLevel.FREE_FLOW
            2 -> TrafficCongestionLevel.HEAVY
            else -> TrafficCongestionLevel.MODERATE
        }

        val seg3Level = when ((cycle + 2) % 3) {
            0 -> TrafficCongestionLevel.FREE_FLOW
            1 -> TrafficCongestionLevel.FREE_FLOW
            else -> TrafficCongestionLevel.MODERATE
        }

        return listOf(
            // North-South Central Expressway (Av. Carrera 30 / NQS)
            TrafficSegment(
                id = "seg_nqs_central",
                streetName = "Av. Carrera 30 (NQS) - Tramo Central",
                startLat = baseLat + 0.012,
                startLng = baseLng,
                endLat = baseLat - 0.008,
                endLng = baseLng,
                congestionLevel = seg1Level,
                speedKmh = when (seg1Level) {
                    TrafficCongestionLevel.FREE_FLOW -> 58
                    TrafficCongestionLevel.MODERATE -> 38
                    TrafficCongestionLevel.HEAVY -> 18
                    TrafficCongestionLevel.SEVERE -> 9
                },
                delaySeconds = if (seg1Level == TrafficCongestionLevel.SEVERE) 720 else 180,
                timestamp = now
            ),
            // East-West Arterial (Calle 63)
            TrafficSegment(
                id = "seg_cll63_arterial",
                streetName = "Calle 63 - Corredor Salitre",
                startLat = baseLat + 0.003,
                startLng = baseLng - 0.011,
                endLat = baseLat + 0.002,
                endLng = baseLng + 0.011,
                congestionLevel = seg2Level,
                speedKmh = when (seg2Level) {
                    TrafficCongestionLevel.FREE_FLOW -> 52
                    TrafficCongestionLevel.MODERATE -> 32
                    TrafficCongestionLevel.HEAVY -> 14
                    TrafficCongestionLevel.SEVERE -> 8
                },
                delaySeconds = if (seg2Level == TrafficCongestionLevel.HEAVY) 340 else 60,
                timestamp = now
            ),
            // South Avenue (Calle 53)
            TrafficSegment(
                id = "seg_cll53",
                streetName = "Calle 53 - Sector Galerías",
                startLat = baseLat - 0.005,
                startLng = baseLng - 0.008,
                endLat = baseLat - 0.004,
                endLng = baseLng + 0.010,
                congestionLevel = TrafficCongestionLevel.MODERATE,
                speedKmh = 34,
                delaySeconds = 120,
                timestamp = now
            ),
            // West Corridor (Av. 68)
            TrafficSegment(
                id = "seg_av68_west",
                streetName = "Av. Carrera 68",
                startLat = baseLat + 0.010,
                startLng = baseLng - 0.005,
                endLat = baseLat - 0.010,
                endLng = baseLng - 0.005,
                congestionLevel = seg3Level,
                speedKmh = if (seg3Level == TrafficCongestionLevel.FREE_FLOW) 60 else 35,
                delaySeconds = 30,
                timestamp = now
            ),
            // East Parkway (Carrera 24)
            TrafficSegment(
                id = "seg_kr24_east",
                streetName = "Carrera 24 - Parkway",
                startLat = baseLat + 0.010,
                startLng = baseLng + 0.005,
                endLat = baseLat - 0.010,
                endLng = baseLng + 0.005,
                congestionLevel = TrafficCongestionLevel.FREE_FLOW,
                speedKmh = 48,
                delaySeconds = 0,
                timestamp = now
            ),
            // Roundabout / Glorieta flow
            TrafficSegment(
                id = "seg_glorieta",
                streetName = "Glorieta de la 63",
                startLat = baseLat + 0.002,
                startLng = baseLng - 0.002,
                endLat = baseLat + 0.002,
                endLng = baseLng + 0.002,
                congestionLevel = if (seg1Level == TrafficCongestionLevel.SEVERE) TrafficCongestionLevel.HEAVY else TrafficCongestionLevel.MODERATE,
                speedKmh = 22,
                delaySeconds = 90,
                timestamp = now
            )
        )
    }
}
