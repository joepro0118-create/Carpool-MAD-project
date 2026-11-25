package com.um.carpool.feature.auth

import com.um.carpool.feature.auth.domain.model.UserProfile
import com.um.carpool.feature.auth.domain.model.VehicleInfo
import com.um.carpool.feature.auth.domain.repository.AuthRepository
import com.um.carpool.feature.auth.ui.AuthScreenStep
import com.um.carpool.feature.auth.ui.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `typing non-siswa email sets inline emailError immediately`() = runTest {
        val fakeRepo = FakeAuthRepository()
        val viewModel = AuthViewModel(fakeRepo)

        viewModel.onEmailChanged("user@yahoo.com")
        assertNotNull(viewModel.uiState.value.emailError)

        viewModel.onEmailChanged("u2100001@siswa.um.edu.my")
        assertNull(viewModel.uiState.value.emailError)
    }

    @Test
    fun `registration flow transitions through profile setup and driver registration`() = runTest {
        val fakeRepo = FakeAuthRepository()
        val viewModel = AuthViewModel(fakeRepo)

        viewModel.navigateToRegister()
        viewModel.onEmailChanged("u2105541@siswa.um.edu.my")
        viewModel.onPasswordChanged("securePass123")
        viewModel.onConfirmPasswordChanged("securePass123")
        viewModel.submitRegistration()

        assertEquals(AuthScreenStep.PROFILE_SETUP, viewModel.uiState.value.currentStep)

        viewModel.onFullNameChanged("Joe Tan")
        viewModel.onMatricIdChanged("u2105541")
        viewModel.onFacultySelected("FSKTM")
        viewModel.onResidentialZoneSelected("Section 17")
        viewModel.submitProfileSetup(wantToRegisterVehicle = true)

        assertEquals(AuthScreenStep.DRIVER_REGISTRATION, viewModel.uiState.value.currentStep)

        viewModel.onVehicleModelChanged("Perodua Myvi")
        viewModel.onLicensePlateChanged("vab 4321")
        viewModel.onSeatCapacitySelected(4)
        viewModel.submitDriverRegistration()

        assertEquals(AuthScreenStep.AUTHENTICATED, viewModel.uiState.value.currentStep)
        assertEquals(UserProfile.ROLE_DRIVER, viewModel.uiState.value.userProfile?.role)
        assertEquals("VAB 4321", viewModel.uiState.value.userProfile?.vehicleInfo?.licensePlate)
        assertEquals(4, viewModel.uiState.value.userProfile?.vehicleInfo?.seatCapacity)
    }

    private class FakeAuthRepository : AuthRepository {
        private var currentUid: String? = null
        private var currentEmail: String? = null
        private val profiles = mutableMapOf<String, UserProfile>()

        override fun getCurrentUserId(): String? = currentUid
        override fun getCurrentUserEmail(): String? = currentEmail

        override suspend fun login(email: String, password: String): Result<UserProfile?> {
            currentUid = "uid-123"
            currentEmail = email
            return Result.success(profiles["uid-123"])
        }

        override suspend fun register(email: String, password: String): Result<String> {
            currentUid = "uid-123"
            currentEmail = email
            return Result.success("uid-123")
        }

        override suspend fun getUserProfile(userId: String): Result<UserProfile?> =
            Result.success(profiles[userId])

        override suspend fun saveUserProfile(profile: UserProfile): Result<UserProfile> {
            profiles[profile.uid] = profile
            return Result.success(profile)
        }

        override suspend fun saveDriverVehicle(
            userId: String,
            vehicleInfo: VehicleInfo
        ): Result<UserProfile> {
            val existing = profiles[userId] ?: UserProfile(uid = userId)
            val updated = existing.copy(
                role = UserProfile.ROLE_DRIVER,
                vehicleInfo = vehicleInfo
            )
            profiles[userId] = updated
            return Result.success(updated)
        }

        override fun logout() {
            currentUid = null
            currentEmail = null
        }
    }
}
