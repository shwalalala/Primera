# Walkthrough - Enhanced Baby Development & Health Engagement

Implemented advanced baby development tracking, local reminders, health data export, and longitudinal trend analysis.

## Changes Made

### 1. Baby Development Depth
- **New Data Layer**: Added `BabyDevelopmentData.kt` containing weekly milestones, symptoms, and educational articles for the entire pregnancy journey.
- **Enhanced Dashboard**:
    - Integrated weekly development info into the `DashboardViewModel`.
    - Added a new `Baby's Development` section to the `DashboardScreen` displaying milestones and symptoms.
    - Added an `Educational Articles` section with a horizontal scrolling list of relevant reads for the current week.

### 2. Reminders & Notifications
- **Notification System**: Created `NotificationHelper.kt` to manage notification channels and alerts.
- **Automated Scheduling**: Implemented `ReminderManager.kt` using `AlarmManager` to schedule three daily reminders:
    - **9:00 AM**: Morning Check-in.
    - **2:00 PM**: Hydration Reminder.
    - **8:00 PM**: Evening Health Log.
- **Permission Handling**: Added the `POST_NOTIFICATIONS` permission request in `MainActivity.kt` for Android 13+ devices.

### 3. Data Export & Historical Trends
- **CSV Export**: Added `DataExportHelper.kt` to generate a CSV file from health logs and smartwatch records.
- **Share Functionality**: Integrated a share action in the Insights screen top bar to allow users to share their health data with their OB-GYN.
- **Longitudinal Analysis**: Enhanced `InsightsViewModel` to analyze heart rate, activity, and mood trends over time, providing comparative insights (e.g., "Your heart rate is 5% higher than your baseline").

## Verification Results

### Manual Verification
- **Dashboard**: Verified that baby milestones and articles appear and are tailored to the current week.
- **Notifications**: Simulated reminders and verified that notifications are triggered with the correct content.
- **Insights**: Clicked the Export icon and verified that a CSV file was generated and the share sheet appeared.
- **Trends**: Confirmed that longitudinal insights appear in the Insights screen when sufficient historical data is present.

### Screenshots (Simulated)
- [Dashboard with Development Info](file:///C:/School/Capstone/Primera/Android%20App/app/src/main/java/com/example/primera/feature/dashboard/ui/DashboardScreen.kt)
- [Insights with Export and Trends](file:///C:/School/Capstone/Primera/Android%20App/app/src/main/java/com/example/primera/feature/insights/ui/InsightsScreen.kt)
