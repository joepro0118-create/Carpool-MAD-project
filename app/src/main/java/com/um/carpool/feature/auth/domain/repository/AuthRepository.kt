package com.um.carpool.feature.auth.domain.repository

import com.um.carpool.feature.auth.domain.model.UserProfile
import com.um.carpool.feature.auth.domain.model.VehicleInfo

interface AuthRepository {
    fun getCurrentUserId(): String?
    fun getCurrentUserEmail(): String?
    suspend fun login(email: String, password: String): Result<UserProfile?>
    suspend fun register(email: String, password: String): Result<String>
    suspend fun getUserProfile(userId: String): Result<UserProfile?>
    suspend fun saveUserProfile(profile: UserProfile): Result<UserProfile>
    suspend fun saveDriverVehicle(userId: String, vehicleInfo: VehicleInfo): Result<UserProfile>
    fun logout()
}
