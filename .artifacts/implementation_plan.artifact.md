# Implementation Plan - Completing Missing Features

This plan addresses the remaining missing features in the Primera application, excluding 3D illustrations.

## Proposed Changes

### 1. User Profile: Due Date Editing
Implement a Date Picker for the Due Date field in the Profile screen to allow users to update their expected due date.

#### [MODIFY] [ProfileScreen.kt](file:///C:/School/Capstone/Primera/Android App/app/src/main/java/com/example/primera/feature/profile/ui/ProfileScreen.kt)
- Integrate `DatePickerDialog` or Compose `DatePicker` for the Due Date field.
- Make the Due Date field clickable when in editing mode.

### 2. Account Security Settings
Implement password change and email update functionality.

#### [MODIFY] [AuthDataSource.kt](file:///C:/School/Capstone/Primera/Android App/app/src/main/java/com/example/primera/feature/auth/data/AuthDataSource.kt)
- Add `updateEmail(newEmail: String)` and `updatePassword(newPassword: String)`.

#### [MODIFY] [AuthRepository.kt](file:///C:/School/Capstone/Primera/Android App/app/src/main/java/com/example/primera/feature/auth/data/AuthRepository.kt)
- Add `updateEmail(newEmail: String): Result<Unit>` and `updatePassword(newPassword: String): Result<Unit>`.

#### [MODIFY] [AuthRepositoryImpl.kt](file:///C:/School/Capstone/Primera/Android App/app/src/main/java/com/example/primera/feature/auth/data/AuthRepositoryImpl.kt)
- Implement the new repository methods.

#### [NEW] [SettingsScreen.kt](file:///C:/School/Capstone/Primera/Android App/app/src/main/java/com/example/primera/feature/profile/ui/SettingsScreen.kt)
- Create a new screen for account settings (Change Email, Change Password, Logout).

#### [NEW] [SettingsViewModel.kt](file:///C:/School/Capstone/Primera/Android App/app/src/main/java/com/example/primera/feature/profile/ui/SettingsViewModel.kt)
- Handle the logic for updating security settings.

#### [MODIFY] [Routes.kt](file:///C:/School/Capstone/Primera/Android App/app/src/main/java/com/example/primera/core/navigation/Routes.kt)
- Add `const val SETTINGS = "settings"`.

#### [MODIFY] [AppNavGraph.kt](file:///C:/School/Capstone/Primera/Android App/app/src/main/java/com/example/primera/core/navigation/AppNavGraph.kt)
- Register the `SettingsScreen`.
- Add navigation from `ProfileScreen` to `SettingsScreen`.

### 3. Baby Development Content Expansion
Expand the static content in `BabyDevelopmentData.kt` to cover more weeks with realistic developmental milestones and articles.

#### [MODIFY] [BabyDevelopmentData.kt](file:///C:/School/Capstone/Primera/Android App/app/src/main/java/com/example/primera/feature/dashboard/domain/BabyDevelopmentData.kt)
- Populate `weeklyContent` with data for a wider range of weeks (e.g., every 4 weeks at minimum, or all 40).

### 4. Offline UI Indicator
Add a visual indicator when the app is offline.

#### [NEW] [ConnectivityObserver.kt](file:///C:/School/Capstone/Primera/Android App/app/src/main/java/com/example/primera/core/util/ConnectivityObserver.kt)
- Utility to observe network status.

#### [MODIFY] [DashboardScreen.kt](file:///C:/School/Capstone/Primera/Android App/app/src/main/java/com/example/primera/feature/dashboard/ui/DashboardScreen.kt)
- Show a small "Offline" badge or banner when there is no internet connection.

## Verification Plan

### Automated Tests
- N/A for UI changes, but I will ensure the project builds.

### Manual Verification
- **Profile**: Verify the Date Picker appears and updates the Due Date.
- **Settings**: Verify password and email updates (simulated or via Firebase).
- **Dashboard**: Check for the "Offline" indicator by disabling internet on the device/emulator.
- **Content**: Browse different weeks (if possible by changing due date) to see expanded development info.
