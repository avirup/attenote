# AGENTS.md

## Rebuild Operating Context
- Date: February 13, 2026
- Project: `attenote` (`com.uteacher.attendancetracker`)
- Goal: Rebuild the app from scratch in strict, testable, device-validated increments.

## Document Overview
- The rebuild process uses 15 sequential prompts.
- Every prompt is self-contained, device-gated, incremental, and comprehensive.

## Rebuild Phases
- Phase 1 (Prompts 01-05): Foundation (Gradle, theme, navigation, Room, DAOs, repositories).
- Phase 2 (Prompts 06-09): Core flows (DI, splash/setup/auth, dashboard/calendar, class management).
- Phase 3 (Prompts 10-13): Feature completion (students, attendance, notes, settings, backup/restore).
- Phase 4 (Prompts 14-15): QA and hardening (UI consistency, regression, review, edge cases).

## Execution Rules
### Sequential Execution
1. Run prompts in strict order: `01 -> 02 -> ... -> 15`.
2. Use one prompt per AI conversation turn (copy full prompt block).
3. Prepend the Global Prefix to every prompt.
4. Do not skip steps.

### Device Gate Requirement
After every prompt:
1. Run the Standard Device Gate commands.
2. Execute that prompt's manual verification checklist.
3. Mark each checklist item pass/fail.
4. Proceed only when all critical items pass.

### Failure Handling
1. Do not proceed to the next prompt.
2. Fix the failing step.
3. Re-run device gate commands.
4. Re-verify checklist.
5. Continue only after pass.

### Version Control
- After each successful prompt, commit using the provided Git commit message.
- Keep one clean checkpoint per increment.

## Alignment Rules
- Conflict precedence:
  `BUSINESS_REQUIREMENTS.md` > `TECHNICAL_REQUIREMENTS.md` > `CODEX_REBUILD_PROMPTS.md` > `ImplementaionPlan.md`.
- System UI policy:
  keep Android navigation buttons visible, never hide navigation bars, keep status bar visible, and use `WindowCompat.setDecorFitsSystemWindows(window, true)`.
- Top ActionBar policy:
  use the Android top ActionBar as the global title/back surface, and reflect route title + back/up state there.
- Navigation policy:
  use typed route objects (`AppRoute`) end-to-end; do not use raw route strings.
- Biometric host policy:
  when using `BiometricPrompt` with an activity host, use `FragmentActivity`.

## Dependency Addendum
- Prompt 01 baseline:
  `activity-compose`, `lifecycle-runtime-compose`, `lifecycle-viewmodel-compose`, `navigation-compose`, `koin-android`, `koin-compose`, `room-runtime`, `room-ktx`, `room-compiler`, `datastore-preferences`, `kotlinx-coroutines-android`, `kotlinx-serialization-json`.
- Prompt 06: `androidx.biometric:biometric`.
- Prompt 09: `kotlin-csv-jvm`.
- Prompt 12: `io.coil-kt:coil-compose`, `richeditor-compose`.
- Prompt 13: `androidx.work:work-runtime-ktx`.

## Step Handoff Requirements
At the end of each prompt output, include:
1. Completed scope.
2. Deferred scope.
3. New dependencies/config changes.
4. Carry-over assumptions.

## Global Prefix
```text
Work only in this repository. Rebuild incrementally from scratch. Keep Android system navigation buttons visible (do not hide navigation bars). Keep UI consistent using one shared minimal colorful light theme (white/cream style). Use Android top ActionBar as the global title/back bar on all screens. Use Jetpack Compose + Material3 + Koin + Room + DataStore + Coroutines/Flow + BiometricPrompt + Coil + WorkManager where required by the step. Use typed AppRoute navigation (no raw route strings). End each step by running compile/install/launch and giving a manual verification checklist with pass/fail.
```

## Standard Device Gate
```bash
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell monkey -p com.uteacher.attendancetracker -c android.intent.category.LAUNCHER 1
```
