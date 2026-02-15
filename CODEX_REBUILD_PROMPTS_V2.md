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
- Calculated class duration displayed in compact format (`1h 30m`, `45m`) on:
  - present-date class cards on Dashboard
  - class information card in Take Attendance
  - slot list in Create Class
- Take Attendance class-level state: `Taken` vs `Not Taken`
- Auto-mark all students as `Skipped` when class is not taken
- Toggle back to `Taken` defaults attendance to `PRESENT`
- Greyed/disabled attendance-taking controls when class is not taken
- Attendance marking screen excludes inactive students
- Sticky, always-visible lesson note at bottom of Take Attendance screen
- Lesson note draft autosaves
- Search within Take Attendance student list
- Create Class layout update: semester dropdown + section field in same row
- Student model update: optional `department` field
- Edit Student dialog: allow updating `registrationNumber`
- Manage Students list keeps active students above inactive students
- Manage Students supports filters by `department` and active/inactive status
- Student CSV template and parser update: include optional `department` column
- Student CSV header parsing supports close/synonym header matches for every header item and order-independent columns
- Student CSV bulk import preview: per-row import/reject selection with `Select All` / `Deselect All`
- Student CSV default import rule: only rows with valid `name` + `registrationNumber` are importable by default
- Student CSV invalid-row rule: missing `name` or `registrationNumber` rows are non-importable
- Existing student match for CSV linking uses `registrationNumber + name`
- CSV import deduplicates duplicate rows and keeps only one normalized entry (duplicates are not shown in preview)
- CSV linking keeps inactive students inactive
- CSV preview visibly indicates inactive students
- Edit Student duplicate conflict flow supports confirm-and-merge to existing student
- Edit Student merge updates all student-id references across the app data model
- Dashboard Notes section: remove "Created on" metadata from note cards
- Notes viewer cards show only "Updated on" metadata
- Delete support for notes, classes, and students
- Cascade hard delete for classes (removes all schedules, attendance sessions/records, and associated notes)
- Cascade hard delete for students (removes all attendance records for that student)
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
- Schema updates (destructive recreate), repository contracts, cascade deletion semantics

**Phase 2: Attendance and Class UX (Prompts 04-06)**
- Create Class layout/duration presentation, Take Attendance V2 behavior, sticky note footer

**Phase 3: Deletion + Summaries + Stats Views (Prompts 07-09)**
- Note/media permanent delete UX, class/student cascade hard delete UX, grouped daily summary and stats viewer

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
  class delete and student delete are cascade hard delete (permanent, removes all associated data after user confirmation); note delete and note media delete are permanent delete. The existing "mark as inactive" feature for classes remains separate from delete.
- Student edit/import policy (V2):
  remove "mark student as active" toggle from Edit Student dialog and replace with a delete action; keep the existing separate "Mark Inactive/Mark Active" action in Manage Students unchanged; allow editing `registrationNumber` from Edit Student dialog with confirm-and-merge behavior on duplicate conflict; `department` is optional in UI but stored as empty string; CSV import eligibility requires both student name and registration number.
- CSV header/identity policy (V2):
  CSV column order does not matter; parse headers using normalized close/synonym matches for every supported header item (`name`, `registrationNumber`, `department`, and any other supported CSV fields); existing-student matching for CSV link uses `registrationNumber + name`; duplicate CSV rows (normalized by `registrationNumber + name`) are collapsed to one record and duplicates are not shown in preview.
- Attendance visibility policy (V2):
  Take Attendance shows only active students.

### Database and Migration Policy (V2)
- No incremental migrations required during active development.
- Use `fallbackToDestructiveMigration()` on the Room database builder.
- When schema changes, the user will uninstall the app / clear data and do a fresh install.
- Do not write migration objects or backfill logic; define the schema as-is for the target version.
- Bump the database version number when schema columns change.

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

## Prompt 01 - V2 Data Model Foundation (Destructive Recreate)
```text
Implement V2 schema and model foundations for duration, attendance status, and optional student department. No migrations — use destructive recreate.

Implement:
1. Schema/version prep
- Bump Room database version.
- Use `fallbackToDestructiveMigration()` on the Room database builder.
- Do NOT write migration objects or backfill logic; the user will uninstall/clear data for a fresh install.

2. Schedule duration support
- Add persisted column on schedules table:
  - `durationMinutes INTEGER NOT NULL DEFAULT 0`
- Keep validation rule that `startTime < endTime` on same day.

3. Attendance status model upgrade
- Add class/session-level taken state:
  - `attendance_sessions.isClassTaken INTEGER NOT NULL DEFAULT 1`
- Add record-level mark status:
  - `attendance_records.status TEXT NOT NULL DEFAULT 'PRESENT'`
  - accepted values: `PRESENT`, `ABSENT`, `SKIPPED`

4. Student model upgrade
- Add persisted optional column on students table:
  - `department TEXT NOT NULL DEFAULT ''`
- Preserve existing student identity/linking rules (no behavior change for duplicate resolution in this step).

5. Domain + mapper updates
- Add duration to schedule domain model.
- Add attendance status enum in domain layer.
- Add class taken flag in session domain model.
- Add optional department to student domain model and entity mappers.

6. Validation and invariants
- Duration must be computed from start/end and stored in minutes.
- Never persist negative/zero duration.
- `SKIPPED` status must be representable even if present/absent booleans exist in legacy structures.
- Student `department` is optional at input level, but persist as empty string when omitted.
- Keep existing DB index behavior unchanged (no new/removed indexes in this step).

Done criteria:
- Project compiles with upgraded schema.
- App launches cleanly on fresh install (after uninstall/clear data).
- New columns (`durationMinutes`, `isClassTaken`, `status`, `department`) visible in generated schema JSON.
- Existing index behavior remains unchanged.

Output format:
1. Changed files.
2. Schema change summary.
3. Device gate command results.
4. Manual checklist with pass/fail.
5. Step handoff (completed, deferred, deps/config, assumptions).
```

### Git Commit Message (V2 Step 01)
`feat(v2-step-01): add duration, attendance status, and optional student department schema foundations`

## Prompt 02 - DAO and Repository Contract Updates (V2 Semantics)
```text
Implement DAO/repository contract changes so V2 behavior is enforceable by data layer rules.

Implement:
1. Duration handling in repositories
- Compute `durationMinutes` from start/end for all create/update schedule writes.
- Reject invalid duration at repository boundary.

2. Attendance save contract v2
- Update attendance save API to accept:
  - class/session taken flag (`isClassTaken`)
  - per-student status list (`PRESENT/ABSENT/SKIPPED`)
  - lesson note
- If `isClassTaken=false`, repository must normalize all records to `SKIPPED` before persistence.
- Keep idempotent upsert behavior by class+schedule+date.

3. Read models for UI
- Return attendance counters supporting `present`, `absent`, `skipped`, `total`.
- Return schedule duration for display and summary calculations.

4. Cascade hard delete contracts for classes
- Class repository:
  - `deleteClassPermanently(classId)` — cascading delete that removes:
    - all schedules for the class
    - all attendance sessions for the class
    - all attendance records for those sessions
    - all notes associated with the class (and their media files)
    - the class row itself
  - Must be transactional at DB level + best-effort file cleanup for note media.

5. Cascade hard delete contracts for students
- Student repository:
  - `deleteStudentPermanently(studentId)` — cascading delete that removes:
    - all attendance records for that student across all sessions
    - the student row itself

6. Permanent delete contracts for notes/media (API only in this step)
- Note repository:
  - `deleteNotePermanently(noteId)`
  - `deleteNoteMediaPermanently(mediaId)`
- Ensure contract states physical media file deletion as part of operation.

7. Student CSV import contract updates
- Update the student CSV template/header contract to include optional `department`.
- Header parsing rules:
  - column order is irrelevant.
  - normalize headers by case-insensitive compare with flexible separators/spaces.
  - accept close/synonym matches for every supported header item.
  - include explicit alias handling at minimum:
    - `name`: `name`, `student name`, `full name`
    - `registrationNumber`: `registration number`, `reg no`, `Registration_number`
    - `department`: `department`, `dept`
- Validation eligibility rules:
  - `name` is required for import.
  - `registrationNumber` is required for import.
  - missing/blank `name` or `registrationNumber` means row is invalid and must not be imported.
  - `department` is optional and may be blank.
- Existing student match rule for import/link:
  - match by `registrationNumber + name` (normalized compare).
- Duplicate row handling before preview:
  - if multiple CSV rows normalize to the same `registrationNumber + name`, keep only one row.
  - exclude duplicate copies from preview and from final import payload.
- Bulk preview contract:
  - each preview row must expose eligibility (`canImport`) and selection state (`selectedForImport`).
  - default selection: `selectedForImport=true` only for rows where `canImport=true`.
  - invalid rows are unselected by default and cannot be imported.
  - support `selectAll` and `deselectAll` actions in preview; `selectAll` applies only to import-eligible rows.
- If a matched existing student is inactive, link to class and preserve inactive status.
- In preview, matched inactive students must be visibly indicated as inactive.
- If no match exists, create student as normal.

Done criteria:
- Compile passes with updated contracts.
- Existing call sites adjusted or clearly deferred to later prompt.
- Unit tests/verification stubs for normalization (`Not Taken => SKIPPED`) added.
- CSV contract covers optional department, header synonym parsing for every supported header, order-independent columns, required name/registration eligibility, `registrationNumber + name` link matching, pre-preview duplicate collapse, inactive-row indication, and preview selection defaults.

Output format:
1. Changed files.
2. Repository contract matrix before/after.
3. Device gate command results.
4. Manual checklist with pass/fail.
5. Step handoff.
```

### Git Commit Message (V2 Step 02)
`refactor(v2-step-02): enforce v2 attendance semantics and add student csv import contracts`

## Prompt 03 - Delete Behavior and Integrity Rules
```text
Implement deletion behavior end-to-end in data/domain layers according to V2 policy. All deletes are permanent.

Implement:
1. Cascade hard delete execution for classes
- `deleteClassPermanently(classId)` must, in a single transaction:
  - delete all attendance records linked to the class's attendance sessions
  - delete all attendance sessions for the class
  - delete all schedules for the class
  - delete all note media rows for notes linked to the class
  - delete all notes linked to the class
  - delete the class row itself
- After transaction, best-effort cleanup of physical media files for deleted notes.
- Log/report any file cleanup failures without rolling back the DB transaction.

2. Cascade hard delete execution for students
- `deleteStudentPermanently(studentId)` must, in a single transaction:
  - delete all attendance records for that student across all sessions
  - delete the student row itself

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

5. Safety
- Treat missing media files as non-blocking success during delete flows.
- Add confirmation-level warnings only for non-missing-file cleanup failures.
- Ensure repeated delete calls are safe (idempotent behavior where practical).
- The existing "mark as inactive" feature for classes is separate from delete and remains unchanged.

6. Verification-oriented tests
- Add/expand tests for:
  - cascade class delete removes all associated rows
  - cascade student delete removes all attendance records
  - permanent note delete removes db rows and files
  - single media delete removes only target media

Done criteria:
- Class delete permanently removes class and all associated schedules, sessions, records, notes, and media.
- Student delete permanently removes student and all associated attendance records.
- Note and note media deletion are permanent.
- Data integrity preserved after delete operations.

Output format:
1. Changed files.
2. Delete behavior table (entity -> cascade scope -> rationale).
3. Device gate command results.
4. Manual checklist with pass/fail.
5. Step handoff.
```

### Git Commit Message (V2 Step 03)
`feat(v2-step-03): implement cascade hard delete for classes/students and permanent delete for notes/media`

## Prompt 04 - Create Class UX Update (Duration + Semester/Section Row)
```text
Implement Create Class screen updates required for V2.

Implement:
1. Semester + section same row
- In Create Class form, place semester dropdown and section input in one horizontal row.
- Keep responsive behavior for small screens (weights/min widths; no clipping).

2. Duration visibility
- In schedule slot draft and added schedule rows, show computed duration label in compact format (e.g., `1h 30m`, `45m`).
- Duration display must always reflect selected start/end times.
- In Dashboard present-date class cards, show calculated class duration in compact format.
- In Take Attendance class information card, show calculated class duration in compact format.
- Do not display raw-minute-only duration labels in these surfaces.

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
- Duration is visible in compact format on Create Class slot list, Dashboard present-date class cards, and Take Attendance class information card.
- Invalid durations are blocked with clear message.

Manual verification checklist:
- [ ] Semester dropdown and section field are in same row on phone portrait.
- [ ] Slot duration updates when start/end time changes.
- [ ] Duration persists after class save and reopen.
- [ ] Create Class slot list shows duration in compact format.
- [ ] Dashboard present-date class cards show duration in compact format.
- [ ] Take Attendance class information card shows duration in compact format.
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
- Show class duration in compact format on this card.

2. Search in Take Attendance screen
- Add search input at top of student attendance section.
- Filter by student name, registration number, and roll number.
- Keep filtered count visible (`Showing X of Y`).
- Exclude inactive students from attendance marking list.

3. Not Taken behavior
- If class is marked `Not Taken`:
  - all student attendance statuses become `SKIPPED`
  - attendance marking controls are disabled
  - attendance list visuals are greyed out
- If switched back to `Taken`, default all visible student statuses to `PRESENT` and restore editable interactions.

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
- Switching back to Taken defaults statuses to Present.
- Inactive students are not shown in attendance marking list.
- Reopen flow preserves status accurately.

Manual verification checklist:
- [ ] Toggle `Not Taken` sets all students to `Skipped`.
- [ ] Toggle back to `Taken` defaults all visible students to `Present`.
- [ ] Attendance controls become disabled and greyed.
- [ ] Search filters by name and registration.
- [ ] Inactive students are not visible in attendance marking list.
- [ ] Class information card shows duration in compact format.
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
- Lesson note draft must autosave while typing and on navigation/background.
- Existing lesson note load/save behavior remains intact.
- No regression to manual-save flow from prior behavior.

Done criteria:
- Bottom note area is always visible during list scroll.
- Keyboard does not fully occlude note input.
- Not Taken still greys attendance section only.
- Lesson note draft autosaves and restores on reopen.

Manual verification checklist:
- [ ] Scroll long student list; note remains visible at bottom.
- [ ] Open keyboard; note remains reachable.
- [ ] Not Taken mode disables list but note stays editable.
- [ ] Typing in lesson note autosaves draft without manual save.
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
- Treat missing media files as non-blocking success (do not fail delete).
- Show user-readable error only for non-missing-file cleanup failures.
- Ensure note content remains intact if one media delete fails.

5. Data integrity
- Deleting note permanently removes associated media rows/files.
- Deleting one media does not delete note.

Done criteria:
- Note delete permanently removes note and media.
- Saved media can be individually deleted in edit mode.
- Missing-file cleanup cases do not block successful delete completion.
- UI reflects delete results without stale thumbnails.

Manual verification checklist:
- [ ] Edit note shows delete action.
- [ ] Confirm note delete removes note from dashboard/summary.
- [ ] Saved media tile has delete control.
- [ ] Deleting media with a missing file still succeeds (non-blocking).
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

## Prompt 08 - Class and Student Delete UX + Student CSV Preview Rules
```text
Implement user-facing permanent cascade delete flows for classes/students and the required student CSV preview selection rules.

Implement:
1. Delete class action
- Add a visible `Delete Class` button in the topbar for each class in the Edit Class page.
- Confirmation dialog must be destructive-styled and clearly warn:
  "This will permanently delete the class and ALL associated data including schedules, attendance records, and notes. This action cannot be undone."
- On confirm, call `deleteClassPermanently(classId)` and navigate back on success.
- Class disappears from all lists, dashboard, and summary after deletion.
- Note: the existing "mark as inactive" feature is separate and remains unchanged.

2. Edit Student dialog delete action
- In Edit Student dialog, remove the "mark student as active" toggle.
- Replace it with a destructive "Delete Student" option.
- Keep the existing separate "Mark Inactive/Mark Active" action in the Manage Students list as currently implemented.
- Allow `registrationNumber` to be edited in Edit Student dialog and persisted on save.
- On registration number update conflict with an existing student, show confirm-and-merge dialog.
- On merge confirm:
  - move all references from edited student to existing student for every relation that uses `studentId` (including class links, attendance records, and any other student foreign-key references) with deduplication.
  - delete the edited/source student row after successful relink.
  - keep existing target student active/inactive status unchanged.
- On merge cancel, keep edit dialog open without applying merge.
- Delete action should be available from Manage Students flow and routed through the Edit Student dialog context.
- Confirmation dialog must be destructive-styled and clearly warn:
  "This will permanently delete the student and ALL their attendance records. This action cannot be undone."
- On confirm, call `deleteStudentPermanently(studentId)` and navigate back on success.
- Student disappears from all student lists and attendance screens after deletion.

3. Manage Students list ordering and filters
- Keep active students listed above inactive students.
- Add filter controls:
  - filter by `department`
  - filter by status (`All`, `Active`, `Inactive`)
- Department filter options must exclude empty/blank department values.
- Students with empty/blank department should not be shown in Manage Students list.
- Keep existing mark active/inactive actions working with filtered/sorted list.

4. Student CSV bulk import preview behavior
- In CSV import preview, each student row must support explicit Import/Reject selection.
- Default selection behavior:
  - rows with both valid `name` and `registrationNumber` start selected for import.
  - rows missing `name` or `registrationNumber` start unselected and are not importable.
- Add `Select All` and `Deselect All` controls in preview.
- `Select All` must select all import-eligible rows; non-importable rows remain unselected.
- Existing-student linking uses `registrationNumber + name`.
- If multiple CSV rows map to one normalized `registrationNumber + name`, keep only one row and do not show duplicate copies in preview.
- If linked existing student is inactive, keep that student inactive after linking.
- Inactive matched students must be visibly indicated in preview (e.g., status badge/label).
- Keep all other CSV flow behavior unchanged.

5. Navigation/flow safety
- If user opens a stale route referencing a deleted class/student (e.g., back-stack), show graceful not-found message or redirect to parent screen.
- No crashes on deleted references.

6. Summary/stat behavior
- After cascade delete, all associated records are gone from the database.
- Daily Summary, viewer screens, and dashboard reflect the deletion immediately.
- No orphaned attendance sessions, records, or notes should remain.

Done criteria:
- Edit Class page shows `Delete Class` button in topbar.
- Class delete action exists and permanently removes class + all associated data.
- Edit Student dialog no longer shows "mark student as active"; it shows delete action instead.
- Manage Students screen still supports separate "Mark Inactive/Mark Active" action for students.
- Manage Students sorts active students above inactive students and supports department/status filters.
- Manage Students excludes empty-department students and hides empty departments from department-filter options.
- Edit Student dialog allows updating `registrationNumber` and persists the updated value.
- Registration number conflict flow supports confirm-and-merge with all student-id reference relinks and source deletion.
- Student delete action exists and permanently removes student + all attendance records.
- CSV preview supports per-row import/reject, select all/deselect all, required-field eligibility defaults, duplicate-row collapse (single kept row), visible inactive indication, and inactive-student-preserving link behavior.
- Confirmation dialogs clearly communicate irreversible cascade behavior.
- UI reflects deletions immediately across all screens.

Manual verification checklist:
- [ ] Edit Class page shows `Delete Class` button in topbar.
- [ ] Delete class shows destructive confirmation with cascade warning.
- [ ] Confirming class delete removes class, schedules, attendance, and notes from DB.
- [ ] Edit Student dialog no longer contains "mark student as active" toggle.
- [ ] Manage Students still provides "Mark Inactive/Mark Active" action for students.
- [ ] Manage Students lists active students above inactive students.
- [ ] Manage Students supports filters by department and Active/Inactive status.
- [ ] Manage Students does not show students with empty department.
- [ ] Department filter options do not include empty/blank department values.
- [ ] Edit Student dialog allows changing registration number and saves the new value.
- [ ] Registration number duplicate conflict shows confirm-and-merge and re-links all student-id references on confirm.
- [ ] Edit Student dialog provides "Delete Student" option with destructive confirmation.
- [ ] Confirming student delete removes student and attendance records from DB.
- [ ] CSV preview defaults selected only for rows with valid name + registration.
- [ ] CSV rows missing name or registration are non-importable.
- [ ] CSV preview has working Select All and Deselect All controls.
- [ ] Existing student in CSV links by registration+name instead of creating duplicate student row.
- [ ] Duplicate CSV rows are collapsed to one row and duplicates are not shown in preview.
- [ ] CSV preview can link inactive students and preserves inactive status.
- [ ] CSV preview visibly indicates inactive students.
- [ ] Dashboard, Daily Summary, and viewer screens reflect deletions.
- [ ] Stale route to deleted class/student is handled gracefully.
- [ ] "Mark as inactive" for classes still works independently of delete.
- [ ] Student active/inactive toggling from Manage Students still works independently of delete.

Output format:
1. Changed files.
2. Cascade delete UX behavior matrix.
3. Device gate command results.
4. Manual checklist with pass/fail.
5. Step handoff.
```

### Git Commit Message (V2 Step 08)
`feat(v2-step-08): add class delete button, student merge-on-conflict, and enhanced manage-students/csv rules`

## Prompt 09 - Daily Summary Date Cards + Read-Only Viewer Navigation
```text
Implement the V2 Daily Summary listing and read-only viewer navigation behavior updates.

Implement:
1. Daily Summary date-card grouping
- Refactor Daily Summary to render date cards (newest day first).
- Each date card must contain:
  - Attendance entries (classes taken/not taken for that date)
  - Notes entries for that date
- Do not show totals/count aggregations on Daily Summary cards.

2. Daily Summary content filter
- Add a top-level filter with three options:
  - `Both`
  - `Notes only`
  - `Attendance only`
- Persist or retain selection as already used by existing screen state patterns.
- Respect global Notes Only Mode:
  - When Notes Only Mode is enabled, hide attendance entries and enforce notes-only listing behavior.

3. Read-only screen A: Notes + Media viewer
- Create typed route: `AppRoute.ViewNotesMedia`.
- Screen purpose: view notes and attached media in read-only mode.
- Include:
  - Date-grouped note list
  - Each note card with title, preview text, and `Updated on` metadata only
  - Attached media gallery (tap to preview image in read-only full-screen dialog/sheet)
- Add an explicit action button to navigate to the note edit flow for the selected note.

4. Read-only screen B: Attendance summary viewer
- Create typed route: `AppRoute.ViewAttendanceStats`.
- Screen purpose: view attendance summary details in read-only mode.
- Include:
  - Session/class attendance detail presentation for the selected entry
  - Linked lesson note preview/read view where applicable
- Add an explicit action button to navigate to the attendance edit/take-attendance flow for that session/class.

5. Navigation wiring
- Daily Summary note entries navigate to `AppRoute.ViewNotesMedia`.
- Daily Summary attendance entries navigate to `AppRoute.ViewAttendanceStats`.
- Dashboard note card `Open` action must navigate to `AppRoute.ViewNotesMedia`.
- Keep Dashboard `Take Attendance` navigation unchanged.
- Keep ActionBar title/back behavior consistent.

6. Data/ViewModel support
- Build UI models for grouped date-card summary, notes-media viewer, and attendance summary viewer.
- Ensure attendance entries still represent taken/not-taken state clearly.
- Ensure note media loading handles missing files gracefully in read-only mode.

Done criteria:
- Daily Summary is grouped into date cards and shows no totals.
- Daily Summary filter supports `Both`, `Notes only`, and `Attendance only`.
- Global Notes Only Mode forces notes-only behavior in Daily Summary.
- New `ViewNotesMedia` screen exists and shows notes with media in read-only mode plus a button to edit note.
- New `ViewAttendanceStats` screen exists and shows attendance summary details in read-only mode plus a button to edit attendance.
- Notes viewer cards show only `Updated on` metadata (no `Created on`).
- Navigation from Daily Summary and Dashboard `Open` to the correct read-only screens is stable.

Manual verification checklist:
- [ ] Daily Summary renders date cards in newest-first order with attendance and notes grouped by day.
- [ ] Daily Summary date cards do not show totals/count aggregations.
- [ ] Filter toggles correctly between `Both`, `Notes only`, and `Attendance only`.
- [ ] With Notes Only Mode enabled, Daily Summary enforces notes-only listing and hides attendance entries.
- [ ] Group ordering is newest day first.
- [ ] Tapping a note entry in Daily Summary opens `ViewNotesMedia`.
- [ ] Tapping an attendance entry in Daily Summary opens `ViewAttendanceStats`.
- [ ] Dashboard note card `Open` navigates to `ViewNotesMedia`.
- [ ] Dashboard `Take Attendance` navigation behavior remains unchanged.
- [ ] `ViewNotesMedia` loads notes and attached media in read-only mode.
- [ ] `ViewNotesMedia` note cards show only `Updated on` metadata.
- [ ] `ViewNotesMedia` provides a working button to navigate to edit note.
- [ ] `ViewAttendanceStats` loads attendance summary details in read-only mode.
- [ ] `ViewAttendanceStats` provides a working button to navigate to edit attendance.
- [ ] Back navigation returns to source screen correctly from both viewer screens.

Output format:
1. Changed files.
2. Grouping/filter/navigation logic summary.
3. Device gate command results.
4. Manual checklist with pass/fail.
5. Step handoff.
```

### Git Commit Message (V2 Step 09)
`feat(v2-step-09): add date-card summary filters and read-only viewer navigation with edit shortcuts`

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
- In the Dashboard Notes section (both Notes Only ON and OFF), remove "Created on" metadata from note cards.
- Keep note title/content preview and existing note actions unchanged.

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
- Keep these route guards mandatory for this step.
- No additional notification/widget/external-entry guard handling is required beyond these route guards.

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
- Dashboard Notes cards no longer show "Created on" metadata.
- FAB menu shows only Settings when enabled.
- Daily Summary shows only notes when enabled.
- Class/attendance routes are blocked when enabled.
- Toggling off restores all class/attendance views with data intact.
- Backup/restore works identically in both modes.

Manual verification checklist:
- [ ] Settings shows "Notes Only Mode" toggle with description.
- [ ] Toggle ON: Dashboard "Scheduled Classes" section disappears.
- [ ] Toggle ON: Calendar dots for class-only dates disappear; note dots remain.
- [ ] Dashboard Notes cards do not show "Created on" metadata (Toggle ON and OFF).
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

## Prompt 11 - V2 Regression QA and Edge-Case Hardening
```text
Run a focused V2 quality pass across deletion semantics, attendance state behavior, and Notes Only Mode.

Implement:
1. Fresh install verification
- Verify app boots cleanly on fresh install with V2 schema.
- Verify `fallbackToDestructiveMigration()` works when schema version changes (uninstall + reinstall).

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
- Cascade delete class with existing attendance sessions, records, and notes; verify no orphans remain.
- Cascade delete student with attendance records across multiple sessions; verify no orphans remain.
- Delete note with multiple media attachments, including missing-file cases.
- Create/edit student with department set and blank department; verify both are accepted.
- CSV import header parsing accepts close/synonym matches for every supported header item and order-independent columns.
- CSV import preview defaults only valid name+registration rows as selected.
- CSV import rejects rows missing name or registration.
- CSV preview `Select All` and `Deselect All` work correctly with mixed valid/invalid rows.
- CSV import with existing student links by registration+name (no duplicate student row).
- CSV duplicate rows are collapsed and not shown multiple times in preview.
- CSV linking preserves inactive status for inactive matched students.
- CSV preview visibly marks inactive matched students.
- Edit Student dialog shows delete option while Manage Students retains active/inactive action.
- Edit Student dialog supports registration number update and persists changes correctly.
- Registration-number duplicate conflict path confirms merge, re-links references, and deletes source student.
- Manage Students keeps active students above inactive students and filters by department/status.
- Manage Students excludes empty-department students and omits empty department filter values.
- Toggle back from Not Taken to Taken defaults visible students to Present.
- Take Attendance excludes inactive students.
- Lesson note draft autosaves correctly.
- Duration appears in compact format on Create Class slot list, Dashboard present-date class cards, and Take Attendance class info.
- Dashboard Notes cards do not show "Created on" metadata.
- `ViewNotesMedia` cards show only `Updated on` metadata.

4. Notes Only Mode edge cases
- Toggle Notes Only Mode on, navigate through all remaining screens, toggle off.
- Enable Notes Only Mode with existing class data; verify data survives round-trip.
- Backup in Notes Only Mode, restore in full mode; verify all data present.
- Toggle mode rapidly; confirm no stale UI state or navigation crashes.
- Deep link or back-stack to a class route while Notes Only Mode is active.

5. Performance/readability checks
- Validate with a realistic stress dataset:
  - at least 20 classes
  - at least 120 students total (including at least 30 inactive)
  - at least 180 attendance sessions
  - at least 300 notes
  - at least 300 media attachments
  - data spread across at least 30 days
- Performance pass thresholds (manual stopwatch/log timestamp measurement is acceptable):
  - Daily Summary first render (initial load) <= 2.0s
  - `ViewNotesMedia` first render <= 2.0s
  - `ViewAttendanceStats` first render <= 2.0s
  - Take Attendance search response <= 300ms per query change on typical inputs
  - Not Taken -> Taken toggle UI update <= 500ms
- Scrolling in summary/viewers should be visually smooth with no repeated frame stutter that blocks interaction.

6. Documentation updates
- Update `BUSINESS_REQUIREMENTS.md` with final V2 behavior changes (attendance, student/CSV, delete semantics, notes metadata, Notes Only Mode).
- Update `TECHNICAL_REQUIREMENTS.md` with implementation-level V2 decisions (schema, contracts, merge behavior, filtering/sorting, autosave, performance expectations).
- No separate addendum file is required for V2 in this repo.
- Record deferred items and known non-blockers in `DEFERRED_ISSUES.md` if needed.

Done criteria:
- No blocker regressions found.
- Fresh install and V2 semantics validated on-device.
- Cascade deletes leave no orphaned rows.
- Notes Only Mode gating verified across all affected screens.
- Performance thresholds pass on the defined stress dataset.
- `BUSINESS_REQUIREMENTS.md` and `TECHNICAL_REQUIREMENTS.md` are updated to reflect final V2 behavior.
- Known residual risks explicitly documented.

Manual verification checklist:
- [ ] Fresh install with V2 schema launches without crash.
- [ ] All critical flows run without crash.
- [ ] Not Taken -> Skipped behavior verified end-to-end.
- [ ] Cascade class delete leaves no orphaned sessions, records, notes, or media.
- [ ] Cascade student delete leaves no orphaned attendance records.
- [ ] Student department field works as optional in create/edit/import flows.
- [ ] CSV preview selection defaults and select-all/deselect-all behavior are correct.
- [ ] CSV import invalid rows (missing name/registration) are blocked from import.
- [ ] CSV header synonym + order-independent parsing works for every supported header item.
- [ ] CSV duplicate handling links existing student by registration+name.
- [ ] CSV duplicate rows are collapsed and not shown multiple times in preview.
- [ ] CSV linking preserves inactive status for inactive matched students.
- [ ] CSV preview visibly indicates inactive matched students.
- [ ] Manage Students active/inactive action for students still works.
- [ ] Manage Students active-first ordering and department/status filters work.
- [ ] Manage Students excludes empty-department students and empty department filter options.
- [ ] Edit Student registration number update is saved and reflected in student lists/search.
- [ ] Edit Student registration-number duplicate conflict merge re-links all student-id references and removes source student.
- [ ] Toggle Not Taken -> Taken defaults visible students to Present.
- [ ] Inactive students are excluded from Take Attendance.
- [ ] Lesson note draft autosaves on edit and restores.
- [ ] Duration displays in compact format on required surfaces.
- [ ] Dashboard Notes cards omit "Created on" metadata.
- [ ] `ViewNotesMedia` note cards show only `Updated on` metadata.
- [ ] Grouped summary, `ViewNotesMedia`, and `ViewAttendanceStats` are verified with realistic data.
- [ ] Stress dataset meets minimum size/distribution requirements.
- [ ] Daily Summary initial render meets <= 2.0s target.
- [ ] `ViewNotesMedia` initial render meets <= 2.0s target.
- [ ] `ViewAttendanceStats` initial render meets <= 2.0s target.
- [ ] Take Attendance search response meets <= 300ms target.
- [ ] Not Taken -> Taken toggle update meets <= 500ms target.
- [ ] `BUSINESS_REQUIREMENTS.md` updated with V2 behavior.
- [ ] `TECHNICAL_REQUIREMENTS.md` updated with V2 technical decisions.
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
`chore(v2-step-11): complete v2 regression pass, cascade-delete QA, notes-only-mode QA, and edge-case hardening`

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
