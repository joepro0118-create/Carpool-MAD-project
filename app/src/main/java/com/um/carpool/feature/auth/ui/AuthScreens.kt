package com.um.carpool.feature.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.um.carpool.feature.auth.domain.model.CampusCatalog
import com.um.carpool.feature.auth.domain.model.UserProfile
import com.um.carpool.feature.auth.domain.model.VehicleInfo
import org.koin.androidx.compose.koinViewModel

@Composable
fun AuthFlowHost(
    viewModel: AuthViewModel = koinViewModel(),
    onAuthCompleted: ((UserProfile?) -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.currentStep, uiState.userProfile) {
        if (uiState.currentStep == AuthScreenStep.AUTHENTICATED && onAuthCompleted != null) {
            onAuthCompleted(uiState.userProfile)
        }
    }

    when (uiState.currentStep) {
        AuthScreenStep.LOGIN -> LoginScreen(
            state = uiState,
            onEmailChanged = viewModel::onEmailChanged,
            onPasswordChanged = viewModel::onPasswordChanged,
            onLoginClicked = viewModel::submitLogin,
            onNavigateToRegister = viewModel::navigateToRegister
        )

        AuthScreenStep.REGISTER -> RegisterScreen(
            state = uiState,
            onEmailChanged = viewModel::onEmailChanged,
            onPasswordChanged = viewModel::onPasswordChanged,
            onConfirmPasswordChanged = viewModel::onConfirmPasswordChanged,
            onRegisterClicked = viewModel::submitRegistration,
            onNavigateToLogin = viewModel::navigateToLogin
        )

        AuthScreenStep.PROFILE_SETUP -> ProfileSetupScreen(
            state = uiState,
            onFullNameChanged = viewModel::onFullNameChanged,
            onMatricIdChanged = viewModel::onMatricIdChanged,
            onFacultySelected = viewModel::onFacultySelected,
            onResidentialZoneSelected = viewModel::onResidentialZoneSelected,
            onGenderPreferenceSelected = viewModel::onGenderPreferenceSelected,
            onContinueAsRider = { viewModel.submitProfileSetup(wantToRegisterVehicle = false) },
            onContinueToDriverRegistration = { viewModel.submitProfileSetup(wantToRegisterVehicle = true) },
            onSignOut = viewModel::logout
        )

        AuthScreenStep.DRIVER_REGISTRATION -> DriverRegistrationScreen(
            state = uiState,
            onVehicleModelChanged = viewModel::onVehicleModelChanged,
            onVehicleColorChanged = viewModel::onVehicleColorChanged,
            onLicensePlateChanged = viewModel::onLicensePlateChanged,
            onSeatCapacitySelected = viewModel::onSeatCapacitySelected,
            onSubmitVehicle = viewModel::submitDriverRegistration,
            onSkipForNow = viewModel::skipDriverRegistration
        )

        AuthScreenStep.AUTHENTICATED -> AuthenticatedProfileSummaryScreen(
            profile = uiState.userProfile,
            emailFallback = uiState.email,
            onRegisterOrUpdateVehicle = viewModel::openDriverRegistration,
            onLogout = viewModel::logout
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    state: AuthUiState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onLoginClicked: () -> Unit,
    onNavigateToRegister: () -> Unit,
    modifier: Modifier = Modifier
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("UM-Ride Sign In", fontWeight = FontWeight.SemiBold) }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.DirectionsCar,
                contentDescription = "UM-Ride Logo",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Verified Campus Carpooling",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Sign in exclusively with your Universiti Malaya Siswa account.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            state.generalError?.let { errorMsg ->
                InlineAlertBanner(message = errorMsg, isError = true)
                Spacer(modifier = Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = state.email,
                onValueChange = onEmailChanged,
                label = { Text("UM Siswa Email") },
                placeholder = { Text("matric@siswa.um.edu.my") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Email, contentDescription = null)
                },
                singleLine = true,
                isError = state.emailError != null,
                supportingText = {
                    val errorText = state.emailError
                    if (errorText != null) {
                        Text(
                            text = errorText,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.testTag("login_email_error")
                        )
                    } else {
                        Text("Must end with @siswa.um.edu.my")
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_email_input")
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.password,
                onValueChange = onPasswordChanged,
                label = { Text("Password") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                singleLine = true,
                isError = state.passwordError != null,
                supportingText = state.passwordError?.let { err ->
                    { Text(text = err, color = MaterialTheme.colorScheme.error) }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_password_input")
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onLoginClicked,
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("login_submit_button")
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Sign In")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = onNavigateToRegister,
                enabled = !state.isLoading
            ) {
                Text("New to UM-Ride? Register with @siswa.um.edu.my")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    state: AuthUiState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onRegisterClicked: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Create UM-Ride Account", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateToLogin) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to sign in"
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.VerifiedUser,
                contentDescription = "Verified Registration",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Campus Trust Verification",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Only verified @siswa.um.edu.my email addresses can register.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            state.generalError?.let { errorMsg ->
                InlineAlertBanner(message = errorMsg, isError = true)
                Spacer(modifier = Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = state.email,
                onValueChange = onEmailChanged,
                label = { Text("UM Siswa Email") },
                placeholder = { Text("u2100000@siswa.um.edu.my") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Email, contentDescription = null)
                },
                singleLine = true,
                isError = state.emailError != null,
                supportingText = {
                    val errorText = state.emailError
                    if (errorText != null) {
                        Text(
                            text = errorText,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.testTag("register_email_error")
                        )
                    } else {
                        Text("Strict domain check: @siswa.um.edu.my")
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_email_input")
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.password,
                onValueChange = onPasswordChanged,
                label = { Text("Password") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                singleLine = true,
                isError = state.passwordError != null,
                supportingText = {
                    val err = state.passwordError
                    if (err != null) {
                        Text(text = err, color = MaterialTheme.colorScheme.error)
                    } else {
                        Text("Minimum 6 characters")
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.confirmPassword,
                onValueChange = onConfirmPasswordChanged,
                label = { Text("Confirm Password") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                },
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                singleLine = true,
                isError = state.confirmPasswordError != null,
                supportingText = state.confirmPasswordError?.let { err ->
                    { Text(text = err, color = MaterialTheme.colorScheme.error) }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onRegisterClicked,
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("register_submit_button")
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Create Account")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = onNavigateToLogin,
                enabled = !state.isLoading
            ) {
                Text("Already have an account? Sign In")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileSetupScreen(
    state: AuthUiState,
    onFullNameChanged: (String) -> Unit,
    onMatricIdChanged: (String) -> Unit,
    onFacultySelected: (String) -> Unit,
    onResidentialZoneSelected: (String) -> Unit,
    onGenderPreferenceSelected: (String) -> Unit,
    onContinueAsRider: () -> Unit,
    onContinueToDriverRegistration: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Campus Commute Profile", fontWeight = FontWeight.SemiBold) },
                actions = {
                    IconButton(onClick = onSignOut) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Sign out"
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
                .imePadding()
        ) {
            state.statusBanner?.let { info ->
                InlineAlertBanner(message = info, isError = false)
                Spacer(modifier = Modifier.height(12.dp))
            }

            state.generalError?.let { err ->
                InlineAlertBanner(message = err, isError = true)
                Spacer(modifier = Modifier.height(12.dp))
            }

            Text(
                text = "Complete your UM commuter profile so we can match rides along your faculty schedule and residential corridor.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = state.fullName,
                onValueChange = onFullNameChanged,
                label = { Text("Full Name") },
                placeholder = { Text("Ahmad Daniel") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null)
                },
                singleLine = true,
                isError = state.fullNameError != null,
                supportingText = state.fullNameError?.let { err ->
                    { Text(text = err, color = MaterialTheme.colorScheme.error) }
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = state.matricId,
                onValueChange = onMatricIdChanged,
                label = { Text("Matric ID (Optional)") },
                placeholder = { Text("U2101234") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Badge, contentDescription = null)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            SelectionDropdownField(
                label = "Primary Faculty",
                selectedValue = state.selectedFaculty,
                options = CampusCatalog.FACULTIES,
                leadingIcon = {
                    Icon(imageVector = Icons.Default.School, contentDescription = null)
                },
                errorMessage = state.facultyError,
                onOptionSelected = onFacultySelected
            )

            Spacer(modifier = Modifier.height(12.dp))

            SelectionDropdownField(
                label = "Residential Zone",
                selectedValue = state.selectedResidentialZone,
                options = CampusCatalog.RESIDENTIAL_ZONES,
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Home, contentDescription = null)
                },
                errorMessage = state.residentialZoneError,
                onOptionSelected = onResidentialZoneSelected
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Ride Gender Preference",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CampusCatalog.GENDER_PREFERENCES.forEach { option ->
                    FilterChip(
                        selected = state.selectedGenderPreference == option,
                        onClick = { onGenderPreferenceSelected(option) },
                        label = { Text(option) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = onContinueToDriverRegistration,
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(imageVector = Icons.Default.DirectionsCar, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save & Register Vehicle (Driver)")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onContinueAsRider,
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Continue as Passenger Only")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DriverRegistrationScreen(
    state: AuthUiState,
    onVehicleModelChanged: (String) -> Unit,
    onVehicleColorChanged: (String) -> Unit,
    onLicensePlateChanged: (String) -> Unit,
    onSeatCapacitySelected: (Int) -> Unit,
    onSubmitVehicle: () -> Unit,
    onSkipForNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Driver Vehicle Setup", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onSkipForNow) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Skip vehicle registration"
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
                .imePadding()
        ) {
            state.generalError?.let { err ->
                InlineAlertBanner(message = err, isError = true)
                Spacer(modifier = Modifier.height(12.dp))
            }

            Text(
                text = "Register your vehicle details so matched UM passengers can verify your car at faculty pickup points.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = state.vehicleModel,
                onValueChange = onVehicleModelChanged,
                label = { Text("Vehicle Model") },
                placeholder = { Text("Perodua Myvi 1.5 AV") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.DirectionsCar, contentDescription = null)
                },
                singleLine = true,
                isError = state.vehicleModelError != null,
                supportingText = state.vehicleModelError?.let { err ->
                    { Text(text = err, color = MaterialTheme.colorScheme.error) }
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = state.licensePlate,
                onValueChange = onLicensePlateChanged,
                label = { Text("License Plate Number") },
                placeholder = { Text("VAB 1234") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Pin, contentDescription = null)
                },
                singleLine = true,
                isError = state.licensePlateError != null,
                supportingText = state.licensePlateError?.let { err ->
                    { Text(text = err, color = MaterialTheme.colorScheme.error) }
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = state.vehicleColor,
                onValueChange = onVehicleColorChanged,
                label = { Text("Vehicle Color (Optional)") },
                placeholder = { Text("Granite Grey") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Palette, contentDescription = null)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.EventSeat,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Available Passenger Seat Capacity (1 - 6)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CampusCatalog.SEAT_CAPACITIES.forEach { count ->
                    FilterChip(
                        selected = state.seatCapacity == count,
                        onClick = { onSeatCapacitySelected(count) },
                        label = { Text("$count ${if (count == 1) "Seat" else "Seats"}") }
                    )
                }
            }
            state.seatCapacityError?.let { seatErr ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = seatErr,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = onSubmitVehicle,
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Complete Driver Registration")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = onSkipForNow,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Skip for now (remain Passenger)")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthenticatedProfileSummaryScreen(
    profile: UserProfile?,
    emailFallback: String,
    onRegisterOrUpdateVehicle: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("UM-Ride Profile", fontWeight = FontWeight.SemiBold) },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Sign out"
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = profile?.fullName?.ifBlank { "UM Student" } ?: "UM Student",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = profile?.siswaEmail?.ifBlank { emailFallback } ?: emailFallback,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text("Role: ${profile?.role ?: UserProfile.ROLE_RIDER}")
                    Text("Faculty: ${profile?.faculty ?: "-"}")
                    Text("Residential Zone: ${profile?.residentialZone ?: "-"}")
                    Text("Gender Preference: ${profile?.genderPreference ?: "-"}")

                    profile?.vehicleInfo?.let { vehicle ->
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Text(
                            text = "Registered Vehicle",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text("Model: ${vehicle.model}")
                        Text("Plate: ${vehicle.licensePlate}")
                        if (vehicle.color.isNotBlank()) {
                            Text("Color: ${vehicle.color}")
                        }
                        Text("Capacity: ${vehicle.seatCapacity} passenger seats")
                    }
                }
            }

            OutlinedButton(
                onClick = onRegisterOrUpdateVehicle,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.DirectionsCar, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (profile?.vehicleInfo == null) {
                        "Register as Driver"
                    } else {
                        "Update Vehicle Details"
                    }
                )
            }

            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectionDropdownField(
    label: String,
    selectedValue: String,
    options: List<String>,
    leadingIcon: @Composable () -> Unit,
    errorMessage: String?,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            leadingIcon = leadingIcon,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            isError = errorMessage != null,
            supportingText = errorMessage?.let { err ->
                { Text(text = err, color = MaterialTheme.colorScheme.error) }
            },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        onOptionSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun InlineAlertBanner(
    message: String,
    isError: Boolean
) {
    val containerColor = if (isError) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }
    val contentColor = if (isError) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isError) Icons.Default.ErrorOutline else Icons.Default.CheckCircle,
                contentDescription = null,
                tint = contentColor
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = contentColor
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    MaterialTheme {
        LoginScreen(
            state = AuthUiState(
                email = "student@gmail.com",
                emailError = "Only verified @siswa.um.edu.my emails are accepted."
            ),
            onEmailChanged = {},
            onPasswordChanged = {},
            onLoginClicked = {},
            onNavigateToRegister = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileSetupScreenPreview() {
    MaterialTheme {
        ProfileSetupScreen(
            state = AuthUiState(
                fullName = "Joe Tan",
                matricId = "U2105541",
                selectedFaculty = "FSKTM",
                selectedResidentialZone = "KK12"
            ),
            onFullNameChanged = {},
            onMatricIdChanged = {},
            onFacultySelected = {},
            onResidentialZoneSelected = {},
            onGenderPreferenceSelected = {},
            onContinueAsRider = {},
            onContinueToDriverRegistration = {},
            onSignOut = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DriverRegistrationScreenPreview() {
    MaterialTheme {
        DriverRegistrationScreen(
            state = AuthUiState(
                vehicleModel = "Perodua Myvi 1.5",
                licensePlate = "VAB 1234",
                vehicleColor = "Silver",
                seatCapacity = 4
            ),
            onVehicleModelChanged = {},
            onVehicleColorChanged = {},
            onLicensePlateChanged = {},
            onSeatCapacitySelected = {},
            onSubmitVehicle = {},
            onSkipForNow = {}
        )
    }
}
