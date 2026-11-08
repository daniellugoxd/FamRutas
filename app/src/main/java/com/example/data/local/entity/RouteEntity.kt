package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Database entity representing a GPS Navigation Route.
 * Stores starting points, destinations, route telemetry, and shared status.
 */
@Entity(tableName = "routes")
data class RouteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    // Starting Point
    val startLatitude: Double,
    val startLongitude: Double,
    val startAddress: String,
    // Destination Point
    val destinationLatitude: Double,
    val destinationLongitude: Double,
    val destinationAddress: String,
    val destinationName: String,
    // Route metrics
    val distanceKm: Double,
    val estimatedMinutes: Int,
    // Status
    val isSharedWithFamily: Boolean = false,
    val sharedCircleId: String? = null,
    val isActive: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
