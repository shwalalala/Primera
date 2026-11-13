# Primera Mobile STD Test Execution: Discoveries and Assumptions

## 1. Project Identification & Environment
- **Application ID:** `cit.edu.primera`
- **Launcher Activity:** `cit.edu.primera.MainActivity`
- **Main Activities / Components:**
  - `MainActivity` (launcher activity with Compose navigation via `AppNavGraph`)
  - `PermissionsRationaleActivity` (exported rationale activity for Health Connect)
  - `ViewPermissionUsageActivity` (alias for `PermissionsRationaleActivity`, permission `android.permission.START_VIEW_PERMISSION_USAGE`, category `android.intent.category.HEALTH_PERMISSIONS`)
  - `ReminderReceiver` (`BroadcastReceiver` for scheduled alarms)
  - `BootReceiver` (`BroadcastReceiver` for `BOOT_COMPLETED` and `MY_PACKAGE_REPLACED`)
  - Facebook SDK components: `FacebookActivity`, `CustomTabActivity`
  - FileProvider: `cit.edu.primera.fileprovider`
- **SDK Targets:** `minSdk = 26`, `targetSdk = 36`, `compileSdk = 37`
- **Compose UI / Material:** Material 3 (`androidx.compose.material3`), Compose BOM, ViewModels injected via `ViewModelProvider.Factory` backed by `AppContainerImpl`.

## 2. Backend & Firebase Configuration
- **Backend Service:** Google Firebase (Authentication, Cloud Firestore, Cloud Storage).
- **Configuration File:** `app/google-services.json`
  - Project ID: `primera-eafe6`
  - Storage Bucket: `primera-eafe6.firebasestorage.app`
  - RTDB URL: `https://primera-eafe6-default-rtdb.asia-southeast1.firebasedatabase.app`
- **Emulator Suite Detection:**
  - No `firebase.json` or local Firebase emulator configuration is present in the repository.
  - The client source code (`AuthDataSource.kt`, `AppContainer.kt`) calls `FirebaseAuth.getInstance()` and `FirebaseFirestore.getInstance()` without emulator port redirection (`useEmulator`).
  - Therefore, the debug build currently points to the live Firebase project (`primera-eafe6`).
  - **Hard Rule 4 Compliance:** All tests will execute strictly with designated test credentials (as requested in Step 0.5 or generated defaults `primera.tester+<timestamp>@example.com`), ensuring production and medical integrity are strictly preserved.

## 3. Navigation Graph & Screen Architecture
- **Navigation Graph:** `AppNavGraph.kt` managing `NavHost` with start destination `Routes.SPLASH`.
- **Navigation Routes:**
  - `Routes.SPLASH` ("splash"): SplashScreen with session check (`hasEverLoggedIn`, `isUserAuthenticated`, `shouldShowOnboarding`).
  - `Routes.WELCOME` ("welcome"): WelcomeScreen with "Log In" and "Create Account" buttons.
  - `Routes.LOGIN` ("login", alias `AUTH_SCREEN`): LoginScreen.
  - `Routes.REGISTER` ("register"): RegisterScreen.
  - `Routes.FORGOT_PW` ("forgot_password"): ForgotPasswordScreen.
  - `Routes.ONBOARDING` ("onboarding"): OnboardingHostScreen (multi-step maternal profile onboarding).
  - `Routes.DASHBOARD` ("dashboard"): DashboardScreen with pregnancy milestones, baby development, health logs summary, vitals, drawer, bottom navigation, and transcription FAB.
  - `Routes.DEVICE` ("device"): SmartwatchRoute (`SmartwatchScreen`) connecting Health Connect (heart rate, steps, sleep, oxygen saturation).
  - `Routes.INSIGHT` ("insight"): InsightsScreen (progress score cards, wellness goals, weekly/monthly trend charts, symptom warning alerts).
  - `Routes.CHECKIN` ("checkin"): CheckinsOverviewScreen (calendar overview, log history, search, date filter, entry preview/delete/edit, prenatal checkups).
  - `Routes.DAILY_CHECKIN` ("daily_checkin"): DailyCheckinScreen (maternal weight, wellness metrics, symptoms, notes).
  - `Routes.CHECKIN_PREVIEW` ("checkin_preview"): CheckinPreviewScreen (review check-in before submit).
  - `PRENATAL_CHECKUP_ROUTE` ("prenatal_checkup?recordId={recordId}"): PrenatalCheckupScreen.
  - `Routes.TRANSCRIPTION` ("transcription"): TranscriptionScreen (speech-to-text logging, symptom tags, editable transcription).
  - `Routes.PROFILE` ("profile"): ProfileScreen (personal info, pregnancy details, emergency contacts, edit profile).
  - `Routes.SETTINGS` ("settings"): SettingsScreen (account management, logout, reminders).
  - `Routes.NOTIFICATIONS` ("notifications"): NotificationsScreen (unread badge, categories: Check-in, Hydration, Wellness, mark all read).
  - `Routes.REMINDER_SETTINGS` ("reminder_settings"): ReminderSettingsScreen (master toggle, category switches, time pickers).
  - `Routes.VACCINATION_INFO` ("vaccination_info"): VaccinationInfoScreen.
  - `Routes.NEWS_EDUCATION` ("news_education"): NewsEducationScreen.
- **Deep Linking / Intent Routing:**
  - Handled in `MainActivity.kt` via `NotificationHelper.EXTRA_DESTINATION_ROUTE` (`intent.getStringExtra("EXTRA_DESTINATION_ROUTE")`).
  - Passed as `pendingDestinationRoute` to `AppNavGraph`.
  - Security check: If `FirebaseAuth.currentUser == null` and target route is protected (e.g. `dashboard`, `daily_checkin`, `profile`), `AppNavGraph` redirects directly to `Routes.LOGIN`.

## 4. UI Elements, Selectors, and Test Identifiers
- **Test Tags:** There are currently no explicit `Modifier.testTag(...)` tags in production code.
- **Selectors Strategy:**
  - Automated tests will match elements using Compose semantic matchers (`hasText`, `hasClickAction`, `hasContentDescription`, `hasSetTextAction`, `isToggleable`) and UI Automator (`UiSelector`).
  - Core button labels used in app:
    - Welcome: "Log In", "Create Account"
    - Auth tab toggle: "Log In", "Sign Up"
    - Register: "Sign Up" (button), "Terms & Conditions"
    - Login: "Login" (button), "Remember me", "Forgot password?"
    - Onboarding: "Continue", "Confirm Information", "Confirm", "Cancel"
    - Profile: "Edit", "Save Changes", "Personal Information", "Pregnancy Details"
    - Check-in: "Daily Check-in", "Review", "Save Check-in", "Submit"
    - Smartwatch: "Connect Health Connect", "Grant Permissions", "Sync"
    - Transcription: "Save & Use in Check-in", "Try Again"
    - Notifications: "Mark all as read", "Reminder Settings"

## 5. Validation Rules and Error Strings
- **Register Screen Form Validation:**
  - Username: Required -> `"Username is required"`
  - Email:
    - Blank -> `"Email is required"`
    - Malformed -> `"Invalid email format"`
  - Password:
    - Minimum 8 characters -> `"Password must be at least 8 characters"`
    - Missing letter -> `"Password must contain letters"`
    - Missing digit or symbol -> `"Password must contain letters and numbers/symbols"`
  - Confirm Password:
    - Blank -> `"Please confirm your password"`
    - Mismatch -> `"Passwords do not match"`
  - Terms & Conditions:
    - Unchecked -> `"You must agree to terms"`
  - Backend Duplicate Email:
    - Firebase Auth User Collision -> `"This email address is already registered. Please sign in or use a different email."`
  - Backend Weak Password:
    - Weak password exception -> `"The password is too weak. Please choose a stronger password."`
- **Login Screen Form Validation:**
  - Email blank -> `"Email is required"`
  - Email invalid -> `"Invalid email format"`
  - Password blank -> `"Password is required"`
  - Wrong credentials / unverified -> `"Invalid email or password"`
  - Rate limiting (Too many requests) -> `"Please wait a moment before trying again."` (30-second cooldown timer displayed on button: `"Wait (Xs)"`)
- **Daily Check-in Validation:**
  - Weight is validated against reasonable thresholds (kg).
- **Profile / Onboarding Validation:**
  - Birthday: Past date required.
  - Height & Weight: Positive integers required.
  - LMP / Ultrasound: Consistent gestational age calculation.

## 6. Assumptions for Modules
- **Module 3.4 (Smartwatch Connection) & 3.5 (Smartwatch Health Data):** An Android emulator cannot be paired with a physical smartwatch. Health data will be tested through Health Connect via HealthConnectClient test records (steps, heart rate, sleep, SpO2), validating the Health Connect data ingestion pipeline.
- **Module 3.6 (Speech):** An emulator environment cannot capture live human voice acoustics reliably; the audio recording and speech recognizer UI flow will be exercised, and for transcription text editing, the specified test string (`"I have a headache and I feel nauseous today"`) will be typed into the transcription editor, marking acoustic recognition as `SKIPPED-MANUAL`.
- **Module 3.19 (Scheduled Notifications):** Reminders will be scheduled via `ReminderManager` / AlarmManager and tested by checking scheduled alarms and triggering alarms/notifications.
- **Module 3.22 (Security):** Firebase security rules and client-side encryption-at-rest checks that require backend deployment will be evaluated against available local configuration and rules in `firestore.rules`.

## 7. Test User & Profile Details (Configured from Step 0.5)
- **Primary Registration Test User (3.1 & 3.2):**
  - Username: `SSTestUser`
  - Email: `primera.tester+reg@example.com`
  - Password: `[REDACTED - STORED SECURELY IN .test-users.env]`
- **Google Sign-In (3.2):**
  - Explicitly skipped per user instruction (`skip gmail login`). Marked as `SKIPPED-MANUAL`.
- **Fictional Default Maternal Health Profile Values (3.3):**
  - Birthday: May 15, 1998 (26 years old)
  - Height: 162 cm
  - Weight: 58 kg
  - Cycle regularity: Regular (28 days)
  - Ultrasound: No
  - LMP: Date corresponding to ~12 weeks gestational age
  - First pregnancy: Yes
