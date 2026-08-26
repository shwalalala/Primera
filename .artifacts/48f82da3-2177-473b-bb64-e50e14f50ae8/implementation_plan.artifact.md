# Implementation Plan - Enhanced Baby Development & Health Engagement

Add detailed baby development information, local notifications for reminders, data export functionality, and longitudinal health trend analysis.

## User Review Required

> [!IMPORTANT]
> The "Data Export" feature will be implemented as a CSV export for this phase, as requested (optional/placeholder for direct doctor sync).
> Notifications will be local-only for now, scheduled via `AlarmManager`.

## Proposed Changes

### 1. Baby Development Depth
Enhance the dashboard with weekly milestones, symptoms, and educational content.

#### [NEW] [BabyDevelopmentData.kt](file:///C:/School/Capstone/Primera/Android App/app/src/main/java/com/example/primera/feature/dashboard/domain/BabyDevelopmentData.kt)
- Define a data structure for weekly milestones, symptoms, and articles.
- Populate with sample content for all 40 weeks.

#### [MODIFY] [DashboardBusinessLogic.kt](file:///C:/School/Capstone/Primera/Android App/app/src/main/java/com/example/primera/feature/dashboard/domain/DashboardBusinessLogic.kt)
- Add functions `getWeeklyMilestones(week)`, `getWeeklySymptoms(week)`, and `getWeeklyArticles(week)`.

#### [MODIFY] [DashboardUiState.kt](file:///C:/School/Capstone/Primera/Android App/app/src/main/java/com/example/primera/feature/dashboard/ui/DashboardUiState.kt)
- Update `DashboardUiModel` to include `milestones: List<String>`, `symptoms: List<String>`, and `articles: List<Article>`.

#### [MODIFY] [DashboardViewModel.kt](file:///C:/School/Capstone/Primera/Android App/app/src/main/java/com/example/primera/feature/dashboard/ui/DashboardViewModel.kt)
- Map the new fields in `mapToUiModel` using `DashboardBusinessLogic`.

#### [MODIFY] [DashboardScreen.kt](file:///C:/School/Capstone/Primera/Android App/app/src/main/java/com/example/primera/feature/dashboard/ui/DashboardScreen.kt)
- Add a new `BabyDevelopmentSection` component to display milestones and symptoms.
- Add an "Educational Articles" horizontal list.

---

### 2. Reminders & Notifications
Implement local notifications for daily check-ins and hydration/movement goals.

#### [NEW] [NotificationHelper.kt](file:///C:/School/Capstone/Primera/Android App/app/src/main/java/com/example/primera/core/notification/NotificationHelper.kt)
- Utility to create notification channels and show notifications.

#### [NEW] [ReminderReceiver.kt](file:///C:/School/Capstone/Primera/Android App/app/src/main/java/com/example/primera/core/notification/ReminderReceiver.kt)
- `BroadcastReceiver` to handle alarm intents and trigger notifications.

#### [NEW] [ReminderManager.kt](file:///C:/School/Capstone/Primera/Android App/app/src/main/java/com/example/primera/core/notification/ReminderManager.kt)
- Logic to schedule daily reminders (e.g., 9 AM for check-in, 2 PM for hydration).

#### [MODIFY] [AndroidManifest.xml](file:///C:/School/Capstone/Primera/Android App/app/src/main/AndroidManifest.xml)
- Add `POST_NOTIFICATIONS` and `SCHEDULE_EXACT_ALARM` permissions.
- Register `ReminderReceiver`.

#### [MODIFY] [MainActivity.kt](file:///C:/School/Capstone/Primera/Android App/app/src/main/java/com/example/primera/MainActivity.kt)
- Initialize reminders on app start (or check permissions).

---

### 3. Data Export & Historical Trends
Add CSV export and advanced longitudinal analysis.

#### [NEW] [DataExportHelper.kt](file:///C:/School/Capstone/Primera/Android App/app/src/main/java/com/example/primera/core/util/DataExportHelper.kt)
- Logic to convert health records and logs into a CSV string.

#### [MODIFY] [InsightsViewModel.kt](file:///C:/School/Capstone/Primera/Android App/app/src/main/java/com/example/primera/feature/insights/ui/InsightsViewModel.kt)
- Add `onExportData()` function to trigger export.
- Implement advanced longitudinal analysis logic (comparing current week/month averages with historical baselines).
- Update `InsightsUiModel` to include "Longitudinal Insights" (e.g., "Your heart rate is 5% lower than your Trimester 1 average").

#### [MODIFY] [InsightsScreen.kt](file:///C:/School/Capstone/Primera/Android App/app/src/main/java/com/example/primera/feature/insights/ui/InsightsScreen.kt)
- Add an "Export" icon to the `FeatureTopBar`.
- Display longitudinal insights in a new card or within the existing circular progress section.

## Verification Plan

### Automated Tests
- Unit tests for `DashboardBusinessLogic` to ensure correct baby info per week.
- Unit tests for `InsightsViewModel` longitudinal analysis logic.

### Manual Verification
- Verify baby development info updates when changing due date (if possible in UI).
- Trigger a reminder manually (by setting a short delay) and verify notification appears.
- Click "Export" in Insights and verify a CSV is generated (and shared/saved).
- Verify "Longitudinal Insights" text appears and makes sense based on mock data.
