# Implementation Plan - Irregular Cycle Support in Onboarding

Enhance the onboarding flow to support users with irregular cycles by capturing cycle regularity and variance, and adjusting the gestational age logic to prioritize ultrasound data for irregular cycles.

## User Review Required

> [!IMPORTANT]
> This change introduces two new steps to the onboarding flow: **Cycle Regularity** and **Cycle Variance**. These steps will appear after the basic vitals (Weight/Height) and before the Last Menstrual Period (LMP) step.

> [!NOTE]
> For users with irregular cycles, the app will now capture the shortest and longest cycle lengths over the past 3-6 months to better estimate fertile windows and prioritize ultrasound dating.

## Proposed Changes

### [Onboarding Data Models]

#### [MODIFY] [OnboardingData.kt](file:///C:/Users/Nina/Documents/CODING%20PROJECTS/Primera/app/src/main/java/cit/edu/primera/feature/onboarding/domain/OnboardingData.kt)
- Add `isCycleRegular`, `shortestCycleDays`, and `longestCycleDays` to `OnboardingProfile`.

#### [MODIFY] [OnboardingUiState.kt](file:///C:/Users/Nina/Documents/CODING%20PROJECTS/Primera/app/src/main/java/cit/edu/primera/feature/onboarding/ui/OnboardingUiState.kt)
- Add `CYCLE_REGULARITY` and `CYCLE_VARIANCE` to `OnboardingStep` enum.
- Add `isCycleRegular`, `shortestCycleDays`, and `longestCycleDays` to `OnboardingState`.

---

### [Onboarding Logic]

#### [MODIFY] [OnboardingViewModel.kt](file:///C:/Users/Nina/Documents/CODING%20PROJECTS/Primera/app/src/main/java/cit/edu/primera/feature/onboarding/ui/OnboardingViewModel.kt)
- Add handler functions for cycle regularity and variance.
- Update `nextStep()` and `previousStep()` to include the new steps and handle branching (skip `CYCLE_VARIANCE` if cycle is regular).
- Update `saveAndFinish()` to include the new fields in the `OnboardingProfile`.

#### [MODIFY] [OnboardingRepositoryImpl.kt](file:///C:/Users/Nina/Documents/CODING%20PROJECTS/Primera/app/src/main/java/cit/edu/primera/feature/onboarding/data/OnboardingRepositoryImpl.kt)
- Update `saveProfile()` to include the new cycle-related fields in the Firestore document.

---

### [Onboarding UI]

#### [MODIFY] [OnboardingScreen.kt](file:///C:/Users/Nina/Documents/CODING%20PROJECTS/Primera/app/src/main/java/cit/edu/primera/feature/onboarding/ui/OnboardingScreen.kt)
- Implement `CycleRegularityStep` composable.
- Implement `CycleVarianceStep` composable.
- Integrate the new steps into `OnboardingHostScreen`.
- Add previews for the new steps.

## Verification Plan

### Automated Tests
- Run `:app:compileDebugKotlin` to ensure no regression in the build.
- (Optional) Add unit tests for `OnboardingViewModel` to verify branching logic for regular vs. irregular cycles.

### Manual Verification
- Deploy the app and walk through the onboarding flow.
- Verify that selecting "Irregular" cycle regularity prompts for shortest and longest cycle lengths.
- Verify that selecting "Regular" cycle regularity skips the variance step.
- Check Firestore to ensure `isCycleRegular`, `shortestCycleDays`, and `longestCycleDays` are correctly saved.
