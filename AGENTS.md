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

### V2 Sequential Execution
1. For `CODEX_REBUILD_PROMPTS_V2.md`, run prompts in strict order: `01 -> 02 -> ... -> 11`.
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
  `BUSINESS_REQUIREMENTS.md` > `TECHNICAL_REQUIREMENTS.md` > `CODEX_REBUILD_PROMPTS.md` > `CODEX_REBUILD_PROMPTS_V2.md` > `ImplementaionPlan.md`.
- System UI policy:
  keep Android navigation buttons visible, never hide navigation bars, keep status bar visible, and use `WindowCompat.setDecorFitsSystemWindows(window, true)`.
- Top ActionBar policy:
  use the Android top ActionBar as the global title/back surface, and reflect route title + back/up state there.
- Navigation policy:
  use typed route objects (`AppRoute`) end-to-end; do not use raw route strings.
- Biometric host policy:
  when using `BiometricPrompt` with an activity host, use `FragmentActivity`.

## V2 Addendum (Pre-Prompt Requirements)
- V2 prompt characteristics:
  self-contained, device-gated, incremental, and traceable with explicit done criteria + commit message.
- V2 scope requirements include:
  - class duration persisted and shown in compact format on Create Class slot list, Dashboard present-date class cards, and Take Attendance class info.
  - Take Attendance `Taken/Not Taken` behavior with `SKIPPED` normalization and reset to `PRESENT` when toggled back to `Taken`.
  - inactive students excluded from attendance marking.
  - lesson note in Take Attendance stays bottom-visible and autosaves draft.
  - student model supports optional `department` (stored as empty string when omitted).
  - Edit Student supports `registrationNumber` updates with confirm-and-merge conflict flow.
  - Manage Students keeps active above inactive and supports department + status filters; empty-department students are not shown.
  - CSV import supports header synonym matching for all supported fields with order-independent columns.
  - CSV import requires name + registration, deduplicates duplicate rows before preview, and indicates inactive linked students visibly.
  - CSV existing-student matching uses `registrationNumber + name`; inactive status is preserved when linked.
  - class/student delete are cascade hard deletes; note/media delete are permanent delete.
  - Dashboard notes cards hide `Created on`; notes viewer cards show only `Updated on`.
  - Daily Summary grouped by day plus read-only notes/media and attendance stats screens.
  - Notes Only Mode hides class/attendance surfaces and uses route guards for class/attendance routes.
- V2 route guard policy:
  keep class/attendance route guards in `AppNavHost` for Notes Only Mode; do not remove route registrations.
- V2 delete policy:
  class and student delete are irreversible cascade deletes after confirmation; class inactive toggle remains separate.
- V2 student edit/import policy:
  remove active toggle from Edit Student dialog, keep active/inactive action in Manage Students, and relink all `studentId` references during merge.
- V2 attendance visibility policy:
  Take Attendance shows only active students.

## V2 Database and Migration Policy
- No incremental migrations required during active development.
- Use `fallbackToDestructiveMigration()` on the Room database builder.
- When schema changes, use fresh install/cleared app data.
- Do not add migration objects or backfill logic in V2 prompts.
- Bump database version when schema columns change.

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
