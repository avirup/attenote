# Codex Rebuild Prompts V2 (Enhancements)

Date: February 14, 2026
Project: attenote (`com.uteacher.attendancetracker`)
Baseline: Existing rebuild through `CODEX_REBUILD_PROMPTS.md` (including Daily Summary)

This document provides comprehensive, copy-paste prompts to implement **Version 2 enhancements** in strict, testable, device-validated increments.

## Document Overview

This V2 guide consists of **11 sequential prompts**. Each prompt is:
- **Self-contained**: complete scope, architecture guidance, and acceptance checks
- **Device-gated**: must pass build/install/launch before continuing
- **Incremental**: designed to avoid broad regressions
- **Traceable**: each step has explicit done criteria and commit message

## V2 Scope Summary

Required V2 capabilities covered by this document:
- Calculated class duration field derived from schedule start/end time
- Take Attendance class-level state: `Taken` vs `Not Taken`
- Auto-mark all students as `Skipped` when class is not taken
- Greyed/disabled attendance-taking controls when class is not taken
- Sticky, always-visible lesson note at bottom of Take Attendance screen
- Search within Take Attendance student list
- Create Class layout update: semester dropdown + section field in same row
- Delete support for notes, classes, and students
- Soft delete for classes and students
- Permanent delete for notes and associated media
- Deletable saved media in note editor
- Daily Summary improved grouping by day
- Two read-only screens:
  - one to view notes with attached media
  - one to view attendance statistics with lesson notes
- Notes Only Mode:
  - Settings toggle to hide all class/attendance views
  - App behaves as a pure notes app when enabled
  - Dashboard shows only notes section
  - FAB menu shows only Settings
  - Daily Summary shows only note items
  - Navigation blocks class/attendance routes
  - No schema changes; purely UI-level gating
  - Backup/restore always includes full database regardless of mode

## Rebuild Phases

**Phase 1: Data and Contract Foundation (Prompts 01-03)**
- Schema updates, migration, repository contracts, deletion semantics

**Phase 2: Attendance and Class UX (Prompts 04-06)**
- Create Class layout/duration presentation, Take Attendance V2 behavior, sticky note footer

**Phase 3: Deletion + Summaries + Stats Views (Prompts 07-09)**
- Note/media hard delete UX, class/student soft delete UX, grouped daily summary and stats viewer

**Phase 4: Notes Only Mode (Prompt 10)**
- Settings toggle, Dashboard/FAB/DailySummary/navigation gating for notes-only experience

**Phase 5: QA and Hardening (Prompt 11)**
- Regression, migration validation, edge-case hardening, release readiness

## How to Use This File

### Sequential Execution
1. Run prompts in strict order: `01 -> 02 -> ... -> 11`
2. Use one prompt per AI conversation turn (copy full prompt block)
3. Prepend the Global Prefix to every prompt
4. Do not skip steps

### Device Gate Requirement
After every prompt:
1. Run the Standard Device Gate commands
2. Execute that prompt's manual verification checklist
3. Mark each checklist item pass/fail
4. Proceed only when all critical items pass

### Failure Handling
1. Do not proceed to the next prompt
2. Fix the failing step
3. Re-run device gate commands
4. Re-verify checklist
5. Continue only after pass

### Version Control
- After each successful prompt, commit using the provided Git commit message
- Keep one clean checkpoint per increment

### Alignment Rules (V2)
- Conflict precedence:
  `BUSINESS_REQUIREMENTS.md` > `TECHNICAL_REQUIREMENTS.md` > `CODEX_REBUILD_PROMPTS.md` > `CODEX_REBUILD_PROMPTS_V2.md` > `ImplementaionPlan.md`.
- System UI policy:
  keep Android navigation buttons visible, never hide navigation bars, keep status bar visible, and use `WindowCompat.setDecorFitsSystemWindows(window, true)`.
- Top ActionBar policy:
  use Android top ActionBar as the global title/back surface.
- Navigation policy:
  use typed route objects (`AppRoute`) end-to-end; do not use raw route strings.
- Delete policy (V2):
  class delete and student delete are soft delete; note delete and note media delete are permanent delete.

### Database and Migration Policy (V2)
- Treat schema migration as mandatory, not optional.
- Backfill existing rows for new non-null columns.
- Default read queries must exclude soft-deleted rows unless explicitly requesting archived data.
- Preserve historical attendance/note integrity when classes/students are soft deleted.

### Step Handoff Requirements
At the end of each prompt output, include:
1. Completed scope
2. Deferred scope
3. New dependencies/config changes
4. Carry-over assumptions

## Global Prefix (prepend to every prompt)
```text
Work only in this repository. Implement Version 2 enhancements incrementally on top of the current app. Keep Android system navigation buttons visible (do not hide navigation bars). Keep UI consistent using the shared minimal colorful light theme (white/cream style). Use Android top ActionBar as the global title/back bar on all screens. Use Jetpack Compose + Material3 + Koin + Room + DataStore + Coroutines/Flow + BiometricPrompt + Coil + WorkManager where required by the step. Use typed AppRoute navigation (no raw route strings). End each step by running compile/install/launch and giving a manual verification checklist with pass/fail.
```

## Standard Device Gate (required in every step)
```bash
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell monkey -p com.uteacher.attendancetracker -c android.intent.category.LAUNCHER 1
```

## Prompt 01 - V2 Data Model and Migration Foundation
```text
Implement V2 schema and model foundations for duration, attendance status, and soft delete support.

Implement:
1. Schema/version prep
- Bump Room database version.
- Add/adjust migration object(s) from current schema version (current repo baseline: v1 schema files).
- Keep migration deterministic and idempotent.

2. Schedule duration support
- Add persisted column on schedules table:
  - `durationMinutes INTEGER NOT NULL DEFAULT 0`
- Backfill duration from `startTime` and `endTime` for existing rows.
- Keep validation rule that `startTime < endTime` on same day.

3. Attendance status model upgrade
- Add class/session-level taken state:
  - `attendance_sessions.isClassTaken INTEGER NOT NULL DEFAULT 1`
- Add record-level mark status:
  - `attendance_records.status TEXT NOT NULL DEFAULT 'PRESENT'`
  - accepted values: `PRESENT`, `ABSENT`, `SKIPPED`
- Backfill existing records:
  - if legacy `isPresent=1` => `PRESENT`
  - if legacy `isPresent=0` => `ABSENT`

4. Soft delete model for classes/students
- Add to `classes`:
  - `isDeleted INTEGER NOT NULL DEFAULT 0`
  - `deletedAt INTEGER NULL`
- Add to `students`:
  - `isDeleted INTEGER NOT NULL DEFAULT 0`
  - `deletedAt INTEGER NULL`
- Add indices to support default filtered queries.

5. Domain + mapper updates
- Add duration to schedule domain model.
- Add attendance status enum in domain layer.
- Add class taken flag in session domain model.
- Add soft delete metadata in class/student domain models.

6. Validation and invariants
- Duration must be computed from start/end and stored in minutes.
- Never persist negative/zero duration.
- `SKIPPED` status must be representable even if present/absent booleans exist in legacy structures.

Done criteria:
- Project compiles with upgraded schema.
- Migration runs on existing local database without crash.
- New columns visible in generated schema JSON.
- Existing attendance rows map to `PRESENT/ABSENT` correctly.

Output format:
1. Changed files.
2. Migration details and backfill logic summary.
3. Device gate command results.
4. Manual checklist with pass/fail.
5. Step handoff (completed, deferred, deps/config, assumptions).
```

### Git Commit Message (V2 Step 01)
`feat(v2-step-01): add duration, attendance status, and soft-delete schema foundations with migration`

## Prompt 02 - DAO and Repository Contract Updates (V2 Semantics)
```text
Implement DAO/repository contract changes so V2 behavior is enforceable by data layer rules.

Implement:
1. DAO query policy for soft delete
- Default class and student list queries must filter `isDeleted = 0`.
- Add archived/withDeleted query variants where needed for management/restore flows.
- Ensure joins used by dashboard, attendance, and class roster ignore soft-deleted entities by default.

2. Duration handling in repositories
- Compute `durationMinutes` from start/end for all create/update schedule writes.
- Reject invalid duration at repository boundary.

3. Attendance save contract v2
- Update attendance save API to accept:
  - class/session taken flag (`isClassTaken`)
  - per-student status list (`PRESENT/ABSENT/SKIPPED`)
  - lesson note
- If `isClassTaken=false`, repository must normalize all records to `SKIPPED` before persistence.
- Keep idempotent upsert behavior by class+schedule+date.

4. Read models for UI
- Return attendance counters supporting `present`, `absent`, `skipped`, `total`.
- Return schedule duration for display and summary calculations.

5. Soft delete methods
- Class repository:
  - `softDeleteClass(classId)`
  - `restoreClass(classId)` (or explicit defer note if restore postponed)
- Student repository:
  - `softDeleteStudent(studentId)`
  - `restoreStudent(studentId)` (or explicit defer note if restore postponed)

6. Hard delete methods for notes/media contracts (API only in this step)
- Note repository:
  - `deleteNotePermanently(noteId)`
  - `deleteNoteMediaPermanently(mediaId)`
- Ensure contract states physical media file deletion as part of operation.

Done criteria:
- Compile passes with updated contracts.
- Existing call sites adjusted or clearly deferred to later prompt.
- Unit tests/verification stubs for normalization (`Not Taken => SKIPPED`) and query filtering added.

Output format:
1. Changed files.
2. Repository contract matrix before/after.
3. Device gate command results.
4. Manual checklist with pass/fail.
5. Step handoff.
```

### Git Commit Message (V2 Step 02)
`refactor(v2-step-02): enforce v2 attendance semantics and soft-delete aware repository contracts`

## Prompt 03 - Delete Behavior and Integrity Rules
```text
Implement deletion behavior end-to-end in data/domain layers according to V2 policy.

Implement:
1. Soft delete execution for classes
- Replace hard delete operations with soft delete updates.
- Deleting a class should hide it from normal UI queries.
- Historical attendance and notes linked to that class remain intact in database.

2. Soft delete execution for students
- Replace hard delete operations with soft delete updates.
- Deleted students should not appear in active lists/search/attendance-taking list.
- Existing historical attendance rows for deleted students remain intact.

3. Permanent note delete
- `deleteNotePermanently(noteId)` must:
  - delete all `note_media` rows for note
  - delete all corresponding media files from internal storage
  - delete note row
- Must be transactional at DB level + best-effort file cleanup with explicit error reporting.

4. Permanent media delete (single attachment)
- `deleteNoteMediaPermanently(mediaId)` must:
  - delete media DB row
  - delete linked file from storage
  - keep note itself intact

5. Safety and recoverability
- Add confirmation-level error messages for partial file cleanup failures.
- Ensure repeated delete calls are safe (idempotent behavior where practical).

6. Verification-oriented tests
- Add/expand tests for:
  - soft delete filters
  - permanent note delete removes db rows and files
  - single media delete removes only target media

Done criteria:
- No hard delete path remains for classes/students.
- Note and note media deletion are permanent.
- Data integrity preserved after delete operations.

Output format:
1. Changed files.
2. Delete behavior table (entity -> soft/hard -> rationale).
3. Device gate command results.
4. Manual checklist with pass/fail.
5. Step handoff.
```

### Git Commit Message (V2 Step 03)
`feat(v2-step-03): implement soft-delete for classes/students and permanent delete for notes/media`

## Prompt 04 - Create Class UX Update (Duration + Semester/Section Row)
```text
Implement Create Class screen updates required for V2.

Implement:
1. Semester + section same row
- In Create Class form, place semester dropdown and section input in one horizontal row.
- Keep responsive behavior for small screens (weights/min widths; no clipping).

2. Duration visibility
- In schedule slot draft and added schedule rows, show computed duration label (e.g., `1h 30m`).
- Duration display must always reflect selected start/end times.

3. Validation
- Block adding slot when computed duration <= 0.
- Keep overlap validation and existing date-range validation.

4. Data wiring
- Ensure saved schedule slot writes include `durationMinutes`.
- Ensure edit/update flows preserve and display duration.

5. UX polish
- Keep ActionBar save behavior and existing picker style policy.
- Keep content descriptions and touch targets intact.

Done criteria:
- Semester and section render on same line in Create Class screen.
- Duration is visible before save and after reload.
- Invalid durations are blocked with clear message.

Manual verification checklist:
- [ ] Semester dropdown and section field are in same row on phone portrait.
- [ ] Slot duration updates when start/end time changes.
- [ ] Duration persists after class save and reopen.
- [ ] Overlap validation still works.

Output format:
1. Changed files.
2. UI layout decisions for small screens.
3. Device gate command results.
4. Manual checklist with pass/fail.
5. Step handoff.
```

### Git Commit Message (V2 Step 04)
`feat(v2-step-04): update create class layout and add schedule duration visibility`

## Prompt 05 - Take Attendance V2: Class Taken/Not Taken + Search + Skipped Logic
```text
Implement the primary V2 attendance behavior.

Implement:
1. Class information card controls
- Add class-level control inside the Class Information card:
  - `Taken`
  - `Not Taken`
- Persist selected state in ViewModel state and repository payload.

2. Search in Take Attendance screen
- Add search input at top of student attendance section.
- Filter by student name, registration number, and roll number.
- Keep filtered count visible (`Showing X of Y`).

3. Not Taken behavior
- If class is marked `Not Taken`:
  - all student attendance statuses become `SKIPPED`
  - attendance marking controls are disabled
  - attendance list visuals are greyed out
- If switched back to `Taken`, restore editable present/absent interactions.

4. Status-based UI
- Replace boolean-only rendering with status-aware rendering:
  - Present style
  - Absent style
  - Skipped style (neutral/grey)

5. Save semantics
- Save payload must include class taken flag and status list.
- `Not Taken` path must never save `PRESENT/ABSENT`; only `SKIPPED`.

6. Existing session load behavior
- On reopen, if session is not taken, screen loads as disabled/grey with `SKIPPED` records.

Done criteria:
- Class Information card shows taken toggle.
- Search works against student list.
- Not Taken mode sets all to skipped and disables attendance interactions.
- Reopen flow preserves status accurately.

Manual verification checklist:
- [ ] Toggle `Not Taken` sets all students to `Skipped`.
- [ ] Attendance controls become disabled and greyed.
- [ ] Search filters by name and registration.
- [ ] Save + reopen preserves taken/skipped state.

Output format:
1. Changed files.
2. Attendance state-transition table.
3. Device gate command results.
4. Manual checklist with pass/fail.
5. Step handoff.
```

### Git Commit Message (V2 Step 05)
`feat(v2-step-05): add taken-not-taken attendance mode with skipped normalization and search`

## Prompt 06 - Take Attendance Sticky Bottom Notes (Always Visible)
```text
Implement the V2 requirement that lesson note stays anchored at screen bottom and always visible.

Implement:
1. Layout restructure
- Refactor Take Attendance screen layout to use a bottom-anchored notes container (e.g., Scaffold bottomBar or anchored surface).
- Student list remains scrollable independently above the note area.

2. Always-visible note editor
- Keep lesson note input visible while scrolling student list.
- Ensure note field remains accessible with keyboard open (`adjustResize` behavior preserved).

3. Interaction with Not Taken mode
- Attendance list remains disabled in Not Taken mode.
- Lesson note remains editable in both Taken and Not Taken states.

4. Visual hierarchy
- Note area should be visually distinct but lightweight.
- Preserve existing theme consistency and spacing.

5. Save/restore behavior
- Existing lesson note load/save behavior remains intact.
- No regression to autosave/manual-save flow from prior behavior.

Done criteria:
- Bottom note area is always visible during list scroll.
- Keyboard does not fully occlude note input.
- Not Taken still greys attendance section only.

Manual verification checklist:
- [ ] Scroll long student list; note remains visible at bottom.
- [ ] Open keyboard; note remains reachable.
- [ ] Not Taken mode disables list but note stays editable.
- [ ] Saved note reappears on reopen.

Output format:
1. Changed files.
2. Layout strategy summary.
3. Device gate command results.
4. Manual checklist with pass/fail.
5. Step handoff.
```

### Git Commit Message (V2 Step 06)
`feat(v2-step-06): anchor attendance lesson notes at bottom and keep always visible`

## Prompt 07 - Notes V2: Permanent Note Delete + Saved Media Delete
```text
Implement note-level deletion UX and saved-media deletion support.

Implement:
1. Delete note action
- Add ActionBar delete action in Add/Edit Note screen (edit mode only).
- Show destructive confirmation dialog.
- On confirm, call permanent note delete flow and navigate back on success.

2. Delete saved media attachments
- Saved media thumbnails in edit mode must support delete action (not read-only anymore).
- On delete confirmation, permanently remove selected media file + DB row.
- Update UI state immediately after successful delete.

3. Pending media behavior
- Keep pending media remove behavior intact.
- Ensure pending-media removal does not affect already saved media unless explicitly deleted.

4. Error handling
- Show user-readable error if file cleanup fails.
- Ensure note content remains intact if one media delete fails.

5. Data integrity
- Deleting note permanently removes associated media rows/files.
- Deleting one media does not delete note.

Done criteria:
- Note delete permanently removes note and media.
- Saved media can be individually deleted in edit mode.
- UI reflects delete results without stale thumbnails.

Manual verification checklist:
- [ ] Edit note shows delete action.
- [ ] Confirm note delete removes note from dashboard/summary.
- [ ] Saved media tile has delete control.
- [ ] Deleting one saved media keeps note and other media intact.

Output format:
1. Changed files.
2. Note/media delete flow diagram (text summary).
3. Device gate command results.
4. Manual checklist with pass/fail.
5. Step handoff.
```

### Git Commit Message (V2 Step 07)
`feat(v2-step-07): add permanent note delete and saved-media delete controls`

## Prompt 08 - Class and Student Delete UX (Soft Delete)
```text
Implement user-facing soft delete flows for classes and students.

Implement:
1. Manage Classes delete
- Add delete action for class items in Manage Classes/Edit Class context.
- Confirmation dialog must clearly state soft delete behavior.
- Soft-deleted classes disappear from default class lists/dashboard.

2. Manage Students delete
- Add delete action for student rows in Manage Students.
- Confirmation dialog must clearly state soft delete behavior.
- Soft-deleted students disappear from default student lists and attendance-taking list.

3. Archived visibility strategy
- Add archived filter/toggle or archived section to verify soft deletes in-app.
- If restore is implemented now, provide restore action; if deferred, explicitly document defer.

4. Navigation/flow safety
- If user opens stale route to soft-deleted class/student, show graceful not-found/archived message.
- No crashes on deleted references.

5. Summary/stat behavior
- Historical records remain queryable for historical stats where appropriate.
- Active operational screens should ignore soft-deleted records by default.

Done criteria:
- Class and student delete actions exist and perform soft delete.
- Default lists exclude deleted entities.
- No hard delete path used for class/student flows.

Manual verification checklist:
- [ ] Delete class removes it from active class lists.
- [ ] Delete student removes from active student lists and attendance list.
- [ ] Historical attendance data still loads where expected.
- [ ] Deleted entity route access is handled gracefully.

Output format:
1. Changed files.
2. Soft-delete UX behavior matrix.
3. Device gate command results.
4. Manual checklist with pass/fail.
5. Step handoff.
```

### Git Commit Message (V2 Step 08)
`feat(v2-step-08): add soft-delete user flows for classes and students`

## Prompt 09 - Daily Summary Grouped by Day + Two Read-Only Viewer Screens
```text
Implement the V2 summary and read-only analytics viewing improvements.

Implement:
1. Daily Summary grouping by day
- Refactor Daily Summary to group items by date (day sections).
- Each day group must include:
  - notes count
  - attendance sessions count
  - classes taken count
  - classes skipped count
  - present/absent/skipped totals
- Keep newest day first.

2. Daily group UI behavior
- Add clear day headers (sticky headers if practical).
- Keep edit shortcuts from summary items.
- Add optional search/filter that operates across grouped items.

3. New read-only screen A: Notes + Media viewer
- Create typed route: `AppRoute.ViewNotesMedia`.
- Screen purpose: view notes and attached media only (read-only; no edit/delete actions).
- Include:
  - Date-grouped note list
  - Each note card with title, preview text, created/updated metadata
  - Attached media gallery (tap to preview image in read-only full-screen dialog/sheet)

4. New read-only screen B: Attendance stats + lesson note viewer
- Create typed route: `AppRoute.ViewAttendanceStats`.
- Screen purpose: view attendance statistics and lesson notes only (read-only; no attendance edit actions).
- Include:
  - Day-level and class-level attendance totals
  - Present/Absent/Skipped counts and percentages
  - Session-level lesson note preview and full-note view
  - Class taken vs skipped session counts

5. Navigation wiring
- Add entry points to both screens from Daily Summary and/or Dashboard action area.
- Keep ActionBar title/back behavior consistent.

6. Data/ViewModel support
- Build UI models for grouped day summary, notes-media viewer, and attendance-stats viewer.
- Ensure skipped records are included in stats and clearly labeled.
- Ensure note media loading handles missing files gracefully in read-only mode.

Done criteria:
- Daily Summary is grouped by day and includes day-level totals.
- New `ViewNotesMedia` screen exists and shows notes with media in read-only mode.
- New `ViewAttendanceStats` screen exists and shows attendance stats + lesson notes in read-only mode.
- Navigation to/from both screens is stable.

Manual verification checklist:
- [ ] Daily Summary renders date groups with correct totals.
- [ ] Group ordering is newest day first.
- [ ] `ViewNotesMedia` loads notes and attached media in read-only mode.
- [ ] `ViewAttendanceStats` loads attendance statistics and lesson notes in read-only mode.
- [ ] Skipped attendance counts appear correctly.
- [ ] Back navigation returns to source screen correctly from both viewer screens.

Output format:
1. Changed files.
2. Grouping/stat aggregation logic summary.
3. Device gate command results.
4. Manual checklist with pass/fail.
5. Step handoff.
```

### Git Commit Message (V2 Step 09)
`feat(v2-step-09): group daily summary by day and add read-only notes-media and attendance-stats viewers`

## Prompt 10 - Notes Only Mode (Settings Toggle + UI Gating)
```text
Implement a Notes Only Mode toggle in Settings that hides all class and attendance views, making the app behave as a pure notes app.

Implement:
1. DataStore preference
- Add `notesOnlyMode` boolean preference to `SettingsPreferencesRepository`.
- Default value: `false` (full app mode with classes + notes).
- Expose as `Flow<Boolean>` for reactive UI observation.

2. Settings toggle UI
- Add a new settings section titled "App Mode" in `SettingsScreen`.
- Place it before the "Default Session Format" section.
- Include a labeled switch: "Notes Only Mode".
- Add a descriptive subtitle below the switch: "Hide class, student, and attendance features. The app will function as a notes-only tool."
- When toggled, persist immediately to DataStore.

3. Dashboard gating
- Observe `notesOnlyMode` in `DashboardViewModel` and expose in `DashboardUiState`.
- When enabled:
  - Hide the "Scheduled Classes" section entirely.
  - Hide class-date calendar indicators (dots); keep note-date indicators.
  - The Notes section remains fully visible and functional.

4. FAB menu gating
- Pass `notesOnlyMode` state to `HamburgerFabMenu`.
- When enabled:
  - Hide "Create Class", "Edit Class", and "Manage Students" menu items.
  - Keep "Settings" menu item visible.
- If only one item remains, keep the FAB functional with just that single item (no empty-menu state).

5. Daily Summary gating
- Observe `notesOnlyMode` in `DailySummaryViewModel` and expose in UI state.
- When enabled:
  - Hide all `AttendanceSummaryItem` entries from the grouped list.
  - Show only `NoteSummaryItem` entries.
  - Day groups with zero remaining items after filtering should be hidden.

6. Navigation route guards
- In `AppNavHost`, guard class/attendance routes when `notesOnlyMode` is enabled:
  - `CreateClass`, `ManageClassList`, `EditClass`, `ManageStudents`, `TakeAttendance`
- If a guarded route is reached (e.g., stale deep link or back-stack), redirect to `Dashboard`.
- Do NOT remove route registrations from the NavGraph; only guard navigation entry.

7. Read-only viewer gating (V2 viewer screens)
- If `ViewAttendanceStats` route exists, guard it the same way as attendance routes.
- `ViewNotesMedia` route remains accessible in both modes.

8. Backup/restore behavior
- No changes to backup/restore logic.
- Backup always exports full database (classes, students, attendance, notes) regardless of mode.
- Restore always imports full database.
- Toggling Notes Only Mode off after restore reveals all restored class/attendance data.

9. Data integrity
- Toggling Notes Only Mode does NOT delete, archive, or modify any class/attendance data.
- All class/attendance data remains intact and queryable in the database.
- Toggling the mode off immediately restores full app behavior with existing data.

Done criteria:
- Toggle exists in Settings and persists across app restarts.
- Dashboard hides class section and class-date indicators when enabled.
- FAB menu shows only Settings when enabled.
- Daily Summary shows only notes when enabled.
- Class/attendance routes are blocked when enabled.
- Toggling off restores all class/attendance views with data intact.
- Backup/restore works identically in both modes.

Manual verification checklist:
- [ ] Settings shows "Notes Only Mode" toggle with description.
- [ ] Toggle ON: Dashboard "Scheduled Classes" section disappears.
- [ ] Toggle ON: Calendar dots for class-only dates disappear; note dots remain.
- [ ] Toggle ON: FAB menu shows only "Settings".
- [ ] Toggle ON: Daily Summary shows only note items; attendance items hidden.
- [ ] Toggle ON: Navigating to a class/attendance route redirects to Dashboard.
- [ ] Toggle OFF: All class/attendance views reappear with data intact.
- [ ] Toggle persists after app kill and relaunch.
- [ ] Backup export in Notes Only Mode includes class/attendance data.
- [ ] Import backup in Notes Only Mode; toggle off; class data visible.

Output format:
1. Changed files.
2. Gating strategy summary (which composables check the flag and how).
3. Device gate command results.
4. Manual checklist with pass/fail.
5. Step handoff.
```

### Git Commit Message (V2 Step 10)
`feat(v2-step-10): add notes-only mode toggle with UI gating across dashboard, FAB, summary, and navigation`

## Prompt 11 - V2 Regression, Migration QA, and Edge-Case Hardening
```text
Run a focused V2 quality pass across migration, deletion semantics, attendance state behavior, and Notes Only Mode.

Implement:
1. Migration verification suite
- Validate upgrade path on a seeded pre-V2 database.
- Verify backfilled durations and attendance statuses.
- Verify app boot with migrated database and no destructive resets.

2. Behavior regression checks
- Ensure existing flows still work:
  - setup/auth/dashboard
  - create/edit class
  - manage students
  - take attendance save/reopen
  - notes add/edit
  - settings/backup

3. V2-specific edge cases
- Toggle Taken/Not Taken repeatedly before save.
- Search filter active while toggling attendance state.
- Soft delete class/student with existing historical attendance.
- Delete note with multiple media attachments, including missing-file cases.

4. Notes Only Mode edge cases
- Toggle Notes Only Mode on, navigate through all remaining screens, toggle off.
- Enable Notes Only Mode with existing class data; verify data survives round-trip.
- Backup in Notes Only Mode, restore in full mode; verify all data present.
- Toggle mode rapidly; confirm no stale UI state or navigation crashes.
- Deep link or back-stack to a class route while Notes Only Mode is active.

5. Performance/readability checks
- Ensure grouped daily summary remains smooth with larger datasets.
- Ensure attendance screen remains responsive with search + sticky note + disabled mode.
- Ensure both read-only viewer screens remain smooth when rendering large note/media and attendance datasets.

6. Documentation updates
- Update BRD/TRD/addendum notes for V2 behavior including Notes Only Mode.
- Record deferred items and known non-blockers in `DEFERRED_ISSUES.md` if needed.

Done criteria:
- No blocker regressions found.
- Migration and V2 semantics validated on-device.
- Notes Only Mode gating verified across all affected screens.
- Known residual risks explicitly documented.

Manual verification checklist:
- [ ] Migration test from pre-V2 DB passes.
- [ ] All critical flows run without crash.
- [ ] Not Taken -> Skipped behavior verified end-to-end.
- [ ] Soft delete vs hard delete rules verified.
- [ ] Grouped summary, `ViewNotesMedia`, and `ViewAttendanceStats` are verified with realistic data.
- [ ] Notes Only Mode on/off cycle preserves all data and UI state.
- [ ] Notes Only Mode blocks class/attendance routes without crash.

Output format:
1. Changed files.
2. Findings list (P0/P1/P2 severity).
3. Device gate command results.
4. Manual checklist with pass/fail.
5. Step handoff.
```

### Git Commit Message (V2 Step 11)
`chore(v2-step-11): complete v2 regression pass, migration QA, notes-only-mode QA, and edge-case hardening`

## Quick Tracker Template (Use After Each Step)
```text
Step:
Build:
Install:
Launch:
Manual checks passed:
Manual checks failed:
Blockers:
Next step allowed: Yes/No
```
