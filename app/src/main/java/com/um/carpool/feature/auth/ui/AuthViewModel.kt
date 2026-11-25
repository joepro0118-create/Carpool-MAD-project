package com.um.carpool.feature.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.um.carpool.feature.auth.domain.model.CampusCatalog
import com.um.carpool.feature.auth.domain.model.UserProfile
import com.um.carpool.feature.auth.domain.model.VehicleInfo
import com.um.carpool.feature.auth.domain.repository.AuthRepository
import com.um.carpool.feature.auth.domain.validation.AuthValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AuthScreenStep {
    LOGIN,
    REGISTER,
    PROFILE_SETUP,
    DRIVER_REGISTRATION,
    AUTHENTICATED
}

data class AuthUiState(
    val currentStep: AuthScreenStep = AuthScreenStep.LOGIN,
    val isLoading: Boolean = false,
    val generalError: String? = null,
    val statusBanner: String? = null,

    // Credentials form
    val email: String = "",
    val emailError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val confirmPassword: String = "",
    val confirmPasswordError: String? = null,

    // Profile setup form
    val authenticatedUid: String = "",
    val fullName: String = "",
    val fullNameError: String? = null,
    val matricId: String = "",
    val selectedFaculty: String = "",
    val facultyError: String? = null,
    val selectedResidentialZone: String = "",
    val residentialZoneError: String? = null,
    val selectedGenderPreference: String = CampusCatalog.GENDER_PREFERENCES.first(),

    // Driver vehicle registration form
    val vehicleModel: String = "",
    val vehicleModelError: String? = null,
    val vehicleColor: String = "",
    val licensePlate: String = "",
    val licensePlateError: String? = null,
    val seatCapacity: Int = 4,
    val seatCapacityError: String? = null,

    // Loaded / saved profile
    val userProfile: UserProfile? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        restoreSessionIfAvailable()
    }

    private fun restoreSessionIfAvailable() {
        val currentUid = authRepository.getCurrentUserId() ?: return
        val currentEmail = authRepository.getCurrentUserEmail().orEmpty()
        _uiState.update {
            it.copy(
                isLoading = true,
                authenticatedUid = currentUid,
                email = currentEmail
            )
        }
        viewModelScope.launch {
            authRepository.getUserProfile(currentUid)
                .onSuccess { existingProfile ->
                    if (existingProfile != null && existingProfile.isProfileComplete) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                userProfile = existingProfile,
                                fullName = existingProfile.fullName,
                                matricId = existingProfile.matricId,
                                selectedFaculty = existingProfile.faculty,
                                selectedResidentialZone = existingProfile.residentialZone,
                                selectedGenderPreference = existingProfile.genderPreference,
                                currentStep = AuthScreenStep.AUTHENTICATED
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                currentStep = AuthScreenStep.PROFILE_SETUP
                            )
                        }
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            currentStep = AuthScreenStep.PROFILE_SETUP
                        )
                    }
                }
        }
    }

    fun onEmailChanged(newEmail: String) {
        val inlineError = if (newEmail.isBlank()) {
            null
        } else {
            AuthValidator.validateSiswaEmail(newEmail).errorMessageOrNull
        }
        _uiState.update {
            it.copy(
                email = newEmail,
                emailError = inlineError,
                generalError = null
            )
        }
    }

    fun onPasswordChanged(newPassword: String) {
        _uiState.update {
            it.copy(
                password = newPassword,
                passwordError = null,
                generalError = null
            )
        }
    }

    fun onConfirmPasswordChanged(newConfirmPassword: String) {
        _uiState.update {
            it.copy(
                confirmPassword = newConfirmPassword,
                confirmPasswordError = null,
                generalError = null
            )
        }
    }

    fun navigateToRegister() {
        _uiState.update {
            it.copy(
                currentStep = AuthScreenStep.REGISTER,
                generalError = null,
                statusBanner = null,
                passwordError = null,
                confirmPasswordError = null
            )
        }
    }

    fun navigateToLogin() {
        _uiState.update {
            it.copy(
                currentStep = AuthScreenStep.LOGIN,
                generalError = null,
                statusBanner = null,
                passwordError = null,
                confirmPasswordError = null
            )
        }
    }

    fun submitLogin() {
        val snapshot = _uiState.value
        val emailCheck = AuthValidator.validateSiswaEmail(snapshot.email)
        val passwordCheck = AuthValidator.validatePassword(snapshot.password)

        val emailErr = emailCheck.errorMessageOrNull
        val passwordErr = passwordCheck.errorMessageOrNull

        if (emailErr != null || passwordErr != null) {
            _uiState.update {
                it.copy(
                    emailError = emailErr,
                    passwordError = passwordErr
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, generalError = null, statusBanner = null) }
        viewModelScope.launch {
            authRepository.login(snapshot.email, snapshot.password)
                .onSuccess { profile ->
                    val uid = profile?.uid ?: authRepository.getCurrentUserId().orEmpty()
                    if (profile != null && profile.isProfileComplete) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                authenticatedUid = uid,
                                userProfile = profile,
                                fullName = profile.fullName,
                                matricId = profile.matricId,
                                selectedFaculty = profile.faculty,
                                selectedResidentialZone = profile.residentialZone,
                                selectedGenderPreference = profile.genderPreference,
                                currentStep = AuthScreenStep.AUTHENTICATED
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                authenticatedUid = uid,
                                currentStep = AuthScreenStep.PROFILE_SETUP
                            )
                        }
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            generalError = throwable.message ?: "Unable to sign in. Please verify your credentials."
                        )
                    }
                }
        }
    }

    fun submitRegistration() {
        val snapshot = _uiState.value
        val emailErr = AuthValidator.validateSiswaEmail(snapshot.email).errorMessageOrNull
        val passwordErr = AuthValidator.validatePassword(snapshot.password).errorMessageOrNull
        val confirmErr = AuthValidator.validateConfirmPassword(
            snapshot.password,
            snapshot.confirmPassword
        ).errorMessageOrNull

        if (emailErr != null || passwordErr != null || confirmErr != null) {
            _uiState.update {
                it.copy(
                    emailError = emailErr,
                    passwordError = passwordErr,
                    confirmPasswordError = confirmErr
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, generalError = null, statusBanner = null) }
        viewModelScope.launch {
            authRepository.register(snapshot.email, snapshot.password)
                .onSuccess { uid ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            authenticatedUid = uid,
                            statusBanner = "Verification link sent to ${AuthValidator.normalizeEmail(snapshot.email)}. Complete your UM profile below.",
                            currentStep = AuthScreenStep.PROFILE_SETUP
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            generalError = throwable.message ?: "Registration failed. Please try again."
                        )
                    }
                }
        }
    }

    fun onFullNameChanged(value: String) {
        _uiState.update { it.copy(fullName = value, fullNameError = null, generalError = null) }
    }

    fun onMatricIdChanged(value: String) {
        _uiState.update { it.copy(matricId = value, generalError = null) }
    }

    fun onFacultySelected(faculty: String) {
        _uiState.update { it.copy(selectedFaculty = faculty, facultyError = null, generalError = null) }
    }

    fun onResidentialZoneSelected(zone: String) {
        _uiState.update {
            it.copy(
                selectedResidentialZone = zone,
                residentialZoneError = null,
                generalError = null
            )
        }
    }

    fun onGenderPreferenceSelected(preference: String) {
        _uiState.update { it.copy(selectedGenderPreference = preference, generalError = null) }
    }

    fun submitProfileSetup(wantToRegisterVehicle: Boolean) {
        val snapshot = _uiState.value
        val nameErr = AuthValidator.validateFullName(snapshot.fullName).errorMessageOrNull
        val facultyErr = AuthValidator.validateFaculty(
            snapshot.selectedFaculty,
            CampusCatalog.FACULTIES
        ).errorMessageOrNull
        val zoneErr = AuthValidator.validateResidentialZone(
            snapshot.selectedResidentialZone,
            CampusCatalog.RESIDENTIAL_ZONES
        ).errorMessageOrNull

        if (nameErr != null || facultyErr != null || zoneErr != null) {
            _uiState.update {
                it.copy(
                    fullNameError = nameErr,
                    facultyError = facultyErr,
                    residentialZoneError = zoneErr
                )
            }
            return
        }

        val uid = snapshot.authenticatedUid.ifBlank {
            authRepository.getCurrentUserId().orEmpty()
        }
        val profile = UserProfile(
            uid = uid,
            fullName = snapshot.fullName.trim(),
            siswaEmail = AuthValidator.normalizeEmail(snapshot.email),
            matricId = snapshot.matricId.trim().uppercase(),
            faculty = snapshot.selectedFaculty,
            residentialZone = snapshot.selectedResidentialZone,
            genderPreference = snapshot.selectedGenderPreference,
            role = if (wantToRegisterVehicle) UserProfile.ROLE_DRIVER else UserProfile.ROLE_RIDER,
            vehicleInfo = snapshot.userProfile?.vehicleInfo
        )

        _uiState.update { it.copy(isLoading = true, generalError = null) }
        viewModelScope.launch {
            authRepository.saveUserProfile(profile)
                .onSuccess { savedProfile ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            userProfile = savedProfile,
                            statusBanner = null,
                            currentStep = if (wantToRegisterVehicle) {
                                AuthScreenStep.DRIVER_REGISTRATION
                            } else {
                                AuthScreenStep.AUTHENTICATED
                            }
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            generalError = throwable.message ?: "Failed to save profile to Firestore."
                        )
                    }
                }
        }
    }

    fun openDriverRegistration() {
        val existingVehicle = _uiState.value.userProfile?.vehicleInfo
        _uiState.update {
            it.copy(
                vehicleModel = existingVehicle?.model ?: it.vehicleModel,
                vehicleColor = existingVehicle?.color ?: it.vehicleColor,
                licensePlate = existingVehicle?.licensePlate ?: it.licensePlate,
                seatCapacity = existingVehicle?.seatCapacity ?: it.seatCapacity,
                generalError = null,
                currentStep = AuthScreenStep.DRIVER_REGISTRATION
            )
        }
    }

    fun skipDriverRegistration() {
        _uiState.update {
            it.copy(
                generalError = null,
                currentStep = AuthScreenStep.AUTHENTICATED
            )
        }
    }

    fun onVehicleModelChanged(model: String) {
        _uiState.update { it.copy(vehicleModel = model, vehicleModelError = null, generalError = null) }
    }

    fun onVehicleColorChanged(color: String) {
        _uiState.update { it.copy(vehicleColor = color, generalError = null) }
    }

    fun onLicensePlateChanged(plate: String) {
        _uiState.update {
            it.copy(
                licensePlate = plate.uppercase(),
                licensePlateError = null,
                generalError = null
            )
        }
    }

    fun onSeatCapacitySelected(seats: Int) {
        _uiState.update { it.copy(seatCapacity = seats, seatCapacityError = null, generalError = null) }
    }

    fun submitDriverRegistration() {
        val snapshot = _uiState.value
        val modelErr = AuthValidator.validateVehicleModel(snapshot.vehicleModel).errorMessageOrNull
        val plateErr = AuthValidator.validateLicensePlate(snapshot.licensePlate).errorMessageOrNull
        val seatErr = AuthValidator.validateSeatCapacity(snapshot.seatCapacity).errorMessageOrNull

        if (modelErr != null || plateErr != null || seatErr != null) {
            _uiState.update {
                it.copy(
                    vehicleModelError = modelErr,
                    licensePlateError = plateErr,
                    seatCapacityError = seatErr
                )
            }
            return
        }

        val uid = snapshot.authenticatedUid.ifBlank {
            authRepository.getCurrentUserId().orEmpty()
        }
        val vehicleInfo = VehicleInfo(
            model = snapshot.vehicleModel.trim(),
            licensePlate = snapshot.licensePlate.trim().uppercase(),
            color = snapshot.vehicleColor.trim(),
            seatCapacity = snapshot.seatCapacity
        )

        _uiState.update { it.copy(isLoading = true, generalError = null) }
        viewModelScope.launch {
            authRepository.saveDriverVehicle(uid, vehicleInfo)
                .onSuccess { updatedProfile ->
                    val mergedProfile = snapshot.userProfile?.copy(
                        role = UserProfile.ROLE_DRIVER,
                        vehicleInfo = vehicleInfo
                    ) ?: updatedProfile
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            userProfile = mergedProfile,
                            currentStep = AuthScreenStep.AUTHENTICATED
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            generalError = throwable.message ?: "Failed to save vehicle registration."
                        )
                    }
                }
        }
    }

    fun logout() {
        authRepository.logout()
        _uiState.value = AuthUiState(currentStep = AuthScreenStep.LOGIN)
    }
}
