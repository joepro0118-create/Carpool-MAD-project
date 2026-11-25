package com.um.carpool.feature.auth.domain.model

object CampusCatalog {
    val FACULTIES: List<String> = listOf(
        "FSKTM",
        "Engineering",
        "Law",
        "Science",
        "Business"
    )

    val RESIDENTIAL_ZONES: List<String> = listOf(
        "KK1",
        "KK2",
        "KK3",
        "KK4",
        "KK5",
        "KK6",
        "KK7",
        "KK8",
        "KK9",
        "KK10",
        "KK11",
        "KK12",
        "Section 17",
        "Bangsar South"
    )

    val GENDER_PREFERENCES: List<String> = listOf(
        "No Preference",
        "Female Only",
        "Male Only"
    )

    val SEAT_CAPACITIES: List<Int> = (1..6).toList()
}

data class VehicleInfo(
    val model: String = "",
    val licensePlate: String = "",
    val color: String = "",
    val seatCapacity: Int = 4
)

data class UserProfile(
    val uid: String = "",
    val fullName: String = "",
    val siswaEmail: String = "",
    val matricId: String = "",
    val faculty: String = "",
    val residentialZone: String = "",
    val genderPreference: String = CampusCatalog.GENDER_PREFERENCES.first(),
    val role: String = ROLE_RIDER,
    val ratingAvg: Double = 5.0,
    val totalCo2SavedKg: Double = 0.0,
    val vehicleInfo: VehicleInfo? = null
) {
    val isProfileComplete: Boolean
        get() = fullName.isNotBlank() && faculty.isNotBlank() && residentialZone.isNotBlank()

    companion object {
        const val ROLE_RIDER = "RIDER"
        const val ROLE_DRIVER = "DRIVER"
    }
}
