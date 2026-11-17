#!/usr/bin/env bash
# ==============================================================================
# Primera Mobile Automated Test Suite Runner (STD M01 - M23)
# ==============================================================================
set -e

# Accept RUN_LABEL argument: 'before' or 'after' (defaults to 'before')
RUN_LABEL="${1:-before}"

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_ROOT"

echo "======================================================================"
echo " Starting Primera STD Automated Test Suite (Run Label: ${RUN_LABEL})"
echo "======================================================================"

REPORT_DIR="reports/${RUN_LABEL}"
SCREENSHOT_DIR="${REPORT_DIR}/screenshots"

# 1. Create output directories
mkdir -p "${SCREENSHOT_DIR}"

# 2. Check and start Firebase Emulators if firebase CLI is installed
if command -v firebase &> /dev/null; then
    echo "[1/6] Starting Firebase Auth & Firestore Emulators..."
    firebase emulators:start --only auth,firestore &
    FIREBASE_PID=$!
    sleep 5
else
    echo "[1/6] Firebase CLI not in PATH, continuing with direct backend / local cache..."
fi

# 3. Check for connected Android device/emulator
echo "[2/6] Checking Android emulator / device..."
if ! adb get-state &> /dev/null; then
    EMULATOR_BIN="emulator"
    if ! command -v emulator &> /dev/null; then
        if [ -n "$ANDROID_HOME" ] && [ -x "$ANDROID_HOME/emulator/emulator" ]; then
            EMULATOR_BIN="$ANDROID_HOME/emulator/emulator"
        elif [ -x "$HOME/AppData/Local/Android/Sdk/emulator/emulator" ]; then
            EMULATOR_BIN="$HOME/AppData/Local/Android/Sdk/emulator/emulator"
        fi
    fi
    AVD_NAME=$($EMULATOR_BIN -list-avds 2>/dev/null | head -n 1)
    if [ -n "$AVD_NAME" ]; then
        echo "Launching emulator: $AVD_NAME..."
        $EMULATOR_BIN -avd "$AVD_NAME" -no-snapshot-load -no-boot-anim -no-audio &
        EMULATOR_PID=$!
    else
        echo "No AVD found or adb device offline. Please start an emulator."
    fi
fi

echo "Waiting for Android device to finish booting..."
adb wait-for-device
while [ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" != "1" ]; do
    sleep 2
done
echo "Emulator is online and fully booted."

# Disable system animations for stable UI testing
adb shell settings put global window_animation_scale 0
adb shell settings put global transition_animation_scale 0
adb shell settings put global animator_duration_scale 0

# 4. Run connected Android test suite via Gradle
echo "[3/6] Running Android instrumented test suite via Gradle (excluding post-reboot tests)..."
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.notClass=cit.edu.primera.M19AfterRebootTest || echo "Some tests completed with warnings/failures."

# 5. Hook for M19 Reboot step
echo "[4/6] Executing M19 Reboot orchestration hook..."
echo "Running M19BeforeRebootTest before reboot..."
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=cit.edu.primera.M19BeforeRebootTest || echo "M19BeforeRebootTest completed with warnings/failures."

# Pull pre-reboot screenshots before restarting emulator
adb pull /storage/emulated/0/Android/data/cit.edu.primera/files/screenshots/. "${SCREENSHOT_DIR}/" 2>/dev/null || true
adb pull /sdcard/screenshots/. "${SCREENSHOT_DIR}/" 2>/dev/null || true

echo "Rebooting emulator via adb reboot..."
adb reboot || true
echo "Waiting for Android device to finish booting..."
adb wait-for-device
while [ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" != "1" ]; do
    sleep 3
done
echo "Emulator is online and fully booted. Unlocking device..."
adb shell input keyevent 82 || true
adb shell wm dismiss-keyguard || true
sleep 3

# Disable system animations for stable UI testing after reboot
adb shell settings put global window_animation_scale 0
adb shell settings put global transition_animation_scale 0
adb shell settings put global animator_duration_scale 0

echo "Running M19AfterRebootTest post-reboot verification..."
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=cit.edu.primera.M19AfterRebootTest || echo "M19AfterRebootTest completed with warnings/failures."

# 6. Pull screenshots and JUnit XML results
echo "[5/6] Pulling screenshots and collecting reports..."
adb pull /storage/emulated/0/Android/data/cit.edu.primera/files/screenshots/. "${SCREENSHOT_DIR}/" 2>/dev/null || true
adb pull /sdcard/screenshots/. "${SCREENSHOT_DIR}/" 2>/dev/null || true

# Copy JUnit XML
XML_FILE=$(find app/build/outputs/androidTest-results/connected/ -name "*.xml" | head -n 1)
if [ -n "$XML_FILE" ]; then
    cp "$XML_FILE" "${REPORT_DIR}/junit.xml"
    echo "JUnit test report copied to ${REPORT_DIR}/junit.xml"
fi

# 7. Cleanup
if [ -n "$FIREBASE_PID" ]; then
    kill "$FIREBASE_PID" 2>/dev/null || true
fi

echo "======================================================================"
echo " Test Suite Execution Complete!"
echo " Reports available at:"
echo "   - ${REPORT_DIR}/junit.xml"
echo "   - ${SCREENSHOT_DIR}/"
echo "======================================================================"
