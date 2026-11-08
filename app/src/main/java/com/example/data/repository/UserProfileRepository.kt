package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.example.R
import com.example.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

private const val TAG = "UserProfileRepo"

class UserProfileRepository(private val context: Context) {

    private val db: FirebaseFirestore by lazy {
        val dbId = context.getString(R.string.firestore_database_id)
        FirebaseFirestore.getInstance(dbId)
    }

    private val auth = Firebase.auth

    suspend fun getUserProfile(): UserProfile? {
        val uid = auth.currentUser?.uid ?: return null
        return try {
            val doc = db.collection("users").document(uid).get().await()
            if (doc.exists()) {
                doc.toObject(UserProfile::class.java)
            } else {
                // Return default from current authenticated Firebase user
                val fbUser = auth.currentUser
                UserProfile(
                    id = uid,
                    userId = uid,
                    displayName = fbUser?.displayName.orEmpty(),
                    email = fbUser?.email.orEmpty(),
                    photoUrl = fbUser?.photoUrl?.toString().orEmpty()
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user profile from Firestore", e)
            null
        }
    }

    suspend fun saveUserProfile(displayName: String, avatarBitmap: Bitmap?, phoneNumber: String = ""): Boolean {
        val uid = auth.currentUser?.uid ?: run {
            Log.w(TAG, "User must be authenticated to save profile to Firestore")
            return false
        }

        return try {
            val avatarBase64 = if (avatarBitmap != null) {
                bitmapToBase64(avatarBitmap)
            } else ""

            val docRef = db.collection("users").document(uid)
            val data = hashMapOf<String, Any>(
                "userId" to uid,
                "displayName" to displayName,
                "email" to (auth.currentUser?.email.orEmpty()),
                "phoneNumber" to phoneNumber,
                "updatedAt" to FieldValue.serverTimestamp()
            )

            if (avatarBase64.isNotEmpty()) {
                data["avatarBase64"] = avatarBase64
            }

            docRef.set(data, SetOptions.merge()).await()

            // Also update FirebaseAuth display name in memory
            try {
                val profileUpdates = userProfileChangeRequest {
                    this.displayName = displayName
                }
                auth.currentUser?.updateProfile(profileUpdates)?.await()
            } catch (e: Exception) {
                Log.w(TAG, "Could not update FirebaseAuth profile name: ${e.message}")
            }

            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user profile to Firestore", e)
            false
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        // Compress and scale down to maintain efficiency
        val maxDimension = 320
        val scale = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
            val factor = maxDimension.toFloat() / maxOf(bitmap.width, bitmap.height)
            factor
        } else 1.0f

        val scaled = if (scale < 1.0f) {
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * scale).toInt(),
                (bitmap.height * scale).toInt(),
                true
            )
        } else {
            bitmap
        }

        val outputStream = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    companion object {
        fun base64ToBitmap(base64Str: String): Bitmap? {
            if (base64Str.isBlank()) return null
            return try {
                val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            } catch (e: Exception) {
                null
            }
        }
    }
}
