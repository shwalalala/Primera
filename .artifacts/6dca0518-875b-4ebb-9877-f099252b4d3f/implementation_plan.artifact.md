# Implementation Plan - Fixing Missing and Non-Functional Features

This plan addresses several "non-functional" or incomplete features identified in the codebase, including missing trend logic, broken weight tracking in insights, and placeholder content.

## User Review Required

> [!IMPORTANT]
> The weight tracking will now be appended to the check-in description to allow the Insights chart to correctly plot weight trends over time.

## Proposed Changes

### Core Logic Improvements

#### [MODIFY] [DashboardBusinessLogic.kt](file:///C:/School/Capstone/Primera/Android App/app/src/main/java/com/example/primera/feature/dashboard/domain/DashboardBusinessLogic.kt)
- Update `getBabyEmoji` with more distinct emojis for each week.
- Remove the TODO regarding 3D illustrations (as emojis are the current implementation).

#### [MODIFY] [SymptomExtractor.kt](file:///C:/School/Capstone/Primera/Android App/app/src/main/java/com/example/primera/feature/transcription/domain/SymptomExtractor.kt)
- Expand the symptom dictionary to include more common pregnancy-related symptoms like "cramping", "heartburn", "insomnia", and "spotting".

---

### Feature Updates

#### [MODIFY] [CheckinsViewModel.kt](file:///C:/School/Capstone/Primera/Android App/app/src/main/java/com/example/primera/feature/checkins/ui/CheckinsViewModel.kt)
- Update `onSaveCheckin` to include the user's weight in the log description (e.g., "Weight: 65 kg"). This fix ensures that `InsightsViewModel` can correctly extract weight data for historical charts.
- Update `loadCheckinForEdit` to correctly parse and remove the weight from the note field when editing.

#### [MODIFY] [DashboardViewModel.kt](file:///C:/School/Capstone/Primera/Android App/app/src/main/java/com/example/primera/feature/dashboard/ui/DashboardViewModel.kt)
- Implement `heartRateTrendingUp` and `heartRateVsLastWeek` logic.
- Calculate these values by comparing today's heart rate with the average from the past week's health records.
- Clean up unused empty methods.

#### [MODIFY] [InsightsViewModel.kt](file:///C:/School/Capstone/Primera/Android App/app/src/main/java/com/example/primera/feature/insights/ui/InsightsViewModel.kt)
- Refine `extractSmartwatchValues` for the "Daily" period to provide a better visualization of data points.
- Ensure weight extraction logic matches the new format from `CheckinsViewModel`.

## Verification Plan

### Automated Tests
- Run `gradle_build` to ensure no regressions in compilation.
- I will create a small scratch script to verify the trend calculation logic if needed.

### Manual Verification
- Deploy the app and verify:
    1.  Submitting a check-in with weight shows up in the Insights weight chart.
    2.  The Dashboard shows a heart rate trend (if historical data exists).
    3.  Baby emojis change correctly based on the week.
