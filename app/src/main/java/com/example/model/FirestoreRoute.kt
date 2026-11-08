package com.example.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class FirestoreRoute(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val currentLatitude: Double = 0.0,
    val currentLongitude: Double = 0.0,
    val currentSpeedKmH: Double = 0.0,
    val currentHeading: Float = 0f,
    val startLatitude: Double = 0.0,
    val startLongitude: Double = 0.0,
    val startAddress: String = "",
    val destinationLatitude: Double = 0.0,
    val destinationLongitude: Double = 0.0,
    val destinationName: String = "",
    val isSharedWithFamily: Boolean = false,
    val sharedCircleId: String = "",
    val isActive: Boolean = false,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
