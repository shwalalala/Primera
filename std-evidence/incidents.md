# Primera Mobile Test Execution - Incidents & Defect Log

This document records architectural, functional, and behavioral discrepancies identified during the execution of the Software Test Document (STD) mobile test suite on the Android emulator (`emulator-5554`, API 34), along with their implemented resolutions.

---

### Incident 1: Health Connect Package Check Fails on Android 14+ (API 34+)
- **Module:** 3.4 (Smartwatch Connection) & 3.5 (Smartwatch Health Data)
- **Severity:** Medium / Compatibility Defect
- **Component:** `cit.edu.primera.feature.smartwatchconnection.data.HealthConnectManager.isPackageInstalled()`
- **Status:** **RESOLVED**
- **Observed Behavior:**
  The app checked for the existence of package `com.google.android.apps.healthdata` using `packageManager.getPackageInfo("com.google.android.apps.healthdata", ...)`. On Android 14 (API 34) and newer, Health Connect is a core Android system framework service rather than a standalone APK download from Google Play. As a result, `getPackageInfo` threw `PackageManager.NameNotFoundException`, causing the UI to display: `"Health Connect is not installed on this device."`
- **Resolution Implemented:**
  Updated `HealthConnectManager.isPackageInstalled()` to check SDK availability status (`getAvailabilityStatus() != HealthConnectClient.SDK_UNAVAILABLE`) when running on Android 14+ (`Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE`). In addition, `openHealthConnect()` now utilizes `HealthConnectClient.getHealthConnectManageDataIntent(context)` with fallback intents to seamlessly open Health Connect on both system-framework and standalone implementations.
- **Impact:**
  Device pairing and sync flows now operate properly on Android 14 and Android 15 devices.

---

### Incident 2: Speech Recognition Hardware Unavailable on Android Emulator
- **Module:** 3.6 (Speech to Text)
- **Severity:** Environment Limitation / Usability Enhancement
- **Component:** `cit.edu.primera.feature.transcription.data.SpeechRecognitionManager`, `TranscriptionScreen`, `TranscriptionViewModel`
- **Status:** **RESOLVED (Mitigated with Manual Input Fallback)**
- **Observed Behavior:**
  When microphone permission is granted and speech recognition starts, the Android emulator's virtual audio layer emits speech error code 11 (`SpeechRecognizer.ERROR_CANNOT_CHECK_SUPPORT` / audio hardware failure). The editable transcription input field in `TranscriptionScreen` was conditionally displayed only after successful voice recognition, preventing acoustic verification and manual testing on emulators.
- **Resolution Implemented:**
  1. Added a direct `"Or type your notes manually"` action button in `TranscriptionScreen` so users and testers can immediately enter notes without physical/emulator audio hardware.
  2. Enhanced the error notification dialog to include a `"Type Manually"` action that gracefully transitions the screen into editable mode.
  3. Added `startManualInput()` in `TranscriptionViewModel` to switch the UI state to `Success`, allowing complete testing of AI punctuation, symptom extraction, editing, and check-in integration on all devices and emulators.

---

### Incident 3: Absence of Delete Action for Daily Check-In Logs in UI
- **Module:** 3.8 (Log History - Steps 7 & 8)
- **Severity:** Functional Discrepancy
- **Component:** `cit.edu.primera.feature.checkins.ui.CheckinsOverviewScreen` / `DailyCheckinDetailsSheet`, `CheckinsViewModel`, `CheckinsRepository`, `CheckinsDataSource`
- **Status:** **RESOLVED**
- **Observed Behavior:**
  The STD specifies deleting a daily check-in entry with a confirmation dialog (Steps 7 & 8). In the Primera app implementation, the daily check-in detail sheet only exposed an `"Edit Daily Check-In"` action and a `"Close"` button. There was no delete button or delete confirmation dialog for daily check-in records in the UI.
- **Resolution Implemented:**
  1. Added a `"Delete Daily Check-In"` button in `DailyCheckinDetailsSheet` with an `AlertDialog` confirmation prompt ("Are you sure you want to delete this check-in entry? This action cannot be undone.").
  2. Implemented `deleteLog(logId)` in `CheckinsDataSource` calling Firestore document deletion (`firestore.collection("checkins").document(logId).delete().await()`), which is permitted by Firestore security rules (`allow delete: if isOwner(...)`).
  3. Propagated `deleteLog` through `CheckinsRepository`, `CheckinsRepositoryImpl`, and `CheckinsViewModel` with immediate local cache removal and reactive listener UI updates.
- **Impact:**
  Steps `3.8_s7` and `3.8_s8` can now execute and PASS.

---

### Incident 4: ReminderReceiver Exported Without Permissions
- **Module:** 3.22 (Security & Permissions Audit)
- **Severity:** Low / Hardening Recommendation
- **Component:** `cit.edu.primera.core.notification.ReminderReceiver` in `AndroidManifest.xml`
- **Status:** **RESOLVED**
- **Observed Behavior:**
  `ReminderReceiver` was declared with `android:exported="true"` but contained no `<intent-filter>` and no signature permission protection.
- **Resolution Implemented:**
  Changed `ReminderReceiver` in `AndroidManifest.xml` to `android:exported="false"`. `AlarmManager` pending intents created by the same application UID do not require the receiver to be exported, completely preventing third-party applications from broadcasting unauthorized alarm triggers.
