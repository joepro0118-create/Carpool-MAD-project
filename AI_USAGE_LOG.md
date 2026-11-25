# AI Usage Log — WIA2007 Appendix 2(b) Compliance

**Project:** UM-Ride (Universiti Malaya Campus Carpooling App)  
**Course:** WIA2007 Mobile Application Development (2026/2027 Semester 1)

---

## Instructions for All Team Members

In accordance with WIA2007 Academic Integrity guidelines (Appendix 2(b)), **every team member must log all AI-assisted tasks** used during design, coding, debugging, or testing.

1. **Log Immediately:** Add a new row to the table below as soon as you complete an AI-assisted task, before opening a Pull Request.
2. **Preserve Exact Prompts:** Paste the exact prompt or query you entered (do not paraphrase).
3. **Document Human Verification:** Never copy-paste AI output blindly. You must record how you tested, debugged, refactored, or modified the generated code to fit our architecture (`Clean Architecture + MVVM + Repository Pattern`) and constraints.
4. **Submission:** Role 1 (Project Manager) will compile and verify this log before the final report deadline (**10 January 2027**).

---

## AI Usage Log Table

| Date | Team Member | Module / Feature | AI Tool Used | Exact Prompt / Query | AI Output Summary | Manual Verification, Testing & Modifications Made |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| 2026-11-22 | Member 1 (Auth/Profile Lead) | Module 1: `@siswa.um.edu.my` Email Domain Validation (`AuthViewModel.kt`) | Gemini 2.5 Pro | "Write a Kotlin function and JUnit4 test cases for validating Universiti Malaya student emails ending with @siswa.um.edu.my, returning sealed validation states." | Generated a regex-based email validator using `Patterns.EMAIL_ADDRESS` and 3 basic JUnit4 tests. | Replaced `android.util.Patterns.EMAIL_ADDRESS` with a pure Kotlin regex so unit tests run on the local JVM without Robolectric; added edge-case tests for uppercase domains, whitespace trimming, and `@um.edu.my` rejection; verified all 6 JUnit tests pass via `./gradlew test`. |
| 2026-11-24 | Member 3 (Backend & Concurrency) | Module 2: Atomic Seat Reservation (`BookingRepositoryImpl.kt`) | ChatGPT (GPT-4o) | "Implement FirebaseFirestore runTransaction in Kotlin to decrement availableSeats on /rides/{rideId} and create a booking subcollection document without overbooking." | Provided a `runTransaction` snippet that decremented `availableSeats` and wrote to `/bookings`. | Added `Result<Unit>` error mapping and an explicit guard for `availableSeats <= 0` throwing `RideFullException`; added status transition to `"FULL"` when `availableSeats - 1 == 0`; tested concurrency behavior using Firebase Local Emulator with two simultaneous coroutines to confirm zero negative seat counts. |
| 2026-11-25 | Member 1 (PM & Auth/Profile Lead) | Root Gradle/Koin DI Setup & Module 1 (`com.um.carpool.feature.auth`) | Antigravity (Gemini) | "Role 1: Setup root repository configuration, Gradle dependencies, Koin DI, and build AuthRepositoryImpl.kt, AuthViewModel.kt, AuthScreens.kt, and domain validation unit tests." | Generated AGP 9.4.1 / Compose Material 3 Gradle setup, Koin DI module, `AuthValidator`, Firebase `AuthRepositoryImpl`, `AuthViewModel` (`StateFlow`), Material 3 Auth/Profile/Driver screens, and JUnit4 tests. | Configured conditional `google-services` plugin activation and `FirebaseApp` availability checks so builds and tests succeed even before `google-services.json` is added locally; replaced deprecated `MenuAnchorType` with `ExposedDropdownMenuAnchorType`; tuned Gradle JVM memory settings (`-Xmx1536m`) and verified `./gradlew testDebugUnitTest assembleDebug` passes with 12/12 unit tests and zero warnings. |
