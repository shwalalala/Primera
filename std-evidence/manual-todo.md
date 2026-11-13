# Primera Mobile Test Execution - Manual Test Procedure (SKIPPED-MANUAL)

The following test steps require physical hardware or third-party accounts that cannot be reliably simulated inside an Android emulator environment. Complete these steps manually on a physical Android device following the exact instructions below.

---

### 1. Module 3.2 - Step 7: Google Sign-In
- **Reason Skipped:** Explicitly skipped per user instruction (`skip gmail login`); requires an active Google Play Services account on the device.
- **Hardware/Prerequisites:** Physical Android device with Google Play Services installed and an active Google Account logged in.
- **Manual Verification Procedure:**
  1. Launch Primera on the device.
  2. On the Welcome screen, tap **Log In**.
  3. On the Login screen, locate and tap the **Continue with Google** button.
  4. The Google One Tap / Credential Manager account selection sheet should appear.
  5. Select your Google account.
  6. Verify that authentication succeeds and the app transitions to either the Onboarding Profile Setup (for new users) or the Home Dashboard (for existing users).
  7. Capture a full-screen screenshot showing the authenticated account landing screen and save as `3.2_s7_google_sign_in.png`.

---

### 2. Module 3.4 - Steps 2, 3, 4: Smartwatch Connection & Pairing
- **Reason Skipped:** An Android emulator cannot pair with a physical Wear OS smartwatch via Bluetooth. Additionally, Android 14+ uses the built-in Health Connect system framework rather than the legacy standalone APK checked by the app.
- **Hardware/Prerequisites:** Physical Android phone (Android 14+) paired with a Wear OS smartwatch (e.g., Pixel Watch, Galaxy Watch) and Health Connect enabled.
- **Manual Verification Procedure:**
  1. Open Primera and tap the **Device** tab in the bottom navigation bar.
  2. Tap **Connect and Sync**.
  3. When the Android system permissions sheet appears requesting access to Heart Rate, Steps, Sleep, and Oxygen Saturation, verify the request screen and capture `3.4_s2_permission_request.png`.
  4. Tap **Deny All** or refuse one permission. Verify the app shows a permissions-denied advisory and capture `3.4_s3_permission_denied.png`.
  5. Tap **Connect and Sync** again and tap **Allow All**. Verify the connected state shows "Permissions accepted" and active sync status. Capture `3.4_s4_connected_state.png`.

---

### 3. Module 3.5 - Steps 1, 2, 3: Live Health Data Ingestion from Watch
- **Reason Skipped:** Requires physical sensor telemetry (heart rate optical PPG, accelerometer, pulse oximeter) synced from a paired smartwatch into Health Connect.
- **Hardware/Prerequisites:** Wear OS smartwatch worn on wrist with recorded health metrics synced to Health Connect.
- **Manual Verification Procedure:**
  1. Wear the smartwatch and record at least 15 minutes of heart rate, steps, and SpO2.
  2. In Primera, tap the **Device** tab and tap **Sync Now**.
  3. Return to the **Home** tab and swipe to the **StatsGrid** carousel card.
  4. Verify that Heart Rate (bpm), Steps, Sleep (hours/min), and SpO2 (%) cards reflect the live watch readings. Capture `3.5_s1_vitals_summary.png`.
  5. Navigate to the **Insight** tab and verify the weekly trend charts. Capture `3.5_s2_weekly_trends.png`.
  6. If abnormal vitals are recorded (e.g. resting heart rate > 110 bpm or SpO2 < 94%), verify the health alert banner and capture `3.5_s3_health_alert_indicator.png`.

---

### 4. Module 3.6 - Steps 3, 4, 5, 6: Speech Acoustic Transcription
- **Reason Skipped:** The Android emulator virtual audio driver emits SpeechRecognizer error code 11 (`ERROR_CANNOT_CHECK_SUPPORT`), preventing live voice capture.
- **Hardware/Prerequisites:** Physical Android device with working microphone and Google Speech Recognition service installed.
- **Manual Verification Procedure:**
  1. Open Primera and tap the floating **Microphone** FAB in the bottom navigation bar.
  2. When prompted, grant microphone access. Verify the pulsating "Listening..." animation is active.
  3. Clearly speak the sentence: *"I have a headache and I feel nauseous today"*.
  4. Verify the transcribed text appears accurately in the editable transcription text field. Capture `3.6_s3_transcription_text.png`.
  5. Verify that symptom tags (e.g., `Headache`, `Nausea`) are automatically detected and highlighted. Capture `3.6_s4_symptom_tags.png`.
  6. Edit the transcription text to append extra details and tap **Save & Use in Check-in**. Capture `3.6_s5_edited_text_saved.png`.
  7. Verify the new check-in entry appears in the Daily Check-in form with the speech notes pre-populated. Capture `3.6_s6_saved_entry.png`.
