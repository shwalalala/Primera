# Implementation Plan - Technical Polish and UI/UX Enhancements

This plan addresses the technical polish items (NLP for transcription, offline experience) and the UI/UX improvement (3D illustrations for baby size).

## User Review Required

> [!IMPORTANT]
> **Gemini API Key**: The AI-powered transcription requires a Google Gemini API key. I will implement the infrastructure, but you will need to provide your key in `local.properties` or a similar configuration.
> **3D Assets**: Since actual 3D `.glb` or high-quality `.png` assets for all 40 weeks are not yet in the project, I will implement the UI support and use `apple.png` (the only existing fruit asset) as a placeholder for the corresponding week to demonstrate the change.

## Proposed Changes

### 1. NLP/AI for Transcription
Upgrade the symptom extraction logic from simple keyword matching to LLM-based analysis.

#### [MODIFY] [libs.versions.toml](file:///C:/School/Capstone/Primera/Android App/gradle/libs.versions.toml)
- Add `generativeai = "0.9.0"` to `[versions]`.
- Add `google-generativeai = { group = "com.google.ai.client.generativeai", name = "generativeai", version.ref = "generativeai" }` to `[libraries]`.

#### [MODIFY] [build.gradle.kts](file:///C:/School/Capstone/Primera/Android App/app/build.gradle.kts)
- Add `implementation(libs.google.generativeai)` to dependencies.

#### [NEW] [SymptomExtractor.kt](file:///C:/School/Capstone/Primera/Android App/app/src/main/java/com/example/primera/feature/transcription/domain/SymptomExtractor.kt)
- Convert `object SymptomExtractor` to an `interface SymptomExtractor`.
- Implement `KeywordSymptomExtractor` (existing logic).
- Create `GeminiSymptomExtractor` using Google Generative AI SDK for robust NLP.

#### [MODIFY] [AppContainer.kt](file:///C:/School/Capstone/Primera/Android App/app/src/main/java/com/example/primera/core/di/AppContainer.kt)
- Provide the `SymptomExtractor` instance (preferring Gemini if configured).

---

### 2. Offline Experience
Enhance the Smartwatch sync experience with connectivity tracking and explicit offline states.

#### [NEW] [NetworkMonitor.kt](file:///C:/School/Capstone/Primera/Android App/app/src/main/java/com/example/primera/core/util/NetworkMonitor.kt)
- Utility to observe network connectivity using `ConnectivityManager`.

#### [MODIFY] [SmartwatchViewModel.kt](file:///C:/School/Capstone/Primera/Android App/app/src/main/java/com/example/primera/feature/smartwatchconnection/ui/SmartwatchViewModel.kt)
- Observe connectivity and update `SmartwatchUiState`.
- Handle sync attempts when offline with appropriate messaging.

#### [MODIFY] [SmartwatchScreen.kt](file:///C:/School/Capstone/Primera/Android App/app/src/main/java/com/example/primera/feature/smartwatchconnection/ui/SmartwatchScreen.kt)
- Add an `OfflineBanner` at the top of the data content.
- Disable the "Sync Now" button or change its text when offline.

---

### 3. 3D Illustrations
Transition from emojis to image-based illustrations in the Dashboard.

#### [MODIFY] [DashboardBusinessLogic.kt](file:///C:/School/Capstone/Primera/Android App/app/src/main/java/com/example/primera/feature/dashboard/domain/DashboardBusinessLogic.kt)
- Add `getBabyIllustration(week: Int): Int?` to return a drawable resource ID.

#### [MODIFY] [DashboardUiModel.kt](file:///C:/School/Capstone/Primera/Android App/app/src/main/java/com/example/primera/feature/dashboard/ui/DashboardUiModel.kt)
- Add `babyIllustration: Int?` field.

#### [MODIFY] [DashboardViewModel.kt](file:///C:/School/Capstone/Primera/Android App/app/src/main/java/com/example/primera/feature/dashboard/ui/DashboardViewModel.kt)
- Map the illustration resource ID from business logic to the UI model.

#### [MODIFY] [DashboardComponents.kt](file:///C:/School/Capstone/Primera/Android App/app/src/main/java/com/example/primera/feature/dashboard/ui/DashboardComponents.kt)
- Update `BabyRingCard` to accept `babyIllustration: Int?`.

#### [MODIFY] [ProgressIndicators.kt](file:///C:/School/Capstone/Primera/Android App/app/src/main/java/com/example/primera/ui/components/ProgressIndicators.kt)
- Update `CircularPregnancyRing` to display an `Image` if `babyIllustration` is provided, falling back to the emoji text.

## Verification Plan

### Automated Tests
- Build the project to verify dependency integration.
- Unit test for `KeywordSymptomExtractor` (regression testing).

### Manual Verification
- **Transcription**: Verify that symptoms are still extracted via keywords when Gemini is not configured.
- **Offline**: Disable Wi-Fi/Data and verify the "Offline" banner appears in the Smartwatch screen.
- **3D Illustrations**: Check week 15 on the Dashboard to see the `apple.png` illustration instead of an emoji.
