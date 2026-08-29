# Implement Check-in Preview and User Profile

This plan addresses two missing features:
1.  **Check-in Preview**: Integrating the review step before saving a daily check-in.
2.  **User Profile**: Creating a screen to view and update user information.

## User Review Required

> [!IMPORTANT]
> The Profile screen will allow updating fields like Name, Weight, Height, and Due Date. Updates will be saved directly to Firestore.

## Proposed Changes

### Core Navigation

#### [MODIFY] [Routes.kt](file:///C:/School/Capstone/Primera/Android App/app/src/main/java/com/example/primera/core/navigation/Routes.kt)
- Add `PROFILE` route.

#### [MODIFY] [AppNavGraph.kt](file:///C:/School/Capstone/Primera/Android App/app/src/main/java/com/example/primera/core/navigation/AppNavGraph.kt)
- Add `Routes.CHECKIN_PREVIEW` and `Routes.PROFILE` destinations.
- Update `DailyCheckinScreen` navigation logic to point to `Routes.CHECKIN_PREVIEW`.
- Pass navigation callback for profile from `DashboardScreen`.

---

### Check-in Feature

#### [MODIFY] [DailyCheckinScreen.kt](file:///C:/School/Capstone/Primera/Android App/app/src/main/java/com/example/primera/feature/checkins/ui/DailyCheckinScreen.kt)
- Ensure `onReview` is correctly triggered to navigate to the preview.

---

### Profile Feature

#### [NEW] [ProfileViewModel.kt](file:///C:/School/Capstone/Primera/Android App/app/src/main/java/com/example/primera/feature/profile/ui/ProfileViewModel.kt)
- Observe user data from `DashboardRepository`.
- Provide methods to update user profile using `OnboardingRepository` (or a dedicated repo if preferred).

#### [NEW] [ProfileScreen.kt](file:///C:/School/Capstone/Primera/Android App/app/src/main/java/com/example/primera/feature/profile/ui/ProfileScreen.kt)
- UI for displaying user details.
- Edit mode for updating Weight, Height, etc.

#### [NEW] [ProfileUiState.kt](file:///C:/School/Capstone/Primera/Android App/app/src/main/java/com/example/primera/feature/profile/ui/ProfileUiState.kt)
- State model for the profile screen.

---

### Dashboard Integration

#### [MODIFY] [DashboardComponents.kt](file:///C:/School/Capstone/Primera/Android App/app/src/main/java/com/example/primera/feature/dashboard/ui/DashboardComponents.kt)
- Add "Profile" option to `DashboardTopBar` dropdown menu.

#### [MODIFY] [DashboardScreen.kt](file:///C:/School/Capstone/Primera/Android App/app/src/main/java/com/example/primera/feature/dashboard/ui/DashboardScreen.kt)
- Add `onProfileClick` parameter and pass it to `DashboardTopBar`.

---

### Dependency Injection

#### [MODIFY] [ViewModelProvider.kt](file:///C:/School/Capstone/Primera/Android App/app/src/main/java/com/example/primera/core/di/ViewModelProvider.kt)
- Add factory logic for `ProfileViewModel`.

## Verification Plan

### Manual Verification
- **Check-in Preview**:
    - Open "Daily Check-in".
    - Fill in some data (symptoms, mood).
    - Click "Review".
    - Verify `CheckinPreviewScreen` is shown with the correct data.
    - Click "Confirm & Save".
    - Verify navigation back to Overview and that the log is saved.
- **User Profile**:
    - On Dashboard, open the top-left menu.
    - Click "Profile".
    - Verify navigation to `ProfileScreen`.
    - Change weight and click "Save".
    - Verify the updated weight appears on the Dashboard.
