# Implementation Plan - Re-verifying and Completing Missing Features

This plan addresses the gaps identified during the re-verification of the Primera application, focusing on onboarding, dashboard content, and UI consistency.

## Proposed Changes

### 1. Onboarding: Name Collection Integration
Enable the name collection step which is currently implemented but skipped in the flow.

#### [MODIFY] [OnboardingUiState.kt](file:///C:/School/Capstone/Primera/Android App/app/src/main/java/com/example/primera/feature/onboarding/ui/OnboardingUiState.kt)
- Add `NAME` to `OnboardingStep` enum.
- Set `NAME` as the default `currentStep` in `OnboardingState`.

#### [MODIFY] [OnboardingViewModel.kt](file:///C:/School/Capstone/Primera/Android App/app/src/main/java/com/example/primera/feature/onboarding/ui/OnboardingViewModel.kt)
- Update `nextStep()` to transition from `NAME` to `BIRTHDAY`.
- Update `previousStep()` to handle `NAME`.

#### [MODIFY] [OnboardingScreen.kt](file:///C:/School/Capstone/Primera/Android App/app/src/main/java/com/example/primera/feature/onboarding/ui/OnboardingScreen.kt)
- Add `OnboardingStep.NAME -> NameStep(state, viewModel)` to the `Crossfade` in `OnboardingHostScreen`.

---

### 2. Dashboard Content: Expansion
Expand the developmental milestones and articles to provide a better user experience throughout pregnancy.

#### [MODIFY] [BabyDevelopmentData.kt](file:///C:/School/Capstone/Primera/Android App/app/src/main/java/com/example/primera/feature/dashboard/domain/BabyDevelopmentData.kt)
- Add data for missing weeks (e.g., 6, 10, 14, 18, 22, 26, 30, 34, 38) to ensure the user sees progress more frequently.

#### [MODIFY] [DashboardBusinessLogic.kt](file:///C:/School/Capstone/Primera/Android App/app/src/main/java/com/example/primera/feature/dashboard/domain/DashboardBusinessLogic.kt)
- Add more illustration mappings if resources exist, or implement a fallback to a generic illustration based on trimester.

---

### 3. Smartwatch: Dynamic Trend Calculation
Replace hardcoded trend strings with actual calculations based on historical data.

#### [MODIFY] [SmartwatchUiState.kt](file:///C:/School/Capstone/Primera/Android App/app/src/main/java/com/example/primera/feature/smartwatchconnection/ui/SmartwatchUiState.kt)
- Add fields for `heartRateTrend`, `stepsTrendText`, etc., to the `SmartwatchUiState`.

#### [MODIFY] [SmartwatchViewModel.kt](file:///C:/School/Capstone/Primera/Android App/app/src/main/java/com/example/primera/feature/smartwatchconnection/ui/SmartwatchViewModel.kt)
- Implement logic to calculate the percentage change between the current day/week and the previous period.
- Update the UI state with these calculated values.

#### [MODIFY] [SmartwatchScreen.kt](file:///C:/School/Capstone/Primera/Android App/app/src/main/java/com/example/primera/feature/smartwatchconnection/ui/SmartwatchScreen.kt)
- Bind the calculated trends from `uiState` to the `HealthStatCard` components.

---

### 4. UI/UX: Offline Indicator & Reinforcement
Ensure consistent feedback across the app.

#### [MODIFY] [InsightsScreen.kt](file:///C:/School/Capstone/Primera/Android App/app/src/main/java/com/example/primera/feature/insights/ui/InsightsScreen.kt)
- Integrate `OfflineBanner` (already used in Dashboard) to show when the device is offline.

#### [MODIFY] [InsightsViewModel.kt](file:///C:/School/Capstone/Primera/Android App/app/src/main/java/com/example/primera/feature/insights/ui/InsightsViewModel.kt)
- Observe `NetworkMonitor` to provide `isOnline` state to the UI.

## Verification Plan

### Automated Tests
- Build the project to ensure no regressions.

### Manual Verification
- **Onboarding**: Start a new onboarding flow and verify the "Tell Us Your Name" screen appears first.
- **Dashboard**: Check developmental info for intermediate weeks (e.g., Week 6).
- **Smartwatch**: Verify trend percentages change based on mocked or real historical data.
- **Insights**: Disable internet and verify the "You're offline" banner appears.
