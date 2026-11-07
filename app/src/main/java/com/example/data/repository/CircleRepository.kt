package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.model.FamilyMember
import com.example.model.TrafficAlert
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CircleRepository(private val context: Context) {

    private val db: FirebaseFirestore by lazy {
        val dbId = context.getString(R.string.firestore_database_id)
        FirebaseFirestore.getInstance(dbId)
    }

    private val auth = Firebase.auth

    suspend fun updateMemberLocation(
        circleId: String,
        member: FamilyMember
    ) {
        val uid = auth.currentUser?.uid ?: return
        try {
            val docRef = db.collection("circles").document(circleId).collection("members").document(uid)
            val data = hashMapOf(
                "userId" to uid,
                "name" to member.name,
                "role" to member.role.name,
                "avatarEmoji" to member.avatarEmoji,
                "avatarBgColorHex" to member.avatarBgColorHex,
                "vehicleType" to member.vehicleType.name,
                "phoneNumber" to member.phoneNumber,
                "lat" to member.location.lat,
                "lng" to member.location.lng,
                "streetName" to member.location.streetName,
                "speedKmH" to member.currentSpeedKmH,
                "batteryLevel" to member.batteryLevel,
                "statusText" to member.statusText,
                "isMoving" to member.isMoving,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(data, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e("CircleRepo", "Error al actualizar ubicación en Firestore", e)
        }
    }

    suspend fun createFamilyCircle(circleName: String, inviteCode: String): String {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("Usuario no autenticado")
        val circleRef = db.collection("circles").document()
        val data = hashMapOf(
            "name" to circleName,
            "ownerId" to uid,
            "inviteCode" to inviteCode,
            "memberUids" to listOf(uid),
            "createdAt" to FieldValue.serverTimestamp()
        )
        circleRef.set(data).await()
        return circleRef.id
    }

    suspend fun publishAlert(alert: TrafficAlert) {
        val uid = auth.currentUser?.uid ?: return
        try {
            val docRef = db.collection("alerts").document(alert.id)
            val data = hashMapOf(
                "type" to alert.type.name,
                "lat" to alert.location.lat,
                "lng" to alert.location.lng,
                "streetName" to alert.location.streetName,
                "speedLimitKmH" to (alert.speedLimitKmH ?: 0),
                "description" to alert.description,
                "reportedBy" to alert.reportedBy,
                "reporterUid" to uid,
                "createdAt" to FieldValue.serverTimestamp()
            )
            docRef.set(data).await()
        } catch (e: Exception) {
            Log.e("CircleRepo", "Error al publicar alerta", e)
        }
    }
}
