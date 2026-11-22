# UM-Ride: 6-Role Team Task Breakdown & AI Prompts
**Course:** WIA2007 Mobile Application Development (2026/2027 Semester 1)  
**Target Platform:** Native Android (Kotlin, Jetpack Compose, Material 3, Firebase)  

---

## Shared Base Context (Include in all prompts)
Every team member should paste this base context first before pasting their individual role prompt so all 6 modules share consistent architecture, styling, and package boundaries.

```text
[SYSTEM BASE CONTEXT]
Project: UM-Ride (WIA2007 Mobile Application Development Group Project)
Target Platform: Native Android (Min SDK 26, Target SDK 34)
Language: Kotlin 1.9+ | UI Framework: Jetpack Compose with Material 3
Architecture: Clean Architecture + MVVM + Repository Pattern
Backend: Firebase Auth, Cloud Firestore, Google Maps SDK, Google Gemini API
Design Constraint: Clean UI, Material 3 Icons only. NO system emojis.
Domain Constraint: Universiti Malaya (@siswa.um.edu.my) carpooling.
```

---

## Role 1: Project Manager & Auth/Profile Lead
### Responsibilities
* Setup root repository configuration, Gradle dependencies, and Base Hilt/Koin DI setup.
* Implement domain-validated Firebase Authentication screens (`LoginScreen`, `RegisterScreen`).
* Implement mandatory user onboarding profile setup and vehicle details registration.
* Coordinate Trello sprints, GitHub PR reviews, and compile the final report & Appendix 2(b) AI Usage Log.

### Production Prompt
```text
[SYSTEM BASE CONTEXT]

Role: Project Manager & Auth/Profile Lead
Package Path: com.um.carpool.feature.auth

Task Overview:
Build the core authentication and profile management module for UM-Ride using Jetpack Compose and Firebase Auth.

Technical Requirements:
1. Implement AuthViewModel handling Login, Registration, and Profile Setup states using StateFlow.
2. Build LoginScreen & RegisterScreen enforcing strict domain validation: text must end with "@siswa.um.edu.my". Show clear Inline Error text on failure.
3. Build ProfileSetupScreen collecting: Full Name, Faculty dropdown (FSKTM, Engineering, Law, Science, Business), Residential Zone dropdown (KK1-KK12, Section 17, Bangsar South), and Gender Preference.
4. Build DriverRegistrationScreen collecting Vehicle Model, License Plate, and Seat Capacity (1-6). Save all data to Firestore path `/users/{userId}` using Clean Repository pattern.
5. Provide standard Material 3 components with out-of-the-box UI styling. Ensure no native emojis are used; use vector icons (e.g., Icons.Default.Email) instead.

Output Requirements:
Provide production-ready Kotlin code for AuthRepositoryImpl.kt, AuthViewModel.kt, and AuthScreens.kt. Include explicit unit test cases for domain validation logic.
```

---

## Role 2: Software Architect & Core Ride-Feed Developer
### Responsibilities
* Architect base infrastructure (`Resource<T>`, UI state wrappers, shared Compose components, `NavHost`).
* Build the main Ride Feed screen displaying upcoming departure trips.
* Build advanced filter UI (Faculty pickup, Time window, Destination corridor).

### Production Prompt
```text
[SYSTEM BASE CONTEXT]

Role: Software Architect & Core Ride-Feed Developer
Package Path: com.um.carpool.core.* & com.um.carpool.feature.rides.feed

Task Overview:
Architect core state management structures and build the high-performance Main Ride Feed UI for UM-Ride.

Technical Requirements:
1. Define universal network state wrapper `sealed class Resource<out T>` (Success, Error, Loading).
2. Design top-level Scaffold with Material 3 NavigationBar and TopAppBar.
3. Implement RideFeedViewModel querying `/rides` collection from Firestore, returning a reactive `StateFlow<Resource<List<Ride>>>`.
4. Build RideFeedScreen featuring:
   - Dynamic Filter Bar: Dropdowns for Pickup Faculty, Time Slot (12 PM, 4 PM, 5 PM, 6 PM), and Destination Corridor.
   - RideCard Component: Display Driver Name, Verification Badge, Origin Faculty, Destination, Departure Time, Seats Left Badge, and Estimated Split Cost.
5. Optimise UI loading speed: UI must update within < 2.0s. Handle Empty and Error UI states gracefully using Material 3 icons.

Output Requirements:
Provide fully typed Kotlin code for Resource.kt, RideFeedViewModel.kt, and RideFeedScreen.kt with Compose preview setups.
```

---

## Role 3: Backend & Concurrency Developer — Ride Posting & Atomic Booking
### Responsibilities
* Implement driver Ride Posting flow with date-time pickers and Firestore write logic.
* Implement atomic passenger seat reservation using Firestore `runTransaction`.
* Prevent overbooking race conditions when multiple users book concurrently.

### Production Prompt
```text
[SYSTEM BASE CONTEXT]

Role: Backend & Concurrency Developer — Ride Posting & Atomic Booking
Package Path: com.um.carpool.feature.rides.booking

Task Overview:
Develop the transactional backend logic and UI flows for posting rides and reserving seats atomically without overbooking.

Technical Requirements:
1. Implement PostRideScreen allowing drivers to select pickup faculty, destination, departure time, and total seats. Save entry to Firestore `/rides`.
2. Implement atomic reservation in `BookingRepositoryImpl.reserveSeat(rideId: String, passengerId: String)` using `FirebaseFirestore.getInstance().runTransaction`.
   - Transaction logic: Read `/rides/{rideId}` document. Check if `availableSeats > 0`. If true, decrement `availableSeats` by 1, update status to "FULL" if seats become 0, and create a document under `/rides/{rideId}/bookings/{bookingId}`. If `availableSeats == 0`, throw Exception("Ride Full").
3. Implement PassengerBookingDialog displaying booking confirmation, remaining seats, and explicit dynamic progress indicators during transaction execution.
4. Ensure complete handling of concurrency exceptions with clear UI feedback.

Output Requirements:
Provide complete Kotlin code for BookingRepositoryImpl.kt, BookingViewModel.kt, and PostRideScreen.kt, including Firestore transaction verification logic.
```

---

## Role 4: Maps, Cost-Splitting & Safety Developer
### Responsibilities
* Integrate Google Maps SDK and Distance Matrix API for automatic fare calculation.
* Build automated dynamic fuel/toll splitting logic per passenger.
* Implement active trip Emergency SOS modal and live WhatsApp/SMS location sharing.

### Production Prompt
```text
[SYSTEM BASE CONTEXT]

Role: Maps, Cost-Splitting & Safety Developer
Package Path: com.um.carpool.feature.cost_safety

Task Overview:
Build the distance-based fuel splitting engine, Google Maps route viewer, and in-ride safety mechanisms.

Technical Requirements:
1. Implement CostCalculator utility: Calculate total fare using `Distance_km * 0.25 MYR`. Calculate split cost using `(Total Fare + Tolls) / (Driver + Passengers)`. Recalculate dynamically as passenger count changes.
2. Build ActiveRideMapScreen integrating Google Maps SDK displaying markers for Faculty Pickup Point and Destination drop-off point with route polyline.
3. Build One-Tap Emergency SOS Modal: When triggered, fetch high-accuracy current location via FusedLocationProviderClient, format an emergency text with Google Maps link, launch SMS Intent to UM Security (`03-7967 7070`), and trigger direct phone call Intent.
4. Implement Live Trip Share Button: Generate a standardized text payload (Driver info, plate number, route, tracking link) and launch Android Chooser Intent for WhatsApp/SMS.

Output Requirements:
Provide Kotlin code for CostCalculator.kt, ActiveRideScreen.kt, and EmergencySOSManager.kt with Android permission handling for Location, Call, and SMS.
```

---

## Role 5: AI Integration & SDG 13 Analytics Developer
### Responsibilities
* Integrate Google Gemini API (`gemini-1.5-flash`) for natural language ride parsing with deterministic fallback.
* Implement SDG 13 Carbon Savings Engine (`Distance_km * 0.192 kg CO2`).
* Build personal sustainability dashboard and real-time Inter-Faculty Leaderboard.

### Production Prompt
```text
[SYSTEM BASE CONTEXT]

Role: AI Integration & SDG 13 Analytics Developer
Package Path: com.um.carpool.feature.ai_sdg

Task Overview:
Implement the Gemini AI Natural Language Commute Search Assistant and the UN SDG 13 Carbon Analytics engine.

Technical Requirements:
1. Build `GeminiRideAssistant`: Call Google Gemini API (`gemini-1.5-flash`) to parse user free-form prompts (e.g., "Need ride from FSKTM to Section 17 at 5pm") into structured JSON: `{"origin": "FSKTM", "destination": "Section 17", "time": "17:00"}`.
2. Build Regex-based offline fallback parser if Gemini API fails or loses connectivity.
3. Build SDG 13 Carbon Engine: Calculate trip emissions saved using `Distance_km * 0.192 kg CO2`. Update aggregate metrics in `/users/{userId}` upon trip completion.
4. Build SDG13DashboardScreen displaying: Personal Lifetime CO2 avoided (kg), Equivalent Trees Planted metric, and an Inter-Faculty SDG Leaderboard UI ranking faculties by total accumulated carbon savings.

Output Requirements:
Provide complete Kotlin code for GeminiRideAssistant.kt, CarbonTrackerRepositoryImpl.kt, and SDG13DashboardScreen.kt with mock unit tests for carbon calculations.
```

---

## Role 6: UI/UX Polish, QA Testing & NFR Benchmarking Lead
### Responsibilities
* Enforce cohesive Material 3 design system, typography, light/dark themes, and component styling across all screens.
* Conduct performance latency benchmarking for ride search feed loading (`< 2.0` seconds requirement).
* Write complete JUnit & Compose UI unit/integration tests to reach required test coverage.

### Production Prompt
```text
[SYSTEM BASE CONTEXT]

Role: UI/UX Polish, QA Testing & NFR Benchmarking Lead
Package Path: com.um.carpool.core.ui & src/test

Task Overview:
Standardize the visual design architecture across all screens, execute NFR latency benchmarking, and construct unit/UI test suites.

Technical Requirements:
1. Define central Material 3 Theme (`Theme.kt`, `Color.kt`, `Type.kt`) using clean corporate blues, dark slates, and clear contrast typography. Standardize custom components (`PrimaryButton`, `RideCard`, `FacultyBadge`). Ensure zero system emojis are present.
2. Implement Latency Benchmarking Utility (`PerformanceBenchmark.kt`): Measure time elapsed from Firestore query invocation to UI item rendering. Assert loading times stay below 2000 ms limit under simulated network throttling.
3. Write Unit Tests (`BookingTransactionTest.kt`): Test concurrent booking logic to guarantee zero negative seat counts.
4. Write Compose UI Automated Tests (`AuthFlowTest.kt`): Test email domain validation rules for non-@siswa.um.edu.my inputs.

Output Requirements:
Provide complete Kotlin code for Theme.kt, CustomComponents.kt, PerformanceBenchmark.kt, and automated testing suites under `src/test/` and `src/androidTest/`.
```

---

## Summary Checklist for WIA2007 Deliverables
1. **Module Completion:** Ensure all 5 Functional Modules are integrated into the `main` branch.
2. **NFR Verification:** Run Latency Benchmark (`< 2.0 s`) and Atomic Booking Stress Tests.
3. **Video Presentation (Due 3 Jan 2027):** Record operational video demo featuring real-time booking, Emergency SOS, Gemini AI Search, and SDG 13 Dashboard.
4. **Final Report & AI Log (Due 10 Jan 2027):** Complete final written report including Appendix 2(b) documenting AI prompts utilized during development.