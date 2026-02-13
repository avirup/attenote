# Business Requirements Document (BRD)

Date: February 13, 2026
Project: attenote
Platform: Android (offline-first)

## 1. Purpose
Define business requirements for an offline-first teacher productivity app that manages classes, students, attendance, and daily notes from one dashboard.

## 2. Product Vision
Enable a teacher to run daily class operations quickly on a mobile device, without dependency on live internet.

## 3. Business Goals
- Reduce daily attendance and note-taking effort.
- Keep all core workflows available offline.
- Minimize data loss risk through local backup and restore.
- Provide a consistent interface across all screens.
- Keep UI minimal but colorful for readability and quick scanning.

## 4. Primary Users
- Teacher (primary user and device owner).
- School staff doing occasional backup/restore (secondary).

## 5. In Scope
- First-run setup with profile, institute, optional biometric lock.
- Dashboard with date-focused class schedule and daily notes.
- Class lifecycle: create, list/manage, edit date range, open/close class.
- Student lifecycle: create, edit, search, active/inactive control.
- Class roster operations: manual add, CSV import, copy from another class.
- Attendance capture per scheduled class/date with lesson notes.
- Rich-text daily notes with image attachments.
- Local backup export/import and interrupted-restore recovery.

## 6. Out of Scope (Current Release)
- Cloud sync and multi-user collaboration.
- Push notifications and server integrations.
- Advanced analytics dashboards.

## 7. Business Functional Requirements
- `BR-FR-01` Setup must block normal app use until required profile fields are saved.
- `BR-FR-02` Dashboard must show selected-date classes and selected-date notes.
- `BR-FR-03` User must be able to create a class with recurring weekly schedule slots.
- `BR-FR-04` User must be able to manage class open/closed state.
- `BR-FR-05` User must be able to maintain a global student list and per-class active state.
- `BR-FR-06` User must be able to take attendance per class schedule/date and save present/absent for each active class student.
- `BR-FR-07` User must be able to author and edit daily rich-text notes with optional images.
- `BR-FR-08` User must be able to export backup zip and import backup zip with clear confirmation.
- `BR-FR-09` If biometric lock is enabled, access must require device credential/biometric.

## 8. UX and Consistency Requirements
- `BR-UX-01` All screens must use one shared design system (theme tokens, typography, component patterns).
- `BR-UX-02` Use a minimal colorful theme (muted navy/teal family), avoiding visual noise.
- `BR-UX-03` Top app bar patterns and back navigation behavior must be consistent on all feature screens.
- `BR-UX-04` Android system navigation buttons must remain visible across the app.
- `BR-UX-05` Critical actions (Save, Export, Import, Attendance Save) must provide clear success/error feedback.

## 9. Device-Testable Delivery Requirement
- `BR-DEL-01` Every implementation step must end with a runnable debug build and physical-device validation.
- `BR-DEL-02` Each step must include:
  - compile: `./gradlew :app:assembleDebug`
  - install: `adb install -r app/build/outputs/apk/debug/app-debug.apk`
  - launch: `adb shell monkey -p com.uteacher.attendancetracker -c android.intent.category.LAUNCHER 1`
  - manual verification checklist for only that step
- `BR-DEL-03` Do not continue to the next step until the current step passes device checks.

## 10. Success Metrics
- Teacher can complete setup and reach dashboard within 2 minutes.
- Attendance capture for one class can be completed and saved in under 60 seconds.
- Note creation with one image can be completed and reopened successfully.
- Backup export and import complete without app crash and with data integrity preserved.

## 11. Risks and Mitigations
- Risk: data corruption during restore.
  - Mitigation: staged restore journal + rollback/recovery flow.
- Risk: inconsistent UX across rapidly added screens.
  - Mitigation: enforce shared themed components and acceptance checklist.
- Risk: navigation confusion.
  - Mitigation: consistent app bars/back actions and visible Android system nav buttons.
