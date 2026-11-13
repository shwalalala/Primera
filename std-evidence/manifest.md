# Primera Mobile Automated Test Evidence Manifest

**Execution Platform:** Android Emulator (`emulator-5554`, API 34, Google APIs, Pixel-class device)  
**Package Name:** `cit.edu.primera`  
**Launcher Activity:** `cit.edu.primera.MainActivity`  
**Test User Account:** `SSTestUser` (`primera.tester+reg@example.com`)  
**Backend:** Firebase Cloud Firestore & Authentication (`primera-eafe6`)  

---

## Summary of Results

| Module | Description | Total Steps | PASS | FAIL | SKIPPED-MANUAL |
| :--- | :--- | :---: | :---: | :---: | :---: |
| **3.1** | User Registration | 6 | 6 | 0 | 0 |
| **3.2** | User Login | 7 | 6 | 0 | 1 |
| **3.3** | Profile Setup & Editing | 5 | 5 | 0 | 0 |
| **3.4** | Smartwatch Connection | 5 | 2 | 0 | 3 |
| **3.5** | Smartwatch Health Data | 4 | 1 | 0 | 3 |
| **3.6** | Speech to Text (Transcription) | 6 | 2 | 0 | 4 |
| **3.7** | Daily Check-in Logging | 5 | 5 | 0 | 0 |
| **3.8** | Check-in Log History | 9 | 7 | 2 | 0 |
| **3.9** | Maternal Dashboard | 3 | 3 | 0 | 0 |
| **3.10**| Insights and Goals | 6 | 6 | 0 | 0 |
| **3.17**| Notifications & Drawer | 1 | 1 | 0 | 0 |
| **3.18**| Reminder Settings & Time Picker | 2 | 2 | 0 | 0 |
| **3.19**| Scheduled Reminders (AlarmManager) | 1 | 1 | 0 | 0 |
| **3.20**| Deep Links & Intent Routing | 3 | 3 | 0 | 0 |
| **3.21**| Performance & Startup Times | 1 | 1 | 0 | 0 |
| **3.22**| Intent & Component Security Audit | 1 | 1 | 0 | 0 |
| **3.23**| Offline Mode & Network Resilience | 2 | 2 | 0 | 0 |
| **TOTAL** | | **67** | **54** | **2** | **11** |

---

## Detailed Step-by-Step Manifest

### Module 3.1: User Registration
| Step | Name | Status | Screenshot File | Description |
| :--- | :--- | :---: | :--- | :--- |
| s1 | Register screen | **PASS** | `3.1_s1_register_screen.png` | Register screen loaded with username, email, password, confirm password, and terms checkbox. |
| s2 | Blank field validation | **PASS** | `3.1_s2_blank_field_errors.png` | Validation triggers inline errors for blank username, email, password, and confirmation. |
| s3a | Invalid email format | **PASS** | `3.1_s3a_invalid_email.png` | Rejection of malformed email with message "Invalid email format". |
| s3b | Weak password format | **PASS** | `3.1_s3b_weak_password.png` | Rejection of password < 8 characters with required complexity messages. |
| s3c | Mismatched password | **PASS** | `3.1_s3c_mismatched_password.png` | Rejection with "Passwords do not match" when confirmation differs. |
| s4 | Terms unchecked blocked | **PASS** | `3.1_s4_terms_unchecked_blocked.png` | Submission blocked when Terms & Conditions checkbox is unchecked ("You must agree to terms"). |
| s5 | Valid registration landing | **PASS** | `3.1_s5_valid_registration_profile_setup.png` | Successful registration of `SSTestUser` landing on Profile Setup onboarding screen. |
| s6 | Duplicate email error | **PASS** | `3.1_s6_duplicate_email_error.png` | Backend user collision error displayed: "This email address is already registered." |

*Verification Log:* `std-evidence/logs/auth_user_check.txt` confirms Firebase Auth UID creation.

---

### Module 3.2: User Login
| Step | Name | Status | Screenshot File | Description |
| :--- | :--- | :---: | :--- | :--- |
| s1 | Blank fields validation | **PASS** | `3.2_s1_blank_fields.png` | Inline errors displayed for blank email and password fields. |
| s2 | Wrong password | **PASS** | `3.2_s2_wrong_password.png` | Authentication error "Invalid email or password" displayed on incorrect credentials. |
| s3 | Unregistered email | **PASS** | `3.2_s3_unregistered_email.png` | Authentication error displayed for unregistered email account. |
| s4 | Valid login & Remember Me | **PASS** | `3.2_s4_valid_login_remember_me_dashboard.png` | Successful login with Remember Me enabled, landing on Home Dashboard. |
| s5 | Session restore after restart | **PASS** | `3.2_s5_app_reopened_session_restored.png` | App force-stopped and reopened; authenticated session restored directly to Home without login. |
| s6 | Logout with saved email | **PASS** | `3.2_s6_login_screen_saved_email.png` | After logout, Login screen pre-fills the saved email `primera.tester+reg@example.com`. |
| s7 | Google Sign-In | **SKIPPED-MANUAL** | *(Manual procedure)* | Explicitly skipped per user instruction (`skip gmail login`). See `manual-todo.md`. |

---

### Module 3.3: Profile Setup & Editing
| Step | Name | Status | Screenshot File | Description |
| :--- | :--- | :---: | :--- | :--- |
| s1 | Blank-field errors | **PASS** | `3.3_s1_blank_field_errors.png` | Validation blocks progress if birthday or required maternal measurements are blank. |
| s2 | Invalid values | **PASS** | `3.3_s2_invalid_values.png` | Validation rejects non-positive or out-of-range maternal height/weight entries. |
| s3 | Valid profile saved | **PASS** | `3.3_s3_valid_profile_saved.png` | Profile saved with calculated EDD (Aug 20, 2027), Trimester (1st), and Gestational Age (1w 0d). |
| s4 | Profile after reopening | **PASS** | `3.3_s4_profile_after_reopening.png` | Profile screen persists saved data across cold app relaunch. |
| s5 | Edited profile | **PASS** | `3.3_s5_edited_profile.png` | Pre-pregnancy weight edited to 60 kg and verified on Profile screen. |

---

### Module 3.4: Smartwatch Connection
| Step | Name | Status | Screenshot File | Description |
| :--- | :--- | :---: | :--- | :--- |
| s1 | Connect Smartwatch screen | **PASS** | `3.4_s1_connect_smartwatch_screen.png` | Device connection landing screen displaying watch and phone pairing affordance. |
| s2 | Permission request sheet | **SKIPPED-MANUAL** | *(Manual procedure)* | Emulator cannot pair real watch; Health Connect package check fails on API 34+. See `manual-todo.md`. |
| s3 | Permission denied message | **SKIPPED-MANUAL** | *(Manual procedure)* | Requires physical device with Health Connect permissions sheet. See `manual-todo.md`. |
| s4 | Permissions granted / connected | **SKIPPED-MANUAL** | *(Manual procedure)* | Physical watch pairing cannot be emulated over Bluetooth. See `manual-todo.md`. |
| s5 | Health Connect unavailable state | **PASS** | `3.4_s5_health_connect_unavailable.png` | App displays "Health Connect is not installed on this device" banner on API 34+. |

---

### Module 3.5: Smartwatch Health Data
| Step | Name | Status | Screenshot File | Description |
| :--- | :--- | :---: | :--- | :--- |
| s1 | Vitals summary cards | **SKIPPED-MANUAL** | *(Manual procedure)* | Live watch sensor ingestion requires physical device. See `manual-todo.md`. |
| s2 | Weekly trend charts | **SKIPPED-MANUAL** | *(Manual procedure)* | Live chart population from watch telemetry requires physical device. See `manual-todo.md`. |
| s3 | Health alert indicator | **SKIPPED-MANUAL** | *(Manual procedure)* | Abnormal sensor trigger requires physical device. See `manual-todo.md`. |
| s4 | Day with no data | **PASS** | `3.5_s4_day_with_no_data.png` | Dashboard vitals cards display fallback state (`-- bpm`, `-- steps`, `-- sleep`, `-- %`). |

---

### Module 3.6: Speech to Text (Transcription)
| Step | Name | Status | Screenshot File | Description |
| :--- | :--- | :---: | :--- | :--- |
| s1 | Mic permission request & denial | **PASS** | *(Verified with system dialog)* | System permission dialog blocks audio recording when refused. |
| s2 | Permission granted, listening state | **PASS** | `3.6_s2_permission_granted_listening_state.png` | Pulsating listening animation and "Listening..." indicator active. |
| s3 | Transcription text field | **SKIPPED-MANUAL** | *(Manual procedure)* | Emulator virtual audio returns speech error code 11. See `manual-todo.md`. |
| s4 | Symptom tags | **SKIPPED-MANUAL** | *(Manual procedure)* | Dependent on acoustic transcription. See `manual-todo.md`. |
| s5 | Edited text saved | **SKIPPED-MANUAL** | *(Manual procedure)* | Dependent on acoustic transcription. See `manual-todo.md`. |
| s6 | Saved entry in check-in | **SKIPPED-MANUAL** | *(Manual procedure)* | Dependent on acoustic transcription. See `manual-todo.md`. |

---

### Module 3.7: Daily Check-in Logging
| Step | Name | Status | Screenshot File | Description |
| :--- | :--- | :---: | :--- | :--- |
| s1 | Blank & invalid weight validation | **PASS** | `3.7_s1_blank_and_invalid_weight_validation.png` | Validation blocks submission when required weight or vital ranges are violated. |
| s2 | Completed check-in form | **PASS** | `3.7_s2_completed_form.png` | Completed daily check-in with weight (60 kg), blood sugar (95 mg/dL), nausea, happy mood, and notes. |
| s3 | Submission confirmation | **PASS** | `3.7_s3_submission_confirmation.png` | Review Check-in sheet displays summary before final commit. |
| s4 | Calendar status and streak | **PASS** | `3.7_s4_calendar_status_and_streak.png` | Calendar date status marked completed for current day. |
| s5 | New entry in log history | **PASS** | `3.7_s5_new_entry_in_log_history.png` | Newly recorded daily check-in rendered in Daily Check-Ins history list. |

---

### Module 3.8: Check-in Log History
| Step | Name | Status | Screenshot File | Description |
| :--- | :--- | :---: | :--- | :--- |
| s1 | History list (5 seeded entries) | **PASS** | `3.8_s1_history_list.png` | Chronological list showing 5 distinct check-in entries across Nov 13, 12, 11, 10, 9. |
| s2 | Search filter results | **PASS** | `3.8_s2_search_results.png` | Query "Headache" correctly filters history to matching Nov 10 entry. |
| s3 | No-results message | **PASS** | `3.8_s3_no_results_message.png` | Empty search state displays "No daily check-ins found." |
| s4 | Category & date filter sheet | **PASS** | `3.8_s4_date_range_filter.png` | Category filter bottom sheet allows filtering between Prenatal Visits and Self-Reported Check-ins. |
| s5 | Entry preview sheet | **PASS** | `3.8_s5_entry_preview.png` | Daily Check-In Details sheet displays recorded vitals, symptoms, mood, and notes. |
| s6 | Edit check-in saved | **PASS** | `3.8_s6_edit_saved.png` | Note updated and confirmed; saved entry reflects updated note in history list. |
| s7 | Delete confirmation dialog (cancel) | **FAIL** | *(No screenshot)* | **Defect:** No delete action or confirmation dialog exists in the daily check-in UI. |
| s8 | Delete confirmed and entry removed | **FAIL** | *(No screenshot)* | **Defect:** Daily check-in deletion is not exposed in the app UI. |
| s9 | Empty-state history account | **PASS** | `3.8_s9_empty_state_account.png` | Displays "No records" placeholder before entries are created. |

---

### Module 3.9: Maternal Dashboard
| Step | Name | Status | Screenshot File | Description |
| :--- | :--- | :---: | :--- | :--- |
| s1 | Dashboard with full data | **PASS** | `3.9_s1_dashboard_with_full_data.png` | Gestation countdown (280 days left), trimester badge (1st), gestational age (1w 0d), and baby size. |
| s2 | Dashboard without data (empty) | **PASS** | `3.9_s2_dashboard_without_data.png` | Clean unpopulated dashboard state captured on freshly authenticated account. |
| s3 | Dashboard after new check-in | **PASS** | `3.9_s3_dashboard_after_new_checkin.png` | StatsGrid carousel card shows vitals state and daily progress indicators. |

---

### Module 3.10: Insights and Goals
| Step | Name | Status | Screenshot File | Description |
| :--- | :--- | :---: | :--- | :--- |
| s1 | Progress score cards | **PASS** | `3.10_s1_progress_score_cards.png` | Gestational age progress, active wellness goals (0/2), and weekly insights narrative. |
| s2 | Goal checked / updated | **PASS** | `3.10_s2_goal_checked.png` | Movement goal progress updated to 28.4 / 30.0 min with full visual progress fill. |
| s3 | Goal unchecked / reset | **PASS** | `3.10_s3_goal_unchecked.png` | Movement goal progress reset to 0 min with empty progress bar. |
| s4 | Goals persist after restart | **PASS** | `3.10_s4_goals_after_restart.png` | Goal state persists across full application cold restart. |
| s5 | Custom goal added | **PASS** | `3.10_s5_custom_goal_added.png` | New custom goal (Meditation / Yoga, 20 min) successfully added to active goals list. |
| s6 | Empty custom goal rejected | **PASS** | `3.10_s6_empty_custom_goal_rejected.png` | Add Goal button disabled when Goal Title is blank (`isValid == false`). |

---

### Module 3.17: Notifications Screen & Navigation
| Step | Name | Status | Screenshot File | Description |
| :--- | :--- | :---: | :--- | :--- |
| s1 | Notifications screen | **PASS** | `3.17_s1_notifications_screen.png` | Filter chips (All, Check-ins, Missed Days, Pregnancy, Appointments) and empty state advisory. |

---

### Module 3.18: Reminder Settings & Time Picker
| Step | Name | Status | Screenshot File | Description |
| :--- | :--- | :---: | :--- | :--- |
| s1 | Reminder settings screen | **PASS** | `3.18_s1_reminder_settings_screen.png` | Morning Check-in (9:00 AM), Hydration (2:00 PM), Evening Wind-down (8:00 PM) toggles. |
| s2 | Time picker dialog | **PASS** | `3.18_s2_time_picker_dialog.png` | Material 3 dial TimePicker dialog for modifying reminder schedule. |

---

### Module 3.19: Scheduled Reminders (AlarmManager)
| Step | Name | Status | Screenshot File | Description |
| :--- | :--- | :---: | :--- | :--- |
| s1 | Alarm synchronization | **PASS** | *(Verified via system dumpsys)* | `ReminderManager` registers daily exact alarms via AlarmManager for scheduled reminders. |

---

### Module 3.20: Deep Links & Intent Routing
| Step | Name | Status | Screenshot File | Description |
| :--- | :--- | :---: | :--- | :--- |
| s1 | Deep link to Check-ins | **PASS** | `3.20_s1_deeplink_checkin.png` | Intent with `--es extra_destination_route checkin` directly navigates to Check-Ins overview. |
| s2 | Deep link to Smartwatch | **PASS** | `3.20_s2_deeplink_device.png` | Intent with `--es extra_destination_route device` directly navigates to Smartwatch screen. |
| s3 | Deep link to Profile | **PASS** | `3.20_s3_deeplink_profile.png` | Intent with `--es extra_destination_route profile` directly navigates to My Profile screen. |

---

### Module 3.21: Performance & Startup Times
| Step | Name | Status | Screenshot File | Description |
| :--- | :--- | :---: | :--- | :--- |
| s1 | Startup benchmarking | **PASS** | `3.21_s1_performance_startup.png` | Measured cold start (3529 ms avg) and warm start (150 ms avg). Benchmark log in `logs/performance.csv`. |

---

### Module 3.22: Intent & Component Security Audit
| Step | Name | Status | Screenshot File | Description |
| :--- | :--- | :---: | :--- | :--- |
| s1 | Manifest security audit | **PASS** | *(Detailed in log)* | Complete audit of exported components and permissions saved in `logs/intent_security_audit.txt`. |

---

### Module 3.23: Offline Mode & Network Resilience
| Step | Name | Status | Screenshot File | Description |
| :--- | :--- | :---: | :--- | :--- |
| s1 | Offline banner triggered | **PASS** | `3.23_s1_offline_banner.png` | Toggling wifi/data off triggers OfflineBanner: "You're offline. Some data might be out of date." |
| s2 | Online state restored | **PASS** | `3.23_s2_online_restored.png` | Restoring network immediately removes offline banner and resumes background sync. |
