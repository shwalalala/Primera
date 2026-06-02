# Primera Project Workflow

This document outlines the standard development workflow for the Primera project to ensure code quality and consistent releases.

## 1. Branching Strategy
We use a simplified Git Flow model:
- **`main`**: Production-ready code. Only merged from `develop` after testing.
- **`develop`**: Integration branch for features. Base for all new work.
- **`feature/name`**: New features (e.g., `feature/smartwatch-sync`).
- **`fix/issue`**: Bug fixes (e.g., `fix/date-picker-layout`).
- **`chore/task`**: Non-functional updates (e.g., `chore/dependency-updates`).

## 2. Development Lifecycle
1.  **Start Task**: Create a branch from `develop`.
    ```bash
    git checkout develop
    git pull
    git checkout -b feature/your-feature-name
    ```
2.  **Commit Changes**: Follow Conventional Commits (e.g., `feat:`, `fix:`, `refactor:`, `chore:`).
3.  **Local Verification**:
    - Build: `./gradlew assembleDebug`
    - Test: `./gradlew testDebugUnitTest`
    - Lint: `./gradlew lintDebug`
4.  **Push & PR**: Push your branch and open a Pull Request (PR) to `develop`.
5.  **CI Check**: Our GitHub Action (`android_ci.yml`) will automatically verify your build and tests.

## 3. Deployment (Release)
- When `develop` is stable, merge it into `main`.
- Tag the release: `git tag -a v1.0.0 -m "Release version 1.0.0"`.

## 4. Manual Sync Procedures (Device Testing)
- **Huawei/Sandbox**: Always open the Health Connect app manually to initialize the service before performing a sync in Primera.
- **OHealth/Watch**: Use the "Open Watch App" shortcut in the Device screen to ensure Bluetooth data is pushed to Health Connect before syncing.

## 5. Coding Standards
- **UI**: Use Jetpack Compose and Material 3 components.
- **Architecture**: MVVM with Repository pattern.
- **Data**: Firestore for remote storage, SharedPreferences for user-specific local preferences.
