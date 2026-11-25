package com.um.carpool.feature.auth.data

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.um.carpool.feature.auth.domain.model.UserProfile
import com.um.carpool.feature.auth.domain.model.VehicleInfo
import com.um.carpool.feature.auth.domain.repository.AuthRepository
import com.um.carpool.feature.auth.domain.validation.AuthValidator
import com.um.carpool.feature.auth.domain.validation.ValidationResult
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val appContext: Context
) : AuthRepository {

    private fun isFirebaseConfigured(): Boolean =
        FirebaseApp.getApps(appContext).isNotEmpty()

    private fun requireFirebaseAuth(): FirebaseAuth {
        check(isFirebaseConfigured()) {
            "Firebase is not configured. Place google-services.json inside app/ and rebuild."
        }
        return FirebaseAuth.getInstance()
    }

    private fun requireFirestore(): FirebaseFirestore {
        check(isFirebaseConfigured()) {
            "Firebase is not configured. Place google-services.json inside app/ and rebuild."
        }
        return FirebaseFirestore.getInstance()
    }

    override fun getCurrentUserId(): String? {
        if (!isFirebaseConfigured()) return null
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    override fun getCurrentUserEmail(): String? {
        if (!isFirebaseConfigured()) return null
        return FirebaseAuth.getInstance().currentUser?.email
    }

    override suspend fun login(email: String, password: String): Result<UserProfile?> = runCatching {
        val normalizedEmail = AuthValidator.normalizeEmail(email)
        when (val emailValidation = AuthValidator.validateSiswaEmail(normalizedEmail)) {
            is ValidationResult.Invalid -> throw IllegalArgumentException(emailValidation.message)
            ValidationResult.Valid -> Unit
        }

        val authResult = requireFirebaseAuth()
            .signInWithEmailAndPassword(normalizedEmail, password)
            .await()

        val uid = authResult.user?.uid
            ?: throw IllegalStateException("Authentication succeeded without a user ID.")

        getUserProfile(uid).getOrThrow()
    }

    override suspend fun register(email: String, password: String): Result<String> = runCatching {
        val normalizedEmail = AuthValidator.normalizeEmail(email)
        when (val emailValidation = AuthValidator.validateSiswaEmail(normalizedEmail)) {
            is ValidationResult.Invalid -> throw IllegalArgumentException(emailValidation.message)
            ValidationResult.Valid -> Unit
        }

        val authResult = requireFirebaseAuth()
            .createUserWithEmailAndPassword(normalizedEmail, password)
            .await()

        val firebaseUser = authResult.user
            ?: throw IllegalStateException("Registration failed to create a Firebase user.")

        // Send UM Siswa verification link (FR-1.1)
        runCatching { firebaseUser.sendEmailVerification().await() }

        firebaseUser.uid
    }

    override suspend fun getUserProfile(userId: String): Result<UserProfile?> = runCatching {
        val snapshot = requireFirestore()
            .collection(USERS_COLLECTION)
            .document(userId)
            .get()
            .await()

        if (!snapshot.exists()) {
            return@runCatching null
        }

        val data = snapshot.data ?: return@runCatching null
        mapToUserProfile(userId, data)
    }

    override suspend fun saveUserProfile(profile: UserProfile): Result<UserProfile> = runCatching {
        require(profile.uid.isNotBlank()) { "Cannot save profile without an authenticated userId." }

        val payload = mutableMapOf<String, Any?>(
            "uid" to profile.uid,
            "name" to profile.fullName.trim(),
            "fullName" to profile.fullName.trim(),
            "siswaEmail" to AuthValidator.normalizeEmail(profile.siswaEmail),
            "matricId" to profile.matricId.trim().uppercase(),
            "faculty" to profile.faculty,
            "residentialZone" to profile.residentialZone,
            "genderPreference" to profile.genderPreference,
            "gender" to profile.genderPreference,
            "role" to profile.role,
            "ratingAvg" to profile.ratingAvg,
            "totalCo2SavedKg" to profile.totalCo2SavedKg
        )

        profile.vehicleInfo?.let { vehicle ->
            payload["vehicleInfo"] = mapOf(
                "model" to vehicle.model.trim(),
                "licensePlate" to vehicle.licensePlate.trim().uppercase(),
                "plate" to vehicle.licensePlate.trim().uppercase(),
                "color" to vehicle.color.trim(),
                "seatCapacity" to vehicle.seatCapacity,
                "seats" to vehicle.seatCapacity
            )
        }

        requireFirestore()
            .collection(USERS_COLLECTION)
            .document(profile.uid)
            .set(payload, SetOptions.merge())
            .await()

        profile
    }

    override suspend fun saveDriverVehicle(
        userId: String,
        vehicleInfo: VehicleInfo
    ): Result<UserProfile> = runCatching {
        require(userId.isNotBlank()) { "Cannot register vehicle without an authenticated userId." }
        when (val seatCheck = AuthValidator.validateSeatCapacity(vehicleInfo.seatCapacity)) {
            is ValidationResult.Invalid -> throw IllegalArgumentException(seatCheck.message)
            ValidationResult.Valid -> Unit
        }

        val normalizedVehicle = vehicleInfo.copy(
            model = vehicleInfo.model.trim(),
            licensePlate = vehicleInfo.licensePlate.trim().uppercase(),
            color = vehicleInfo.color.trim()
        )

        val updates = mapOf(
            "role" to UserProfile.ROLE_DRIVER,
            "vehicleInfo" to mapOf(
                "model" to normalizedVehicle.model,
                "licensePlate" to normalizedVehicle.licensePlate,
                "plate" to normalizedVehicle.licensePlate,
                "color" to normalizedVehicle.color,
                "seatCapacity" to normalizedVehicle.seatCapacity,
                "seats" to normalizedVehicle.seatCapacity
            )
        )

        requireFirestore()
            .collection(USERS_COLLECTION)
            .document(userId)
            .set(updates, SetOptions.merge())
            .await()

        getUserProfile(userId).getOrNull() ?: UserProfile(
            uid = userId,
            siswaEmail = getCurrentUserEmail().orEmpty(),
            role = UserProfile.ROLE_DRIVER,
            vehicleInfo = normalizedVehicle
        )
    }

    override fun logout() {
        if (isFirebaseConfigured()) {
            FirebaseAuth.getInstance().signOut()
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun mapToUserProfile(uid: String, data: Map<String, Any?>): UserProfile {
        val vehicleMap = data["vehicleInfo"] as? Map<String, Any?>
        val vehicle = vehicleMap?.let { map ->
            val model = (map["model"] as? String).orEmpty()
            val plate = (map["licensePlate"] as? String)
                ?: (map["plate"] as? String).orEmpty()
            val color = (map["color"] as? String).orEmpty()
            val seats = ((map["seatCapacity"] ?: map["seats"]) as? Number)?.toInt() ?: 4
            if (model.isNotBlank() || plate.isNotBlank()) {
                VehicleInfo(
                    model = model,
                    licensePlate = plate,
                    color = color,
                    seatCapacity = seats
                )
            } else {
                null
            }
        }

        return UserProfile(
            uid = uid,
            fullName = ((data["fullName"] ?: data["name"]) as? String).orEmpty(),
            siswaEmail = (data["siswaEmail"] as? String).orEmpty(),
            matricId = (data["matricId"] as? String).orEmpty(),
            faculty = (data["faculty"] as? String).orEmpty(),
            residentialZone = (data["residentialZone"] as? String).orEmpty(),
            genderPreference = (data["genderPreference"] as? String) ?: "No Preference",
            role = (data["role"] as? String) ?: UserProfile.ROLE_RIDER,
            ratingAvg = (data["ratingAvg"] as? Number)?.toDouble() ?: 5.0,
            totalCo2SavedKg = (data["totalCo2SavedKg"] as? Number)?.toDouble() ?: 0.0,
            vehicleInfo = vehicle
        )
    }

    companion object {
        private const val USERS_COLLECTION = "users"
    }
}
