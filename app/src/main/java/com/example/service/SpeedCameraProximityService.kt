package com.example.service

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import com.example.model.AlertType
import com.example.model.GeoPoint
import com.example.model.TrafficAlert
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sqrt

private const val TAG = "SpeedCameraService"

data class CameraProximityEvent(
    val alert: TrafficAlert,
    val distanceMeters: Int,
    val isOverSpeedLimit: Boolean,
    val userSpeedKmH: Int
)

/**
 * Service that checks real-time user GPS coordinates against fixed speed camera coordinates (fotomultas)
 * and triggers high-visibility audio-haptic proximity alerts when entering the radar zone (< 400m).
 */
class SpeedCameraProximityService(
    private val context: Context,
    private val alertThresholdMeters: Double = 400.0
) {

    private val _activeCameraAlert = MutableStateFlow<CameraProximityEvent?>(null)
    val activeCameraAlert: StateFlow<CameraProximityEvent?> = _activeCameraAlert.asStateFlow()

    // Keep track of alerted cameras to prevent repeating sound spam within same approach
    private val alertedCameraIds = mutableSetOf<String>()

    /**
     * Checks user location and speed against a list of known fixed speed camera zones.
     */
    fun checkProximity(
        userLocation: GeoPoint,
        userSpeedKmH: Int,
        knownAlerts: List<TrafficAlert>
    ): CameraProximityEvent? {
        val speedCameras = knownAlerts.filter { it.type == AlertType.SPEED_CAMERA }

        var nearestCameraEvent: CameraProximityEvent? = null
        var minDistance = Double.MAX_VALUE

        for (camera in speedCameras) {
            val dist = calculateDistanceMeters(userLocation, camera.location)
            if (dist <= alertThresholdMeters && dist < minDistance) {
                minDistance = dist
                val speedLimit = camera.speedLimitKmH ?: 60
                val isSpeeding = userSpeedKmH > speedLimit

                nearestCameraEvent = CameraProximityEvent(
                    alert = camera,
                    distanceMeters = dist.roundToInt(),
                    isOverSpeedLimit = isSpeeding,
                    userSpeedKmH = userSpeedKmH
                )
            }
        }

        if (nearestCameraEvent != null) {
            _activeCameraAlert.value = nearestCameraEvent

            // Vibrate if entering zone for the first time or if exceeding speed limit
            val camId = nearestCameraEvent.alert.id
            if (!alertedCameraIds.contains(camId)) {
                alertedCameraIds.add(camId)
                triggerHapticWarning(nearestCameraEvent.isOverSpeedLimit)
                Log.i(TAG, "Speed camera detected: ${nearestCameraEvent.alert.description} at ${nearestCameraEvent.distanceMeters}m")
            }
        } else {
            // Cleared radar zone
            if (_activeCameraAlert.value != null) {
                _activeCameraAlert.value = null
                alertedCameraIds.clear()
            }
        }

        return nearestCameraEvent
    }

    fun dismissCurrentAlert() {
        _activeCameraAlert.value = null
    }

    private fun triggerHapticWarning(isOverSpeed: Boolean) {
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (vibrator != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val timings = if (isOverSpeed) longArrayOf(0, 300, 150, 300) else longArrayOf(0, 200)
                    val amplitudes = if (isOverSpeed) intArrayOf(0, 255, 0, 255) else intArrayOf(0, 180)
                    vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(if (isOverSpeed) 600 else 250)
                }
            }
        } catch (_: Exception) {}
    }

    private fun calculateDistanceMeters(p1: GeoPoint, p2: GeoPoint): Double {
        val dLat = (p1.lat - p2.lat) * 111000.0
        val dLng = (p1.lng - p2.lng) * 111000.0 * cos(Math.toRadians(p1.lat))
        return sqrt(dLat * dLat + dLng * dLng)
    }
}
