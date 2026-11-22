# Product Requirements Document (PRD): UM-Pool
**Course:** WIA2007 Mobile Application Development (2026/2027 Semester 1)  
**Theme:** Mobile Application and SDGs: Making Technology Purposeful for a Better Future  
**Target SDG:** SDG 13 – Climate Action (Targets 13.2 & 13.3)  
**Target Community:** Universiti Malaya (UM) Students & Staff  

---

## 1. Executive Summary & Problem Context
### 1.1 Problem Statement
Universiti Malaya (UM) commuters face severe daily mobility bottlenecks during peak class dismissal hours (4:00 PM – 7:00 PM) and sudden tropical rainstorms:
* **For Non-Drivers:** Campus shuttle buses face unpredictable intervals and overcrowding, last-mile transit to nearby LRT stations (LRT Universiti, LRT Kerinchi) is inconvenient, and e-hailing platforms (e.g., Grab) impose high surge fares.
* **For Student Drivers:** Parking spaces across faculties (e.g., FSKTM, FK, FPE) are chronically congested, while single-occupancy drivers bear high fuel and toll costs alone despite having 3–4 empty seats.

### 1.2 Solution Overview
**UM-Pool** is a verified, faculty-centric peer-to-peer carpooling mobile application built natively for Android. Unlike generic on-demand e-hailing platforms, UM-Pool aligns rides with **fixed faculty lecture dismissal schedules** and **common residential corridors** (e.g., PJ SS2/Sec 17, Bangsar South, KL Sentral). It uses a transparent fuel-subsidy cost-splitting model, integrates AI for smart schedule matching and demand prediction, and quantifies cumulative $CO_2$ savings to advance **SDG 13 (Climate Action)**.

### 1.3 SDG 13 Alignment
* **Target 13.2 (Climate Change Measures in Planning):** Reduces single-occupancy vehicle (SOV) traffic entering and exiting UM gates, lowering campus carbon emissions and parking congestion.
* **Target 13.3 (Awareness & Human Capacity):** Educates and motivates students through real-time personal and inter-faculty carbon offset metrics ($kg\text{ }CO_2$ saved).

---

## 2. Target Users & Stakeholders
* **Primary Users (Riders):** UM students relying on public transit or e-hailing seeking affordable, weather-safe rides home after class.
* **Primary Users (Drivers):** UM student/staff vehicle owners seeking to offset petrol/toll costs and reduce solo driving fatigue.
* **Institutional Stakeholders:** UM Sustainable Development Centre (UMSDC), Department of Development & Estate Maintenance (JPPHB), Residential College Committees (JTK), and UM Security Office.

---

## 3. Functional Requirements (5 Core Modules)

### Module 1: Campus Authentication & Trust Verification
* **FR-1.1 (Domain-Restricted Registration):** The system shall restrict user registration and authentication exclusively to verified Universiti Malaya email domains (`@siswa.um.edu.my` and `@um.edu.my`) via Firebase Authentication OTP/verification links.
* **FR-1.2 (Academic & Commute Profile Setup):** The user shall be able to configure their profile with their primary UM faculty/academy, matric/staff ID, gender preference, and default residential zone.
* **FR-1.3 (Driver Vehicle Onboarding):** Users opting to offer rides shall be able to register and update their vehicle details (car model, color, license plate number, and available passenger seat capacity).

### Module 2: Faculty-Schedule Ride Publishing & Matching
* **FR-2.1 (Schedule-Aligned Ride Posting):** Drivers shall be able to publish carpool listings tied to specific UM pickup points (e.g., FSKTM Main Lobby, KK12), standardized lecture dismissal slots (e.g., 4:00 PM, 5:00 PM, 6:00 PM), and destination corridors.
* **FR-2.2 (Multi-Criteria Ride Filtering):** Riders shall be able to search and filter active carpool listings by departure faculty, dismissal time window ($\pm 15\text{ mins}$), destination area, and gender-matching preference.
* **FR-2.3 (Concurrency-Safe Seat Reservation):** Riders shall be able to request and confirm available seats in real time, utilizing atomic database transactions to decrement seat counts and prevent overbooking race conditions.

### Module 3: Automated Fuel-Split & Cost-Sharing
* **FR-3.1 (Fair Fuel-Subsidy Calculation):** The system shall automatically compute a capped, non-commercial fuel and toll split-contribution per passenger based on route distance (via Google Distance Matrix API) to ensure legal compliance as non-commercial carpooling.
* **FR-3.2 (Dynamic Multi-Rider Cost Recalculation):** The system shall dynamically recalculate and lower the per-person fuel share as additional passengers join the same vehicle.
* **FR-3.3 (Trip Settlement Tracking):** Drivers and riders shall be able to log and confirm peer-to-peer cost settlement status (e.g., DuitNow QR scan confirmation or cash settled) upon trip completion.

### Module 4: In-Ride Safety & Community Reputation
* **FR-4.1 (Active Trip Status & Contact Sharing):** Riders shall be able to share live trip details (driver name, verified matric status, vehicle plate, and planned route) with an emergency contact upon boarding.
* **FR-4.2 (Campus Emergency SOS Dispatch):** The system shall provide a persistent one-tap SOS trigger during active rides that displays live GPS coordinates and initiates direct emergency calls/alerts to the UM Security Office (`03-7967 7070`).
* **FR-4.3 (Mutual Post-Ride Rating & Review):** Both drivers and passengers shall be able to submit a 1-to-5 star rating and structured feedback tags after each completed ride to maintain community accountability.

### Module 5: AI Commute Assistant & SDG 13 Carbon Tracker
* **FR-5.1 (AI Smart Route & Schedule Recommender):** An integrated AI assistant (powered by Gemini API / on-device matching heuristics) shall parse natural-language user queries (e.g., *"Leaving Engineering at 5pm going to LRT Universiti"*) and recommend optimal departure windows and matching carpools.
* **FR-5.2 (Carbon Footprint Offset Calculator):** The system shall automatically compute the net $CO_2$ emissions saved per trip using the formula:
  $$\text{CO}_2\text{ Saved (kg)} = \text{Distance (km)} \times 0.192 \times (\text{Total Occupants} - 1)$$
* **FR-5.3 (Personal & Inter-Faculty SDG 13 Leaderboard):** The system shall aggregate individual and faculty-wide $CO_2$ savings into visual progress milestones and a real-time UM inter-faculty sustainability leaderboard.

---

## 4. Non-Functional Requirements (NFRs)
1. **NFR-1 (Peak-Hour Performance & Latency):** The ride discovery feed and seat reservation transactions shall load and complete within **2.5 seconds** under simulated peak dismissal concurrency (50+ simultaneous requests at 5:00 PM) using indexed Cloud Firestore queries.
2. **NFR-2 (Data Privacy & Security):** All user authentication tokens, matric numbers, and real-time location coordinates shall be transmitted over TLS 1.3 encryption, with Firestore Security Rules enforcing that sensitive contact details and vehicle plates are only visible to matched ride participants.

---

## 5. Technical Architecture & Stack
* **Mobile Platform:** Native Android (Minimum SDK 26 / Android 8.0+)
* **Language & UI Framework:** Kotlin with Jetpack Compose (Material Design 3)
* **Backend & Database:** Firebase Authentication, Cloud Firestore (NoSQL real-time sync), Firebase Cloud Messaging (FCM) for ride status push notifications
* **Maps & Geospatial:** Google Maps SDK for Android, Google Places API, Distance Matrix API
* **AI Integration:** Google AI SDK for Android (Gemini Flash API) for natural-language ride matching and commute pattern suggestions
* **Version Control & Project Management:** GitHub (feature-branch workflow) & Trello

---

## 6. Database Schema Overview (Cloud Firestore)
* `users/{uid}`: `name`, `siswaEmail`, `matricId`, `faculty`, `gender`, `role`, `ratingAvg`, `totalCo2SavedKg`, `vehicleInfo{model, plate, color, seats}`
* `rides/{rideId}`: `driverId`, `originFaculty`, `destinationZone`, `departureTimestamp`, `availableSeats`, `totalSeats`, `estimatedDistanceKm`, `baseFuelCostRm`, `genderPreference`, `status` (`OPEN`, `IN_PROGRESS`, `COMPLETED`)
* `bookings/{bookingId}`: `rideId`, `riderId`, `pickupNote`, `calculatedShareRm`, `paymentStatus`, `sosTriggered`, `createdAt`
* `ratings/{ratingId}`: `rideId`, `fromUserId`, `toUserId`, `score`, `tags`, `comment`

---

## 7. Team Roles & Responsibilities (6 Members)
Every team member is required to contribute **at least 20 hours** and actively write code (no documentation-only roles allowed per WIA2007 guidelines).

| Role | Assigned Member | Core Technical & Project Responsibilities |
| :--- | :--- | :--- |
| **1. Project Manager & QA Lead** | Member 1 | • Coordinates Trello sprints, GitHub PR reviews, and UM stakeholder survey/interviews.<br>• Develops **Module 4 (Safety & Ratings)** UI and logic.<br>• Designs and executes test cases to verify FRs and benchmark **NFR-1** (latency testing).<br>• Compiles the **Mandatory AI Usage Log** across all teammates. |
| **2. Software Architect & Backend Lead** | Member 2 | • Designs overall MVVM architecture, Firestore schema, and Security Rules (**NFR-2**).<br>• Implements **Module 1 (Siswa Email Auth & Profile Verification)**.<br>• Implements atomic Firestore transactions for **FR-2.3 (Concurrency-Safe Seat Booking)** to prevent race conditions. |
| **3. Lead Frontend / UI-UX Developer** | Member 3 | • Designs app wireframes, Material 3 color system, and accessible navigation flows.<br>• Builds the core Jetpack Compose screens for **Module 2 (Home Ride Feed, Filter Chips, and Post-a-Ride Wizard)**.<br>• Ensures responsive layouts across different Android screen sizes. |
| **4. Geospatial & Cost-Split Developer** | Member 4 | • Integrates **Google Maps SDK, Places Autocomplete, and Distance Matrix API**.<br>• Develops **Module 3 (Smart Fuel-Subsidy Calculator, Dynamic Multi-Rider Split Logic, and DuitNow/Settlement Tracker)**.<br>• Builds the interactive route preview map on ride detail screens. |
| **5. AI Integration Developer** | Member 5 | • Integrates the **Gemini API / smart matching engine** for **Module 5 (FR-5.1 Natural-Language Ride Finder & Departure Window Predictor)**.<br>• Evaluates AI response accuracy and fallback error handling for the testing section of the report. |
| **6. Sustainability & Analytics Developer** | Member 6 | • Develops **Module 5 (FR-5.2 $CO_2$ Offset Engine & FR-5.3 Inter-Faculty SDG 13 Leaderboard)**.<br>• Implements Firebase Cloud Messaging (FCM) push notifications for booking confirmations and ride arrival alerts.<br>• Leads data visualization and screen recording for the 10–15 minute demo video. |

---

## 8. Milestones & Deliverables Timeline
* **Group Registration:** Sunday, 21 October 2026
* **User Study & Stakeholder Survey Completion:** Week 5
* **MVP Core Modules (Auth, Ride Feed, Booking) Complete:** Week 8
* **Maps, Cost-Split, AI Assistant & Carbon Tracker Complete:** Week 10
* **Testing, NFR Benchmarking & User Testing:** Week 11
* **Video Presentation Submission (10–15 mins):** Sunday, 3 January 2027 (12:00 PM)
* **Final Report & AI Usage Log Submission:** Sunday, 10 January 2027 (12:00 PM)