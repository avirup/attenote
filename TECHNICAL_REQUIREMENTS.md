# Technical Requirements Document (TRD)

Date: February 13, 2026
Project: attenote

## 1. Objective
Define the technical architecture and implementation requirements to (re)build the current app behavior with stable, testable increments.

## 2. Platform and Toolchain
- Android: `minSdk 26`, `targetSdk 35`, `compileSdk 35`
- Kotlin: `2.2.10`, JVM target `17`
- AGP: `9.0.0`
- Build: Gradle Kotlin DSL

## 3. Core Stack
- UI: Jetpack Compose + Material 3
- Navigation: Navigation Compose typed routes (`@Serializable`)
- DI: Koin
- Local storage: Room + TypeConverters
- Preferences: DataStore Preferences
- Async: Coroutines + Flow
- Background cleanup: WorkManager
- Security: BiometricPrompt
- Media: Coil
- Rich text: `richeditor-compose`

## 4. Architecture
- Pattern: MVVM + layered data/domain/ui packages.
- Layers:
  - `data/`: Room entities, DAOs, repositories, backup/restore, normalization.
  - `domain/`: plain models + entity/domain mappers.
  - `ui/`: screens, ui state, navigation, theme components.
- Dependency wiring:
  - `AppModule`: Room, DAOs, DataStore, settings repo.
  - `RepositoryModule`: class/student/attendance/note/backup repositories.

## 5. System UI Requirement (Navigation Buttons)
- `TR-SYS-01` Android system navigation buttons must be visible.
- `TR-SYS-02` Do not hide navigation bars in window inset policy.
- `TR-SYS-03` Keep status bar visible and use standard insets-safe layout behavior.
- Note: current code hides nav bars in `MainActivity.applySystemBarPolicy`; rebuild must correct this.

## 6. Navigation Contract
Routes to implement:
- `Splash`
- `Setup`
- `AuthGate`
- `Dashboard`
- `CreateClass`
- `ManageClassList`
- `EditClass(classId)`
- `ManageStudents`
- `TakeAttendance(classId, scheduleId, date: yyyy-MM-dd)`
- `AddNote(date: yyyy-MM-dd, noteId = -1)`
- `Settings`

Route constraints:
- Date route parameters must be ISO-8601 (`yyyy-MM-dd`) and validated.

## 7. Data Model Requirements
Entities (Room):
- `classes`
- `students`
- `class_student_cross_ref`
- `schedules`
- `attendance_sessions`
- `attendance_records`
- `notes`
- `note_media`

Key constraints:
- Unique class key across institute/session/department/semester/subject/section.
- Unique student key across name + registrationNumber.
- Unique attendance session per class+schedule+date.
- Unique attendance record per session+student.
- Note media FK uses `ON DELETE CASCADE`.

Normalization/validation requirements:
- Normalize storage text inputs (trim + collapse whitespace).
- Reject invalid date ranges and overlapping schedules.
- Enforce schedule day/time consistency for attendance save.

## 8. Repository Contracts
- Class repository:
  - transactionally create class + schedules
  - validate duplicate class + overlapping slots
- Student repository:
  - create/update with duplicate detection by normalized unique key
- Attendance repository:
  - idempotent save by class+schedule+date
  - overwrite attendance records for target session on save
- Note repository:
  - create note + media transactionally
  - edit note + append media
  - best-effort cleanup of unreferenced media on delete
- Backup repository:
  - export/import zip with checksum manifest
  - staged restore with rollback/recovery journal

## 9. UI/Design System Requirements
- Use one global theme and shared themed components (`AttenoteTopAppBar`, `AttenoteSectionCard`, etc.).
- Keep a minimal colorful palette (navy/teal and neutral surfaces).
- Keep top app bars consistent (`titleMedium`, back icon semantics, save text actions).
- Maintain consistent tone usage for cards and section grouping.

## 10. Feature Requirements by Module
- Setup:
  - required name
  - optional institute/profile image
  - optional biometric lock only if device security is enabled
- Dashboard:
  - selected-date state
  - scheduled classes and notes for same date
  - add/open note actions
  - quick action menu to class/student/settings flows
- Class management:
  - create class with weekly schedule slots
  - manage class open state
  - edit class date range and class roster sourcing options
- Student management:
  - search/filter
  - add/edit fields
  - active toggle
- Attendance:
  - per student present toggle
  - lesson note text
  - save flow with confirmation messaging
- Notes:
  - rich text formatting toolbar
  - date change on note screen
  - pending media attach/remove + saved media preview
  - save and autosave-on-exit behavior
- Settings:
  - profile and biometric prefs
  - session format preference
  - backup export/import confirmations

## 11. Non-Functional Requirements
- Offline-first operation for core workflows.
- No app crash during normal CRUD operations.
- Input and repository validation error messages must be user-readable.
- Backups must be integrity-checked before import.
- Accessibility:
  - content descriptions for actionable icons/toggles
  - readable color contrast

## 12. Incremental Implementation Plan with Device Gates
Each phase is independently shippable and testable on a real device.

Phase 1: Bootstrap, theme, navigation skeleton
- Output: app launches; routes and placeholders wired.
- Gate: build/install/launch; system navigation buttons visible.

Phase 2: Data foundation (Room + DAOs + converters + repositories)
- Output: database and repository contracts compile.
- Gate: build/install/launch with no startup crash.

Phase 3: Setup + splash + auth gate
- Output: first-run flow and biometric gate behavior.
- Gate: complete setup, relaunch, confirm gated navigation.

Phase 4: Dashboard + calendar shell + quick actions
- Output: date state, calendar interactions, route handoffs.
- Gate: switch dates, open target screens from dashboard actions.

Phase 5: Create class and schedule slots
- Output: class creation with validation and persistence.
- Gate: create class and verify it appears in management list/dashboard.

Phase 6: Manage classes + edit class roster tools
- Output: open/close, date range edit, manual/CSV/copy student linking.
- Gate: edit class and confirm data changes persist after relaunch.

Phase 7: Manage students module
- Output: CRUD-like add/edit + search + active toggle.
- Gate: create/edit student and verify search/filter behavior.

Phase 8: Attendance module
- Output: save attendance session and records for scheduled class/date.
- Gate: take attendance and confirm persisted state via reopen flow.

Phase 9: Notes module (rich text + media)
- Output: add/edit note, toolbar formatting, media attachment.
- Gate: create note with image, reopen same note, verify content/media/date.

Phase 10: Settings + backup/restore + media cleanup worker
- Output: profile settings, session format, backup export/import.
- Gate: export backup, modify data, import backup, confirm restoration.

Phase 11: Consistency pass + regression checks
- Output: consistent UI patterns and stable navigation across app.
- Gate: full smoke test of all routes on physical device.

## 13. Test Command Baseline
- Build: `./gradlew :app:assembleDebug`
- Install: `adb install -r app/build/outputs/apk/debug/app-debug.apk`
- Launch: `adb shell monkey -p com.uteacher.attendancetracker -c android.intent.category.LAUNCHER 1`

## 14. Completion Criteria
- All phases pass device gate checks.
- Core business flows are usable offline.
- Navigation/buttons behavior is consistent and system nav buttons remain visible.
