package com.example.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.R
import com.example.data.local.FamRutaDatabase
import com.example.model.FirestoreRoute
import com.google.android.gms.location.*
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TAG = "LocationService"

/**
 * Service that tracks real-time user GPS coordinates using FusedLocationProviderClient
 * and synchronizes live location fields to the Firestore 'Route' entity and local Room database.
 */
class LocationService(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    private val db: FirebaseFirestore by lazy {
        val dbId = context.getString(R.string.firestore_database_id)
        FirebaseFirestore.getInstance(dbId)
    }

    private val roomDao by lazy {
        FamRutaDatabase.getInstance(context).routeDao()
    }

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()

    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private var activeRouteId: String = "active_user_route"

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val lastLocation = result.lastLocation ?: return
            _currentLocation.value = lastLocation
            Log.d(TAG, "GPS fix: ${lastLocation.latitude}, ${lastLocation.longitude} Speed: ${lastLocation.speed * 3.6} km/h")

            // Update Firestore 'Route' entity location fields
            scope.launch {
                updateFirestoreRouteLocation(lastLocation)
            }
        }

        override fun onLocationAvailability(availability: LocationAvailability) {
            Log.d(TAG, "Location availability: ${availability.isLocationAvailable}")
        }
    }

    fun startTracking(routeId: String = "active_user_route") {
        this.activeRouteId = routeId
        val fineLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fineLocationPermission && !coarseLocationPermission) {
            Log.w(TAG, "Cannot start tracking: Location permissions not granted")
            return
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            3000L // 3 seconds interval
        ).apply {
            setMinUpdateIntervalMillis(1500L)
            setMinUpdateDistanceMeters(2.0f) // 2 meters
            setWaitForAccurateLocation(false)
        }.build()

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            _isTracking.value = true
            Log.i(TAG, "Location tracking started successfully with FusedLocationProviderClient")

            // Also request last known location immediately
            fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) {
                    _currentLocation.value = loc
                    scope.launch { updateFirestoreRouteLocation(loc) }
                }
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException starting location updates", e)
        }
    }

    fun stopTracking() {
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            _isTracking.value = false
            Log.i(TAG, "Location tracking stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping location updates", e)
        }
    }

    /**
     * Updates Firestore 'Route' entity location fields (currentLatitude, currentLongitude,
     * currentSpeedKmH, currentHeading, updatedAt) and active status.
     */
    private suspend fun updateFirestoreRouteLocation(location: Location) {
        val uid = Firebase.auth.currentUser?.uid ?: return
        val speedKmH = (location.speed * 3.6).toDouble()
        val heading = location.bearing

        val routeDocRef = db.collection("routes").document("${uid}_$activeRouteId")

        val payload = hashMapOf<String, Any>(
            "userId" to uid,
            "title" to "Ruta en Vivo",
            "currentLatitude" to location.latitude,
            "currentLongitude" to location.longitude,
            "currentSpeedKmH" to speedKmH,
            "currentHeading" to heading,
            "isActive" to true,
            "isSharedWithFamily" to true,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            routeDocRef.set(payload, SetOptions.merge()).await()
            Log.d(TAG, "Firestore Route '${routeDocRef.id}' location updated: ${location.latitude}, ${location.longitude}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update Firestore Route entity location", e)
        }

        // Also update circle member telemetry if in circle
        try {
            val userCircleDoc = db.collection("circles").document("fam_circle_default")
                .collection("members").document(uid)

            val memberPayload = hashMapOf<String, Any>(
                "userId" to uid,
                "lat" to location.latitude,
                "lng" to location.longitude,
                "speedKmH" to speedKmH.toInt(),
                "isMoving" to (speedKmH > 2.0),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            userCircleDoc.set(memberPayload, SetOptions.merge()).await()
        } catch (e: Exception) {
            // Silently ignore if circle doesn't exist
        }
    }

    /**
     * Initializes or assigns a route destination to the Firestore Route entity.
     */
    suspend fun assignRouteDestination(
        destinationName: String,
        destLat: Double,
        destLng: Double,
        startLat: Double,
        startLng: Double,
        startAddress: String
    ) {
        val uid = Firebase.auth.currentUser?.uid ?: return
        val routeDocRef = db.collection("routes").document("${uid}_$activeRouteId")

        val payload = hashMapOf<String, Any>(
            "userId" to uid,
            "title" to "Viaje a $destinationName",
            "destinationName" to destinationName,
            "destinationLatitude" to destLat,
            "destinationLongitude" to destLng,
            "startLatitude" to startLat,
            "startLongitude" to startLng,
            "startAddress" to startAddress,
            "isActive" to true,
            "isSharedWithFamily" to true,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            routeDocRef.set(payload, SetOptions.merge()).await()
            Log.i(TAG, "Firestore Route destination assigned to $destinationName")
        } catch (e: Exception) {
            Log.e(TAG, "Error assigning route destination in Firestore", e)
        }
    }
}
