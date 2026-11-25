package com.um.carpool.feature.auth.domain.validation

sealed interface ValidationResult {
    data object Valid : ValidationResult
    data class Invalid(val message: String) : ValidationResult

    val errorMessageOrNull: String?
        get() = (this as? Invalid)?.message
}

object AuthValidator {
    const val SISWA_DOMAIN = "@siswa.um.edu.my"
    private val LOCAL_PART_REGEX = Regex("^[a-z0-9._%+-]+$")
    private val PLATE_REGEX = Regex("^[A-Z0-9 ]{2,12}$")

    fun normalizeEmail(rawEmail: String): String = rawEmail.trim().lowercase()

    /**
     * Enforces strict Universiti Malaya student domain validation:
     * email must have a valid local username and end strictly with "@siswa.um.edu.my".
     */
    fun validateSiswaEmail(rawEmail: String): ValidationResult {
        val trimmed = rawEmail.trim()
        if (trimmed.isEmpty()) {
            return ValidationResult.Invalid("UM Siswa email is required.")
        }

        val normalized = trimmed.lowercase()
        val atIndex = normalized.indexOf('@')
        if (atIndex <= 0) {
            return ValidationResult.Invalid("Enter a valid email ending with $SISWA_DOMAIN.")
        }

        val localPart = normalized.substring(0, atIndex)
        val domainPart = normalized.substring(atIndex)

        if (domainPart != SISWA_DOMAIN) {
            return ValidationResult.Invalid("Only verified $SISWA_DOMAIN emails are accepted.")
        }

        if (!LOCAL_PART_REGEX.matches(localPart) || localPart.startsWith('.') || localPart.endsWith('.') || localPart.contains("..")) {
            return ValidationResult.Invalid("Invalid email username before $SISWA_DOMAIN.")
        }

        return ValidationResult.Valid
    }

    fun validatePassword(password: String): ValidationResult {
        if (password.isBlank()) {
            return ValidationResult.Invalid("Password is required.")
        }
        if (password.length < 6) {
            return ValidationResult.Invalid("Password must be at least 6 characters.")
        }
        return ValidationResult.Valid
    }

    fun validateConfirmPassword(password: String, confirmPassword: String): ValidationResult {
        if (confirmPassword.isBlank()) {
            return ValidationResult.Invalid("Please confirm your password.")
        }
        if (password != confirmPassword) {
            return ValidationResult.Invalid("Passwords do not match.")
        }
        return ValidationResult.Valid
    }

    fun validateFullName(fullName: String): ValidationResult {
        val trimmed = fullName.trim()
        if (trimmed.isEmpty()) {
            return ValidationResult.Invalid("Full name is required.")
        }
        if (trimmed.length < 2) {
            return ValidationResult.Invalid("Full name must be at least 2 characters.")
        }
        return ValidationResult.Valid
    }

    fun validateFaculty(faculty: String, allowedFaculties: List<String>): ValidationResult {
        if (faculty.isBlank() || faculty !in allowedFaculties) {
            return ValidationResult.Invalid("Please select your UM faculty.")
        }
        return ValidationResult.Valid
    }

    fun validateResidentialZone(zone: String, allowedZones: List<String>): ValidationResult {
        if (zone.isBlank() || zone !in allowedZones) {
            return ValidationResult.Invalid("Please select your residential zone.")
        }
        return ValidationResult.Valid
    }

    fun validateVehicleModel(model: String): ValidationResult {
        if (model.trim().length < 2) {
            return ValidationResult.Invalid("Enter a valid vehicle model (e.g., Perodua Myvi).")
        }
        return ValidationResult.Valid
    }

    fun validateLicensePlate(plate: String): ValidationResult {
        val normalized = plate.trim().uppercase()
        if (normalized.isEmpty()) {
            return ValidationResult.Invalid("License plate number is required.")
        }
        if (!PLATE_REGEX.matches(normalized) || normalized.none { it.isLetter() } || normalized.none { it.isDigit() }) {
            return ValidationResult.Invalid("Enter a valid plate number (e.g., VAB 1234).")
        }
        return ValidationResult.Valid
    }

    fun validateSeatCapacity(seats: Int): ValidationResult {
        if (seats !in 1..6) {
            return ValidationResult.Invalid("Available seat capacity must be between 1 and 6.")
        }
        return ValidationResult.Valid
    }
}
