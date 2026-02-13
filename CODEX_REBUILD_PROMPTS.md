# Codex Rebuild Prompts (Detailed, Step-by-Step)

Date: February 13, 2026
Project: attenote (`com.uteacher.attendancetracker`)

This document provides comprehensive, copy-paste prompts to rebuild the attenote Android app from scratch in **strict, testable, device-validated increments**.

## Document Overview

This rebuild guide consists of **15 sequential prompts** that take you from an empty project to a fully-functional, production-ready Android app. Each prompt is:
- **Self-contained**: Complete specifications, code examples, and testing checklists
- **Device-gated**: Must pass build/install/launch verification before proceeding
- **Incremental**: Each step builds on previous steps without breaking existing functionality
- **Comprehensive**: Includes UiState, ViewModel, Repository, UI, and integration details

## Rebuild Phases

**Phase 1: Foundation (Prompts 01-05)**
- Gradle setup, theme system, navigation routes, Room database, DAOs, repositories
- Output: Compilable app with full data layer and navigation skeleton

**Phase 2: Core Flows (Prompts 06-09)**
- Koin DI, splash/setup/auth, dashboard with calendar, class creation and management
- Output: Functional first-run experience and class management workflows

**Phase 3: Feature Completion (Prompts 10-13)**
- Student management, attendance tracking, rich-text notes, settings and backup/restore
- Output: All business features implemented and integrated

**Phase 4: Quality Assurance (Prompts 14-15)**
- UI consistency pass, regression testing, code review, edge case hardening
- Output: Production-ready app with no known blockers

## How to Use This File

### Sequential Execution
1. **Run prompts in strict order** (01 → 02 → ... → 15)
2. **Use one prompt per AI conversation turn** (copy entire prompt block including triple backticks)
3. **Prepend the Global Prefix** to every prompt before submitting
4. **Do not skip steps** - each step validates the previous step's output

### Device Gate Requirement
After EVERY prompt:
1. Run the Standard Device Gate commands (build/install/launch)
2. Execute the manual verification checklist provided in that prompt
3. Mark each checklist item as pass/fail
4. **ONLY proceed to next prompt if ALL critical items pass**

### Failure Handling
If a step fails:
1. Do NOT proceed to the next prompt
2. Ask the AI to fix the failing step
3. Re-run device gate commands
4. Re-verify checklist
5. Only proceed when step passes

### Version Control
After each successfully completed prompt:
- Commit changes using the provided Git Commit Message
- This creates a clean checkpoint for each increment

### Alignment Rules (Read Once)
- Source-of-truth order for conflicts: `BUSINESS_REQUIREMENTS.md` > `TECHNICAL_REQUIREMENTS.md` > `CODEX_REBUILD_PROMPTS.md` > `ImplementaionPlan.md`.
- System UI policy is fixed for all steps: keep Android navigation buttons visible, never hide navigation bars, keep status bar visible, and use `WindowCompat.setDecorFitsSystemWindows(window, true)`.
- Top ActionBar policy is fixed for all steps: use the Android top ActionBar as the global title/back surface, and keep route title + back/up behavior reflected there.
- Typed navigation policy is fixed for all steps: use typed route objects (`AppRoute`) end-to-end, not raw string route names.
- Biometric host policy is fixed for all steps: when using `BiometricPrompt` with an activity host, use `FragmentActivity`.

### Dependency Addendum (Prevent Missing-Lib Gaps)
In addition to prompt-specific dependencies, ensure these are present before the first step that uses them:
- Prompt 01 baseline: `activity-compose`, `lifecycle-runtime-compose`, `lifecycle-viewmodel-compose`, `navigation-compose`, `koin-android`, `koin-compose`, `room-runtime`, `room-ktx`, `room-compiler`, `datastore-preferences`, `kotlinx-coroutines-android`, `kotlinx-serialization-json`.
- Prompt 06: `androidx.biometric:biometric`.
- Prompt 09: `kotlin-csv-jvm` (CSV parsing).
- Prompt 12: `io.coil-kt:coil-compose`, `richeditor-compose`.
- Prompt 13: `androidx.work:work-runtime-ktx`.

### Implemented UX Addendum (February 14, 2026)
The following behavior updates are now part of the baseline and should be treated as source-of-truth for rebuild validation:
- **Dashboard calendar (Prompt 07):**
  - Calendar is anchored at the bottom as an overlay panel, not only as a trailing scroll item.
  - Hamburger menu/FAB is rendered above the calendar layer.
  - Calendar supports vertical swipe gestures: swipe up to expand, swipe down to collapse.
  - In collapsed mode, left/right arrows move by **day** (previous day / next day), not by week.
  - Use chevrons for expand/collapse affordances (`v` when collapsed, `^` when expanded).
- **Dashboard notes metadata (Prompt 07):**
  - Note cards show both `Created on ...` and `Updated on ...` metadata.
- **Settings save action (Prompt 13):**
  - Save Profile action is hosted in the Android ActionBar right action (global top bar surface).
  - Do not keep a duplicate inline "Save Profile" button inside Settings content.

### Step Handoff (Close Loose Ends Every Step)
At the end of each prompt output, include:
1. Completed scope (exactly what was done in this step).
2. Deferred scope (what intentionally remains for later prompts).
3. New dependencies/config changes added in this step.
4. Carry-over assumptions for the next prompt.

## Global Prefix (prepend to every prompt)
```text
Work only in this repository. Rebuild incrementally from scratch. Keep Android system navigation buttons visible (do not hide navigation bars). Keep UI consistent using one shared minimal colorful light theme (white/cream style). Use Android top ActionBar as the global title/back bar on all screens. Use Jetpack Compose + Material3 + Koin + Room + DataStore + Coroutines/Flow + BiometricPrompt + Coil + WorkManager where required by the step. Use typed AppRoute navigation (no raw route strings). End each step by running compile/install/launch and giving a manual verification checklist with pass/fail.
```

## Standard Device Gate (required in every step)
```bash
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell monkey -p com.uteacher.attendancetracker -c android.intent.category.LAUNCHER 1
```

## Prompt 01 - Bootstrap + Build + Theme Foundation
```text
Create the project foundation and compile-ready Android app shell.

Implement:
1. Gradle setup
- Configure `settings.gradle.kts`, top-level `build.gradle.kts`, `app/build.gradle.kts`, `gradle/libs.versions.toml`.
- Use:
  - compileSdk/targetSdk 35
  - minSdk 26
  - Kotlin 2.2.10
  - JVM target 17
  - AGP 9.0.0
  - KSP plugin
  - Kotlin serialization plugin
  - Compose BOM 2025.01.00 (for consistent Compose library versions)
- Required dependencies in version catalog:
  - compose-bom, compose-ui, compose-material3, compose-ui-tooling, compose-ui-tooling-preview
  - activity-compose, lifecycle-runtime-compose, lifecycle-viewmodel-compose
  - navigation-compose, koin-android, koin-compose
  - room-runtime, room-ktx, room-compiler (KSP)
  - datastore-preferences
  - kotlinx-coroutines-android
  - kotlinx-serialization-json

2. Android entry points
- Create/confirm:
  - `app/src/main/AndroidManifest.xml`
  - `AttendanceTrackerApplication.kt`
  - `MainActivity.kt`
- Ensure `android:name` points to `AttendanceTrackerApplication`.
- Set `android:windowSoftInputMode="adjustResize"` in manifest for proper keyboard behavior.

3. Theme system
- Create:
  - `ui/theme/Color.kt`
  - `ui/theme/Type.kt`
  - `ui/theme/Shape.kt`
  - `ui/theme/Theme.kt`
  - `ui/theme/component/AttenoteComponents.kt`

3.1. Color palette (Light White/Cream theme)
Light theme colors:
  - Primary: Soft Blue #2E5B7A
  - PrimaryContainer: #E7F0F7
  - Secondary: Muted Teal #4B9A93
  - SecondaryContainer: #DFF3EF
  - Tertiary: Soft Lavender #8A6FA8
  - TertiaryContainer: #F0E9F7
  - Error: #BA1A1A
  - ErrorContainer: #FFDAD6
  - Background: Cream White #FFFDF6
  - Surface: Warm White #FFFCF8
  - SurfaceVariant: #F3EEE2
  - Outline: #8A857B

Dark theme colors (for future support):
  - Primary: #A0C8E8
  - PrimaryContainer: #00497D
  - Secondary: #80CBC4
  - SecondaryContainer: #004D40
  - Background: #191C1E
  - Surface: #191C1E
  - SurfaceVariant: #42474E
  - Outline: #8C9199

Color usage guidelines:
  - Primary: Main actions, FABs, selected states
  - Secondary: Accents, highlights, calendar indicators
  - Tertiary: Badges, special states
  - Error: Validation errors, destructive actions
  - Surface/Background: Card backgrounds, screen backgrounds

3.2. Typography scale (Material 3 roles)
Define typography with role mappings:
  - displayMedium: Screen titles (Dashboard, Settings)
  - titleLarge: Section headers (Scheduled Classes, Notes)
  - titleMedium: Top app bar titles
  - titleSmall: Card titles, dialog titles
  - bodyLarge: Primary content text
  - bodyMedium: Secondary content, descriptions
  - bodySmall: Helper text, timestamps
  - labelLarge: Button text
  - labelMedium: Input labels, tab labels
  - labelSmall: Captions, metadata

3.3. Shared themed components (`ui/theme/component/AttenoteComponents.kt`)
Implement reusable components:
  - AttenoteTopAppBar: Top app bar with title, optional back navigation, optional actions
  - AttenoteButton: Filled primary button with enabled/disabled states
  - AttenoteSecondaryButton: Outlined button variant
  - AttenoteTextField: Outlined text field with label and error message support
  - AttenoteSectionCard: Elevated card for grouping content sections
  - AttenoteFloatingActionButton: Primary-colored FAB with icon support
  - AttenoteDialog: Standard dialog with title, content, confirm/dismiss actions
  - AttenoteDatePicker: Material 3 DatePickerDialog wrapper with theme consistency
  - AttenoteTimePicker: Material 3 TimePickerDialog wrapper with theme consistency
  - AttenoteLoadingIndicator: Centered circular progress indicator

4. System bar policy
- Keep Android nav buttons visible.
- Do not call APIs that hide navigation bars.
- Keep status bar visible and use standard insets behavior.
- Use WindowCompat.setDecorFitsSystemWindows(window, true) for standard behavior.

5. Accessibility baseline
- All interactive components must have contentDescription for screen readers.
- Minimum touch target size 48dp for all clickable items.
- Color contrast meets WCAG AA standards (4.5:1 for normal text, 3:1 for large text).

6. App shell
- Render a simple placeholder composable from `MainActivity` using `AttenoteTheme`.
- Display "attenote" text using themed typography to verify theme works.

Done criteria:
- App builds and launches.
- No startup crash.
- Android navigation buttons are visible.
- Themed components compile and render correctly.
- Color palette and typography are applied in theme.

Output format:
1. Changed files.
2. What was implemented.
3. Device gate command results.
4. Manual checklist with pass/fail.
```

### Git Commit Message (Step 01)
`feat(step-01): bootstrap attenote shell, theme foundation, and visible system navigation buttons`

## Prompt 02 - Navigation Contract + Screen Placeholders
```text
Implement typed navigation routes and a placeholder screen for each feature.

Implement:
1. Route contract
- Create `ui/navigation/Routes.kt` with `@Serializable` sealed interface or sealed class `AppRoute`.

1.1. Define all routes with parameter validation:

Root routes (no back navigation):
  @Serializable data object Splash : AppRoute
  @Serializable data object AuthGate : AppRoute
  @Serializable data object Dashboard : AppRoute

First-run route:
  @Serializable data object Setup : AppRoute

Feature routes (with back navigation):
  @Serializable data object CreateClass : AppRoute
  @Serializable data object ManageClassList : AppRoute
  @Serializable data class EditClass(val classId: Long) : AppRoute {
      init { require(classId > 0) { "classId must be positive" } }
  }
  @Serializable data object ManageStudents : AppRoute
  @Serializable data class TakeAttendance(
      val classId: Long,
      val scheduleId: Long,
      val date: String
  ) : AppRoute {
      init {
          require(classId > 0) { "classId must be positive" }
          require(scheduleId > 0) { "scheduleId must be positive" }
          require(date.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$"))) {
              "date must be yyyy-MM-dd format, got: $date"
          }
      }
  }
  @Serializable data class AddNote(
      val date: String,
      val noteId: Long = -1L
  ) : AppRoute {
      init {
          require(date.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$"))) {
              "date must be yyyy-MM-dd format, got: $date"
          }
      }
  }
  @Serializable data object Settings : AppRoute

1.2. Date validation
- ISO yyyy-MM-dd format enforced in route init blocks
- Regex pattern: ^\\d{4}-\\d{2}-\\d{2}$
- Invalid dates throw IllegalArgumentException with clear message

2. Nav host
- Create `ui/navigation/AppNavHost.kt`.

2.1. NavController management
- Accept NavController as parameter (created in MainActivity via rememberNavController())
- Do NOT pass NavController deep into composable trees
- Use lambda callbacks for navigation actions

2.2. Navigation behaviors
- composable<Splash>: no back navigation
- composable<Setup>: no back navigation, on completion navigate to Dashboard with popUpTo(Splash) { inclusive = true }
- composable<AuthGate>: no back navigation, on success navigate to Dashboard with popUpTo(AuthGate) { inclusive = true }
- composable<Dashboard>: root destination, no back navigation
- All feature routes: standard back navigation with navController.popBackStack()

2.3. Placeholder implementation pattern
Each placeholder should use this structure:

Scaffold { padding ->
    Column(
        modifier = Modifier
            .padding(padding)
            .padding(16.dp)
            .fillMaxSize()
    ) {
        Text(
            text = "[Screen Name] Placeholder",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Ready for implementation in Step [X]",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // For routes with parameters, display them for verification:
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Route Parameters:",
            style = MaterialTheme.typography.labelMedium
        )
        // Example: Text("classId: $classId", style = MaterialTheme.typography.bodySmall)
    }
}

2.4. Top ActionBar configuration per route:
- Splash: title "Splash", no back button
- AuthGate: title "Auth Gate", no back button
- Setup: title "Setup", no back button
- Dashboard: title "Dashboard", no back button
- CreateClass: title "Create Class", back button
- ManageClassList: title "Manage Classes", back button
- EditClass: title "Edit Class", back button, display classId parameter
- ManageStudents: title "Manage Students", back button
- TakeAttendance: title "Take Attendance", back button, display classId/scheduleId/date parameters
- AddNote: title "Add Note", back button, display date/noteId parameters
- Settings: title "Settings", back button

3. Wire into activity
- `MainActivity` creates NavController via rememberNavController()
- `MainActivity` renders `AppNavHost(startDestination = Splash, navController = navController)` and applies route title/back state to Android ActionBar.
- Handle top ActionBar back/up click in activity (`android.R.id.home`) by delegating to `navController.popBackStack()`.
- Use default Material Motion transitions (do not customize animations in this step)

Done criteria:
- All routes compile without errors.
- Route parameter validation works (test with invalid dates).
- Navigation between all placeholder screens works.
- Back navigation works correctly on feature routes.
- Back navigation does NOT work on root routes.
- Top ActionBar title + back/up state are consistent per route.
- Route parameters display correctly in placeholders.

Output format:
1. Changed files.
2. Route list with validation rules.
3. Navigation behavior matrix (which routes allow back, special popUpTo behaviors).
4. Device gate command results.
5. Manual checklist with pass/fail:
   □ Build succeeds
   □ App launches to Splash
   □ Can navigate to each route
   □ Back button visibility correct per route type
   □ Route parameters display in placeholders
   □ Invalid date formats rejected (test "2026/02/13", "13-02-2026")
   □ Top ActionBar title/back state is correct per route
```

### Git Commit Message (Step 02)
`feat(step-02): add typed navigation routes and placeholder destinations`

## Prompt 03 - Room Database + Entities + TypeConverters
```text
Implement full local schema and converters.

Implement these files:
- `data/local/AppDatabase.kt`
- `data/local/converter/RoomTypeConverters.kt`
- Entities in `data/local/entity/`:
  - `ClassEntity.kt`
  - `StudentEntity.kt`
  - `ClassStudentCrossRef.kt`
  - `ScheduleEntity.kt`
  - `AttendanceSessionEntity.kt`
  - `AttendanceRecordEntity.kt`
  - `NoteEntity.kt`
  - `NoteMediaEntity.kt`

1. Type Converters (`data/local/converter/RoomTypeConverters.kt`)
Implement converters for:

1.1. LocalDate <-> String
- Format: ISO-8601 yyyy-MM-dd (e.g., "2026-02-13")
- Use DateTimeFormatter.ISO_LOCAL_DATE
- Nullable conversion support
Example:
  @TypeConverter
  fun fromLocalDate(date: LocalDate?): String? = date?.format(DateTimeFormatter.ISO_LOCAL_DATE)
  @TypeConverter
  fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }

1.2. LocalTime <-> String
- Format: HH:mm (24-hour, e.g., "14:30")
- Use DateTimeFormatter.ofPattern("HH:mm")
- Nullable conversion support

1.3. DayOfWeek <-> Int
- Mapping: Monday=1, Tuesday=2, ..., Sunday=7 (ISO-8601 standard)
- Use DayOfWeek.of(int) and DayOfWeek.value
- Nullable conversion support

2. Entity Definitions

2.1. ClassEntity (table: "classes")
@Entity(
    tableName = "classes",
    indices = [
        Index(value = ["instituteName", "session", "department", "semester", "subject", "section"], unique = true)
    ]
)
Fields:
  - classId: Long @PrimaryKey(autoGenerate = true)
  - instituteName: String (non-null)
  - session: String (non-null)
  - department: String (non-null)
  - semester: String (non-null)
  - section: String (non-null, default = "") // Empty string default to avoid SQLite NULL uniqueness issues
  - subject: String (non-null)
  - className: String (non-null) // Display label, auto-generated as "Subject - Institute", editable
  - startDate: LocalDate (non-null)
  - endDate: LocalDate (non-null)
  - isOpen: Boolean (non-null, default = true)
  - createdAt: LocalDate (non-null, default = current date)

Unique constraint: (instituteName, session, department, semester, subject, section)
Note: section defaults to empty string ("") not null to ensure proper uniqueness behavior

2.2. StudentEntity (table: "students")
@Entity(
    tableName = "students",
    indices = [
        Index(value = ["name", "registrationNumber"], unique = true)
    ]
)
Fields:
  - studentId: Long @PrimaryKey(autoGenerate = true)
  - name: String (non-null)
  - registrationNumber: String (non-null)
  - rollNumber: String? (nullable)
  - email: String? (nullable)
  - phone: String? (nullable)
  - isActive: Boolean (non-null, default = true)
  - createdAt: LocalDate (non-null, default = current date)

Unique constraint: (name, registrationNumber) - globally unique across all students

2.3. ClassStudentCrossRef (table: "class_student_cross_ref")
@Entity(
    tableName = "class_student_cross_ref",
    primaryKeys = ["classId", "studentId"],
    foreignKeys = [
        ForeignKey(
            entity = ClassEntity::class,
            parentColumns = ["classId"],
            childColumns = ["classId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["studentId"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("classId"),
        Index("studentId")
    ]
)
Fields:
  - classId: Long (non-null, part of composite PK)
  - studentId: Long (non-null, part of composite PK)
  - isActiveInClass: Boolean (non-null, default = true)
  - addedAt: LocalDate (non-null, default = current date)

Cascade delete on both foreign keys - deleting class or student removes cross-ref entries

2.4. ScheduleEntity (table: "schedules")
@Entity(
    tableName = "schedules",
    foreignKeys = [
        ForeignKey(
            entity = ClassEntity::class,
            parentColumns = ["classId"],
            childColumns = ["classId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("classId"),
        Index(value = ["classId", "dayOfWeek"])
    ]
)
Fields:
  - scheduleId: Long @PrimaryKey(autoGenerate = true)
  - classId: Long (non-null, FK to classes)
  - dayOfWeek: DayOfWeek (non-null, 1-7 via converter)
  - startTime: LocalTime (non-null)
  - endTime: LocalTime (non-null)

Indices for query performance on classId and classId+dayOfWeek

2.5. AttendanceSessionEntity (table: "attendance_sessions")
@Entity(
    tableName = "attendance_sessions",
    foreignKeys = [
        ForeignKey(
            entity = ClassEntity::class,
            parentColumns = ["classId"],
            childColumns = ["classId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ScheduleEntity::class,
            parentColumns = ["scheduleId"],
            childColumns = ["scheduleId"],
            onDelete = ForeignKey.RESTRICT  // CRITICAL: Prevent schedule deletion if attendance exists
        )
    ],
    indices = [
        Index(value = ["classId", "scheduleId", "date"], unique = true),
        Index("classId"),
        Index("scheduleId"),
        Index("date")
    ]
)
Fields:
  - sessionId: Long @PrimaryKey(autoGenerate = true)
  - classId: Long (non-null, FK to classes)
  - scheduleId: Long (non-null, FK to schedules with ON DELETE RESTRICT)
  - date: LocalDate (non-null)
  - lessonNotes: String? (nullable, HTML content)
  - createdAt: LocalDate (non-null, default = current date)

Unique constraint: (classId, scheduleId, date) - prevents duplicate attendance sessions
CRITICAL: scheduleId FK uses ON DELETE RESTRICT to prevent schedule deletion when attendance exists

2.6. AttendanceRecordEntity (table: "attendance_records")
@Entity(
    tableName = "attendance_records",
    foreignKeys = [
        ForeignKey(
            entity = AttendanceSessionEntity::class,
            parentColumns = ["sessionId"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["studentId"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["sessionId", "studentId"], unique = true),
        Index("sessionId"),
        Index("studentId")
    ]
)
Fields:
  - recordId: Long @PrimaryKey(autoGenerate = true)
  - sessionId: Long (non-null, FK to attendance_sessions)
  - studentId: Long (non-null, FK to students)
  - isPresent: Boolean (non-null, default = true) // Attendance defaults to present

Unique constraint: (sessionId, studentId) - one record per student per session

2.7. NoteEntity (table: "notes")
@Entity(
    tableName = "notes",
    indices = [
        Index("date")
    ]
)
Fields:
  - noteId: Long @PrimaryKey(autoGenerate = true)
  - title: String (non-null, can be empty)
  - content: String (non-null, HTML content, can be empty)
  - date: LocalDate (non-null)
  - createdAt: LocalDate (non-null, default = current date)
  - updatedAt: LocalDate (non-null, default = current date)

Index on date for dashboard queries

2.8. NoteMediaEntity (table: "note_media")
@Entity(
    tableName = "note_media",
    foreignKeys = [
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["noteId"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE  // Delete media when note is deleted
        )
    ],
    indices = [
        Index("noteId")
    ]
)
Fields:
  - mediaId: Long @PrimaryKey(autoGenerate = true)
  - noteId: Long (non-null, FK to notes with ON DELETE CASCADE)
  - filePath: String (non-null, internal storage path)
  - mimeType: String (non-null, e.g., "image/jpeg")
  - addedAt: LocalDate (non-null, default = current date)

ON DELETE CASCADE - media records are automatically removed when note is deleted

3. Database Configuration (`data/local/AppDatabase.kt`)

@Database(
    entities = [
        ClassEntity::class,
        StudentEntity::class,
        ClassStudentCrossRef::class,
        ScheduleEntity::class,
        AttendanceSessionEntity::class,
        AttendanceRecordEntity::class,
        NoteEntity::class,
        NoteMediaEntity::class
    ],
    version = 1,
    exportSchema = true  // Export schema to schemas/ directory for version control
)
@TypeConverters(RoomTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    // DAO abstract getters will be added in Step 04
    companion object {
        const val DATABASE_NAME = "attenote.db"
    }
}

Database builder configuration:
- Database name: "attenote.db"
- Schema export location: app/schemas/ (configure in build.gradle)
- Version: 1
- For testing: fallbackToDestructiveMigration() acceptable in debug builds

4. Gradle Configuration
Add to app/build.gradle.kts:
```
android {
    defaultConfig {
        ...
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }
}
```

Done criteria:
- All 8 entities compile without errors.
- TypeConverters compile and are wired to database.
- AppDatabase class compiles with all entities listed.
- Schema exports to app/schemas/ directory.
- No runtime crash on database initialization.
- Foreign key constraints are properly defined (especially ON DELETE RESTRICT for scheduleId).

Output format:
1. Changed files.
2. Entity summary table:
   | Entity | Table Name | PK | Unique Constraints | Foreign Keys | ON DELETE |
3. TypeConverter format specifications.
4. Device gate command results.
5. Manual checklist with pass/fail:
   □ Build succeeds
   □ All entities compile
   □ TypeConverters compile
   □ AppDatabase compiles
   □ Schema file exported to app/schemas/com.uteacher.attendancetracker.data.local.AppDatabase/1.json
   □ App launches without DB crash
   □ Logcat shows Room database created successfully
```

### Git Commit Message (Step 03)
`feat(step-03): implement Room schema entities and type converters`

## Prompt 04 - DAOs + Domain Models + Mappers
```text
Implement all DAO interfaces, domain models, and mapping extensions.

CRITICAL DOMAIN BOUNDARY RULE:
- UI and ViewModel layers MUST NOT import data.local.entity.* classes
- All UiState classes MUST use domain models only
- Entity-to-Domain conversion happens at repository boundaries via mappers
- This separation ensures clean architecture and testability

Implement these files:

DAOs in `data/local/dao/`:
- `ClassDao.kt`
- `StudentDao.kt`
- `ClassStudentCrossRefDao.kt`
- `ScheduleDao.kt`
- `AttendanceSessionDao.kt`
- `AttendanceRecordDao.kt`
- `NoteDao.kt`
- `NoteMediaDao.kt`

Embedded relationship classes in `data/local/entity/`:
- `ClassWithSchedules.kt`
- `SessionWithRecords.kt`
- `NoteWithMedia.kt`

Domain models in `domain/model/`:
- `Class.kt`
- `Student.kt`
- `ClassStudentLink.kt`
- `Schedule.kt`
- `AttendanceSession.kt`
- `AttendanceRecord.kt`
- `Note.kt`
- `NoteMedia.kt`

Mappers in `domain/mapper/`:
- `EntityDomainMappers.kt`

1. DAO Interface Specifications

Pattern rules:
- Use Flow<T> for reactive data (observe methods)
- Use suspend fun for one-shot reads and all writes
- Use @Transaction for queries involving joins/relationships
- OnConflictStrategy.ABORT for unique constraint violations (let repository handle)
- OnConflictStrategy.REPLACE for upsert behavior

1.1. ClassDao (`data/local/dao/ClassDao.kt`)

@Dao
interface ClassDao {
    // Observe
    @Query("SELECT * FROM classes WHERE isOpen = :isOpen ORDER BY createdAt DESC")
    fun observeClassesByOpenState(isOpen: Boolean): Flow<List<ClassEntity>>

    @Query("SELECT * FROM classes ORDER BY createdAt DESC")
    fun observeAllClasses(): Flow<List<ClassEntity>>

    // Get
    @Query("SELECT * FROM classes WHERE classId = :id")
    suspend fun getClassById(id: Long): ClassEntity?

    @Transaction
    @Query("SELECT * FROM classes WHERE classId = :id")
    suspend fun getClassWithSchedules(id: Long): ClassWithSchedules?

    // Insert/Update/Delete
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertClass(classEntity: ClassEntity): Long

    @Update
    suspend fun updateClass(classEntity: ClassEntity)

    @Delete
    suspend fun deleteClass(classEntity: ClassEntity)
}

1.2. StudentDao (`data/local/dao/StudentDao.kt`)

@Dao
interface StudentDao {
    // Observe
    @Query("SELECT * FROM students ORDER BY name ASC")
    fun observeAllStudents(): Flow<List<StudentEntity>>

    @Query("""
        SELECT s.* FROM students s
        INNER JOIN class_student_cross_ref cs ON s.studentId = cs.studentId
        WHERE cs.classId = :classId
        AND s.isActive = 1
        AND cs.isActiveInClass = 1
        ORDER BY s.name ASC
    """)
    fun observeActiveStudentsForClass(classId: Long): Flow<List<StudentEntity>>

    // Get
    @Query("SELECT * FROM students WHERE studentId = :id")
    suspend fun getStudentById(id: Long): StudentEntity?

    @Query("SELECT * FROM students WHERE name = :name AND registrationNumber = :regNumber")
    suspend fun getStudentByUniqueKey(name: String, regNumber: String): StudentEntity?

    // Insert/Update/Delete
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertStudent(student: StudentEntity): Long

    @Update
    suspend fun updateStudent(student: StudentEntity)

    @Delete
    suspend fun deleteStudent(student: StudentEntity)
}

1.3. ClassStudentCrossRefDao (`data/local/dao/ClassStudentCrossRefDao.kt`)

@Dao
interface ClassStudentCrossRefDao {
    // Observe
    @Query("""
        SELECT * FROM class_student_cross_ref
        WHERE classId = :classId
        ORDER BY addedAt DESC
    """)
    fun observeLinksForClass(classId: Long): Flow<List<ClassStudentCrossRef>>

    // Get
    @Query("""
        SELECT * FROM class_student_cross_ref
        WHERE classId = :classId AND studentId = :studentId
    """)
    suspend fun getLink(classId: Long, studentId: Long): ClassStudentCrossRef?

    // Insert/Update/Delete
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertLink(link: ClassStudentCrossRef)

    @Update
    suspend fun updateLink(link: ClassStudentCrossRef)

    @Delete
    suspend fun deleteLink(link: ClassStudentCrossRef)

    @Query("DELETE FROM class_student_cross_ref WHERE classId = :classId")
    suspend fun deleteAllLinksForClass(classId: Long)
}

1.4. ScheduleDao (`data/local/dao/ScheduleDao.kt`)

@Dao
interface ScheduleDao {
    // Observe
    @Query("""
        SELECT * FROM schedules
        WHERE classId = :classId
        ORDER BY dayOfWeek ASC, startTime ASC
    """)
    fun observeSchedulesForClass(classId: Long): Flow<List<ScheduleEntity>>

    // Get
    @Query("SELECT * FROM schedules WHERE scheduleId = :id")
    suspend fun getScheduleById(id: Long): ScheduleEntity?

    @Query("""
        SELECT * FROM schedules
        WHERE classId = :classId AND dayOfWeek = :dayOfWeek
        ORDER BY startTime ASC
    """)
    suspend fun getSchedulesForDay(classId: Long, dayOfWeek: Int): List<ScheduleEntity>

    // Insert/Update/Delete
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSchedule(schedule: ScheduleEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSchedules(schedules: List<ScheduleEntity>): List<Long>

    @Update
    suspend fun updateSchedule(schedule: ScheduleEntity)

    @Delete
    suspend fun deleteSchedule(schedule: ScheduleEntity)

    @Query("DELETE FROM schedules WHERE classId = :classId")
    suspend fun deleteAllSchedulesForClass(classId: Long)
}

1.5. AttendanceSessionDao (`data/local/dao/AttendanceSessionDao.kt`)

@Dao
interface AttendanceSessionDao {
    // Observe
    @Query("SELECT * FROM attendance_sessions WHERE classId = :classId ORDER BY date DESC")
    fun observeSessionsForClass(classId: Long): Flow<List<AttendanceSessionEntity>>

    // Get
    @Query("SELECT * FROM attendance_sessions WHERE sessionId = :id")
    suspend fun getSessionById(id: Long): AttendanceSessionEntity?

    @Query("""
        SELECT * FROM attendance_sessions
        WHERE classId = :classId AND scheduleId = :scheduleId AND date = :date
    """)
    suspend fun findSession(classId: Long, scheduleId: Long, date: String): AttendanceSessionEntity?

    @Transaction
    @Query("SELECT * FROM attendance_sessions WHERE sessionId = :sessionId")
    suspend fun getSessionWithRecords(sessionId: Long): SessionWithRecords?

    // Insert/Update/Delete
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSession(session: AttendanceSessionEntity): Long

    @Update
    suspend fun updateSession(session: AttendanceSessionEntity)

    @Delete
    suspend fun deleteSession(session: AttendanceSessionEntity)
}

1.6. AttendanceRecordDao (`data/local/dao/AttendanceRecordDao.kt`)

@Dao
interface AttendanceRecordDao {
    // Observe
    @Query("SELECT * FROM attendance_records WHERE sessionId = :sessionId ORDER BY studentId ASC")
    fun observeRecordsForSession(sessionId: Long): Flow<List<AttendanceRecordEntity>>

    // Get
    @Query("SELECT * FROM attendance_records WHERE recordId = :id")
    suspend fun getRecordById(id: Long): AttendanceRecordEntity?

    @Query("""
        SELECT * FROM attendance_records
        WHERE sessionId = :sessionId AND studentId = :studentId
    """)
    suspend fun getRecord(sessionId: Long, studentId: Long): AttendanceRecordEntity?

    // Insert/Update/Delete
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: AttendanceRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<AttendanceRecordEntity>): List<Long>

    @Update
    suspend fun updateRecord(record: AttendanceRecordEntity)

    @Delete
    suspend fun deleteRecord(record: AttendanceRecordEntity)

    @Query("DELETE FROM attendance_records WHERE sessionId = :sessionId")
    suspend fun deleteAllRecordsForSession(sessionId: Long)
}

1.7. NoteDao (`data/local/dao/NoteDao.kt`)

@Dao
interface NoteDao {
    // Observe
    @Query("SELECT * FROM notes WHERE date = :date ORDER BY updatedAt DESC")
    fun observeNotesForDate(date: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun observeAllNotes(): Flow<List<NoteEntity>>

    @Transaction
    @Query("SELECT * FROM notes WHERE date = :date ORDER BY updatedAt DESC")
    fun observeNotesWithMediaForDate(date: String): Flow<List<NoteWithMedia>>

    // Get
    @Query("SELECT * FROM notes WHERE noteId = :id")
    suspend fun getNoteById(id: Long): NoteEntity?

    @Transaction
    @Query("SELECT * FROM notes WHERE noteId = :id")
    suspend fun getNoteWithMedia(id: Long): NoteWithMedia?

    // Insert/Update/Delete
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)
}

1.8. NoteMediaDao (`data/local/dao/NoteMediaDao.kt`)

@Dao
interface NoteMediaDao {
    // Observe
    @Query("SELECT * FROM note_media WHERE noteId = :noteId ORDER BY addedAt ASC")
    fun observeMediaForNote(noteId: Long): Flow<List<NoteMediaEntity>>

    // Get
    @Query("SELECT * FROM note_media WHERE mediaId = :id")
    suspend fun getMediaById(id: Long): NoteMediaEntity?

    @Query("SELECT COUNT(*) FROM note_media WHERE filePath = :filePath")
    suspend fun countMediaReferences(filePath: String): Int

    // Insert/Update/Delete
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertMedia(media: NoteMediaEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertMediaList(mediaList: List<NoteMediaEntity>): List<Long>

    @Update
    suspend fun updateMedia(media: NoteMediaEntity)

    @Delete
    suspend fun deleteMedia(media: NoteMediaEntity)

    @Query("DELETE FROM note_media WHERE noteId = :noteId")
    suspend fun deleteAllMediaForNote(noteId: Long)
}

2. Embedded Relationship Classes (`data/local/entity/`)

2.1. ClassWithSchedules.kt
data class ClassWithSchedules(
    @Embedded val classEntity: ClassEntity,
    @Relation(
        parentColumn = "classId",
        entityColumn = "classId"
    )
    val schedules: List<ScheduleEntity>
)

2.2. SessionWithRecords.kt
data class SessionWithRecords(
    @Embedded val session: AttendanceSessionEntity,
    @Relation(
        parentColumn = "sessionId",
        entityColumn = "sessionId"
    )
    val records: List<AttendanceRecordEntity>
)

2.3. NoteWithMedia.kt
data class NoteWithMedia(
    @Embedded val note: NoteEntity,
    @Relation(
        parentColumn = "noteId",
        entityColumn = "noteId"
    )
    val media: List<NoteMediaEntity>
)

3. Domain Models (`domain/model/`)

IMPORTANT: Domain models are plain data classes with NO Room annotations.
They live in the domain layer and are used by UI/ViewModel code.

3.1. Class.kt
data class Class(
    val classId: Long,
    val instituteName: String,
    val session: String,
    val department: String,
    val semester: String,
    val section: String,
    val subject: String,
    val className: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val isOpen: Boolean,
    val createdAt: LocalDate
)

3.2. Student.kt
data class Student(
    val studentId: Long,
    val name: String,
    val registrationNumber: String,
    val rollNumber: String?,
    val email: String?,
    val phone: String?,
    val isActive: Boolean,
    val createdAt: LocalDate
)

3.3. ClassStudentLink.kt
data class ClassStudentLink(
    val classId: Long,
    val studentId: Long,
    val isActiveInClass: Boolean,
    val addedAt: LocalDate
)

3.4. Schedule.kt
data class Schedule(
    val scheduleId: Long,
    val classId: Long,
    val dayOfWeek: DayOfWeek,
    val startTime: LocalTime,
    val endTime: LocalTime
)

3.5. AttendanceSession.kt
data class AttendanceSession(
    val sessionId: Long,
    val classId: Long,
    val scheduleId: Long,
    val date: LocalDate,
    val lessonNotes: String?,
    val createdAt: LocalDate
)

3.6. AttendanceRecord.kt
data class AttendanceRecord(
    val recordId: Long,
    val sessionId: Long,
    val studentId: Long,
    val isPresent: Boolean
)

3.7. Note.kt
data class Note(
    val noteId: Long,
    val title: String,
    val content: String,
    val date: LocalDate,
    val createdAt: LocalDate,
    val updatedAt: LocalDate
)

3.8. NoteMedia.kt
data class NoteMedia(
    val mediaId: Long,
    val noteId: Long,
    val filePath: String,
    val mimeType: String,
    val addedAt: LocalDate
)

4. Mappers (`domain/mapper/EntityDomainMappers.kt`)

Use extension functions for bidirectional mapping.
Pattern: Entity -> Domain uses .toDomain(), Domain -> Entity uses .toEntity()

4.1. ClassEntity <-> Class
fun ClassEntity.toDomain() = Class(
    classId = classId,
    instituteName = instituteName,
    session = session,
    department = department,
    semester = semester,
    section = section,
    subject = subject,
    className = className,
    startDate = startDate,
    endDate = endDate,
    isOpen = isOpen,
    createdAt = createdAt
)

fun Class.toEntity() = ClassEntity(
    classId = classId,
    instituteName = instituteName,
    session = session,
    department = department,
    semester = semester,
    section = section,
    subject = subject,
    className = className,
    startDate = startDate,
    endDate = endDate,
    isOpen = isOpen,
    createdAt = createdAt
)

4.2. StudentEntity <-> Student
fun StudentEntity.toDomain() = Student(
    studentId = studentId,
    name = name,
    registrationNumber = registrationNumber,
    rollNumber = rollNumber,
    email = email,
    phone = phone,
    isActive = isActive,
    createdAt = createdAt
)

fun Student.toEntity() = StudentEntity(
    studentId = studentId,
    name = name,
    registrationNumber = registrationNumber,
    rollNumber = rollNumber,
    email = email,
    phone = phone,
    isActive = isActive,
    createdAt = createdAt
)

4.3. ClassStudentCrossRef <-> ClassStudentLink
fun ClassStudentCrossRef.toDomain() = ClassStudentLink(
    classId = classId,
    studentId = studentId,
    isActiveInClass = isActiveInClass,
    addedAt = addedAt
)

fun ClassStudentLink.toEntity() = ClassStudentCrossRef(
    classId = classId,
    studentId = studentId,
    isActiveInClass = isActiveInClass,
    addedAt = addedAt
)

4.4. ScheduleEntity <-> Schedule
fun ScheduleEntity.toDomain() = Schedule(
    scheduleId = scheduleId,
    classId = classId,
    dayOfWeek = dayOfWeek,
    startTime = startTime,
    endTime = endTime
)

fun Schedule.toEntity() = ScheduleEntity(
    scheduleId = scheduleId,
    classId = classId,
    dayOfWeek = dayOfWeek,
    startTime = startTime,
    endTime = endTime
)

4.5. AttendanceSessionEntity <-> AttendanceSession
fun AttendanceSessionEntity.toDomain() = AttendanceSession(
    sessionId = sessionId,
    classId = classId,
    scheduleId = scheduleId,
    date = date,
    lessonNotes = lessonNotes,
    createdAt = createdAt
)

fun AttendanceSession.toEntity() = AttendanceSessionEntity(
    sessionId = sessionId,
    classId = classId,
    scheduleId = scheduleId,
    date = date,
    lessonNotes = lessonNotes,
    createdAt = createdAt
)

4.6. AttendanceRecordEntity <-> AttendanceRecord
fun AttendanceRecordEntity.toDomain() = AttendanceRecord(
    recordId = recordId,
    sessionId = sessionId,
    studentId = studentId,
    isPresent = isPresent
)

fun AttendanceRecord.toEntity() = AttendanceRecordEntity(
    recordId = recordId,
    sessionId = sessionId,
    studentId = studentId,
    isPresent = isPresent
)

4.7. NoteEntity <-> Note
fun NoteEntity.toDomain() = Note(
    noteId = noteId,
    title = title,
    content = content,
    date = date,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Note.toEntity() = NoteEntity(
    noteId = noteId,
    title = title,
    content = content,
    date = date,
    createdAt = createdAt,
    updatedAt = updatedAt
)

4.8. NoteMediaEntity <-> NoteMedia
fun NoteMediaEntity.toDomain() = NoteMedia(
    mediaId = mediaId,
    noteId = noteId,
    filePath = filePath,
    mimeType = mimeType,
    addedAt = addedAt
)

fun NoteMedia.toEntity() = NoteMediaEntity(
    mediaId = mediaId,
    noteId = noteId,
    filePath = filePath,
    mimeType = mimeType,
    addedAt = addedAt
)

4.9. List mappers
fun List<ClassEntity>.toDomain() = map { it.toDomain() }
fun List<Class>.toEntity() = map { it.toEntity() }

fun List<StudentEntity>.toDomain() = map { it.toDomain() }
fun List<Student>.toEntity() = map { it.toEntity() }

fun List<ClassStudentCrossRef>.toDomain() = map { it.toDomain() }
fun List<ClassStudentLink>.toEntity() = map { it.toEntity() }

fun List<ScheduleEntity>.toDomain() = map { it.toDomain() }
fun List<Schedule>.toEntity() = map { it.toEntity() }

fun List<AttendanceSessionEntity>.toDomain() = map { it.toDomain() }
fun List<AttendanceSession>.toEntity() = map { it.toEntity() }

fun List<AttendanceRecordEntity>.toDomain() = map { it.toDomain() }
fun List<AttendanceRecord>.toEntity() = map { it.toEntity() }

fun List<NoteEntity>.toDomain() = map { it.toDomain() }
fun List<Note>.toEntity() = map { it.toEntity() }

fun List<NoteMediaEntity>.toDomain() = map { it.toDomain() }
fun List<NoteMedia>.toEntity() = map { it.toEntity() }

5. Database Wiring

Update AppDatabase.kt to add DAO abstract getters:

abstract class AppDatabase : RoomDatabase() {
    abstract fun classDao(): ClassDao
    abstract fun studentDao(): StudentDao
    abstract fun classStudentCrossRefDao(): ClassStudentCrossRefDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun attendanceSessionDao(): AttendanceSessionDao
    abstract fun attendanceRecordDao(): AttendanceRecordDao
    abstract fun noteDao(): NoteDao
    abstract fun noteMediaDao(): NoteMediaDao

    companion object {
        const val DATABASE_NAME = "attenote.db"
    }
}

Done criteria:
- All 8 DAOs compile without errors.
- All embedded relationship classes compile.
- All 8 domain models compile without errors.
- All mappers compile with bidirectional coverage.
- AppDatabase compiles with all DAO getters.
- No UI/ViewModel imports of data.local.entity.* (manual audit).
- App builds and launches without crash.

Output format:
1. Changed files.
2. DAO method summary table:
   | DAO | Observe Methods | Get Methods | Insert Methods | Update/Delete |
3. Domain model coverage list.
4. Mapper coverage (Entity->Domain, Domain->Entity, List mappers).
5. Device gate command results.
6. Manual checklist with pass/fail:
   □ Build succeeds
   □ All DAOs compile
   □ All embedded relationship classes compile
   □ All domain models compile
   □ All mappers compile
   □ AppDatabase DAO getters compile
   □ App launches without crash
   □ Logcat shows no Room/DAO errors
```

### Git Commit Message (Step 04)
`feat(step-04): add dao layer, domain models, and entity-domain mappers`

## Prompt 05 - Repository Contracts + Implementations + Validation Utilities
```text
Implement repository interfaces and concrete behavior, including validation and normalization.

IMPORTANT: Repositories are the boundary where Entity-to-Domain mapping occurs.
- Repositories accept and return domain models (from domain/model/)
- Repositories use DAOs internally (which work with entities)
- All normalization, validation, and business logic lives in repositories
- Use mappers (from Step 04) for all Entity <-> Domain conversions

Implement these files:

Repository interfaces in `data/repository/`:
- `ClassRepository.kt`
- `StudentRepository.kt`
- `AttendanceRepository.kt`
- `NoteRepository.kt`
- `SettingsPreferencesRepository.kt`
- `BackupSupportRepository.kt`

Repository implementations in `data/repository/`:
- `ClassRepositoryImpl.kt`
- `StudentRepositoryImpl.kt`
- `AttendanceRepositoryImpl.kt`
- `NoteRepositoryImpl.kt`
- `SettingsPreferencesRepositoryImpl.kt`
- `BackupSupportRepositoryImpl.kt`

Utility classes in `data/repository/internal/`:
- `InputNormalizer.kt`
- `ScheduleValidation.kt`
- `RepositoryResult.kt` (sealed class for success/error)

1. Utility Classes

1.1. InputNormalizer (`data/repository/internal/InputNormalizer.kt`)

Input normalization rules (from CLAUDE.md):
- Trim leading/trailing whitespace
- Collapse internal whitespace to single space
- Case-insensitive comparison (store as-entered, compare lowercase)

object InputNormalizer {
    /**
     * Normalizes a string for storage and comparison.
     * - Trims leading/trailing whitespace
     * - Collapses internal whitespace to single space
     * - Returns normalized string (preserves original casing for storage)
     */
    fun normalize(input: String): String {
        return input.trim().replace(Regex("\\s+"), " ")
    }

    /**
     * Checks if two strings are equal after normalization and case-insensitive comparison.
     */
    fun areEqual(a: String, b: String): Boolean {
        return normalize(a).equals(normalize(b), ignoreCase = true)
    }
}

Applied to:
- ClassEntity fields: instituteName, session, department, semester, subject, section
- StudentEntity fields: name, registrationNumber

1.2. ScheduleValidation (`data/repository/internal/ScheduleValidation.kt`)

Schedule validation rules:
- endTime must be after startTime
- No overlapping schedules for same class+day

object ScheduleValidation {
    /**
     * Validates that endTime > startTime.
     * Returns error message if invalid, null if valid.
     */
    fun validateTimeOrder(startTime: LocalTime, endTime: LocalTime): String? {
        return if (endTime <= startTime) {
            "End time must be after start time"
        } else null
    }

    /**
     * Detects overlapping schedules for the same class and day.
     * Two schedules overlap if: start1 < end2 AND start2 < end1
     * Returns error message if overlap detected, null if valid.
     */
    fun validateNoOverlap(
        schedules: List<Schedule>,
        newSchedule: Schedule
    ): String? {
        val conflicting = schedules.filter { existing ->
            existing.scheduleId != newSchedule.scheduleId &&
            existing.classId == newSchedule.classId &&
            existing.dayOfWeek == newSchedule.dayOfWeek &&
            (existing.startTime < newSchedule.endTime && newSchedule.startTime < existing.endTime)
        }

        return if (conflicting.isNotEmpty()) {
            "Schedule overlaps with existing schedule on ${newSchedule.dayOfWeek}"
        } else null
    }
}

1.3. RepositoryResult (`data/repository/internal/RepositoryResult.kt`)

Sealed class for repository operation results:

sealed class RepositoryResult<out T> {
    data class Success<T>(val data: T) : RepositoryResult<T>()
    data class Error(val message: String) : RepositoryResult<Nothing>()
}

Extension helpers:
fun <T> RepositoryResult<T>.getOrNull(): T? = when (this) {
    is RepositoryResult.Success -> data
    is RepositoryResult.Error -> null
}

fun <T> RepositoryResult<T>.getOrThrow(): T = when (this) {
    is RepositoryResult.Success -> data
    is RepositoryResult.Error -> throw IllegalStateException(message)
}

2. Repository Interfaces

2.1. ClassRepository (`data/repository/ClassRepository.kt`)

interface ClassRepository {
    // Observe
    fun observeClasses(isOpen: Boolean): Flow<List<Class>>
    fun observeAllClasses(): Flow<List<Class>>

    // Get
    suspend fun getClassById(classId: Long): Class?
    suspend fun getClassWithSchedules(classId: Long): Pair<Class, List<Schedule>>?

    // Create (with validation)
    suspend fun createClass(
        class_: Class,
        schedules: List<Schedule>
    ): RepositoryResult<Long>

    // Update
    suspend fun updateClass(class_: Class): RepositoryResult<Unit>
    suspend fun updateClassOpenState(classId: Long, isOpen: Boolean): RepositoryResult<Unit>

    // Delete
    suspend fun deleteClass(classId: Long): RepositoryResult<Unit>
}

2.2. StudentRepository (`data/repository/StudentRepository.kt`)

interface StudentRepository {
    // Observe
    fun observeAllStudents(): Flow<List<Student>>
    fun observeActiveStudentsForClass(classId: Long): Flow<List<Student>>

    // Get
    suspend fun getStudentById(studentId: Long): Student?

    // Create (with validation)
    suspend fun createStudent(student: Student): RepositoryResult<Long>

    // Update (with validation)
    suspend fun updateStudent(student: Student): RepositoryResult<Unit>
    suspend fun updateStudentActiveState(studentId: Long, isActive: Boolean): RepositoryResult<Unit>

    // Delete
    suspend fun deleteStudent(studentId: Long): RepositoryResult<Unit>

    // Class roster operations
    suspend fun addStudentToClass(classId: Long, studentId: Long): RepositoryResult<Unit>
    suspend fun removeStudentFromClass(classId: Long, studentId: Long): RepositoryResult<Unit>
    suspend fun updateStudentActiveInClass(classId: Long, studentId: Long, isActive: Boolean): RepositoryResult<Unit>
}

2.3. AttendanceRepository (`data/repository/AttendanceRepository.kt`)

interface AttendanceRepository {
    // Observe
    fun observeSessionsForClass(classId: Long): Flow<List<AttendanceSession>>

    // Get
    suspend fun getSessionById(sessionId: Long): AttendanceSession?
    suspend fun findSession(classId: Long, scheduleId: Long, date: LocalDate): AttendanceSession?

    // Save attendance (idempotent - find-or-create session, replace records)
    suspend fun saveAttendance(
        classId: Long,
        scheduleId: Long,
        date: LocalDate,
        lessonNotes: String?,
        records: List<AttendanceRecord>
    ): RepositoryResult<Long> // Returns sessionId

    // Delete
    suspend fun deleteSession(sessionId: Long): RepositoryResult<Unit>
}

2.4. NoteRepository (`data/repository/NoteRepository.kt`)

interface NoteRepository {
    // Observe
    fun observeNotesForDate(date: LocalDate): Flow<List<Note>>
    fun observeAllNotes(): Flow<List<Note>>

    // Get
    suspend fun getNoteById(noteId: Long): Note?
    suspend fun getNoteWithMedia(noteId: Long): Pair<Note, List<NoteMedia>>?

    // Create
    suspend fun createNote(
        note: Note,
        mediaPaths: List<String> = emptyList()
    ): RepositoryResult<Long>

    // Update
    suspend fun updateNote(note: Note): RepositoryResult<Unit>
    suspend fun addMediaToNote(noteId: Long, mediaPaths: List<String>): RepositoryResult<Unit>

    // Delete (with best-effort media file cleanup)
    suspend fun deleteNote(noteId: Long): RepositoryResult<Unit>
    suspend fun deleteMedia(mediaId: Long): RepositoryResult<Unit>
}

2.5. SettingsPreferencesRepository (`data/repository/SettingsPreferencesRepository.kt`)

Session format enum:
enum class SessionFormat {
    CURRENT_YEAR,      // "2026"
    ACADEMIC_YEAR      // "2025-2026" or "2026-2027" based on current date
}

interface SettingsPreferencesRepository {
    // Observe preferences
    val isSetupComplete: Flow<Boolean>
    val name: Flow<String>
    val institute: Flow<String>
    val profileImagePath: Flow<String?>
    val biometricEnabled: Flow<Boolean>
    val sessionFormat: Flow<SessionFormat>

    // Update preferences
    suspend fun setSetupComplete(complete: Boolean)
    suspend fun setName(name: String)
    suspend fun setInstitute(institute: String)
    suspend fun setProfileImagePath(path: String?)
    suspend fun setBiometricEnabled(enabled: Boolean)
    suspend fun setSessionFormat(format: SessionFormat)

    // Clear all (for reset/logout)
    suspend fun clearAll()
}

DataStore preference keys:
- "is_setup_complete" -> Boolean (default: false)
- "user_name" -> String (default: "")
- "user_institute" -> String (default: "")
- "profile_image_path" -> String? (default: null)
- "biometric_enabled" -> Boolean (default: false)
- "session_format" -> String (default: "CURRENT_YEAR", stored as enum name)

2.6. BackupSupportRepository (`data/repository/BackupSupportRepository.kt`)

interface BackupSupportRepository {
    // Export backup to zip file
    suspend fun exportBackup(destinationUri: Uri): RepositoryResult<Unit>

    // Import backup from zip file
    suspend fun importBackup(sourceUri: Uri): RepositoryResult<Unit>

    // Check if restore journal exists (interrupted restore detection)
    suspend fun hasInterruptedRestore(): Boolean

    // Complete interrupted restore
    suspend fun completeInterruptedRestore(): RepositoryResult<Unit>
}

3. Repository Implementations

3.1. ClassRepositoryImpl

class ClassRepositoryImpl(
    private val classDao: ClassDao,
    private val scheduleDao: ScheduleDao,
    private val db: AppDatabase
) : ClassRepository {

    override fun observeClasses(isOpen: Boolean): Flow<List<Class>> =
        classDao.observeClassesByOpenState(isOpen).map { it.toDomain() }

    override fun observeAllClasses(): Flow<List<Class>> =
        classDao.observeAllClasses().map { it.toDomain() }

    override suspend fun getClassById(classId: Long): Class? =
        classDao.getClassById(classId)?.toDomain()

    override suspend fun getClassWithSchedules(classId: Long): Pair<Class, List<Schedule>>? {
        val classWithSchedules = classDao.getClassWithSchedules(classId) ?: return null
        return classWithSchedules.classEntity.toDomain() to classWithSchedules.schedules.toDomain()
    }

    override suspend fun createClass(
        class_: Class,
        schedules: List<Schedule>
    ): RepositoryResult<Long> {
        // 1. Normalize class identity fields
        val normalizedClass = class_.copy(
            instituteName = InputNormalizer.normalize(class_.instituteName),
            session = InputNormalizer.normalize(class_.session),
            department = InputNormalizer.normalize(class_.department),
            semester = InputNormalizer.normalize(class_.semester),
            subject = InputNormalizer.normalize(class_.subject),
            section = InputNormalizer.normalize(class_.section)
        )

        // 2. Check for duplicate class (case-insensitive, normalized)
        // Note: Room unique constraint will catch exact duplicates, but we check normalized first
        // for better error messages

        // 3. Validate schedules
        for (schedule in schedules) {
            ScheduleValidation.validateTimeOrder(schedule.startTime, schedule.endTime)?.let {
                return RepositoryResult.Error(it)
            }
        }

        // Check for overlaps within new schedules
        for (i in schedules.indices) {
            for (j in i + 1 until schedules.size) {
                if (schedules[i].dayOfWeek == schedules[j].dayOfWeek) {
                    ScheduleValidation.validateNoOverlap(
                        listOf(schedules[i]),
                        schedules[j]
                    )?.let {
                        return RepositoryResult.Error(it)
                    }
                }
            }
        }

        // 4. Insert class + schedules in transaction
        return try {
            val classId = db.withTransaction {
                val id = classDao.insertClass(normalizedClass.toEntity())
                val schedulesWithClassId = schedules.map { it.copy(classId = id) }
                scheduleDao.insertSchedules(schedulesWithClassId.toEntity())
                id
            }
            RepositoryResult.Success(classId)
        } catch (e: SQLiteConstraintException) {
            RepositoryResult.Error("Class already exists with this identity")
        } catch (e: Exception) {
            RepositoryResult.Error("Failed to create class: ${e.message}")
        }
    }

    override suspend fun updateClass(class_: Class): RepositoryResult<Unit> {
        return try {
            classDao.updateClass(class_.toEntity())
            RepositoryResult.Success(Unit)
        } catch (e: Exception) {
            RepositoryResult.Error("Failed to update class: ${e.message}")
        }
    }

    override suspend fun updateClassOpenState(classId: Long, isOpen: Boolean): RepositoryResult<Unit> {
        return try {
            val class_ = classDao.getClassById(classId)
                ?: return RepositoryResult.Error("Class not found")
            classDao.updateClass(class_.copy(isOpen = isOpen))
            RepositoryResult.Success(Unit)
        } catch (e: Exception) {
            RepositoryResult.Error("Failed to update class state: ${e.message}")
        }
    }

    override suspend fun deleteClass(classId: Long): RepositoryResult<Unit> {
        return try {
            val class_ = classDao.getClassById(classId)
                ?: return RepositoryResult.Error("Class not found")
            classDao.deleteClass(class_)
            RepositoryResult.Success(Unit)
        } catch (e: Exception) {
            RepositoryResult.Error("Failed to delete class: ${e.message}")
        }
    }
}

3.2. StudentRepositoryImpl

Similar pattern:
- Normalize name and registrationNumber before save
- Check for duplicate (name, registrationNumber) case-insensitively
- Use transactions for multi-table operations
- Return RepositoryResult with error messages

3.3. AttendanceRepositoryImpl

Key behavior:
```kotlin
override suspend fun saveAttendance(
    classId: Long,
    scheduleId: Long,
    date: LocalDate,
    lessonNotes: String?,
    records: List<AttendanceRecord>
): RepositoryResult<Long> {
    return try {
        val sessionId = db.withTransaction {
            // Find or create session (idempotent)
            val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val existingSession = attendanceSessionDao.findSession(classId, scheduleId, dateString)

            val sid = if (existingSession != null) {
                // Update existing session
                attendanceSessionDao.updateSession(
                    existingSession.copy(lessonNotes = lessonNotes)
                )
                existingSession.sessionId
            } else {
                // Create new session
                attendanceSessionDao.insertSession(
                    AttendanceSessionEntity(
                        sessionId = 0,
                        classId = classId,
                        scheduleId = scheduleId,
                        date = date,
                        lessonNotes = lessonNotes,
                        createdAt = LocalDate.now()
                    )
                )
            }

            // Replace all records for this session
            attendanceRecordDao.deleteAllRecordsForSession(sid)
            val recordsWithSession = records.map { it.copy(sessionId = sid) }
            attendanceRecordDao.insertRecords(recordsWithSession.toEntity())

            sid
        }
        RepositoryResult.Success(sessionId)
    } catch (e: Exception) {
        RepositoryResult.Error("Failed to save attendance: ${e.message}")
    }
}
```

3.4. NoteRepositoryImpl

Key behavior:
- createNote: transaction to insert note + media entities
- deleteNote: delete note (CASCADE deletes media), then best-effort file cleanup
- Best-effort file cleanup: wrap in try-catch, log errors but don't fail operation

3.5. SettingsPreferencesRepositoryImpl

Use DataStore Preferences:
```kotlin
class SettingsPreferencesRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : SettingsPreferencesRepository {

    private object PreferencesKeys {
        val IS_SETUP_COMPLETE = booleanPreferencesKey("is_setup_complete")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_INSTITUTE = stringPreferencesKey("user_institute")
        val PROFILE_IMAGE_PATH = stringPreferencesKey("profile_image_path")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val SESSION_FORMAT = stringPreferencesKey("session_format")
    }

    override val isSetupComplete: Flow<Boolean> =
        dataStore.data.map { it[PreferencesKeys.IS_SETUP_COMPLETE] ?: false }

    override val name: Flow<String> =
        dataStore.data.map { it[PreferencesKeys.USER_NAME] ?: "" }

    // ... similar for other fields

    override val sessionFormat: Flow<SessionFormat> =
        dataStore.data.map {
            val value = it[PreferencesKeys.SESSION_FORMAT] ?: SessionFormat.CURRENT_YEAR.name
            SessionFormat.valueOf(value)
        }

    override suspend fun setSetupComplete(complete: Boolean) {
        dataStore.edit { it[PreferencesKeys.IS_SETUP_COMPLETE] = complete }
    }

    // ... similar setters
}
```

3.6. BackupSupportRepositoryImpl

Backup format:
- ZIP file containing:
  - attenote.db (Room database, WAL checkpointed)
  - datastore/*.preferences_pb (DataStore files)
  - app_images/*.jpg (profile/note media)
  - backup_manifest.json (metadata + checksums)

Manifest structure:
```json
{
  "applicationId": "com.uteacher.attendancetracker",
  "buildVariant": "debug",
  "appVersion": "1.0.0",
  "schemaVersion": 1,
  "timestamp": "2026-02-13T14:30:00Z",
  "fileChecksums": {
    "attenote.db": "sha256:...",
    "datastore/preferences.preferences_pb": "sha256:..."
  }
}
```

Export process:
1. Checkpoint WAL: db.query("PRAGMA wal_checkpoint(TRUNCATE)")
2. Close database
3. Create ZIP with all files
4. Generate manifest with checksums
5. Reopen database

Import/restore process (staged with journal):
1. Validate manifest (applicationId, schemaVersion)
2. Verify all checksums
3. Write restore_journal.json: {"phase": "extracting", "timestamp": "...", "paths": {...}}
4. Extract to staging directory
5. Update journal: {"phase": "swapping", ...}
6. Rename current data dirs to _backup_old
7. Rename staged dirs to live paths
8. Update journal: {"phase": "complete", ...}
9. Delete journal and _backup_old
10. On failure: rollback using journal

Startup restore recovery:
- Check for restore_journal.json on app startup
- If found and phase != "complete": attempt rollback
- If found and phase == "complete": finish cleanup

Done criteria:
- All 6 repositories compile without errors.
- InputNormalizer and ScheduleValidation utilities compile.
- RepositoryResult sealed class compiles.
- SessionFormat enum defined.
- No crash on app startup with repository initialization.
- All normalization applied correctly (trim, collapse whitespace, case-insensitive).
- Schedule validation prevents overlaps and invalid time ranges.
- Idempotent attendance save works correctly.

Output format:
1. Changed files.
2. Repository method summary table:
   | Repository | Observe Methods | CRUD Methods | Special Operations |
3. Validation rules implemented.
4. Device gate command results.
5. Manual checklist with pass/fail:
   □ Build succeeds
   □ All repositories compile
   □ Utility classes compile
   □ RepositoryResult compiles
   □ App launches without crash
   □ Logcat shows repositories initialized successfully
```

### Git Commit Message (Step 05)
`feat(step-05): implement repositories with normalization and schedule validation`

## Prompt 06 - Koin DI + Startup Gate + Splash/Setup/Auth
```text
Implement dependency injection with Koin and the first-run/auth flow end-to-end.

## 1. Koin Dependency Injection Modules

1.1. AppModule (`di/AppModule.kt`)

val appModule = module {
    // Room Database
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "attendance_tracker_db"
        )
        .addTypeConverters(Converters())
        .fallbackToDestructiveMigration() // For dev; remove for production
        .build()
    }

    // DAOs (8 total)
    single { get<AppDatabase>().classDao() }
    single { get<AppDatabase>().studentDao() }
    single { get<AppDatabase>().classStudentCrossRefDao() }
    single { get<AppDatabase>().scheduleDao() }
    single { get<AppDatabase>().attendanceSessionDao() }
    single { get<AppDatabase>().attendanceRecordDao() }
    single { get<AppDatabase>().noteDao() }
    single { get<AppDatabase>().noteMediaDao() }

    // DataStore Preferences
    single {
        androidContext().dataStore
    }

    // BiometricHelper
    single { BiometricHelper(androidContext()) }
}

DataStore extension:
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "attenote_preferences")

1.2. RepositoryModule (`di/RepositoryModule.kt`)

val repositoryModule = module {
    single<ClassRepository> {
        ClassRepositoryImpl(
            classDao = get(),
            scheduleDao = get(),
            db = get()
        )
    }

    single<StudentRepository> {
        StudentRepositoryImpl(
            studentDao = get(),
            crossRefDao = get(),
            db = get()
        )
    }

    single<AttendanceRepository> {
        AttendanceRepositoryImpl(
            sessionDao = get(),
            recordDao = get(),
            db = get()
        )
    }

    single<NoteRepository> {
        NoteRepositoryImpl(
            noteDao = get(),
            mediaDao = get(),
            db = get(),
            context = androidContext()
        )
    }

    single<SettingsPreferencesRepository> {
        SettingsPreferencesRepositoryImpl(
            dataStore = get()
        )
    }

    single<BackupSupportRepository> {
        BackupSupportRepositoryImpl(
            db = get(),
            dataStore = get(),
            context = androidContext()
        )
    }
}

1.3. ViewModelModule (`di/ViewModelModule.kt`)

val viewModelModule = module {
    viewModel { SplashViewModel(get()) }
    viewModel { SetupViewModel(get(), get()) }
    viewModel { AuthGateViewModel(get()) }
    // Add more ViewModels in later steps
}

1.4. Application Class (`AttendanceTrackerApplication.kt`)

class AttendanceTrackerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@AttendanceTrackerApplication)
            modules(appModule, repositoryModule, viewModelModule)
        }
    }
}

Register in AndroidManifest.xml:
<application
    android:name=".AttendanceTrackerApplication"
    ...>

## 2. BiometricPrompt Helper

2.1. BiometricHelper (`util/BiometricHelper.kt`)

class BiometricHelper(private val context: Context) {

    private val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    private val biometricManager = BiometricManager.from(context)

    /**
     * Check if device has secure lock screen (PIN/pattern/password/biometric).
     * Used to enable/disable biometric lock toggle in Setup/Settings.
     */
    fun isDeviceSecure(): Boolean {
        return keyguardManager.isDeviceSecure
    }

    /**
     * Check if biometric authentication is available.
     * Returns one of:
     * - BIOMETRIC_SUCCESS: biometric hardware + enrollment OK
     * - BIOMETRIC_ERROR_NO_HARDWARE: no biometric sensor
     * - BIOMETRIC_ERROR_HW_UNAVAILABLE: sensor disabled/unavailable
     * - BIOMETRIC_ERROR_NONE_ENROLLED: no biometrics enrolled
     * - BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED: OS update required
     */
    fun canAuthenticate(): Int {
        return biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
    }

    /**
     * Create BiometricPrompt.PromptInfo configured for strong biometric or device credential.
     */
    fun createPromptInfo(
        title: String = "Authenticate",
        subtitle: String? = null,
        description: String? = null
    ): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .apply {
                subtitle?.let { setSubtitle(it) }
                description?.let { setDescription(it) }
            }
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()
        // Note: setNegativeButtonText NOT needed when DEVICE_CREDENTIAL is allowed
    }
}

## 3. MainActivity Startup Gate

3.1. MainActivity (`MainActivity.kt`)

class MainActivity : FragmentActivity() {

    private val settingsRepo: SettingsPreferencesRepository by inject()
    private val biometricHelper: BiometricHelper by inject()

    // State to control content visibility (prevent flash before auth)
    private var showContent by mutableStateOf(false)
    private var startDestination: AppRoute? by mutableStateOf(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply standard system bar policy (keep navigation and status bars visible)
        WindowCompat.setDecorFitsSystemWindows(window, true)

        lifecycleScope.launch {
            // Step 1: Read preferences
            val isSetupComplete = settingsRepo.isSetupComplete.first()
            val biometricEnabled = settingsRepo.biometricEnabled.first()

            // Step 2: Check for lock removal (if biometric was enabled but device lock removed)
            if (biometricEnabled && !biometricHelper.isDeviceSecure()) {
                // Device lock was removed - show warning and disable biometric pref
                showLockRemovedDialog()
                settingsRepo.setBiometricEnabled(false)
                // Proceed without auth gate
                startDestination = Dashboard
                showContent = true
                return@launch
            }

            // Step 3: Determine start destination
            startDestination = when {
                !isSetupComplete -> Splash // First run
                biometricEnabled -> null // Will trigger auth gate
                else -> Dashboard // Normal launch without auth
            }

            // Step 4: If biometric auth required, show prompt before content
            if (biometricEnabled) {
                showBiometricPrompt(
                    onSuccess = {
                        startDestination = Dashboard
                        showContent = true
                    },
                    onFailure = {
                        // Show auth gate screen for retry
                        startDestination = AuthGate
                        showContent = true
                    }
                )
            } else {
                showContent = true
            }
        }

        setContent {
            AttenoteTheme {
                if (showContent && startDestination != null) {
                    AppNavHost(startDestination = startDestination!!)
                } else {
                    // Loading state (blank or splash placeholder)
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }

    private fun showBiometricPrompt(onSuccess: () -> Unit, onFailure: () -> Unit) {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // User cancelled or error - show AuthGate for retry
                    onFailure()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // Single failed attempt (e.g., wrong fingerprint) - prompt stays open
                    // Do nothing here, user can retry
                }
            }
        )

        val promptInfo = biometricHelper.createPromptInfo(
            title = "Unlock attenote",
            description = "Authenticate to access your data"
        )

        biometricPrompt.authenticate(promptInfo)
    }

    private fun showLockRemovedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Biometric Lock Disabled")
            .setMessage("Your device lock screen has been removed. Biometric lock has been disabled. You can re-enable it in Settings after setting up a device lock.")
            .setPositiveButton("OK", null)
            .show()
    }
}

## 4. Screen ViewModels and UiState

4.1. SplashViewModel and SplashUiState (`ui/screen/splash/`)

data class SplashUiState(
    val isNavigating: Boolean = false
)

class SplashViewModel(
    private val settingsRepo: SettingsPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    fun onStartClicked() {
        _uiState.update { it.copy(isNavigating = true) }
        // Navigation handled by screen composable
    }
}

4.2. SetupViewModel and SetupUiState (`ui/screen/setup/`)

data class SetupUiState(
    val name: String = "",
    val institute: String = "",
    val profileImagePath: String? = null,
    val biometricEnabled: Boolean = false,
    val isDeviceSecure: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaveComplete: Boolean = false
)

class SetupViewModel(
    private val settingsRepo: SettingsPreferencesRepository,
    private val biometricHelper: BiometricHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(SetupUiState())
    val uiState: StateFlow<SetupUiState> = _uiState.asStateFlow()

    init {
        // Check device security status
        _uiState.update { it.copy(isDeviceSecure = biometricHelper.isDeviceSecure()) }
    }

    fun onNameChanged(name: String) {
        _uiState.update { it.copy(name = name, error = null) }
    }

    fun onInstituteChanged(institute: String) {
        _uiState.update { it.copy(institute = institute, error = null) }
    }

    fun onProfileImageSelected(imagePath: String?) {
        _uiState.update { it.copy(profileImagePath = imagePath, error = null) }
    }

    fun onBiometricToggled(enabled: Boolean) {
        if (_uiState.value.isDeviceSecure) {
            _uiState.update { it.copy(biometricEnabled = enabled, error = null) }
        }
    }

    fun onSaveClicked() {
        val state = _uiState.value

        // Validate required fields
        if (state.name.trim().isEmpty()) {
            _uiState.update { it.copy(error = "Name is required") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                // Save all preferences
                settingsRepo.setName(state.name.trim())
                settingsRepo.setInstitute(state.institute.trim())
                settingsRepo.setProfileImagePath(state.profileImagePath)
                settingsRepo.setBiometricEnabled(state.biometricEnabled)
                settingsRepo.setSetupComplete(true)

                _uiState.update { it.copy(isLoading = false, isSaveComplete = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to save: ${e.message}"
                    )
                }
            }
        }
    }
}

4.3. AuthGateViewModel and AuthGateUiState (`ui/screen/auth/`)

enum class AuthState {
    IDLE,
    AUTHENTICATING,
    SUCCESS,
    FAILURE,
    ERROR
}

data class AuthGateUiState(
    val authState: AuthState = AuthState.IDLE,
    val errorMessage: String? = null
)

class AuthGateViewModel(
    private val biometricHelper: BiometricHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthGateUiState())
    val uiState: StateFlow<AuthGateUiState> = _uiState.asStateFlow()

    fun onAuthSuccess() {
        _uiState.update { it.copy(authState = AuthState.SUCCESS) }
    }

    fun onAuthFailure(message: String) {
        _uiState.update {
            it.copy(
                authState = AuthState.FAILURE,
                errorMessage = message
            )
        }
    }

    fun onAuthError(message: String) {
        _uiState.update {
            it.copy(
                authState = AuthState.ERROR,
                errorMessage = message
            )
        }
    }

    fun onRetryClicked() {
        _uiState.update {
            it.copy(
                authState = AuthState.IDLE,
                errorMessage = null
            )
        }
    }
}

## 5. Screen Implementations

5.1. SplashScreen (`ui/screen/splash/SplashScreen.kt`)

@Composable
fun SplashScreen(
    onNavigateToSetup: () -> Unit,
    viewModel: SplashViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isNavigating) {
        if (uiState.isNavigating) {
            onNavigateToSetup()
        }
    }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                // App branding
                Text(
                    text = "attenote",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Track attendance, capture notes",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Start button
                AttenoteButton(
                    text = "Start",
                    onClick = { viewModel.onStartClicked() }
                )
            }
        }
    }
}

5.2. SetupScreen (`ui/screen/setup/SetupScreen.kt`)

@Composable
fun SetupScreen(
    onNavigateToDashboard: () -> Unit,
    viewModel: SetupViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Profile image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            // Copy image to internal storage
            val internalPath = copyImageToInternalStorage(context, it)
            viewModel.onProfileImageSelected(internalPath)
        }
    }

    // Navigate on save complete
    LaunchedEffect(uiState.isSaveComplete) {
        if (uiState.isSaveComplete) {
            onNavigateToDashboard()
        }
    }

    Scaffold(
        topBar = {
            AttenoteTopAppBar(
                title = "Setup",
                showBackButton = false
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Name field (required)
            AttenoteTextField(
                value = uiState.name,
                onValueChange = { viewModel.onNameChanged(it) },
                label = "Name *",
                enabled = !uiState.isLoading
            )

            // Institute field (optional)
            AttenoteTextField(
                value = uiState.institute,
                onValueChange = { viewModel.onInstituteChanged(it) },
                label = "Institute (optional)",
                enabled = !uiState.isLoading
            )

            // Profile image picker
            AttenoteSectionCard(title = "Profile Picture") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (uiState.profileImagePath != null) {
                        AsyncImage(
                            model = uiState.profileImagePath,
                            contentDescription = "Profile picture",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        AttenoteTextButton(
                            text = "Remove",
                            onClick = {
                                // Delete old file
                                uiState.profileImagePath?.let { path ->
                                    File(path).delete()
                                }
                                viewModel.onProfileImageSelected(null)
                            },
                            enabled = !uiState.isLoading
                        )
                    } else {
                        AttenoteButton(
                            text = "Select Image",
                            onClick = {
                                imagePickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            enabled = !uiState.isLoading
                        )
                    }
                }
            }

            // Biometric lock toggle
            AttenoteSectionCard(title = "Security") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Enable Biometric Lock",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Switch(
                            checked = uiState.biometricEnabled,
                            onCheckedChange = { viewModel.onBiometricToggled(it) },
                            enabled = uiState.isDeviceSecure && !uiState.isLoading
                        )
                    }
                    if (!uiState.isDeviceSecure) {
                        Text(
                            text = "Set up a screen lock in device settings to enable this feature.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Error message
            if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Save button
            AttenoteButton(
                text = "Save",
                onClick = { viewModel.onSaveClicked() },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        }
    }
}

/**
 * Copy selected image URI to internal storage as JPEG.
 * Returns internal file path or null on error.
 */
private fun copyImageToInternalStorage(context: Context, sourceUri: Uri): String? {
    return try {
        // Create app_images directory
        val imagesDir = File(context.filesDir, "app_images")
        if (!imagesDir.exists()) {
            imagesDir.mkdirs()
        }

        // Generate timestamped filename
        val timestamp = System.currentTimeMillis()
        val destFile = File(imagesDir, "profile_$timestamp.jpg")

        // Read source image and re-encode to JPEG
        context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
            val bitmap = BitmapFactory.decodeStream(inputStream)
            FileOutputStream(destFile).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            }
        }

        destFile.absolutePath
    } catch (e: Exception) {
        Log.e("SetupScreen", "Failed to copy image", e)
        null
    }
}

5.3. AuthGateScreen (`ui/screen/auth/AuthGateScreen.kt`)

@Composable
fun AuthGateScreen(
    onNavigateToDashboard: () -> Unit,
    viewModel: AuthGateViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as FragmentActivity

    val biometricHelper: BiometricHelper = get() // Koin injection in Composable

    // Trigger biometric prompt on screen entry
    LaunchedEffect(uiState.authState) {
        if (uiState.authState == AuthState.IDLE) {
            showBiometricPrompt(
                activity = activity,
                biometricHelper = biometricHelper,
                onSuccess = { viewModel.onAuthSuccess() },
                onFailure = { message -> viewModel.onAuthFailure(message) },
                onError = { message -> viewModel.onAuthError(message) }
            )
        } else if (uiState.authState == AuthState.SUCCESS) {
            onNavigateToDashboard()
        }
    }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Lock icon",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Authentication Required",
                    style = MaterialTheme.typography.headlineMedium
                )

                if (uiState.authState == AuthState.FAILURE || uiState.authState == AuthState.ERROR) {
                    uiState.errorMessage?.let { message ->
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    AttenoteButton(
                        text = "Retry",
                        onClick = { viewModel.onRetryClicked() }
                    )
                }
            }
        }
    }
}

private fun showBiometricPrompt(
    activity: FragmentActivity,
    biometricHelper: BiometricHelper,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit,
    onError: (String) -> Unit
) {
    val executor = ContextCompat.getMainExecutor(activity)
    val biometricPrompt = BiometricPrompt(
        activity,
        executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError(errString.toString())
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onFailure("Authentication failed. Try again.")
            }
        }
    )

    val promptInfo = biometricHelper.createPromptInfo(
        title = "Unlock attenote",
        description = "Authenticate to access your data"
    )

    biometricPrompt.authenticate(promptInfo)
}

## 6. Navigation Wiring

Update AppNavHost to handle conditional start destination:

@Composable
fun AppNavHost(
    startDestination: AppRoute,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable<Splash> {
            SplashScreen(
                onNavigateToSetup = {
                    navController.navigate(Setup) {
                        popUpTo(Splash) { inclusive = true }
                    }
                }
            )
        }

        composable<Setup> {
            SetupScreen(
                onNavigateToDashboard = {
                    navController.navigate(Dashboard) {
                        popUpTo(Setup) { inclusive = true }
                    }
                }
            )
        }

        composable<AuthGate> {
            AuthGateScreen(
                onNavigateToDashboard = {
                    navController.navigate(Dashboard) {
                        popUpTo(AuthGate) { inclusive = true }
                    }
                }
            )
        }

        composable<Dashboard> {
            DashboardScreen() // Placeholder for now
        }

        // Other routes remain placeholders for now
    }
}

## 7. Done Criteria

Build, install, and verify:
- `./gradlew :app:assembleDebug`
- `adb install -r app/build/outputs/apk/debug/app-debug.apk`
- `adb shell monkey -p com.uteacher.attendancetracker -c android.intent.category.LAUNCHER 1`

Manual testing checklist:

### First Run Flow
- [ ] Fresh install → Splash screen appears with app title and Start button
- [ ] Tap Start → Setup screen appears
- [ ] Setup: Name field empty → validation error on Save
- [ ] Setup: Enter name only → Save succeeds → Dashboard appears
- [ ] Close app → Relaunch → Dashboard appears directly (no Splash)

### Profile Image Flow
- [ ] Setup: Tap "Select Image" → image picker opens
- [ ] Select image → image preview appears as circular thumbnail
- [ ] Save → verify image copied to `app_images/profile_<timestamp>.jpg`
- [ ] Settings: Replace profile image → old file deleted
- [ ] Settings: Remove profile image → file deleted, preview gone

### Biometric Lock Flow
- [ ] Device with no lock screen → Setup: Biometric toggle disabled with helper text
- [ ] Enable device lock screen → Biometric toggle becomes enabled
- [ ] Enable Biometric Lock → Save → close app
- [ ] Relaunch → BiometricPrompt appears immediately
- [ ] Cancel prompt → AuthGate screen shows with Retry button
- [ ] Tap Retry → BiometricPrompt appears again
- [ ] Authenticate successfully → Dashboard appears
- [ ] Close app with biometric enabled → Relaunch → BiometricPrompt again

### Lock Removal Detection
- [ ] Enable biometric lock → save → close app
- [ ] Remove device lock screen in system settings
- [ ] Relaunch app → dialog appears: "Biometric Lock Disabled"
- [ ] Dismiss dialog → Dashboard appears
- [ ] Go to Settings → verify Biometric Lock is now disabled

### System UI Visibility
- [ ] All screens (Splash, Setup, AuthGate, Dashboard) → Android navigation buttons remain visible
- [ ] No screen hides system navigation bars

### Data Persistence
- [ ] Setup: Enter name "John Doe", institute "XYZ School" → Save
- [ ] Close app → Clear app data
- [ ] Relaunch → Splash appears (setup required again)
- [ ] Complete setup → Close app (normal, not force stop)
- [ ] Relaunch → Dashboard appears (setup data persisted)

## 8. Output Format

1. **Changed Files:**
   - di/AppModule.kt
   - di/RepositoryModule.kt
   - di/ViewModelModule.kt
   - AttendanceTrackerApplication.kt
   - AndroidManifest.xml
   - util/BiometricHelper.kt
   - MainActivity.kt
   - ui/screen/splash/SplashViewModel.kt
   - ui/screen/splash/SplashUiState.kt
   - ui/screen/splash/SplashScreen.kt
   - ui/screen/setup/SetupViewModel.kt
   - ui/screen/setup/SetupUiState.kt
   - ui/screen/setup/SetupScreen.kt
   - ui/screen/auth/AuthGateViewModel.kt
   - ui/screen/auth/AuthGateUiState.kt
   - ui/screen/auth/AuthGateScreen.kt
   - ui/navigation/AppNavHost.kt
   - Context.dataStore extension

2. **Navigation Flow Summary:**
   - Fresh install: Splash → Setup → Dashboard
   - Normal launch: Dashboard
   - Biometric enabled launch: MainActivity BiometricPrompt → Dashboard (or AuthGate on failure)
   - AuthGate retry: AuthGate → BiometricPrompt → Dashboard

3. **Device Gate Command Results:**
   ```
   ./gradlew :app:assembleDebug
   BUILD SUCCESSFUL in Xs

   adb install -r app/build/outputs/apk/debug/app-debug.apk
   Success

   adb shell monkey -p com.uteacher.attendancetracker -c android.intent.category.LAUNCHER 1
   Events injected: 1
   ```

4. **Manual Checklist with Pass/Fail:**
   (Complete the checklist above with actual test results)
```

### Git Commit Message (Step 06)
`feat(step-06): wire koin and implement startup, setup, and auth gate flows`

## Prompt 07 - Dashboard + Calendar + Quick Actions + Navigation Wiring
```text
Implement the complete dashboard module with selected-date behavior, week/month calendar UI (positioned at bottom), scheduled classes expansion, notes display, and quick action navigation with movable FAB.

IMPORTANT IMPLEMENTATION UPDATE (2026-02-13):
- Do not render a Compose top bar on Dashboard.
- Use the Activity title bar (native ActionBar) as the only top bar.
- Keep dashboard content below system bars and above navigation buttons.
- FAB side change supports both persisted preference and direct swipe left/right.

## 1. Domain Model for Dashboard

1.1. ScheduledClassItem (`domain/model/ScheduledClassItem.kt`)

/**
 * Represents a class scheduled for a specific date.
 * Derived from ClassWithSchedules by filtering schedules matching the target date's day of week.
 */
data class ScheduledClassItem(
    val classId: Long,
    val className: String,
    val subject: String,
    val scheduleId: Long,
    val dayOfWeek: DayOfWeek,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val instituteName: String,
    val session: String,
    val department: String,
    val semester: String,
    val section: String
)

## 2. FAB Position Preference

2.1. Add to SessionFormat enum file or create new enum (`domain/model/FabPosition.kt`)

enum class FabPosition {
    LEFT,
    RIGHT
}

2.2. Update SettingsPreferencesRepository interface

interface SettingsPreferencesRepository {
    // ... existing preferences
    val fabPosition: Flow<FabPosition>
    suspend fun setFabPosition(position: FabPosition)
}

2.3. Update SettingsPreferencesRepositoryImpl

DataStore preference key:
- "fab_position" -> String (default: "RIGHT", stored as enum name)

Mapping:
fun String.toFabPosition(): FabPosition = try {
    FabPosition.valueOf(this)
} catch (e: IllegalArgumentException) {
    FabPosition.RIGHT
}

## 3. DashboardUiState and ViewModel

3.1. DashboardUiState (`ui/screen/dashboard/DashboardUiState.kt`)

data class DashboardUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val currentMonth: YearMonth = YearMonth.now(),
    val calendarExpanded: Boolean = false, // Default: collapsed (week view)

    // Content for selected date
    val scheduledClasses: List<ScheduledClassItem> = emptyList(),
    val notes: List<Note> = emptyList(),

    // Dates with content (for calendar dot indicators)
    val datesWithClasses: Set<LocalDate> = emptySet(),
    val datesWithNotes: Set<LocalDate> = emptySet(),

    // Loading/error states
    val isLoading: Boolean = true,
    val error: String? = null,

    // FAB menu state
    val fabMenuExpanded: Boolean = false,
    val fabPosition: FabPosition = FabPosition.RIGHT
)

3.2. DashboardViewModel (`ui/screen/dashboard/DashboardViewModel.kt`)

class DashboardViewModel(
    private val classRepository: ClassRepository,
    private val noteRepository: NoteRepository,
    private val settingsRepo: SettingsPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
        loadFabPosition()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            try {
                // Collect open classes and notes
                combine(
                    classRepository.observeClasses(isOpen = true),
                    noteRepository.observeAllNotes()
                ) { classes, allNotes ->
                    Pair(classes, allNotes)
                }.collect { (classes, allNotes) ->
                    val selectedDate = _uiState.value.selectedDate

                    // Expand schedules for selected date
                    val scheduledClasses = expandSchedulesForDate(classes, selectedDate)

                    // Filter notes for selected date
                    val notesForDate = allNotes.filter { it.date == selectedDate }

                    // Calculate dates with content for calendar indicators (current week or month)
                    val datesWithClasses = calculateDatesWithClasses(classes)
                    val datesWithNotes = allNotes.map { it.date }.toSet()

                    _uiState.update {
                        it.copy(
                            scheduledClasses = scheduledClasses,
                            notes = notesForDate,
                            datesWithClasses = datesWithClasses,
                            datesWithNotes = datesWithNotes,
                            isLoading = false,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load dashboard: ${e.message}"
                    )
                }
            }
        }
    }

    private fun loadFabPosition() {
        viewModelScope.launch {
            settingsRepo.fabPosition.collect { position ->
                _uiState.update { it.copy(fabPosition = position) }
            }
        }
    }

    /**
     * Expand class schedules to ScheduledClassItems for target date.
     * - Filter classes where startDate <= targetDate <= endDate
     * - Match schedule dayOfWeek to targetDate.dayOfWeek
     */
    private fun expandSchedulesForDate(
        classes: List<Class>,
        targetDate: LocalDate
    ): List<ScheduledClassItem> {
        return classes.flatMap { classItem ->
            // Check if class is active on target date
            if (targetDate < classItem.startDate || targetDate > classItem.endDate) {
                return@flatMap emptyList()
            }

            // Filter schedules matching target date's day of week
            classItem.schedules.filter { schedule ->
                schedule.dayOfWeek == targetDate.dayOfWeek
            }.map { schedule ->
                ScheduledClassItem(
                    classId = classItem.classId,
                    className = classItem.className,
                    subject = classItem.subject,
                    scheduleId = schedule.scheduleId,
                    dayOfWeek = schedule.dayOfWeek,
                    startTime = schedule.startTime,
                    endTime = schedule.endTime,
                    instituteName = classItem.instituteName,
                    session = classItem.session,
                    department = classItem.department,
                    semester = classItem.semester,
                    section = classItem.section
                )
            }
        }.sortedBy { it.startTime }
    }

    /**
     * Calculate all dates that have scheduled classes.
     * Returns all dates with classes (used for dot indicators).
     */
    private fun calculateDatesWithClasses(classes: List<Class>): Set<LocalDate> {
        return classes.flatMap { classItem ->
            // Generate all dates within class date range
            val dateRange = mutableListOf<LocalDate>()
            var current = classItem.startDate
            while (!current.isAfter(classItem.endDate)) {
                // Check if this date matches any schedule
                if (classItem.schedules.any { it.dayOfWeek == current.dayOfWeek }) {
                    dateRange.add(current)
                }
                current = current.plusDays(1)
            }
            dateRange
        }.toSet()
    }

    /**
     * Get week range (Mon-Sun) containing the target date.
     */
    fun getWeekRange(date: LocalDate): List<LocalDate> {
        val dayOfWeek = date.dayOfWeek.value // 1=Monday, 7=Sunday
        val startOfWeek = date.minusDays((dayOfWeek - 1).toLong())
        return (0..6).map { startOfWeek.plusDays(it.toLong()) }
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update {
            it.copy(
                selectedDate = date,
                calendarExpanded = false // Auto-collapse to week view
            )
        }
        // Data automatically updates via Flow collection
    }

    fun onPreviousDayClicked() {
        val newDate = _uiState.value.selectedDate.minusDays(1)
        _uiState.update { it.copy(selectedDate = newDate) }
    }

    fun onNextDayClicked() {
        val newDate = _uiState.value.selectedDate.plusDays(1)
        _uiState.update { it.copy(selectedDate = newDate) }
    }

    fun onPreviousMonthClicked() {
        val newMonth = _uiState.value.currentMonth.minusMonths(1)
        _uiState.update { it.copy(currentMonth = newMonth) }
    }

    fun onNextMonthClicked() {
        val newMonth = _uiState.value.currentMonth.plusMonths(1)
        _uiState.update { it.copy(currentMonth = newMonth) }
    }

    fun onToggleCalendar() {
        _uiState.update { it.copy(calendarExpanded = !it.calendarExpanded) }
    }

    fun onToggleFabMenu() {
        _uiState.update { it.copy(fabMenuExpanded = !it.fabMenuExpanded) }
    }

    fun onDismissFabMenu() {
        _uiState.update { it.copy(fabMenuExpanded = false) }
    }

    fun onContentScrolled() {
        // Dismiss FAB menu when user scrolls content
        if (_uiState.value.fabMenuExpanded) {
            _uiState.update { it.copy(fabMenuExpanded = false) }
        }
    }
}

## 4. Dashboard Screen Implementation

4.1. DashboardScreen (`ui/screen/dashboard/DashboardScreen.kt`)

IMPORTANT:
- Remove `Scaffold(topBar = ...)` if present in draft code.
- The app uses native ActionBar only; Compose screen-level top bars are not allowed.
- Treat the calendar as a bottom-anchored panel in the final implementation.
- Keep hamburger menu/FAB above the calendar layer.
- Support vertical swipe gestures on calendar panel: up expands, down collapses.

@Composable
fun DashboardScreen(
    onNavigateToCreateClass: () -> Unit,
    onNavigateToManageClassList: () -> Unit,
    onNavigateToManageStudents: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToTakeAttendance: (classId: Long, scheduleId: Long, date: String) -> Unit,
    onNavigateToAddNote: (date: String, noteId: Long) -> Unit,
    viewModel: DashboardViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    // Detect scroll and dismiss FAB menu
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            viewModel.onContentScrolled()
        }
    }

    Scaffold(
        topBar = {
            AttenoteTopAppBar(
                title = "Dashboard",
                showBackButton = false
            )
        },
        floatingActionButton = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = when (uiState.fabPosition) {
                    FabPosition.RIGHT -> Alignment.BottomEnd
                    FabPosition.LEFT -> Alignment.BottomStart
                }
            ) {
                HamburgerFabMenu(
                    expanded = uiState.fabMenuExpanded,
                    onToggle = { viewModel.onToggleFabMenu() },
                    onDismiss = { viewModel.onDismissFabMenu() },
                    onCreateClass = onNavigateToCreateClass,
                    onManageClasses = onNavigateToManageClassList,
                    onManageStudents = onNavigateToManageStudents,
                    onSettings = onNavigateToSettings
                )
            }
        }
    ) { paddingValues ->
        // Loading/error states overlay
        Box(modifier = Modifier.fillMaxSize()) {
            // Content
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Selected date header
                item {
                    Text(
                        text = formatDateHeader(uiState.selectedDate),
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                // Scheduled Classes section
                item {
                    AttenoteSectionCard(title = "Scheduled Classes") {
                        if (uiState.scheduledClasses.isEmpty()) {
                            Text(
                                text = "No classes scheduled for this date",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(8.dp)
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                uiState.scheduledClasses.forEach { scheduledClass ->
                                    ScheduledClassCard(
                                        scheduledClass = scheduledClass,
                                        onTakeAttendance = {
                                            onNavigateToTakeAttendance(
                                                scheduledClass.classId,
                                                scheduledClass.scheduleId,
                                                uiState.selectedDate.toString()
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Notes section with "+ New Note" button
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Section header with action button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Notes",
                                style = MaterialTheme.typography.titleMedium
                            )
                            AttenoteTextButton(
                                text = "+ New Note",
                                onClick = {
                                    onNavigateToAddNote(
                                        uiState.selectedDate.toString(),
                                        -1L
                                    )
                                }
                            )
                        }

                        // Notes content card
                        AttenoteSectionCard(title = null) {
                            if (uiState.notes.isEmpty()) {
                                Text(
                                    text = "No notes for this date",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(8.dp)
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    uiState.notes.forEach { note ->
                                        NoteCard(
                                            note = note,
                                            onOpenNote = {
                                                onNavigateToAddNote(
                                                    note.date.toString(),
                                                    note.noteId
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Calendar section at bottom (collapsible week/month view)
                item {
                    CalendarSection(
                        expanded = uiState.calendarExpanded,
                        selectedDate = uiState.selectedDate,
                        currentMonth = uiState.currentMonth,
                        datesWithContent = uiState.datesWithClasses + uiState.datesWithNotes,
                        weekRange = viewModel.getWeekRange(uiState.selectedDate),
                        onDateSelected = { viewModel.onDateSelected(it) },
                        onPreviousDay = { viewModel.onPreviousDayClicked() },
                        onNextDay = { viewModel.onNextDayClicked() },
                        onPreviousMonth = { viewModel.onPreviousMonthClicked() },
                        onNextMonth = { viewModel.onNextMonthClicked() },
                        onToggleExpanded = { viewModel.onToggleCalendar() }
                    )
                }
            }

            // Loading state
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // Error state
            if (uiState.error != null) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text(text = uiState.error!!)
                }
            }
        }
    }
}

/**
 * Format date as "Wednesday, Jan 15, 2026"
 */
private fun formatDateHeader(date: LocalDate): String {
    val dayName = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
    val monthName = date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    return "$dayName, $monthName ${date.dayOfMonth}, ${date.year}"
}

3.2. ScheduledClassCard Component (`ui/screen/dashboard/components/ScheduledClassCard.kt`)

@Composable
fun ScheduledClassCard(
    scheduledClass: ScheduledClassItem,
    onTakeAttendance: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Time range
                Text(
                    text = "${scheduledClass.startTime} - ${scheduledClass.endTime}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                // Subject
                Text(
                    text = scheduledClass.subject,
                    style = MaterialTheme.typography.titleMedium
                )

                // Class name
                Text(
                    text = scheduledClass.className,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Take Attendance button
            AttenoteButton(
                text = "Take Attendance",
                onClick = onTakeAttendance
            )
        }
    }
}

3.3. NoteCard Component (`ui/screen/dashboard/components/NoteCard.kt`)

@Composable
fun NoteCard(
    note: Note,
    onOpenNote: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Title
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium
                )

                // Content preview (strip HTML, limit to 100 chars)
                val contentPreview = note.content
                    .replace(Regex("<[^>]*>"), "") // Strip HTML tags
                    .take(100)
                    .let { if (note.content.length > 100) "$it..." else it }

                if (contentPreview.isNotEmpty()) {
                    Text(
                        text = contentPreview,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Created + updated metadata
                Text(
                    text = "Created on ${formatRelativeTime(note.createdAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Updated on ${formatRelativeTime(note.updatedAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Open button
            AttenoteTextButton(
                text = "Open",
                onClick = onOpenNote
            )
        }
    }
}

private fun formatRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val minutes = diff / (1000 * 60)
    val hours = minutes / 60
    val days = hours / 24

    return when {
        minutes < 1 -> "just now"
        minutes < 60 -> "$minutes min ago"
        hours < 24 -> "$hours hr ago"
        days < 7 -> "$days day${if (days > 1) "s" else ""} ago"
        else -> {
            val date = LocalDate.ofEpochDay(timestamp / (1000 * 60 * 60 * 24))
            "${date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())} ${date.dayOfMonth}"
        }
    }
}

## 5. Calendar Component (Week + Month Views)

5.1. CalendarSection (`ui/screen/dashboard/components/CalendarSection.kt`)

@Composable
fun CalendarSection(
    expanded: Boolean,
    selectedDate: LocalDate,
    currentMonth: YearMonth,
    datesWithContent: Set<LocalDate>,
    weekRange: List<LocalDate>,
    onDateSelected: (LocalDate) -> Unit,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onToggleExpanded: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        if (expanded) {
            // Full month view
            MonthCalendarView(
                selectedDate = selectedDate,
                currentMonth = currentMonth,
                datesWithContent = datesWithContent,
                onDateSelected = onDateSelected,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth,
                onCollapse = onToggleExpanded
            )
        } else {
            // Week view (collapsed)
            WeekCalendarView(
                selectedDate = selectedDate,
                weekRange = weekRange,
                datesWithContent = datesWithContent,
                onDateSelected = onDateSelected,
                onPreviousDay = onPreviousDay,
                onNextDay = onNextDay,
                onExpand = onToggleExpanded
            )
        }
    }
}

5.2. WeekCalendarView (`ui/screen/dashboard/components/WeekCalendarView.kt`)

@Composable
fun WeekCalendarView(
    selectedDate: LocalDate,
    weekRange: List<LocalDate>,
    datesWithContent: Set<LocalDate>,
    onDateSelected: (LocalDate) -> Unit,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(12.dp)) {
        // Week navigation header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous day button
            IconButton(onClick = onPreviousDay) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous day")
            }

            // Selected day label
            Text(
                text = "${selectedDate.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())} ${selectedDate.dayOfMonth}, ${selectedDate.year}",
                style = MaterialTheme.typography.labelMedium
            )

            // Next day button
            IconButton(onClick = onNextDay) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next day")
            }

            // Expand button
            IconButton(onClick = onExpand) {
                Icon(Icons.Default.ExpandMore, contentDescription = "Expand to month view")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Day of week headers
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { dayName ->
                Text(
                    text = dayName,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Week row (7 date cells)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            weekRange.forEach { date ->
                Box(modifier = Modifier.weight(1f)) {
                    DateCell(
                        date = date,
                        isSelected = date == selectedDate,
                        isToday = date == LocalDate.now(),
                        hasContent = date in datesWithContent,
                        onClick = { onDateSelected(date) }
                    )
                }
            }
        }
    }
}

5.3. MonthCalendarView (`ui/screen/dashboard/components/MonthCalendarView.kt`)

@Composable
fun MonthCalendarView(
    selectedDate: LocalDate,
    currentMonth: YearMonth,
    datesWithContent: Set<LocalDate>,
    onDateSelected: (LocalDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onCollapse: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(12.dp)) {
        // Month header with navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousMonth) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month")
            }

            Text(
                text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
                style = MaterialTheme.typography.titleMedium
            )

            IconButton(onClick = onNextMonth) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next month")
            }

            // Collapse button
            IconButton(onClick = onCollapse) {
                Icon(Icons.Default.ExpandLess, contentDescription = "Collapse to week view")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Day of week headers
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { dayName ->
                Text(
                    text = dayName,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Date grid
        val firstDayOfMonth = currentMonth.atDay(1)
        val daysInMonth = currentMonth.lengthOfMonth()
        val startDayOfWeek = firstDayOfMonth.dayOfWeek.value // 1=Monday, 7=Sunday

        // Calculate grid cells (including leading empty cells)
        val totalCells = startDayOfWeek - 1 + daysInMonth
        val rows = (totalCells + 6) / 7

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height((rows * 48).dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Leading empty cells
            items(startDayOfWeek - 1) {
                Spacer(modifier = Modifier.size(48.dp))
            }

            // Date cells
            items(daysInMonth) { dayIndex ->
                val date = currentMonth.atDay(dayIndex + 1)
                DateCell(
                    date = date,
                    isSelected = date == selectedDate,
                    isToday = date == LocalDate.now(),
                    hasContent = date in datesWithContent,
                    onClick = { onDateSelected(date) }
                )
            }
        }
    }
}

5.4. DateCell (`ui/screen/dashboard/components/DateCell.kt`)

@Composable
fun DateCell(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    hasContent: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isToday -> MaterialTheme.colorScheme.primaryContainer
                    else -> Color.Transparent
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )

            // Content indicator dot
            if (hasContent) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.primary
                        )
                )
            }
        }
    }
}

## 6. HamburgerFabMenu Component

6.1. HamburgerFabMenu (`ui/screen/dashboard/components/HamburgerFabMenu.kt`)

@Composable
fun HamburgerFabMenu(
    expanded: Boolean,
    onToggle: () -> Unit,
    onDismiss: () -> Unit,
    onCreateClass: () -> Unit,
    onManageClasses: () -> Unit,
    onManageStudents: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // Scrim overlay when expanded
        // Auto-dismisses on: tap, scroll, date selection
        if (expanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .clickable(
                        onClick = onDismiss,
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    )
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Bottom)
        ) {
            // Menu items (visible when expanded)
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FabMenuItem(
                        text = "Settings",
                        icon = Icons.Default.Settings,
                        onClick = {
                            onSettings()
                            onDismiss()
                        }
                    )
                    FabMenuItem(
                        text = "Manage Students",
                        icon = Icons.Default.Person,
                        onClick = {
                            onManageStudents()
                            onDismiss()
                        }
                    )
                    FabMenuItem(
                        text = "Edit Class",
                        icon = Icons.Default.Edit,
                        onClick = {
                            onManageClasses()
                            onDismiss()
                        }
                    )
                    FabMenuItem(
                        text = "Create Class",
                        icon = Icons.Default.Add,
                        onClick = {
                            onCreateClass()
                            onDismiss()
                        }
                    )
                }
            }

            // Toggle FAB
            FloatingActionButton(
                onClick = onToggle,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Quick actions menu",
                    modifier = Modifier.rotate(if (expanded) 90f else 0f)
                )
            }
        }
    }
}

@Composable
fun FabMenuItem(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )

        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Icon(icon, contentDescription = text)
        }
    }
}

## 7. Navigation Wiring

Update AppNavHost:

composable<Dashboard> {
    DashboardScreen(
        onNavigateToCreateClass = {
            navController.navigate(CreateClass)
        },
        onNavigateToManageClassList = {
            navController.navigate(ManageClassList)
        },
        onNavigateToManageStudents = {
            navController.navigate(ManageStudents)
        },
        onNavigateToSettings = {
            navController.navigate(Settings)
        },
        onNavigateToTakeAttendance = { classId, scheduleId, date ->
            navController.navigate(TakeAttendance(classId, scheduleId, date))
        },
        onNavigateToAddNote = { date, noteId ->
            navController.navigate(AddNote(date, noteId))
        }
    )
}

## 8. Done Criteria

Build, install, and verify:
- `./gradlew :app:assembleDebug`
- `adb install -r app/build/outputs/apk/debug/app-debug.apk`
- `adb shell monkey -p com.uteacher.attendancetracker -c android.intent.category.LAUNCHER 1`

Manual testing checklist:

### Dashboard Loading
- [ ] Dashboard loads without crash
- [ ] Selected date defaults to today
- [ ] Calendar collapsed by default (shows week view)
- [ ] Today's date highlighted in week view
- [ ] Week range shown (Mon-Sun containing today)

### Week View (Collapsed Calendar)
- [ ] Week view shows 7 days (Mon-Sun) in single row
- [ ] Selected date highlighted with filled circle
- [ ] Today indicator (light background if today is in current week)
- [ ] Content dots appear on dates with classes/notes
- [ ] Tap date in week view → content updates, week view stays
- [ ] Tap left arrow → previous day
- [ ] Tap right arrow → next day
- [ ] Selected date label updates ("Jan 13, 2026")
- [ ] Tap expand chevron (v) → calendar expands to full month

### Month View (Expanded Calendar)
- [ ] Full month grid visible
- [ ] Month header shows "January 2026"
- [ ] Selected date highlighted
- [ ] Content dots on dates with classes/notes
- [ ] Tap "Previous month" → shows previous month
- [ ] Tap "Next month" → shows next month
- [ ] Tap date in month view → content updates, calendar auto-collapses to week view
- [ ] Tap collapse chevron (^) → calendar collapses to week view

### Date Selection & Content Updates
- [ ] Selected date shown in header ("Wednesday, Jan 15, 2026")
- [ ] Scheduled classes refresh for new date
- [ ] Notes refresh for new date
- [ ] Calendar auto-collapses after date selection (from month view)
- [ ] Week row remains aligned to selected date context after day navigation

### Scheduled Classes Section
- [ ] Classes appear only if date is within class date range AND matches schedule day
- [ ] Classes sorted by start time
- [ ] Class card shows: time range, subject, class name
- [ ] Empty state shown when no classes scheduled
- [ ] Tap "Take Attendance" → navigates to TakeAttendance with correct classId, scheduleId, date

### Notes Section
- [ ] Section header shows "Notes" title
- [ ] "+ New Note" button visible in section header
- [ ] Tap "+ New Note" → navigates to AddNote with selected date, noteId = -1
- [ ] Notes for selected date appear
- [ ] Notes sorted by updatedAt desc
- [ ] Note card shows: title, content preview (HTML stripped, 100 char limit), created-on metadata, updated-on metadata
- [ ] Empty state shown when no notes
- [ ] Tap "Open" → navigates to AddNote with correct date and noteId

### Hamburger FAB Menu
- [ ] Hamburger FAB visible in bottom-right (or bottom-left if preference set)
- [ ] Tap Hamburger FAB → menu expands with 4 actions
- [ ] Menu items appear above FAB in order: Create Class, Edit Class, Manage Students, Settings
- [ ] Scrim overlay appears when menu expanded
- [ ] Tap scrim → menu collapses
- [ ] Scroll content → menu auto-collapses
- [ ] Tap "Create Class" → navigates to CreateClass, menu closes
- [ ] Tap "Edit Class" → navigates to ManageClassList, menu closes
- [ ] Tap "Manage Students" → navigates to ManageStudents, menu closes
- [ ] Tap "Settings" → navigates to Settings, menu closes

### FAB Position Preference
- [ ] Default FAB position: bottom-right
- [ ] Go to Settings → change FAB position to LEFT
- [ ] Return to Dashboard → FAB now in bottom-left
- [ ] Go to Settings → change FAB position to RIGHT
- [ ] Return to Dashboard → FAB back in bottom-right
- [ ] Position persists after app restart

### Content Indicators
- [ ] Create a class with schedule for specific date → dot appears on that date in calendar (week & month views)
- [ ] Add a note for specific date → dot appears on that date in calendar
- [ ] Dates with both classes and notes show one dot

### Calendar Layout Position
- [ ] Content order: Selected date header → Scheduled Classes → Notes, with calendar anchored as bottom overlay panel
- [ ] Calendar remains visible at the bottom without requiring scroll-to-end
- [ ] Hamburger menu/FAB renders above the calendar panel
- [ ] Swipe up on calendar expands to month view
- [ ] Swipe down on calendar collapses to week view

### Edge Cases & Reactivity
- [ ] Select date with no classes and no notes → both sections show empty states
- [ ] Create class → Dashboard updates automatically (Flow reactivity)
- [ ] Create note → Dashboard updates automatically
- [ ] Calendar expand/collapse toggle works smoothly
- [ ] Week/month navigation doesn't cause flicker or lag
- [ ] Rapid date selection doesn't crash app

## 9. Output Format

1. **Changed Files:**
   - domain/model/ScheduledClassItem.kt
   - domain/model/FabPosition.kt (new)
   - data/repository/SettingsPreferencesRepository.kt (add fabPosition)
   - ui/screen/dashboard/DashboardUiState.kt
   - ui/screen/dashboard/DashboardViewModel.kt
   - ui/screen/dashboard/DashboardScreen.kt
   - ui/screen/dashboard/components/ScheduledClassCard.kt
   - ui/screen/dashboard/components/NoteCard.kt
   - ui/screen/dashboard/components/CalendarSection.kt (new)
   - ui/screen/dashboard/components/WeekCalendarView.kt (new)
   - ui/screen/dashboard/components/MonthCalendarView.kt (new)
   - ui/screen/dashboard/components/DateCell.kt
   - ui/screen/dashboard/components/HamburgerFabMenu.kt
   - ui/navigation/AppNavHost.kt
   - di/ViewModelModule.kt (add DashboardViewModel)

2. **Dashboard Data-Flow Summary:**
   - ViewModel observes ClassRepository.observeClasses(isOpen=true), NoteRepository.observeAllNotes(), and SettingsPreferencesRepository.fabPosition
   - On selectedDate change, filters content via combine operator and auto-collapses calendar
   - expandSchedulesForDate() matches class schedules to date's day of week
   - calculateDatesWithClasses() generates dot indicator set for calendar (all dates, not just current month)
   - getWeekRange() calculates Mon-Sun range for week view
   - onContentScrolled() dismisses FAB menu when user scrolls
   - Flow reactivity ensures automatic UI updates on data changes

3. **Device Gate Command Results:**
   ```
   ./gradlew :app:assembleDebug
   BUILD SUCCESSFUL in Xs

   adb install -r app/build/outputs/apk/debug/app-debug.apk
   Success

   adb shell monkey -p com.uteacher.attendancetracker -c android.intent.category.LAUNCHER 1
   Events injected: 1
   ```

4. **Manual Checklist with Pass/Fail:**
   (Complete the checklist above with actual test results)
```

### Git Commit Message (Step 07)
`feat(step-07): implement dashboard date flow, calendar ui, and quick actions`

## Prompt 08 - Create Class Module (UI + VM + Persistence)
```text
Implement the complete class creation flow with form inputs, auto-generation, validation, schedule editor, and repository persistence.

IMPORTANT IMPLEMENTATION UPDATE (2026-02-13):
- Use native ActionBar title/back only (no Compose top bar).
- `Save` must appear as right-side ActionBar action (menu item), not as a Compose top-row button.
- Dropdown/date/time pickers use text-box style picker fields (not outlined buttons).
- Date range fields are side by side with calendar trailing icons.
- Time picker is 12-hour with AM/PM selector.

## 1. CreateClassUiState and ScheduleSlotDraft

1.1. ScheduleSlotDraft (`ui/screen/createclass/ScheduleSlotDraft.kt`)

data class ScheduleSlotDraft(
    val dayOfWeek: DayOfWeek = DayOfWeek.MONDAY,
    val startTime: LocalTime = LocalTime.of(9, 0),
    val endTime: LocalTime = LocalTime.of(10, 30),
    val validationError: String? = null
)

1.2. CreateClassUiState (`ui/screen/createclass/CreateClassUiState.kt`)

data class CreateClassUiState(
    // Form fields
    val instituteName: String = "",
    val session: String = "", // Auto-filled from Settings, editable
    val department: String = "",
    val semester: String = "",
    val section: String = "", // Optional, defaults to ""
    val subject: String = "",
    val className: String = "", // Auto-generated "{subject} - {institute}", editable
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,

    // Schedule slots
    val schedules: List<ScheduleSlotDraft> = emptyList(),
    val currentSlot: ScheduleSlotDraft = ScheduleSlotDraft(),

    // Validation errors (per field)
    val instituteError: String? = null,
    val sessionError: String? = null,
    val departmentError: String? = null,
    val semesterError: String? = null,
    val subjectError: String? = null,
    val classNameError: String? = null,
    val dateRangeError: String? = null,
    val schedulesError: String? = null,

    // UI state
    val showDatePicker: Boolean = false,
    val datePickerTarget: DatePickerTarget? = null, // START_DATE or END_DATE
    val showTimePicker: Boolean = false,
    val timePickerTarget: TimePickerTarget? = null, // SLOT_START or SLOT_END
    val isLoading: Boolean = false,
    val saveSuccess: Boolean = false,
    val saveError: String? = null
)

enum class DatePickerTarget {
    START_DATE,
    END_DATE
}

enum class TimePickerTarget {
    SLOT_START,
    SLOT_END
}

## 2. CreateClassViewModel

2.1. CreateClassViewModel (`ui/screen/createclass/CreateClassViewModel.kt`)

class CreateClassViewModel(
    private val classRepository: ClassRepository,
    private val settingsRepo: SettingsPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateClassUiState())
    val uiState: StateFlow<CreateClassUiState> = _uiState.asStateFlow()

    init {
        loadSessionFormatPreference()
    }

    /**
     * Auto-fill session field based on user's default session format preference.
     */
    private fun loadSessionFormatPreference() {
        viewModelScope.launch {
            settingsRepo.sessionFormat.first().let { format ->
                val currentYear = LocalDate.now().year
                val session = when (format) {
                    SessionFormat.CURRENT_YEAR -> currentYear.toString()
                    SessionFormat.ACADEMIC_YEAR -> {
                        val currentDate = LocalDate.now()
                        val cutoffDate = LocalDate.of(currentYear, 6, 30)
                        if (currentDate.isAfter(cutoffDate)) {
                            "$currentYear-${currentYear + 1}"
                        } else {
                            "${currentYear - 1}-$currentYear"
                        }
                    }
                }
                _uiState.update { it.copy(session = session) }
            }
        }
    }

    // Form field handlers
    fun onInstituteChanged(value: String) {
        _uiState.update {
            it.copy(
                instituteName = value,
                instituteError = null,
                className = generateClassName(it.subject, value)
            )
        }
    }

    fun onSessionChanged(value: String) {
        _uiState.update { it.copy(session = value, sessionError = null) }
    }

    fun onDepartmentChanged(value: String) {
        _uiState.update { it.copy(department = value, departmentError = null) }
    }

    fun onSemesterChanged(value: String) {
        _uiState.update { it.copy(semester = value, semesterError = null) }
    }

    fun onSectionChanged(value: String) {
        _uiState.update { it.copy(section = value) }
    }

    fun onSubjectChanged(value: String) {
        _uiState.update {
            it.copy(
                subject = value,
                subjectError = null,
                className = generateClassName(value, it.instituteName)
            )
        }
    }

    fun onClassNameChanged(value: String) {
        // User can manually override auto-generated className
        _uiState.update { it.copy(className = value, classNameError = null) }
    }

    /**
     * Auto-generate className as "{subject} - {institute}".
     * Returns empty string if either field is empty.
     */
    private fun generateClassName(subject: String, institute: String): String {
        return if (subject.isNotBlank() && institute.isNotBlank()) {
            "$subject - $institute"
        } else {
            ""
        }
    }

    // Date picker handlers
    fun onStartDatePickerRequested() {
        _uiState.update {
            it.copy(
                showDatePicker = true,
                datePickerTarget = DatePickerTarget.START_DATE
            )
        }
    }

    fun onEndDatePickerRequested() {
        _uiState.update {
            it.copy(
                showDatePicker = true,
                datePickerTarget = DatePickerTarget.END_DATE
            )
        }
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update { state ->
            when (state.datePickerTarget) {
                DatePickerTarget.START_DATE -> state.copy(
                    startDate = date,
                    showDatePicker = false,
                    datePickerTarget = null,
                    dateRangeError = null
                )
                DatePickerTarget.END_DATE -> state.copy(
                    endDate = date,
                    showDatePicker = false,
                    datePickerTarget = null,
                    dateRangeError = null
                )
                null -> state.copy(showDatePicker = false)
            }
        }
    }

    fun onDatePickerDismissed() {
        _uiState.update {
            it.copy(
                showDatePicker = false,
                datePickerTarget = null
            )
        }
    }

    // Schedule slot handlers
    fun onSlotDayChanged(dayOfWeek: DayOfWeek) {
        _uiState.update {
            it.copy(
                currentSlot = it.currentSlot.copy(
                    dayOfWeek = dayOfWeek,
                    validationError = null
                )
            )
        }
    }

    fun onSlotStartTimePickerRequested() {
        _uiState.update {
            it.copy(
                showTimePicker = true,
                timePickerTarget = TimePickerTarget.SLOT_START
            )
        }
    }

    fun onSlotEndTimePickerRequested() {
        _uiState.update {
            it.copy(
                showTimePicker = true,
                timePickerTarget = TimePickerTarget.SLOT_END
            )
        }
    }

    fun onTimeSelected(time: LocalTime) {
        _uiState.update { state ->
            when (state.timePickerTarget) {
                TimePickerTarget.SLOT_START -> state.copy(
                    currentSlot = state.currentSlot.copy(
                        startTime = time,
                        validationError = null
                    ),
                    showTimePicker = false,
                    timePickerTarget = null
                )
                TimePickerTarget.SLOT_END -> state.copy(
                    currentSlot = state.currentSlot.copy(
                        endTime = time,
                        validationError = null
                    ),
                    showTimePicker = false,
                    timePickerTarget = null
                )
                null -> state.copy(showTimePicker = false)
            }
        }
    }

    fun onTimePickerDismissed() {
        _uiState.update {
            it.copy(
                showTimePicker = false,
                timePickerTarget = null
            )
        }
    }

    fun onAddScheduleSlot() {
        val state = _uiState.value
        val slot = state.currentSlot

        // Validate slot
        if (slot.startTime >= slot.endTime) {
            _uiState.update {
                it.copy(
                    currentSlot = slot.copy(
                        validationError = "End time must be after start time"
                    )
                )
            }
            return
        }

        // Check for overlap with existing slots on same day
        val overlaps = state.schedules.any { existingSlot ->
            existingSlot.dayOfWeek == slot.dayOfWeek &&
            !(slot.endTime <= existingSlot.startTime || slot.startTime >= existingSlot.endTime)
        }

        if (overlaps) {
            _uiState.update {
                it.copy(
                    currentSlot = slot.copy(
                        validationError = "Schedule overlaps with existing slot on ${slot.dayOfWeek}"
                    )
                )
            }
            return
        }

        // Add slot and reset draft
        _uiState.update {
            it.copy(
                schedules = it.schedules + slot,
                currentSlot = ScheduleSlotDraft(),
                schedulesError = null
            )
        }
    }

    fun onDeleteScheduleSlot(index: Int) {
        _uiState.update {
            it.copy(
                schedules = it.schedules.filterIndexed { i, _ -> i != index }
            )
        }
    }

    // Save handler
    fun onSaveClicked() {
        val state = _uiState.value

        // Validate all required fields
        val errors = mutableMapOf<String, String?>()

        if (state.instituteName.trim().isEmpty()) {
            errors["institute"] = "Institute name is required"
        }
        if (state.session.trim().isEmpty()) {
            errors["session"] = "Session is required"
        }
        if (state.department.trim().isEmpty()) {
            errors["department"] = "Department is required"
        }
        if (state.semester.trim().isEmpty()) {
            errors["semester"] = "Semester is required"
        }
        if (state.subject.trim().isEmpty()) {
            errors["subject"] = "Subject is required"
        }
        if (state.className.trim().isEmpty()) {
            errors["className"] = "Class name is required"
        }
        if (state.startDate == null || state.endDate == null) {
            errors["dateRange"] = "Start and end dates are required"
        } else if (state.startDate.isAfter(state.endDate)) {
            errors["dateRange"] = "Start date must be before or equal to end date"
        }
        if (state.schedules.isEmpty()) {
            errors["schedules"] = "At least one schedule slot is required"
        }

        if (errors.isNotEmpty()) {
            _uiState.update {
                it.copy(
                    instituteError = errors["institute"],
                    sessionError = errors["session"],
                    departmentError = errors["department"],
                    semesterError = errors["semester"],
                    subjectError = errors["subject"],
                    classNameError = errors["className"],
                    dateRangeError = errors["dateRange"],
                    schedulesError = errors["schedules"]
                )
            }
            return
        }

        // All validations passed - save to repository
        _uiState.update { it.copy(isLoading = true, saveError = null) }

        viewModelScope.launch {
            try {
                // Create domain Class object
                val classToCreate = Class(
                    classId = 0L, // Will be assigned by DB
                    instituteName = state.instituteName.trim(),
                    session = state.session.trim(),
                    department = state.department.trim(),
                    semester = state.semester.trim(),
                    section = state.section.trim(), // Can be empty string
                    subject = state.subject.trim(),
                    className = state.className.trim(),
                    startDate = state.startDate!!,
                    endDate = state.endDate!!,
                    isOpen = true, // New classes default to open
                    createdAt = System.currentTimeMillis(),
                    schedules = state.schedules.map { slot ->
                        Schedule(
                            scheduleId = 0L,
                            classId = 0L,
                            dayOfWeek = slot.dayOfWeek,
                            startTime = slot.startTime,
                            endTime = slot.endTime
                        )
                    }
                )

                // Call repository
                val result = classRepository.createClass(classToCreate)

                when (result) {
                    is RepositoryResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                saveSuccess = true
                            )
                        }
                    }
                    is RepositoryResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                saveError = result.message
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        saveError = "Failed to save class: ${e.message}"
                    )
                }
            }
        }
    }
}

## 3. CreateClassScreen Implementation

3.1. CreateClassScreen (`ui/screen/createclass/CreateClassScreen.kt`)

IMPORTANT:
- Do not use `Scaffold(topBar = AttenoteTopAppBar(...))` for this screen.
- Configure ActionBar right action via host Activity (route-scoped callback) and trigger `viewModel.onSaveClicked()`.
- Picker/dropdown triggers must be whole-field tappable, not icon-only.

@Composable
fun CreateClassScreen(
    onNavigateBack: () -> Unit,
    viewModel: CreateClassViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Navigate back on save success
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            AttenoteTopAppBar(
                title = "Create Class",
                showBackButton = true,
                onBackClick = onNavigateBack,
                actions = {
                    TextButton(
                        onClick = { viewModel.onSaveClicked() },
                        enabled = !uiState.isLoading
                    ) {
                        Text("Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Class Information Section
            AttenoteSectionCard(title = "Class Information") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Institute
                    AttenoteTextField(
                        value = uiState.instituteName,
                        onValueChange = { viewModel.onInstituteChanged(it) },
                        label = "Institute Name *",
                        enabled = !uiState.isLoading,
                        isError = uiState.instituteError != null,
                        supportingText = uiState.instituteError
                    )

                    // Session (auto-filled, editable)
                    AttenoteTextField(
                        value = uiState.session,
                        onValueChange = { viewModel.onSessionChanged(it) },
                        label = "Session *",
                        enabled = !uiState.isLoading,
                        isError = uiState.sessionError != null,
                        supportingText = uiState.sessionError ?: "Auto-filled from settings"
                    )

                    // Department
                    AttenoteTextField(
                        value = uiState.department,
                        onValueChange = { viewModel.onDepartmentChanged(it) },
                        label = "Department *",
                        enabled = !uiState.isLoading,
                        isError = uiState.departmentError != null,
                        supportingText = uiState.departmentError
                    )

                    // Semester dropdown
                    ExposedDropdownMenuBox(
                        expanded = false, // Implement dropdown state if needed
                        onExpandedChange = { }
                    ) {
                        AttenoteTextField(
                            value = uiState.semester,
                            onValueChange = { viewModel.onSemesterChanged(it) },
                            label = "Semester *",
                            enabled = !uiState.isLoading,
                            isError = uiState.semesterError != null,
                            supportingText = uiState.semesterError ?: "e.g., 1st, 2nd, 3rd"
                        )
                    }

                    // Section (optional)
                    AttenoteTextField(
                        value = uiState.section,
                        onValueChange = { viewModel.onSectionChanged(it) },
                        label = "Section (optional)",
                        enabled = !uiState.isLoading,
                        supportingText = "Leave empty for no section"
                    )

                    // Subject
                    AttenoteTextField(
                        value = uiState.subject,
                        onValueChange = { viewModel.onSubjectChanged(it) },
                        label = "Subject *",
                        enabled = !uiState.isLoading,
                        isError = uiState.subjectError != null,
                        supportingText = uiState.subjectError
                    )

                    // Class Name (auto-generated, editable)
                    AttenoteTextField(
                        value = uiState.className,
                        onValueChange = { viewModel.onClassNameChanged(it) },
                        label = "Class Name *",
                        enabled = !uiState.isLoading,
                        isError = uiState.classNameError != null,
                        supportingText = uiState.classNameError ?: "Auto-generated, can edit"
                    )
                }
            }

            // Date Range Section
            AttenoteSectionCard(title = "Date Range") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Start Date
                    OutlinedButton(
                        onClick = { viewModel.onStartDatePickerRequested() },
                        enabled = !uiState.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = uiState.startDate?.toString() ?: "Select Start Date *"
                        )
                    }

                    // End Date
                    OutlinedButton(
                        onClick = { viewModel.onEndDatePickerRequested() },
                        enabled = !uiState.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = uiState.endDate?.toString() ?: "Select End Date *"
                        )
                    }

                    // Date range error
                    if (uiState.dateRangeError != null) {
                        Text(
                            text = uiState.dateRangeError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Schedule Section
            AttenoteSectionCard(title = "Weekly Schedule") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Current slot draft
                    Text(
                        text = "Add Schedule Slot",
                        style = MaterialTheme.typography.labelLarge
                    )

                    // Day of week dropdown
                    ExposedDropdownMenuBox(
                        expanded = false,
                        onExpandedChange = { }
                    ) {
                        OutlinedButton(
                            onClick = { /* Show day picker */ },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(uiState.currentSlot.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()))
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Start time
                        OutlinedButton(
                            onClick = { viewModel.onSlotStartTimePickerRequested() },
                            enabled = !uiState.isLoading,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(uiState.currentSlot.startTime.toString())
                        }

                        // End time
                        OutlinedButton(
                            onClick = { viewModel.onSlotEndTimePickerRequested() },
                            enabled = !uiState.isLoading,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(uiState.currentSlot.endTime.toString())
                        }
                    }

                    // Slot validation error
                    if (uiState.currentSlot.validationError != null) {
                        Text(
                            text = uiState.currentSlot.validationError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    // Add slot button
                    AttenoteButton(
                        text = "Add Slot",
                        onClick = { viewModel.onAddScheduleSlot() },
                        enabled = !uiState.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )

                    HorizontalDivider()

                    // Existing slots list
                    Text(
                        text = "Scheduled Slots (${uiState.schedules.size})",
                        style = MaterialTheme.typography.labelLarge
                    )

                    if (uiState.schedules.isEmpty()) {
                        Text(
                            text = "No schedule slots added yet",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        uiState.schedules.forEachIndexed { index, slot ->
                            ScheduleSlotCard(
                                slot = slot,
                                onDelete = { viewModel.onDeleteScheduleSlot(index) },
                                enabled = !uiState.isLoading
                            )
                        }
                    }

                    // Schedules error
                    if (uiState.schedulesError != null) {
                        Text(
                            text = uiState.schedulesError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Save error
            if (uiState.saveError != null) {
                Text(
                    text = uiState.saveError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Loading indicator
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }

    // Date Picker Dialog
    if (uiState.showDatePicker) {
        AttenoteDatePickerDialog(
            onDateSelected = { viewModel.onDateSelected(it) },
            onDismiss = { viewModel.onDatePickerDismissed() }
        )
    }

    // Time Picker Dialog
    if (uiState.showTimePicker) {
        AttenoteTimePickerDialog(
            initialTime = when (uiState.timePickerTarget) {
                TimePickerTarget.SLOT_START -> uiState.currentSlot.startTime
                TimePickerTarget.SLOT_END -> uiState.currentSlot.endTime
                null -> LocalTime.now()
            },
            onTimeSelected = { viewModel.onTimeSelected(it) },
            onDismiss = { viewModel.onTimePickerDismissed() }
        )
    }
}

3.2. ScheduleSlotCard (`ui/screen/createclass/components/ScheduleSlotCard.kt`)

@Composable
fun ScheduleSlotCard(
    slot: ScheduleSlotDraft,
    onDelete: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = slot.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "${slot.startTime} - ${slot.endTime}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onDelete,
                enabled = enabled
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete slot",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

## 4. Done Criteria

Build, install, and verify:
- `./gradlew :app:assembleDebug`
- `adb install -r app/build/outputs/apk/debug/app-debug.apk`
- `adb shell monkey -p com.uteacher.attendancetracker -c android.intent.category.LAUNCHER 1`

Manual testing checklist:

### Form Loading
- [ ] Screen loads without crash
- [ ] Session field auto-filled based on Settings preference (CURRENT_YEAR or ACADEMIC_YEAR)
- [ ] All fields empty except session
- [ ] Back button works (navigate back without saving)

### Auto-Generation
- [ ] Enter subject "Computer Science" → className remains empty
- [ ] Enter institute "XYZ University" → className auto-updates to "Computer Science - XYZ University"
- [ ] Change subject to "Mathematics" → className updates to "Mathematics - XYZ University"
- [ ] Manually edit className to "Math 101" → manual edit persists
- [ ] Change subject again → className does NOT auto-update (manual override preserved)

### Date Pickers
- [ ] Tap Start Date field (textbox) → Material3 DatePicker dialog opens
- [ ] Select date → dialog closes, date displays in button
- [ ] Tap End Date field (textbox) → DatePicker opens
- [ ] Select date → dialog closes, date displays in button
- [ ] Dates persist when scrolling form

### Schedule Slot Creation
- [ ] Default slot: Monday 09:00 - 10:30
- [ ] Tap day field dropdown → can select different day
- [ ] Tap start time field → TimePicker opens, can select time (12h with AM/PM)
- [ ] Tap end time field → TimePicker opens, can select time (12h with AM/PM)
- [ ] Tap "Add Slot" → slot appears in list below
- [ ] Slot shows 12-hour time format with AM/PM
- [ ] Slot draft resets to Monday 09:00 - 10:30

### Schedule Validation
- [ ] Set slot startTime 10:00, endTime 09:00 → tap "Add Slot" → error: "End time must be after start time"
- [ ] Add Monday 09:00-10:00 → add Monday 09:30-11:00 → error: "Schedule overlaps with existing slot on Monday"
- [ ] Add Monday 09:00-10:00 → add Monday 10:00-11:00 → success (no overlap, times are adjacent)
- [ ] Add Monday 09:00-10:00 → add Tuesday 09:00-10:00 → success (different days)

### Schedule Slot Deletion
- [ ] Tap delete icon on slot → slot removed from list
- [ ] Delete all slots → list shows "No schedule slots added yet"

### Form Validation (Required Fields)
- [ ] Tap Save with empty form → errors shown on all required fields
- [ ] Fill institute only → tap Save → other fields still show errors
- [ ] Fill all required fields except schedules → tap Save → error: "At least one schedule slot is required"
- [ ] Fill all fields + add schedule → tap Save → validation passes

### Date Range Validation
- [ ] Set startDate 2026-01-15, endDate 2026-01-10 → tap Save → error: "Start date must be before or equal to end date"
- [ ] Set startDate 2026-01-15, endDate 2026-01-15 → tap Save → success (same date allowed)
- [ ] Set startDate 2026-01-15, endDate 2026-06-30 → tap Save → success

### Save Success
- [ ] Fill valid form → tap Save → loading indicator appears
- [ ] Save completes → navigate back to previous screen (Dashboard or ManageClassList)
- [ ] Navigate to Dashboard → new class appears in scheduled classes (if date matches)
- [ ] Navigate to ManageClassList → new class appears in list

### Save Error (Duplicate Class)
- [ ] Create class: Institute="ABC", Session="2026", Dept="CS", Sem="1", Subject="Math", Section=""
- [ ] Save → success
- [ ] Create another class with SAME identity fields
- [ ] Tap Save → error message: "Class with these details already exists" (or similar)
- [ ] Error message displayed, form remains editable

### Save Error (Overlapping Schedules - Repository Level)
- [ ] This validation happens at ViewModel level (already tested above)
- [ ] Repository validation is secondary safety check

### Input Normalization
- [ ] Enter institute " XYZ  University " (extra spaces)
- [ ] Enter subject "Computer   Science" (multiple spaces)
- [ ] Save → success
- [ ] Verify in database: fields trimmed and normalized (single spaces)

### Edge Cases
- [ ] Scroll form up/down → all field values persist
- [ ] Rotate device (if applicable) → form state persists
- [ ] Press back after editing → changes discarded, no save
- [ ] Add 10 schedule slots → all display correctly
- [ ] Section field: leave empty → saves as empty string (not null)

## 5. Output Format

1. **Changed Files:**
   - domain/model/Class.kt (already exists)
   - domain/model/Schedule.kt (already exists)
   - ui/screen/createclass/ScheduleSlotDraft.kt (new)
   - ui/screen/createclass/CreateClassUiState.kt
   - ui/screen/createclass/CreateClassViewModel.kt
   - ui/screen/createclass/CreateClassScreen.kt
   - ui/screen/createclass/components/ScheduleSlotCard.kt (new)
   - ui/components/AttenoteDatePickerDialog.kt (new themed component)
   - ui/components/AttenoteTimePickerDialog.kt (new themed component)
   - ui/navigation/AppNavHost.kt (wire CreateClass route)
   - di/ViewModelModule.kt (add CreateClassViewModel)

2. **Validation Rules Implemented:**
   - Required fields: instituteName, session, department, semester, subject, className, startDate, endDate, schedules (at least 1)
   - Section field: optional, defaults to empty string
   - Date range: startDate <= endDate
   - Schedule slot: startTime < endTime
   - Schedule overlap: same class, same day, overlapping time ranges
   - Input normalization: trim + collapse whitespace before save
   - Duplicate class detection: handled by repository, displayed as save error

3. **Device Gate Command Results:**
   ```
   ./gradlew :app:assembleDebug
   BUILD SUCCESSFUL in Xs

   adb install -r app/build/outputs/apk/debug/app-debug.apk
   Success

   adb shell monkey -p com.uteacher.attendancetracker -c android.intent.category.LAUNCHER 1
   Events injected: 1
   ```

4. **Manual Checklist with Pass/Fail:**
   (Complete the checklist above with actual test results)
```

### Git Commit Message (Step 08)
`feat(step-08): implement create class ui, validation, and schedule persistence`

### Step 07-08 Amendment (Source of Truth)
`This amendment supersedes conflicting older lines in Prompt 07/08.`

1. Top Bar and Save Action
- Use only native Activity ActionBar as global top bar.
- No Compose `TopAppBar` on screens.
- Route-specific right-side actions (for example, `Save` on CreateClass) are provided through host-level ActionBar menu wiring.

2. Dashboard (Step 07)
- Keep dashboard content inset-safe for status/navigation bars.
- FAB should remain tappable above system navigation area.
- FAB side can be changed by persisted preference and swipe gesture.

3. CreateClass (Step 08)
- Save is in ActionBar right action.
- Semester/day/date/time inputs use text-box style picker fields.
- Date range fields are side by side with calendar icons.
- Time picker is 12-hour (AM/PM).
- Picker/dropdown/date/time controls must open from whole-field tap, not just icon tap.
- Class name manual override is preserved after user edits.

## Prompt 09 - Manage Class List + Edit Class + Roster Operations
```text
Implement class management list, edit class screen with date range editing, and roster operations (manual add, CSV import, copy from class).

## 1. ManageClassList Module

1.1. ManageClassListUiState (`ui/screen/manageclass/ManageClassListUiState.kt`)

data class ManageClassListUiState(
    val classes: List<Class> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

1.2. ManageClassListViewModel (`ui/screen/manageclass/ManageClassListViewModel.kt`)

class ManageClassListViewModel(
    private val classRepository: ClassRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManageClassListUiState())
    val uiState: StateFlow<ManageClassListUiState> = _uiState.asStateFlow()

    init {
        loadClasses()
    }

    private fun loadClasses() {
        viewModelScope.launch {
            try {
                classRepository.observeAllClasses().collect { classes ->
                    _uiState.update {
                        it.copy(
                            classes = classes.sortedByDescending { cls -> cls.createdAt },
                            isLoading = false,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load classes: ${e.message}"
                    )
                }
            }
        }
    }

    fun onOpenClosedToggled(classId: Long, isOpen: Boolean) {
        viewModelScope.launch {
            classRepository.updateClassOpenState(classId, isOpen)
        }
    }
}

1.3. ManageClassListScreen (`ui/screen/manageclass/ManageClassListScreen.kt`)

@Composable
fun ManageClassListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEditClass: (classId: Long) -> Unit,
    viewModel: ManageClassListViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            AttenoteTopAppBar(
                title = "Manage Classes",
                showBackButton = true,
                onBackClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (uiState.classes.isEmpty()) {
                Text(
                    text = "No classes found.\nCreate a class from the Dashboard.",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.classes) { classItem ->
                        ClassCard(
                            classItem = classItem,
                            onOpenCloseToggled = { isOpen ->
                                viewModel.onOpenClosedToggled(classItem.classId, isOpen)
                            },
                            onEditClicked = {
                                onNavigateToEditClass(classItem.classId)
                            }
                        )
                    }
                }
            }

            if (uiState.error != null) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text(text = uiState.error!!)
                }
            }
        }
    }
}

1.4. ClassCard Component (`ui/screen/manageclass/components/ClassCard.kt`)

@Composable
fun ClassCard(
    classItem: Class,
    onOpenCloseToggled: (Boolean) -> Unit,
    onEditClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header row: className + open/close switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = classItem.className,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (classItem.isOpen) "Open" else "Closed",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Switch(
                        checked = classItem.isOpen,
                        onCheckedChange = onOpenCloseToggled
                    )
                }
            }

            // Class details
            Text(
                text = "${classItem.subject} • ${classItem.semester}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "${classItem.instituteName} • ${classItem.session}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (classItem.section.isNotEmpty()) {
                Text(
                    text = "Section: ${classItem.section}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "${classItem.startDate} to ${classItem.endDate}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )

            // Edit button
            AttenoteTextButton(
                text = "Edit Class",
                onClick = onEditClicked,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

## 2. EditClass Module

2.1. EditClassUiState (`ui/screen/manageclass/EditClassUiState.kt`)

data class EditClassUiState(
    // Class data
    val classItem: Class? = null,

    // Editable date range
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val dateRangeError: String? = null,
    val outOfRangeWarning: String? = null, // Warning if attendance exists outside new range

    // Student roster
    val students: List<StudentInClass> = emptyList(), // Students linked to this class
    val allStudents: List<Student> = emptyList(), // All students (for copy/add operations)

    // Dialog states
    val showAddStudentDialog: Boolean = false,
    val showCsvImportDialog: Boolean = false,
    val showCopyFromClassDialog: Boolean = false,

    // CSV import state
    val csvPreviewData: List<CsvStudentRow> = emptyList(),
    val csvImportError: String? = null,

    // Copy from class state
    val availableClasses: List<Class> = emptyList(),

    // Date picker state
    val showDatePicker: Boolean = false,
    val datePickerTarget: DatePickerTarget? = null,

    // Loading/error states
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saveError: String? = null
)

data class StudentInClass(
    val student: Student,
    val isActiveInClass: Boolean
)

data class CsvStudentRow(
    val name: String,
    val registrationNumber: String,
    val rollNumber: String?,
    val email: String?,
    val phone: String?,
    val isDuplicate: Boolean = false, // Duplicate within CSV
    val alreadyExists: Boolean = false, // Already in database
    val hasWarning: Boolean = false
)

enum class DatePickerTarget {
    START_DATE,
    END_DATE
}

2.2. EditClassViewModel (`ui/screen/manageclass/EditClassViewModel.kt`)

class EditClassViewModel(
    private val classId: Long,
    private val classRepository: ClassRepository,
    private val studentRepository: StudentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditClassUiState())
    val uiState: StateFlow<EditClassUiState> = _uiState.asStateFlow()

    init {
        loadClassData()
    }

    private fun loadClassData() {
        viewModelScope.launch {
            try {
                // Load class details
                val classItem = classRepository.getClassById(classId)
                if (classItem == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            saveError = "Class not found"
                        )
                    }
                    return@launch
                }

                // Load students for this class
                // Note: studentRepository.observeActiveStudentsForClass should return students WITH their
                // isActiveInClass flag from ClassStudentCrossRef (via JOIN query in DAO)
                // Example DAO query:
                // @Query("""
                //     SELECT s.*, cscr.isActiveInClass FROM students s
                //     INNER JOIN class_student_cross_ref cscr ON s.studentId = cscr.studentId
                //     WHERE cscr.classId = :classId AND s.isActive = 1
                // """)
                // This returns StudentEntity with isActiveInClass accessible via a wrapper or Embedded relation

                studentRepository.observeActiveStudentsForClass(classId).collect { students ->
                    _uiState.update {
                        it.copy(
                            classItem = classItem,
                            startDate = classItem.startDate,
                            endDate = classItem.endDate,
                            students = students.map { student ->
                                // Assuming repository returns StudentWithClassStatus(student: Student, isActiveInClass: Boolean)
                                // If not, create this data class in domain layer and update repository contract
                                StudentInClass(student.student, student.isActiveInClass)
                            },
                            isLoading = false
                        )
                    }
                }

                // Load all students for add/copy operations
                studentRepository.observeAllStudents().collect { allStudents ->
                    _uiState.update { it.copy(allStudents = allStudents) }
                }

                // Load available classes for copy operation
                classRepository.observeAllClasses().collect { classes ->
                    _uiState.update {
                        it.copy(
                            availableClasses = classes.filter { cls -> cls.classId != classId }
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        saveError = "Failed to load class: ${e.message}"
                    )
                }
            }
        }
    }

    // Date range editing
    fun onStartDatePickerRequested() {
        _uiState.update {
            it.copy(
                showDatePicker = true,
                datePickerTarget = DatePickerTarget.START_DATE
            )
        }
    }

    fun onEndDatePickerRequested() {
        _uiState.update {
            it.copy(
                showDatePicker = true,
                datePickerTarget = DatePickerTarget.END_DATE
            )
        }
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update { state ->
            when (state.datePickerTarget) {
                DatePickerTarget.START_DATE -> state.copy(
                    startDate = date,
                    showDatePicker = false,
                    datePickerTarget = null,
                    dateRangeError = null,
                    outOfRangeWarning = null
                )
                DatePickerTarget.END_DATE -> state.copy(
                    endDate = date,
                    showDatePicker = false,
                    datePickerTarget = null,
                    dateRangeError = null,
                    outOfRangeWarning = null
                )
                null -> state.copy(showDatePicker = false)
            }
        }
    }

    fun onDatePickerDismissed() {
        _uiState.update {
            it.copy(
                showDatePicker = false,
                datePickerTarget = null
            )
        }
    }

    fun onSaveDateRange() {
        val state = _uiState.value
        val startDate = state.startDate
        val endDate = state.endDate

        // Validate
        if (startDate == null || endDate == null) {
            _uiState.update {
                it.copy(dateRangeError = "Both dates are required")
            }
            return
        }

        if (startDate.isAfter(endDate)) {
            _uiState.update {
                it.copy(dateRangeError = "Start date must be before or equal to end date")
            }
            return
        }

        _uiState.update { it.copy(isSaving = true, saveError = null) }

        viewModelScope.launch {
            try {
                // Check if attendance exists outside new range
                // Query AttendanceRepository to count sessions that fall outside the new date range
                val outOfRangeSessions = attendanceRepository.countSessionsOutsideDateRange(
                    classId = classId,
                    startDate = startDate,
                    endDate = endDate
                )

                // If attendance records exist outside new range, show warning dialog
                if (outOfRangeSessions > 0) {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            showDateRangeShrinkWarning = true,
                            outOfRangeSessionCount = outOfRangeSessions
                        )
                    }
                    // User must confirm via dialog, which calls onDateRangeShrinkConfirmed()
                    return@launch
                }

                // Proceed with save (no warning needed)
                val result = classRepository.updateClassDateRange(classId, startDate, endDate)

                when (result) {
                    is RepositoryResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                classItem = it.classItem?.copy(
                                    startDate = startDate,
                                    endDate = endDate
                                )
                            )
                        }
                        // Show success message
                    }
                    is RepositoryResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                saveError = result.message
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        saveError = "Failed to update date range: ${e.message}"
                    )
                }
            }
        }
    }

    // Student roster operations
    fun onToggleStudentActiveInClass(studentId: Long, isActive: Boolean) {
        viewModelScope.launch {
            studentRepository.updateStudentActiveInClass(classId, studentId, isActive)
        }
    }

    fun onShowAddStudentDialog() {
        _uiState.update { it.copy(showAddStudentDialog = true) }
    }

    fun onDismissAddStudentDialog() {
        _uiState.update { it.copy(showAddStudentDialog = false) }
    }

    fun onShowCsvImportDialog() {
        _uiState.update { it.copy(showCsvImportDialog = true) }
    }

    fun onDismissCsvImportDialog() {
        _uiState.update {
            it.copy(
                showCsvImportDialog = false,
                csvPreviewData = emptyList(),
                csvImportError = null
            )
        }
    }

    fun onShowCopyFromClassDialog() {
        _uiState.update { it.copy(showCopyFromClassDialog = true) }
    }

    fun onDismissCopyFromClassDialog() {
        _uiState.update { it.copy(showCopyFromClassDialog = false) }
    }

    // Manual add student
    fun onAddStudent(name: String, regNumber: String, rollNumber: String?) {
        viewModelScope.launch {
            // Create or find student
            val student = Student(
                studentId = 0L,
                name = name.trim(),
                registrationNumber = regNumber.trim(),
                rollNumber = rollNumber?.trim(),
                email = null,
                phone = null,
                isActive = true,
                createdAt = System.currentTimeMillis()
            )

            val result = studentRepository.createStudent(student)

            when (result) {
                is RepositoryResult.Success -> {
                    val studentId = result.data
                    // Link to class
                    studentRepository.addStudentToClass(classId, studentId)
                    _uiState.update { it.copy(showAddStudentDialog = false) }
                }
                is RepositoryResult.Error -> {
                    _uiState.update {
                        it.copy(saveError = result.message)
                    }
                }
            }
        }
    }

    // CSV import
    fun onCsvFileSelected(csvContent: String) {
        viewModelScope.launch {
            try {
                // Parse CSV with kotlin-csv
                val rows = parseCsv(csvContent)

                // Detect duplicates and existing students
                val previewData = rows.mapIndexed { index, row ->
                    val isDuplicate = rows.take(index).any {
                        it.name == row.name && it.registrationNumber == row.registrationNumber
                    }
                    val alreadyExists = _uiState.value.allStudents.any {
                        it.name.equals(row.name, ignoreCase = true) &&
                        it.registrationNumber.equals(row.registrationNumber, ignoreCase = true)
                    }

                    row.copy(
                        isDuplicate = isDuplicate,
                        alreadyExists = alreadyExists,
                        hasWarning = isDuplicate || row.name.startsWith("UNKNOWN_") || row.registrationNumber.startsWith("UNREG_")
                    )
                }

                _uiState.update {
                    it.copy(
                        csvPreviewData = previewData,
                        csvImportError = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        csvImportError = "Failed to parse CSV: ${e.message}"
                    )
                }
            }
        }
    }

    private fun parseCsv(content: String): List<CsvStudentRow> {
        // Implementation using kotlin-csv library
        // Add dependency in libs.versions.toml: kotlin-csv-jvm = "1.9.2"
        // Import: import com.github.doyaaaaaken.kotlincsv.dsl.csvReader

        val batchTimestamp = System.currentTimeMillis()
        val rows = mutableListOf<CsvStudentRow>()

        try {
            // Parse CSV with header detection
            val csvData = csvReader {
                charset = "UTF-8"
                quoteChar = '"'
                delimiter = ','
                escapeChar = '\\'
            }.readAllWithHeader(content.byteInputStream())

            csvData.forEachIndexed { index, row ->
                // Extract fields with case-insensitive header matching
                val name = row["Name"]?.trim() ?: row["name"]?.trim()
                val regNumber = row["Registration Number"]?.trim()
                    ?: row["RegistrationNumber"]?.trim()
                    ?: row["Reg Number"]?.trim()
                    ?: row["RegNumber"]?.trim()
                val rollNumber = row["Roll Number"]?.trim() ?: row["RollNumber"]?.trim()
                val email = row["Email"]?.trim() ?: row["email"]?.trim()
                val phone = row["Phone"]?.trim() ?: row["phone"]?.trim()

                // Generate placeholders for missing required fields
                val finalName = if (name.isNullOrBlank()) {
                    "UNKNOWN_${batchTimestamp}_${index + 1}"
                } else {
                    name
                }

                val finalRegNumber = if (regNumber.isNullOrBlank()) {
                    "UNREG_${batchTimestamp}_${index + 1}"
                } else {
                    regNumber
                }

                rows.add(
                    CsvStudentRow(
                        name = finalName,
                        registrationNumber = finalRegNumber,
                        rollNumber = rollNumber?.takeIf { it.isNotBlank() },
                        email = email?.takeIf { it.isNotBlank() },
                        phone = phone?.takeIf { it.isNotBlank() },
                        isDuplicate = false, // Will be set by caller
                        alreadyExists = false, // Will be set by caller
                        hasWarning = false // Will be set by caller
                    )
                )
            }
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to parse CSV: ${e.message}", e)
        }

        return rows
    }

    fun onConfirmCsvImport() {
        viewModelScope.launch {
            val rows = _uiState.value.csvPreviewData.filter { !it.isDuplicate }

            rows.forEach { row ->
                if (row.alreadyExists) {
                    // Link existing student
                    val existingStudent = _uiState.value.allStudents.find {
                        it.name.equals(row.name, ignoreCase = true) &&
                        it.registrationNumber.equals(row.registrationNumber, ignoreCase = true)
                    }
                    existingStudent?.let {
                        studentRepository.addStudentToClass(classId, it.studentId)
                    }
                } else {
                    // Create new student
                    val student = Student(
                        studentId = 0L,
                        name = row.name,
                        registrationNumber = row.registrationNumber,
                        rollNumber = row.rollNumber,
                        email = row.email,
                        phone = row.phone,
                        isActive = true,
                        createdAt = System.currentTimeMillis()
                    )
                    val result = studentRepository.createStudent(student)
                    if (result is RepositoryResult.Success) {
                        studentRepository.addStudentToClass(classId, result.data)
                    }
                }
            }

            _uiState.update {
                it.copy(
                    showCsvImportDialog = false,
                    csvPreviewData = emptyList()
                )
            }
        }
    }

    // Copy from class
    fun onCopyFromClass(sourceClassId: Long) {
        viewModelScope.launch {
            // Get students from source class
            val sourceStudents = studentRepository.getStudentsForClass(sourceClassId)

            // Add all to current class
            sourceStudents.forEach { student ->
                studentRepository.addStudentToClass(classId, student.studentId)
            }

            _uiState.update { it.copy(showCopyFromClassDialog = false) }
        }
    }
}

2.3. EditClassScreen (`ui/screen/manageclass/EditClassScreen.kt`)

@Composable
fun EditClassScreen(
    classId: Long,
    onNavigateBack: () -> Unit,
    viewModel: EditClassViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            AttenoteTopAppBar(
                title = "Edit Class",
                showBackButton = true,
                onBackClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Class Info (Read-only)
                item {
                    AttenoteSectionCard(title = "Class Information") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            uiState.classItem?.let { classItem ->
                                InfoRow("Class Name", classItem.className)
                                InfoRow("Subject", classItem.subject)
                                InfoRow("Institute", classItem.instituteName)
                                InfoRow("Session", classItem.session)
                                InfoRow("Department", classItem.department)
                                InfoRow("Semester", classItem.semester)
                                if (classItem.section.isNotEmpty()) {
                                    InfoRow("Section", classItem.section)
                                }
                            }
                        }
                    }
                }

                // Date Range (Editable)
                item {
                    AttenoteSectionCard(title = "Date Range") {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                onClick = { viewModel.onStartDatePickerRequested() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = "Start: ${uiState.startDate}")
                            }

                            OutlinedButton(
                                onClick = { viewModel.onEndDatePickerRequested() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = "End: ${uiState.endDate}")
                            }

                            if (uiState.dateRangeError != null) {
                                Text(
                                    text = uiState.dateRangeError!!,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            if (uiState.outOfRangeWarning != null) {
                                Text(
                                    text = uiState.outOfRangeWarning!!,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            AttenoteButton(
                                text = "Save Date Range",
                                onClick = { viewModel.onSaveDateRange() },
                                enabled = !uiState.isSaving,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Student Roster
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Student Roster (${uiState.students.size})",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        // Roster action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AttenoteTextButton(
                                text = "+ Manual",
                                onClick = { viewModel.onShowAddStudentDialog() },
                                modifier = Modifier.weight(1f)
                            )
                            AttenoteTextButton(
                                text = "CSV Import",
                                onClick = { viewModel.onShowCsvImportDialog() },
                                modifier = Modifier.weight(1f)
                            )
                            AttenoteTextButton(
                                text = "Copy Class",
                                onClick = { viewModel.onShowCopyFromClassDialog() },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        AttenoteSectionCard(title = null) {
                            if (uiState.students.isEmpty()) {
                                Text(
                                    text = "No students in this class",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(8.dp)
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    uiState.students.forEach { studentInClass ->
                                        StudentRosterCard(
                                            studentInClass = studentInClass,
                                            onActiveToggled = { isActive ->
                                                viewModel.onToggleStudentActiveInClass(
                                                    studentInClass.student.studentId,
                                                    isActive
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (uiState.showDatePicker) {
        AttenoteDatePickerDialog(
            onDateSelected = { viewModel.onDateSelected(it) },
            onDismiss = { viewModel.onDatePickerDismissed() }
        )
    }

    if (uiState.showAddStudentDialog) {
        AddStudentDialog(
            onDismiss = { viewModel.onDismissAddStudentDialog() },
            onConfirm = { name, reg, roll ->
                viewModel.onAddStudent(name, reg, roll)
            }
        )
    }

    if (uiState.showCsvImportDialog) {
        CsvImportDialog(
            previewData = uiState.csvPreviewData,
            error = uiState.csvImportError,
            onFileSelected = { content -> viewModel.onCsvFileSelected(content) },
            onConfirm = { viewModel.onConfirmCsvImport() },
            onDismiss = { viewModel.onDismissCsvImportDialog() }
        )
    }

    if (uiState.showCopyFromClassDialog) {
        CopyFromClassDialog(
            availableClasses = uiState.availableClasses,
            onClassSelected = { classId -> viewModel.onCopyFromClass(classId) },
            onDismiss = { viewModel.onDismissCopyFromClassDialog() }
        )
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun StudentRosterCard(
    studentInClass: StudentInClass,
    onActiveToggled: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = studentInClass.student.name,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "Reg: ${studentInClass.student.registrationNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                studentInClass.student.rollNumber?.let {
                    Text(
                        text = "Roll: $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Switch(
                checked = studentInClass.isActiveInClass,
                onCheckedChange = onActiveToggled
            )
        }
    }
}

## 3. Done Criteria

Build, install, and verify:
- `./gradlew :app:assembleDebug`
- `adb install -r app/build/outputs/apk/debug/app-debug.apk`
- `adb shell monkey -p com.uteacher.attendancetracker -c android.intent.category.LAUNCHER 1`

Manual testing checklist:

### ManageClassList Screen
- [ ] Screen loads without crash
- [ ] All classes displayed in list
- [ ] Classes sorted by creation date (newest first)
- [ ] Each class card shows: className, subject, semester, institute, session, section (if any), date range
- [ ] Open/close switch visible per class
- [ ] Toggle switch → state persists immediately
- [ ] Tap "Edit Class" button → navigates to EditClass screen with correct classId
- [ ] Empty state shown when no classes exist

### EditClass - Class Info Section
- [ ] Read-only class info displayed correctly
- [ ] All identity fields shown (className, subject, institute, session, department, semester, section)
- [ ] Fields are NOT editable (display-only)

### EditClass - Date Range Editing
- [ ] Start date button shows current start date
- [ ] End date button shows current end date
- [ ] Tap start date → date picker opens
- [ ] Select new start date → button updates
- [ ] Tap end date → date picker opens
- [ ] Select new end date → button updates
- [ ] Set start date > end date → tap "Save Date Range" → error: "Start date must be before or equal to end date"
- [ ] Set valid date range → tap "Save" → success, dates persist
- [ ] Navigate back → return to EditClass → dates updated

### EditClass - Date Range Warning (Out-of-Range Attendance)
- [ ] Create attendance for date 2026-03-15
- [ ] Edit class → change end date to 2026-03-10 (before attendance date)
- [ ] Tap "Save Date Range" → warning shown: "X attendance records fall outside new date range"
- [ ] Date range saved successfully (warning, not error)
- [ ] Navigate to take attendance for 2026-03-15 → attendance still exists (not deleted)
- [ ] Edit class → change end date back to 2026-06-30
- [ ] Attendance for 2026-03-15 visible/accessible again

### EditClass - Student Roster Display
- [ ] Student roster section shows count: "Student Roster (5)"
- [ ] Action buttons visible: "+ Manual", "CSV Import", "Copy Class"
- [ ] Students listed with name, registration number, roll number (if any)
- [ ] Active toggle switch per student
- [ ] Empty state when no students: "No students in this class"

### EditClass - Student Active Toggle
- [ ] Toggle student switch to OFF → student marked inactive in class
- [ ] Navigate to TakeAttendance → inactive student NOT shown in attendance list
- [ ] Return to EditClass → student still visible with switch OFF
- [ ] Toggle back to ON → student active again
- [ ] Navigate to TakeAttendance → student appears in list

### EditClass - Manual Add Student
- [ ] Tap "+ Manual" → dialog opens
- [ ] Dialog has fields: Name (required), Registration Number (required), Roll Number (optional)
- [ ] Leave name empty → tap "Add" → validation error
- [ ] Fill name only → tap "Add" → validation error (reg required)
- [ ] Fill name + reg → tap "Add" → student created and linked to class
- [ ] Student appears in roster list
- [ ] Try to add duplicate (same name + reg) → error: "Student already exists"
- [ ] Existing student linked to class (not duplicated)

### EditClass - CSV Import
- [ ] Tap "CSV Import" → dialog opens
- [ ] Dialog has file picker button
- [ ] Select CSV file with valid data → preview table shown
- [ ] Preview shows: Name, Reg Number, Roll, Email, Phone columns
- [ ] CSV with missing Name → placeholder "UNKNOWN_{timestamp}_{row}" shown with warning indicator
- [ ] CSV with missing Reg → placeholder "UNREG_{timestamp}_{row}" shown with warning indicator
- [ ] CSV with duplicate rows (same name+reg) → duplicates marked with warning
- [ ] Existing students in DB → marked as "Already exists" (will be linked, not created)
- [ ] Tap "Cancel" → dialog closes, no students added
- [ ] Tap "Import" → students created/linked, dialog closes
- [ ] Students appear in roster list

### EditClass - Copy From Class
- [ ] Tap "Copy Class" → dialog opens
- [ ] Dialog shows list of other classes (excludes current class)
- [ ] Select source class → tap "Copy" → all students from source copied to current class
- [ ] Students appear in roster list
- [ ] Active state preserved from source class
- [ ] Duplicate students (already in both classes) handled gracefully (no duplication)

### Integration Tests
- [ ] Create class → Edit class → Add students manually → Navigate to Dashboard → class shows correct student count
- [ ] Create class → Edit class → Import CSV → Take attendance → only active students shown
- [ ] Create class → Edit class → Copy from another class → Both classes share same students in roster

## 4. Output Format

1. **Changed Files:**
   - ui/screen/manageclass/ManageClassListUiState.kt
   - ui/screen/manageclass/ManageClassListViewModel.kt
   - ui/screen/manageclass/ManageClassListScreen.kt
   - ui/screen/manageclass/components/ClassCard.kt
   - ui/screen/manageclass/EditClassUiState.kt
   - ui/screen/manageclass/EditClassViewModel.kt
   - ui/screen/manageclass/EditClassScreen.kt
   - ui/screen/manageclass/components/StudentRosterCard.kt
   - ui/screen/manageclass/dialogs/AddStudentDialog.kt
   - ui/screen/manageclass/dialogs/CsvImportDialog.kt
   - ui/screen/manageclass/dialogs/CopyFromClassDialog.kt
   - data/repository/ClassRepository.kt (add updateClassDateRange method)
   - data/repository/StudentRepository.kt (add addStudentToClass, updateStudentActiveInClass methods)
   - ui/navigation/AppNavHost.kt (wire ManageClassList and EditClass routes)
   - di/ViewModelModule.kt (add ViewModels)

2. **EditClass Operation Matrix:**
   - Manual Add: Creates new student OR links existing student → adds to class roster
   - CSV Import: Parses CSV → creates/links students → deduplicates → adds to class roster
   - Copy From Class: Gets students from source → links to target class → preserves active state

3. **Device Gate Command Results:**
   ```
   ./gradlew :app:assembleDebug
   BUILD SUCCESSFUL in Xs

   adb install -r app/build/outputs/apk/debug/app-debug.apk
   Success

   adb shell monkey -p com.uteacher.attendancetracker -c android.intent.category.LAUNCHER 1
   Events injected: 1
   ```

4. **Manual Checklist with Pass/Fail:**
   (Complete the checklist above with actual test results)
```

### Git Commit Message (Step 09)
`feat(step-09): implement class management, edit class, and roster operations`

## Prompt 10 - Manage Students Module
```text
Implement global student management with search, CRUD operations, and active state toggle.

## 1. ManageStudentsUiState and ViewModel

1.1. ManageStudentsUiState (`ui/screen/managestudents/ManageStudentsUiState.kt`)

data class ManageStudentsUiState(
    val allStudents: List<Student> = emptyList(),
    val filteredStudents: List<Student> = emptyList(),
    val searchQuery: String = "",

    // Editor dialog state
    val showEditorDialog: Boolean = false,
    val editMode: StudentEditMode = StudentEditMode.ADD,
    val editingStudent: Student? = null,

    // Editor form fields
    val editorName: String = "",
    val editorRegistrationNumber: String = "",
    val editorRollNumber: String = "",
    val editorEmail: String = "",
    val editorPhone: String = "",
    val editorIsActive: Boolean = true,

    // Editor validation
    val editorNameError: String? = null,
    val editorRegError: String? = null,
    val editorError: String? = null,

    // Loading/error states
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null
)

enum class StudentEditMode {
    ADD,
    EDIT
}

1.2. ManageStudentsViewModel (`ui/screen/managestudents/ManageStudentsViewModel.kt`)

class ManageStudentsViewModel(
    private val studentRepository: StudentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManageStudentsUiState())
    val uiState: StateFlow<ManageStudentsUiState> = _uiState.asStateFlow()

    init {
        loadStudents()
    }

    private fun loadStudents() {
        viewModelScope.launch {
            try {
                studentRepository.observeAllStudents().collect { students ->
                    val sortedStudents = students.sortedBy { it.name.lowercase() }
                    _uiState.update {
                        it.copy(
                            allStudents = sortedStudents,
                            filteredStudents = filterStudents(sortedStudents, it.searchQuery),
                            isLoading = false,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load students: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Filter students by search query.
     * Searches across: name, registration number, roll number, email, phone.
     * Case-insensitive.
     */
    private fun filterStudents(students: List<Student>, query: String): List<Student> {
        if (query.isBlank()) return students

        val lowerQuery = query.lowercase().trim()
        return students.filter { student ->
            student.name.lowercase().contains(lowerQuery) ||
            student.registrationNumber.lowercase().contains(lowerQuery) ||
            student.rollNumber?.lowercase()?.contains(lowerQuery) == true ||
            student.email?.lowercase()?.contains(lowerQuery) == true ||
            student.phone?.contains(lowerQuery) == true
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update {
            it.copy(
                searchQuery = query,
                filteredStudents = filterStudents(it.allStudents, query)
            )
        }
    }

    fun onToggleStudentActive(studentId: Long, isActive: Boolean) {
        viewModelScope.launch {
            studentRepository.updateStudentActiveState(studentId, isActive)
        }
    }

    // Editor dialog - Add mode
    fun onShowAddDialog() {
        _uiState.update {
            it.copy(
                showEditorDialog = true,
                editMode = StudentEditMode.ADD,
                editingStudent = null,
                editorName = "",
                editorRegistrationNumber = "",
                editorRollNumber = "",
                editorEmail = "",
                editorPhone = "",
                editorIsActive = true,
                editorNameError = null,
                editorRegError = null,
                editorError = null
            )
        }
    }

    // Editor dialog - Edit mode
    fun onShowEditDialog(student: Student) {
        _uiState.update {
            it.copy(
                showEditorDialog = true,
                editMode = StudentEditMode.EDIT,
                editingStudent = student,
                editorName = student.name,
                editorRegistrationNumber = student.registrationNumber,
                editorRollNumber = student.rollNumber ?: "",
                editorEmail = student.email ?: "",
                editorPhone = student.phone ?: "",
                editorIsActive = student.isActive,
                editorNameError = null,
                editorRegError = null,
                editorError = null
            )
        }
    }

    fun onDismissEditorDialog() {
        _uiState.update {
            it.copy(
                showEditorDialog = false,
                editorNameError = null,
                editorRegError = null,
                editorError = null
            )
        }
    }

    // Editor form field handlers
    fun onEditorNameChanged(value: String) {
        _uiState.update {
            it.copy(editorName = value, editorNameError = null)
        }
    }

    fun onEditorRegChanged(value: String) {
        _uiState.update {
            it.copy(editorRegistrationNumber = value, editorRegError = null)
        }
    }

    fun onEditorRollChanged(value: String) {
        _uiState.update { it.copy(editorRollNumber = value) }
    }

    fun onEditorEmailChanged(value: String) {
        _uiState.update { it.copy(editorEmail = value) }
    }

    fun onEditorPhoneChanged(value: String) {
        _uiState.update { it.copy(editorPhone = value) }
    }

    fun onEditorActiveToggled(isActive: Boolean) {
        _uiState.update { it.copy(editorIsActive = isActive) }
    }

    // Save student (add or edit)
    fun onSaveStudent() {
        val state = _uiState.value

        // Validate required fields
        if (state.editorName.trim().isEmpty()) {
            _uiState.update { it.copy(editorNameError = "Name is required") }
            return
        }

        if (state.editorRegistrationNumber.trim().isEmpty()) {
            _uiState.update { it.copy(editorRegError = "Registration number is required") }
            return
        }

        _uiState.update { it.copy(isSaving = true, editorError = null) }

        viewModelScope.launch {
            try {
                val student = Student(
                    studentId = state.editingStudent?.studentId ?: 0L,
                    name = state.editorName.trim(),
                    registrationNumber = state.editorRegistrationNumber.trim(),
                    rollNumber = state.editorRollNumber.trim().ifBlank { null },
                    email = state.editorEmail.trim().ifBlank { null },
                    phone = state.editorPhone.trim().ifBlank { null },
                    isActive = state.editorIsActive,
                    createdAt = state.editingStudent?.createdAt ?: System.currentTimeMillis()
                )

                val result = if (state.editMode == StudentEditMode.ADD) {
                    studentRepository.createStudent(student)
                } else {
                    studentRepository.updateStudent(student)
                }

                when (result) {
                    is RepositoryResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                showEditorDialog = false
                            )
                        }
                    }
                    is RepositoryResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                editorError = result.message
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        editorError = "Failed to save student: ${e.message}"
                    )
                }
            }
        }
    }
}

## 2. ManageStudentsScreen Implementation

2.1. ManageStudentsScreen (`ui/screen/managestudents/ManageStudentsScreen.kt`)

@Composable
fun ManageStudentsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ManageStudentsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            AttenoteTopAppBar(
                title = "Manage Students",
                showBackButton = true,
                onBackClick = onNavigateBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onShowAddDialog() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add student")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            AttenoteTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                label = "Search students...",
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            // Loading state
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            // Empty state
            else if (uiState.filteredStudents.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (uiState.searchQuery.isNotEmpty()) {
                            "No students found for \"${uiState.searchQuery}\""
                        } else {
                            "No students.\nTap + to add a student."
                        },
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            // Student list
            else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Result count
                    item {
                        Text(
                            text = "${uiState.filteredStudents.size} student${if (uiState.filteredStudents.size != 1) "s" else ""}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Student cards
                    items(uiState.filteredStudents) { student ->
                        StudentCard(
                            student = student,
                            onActiveToggled = { isActive ->
                                viewModel.onToggleStudentActive(student.studentId, isActive)
                            },
                            onEditClicked = {
                                viewModel.onShowEditDialog(student)
                            }
                        )
                    }
                }
            }

            // Error state
            if (uiState.error != null) {
                Snackbar(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(text = uiState.error!!)
                }
            }
        }
    }

    // Editor dialog
    if (uiState.showEditorDialog) {
        StudentEditorDialog(
            mode = uiState.editMode,
            name = uiState.editorName,
            registrationNumber = uiState.editorRegistrationNumber,
            rollNumber = uiState.editorRollNumber,
            email = uiState.editorEmail,
            phone = uiState.editorPhone,
            isActive = uiState.editorIsActive,
            nameError = uiState.editorNameError,
            regError = uiState.editorRegError,
            error = uiState.editorError,
            isSaving = uiState.isSaving,
            onNameChanged = { viewModel.onEditorNameChanged(it) },
            onRegChanged = { viewModel.onEditorRegChanged(it) },
            onRollChanged = { viewModel.onEditorRollChanged(it) },
            onEmailChanged = { viewModel.onEditorEmailChanged(it) },
            onPhoneChanged = { viewModel.onEditorPhoneChanged(it) },
            onActiveToggled = { viewModel.onEditorActiveToggled(it) },
            onSave = { viewModel.onSaveStudent() },
            onDismiss = { viewModel.onDismissEditorDialog() }
        )
    }
}

2.2. StudentCard Component (`ui/screen/managestudents/components/StudentCard.kt`)

@Composable
fun StudentCard(
    student: Student,
    onActiveToggled: (Boolean) -> Unit,
    onEditClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Name
                Text(
                    text = student.name,
                    style = MaterialTheme.typography.titleMedium
                )

                // Registration number
                Text(
                    text = "Reg: ${student.registrationNumber}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Roll number (if present)
                student.rollNumber?.let {
                    Text(
                        text = "Roll: $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Email (if present)
                student.email?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Phone (if present)
                student.phone?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Active toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (student.isActive) "Active" else "Inactive",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Switch(
                        checked = student.isActive,
                        onCheckedChange = onActiveToggled
                    )
                }

                // Edit button
                AttenoteTextButton(
                    text = "Edit",
                    onClick = onEditClicked
                )
            }
        }
    }
}

2.3. StudentEditorDialog (`ui/screen/managestudents/dialogs/StudentEditorDialog.kt`)

@Composable
fun StudentEditorDialog(
    mode: StudentEditMode,
    name: String,
    registrationNumber: String,
    rollNumber: String,
    email: String,
    phone: String,
    isActive: Boolean,
    nameError: String?,
    regError: String?,
    error: String?,
    isSaving: Boolean,
    onNameChanged: (String) -> Unit,
    onRegChanged: (String) -> Unit,
    onRollChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPhoneChanged: (String) -> Unit,
    onActiveToggled: (Boolean) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Dialog title
                Text(
                    text = if (mode == StudentEditMode.ADD) "Add Student" else "Edit Student",
                    style = MaterialTheme.typography.titleLarge
                )

                // Name field
                AttenoteTextField(
                    value = name,
                    onValueChange = onNameChanged,
                    label = "Name *",
                    enabled = !isSaving,
                    isError = nameError != null,
                    supportingText = nameError
                )

                // Registration number field
                AttenoteTextField(
                    value = registrationNumber,
                    onValueChange = onRegChanged,
                    label = "Registration Number *",
                    enabled = !isSaving && mode == StudentEditMode.ADD, // Read-only in edit mode
                    isError = regError != null,
                    supportingText = regError ?: if (mode == StudentEditMode.EDIT) "Cannot be changed" else null
                )

                // Roll number field
                AttenoteTextField(
                    value = rollNumber,
                    onValueChange = onRollChanged,
                    label = "Roll Number (optional)",
                    enabled = !isSaving
                )

                // Email field
                AttenoteTextField(
                    value = email,
                    onValueChange = onEmailChanged,
                    label = "Email (optional)",
                    enabled = !isSaving,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                // Phone field
                AttenoteTextField(
                    value = phone,
                    onValueChange = onPhoneChanged,
                    label = "Phone (optional)",
                    enabled = !isSaving,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                // Active toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Active",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Switch(
                        checked = isActive,
                        onCheckedChange = onActiveToggled,
                        enabled = !isSaving
                    )
                }

                // Error message
                if (error != null) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isSaving
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onSave,
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(if (mode == StudentEditMode.ADD) "Add" else "Save")
                        }
                    }
                }
            }
        }
    }
}

## 3. Done Criteria

Build, install, and verify:
- `./gradlew :app:assembleDebug`
- `adb install -r app/build/outputs/apk/debug/app-debug.apk`
- `adb shell monkey -p com.uteacher.attendancetracker -c android.intent.category.LAUNCHER 1`

Manual testing checklist:

### Screen Loading
- [ ] Screen loads without crash
- [ ] All students displayed in list
- [ ] Students sorted alphabetically by name
- [ ] Loading indicator shown while loading
- [ ] FAB (+ button) visible in bottom-right

### Student List Display
- [ ] Each student card shows: name, reg number, roll (if any), email (if any), phone (if any)
- [ ] Active/inactive label shown per student
- [ ] Active switch visible per student
- [ ] "Edit" button visible per student
- [ ] Result count shown: "12 students"

### Search Functionality
- [ ] Search bar visible at top
- [ ] Type "john" → filters students with "john" in name
- [ ] Type "2026" → filters students with "2026" in reg number, roll, email, or phone
- [ ] Search is case-insensitive
- [ ] Clear icon (X) appears when search has text
- [ ] Tap clear icon → search cleared, all students shown
- [ ] Search with no matches → "No students found for \"xyz\""
- [ ] Result count updates: "3 students" when filtered

### Active Toggle
- [ ] Toggle student switch OFF → student marked inactive
- [ ] Switch state persists immediately
- [ ] Navigate away and back → switch state persists
- [ ] Inactive students shown with "Inactive" label
- [ ] Inactive students still visible in list (not hidden)

### Add Student Flow
- [ ] Tap FAB (+) → editor dialog opens
- [ ] Dialog title: "Add Student"
- [ ] All fields empty except "Active" toggle (ON by default)
- [ ] Leave name empty → tap "Add" → validation error: "Name is required"
- [ ] Fill name only → tap "Add" → validation error: "Registration number is required"
- [ ] Fill name + reg → tap "Add" → student created, dialog closes
- [ ] New student appears in list
- [ ] Tap "Cancel" → dialog closes without saving

### Add Student - Duplicate Detection
- [ ] Add student: Name="John Doe", Reg="20260101"
- [ ] Save → success
- [ ] Try to add again: Name="John Doe", Reg="20260101"
- [ ] Tap "Add" → error: "Student with this name and registration number already exists"
- [ ] Dialog remains open, fields editable
- [ ] Change name to "Jane Doe" → tap "Add" → error: "Registration number already exists"
- [ ] Change reg to "20260102" → tap "Add" → success (different identity)

### Edit Student Flow
- [ ] Tap "Edit" on student card → dialog opens
- [ ] Dialog title: "Edit Student"
- [ ] All fields pre-filled with current values
- [ ] Registration number field disabled (gray), helper text: "Cannot be changed"
- [ ] Change name → tap "Save" → updates saved, dialog closes
- [ ] Student card shows updated name
- [ ] Change roll number → tap "Save" → updates saved
- [ ] Add email → tap "Save" → email appears on card
- [ ] Remove phone (clear field) → tap "Save" → phone removed from card

### Edit Student - Duplicate Detection
- [ ] Student A: Name="John Doe", Reg="20260101"
- [ ] Student B: Name="Jane Smith", Reg="20260102"
- [ ] Edit Student B → change name to "John Doe"
- [ ] Tap "Save" → error: "Student with this name and registration number already exists"
- [ ] Change name back to "Jane Smith" → tap "Save" → success

### Optional Fields
- [ ] Add student with only name + reg (no roll, email, phone) → success
- [ ] Card shows only name and reg (no extra lines)
- [ ] Add student with all fields → success
- [ ] Card shows all 5 lines: name, reg, roll, email, phone

### Empty State
- [ ] Delete all students (if possible) OR fresh install with no students
- [ ] Screen shows: "No students. Tap + to add a student."
- [ ] FAB still visible

### Search Empty State
- [ ] Type search query that matches nothing: "zzzzz"
- [ ] Screen shows: "No students found for \"zzzzz\""
- [ ] Clear search → all students visible again

### Edge Cases
- [ ] Add student with very long name (50+ chars) → card displays correctly (wraps or truncates)
- [ ] Add student with special characters in name: "O'Brien" → saves correctly
- [ ] Search with special characters → works correctly
- [ ] Toggle active while search active → toggle works, filtered list updates
- [ ] Edit student while search active → saves correctly, card updates in filtered view

## 4. Output Format

1. **Changed Files:**
   - domain/model/Student.kt (already exists)
   - ui/screen/managestudents/ManageStudentsUiState.kt
   - ui/screen/managestudents/ManageStudentsViewModel.kt
   - ui/screen/managestudents/ManageStudentsScreen.kt
   - ui/screen/managestudents/components/StudentCard.kt
   - ui/screen/managestudents/dialogs/StudentEditorDialog.kt
   - data/repository/StudentRepository.kt (add updateStudentActiveState if not exists)
   - ui/navigation/AppNavHost.kt (wire ManageStudents route)
   - di/ViewModelModule.kt (add ManageStudentsViewModel)

2. **Student CRUD Behavior Summary:**
   - **Create (Add)**: Validates name + reg required → checks duplicate (name, reg) → creates via repository → appears in list
   - **Read (List)**: Observes all students → sorts by name → displays with search filter
   - **Update (Edit)**: Pre-fills form → disables reg field → validates → checks duplicate (name only, since reg unchanged) → updates via repository
   - **Delete**: Not implemented in this phase (deferred)
   - **Search**: Filters by name, reg, roll, email, phone (case-insensitive, partial match)
   - **Active Toggle**: Updates isActive field → persists immediately → affects attendance visibility

3. **Device Gate Command Results:**
   ```
   ./gradlew :app:assembleDebug
   BUILD SUCCESSFUL in Xs

   adb install -r app/build/outputs/apk/debug/app-debug.apk
   Success

   adb shell monkey -p com.uteacher.attendancetracker -c android.intent.category.LAUNCHER 1
   Events injected: 1
   ```

4. **Manual Checklist with Pass/Fail:**
   (Complete the checklist above with actual test results)
```

### Git Commit Message (Step 10)
`feat(step-10): implement student directory search, editor, and active state updates`

## Prompt 11 - Attendance Module
```text
Implement the complete take-attendance flow with student list, present/absent toggles, lesson notes, and save persistence.

## 1. TakeAttendanceUiState and ViewModel

1.1. AttendanceRecordItem (`ui/screen/attendance/AttendanceRecordItem.kt`)

data class AttendanceRecordItem(
    val student: Student,
    val isPresent: Boolean = true // Default: present
)

1.2. TakeAttendanceUiState (`ui/screen/attendance/TakeAttendanceUiState.kt`)

data class TakeAttendanceUiState(
    // Route parameters
    val classId: Long = 0L,
    val scheduleId: Long = 0L,
    val date: LocalDate? = null,

    // Loaded context
    val classItem: Class? = null,
    val schedule: Schedule? = null,
    val attendanceRecords: List<AttendanceRecordItem> = emptyList(),

    // Editable content
    val lessonNotes: String = "", // HTML from RichTextEditor

    // Loading/error/success states
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null
)

1.3. TakeAttendanceViewModel (`ui/screen/attendance/TakeAttendanceViewModel.kt`)

class TakeAttendanceViewModel(
    private val classId: Long,
    private val scheduleId: Long,
    private val dateString: String, // yyyy-MM-dd
    private val classRepository: ClassRepository,
    private val studentRepository: StudentRepository,
    private val attendanceRepository: AttendanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TakeAttendanceUiState(
        classId = classId,
        scheduleId = scheduleId
    ))
    val uiState: StateFlow<TakeAttendanceUiState> = _uiState.asStateFlow()

    init {
        loadAttendanceContext()
    }

    private fun loadAttendanceContext() {
        viewModelScope.launch {
            try {
                // Parse date
                val date = LocalDate.parse(dateString) // yyyy-MM-dd format
                _uiState.update { it.copy(date = date) }

                // Load class
                val classItem = classRepository.getClassById(classId)
                if (classItem == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Class not found"
                        )
                    }
                    return@launch
                }

                // Load schedule
                val schedule = classItem.schedules.find { it.scheduleId == scheduleId }
                if (schedule == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Schedule not found"
                        )
                    }
                    return@launch
                }

                // Load active students for this class (both class-active and globally-active)
                val students = studentRepository.getActiveStudentsForClass(classId)
                    .filter { it.isActive } // Globally active

                if (students.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "No active students in this class"
                        )
                    }
                    return@launch
                }

                // Check if attendance session already exists
                val existingSession = attendanceRepository.findSession(classId, scheduleId, date)

                val attendanceRecords = if (existingSession != null) {
                    // Load existing attendance records
                    students.map { student ->
                        val existingRecord = existingSession.records.find { it.studentId == student.studentId }
                        AttendanceRecordItem(
                            student = student,
                            isPresent = existingRecord?.isPresent ?: true
                        )
                    }
                } else {
                    // New session - default all present
                    students.map { student ->
                        AttendanceRecordItem(student = student, isPresent = true)
                    }
                }

                _uiState.update {
                    it.copy(
                        classItem = classItem,
                        schedule = schedule,
                        attendanceRecords = attendanceRecords.sortedBy { record -> record.student.name },
                        lessonNotes = existingSession?.lessonNotes ?: "",
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load attendance: ${e.message}"
                    )
                }
            }
        }
    }

    fun onToggleStudentPresent(studentId: Long, isPresent: Boolean) {
        _uiState.update { state ->
            state.copy(
                attendanceRecords = state.attendanceRecords.map { record ->
                    if (record.student.studentId == studentId) {
                        record.copy(isPresent = isPresent)
                    } else {
                        record
                    }
                }
            )
        }
    }

    fun onLessonNotesChanged(html: String) {
        _uiState.update { it.copy(lessonNotes = html) }
    }

    fun onSaveClicked() {
        val state = _uiState.value

        if (state.date == null) {
            _uiState.update { it.copy(error = "Invalid date") }
            return
        }

        if (state.attendanceRecords.isEmpty()) {
            _uiState.update { it.copy(error = "No students to save attendance for") }
            return
        }

        _uiState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            try {
                // Convert to domain AttendanceRecord list
                val records = state.attendanceRecords.map { item ->
                    AttendanceRecord(
                        recordId = 0L,
                        sessionId = 0L, // Will be set by repository
                        studentId = item.student.studentId,
                        isPresent = item.isPresent
                    )
                }

                // Save via repository (idempotent - find or create session)
                val result = attendanceRepository.saveAttendance(
                    classId = state.classId,
                    scheduleId = state.scheduleId,
                    date = state.date,
                    lessonNotes = state.lessonNotes.ifBlank { null },
                    records = records
                )

                when (result) {
                    is RepositoryResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                saveSuccess = true
                            )
                        }
                    }
                    is RepositoryResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                error = result.message
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = "Failed to save attendance: ${e.message}"
                    )
                }
            }
        }
    }
}

## 2. TakeAttendanceScreen Implementation

2.1. TakeAttendanceScreen (`ui/screen/attendance/TakeAttendanceScreen.kt`)

@Composable
fun TakeAttendanceScreen(
    classId: Long,
    scheduleId: Long,
    date: String,
    onNavigateBack: () -> Unit,
    viewModel: TakeAttendanceViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Navigate back on save success
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            AttenoteTopAppBar(
                title = "Take Attendance",
                showBackButton = true,
                onBackClick = onNavigateBack,
                actions = {
                    TextButton(
                        onClick = { viewModel.onSaveClicked() },
                        enabled = !uiState.isSaving && !uiState.isLoading
                    ) {
                        Text("Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                    AttenoteButton(
                        text = "Go Back",
                        onClick = onNavigateBack
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Class/Schedule Info Header
                item {
                    AttenoteSectionCard(title = "Class Information") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            uiState.classItem?.let { classItem ->
                                Text(
                                    text = classItem.className,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "${classItem.subject} • ${classItem.semester}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            uiState.schedule?.let { schedule ->
                                Text(
                                    text = "${schedule.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())} • ${schedule.startTime} - ${schedule.endTime}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            uiState.date?.let { date ->
                                Text(
                                    text = "Date: $date",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Attendance List
                item {
                    Text(
                        text = "Students (${uiState.attendanceRecords.size})",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                items(uiState.attendanceRecords) { record ->
                    AttendanceRecordCard(
                        record = record,
                        onTogglePresent = { isPresent ->
                            viewModel.onToggleStudentPresent(record.student.studentId, isPresent)
                        }
                    )
                }

                // Lesson Notes Section
                item {
                    AttenoteSectionCard(title = "Lesson Notes (optional)") {
                        // Note: Using plain text field for lesson notes (simple inline note-taking)
                        // Full RichTextEditor is reserved for dedicated Notes module (Prompt 12)
                        // This keeps attendance workflow fast and focused
                        AttenoteTextField(
                            value = uiState.lessonNotes,
                            onValueChange = { viewModel.onLessonNotesChanged(it) },
                            label = "Lesson notes",
                            minLines = 4,
                            maxLines = 8,
                            enabled = !uiState.isSaving,
                            supportingText = "Quick notes about today's lesson (plain text)"
                        )
                    }
                }

                // Save button (redundant with top bar, but helpful)
                item {
                    AttenoteButton(
                        text = "Save Attendance",
                        onClick = { viewModel.onSaveClicked() },
                        enabled = !uiState.isSaving,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

2.2. AttendanceRecordCard Component (`ui/screen/attendance/components/AttendanceRecordCard.kt`)

@Composable
fun AttendanceRecordCard(
    record: AttendanceRecordItem,
    onTogglePresent: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (record.isPresent) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = record.student.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Reg: ${record.student.registrationNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                record.student.rollNumber?.let {
                    Text(
                        text = "Roll: $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (record.isPresent) "Present" else "Absent",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (record.isPresent) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
                Switch(
                    checked = record.isPresent,
                    onCheckedChange = onTogglePresent,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.error
                    )
                )
            }
        }
    }
}

## 3. Done Criteria

Build, install, and verify:
- `./gradlew :app:assembleDebug`
- `adb install -r app/build/outputs/apk/debug/app-debug.apk`
- `adb shell monkey -p com.uteacher.attendancetracker -c android.intent.category.LAUNCHER 1`

Manual testing checklist:

### Navigation to Take Attendance
- [ ] Dashboard shows scheduled class for today
- [ ] Tap "Take Attendance" button on class card
- [ ] Navigate to TakeAttendance screen with correct classId, scheduleId, date

### Screen Loading
- [ ] Screen loads without crash
- [ ] Loading indicator shown while loading
- [ ] Class information section displays:
  - Class name: "Data Structures - XYZ University"
  - Subject and semester: "Data Structures • 1st Semester"
  - Schedule: "Monday • 09:00 - 10:30"
  - Date: "2026-01-15"

### Student List Display
- [ ] Students section shows count: "Students (12)"
- [ ] All active students displayed (both class-active AND globally-active)
- [ ] Students sorted alphabetically by name
- [ ] Each student card shows: name, reg number, roll (if any)
- [ ] Default state: All students marked "Present" with green background
- [ ] Present/Absent label visible
- [ ] Toggle switch visible per student

### Present/Absent Toggle
- [ ] Initially all students: Present (green background, switch ON)
- [ ] Toggle student switch OFF → changes to "Absent" (red background, switch OFF)
- [ ] Toggle back ON → changes to "Present" (green background, switch ON)
- [ ] Toggle multiple students → all states independent
- [ ] Scroll list → toggle states persist

### Lesson Notes Field
- [ ] "Lesson Notes (optional)" section visible
- [ ] Text field for notes (plain text for attendance module in v1)
- [ ] Can type notes → text persists while on screen
- [ ] Optional (can leave empty)

### Save Functionality - First Time (Create Session)
- [ ] Mark some students absent
- [ ] Type lesson notes: "Covered Chapter 5"
- [ ] Tap "Save" button (top bar or bottom)
- [ ] Loading indicator appears on button
- [ ] Save completes → navigate back to Dashboard
- [ ] Return to same class/date → attendance screen
- [ ] Previously saved attendance loaded:
  - Students marked absent still show absent
  - Lesson notes field shows "Covered Chapter 5"

### Save Functionality - Update Existing Session
- [ ] Take attendance for a class on date X → save
- [ ] Navigate back
- [ ] Open same class/date again
- [ ] Change some present→absent, absent→present
- [ ] Update lesson notes
- [ ] Tap "Save"
- [ ] Save completes → navigate back
- [ ] Open again → updated values loaded

### Validation - No Active Students
- [ ] Create class with no students OR all students inactive
- [ ] Try to open attendance
- [ ] Error shown: "No active students in this class"
- [ ] "Go Back" button visible
- [ ] Tap → navigate back

### Validation - Invalid Class/Schedule
- [ ] Navigate with invalid classId (if possible via URL manipulation)
- [ ] Error shown: "Class not found" or "Schedule not found"
- [ ] "Go Back" button visible

### Idempotent Save (Duplicate Prevention)
- [ ] Take attendance → save
- [ ] Rapidly tap "Save" button multiple times
- [ ] Only ONE attendance session created in database
- [ ] Unique constraint: (classId, scheduleId, date) prevents duplicates

### Empty Lesson Notes
- [ ] Take attendance without typing notes
- [ ] Tap "Save"
- [ ] Saves successfully (notes optional)
- [ ] Reopen → lesson notes field empty

### Edge Cases
- [ ] Take attendance with 50+ students → all display correctly, scrollable
- [ ] Toggle all students to absent → save → all marked absent
- [ ] Toggle all students to present → save → all marked present
- [ ] Save with very long lesson notes (1000+ chars) → saves correctly
- [ ] Navigate away (back button) without saving → changes discarded

### Date Validation (Scheduled-Only v1)
- [ ] Can only access attendance for dates matching class schedule
- [ ] Class has Monday schedule, try to take attendance on Tuesday → should be prevented by navigation (Dashboard only shows scheduled classes)

## 4. Output Format

1. **Changed Files:**
   - domain/model/AttendanceSession.kt (already exists)
   - domain/model/AttendanceRecord.kt (already exists)
   - ui/screen/attendance/AttendanceRecordItem.kt
   - ui/screen/attendance/TakeAttendanceUiState.kt
   - ui/screen/attendance/TakeAttendanceViewModel.kt
   - ui/screen/attendance/TakeAttendanceScreen.kt
   - ui/screen/attendance/components/AttendanceRecordCard.kt
   - data/repository/AttendanceRepository.kt (ensure findSession, saveAttendance exist)
   - ui/navigation/AppNavHost.kt (wire TakeAttendance route)
   - di/ViewModelModule.kt (add TakeAttendanceViewModel)

2. **Attendance Save Flow Summary:**
   1. User navigates with classId, scheduleId, date (yyyy-MM-dd)
   2. ViewModel loads: class, schedule, active students
   3. ViewModel checks for existing session via `findSession(classId, scheduleId, date)`
   4. If exists: load existing records → populate toggles
   5. If new: default all students present
   6. User toggles present/absent per student
   7. User types lesson notes (optional plain text)
   8. User taps "Save"
   9. ViewModel calls `AttendanceRepository.saveAttendance(classId, scheduleId, date, lessonNotes, records)`
   10. Repository behavior (idempotent):
       - Find existing session OR create new session (unique: classId+scheduleId+date)
       - Delete all old records for this session
       - Insert new records (one per student)
       - All in ONE transaction
   11. On success: navigate back
   12. On error: show error message, keep form editable

3. **Device Gate Command Results:**
   ```
   ./gradlew :app:assembleDebug
   BUILD SUCCESSFUL in Xs

   adb install -r app/build/outputs/apk/debug/app-debug.apk
   Success

   adb shell monkey -p com.uteacher.attendancetracker -c android.intent.category.LAUNCHER 1
   Events injected: 1
   ```

4. **Manual Checklist with Pass/Fail:**
   (Complete the checklist above with actual test results)
```

### Git Commit Message (Step 11)
`feat(step-11): implement attendance capture flow with save and validation`

## Prompt 12 - Notes Module (Rich Text, Date, Media, Autosave)
```text
Implement the complete add/edit note feature with rich text editor (richeditor-compose), media attachments, date selection, and autosave-on-exit.

## 1. Route Contract and Modes

Route: `AddNote(date: String, noteId: Long)`
- date: yyyy-MM-dd format
- noteId: -1 for create mode, >0 for edit mode

**Create Mode** (noteId <= 0):
- Start with empty title and content
- Date pre-filled from route parameter
- No existing media

**Edit Mode** (noteId > 0):
- Load existing note by noteId
- Pre-fill title, content (HTML), date
- Load existing media

## 2. AddNoteUiState and ViewModel

2.1. AddNoteUiState (`ui/screen/notes/AddNoteUiState.kt`)

data class AddNoteUiState(
    // Route parameters
    val noteId: Long = -1L,
    val initialDate: LocalDate? = null,

    // Note fields
    val title: String = "",
    val date: LocalDate = LocalDate.now(),
    val richTextState: RichTextState = RichTextState(), // From richeditor-compose

    // Media
    val pendingMedia: List<PendingMedia> = emptyList(), // Selected but not yet saved
    val savedMedia: List<NoteMedia> = emptyList(), // Already persisted in DB

    // UI state
    val showDatePicker: Boolean = false,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null,

    // Unsaved changes detection
    val hasUnsavedChanges: Boolean = false
)

data class PendingMedia(
    val uri: Uri,
    val localPath: String? = null // Path after copying to internal storage
)

2.2. AddNoteViewModel (`ui/screen/notes/AddNoteViewModel.kt`)

class AddNoteViewModel(
    private val noteId: Long,
    private val dateString: String,
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddNoteUiState(noteId = noteId))
    val uiState: StateFlow<AddNoteUiState> = _uiState.asStateFlow()

    // Baseline for change detection
    private var baselineTitle: String = ""
    private var baselineHtml: String = ""
    private var baselineDate: LocalDate? = null

    init {
        loadNote()
    }

    private fun loadNote() {
        viewModelScope.launch {
            try {
                val initialDate = LocalDate.parse(dateString)

                if (noteId > 0) {
                    // Edit mode - load existing note
                    val noteWithMedia = noteRepository.getNoteWithMedia(noteId)

                    if (noteWithMedia == null) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Note not found"
                            )
                        }
                        return@launch
                    }

                    val (note, media) = noteWithMedia
                    val richTextState = RichTextState()
                    richTextState.setHtml(note.content) // Load HTML into editor

                    // Set baseline for change detection
                    baselineTitle = note.title
                    baselineHtml = note.content
                    baselineDate = note.date

                    _uiState.update {
                        it.copy(
                            title = note.title,
                            date = note.date,
                            richTextState = richTextState,
                            savedMedia = media,
                            initialDate = initialDate,
                            isLoading = false,
                            hasUnsavedChanges = false
                        )
                    }
                } else {
                    // Create mode - start empty
                    baselineTitle = ""
                    baselineHtml = ""
                    baselineDate = initialDate

                    _uiState.update {
                        it.copy(
                            date = initialDate,
                            initialDate = initialDate,
                            isLoading = false,
                            hasUnsavedChanges = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load note: ${e.message}"
                    )
                }
            }
        }
    }

    fun onTitleChanged(title: String) {
        _uiState.update {
            it.copy(
                title = title,
                hasUnsavedChanges = detectChanges(title, it.richTextState.toHtml(), it.date)
            )
        }
    }

    fun onRichTextChanged() {
        val currentHtml = _uiState.value.richTextState.toHtml()
        _uiState.update {
            it.copy(
                hasUnsavedChanges = detectChanges(it.title, currentHtml, it.date)
            )
        }
    }

    fun onDatePickerRequested() {
        _uiState.update { it.copy(showDatePicker = true) }
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update {
            it.copy(
                date = date,
                showDatePicker = false,
                hasUnsavedChanges = detectChanges(it.title, it.richTextState.toHtml(), date)
            )
        }
    }

    fun onDatePickerDismissed() {
        _uiState.update { it.copy(showDatePicker = false) }
    }

    fun onAddMedia(uri: Uri, context: Context) {
        viewModelScope.launch {
            try {
                // Copy image to internal storage
                val mediaDir = File(context.filesDir, "note_media")
                if (!mediaDir.exists()) {
                    mediaDir.mkdirs()
                }

                val timestamp = System.currentTimeMillis()
                val filename = "note_media_$timestamp.jpg"
                val destFile = File(mediaDir, filename)

                // Copy and re-encode to JPEG
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    FileOutputStream(destFile).use { outputStream ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                    }
                }

                val pendingMedia = PendingMedia(
                    uri = uri,
                    localPath = destFile.absolutePath
                )

                _uiState.update {
                    it.copy(
                        pendingMedia = it.pendingMedia + pendingMedia,
                        hasUnsavedChanges = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to add media: ${e.message}")
                }
            }
        }
    }

    fun onRemovePendingMedia(index: Int) {
        val state = _uiState.value
        val mediaToRemove = state.pendingMedia.getOrNull(index)

        // Delete file
        mediaToRemove?.localPath?.let { path ->
            File(path).delete()
        }

        _uiState.update {
            it.copy(
                pendingMedia = it.pendingMedia.filterIndexed { i, _ -> i != index },
                hasUnsavedChanges = true
            )
        }
    }

    private fun detectChanges(title: String, html: String, date: LocalDate): Boolean {
        return title != baselineTitle ||
               html != baselineHtml ||
               date != baselineDate ||
               _uiState.value.pendingMedia.isNotEmpty()
    }

    fun onSaveClicked() {
        val state = _uiState.value
        val html = state.richTextState.toHtml()

        // Title is optional, can be empty
        // Content can be empty (blank note)

        _uiState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            try {
                val note = Note(
                    noteId = if (noteId > 0) noteId else 0L,
                    title = state.title.trim(),
                    content = html,
                    date = state.date,
                    createdAt = System.currentTimeMillis(), // Will be preserved in edit mode
                    updatedAt = System.currentTimeMillis()
                )

                // Collect pending media paths
                val mediaPaths = state.pendingMedia.mapNotNull { it.localPath }

                val result = if (noteId > 0) {
                    // Update existing note
                    noteRepository.updateNote(note)
                    // Add new media
                    if (mediaPaths.isNotEmpty()) {
                        noteRepository.addMediaToNote(noteId, mediaPaths)
                    }
                    RepositoryResult.Success(Unit)
                } else {
                    // Create new note with media
                    noteRepository.createNote(note, mediaPaths)
                }

                when (result) {
                    is RepositoryResult.Success -> {
                        // Update baseline
                        baselineTitle = state.title
                        baselineHtml = html
                        baselineDate = state.date

                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                saveSuccess = true,
                                hasUnsavedChanges = false,
                                pendingMedia = emptyList() // Clear after successful save
                            )
                        }
                    }
                    is RepositoryResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                error = result.message
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = "Failed to save note: ${e.message}"
                    )
                }
            }
        }
    }

    // Autosave on exit (called from screen's DisposableEffect)
    fun onAutoSave() {
        val state = _uiState.value

        // Only autosave if there are meaningful changes
        if (!state.hasUnsavedChanges) return
        if (state.title.isBlank() && state.richTextState.toHtml().isBlank()) return

        onSaveClicked()
    }
}

## 3. AddNoteScreen Implementation

3.1. AddNoteScreen (`ui/screen/notes/AddNoteScreen.kt`)

@Composable
fun AddNoteScreen(
    date: String,
    noteId: Long,
    onNavigateBack: () -> Unit,
    viewModel: AddNoteViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { viewModel.onAddMedia(it, context) }
    }

    // Navigate back on save success
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onNavigateBack()
        }
    }

    // Autosave on exit
    DisposableEffect(Unit) {
        onDispose {
            if (uiState.hasUnsavedChanges) {
                viewModel.onAutoSave()
            }
        }
    }

    Scaffold(
        topBar = {
            AttenoteTopAppBar(
                title = if (noteId > 0) "Edit Note" else "Add Note",
                showBackButton = true,
                onBackClick = onNavigateBack,
                actions = {
                    TextButton(
                        onClick = { viewModel.onSaveClicked() },
                        enabled = !uiState.isSaving && !uiState.isLoading
                    ) {
                        Text("Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Date selector
                OutlinedButton(
                    onClick = { viewModel.onDatePickerRequested() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = "Select date")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Date: ${uiState.date}")
                }

                // Title field (optional)
                AttenoteTextField(
                    value = uiState.title,
                    onValueChange = { viewModel.onTitleChanged(it) },
                    label = "Title (optional)",
                    enabled = !uiState.isSaving,
                    modifier = Modifier.fillMaxWidth()
                )

                // Rich text editor
                RichTextEditor(
                    state = uiState.richTextState,
                    onTextChange = { viewModel.onRichTextChanged() },
                    enabled = !uiState.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                )

                // Rich text toolbar
                RichTextToolbar(
                    state = uiState.richTextState,
                    enabled = !uiState.isSaving
                )

                // Media section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Attachments",
                        style = MaterialTheme.typography.labelLarge
                    )

                    // Add media button
                    OutlinedButton(
                        onClick = {
                            imagePickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        enabled = !uiState.isSaving,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add image")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Image")
                    }

                    // Pending media thumbnails
                    if (uiState.pendingMedia.isNotEmpty()) {
                        Text(
                            text = "Pending (${uiState.pendingMedia.size})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(uiState.pendingMedia) { index, media ->
                                MediaThumbnail(
                                    imagePath = media.localPath,
                                    onRemove = { viewModel.onRemovePendingMedia(index) },
                                    removable = true
                                )
                            }
                        }
                    }

                    // Saved media thumbnails (read-only)
                    if (uiState.savedMedia.isNotEmpty()) {
                        Text(
                            text = "Saved (${uiState.savedMedia.size})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.savedMedia) { media ->
                                MediaThumbnail(
                                    imagePath = media.filePath,
                                    onRemove = null,
                                    removable = false
                                )
                            }
                        }
                    }
                }

                // Error message
                if (uiState.error != null) {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Unsaved changes indicator
                if (uiState.hasUnsavedChanges) {
                    Text(
                        text = "• Unsaved changes",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    // Date picker dialog
    if (uiState.showDatePicker) {
        AttenoteDatePickerDialog(
            onDateSelected = { viewModel.onDateSelected(it) },
            onDismiss = { viewModel.onDatePickerDismissed() }
        )
    }
}

3.2. RichTextToolbar Component (Simplified - using richeditor-compose)

@Composable
fun RichTextToolbar(
    state: RichTextState,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    // Use richeditor-compose's built-in toolbar components
    // Implement tabs: Basic, Paragraph, Insert/Style

    var selectedTab by remember { mutableStateOf(0) }

    Column(modifier = modifier) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Basic") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Paragraph") }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Style") }
            )
        }

        when (selectedTab) {
            0 -> BasicToolbar(state, enabled)
            1 -> ParagraphToolbar(state, enabled)
            2 -> StyleToolbar(state, enabled)
        }
    }
}

// Toolbar implementations use richeditor-compose's RichTextStateScope actions
// Example: state.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold))

3.3. MediaThumbnail Component

@Composable
fun MediaThumbnail(
    imagePath: String?,
    onRemove: (() -> Unit)?,
    removable: Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.size(100.dp)) {
        AsyncImage(
            model = imagePath,
            contentDescription = "Attached image",
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        if (removable && onRemove != null) {
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(24.dp)
                    .background(
                        MaterialTheme.colorScheme.errorContainer,
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

## 4. Done Criteria

Build, install, and verify:
- `./gradlew :app:assembleDebug`
- `adb install -r app/build/outputs/apk/debug/app-debug.apk`
- `adb shell monkey -p com.uteacher.attendancetracker -c android.intent.category.LAUNCHER 1`

Manual testing checklist:

### Create Note Flow
- [ ] Dashboard → tap "+ New Note" → AddNote screen opens
- [ ] Title: "Add Note", Save button visible
- [ ] Date pre-filled with selected date from Dashboard
- [ ] Title field empty (optional)
- [ ] Rich text editor empty
- [ ] No attachments

### Rich Text Editing
- [ ] Type text in editor → appears correctly
- [ ] Tap Bold button → selected text becomes bold
- [ ] Tap Italic → selected text becomes italic
- [ ] Tap Underline → selected text underlined
- [ ] Clear formatting → removes all styles
- [ ] Font size up/down → changes size
- [ ] Bullet list → creates unordered list
- [ ] Numbered list → creates ordered list
- [ ] Alignment buttons → change text alignment
- [ ] Undo → reverts last change
- [ ] Redo → reapplies undone change

### Date Selection
- [ ] Tap date button → DatePicker opens
- [ ] Select different date → button updates
- [ ] Date persists in form

### Media Attachment
- [ ] Tap "Add Image" → image picker opens
- [ ] Select image → thumbnail appears in "Pending" section
- [ ] Thumbnail shows correct image preview
- [ ] Remove button (X) visible on thumbnail
- [ ] Tap remove → thumbnail disappears, file deleted
- [ ] Add multiple images → all appear in pending section

### Save Note - Create Mode
- [ ] Type title: "My First Note"
- [ ] Type rich text content with formatting
- [ ] Add 2 images
- [ ] Tap "Save" → saving indicator appears
- [ ] Save completes → navigate back to Dashboard
- [ ] Note appears in Dashboard notes list

### Edit Note Flow
- [ ] Dashboard → tap "Open" on existing note
- [ ] Title: "Edit Note"
- [ ] Title field pre-filled with note title
- [ ] Rich text editor shows existing content with formatting preserved
- [ ] Date shows note's date
- [ ] Existing images show in "Saved" section (read-only, no remove button)

### Edit and Save
- [ ] Change title
- [ ] Edit rich text (add/remove formatting)
- [ ] Add new image (appears in "Pending")
- [ ] Tap "Save" → updates persisted
- [ ] Navigate back → reopen note → changes visible
- [ ] Existing images still in "Saved", new image now in "Saved"

### Unsaved Changes Detection
- [ ] Create new note → type text → "• Unsaved changes" indicator appears
- [ ] Save note → indicator disappears
- [ ] Edit note → change text → indicator appears
- [ ] Undo change → if back to baseline, indicator disappears

### Autosave on Exit
- [ ] Create note → type text → press back (without saving)
- [ ] Note auto-saved before navigation
- [ ] Return to Dashboard → note appears in list
- [ ] Open note → content persisted

### Autosave - No Meaningless Saves
- [ ] Create note → don't type anything → press back
- [ ] No autosave (blank note not saved)
- [ ] Edit existing note → don't change anything → press back
- [ ] No autosave (no changes)

### Media File Handling
- [ ] Add image → copy saved to `filesDir/note_media/note_media_{timestamp}.jpg`
- [ ] Verify file exists in internal storage
- [ ] Remove pending image → file deleted immediately
- [ ] Save note → pending media moved to saved media
- [ ] Delete note (future feature) → media files cleaned up

### Empty Note Edge Cases
- [ ] Create note with title only (no content) → saves successfully
- [ ] Create note with content only (no title) → saves successfully
- [ ] Create note with empty title and empty content → autosave skipped (meaningless)

### Date Change Persistence
- [ ] Create note for Jan 15 → change date to Jan 20 → save
- [ ] Note appears on Jan 20 in Dashboard (not Jan 15)

## 5. Output Format

1. **Changed Files:**
   - domain/model/Note.kt (already exists)
   - domain/model/NoteMedia.kt (already exists)
   - ui/screen/notes/AddNoteUiState.kt
   - ui/screen/notes/PendingMedia.kt
   - ui/screen/notes/AddNoteViewModel.kt
   - ui/screen/notes/AddNoteScreen.kt
   - ui/screen/notes/components/RichTextToolbar.kt
   - ui/screen/notes/components/MediaThumbnail.kt
   - data/repository/NoteRepository.kt (ensure createNote, updateNote, addMediaToNote exist)
   - ui/navigation/AppNavHost.kt (wire AddNote route)
   - di/ViewModelModule.kt (add AddNoteViewModel)

2. **Notes Feature Matrix:**
   - **Create**: Empty form → fill title/content/media → save → navigate back
   - **Edit**: Load existing → pre-fill → modify → save → updates persisted
   - **Media**: Pick image → copy to internal storage → show thumbnail → remove or save
   - **Autosave**: On exit (DisposableEffect) → only if hasUnsavedChanges and non-empty
   - **Change Detection**: Compare current (title, html, date, pendingMedia) vs baseline
   - **Rich Text**: HTML storage via richeditor-compose → toHtml() / setHtml()

3. **Device Gate Command Results:**
   ```
   ./gradlew :app:assembleDebug
   BUILD SUCCESSFUL in Xs

   adb install -r app/build/outputs/apk/debug/app-debug.apk
   Success

   adb shell monkey -p com.uteacher.attendancetracker -c android.intent.category.LAUNCHER 1
   Events injected: 1
   ```

4. **Manual Checklist with Pass/Fail:**
   (Complete the checklist above with actual test results)
```

### Git Commit Message (Step 12)
`feat(step-12): implement rich notes editor, media attachments, and autosave`

## Prompt 13 - Settings + Backup/Restore + Media Cleanup Worker
```text
Implement Settings module, backup/restore with safety protocols, and orphan media cleanup worker.

## 1. SettingsUiState and ViewModel

1.1. SettingsUiState (`ui/screen/settings/SettingsUiState.kt`)

data class SettingsUiState(
    // Profile section
    val userName: String = "",
    val instituteName: String = "",
    val profileImagePath: String? = null,
    val biometricEnabled: Boolean = false,
    val canEnableBiometric: Boolean = true, // From KeyguardManager.isDeviceSecure()

    // Session format section
    val sessionFormat: SessionFormat = SessionFormat.CURRENT_YEAR,
    val sessionPreview: String = "", // Computed preview based on current date

    // FAB position section
    val fabPosition: FabPosition = FabPosition.RIGHT, // From Dashboard preference

    // Data management states
    val isExporting: Boolean = false,
    val exportSuccess: Boolean = false,
    val exportError: String? = null,
    val exportedFilePath: String? = null,

    val isImporting: Boolean = false,
    val importSuccess: Boolean = false,
    val importError: String? = null,

    // Confirmation dialogs
    val showExportConfirmDialog: Boolean = false,
    val showImportConfirmDialog: Boolean = false,
    val showBiometricDisabledWarningDialog: Boolean = false,

    // General loading and profile save
    val isLoading: Boolean = false,
    val profileSaveSuccess: Boolean = false,
    val profileSaveError: String? = null,

    // Image picker state
    val showImagePicker: Boolean = false
)

enum class FabPosition {
    LEFT, RIGHT
}

1.2. SettingsViewModel (`ui/screen/settings/SettingsViewModel.kt`)

class SettingsViewModel(
    private val settingsRepository: SettingsPreferencesRepository,
    private val backupRepository: BackupSupportRepository,
    private val biometricHelper: BiometricHelper,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
        checkDeviceSecurity()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            combine(
                settingsRepository.getUserName(),
                settingsRepository.getInstituteName(),
                settingsRepository.getProfileImagePath(),
                settingsRepository.getBiometricEnabled(),
                settingsRepository.getSessionFormat(),
                settingsRepository.getFabPosition()
            ) { userName, institute, profilePath, biometric, sessionFormat, fabPosition ->
                _uiState.update { currentState ->
                    currentState.copy(
                        userName = userName,
                        instituteName = institute,
                        profileImagePath = profilePath,
                        biometricEnabled = biometric,
                        sessionFormat = sessionFormat,
                        sessionPreview = computeSessionPreview(sessionFormat),
                        fabPosition = fabPosition,
                        isLoading = false
                    )
                }
            }.collect()
        }
    }

    private fun checkDeviceSecurity() {
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        val isDeviceSecure = keyguardManager.isDeviceSecure

        _uiState.update { it.copy(canEnableBiometric = isDeviceSecure) }

        // If biometric was enabled but device lock is now removed, show warning and disable
        if (_uiState.value.biometricEnabled && !isDeviceSecure) {
            _uiState.update {
                it.copy(
                    showBiometricDisabledWarningDialog = true
                )
            }
        }
    }

    private fun computeSessionPreview(format: SessionFormat): String {
        val currentDate = LocalDate.now()
        return when (format) {
            SessionFormat.CURRENT_YEAR -> currentDate.year.toString()
            SessionFormat.ACADEMIC_YEAR -> {
                val cutoffDate = LocalDate.of(currentDate.year, 6, 30) // June 30
                if (currentDate <= cutoffDate) {
                    "${currentDate.year - 1}-${currentDate.year}"
                } else {
                    "${currentDate.year}-${currentDate.year + 1}"
                }
            }
        }
    }

    // Profile actions
    fun onUserNameChanged(name: String) {
        _uiState.update { it.copy(userName = name) }
    }

    fun onInstituteNameChanged(institute: String) {
        _uiState.update { it.copy(instituteName = institute) }
    }

    fun onProfileImagePickRequested() {
        _uiState.update { it.copy(showImagePicker = true) }
    }

    fun onImagePickerDismissed() {
        _uiState.update { it.copy(showImagePicker = false) }
    }

    fun onProfileImageSelected(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showImagePicker = false) }

            try {
                // Copy image to internal storage
                val timestamp = System.currentTimeMillis()
                val destFile = File(context.filesDir, "app_images/profile_$timestamp.jpg")
                destFile.parentFile?.mkdirs()

                context.contentResolver.openInputStream(uri)?.use { input ->
                    val bitmap = BitmapFactory.decodeStream(input)
                    FileOutputStream(destFile).use { output ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)
                    }
                }

                // Delete old profile image if exists
                _uiState.value.profileImagePath?.let { oldPath ->
                    File(oldPath).delete()
                }

                // Save new path to DataStore
                settingsRepository.setProfileImagePath(destFile.absolutePath)

                _uiState.update {
                    it.copy(
                        profileImagePath = destFile.absolutePath,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        profileSaveError = "Failed to save image: ${e.message}"
                    )
                }
            }
        }
    }

    fun onProfileImageRemoved() {
        viewModelScope.launch {
            _uiState.value.profileImagePath?.let { path ->
                File(path).delete()
            }
            settingsRepository.setProfileImagePath(null)
            _uiState.update { it.copy(profileImagePath = null) }
        }
    }

    fun onBiometricToggled(enabled: Boolean) {
        if (!_uiState.value.canEnableBiometric && enabled) {
            // Cannot enable if device lock not set
            return
        }

        viewModelScope.launch {
            settingsRepository.setBiometricEnabled(enabled)
            _uiState.update { it.copy(biometricEnabled = enabled) }
        }
    }

    fun onBiometricDisabledWarningDismissed() {
        viewModelScope.launch {
            // Disable biometric in preferences
            settingsRepository.setBiometricEnabled(false)
            _uiState.update {
                it.copy(
                    biometricEnabled = false,
                    showBiometricDisabledWarningDialog = false
                )
            }
        }
    }

    fun onSaveProfileClicked() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, profileSaveError = null) }

            try {
                settingsRepository.setUserName(_uiState.value.userName.trim())
                settingsRepository.setInstituteName(_uiState.value.instituteName.trim())

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        profileSaveSuccess = true
                    )
                }

                // Reset success flag after 2 seconds
                delay(2000)
                _uiState.update { it.copy(profileSaveSuccess = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        profileSaveError = "Failed to save profile: ${e.message}"
                    )
                }
            }
        }
    }

    // Session format actions
    fun onSessionFormatChanged(format: SessionFormat) {
        viewModelScope.launch {
            settingsRepository.setSessionFormat(format)
            _uiState.update {
                it.copy(
                    sessionFormat = format,
                    sessionPreview = computeSessionPreview(format)
                )
            }
        }
    }

    // FAB position actions
    fun onFabPositionChanged(position: FabPosition) {
        viewModelScope.launch {
            settingsRepository.setFabPosition(position)
            _uiState.update { it.copy(fabPosition = position) }
        }
    }

    // Backup/Restore actions
    fun onExportBackupRequested() {
        _uiState.update { it.copy(showExportConfirmDialog = true) }
    }

    fun onExportConfirmed() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    showExportConfirmDialog = false,
                    isExporting = true,
                    exportSuccess = false,
                    exportError = null,
                    exportedFilePath = null
                )
            }

            when (val result = backupRepository.exportBackup()) {
                is RepositoryResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isExporting = false,
                            exportSuccess = true,
                            exportedFilePath = result.data
                        )
                    }

                    // Reset success after 5 seconds
                    delay(5000)
                    _uiState.update {
                        it.copy(
                            exportSuccess = false,
                            exportedFilePath = null
                        )
                    }
                }
                is RepositoryResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isExporting = false,
                            exportError = result.message
                        )
                    }
                }
            }
        }
    }

    fun onExportCancelled() {
        _uiState.update { it.copy(showExportConfirmDialog = false) }
    }

    fun onImportBackupRequested() {
        _uiState.update { it.copy(showImportConfirmDialog = true) }
    }

    fun onImportConfirmed(uri: Uri) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    showImportConfirmDialog = false,
                    isImporting = true,
                    importSuccess = false,
                    importError = null
                )
            }

            when (val result = backupRepository.importBackup(uri)) {
                is RepositoryResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isImporting = false,
                            importSuccess = true
                        )
                    }

                    // Restart app after successful import
                    delay(2000)
                    // Trigger app restart via MainActivity intent
                }
                is RepositoryResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isImporting = false,
                            importError = result.message
                        )
                    }
                }
            }
        }
    }

    fun onImportCancelled() {
        _uiState.update { it.copy(showImportConfirmDialog = false) }
    }

    fun onErrorDismissed() {
        _uiState.update {
            it.copy(
                exportError = null,
                importError = null,
                profileSaveError = null
            )
        }
    }
}

## 2. SettingsScreen Implementation

2.1. SettingsScreen (`ui/screen/settings/SettingsScreen.kt`)

@Composable
fun SettingsScreen(
    onSetActionBarPrimaryAction: (ActionBarPrimaryAction?) -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onProfileImageSelected(it) }
    }

    // File picker launcher for import
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onImportConfirmed(it) }
    }

    SideEffect {
        onSetActionBarPrimaryAction(
            ActionBarPrimaryAction(
                title = if (uiState.isLoading) "Saving..." else "Save",
                enabled = !uiState.isLoading,
                onClick = { viewModel.onSaveProfileClicked() }
            )
        )
    }

    DisposableEffect(onSetActionBarPrimaryAction) {
        onDispose { onSetActionBarPrimaryAction(null) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
            // Profile Section
            AttenoteSectionCard(title = "Profile") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Profile image
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.profileImagePath != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(File(uiState.profileImagePath!!))
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Default Profile",
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(24.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AttenoteButton(
                            text = "Change Photo",
                            onClick = { imagePickerLauncher.launch("image/*") },
                            enabled = !uiState.isLoading,
                            modifier = Modifier.weight(1f)
                        )

                        if (uiState.profileImagePath != null) {
                            OutlinedButton(
                                onClick = { viewModel.onProfileImageRemoved() },
                                enabled = !uiState.isLoading,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Remove")
                            }
                        }
                    }

                    // Name field
                    AttenoteTextField(
                        value = uiState.userName,
                        onValueChange = { viewModel.onUserNameChanged(it) },
                        label = "Name",
                        enabled = !uiState.isLoading
                    )

                    // Institute field
                    AttenoteTextField(
                        value = uiState.instituteName,
                        onValueChange = { viewModel.onInstituteNameChanged(it) },
                        label = "Institute",
                        enabled = !uiState.isLoading
                    )

                    // Biometric toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Biometric Lock",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            if (!uiState.canEnableBiometric) {
                                Text(
                                    text = "Set up a screen lock in device settings to enable this feature",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = uiState.biometricEnabled,
                            onCheckedChange = { viewModel.onBiometricToggled(it) },
                            enabled = uiState.canEnableBiometric && !uiState.isLoading
                        )
                    }

                    if (uiState.profileSaveSuccess) {
                        Text(
                            text = "Profile saved successfully",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Session Format Section
            AttenoteSectionCard(title = "Default Session Format") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Preview: ${uiState.sessionPreview}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    SessionFormat.values().forEach { format ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = uiState.sessionFormat == format,
                                    onClick = { viewModel.onSessionFormatChanged(format) }
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = uiState.sessionFormat == format,
                                onClick = { viewModel.onSessionFormatChanged(format) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = when (format) {
                                        SessionFormat.CURRENT_YEAR -> "Current Year"
                                        SessionFormat.ACADEMIC_YEAR -> "Academic Year"
                                    },
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = when (format) {
                                        SessionFormat.CURRENT_YEAR -> "e.g., 2026"
                                        SessionFormat.ACADEMIC_YEAR -> "e.g., 2025-2026 or 2026-2027 (based on date)"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // FAB Position Section
            AttenoteSectionCard(title = "Dashboard Menu Position") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FabPosition.values().forEach { position ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = uiState.fabPosition == position,
                                    onClick = { viewModel.onFabPositionChanged(position) }
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = uiState.fabPosition == position,
                                onClick = { viewModel.onFabPositionChanged(position) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (position) {
                                    FabPosition.LEFT -> "Left"
                                    FabPosition.RIGHT -> "Right"
                                },
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }

            // Data Management Section
            AttenoteSectionCard(title = "Data Management") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Export backup
                    AttenoteButton(
                        text = if (uiState.isExporting) "Exporting..." else "Export Backup",
                        onClick = { viewModel.onExportBackupRequested() },
                        enabled = !uiState.isExporting && !uiState.isImporting,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (uiState.exportSuccess) {
                        Text(
                            text = "Backup exported to:\n${uiState.exportedFilePath}",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    // Import backup
                    OutlinedButton(
                        onClick = { filePickerLauncher.launch("application/zip") },
                        enabled = !uiState.isExporting && !uiState.isImporting,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (uiState.isImporting) "Importing..." else "Import Backup")
                    }

                    if (uiState.importSuccess) {
                        Text(
                            text = "Backup imported successfully. App will restart...",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Text(
                        text = "⚠️ Importing will replace all existing data",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
    }

    // Dialogs
    if (uiState.showExportConfirmDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onExportCancelled() },
            title = { Text("Export Backup") },
            text = { Text("Export all app data to a ZIP file?") },
            confirmButton = {
                TextButton(onClick = { viewModel.onExportConfirmed() }) {
                    Text("Export")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onExportCancelled() }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (uiState.showBiometricDisabledWarningDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onBiometricDisabledWarningDismissed() },
            title = { Text("Biometric Lock Disabled") },
            text = { Text("Device lock screen has been removed. Biometric lock has been automatically disabled for security.") },
            confirmButton = {
                TextButton(onClick = { viewModel.onBiometricDisabledWarningDismissed() }) {
                    Text("OK")
                }
            }
        )
    }

    // Error snackbars
    if (uiState.exportError != null || uiState.importError != null || uiState.profileSaveError != null) {
        val errorMessage = uiState.exportError ?: uiState.importError ?: uiState.profileSaveError
        LaunchedEffect(errorMessage) {
            // Show snackbar with error
            delay(3000)
            viewModel.onErrorDismissed()
        }
    }
}

## 3. Backup/Restore Implementation

3.1. BackupSupportRepository Interface (`data/repository/BackupSupportRepository.kt`)

interface BackupSupportRepository {
    suspend fun exportBackup(): RepositoryResult<String> // Returns file path
    suspend fun importBackup(uri: Uri): RepositoryResult<Unit>
    suspend fun checkAndRecoverInterruptedRestore(): RepositoryResult<Unit>
}

3.2. BackupSupportRepositoryImpl (`data/repository/BackupSupportRepositoryImpl.kt`)

Key methods:

1. exportBackup():
   - PRAGMA wal_checkpoint(TRUNCATE) to flush WAL
   - Close database
   - Create ZIP with:
     - attendance_tracker_db (Room .db file, no WAL/SHM)
     - attenote_preferences.preferences_pb (DataStore protobuf file)
     - app_images/ folder (profile pictures)
     - note_media/ folder (note attachments)
     - backup_manifest.json with:
       {
         "applicationId": "com.uteacher.attendancetracker",
         "buildVariant": "debug",
         "appVersion": "1.0.0",
         "schemaVersion": 1,
         "timestamp": <epochMillis>,
         "fileChecksums": {
           "attendance_tracker_db": "<SHA256>",
           "attenote_preferences.preferences_pb": "<SHA256>",
           ...
         }
       }
   - Save ZIP to Downloads/attenote_backup_<timestamp>.zip
   - Reopen database
   - Return ZIP file path

2. importBackup(uri):
   - Read ZIP from uri
   - Extract and validate backup_manifest.json:
     - Check applicationId matches
     - Check buildVariant if present
     - Check schemaVersion compatibility (current: 1)
   - Verify all file checksums
   - If validation fails, abort and return error
   - If validation passes, begin staged restore:

     a. Write restore_journal.json to internal storage:
        {
          "phase": "extracting",
          "timestamp": <epochMillis>,
          "stagedPaths": {
            "database": "/data/data/.../staged_db",
            "datastore": "/data/data/.../staged_datastore",
            "media": "/data/data/.../staged_media"
          }
        }

     b. Extract all files to staging directory (_restore_staging/)

     c. Update journal phase to "swapping"

     d. Atomic swap:
        - Close database
        - Rename live dirs to _backup_old/
        - Rename staged dirs to live paths
        - Update journal phase to "completed"

     e. On success:
        - Delete _backup_old/
        - Delete restore_journal.json
        - Return success

     f. On failure (I/O exception during swap):
        - Attempt rollback: rename _backup_old/ back to live paths
        - Update journal phase to "rollback_attempted"
        - Return error

3. checkAndRecoverInterruptedRestore():
   - Called in MainActivity.onCreate() BEFORE Koin initialization
   - Check if restore_journal.json exists
   - If exists:
     - Read phase:
       - If "extracting" or "swapping": attempt to complete swap or rollback
       - If "completed": delete journal and proceed normally
       - If "rollback_attempted": log warning, proceed with current data
     - After recovery attempt, delete journal
   - If not exists: proceed normally

3.3. Restore Journal Data Class

data class RestoreJournal(
    val phase: RestorePhase,
    val timestamp: Long,
    val stagedPaths: Map<String, String> = emptyMap(),
    val backupPaths: Map<String, String> = emptyMap()
)

enum class RestorePhase {
    EXTRACTING,
    SWAPPING,
    COMPLETED,
    ROLLBACK_ATTEMPTED
}

## 4. Orphan Media Cleanup Worker

4.1. MediaCleanupScheduler (`util/MediaCleanupScheduler.kt`)

object MediaCleanupScheduler {
    fun schedule(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<OrphanMediaCleanupWorker>(
            repeatInterval = 7,
            repeatIntervalTimeUnit = TimeUnit.DAYS
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "orphan_media_cleanup",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}

Call this in AttendanceTrackerApplication.onCreate() after Koin initialization.

4.2. OrphanMediaCleanupWorker (`util/OrphanMediaCleanupWorker.kt`)

class OrphanMediaCleanupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val noteMediaDao = get<NoteMediaDao>() // Via Koin
            val referencedPaths = noteMediaDao.getAllFilePaths() // Get all filePath values from note_media table

            val noteMediaDir = File(applicationContext.filesDir, "note_media")
            if (!noteMediaDir.exists()) {
                return Result.success()
            }

            val allFiles = noteMediaDir.listFiles() ?: emptyArray()
            val orphanedFiles = allFiles.filter { file ->
                !referencedPaths.contains(file.absolutePath)
            }

            orphanedFiles.forEach { file ->
                try {
                    file.delete()
                    Log.d("MediaCleanup", "Deleted orphaned file: ${file.name}")
                } catch (e: Exception) {
                    Log.w("MediaCleanup", "Failed to delete ${file.name}: ${e.message}")
                }
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("MediaCleanup", "Worker failed: ${e.message}")
            Result.failure()
        }
    }
}

## 5. Integration Points

5.1. Add SettingsViewModel to ViewModelModule:

val viewModelModule = module {
    // ... existing ViewModels
    viewModel {
        SettingsViewModel(
            settingsRepository = get(),
            backupRepository = get(),
            biometricHelper = get(),
            context = androidContext()
        )
    }
}

5.2. Add Settings route to AppNavHost:

composable<Settings> {
    SettingsScreen(
        onSetActionBarPrimaryAction = onActionBarPrimaryActionChanged
    )
}

5.3. Add MediaCleanupScheduler call in AttendanceTrackerApplication:

class AttendanceTrackerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@AttendanceTrackerApplication)
            modules(appModule, repositoryModule, viewModelModule)
        }

        // Schedule orphan media cleanup
        MediaCleanupScheduler.schedule(this)
    }
}

5.4. Add restore recovery check in MainActivity.onCreate():

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Check for interrupted restore BEFORE Koin/Compose initialization
    lifecycleScope.launch {
        val backupRepo: BackupSupportRepository = get() // Requires Koin already started
        backupRepo.checkAndRecoverInterruptedRestore()
    }

    // ... rest of MainActivity setup
}

## 6. Testing Checklist

Settings Module:
□ Settings screen loads with current preferences
□ Profile name and institute edit persist to DataStore
□ Profile image picker opens and saves image to internal storage
□ Old profile image deleted on replacement
□ Profile image remove button works
□ Biometric toggle only enabled if KeyguardManager.isDeviceSecure() == true
□ Biometric toggle disabled with helper text when no device lock
□ If biometric was enabled but device lock removed, warning dialog shows on app launch
□ Warning dialog dismissal disables biometric in preferences
□ Session format radio buttons update preview text correctly
□ CURRENT_YEAR shows "2026" (if current year is 2026)
□ ACADEMIC_YEAR shows "2025-2026" before June 30, "2026-2027" after June 30
□ FAB position radio buttons update Dashboard FAB position
□ ActionBar "Save" action triggers profile save and shows success message
□ Profile save error shows error message
□ All settings persist across app restarts

Backup/Restore Module:
□ Export backup button shows confirmation dialog
□ Export confirmation triggers backup creation
□ Export creates ZIP in Downloads folder
□ ZIP contains: database, DataStore files, app_images/, note_media/
□ ZIP contains backup_manifest.json with correct metadata
□ File checksums in manifest are accurate SHA256 hashes
□ Export success shows file path
□ Export error shows error message
□ Import backup button opens file picker
□ Import validates manifest (applicationId, schemaVersion, checksums)
□ Import with wrong applicationId rejected with error
□ Import with wrong schemaVersion rejected with error
□ Import with invalid checksums rejected with error
□ Import with valid backup triggers staged restore
□ Staged restore extracts files to _restore_staging/
□ Staged restore writes restore_journal.json with phase "extracting"
□ Staged restore updates journal to "swapping" before atomic swap
□ Atomic swap renames live dirs to _backup_old/
□ Atomic swap renames staged dirs to live paths
□ Atomic swap completes and deletes _backup_old/
□ Import success shows success message and triggers app restart
□ Import error shows error message
□ Interrupted restore (app killed mid-import) leaves restore_journal.json
□ On next app launch, checkAndRecoverInterruptedRestore() runs
□ Recovery completes swap or rolls back based on journal phase
□ Recovery deletes restore_journal.json after handling
□ Full restore flow: export → modify data → import → verify all data restored
□ No crash during export or import

Orphan Media Cleanup:
□ MediaCleanupScheduler.schedule() called in Application.onCreate()
□ OrphanMediaCleanupWorker scheduled as periodic work (7-day interval)
□ Worker executes and fetches all referenced file paths from note_media table
□ Worker scans note_media/ directory for all files
□ Worker identifies orphaned files (not in referenced paths)
□ Worker deletes orphaned files
□ Worker logs deleted file names
□ Worker handles deletion errors gracefully (logs warning, continues)
□ Worker completes successfully
□ Manual test: create note with media, save, delete note, wait for worker, verify media file deleted

Integration:
□ Settings route navigates from Dashboard hamburger menu
□ Settings screen back button returns to Dashboard
□ Session format change affects Create Class screen's auto-filled session
□ FAB position change affects Dashboard FAB position immediately
□ Biometric setting change affects AuthGate behavior on next app launch
□ All preferences persist correctly across app kills

Done Criteria:
- All settings persist to DataStore correctly
- Backup export creates valid ZIP with manifest
- Backup import validates and restores data safely
- Interrupted restore recovery works
- Orphan media cleanup worker runs and cleans up unreferenced files
- No crash during any backup/restore operation

Device Gate Commands:
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell monkey -p com.uteacher.attendancetracker -c android.intent.category.LAUNCHER 1

Manual Verification:
1. Launch app, navigate to Settings
2. Edit profile, change session format, toggle FAB position
3. Verify all changes persist after app restart
4. Export backup, verify ZIP created in Downloads
5. Modify some data (add class, student, note)
6. Import backup, verify app restarts and data restored to backup state
7. Kill app during import (adb shell am force-stop), relaunch, verify recovery completes
8. Create note with media, delete note, check note_media folder after 7 days (or trigger worker manually)

Output format:
1. Changed files list.
2. Backup safety flow summary (export steps, import stages, recovery logic).
3. Device gate command results (build/install/launch).
4. Manual checklist with pass/fail for each item above.
```

### Git Commit Message (Step 13)
`feat(step-13): implement settings, backup restore workflows, and media cleanup worker`

## Prompt 14 - Final Consistency Pass + Regression + Navigation Button Audit
```text
Run a comprehensive quality pass to ensure UI consistency, fix regressions, and audit system navigation visibility across all screens.

## 1. UI Consistency Audit and Fixes

1.1. Theme Component Usage Audit

Check all screens use shared themed components from `ui/theme/component/`:

Required components:
- **AttenoteTopAppBar**: All feature screens MUST use this (consistent title style, back button, actions)
- **AttenoteSectionCard**: Group related content with consistent padding and elevation
- **AttenoteTextField**: All text inputs MUST use this (consistent label, error, support text style)
- **AttenoteButton**: Primary actions (Save, Add, etc.)
- **AttenoteSecondaryButton**: Secondary actions (Cancel, Remove, etc.)

Screens to audit:
□ SplashScreen
□ SetupScreen
□ AuthGateScreen
□ DashboardScreen
□ CreateClassScreen
□ ManageClassListScreen
□ EditClassScreen
□ ManageStudentsScreen
□ TakeAttendanceScreen
□ AddNoteScreen
□ SettingsScreen

For each screen:
1. Verify AttenoteTopAppBar used (not raw TopAppBar)
2. Verify content sections wrapped in AttenoteSectionCard where appropriate
3. Verify all TextFields use AttenoteTextField
4. Verify primary action buttons use AttenoteButton
5. Verify consistent spacing (16.dp screen padding, 12.dp between fields, 8.dp between small elements)

1.2. Typography Consistency Audit

Typography hierarchy must be consistent:
- **Top bar titles**: MaterialTheme.typography.titleMedium
- **Section card titles**: MaterialTheme.typography.titleSmall
- **Field labels**: Built into AttenoteTextField
- **Body text**: MaterialTheme.typography.bodyMedium
- **Supporting text**: MaterialTheme.typography.bodySmall
- **Button text**: Built into AttenoteButton/AttenoteSecondaryButton (or wrapped Button/OutlinedButton)

Check all Text() composables use MaterialTheme.typography.* (no hardcoded font sizes).

1.3. Color Consistency Audit

Primary color usage:
- Navy (#1A4D7A): primary actions, selected states, emphasis text
- Teal (#26A69A): secondary actions, accents
- Surface colors: MaterialTheme.colorScheme.surface, surfaceVariant
- Error: MaterialTheme.colorScheme.error

Check no hardcoded Color() values exist (all use MaterialTheme.colorScheme.*).

1.4. Spacing Consistency Audit

Standard spacing values:
- Screen padding: 16.dp
- Section card internal padding: 12.dp
- Between form fields: 12.dp
- Between buttons in row: 8.dp
- Between sections: 16.dp
- Icon-to-text spacing: 8.dp

Audit all screens for spacing consistency. Fix any outliers.

1.5. App Bar Consistency Audit

All feature screens MUST have:
- Title in titleMedium style
- Back button with proper navigation (left arrow icon)
- Actions on right (text buttons for Save, icon buttons for others)
- Consistent colors (primary container background, onPrimaryContainer content)

Audit pattern:
```kotlin
AttenoteTopAppBar(
    title = "Screen Title",
    onNavigateBack = { /* navigate up */ },
    actions = {
        // Optional: Save, Edit, etc.
        TextButton(onClick = { }) {
            Text("Save")
        }
    }
)
```

Check all screens follow this pattern exactly.

## 2. Navigation Route Checks

2.1. Dashboard Navigation Actions

From Dashboard, verify all actions navigate correctly:

□ Tap scheduled class card → TakeAttendance(classId, scheduleId, date)
  - classId, scheduleId, date passed correctly
  - Date in ISO-8601 format (yyyy-MM-dd)

□ Tap note card → AddNote(date, noteId)
  - date in ISO-8601 format
  - noteId passed for existing notes

□ Tap "+ New Note" button → AddNote(date, noteId = -1)
  - Current selected date passed
  - noteId = -1 for new note

□ FAB menu "Manage Classes" → ManageClassList

□ FAB menu "Manage Students" → ManageStudents

□ FAB menu "Create Class" → CreateClass

□ FAB menu "Settings" → Settings

2.2. ManageClassList Navigation Actions

□ Tap class card → EditClass(classId)
  - classId passed correctly

□ Tap "Create Class" button (if present) → CreateClass

2.3. EditClass Navigation Actions

□ Manual add student → No navigation (opens dialog)

□ CSV import → No navigation (opens file picker)

□ Copy from class → No navigation (opens dialog)

□ Tap active/inactive student → No navigation (toggle in place)

2.4. ManageStudents Navigation Actions

□ Tap student card → Opens StudentEditorDialog (no navigation)

□ Tap FAB → Opens StudentEditorDialog in ADD mode (no navigation)

2.5. Back Navigation Checks

Every feature screen MUST support back navigation:

□ SetupScreen → No back (entry point for first run)
□ AuthGateScreen → No back (gate before dashboard)
□ DashboardScreen → No back (main screen)
□ CreateClassScreen → Back to Dashboard
□ ManageClassListScreen → Back to Dashboard
□ EditClassScreen → Back to ManageClassList
□ ManageStudentsScreen → Back to Dashboard
□ TakeAttendanceScreen → Back to Dashboard
□ AddNoteScreen → Back to Dashboard (with autosave)
□ SettingsScreen → Back to Dashboard

Test back button and Android system back gesture for each screen.

2.6. Route Parameter Validation

Check all routes with parameters validate inputs:

□ TakeAttendance(classId, scheduleId, date):
  - classId > 0
  - scheduleId > 0
  - date matches yyyy-MM-dd format
  - If invalid, show error and navigate back

□ EditClass(classId):
  - classId > 0
  - If invalid, show error and navigate back

□ AddNote(date, noteId):
  - date matches yyyy-MM-dd format
  - noteId >= -1 (can be -1 for new note)
  - If invalid, show error and navigate back

Add validation checks in each ViewModel's init block or LoadData method.

## 3. System Navigation Bar Visibility Audit

**CRITICAL REQUIREMENT**: Android system navigation buttons MUST remain visible at all times.

3.1. MainActivity System Bar Policy Check

In [MainActivity.kt](app/src/main/java/com/uteacher/attendancetracker/MainActivity.kt), verify:

□ NO calls to `WindowCompat.setDecorFitsSystemWindows(window, false)`
□ NO calls to `WindowInsetsControllerCompat.hide(WindowInsetsCompat.Type.navigationBars())`
□ NO use of `systemBarsBehavior = BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE`

Current code check:
```kotlin
private fun applySystemBarPolicy() {
    WindowCompat.setDecorFitsSystemWindows(window, true) // ✓ Required standard behavior
    window.statusBarColor = Color.Transparent
    window.navigationBarColor = Color.Transparent

    // VERIFY: No hide() calls for navigation bars
    WindowCompat.getInsetsController(window, window.decorView).apply {
        systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT // ✓ OK
        // MUST NOT contain: hide(WindowInsetsCompat.Type.navigationBars())
    }
}
```

If any hide() calls found, REMOVE them immediately.

3.2. Compose Insets Handling Check

Check all Scaffold usages apply insets correctly:

```kotlin
Scaffold(
    topBar = { /* ... */ },
    modifier = Modifier.fillMaxSize() // No .windowInsetsPadding() that might clip bars
) { paddingValues ->
    // Content must use paddingValues to avoid status bar overlap
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues) // ✓ Correct - applies system bar padding
    ) {
        // Screen content
    }
}
```

Audit all screens for correct paddingValues usage.

3.3. Full-Screen Content Check

Check screens with full-screen elements (calendar, image pickers, dialogs):

□ Dashboard calendar expansion → Does NOT hide nav bars
□ Image picker (profile, note media) → Does NOT hide nav bars
□ Date picker dialogs → Does NOT hide nav bars
□ Confirmation dialogs → Does NOT hide nav bars

3.4. Device Testing for Nav Bar Visibility

Test on physical device:
1. Launch app
2. Navigate through ALL screens (Splash → Setup → Dashboard → all feature screens)
3. At EVERY screen, verify Android navigation buttons visible at bottom
4. Test with gesture navigation AND button navigation (if device supports both)
5. If any screen hides nav buttons, this is a BLOCKER - must fix before proceeding

## 4. Reliability and Error Handling Checks

4.1. Null Safety Audit

Check all ViewModel/Repository code for unsafe null operations:

□ Flow collection: Use `collectAsState()` with proper defaults
□ Database queries: Handle null results (e.g., `findClassById()` may return null)
□ Route parameters: Validate non-null before use
□ DataStore reads: Provide default values in `.first()` calls
□ File operations: Check file existence before read/delete

4.2. Exception Handling Audit

All repository suspend functions MUST return RepositoryResult (not throw):

```kotlin
// ✓ Correct
suspend fun saveClass(class: ClassDomain): RepositoryResult<Long> {
    return try {
        // ... operation
        RepositoryResult.Success(classId)
    } catch (e: Exception) {
        RepositoryResult.Error("Failed to save class: ${e.message}")
    }
}

// ✗ Wrong
suspend fun saveClass(class: ClassDomain): Long {
    // ... operation (throws on error)
}
```

Audit all repository methods for proper error wrapping.

4.3. ViewModel Error State Audit

All ViewModels MUST surface repository errors to UI:

```kotlin
when (val result = repository.saveClass(classDomain)) {
    is RepositoryResult.Success -> {
        _uiState.update { it.copy(saveSuccess = true) }
    }
    is RepositoryResult.Error -> {
        _uiState.update { it.copy(saveError = result.message) } // ✓ Surface error
    }
}
```

Check all ViewModels display errors in UiState.

4.4. Input Validation Audit

All forms MUST validate before save:

□ SetupScreen: userName not blank
□ CreateClassScreen: all required fields filled, schedules not empty, no overlaps
□ ManageStudents: name and registration number not blank, no duplicate (name, regNumber)
□ TakeAttendance: lesson notes optional (allow blank)
□ AddNote: title and content cannot both be blank
□ Settings: profile name not blank

Check validation logic in each ViewModel before repository calls.

4.5. Concurrent Operation Safety

Check for race conditions:

□ Rapid double-tap on Save buttons → Disable button during loading
□ Attendance save rapid retries → Idempotent with unique constraint
□ Profile image selection while saving → Disable picker during loading
□ Backup export during import → Disable both buttons when either is running

Verify all action buttons respect `enabled = !uiState.isLoading`.

## 5. Full Module Regression Checklist

Test each module end-to-end on device.

5.1. Setup + Splash + Auth Module
□ First launch shows Splash with "Start" button
□ Tap Start navigates to Setup
□ Setup requires name (validation works)
□ Setup allows optional profile image (picker works, image saves)
□ Setup allows optional institute name
□ Biometric toggle only enabled if device lock exists
□ Complete setup navigates to Dashboard
□ Close app, relaunch → goes directly to Dashboard (or AuthGate if biometric enabled)
□ If biometric enabled, AuthGate shows before Dashboard
□ AuthGate prompts biometric/device credential
□ Successful auth navigates to Dashboard
□ Failed auth shows retry button

5.2. Dashboard Module
□ Dashboard loads with current date selected
□ Calendar collapsed by default (shows week view)
□ Week view shows 7 days with left/right arrows (previous day / next day)
□ Tap calendar expand button → shows month view
□ Tap date in calendar → updates selected date
□ Calendar auto-collapses after date selection
□ Scheduled classes for selected date appear in "Scheduled Classes" section
□ Notes for selected date appear in "Notes" section
□ "+ New Note" button visible in Notes section header
□ Tap "+ New Note" → navigates to AddNote with current date
□ Tap scheduled class card → navigates to TakeAttendance with correct params
□ Tap note card → navigates to AddNote with existing note
□ FAB position matches Settings preference (LEFT or RIGHT)
□ FAB menu opens on tap
□ FAB menu closes on scrim tap
□ FAB menu closes on content scroll
□ FAB menu "Manage Classes" → navigates to ManageClassList
□ FAB menu "Manage Students" → navigates to ManageStudents
□ FAB menu "Create Class" → navigates to CreateClass
□ FAB menu "Settings" → navigates to Settings
□ Calendar date indicators (dots) show for dates with classes or notes

5.3. Create Class Module
□ CreateClassScreen loads with empty form
□ Session auto-filled based on Settings preference (CURRENT_YEAR or ACADEMIC_YEAR)
□ Subject and institute fields trigger className auto-generation
□ ClassName shows as "{subject} - {institute}"
□ ClassName is editable
□ Date pickers open and set startDate/endDate
□ End date must be after start date (validation works)
□ Schedule slot editor allows selecting day, start time, end time
□ Add slot button adds to schedules list
□ Cannot add overlapping slots for same day (validation error)
□ Cannot add slot with endTime <= startTime (validation error)
□ Delete slot button removes from list
□ Save button disabled until all required fields filled and at least one schedule added
□ Save creates class and navigates back to Dashboard
□ Created class appears in Dashboard on matching scheduled dates
□ Duplicate class (same instituteName, session, department, semester, subject, section) rejected

5.4. Manage Class List Module
□ ManageClassListScreen loads with all classes
□ Each class card shows class info and open/close toggle
□ Toggle open/close updates class state
□ Closed classes not shown in Dashboard scheduled classes
□ Tap class card → navigates to EditClass(classId)

5.5. Edit Class Module
□ EditClassScreen loads class details
□ Class info section shows read-only fields (instituteName, session, department, semester, subject, section, className, schedules)
□ Date range section shows editable startDate and endDate
□ Changing date range to exclude existing attendance dates shows warning
□ Warning states attendance records preserved (not deleted)
□ Save date range updates class
□ Expanding date range back makes out-of-range attendance visible again
□ Student roster section shows students linked to class
□ Active toggle per student updates isActiveInClass
□ Manual add student dialog opens, allows selecting from global student list
□ CSV import opens file picker, shows preview with warnings
□ CSV missing Name → placeholder "UNKNOWN_<timestamp>_<row>"
□ CSV missing RegNumber → placeholder "UNREG_<timestamp>_<row>"
□ CSV duplicate rows deduplicated with warning
□ CSV confirm adds students to class roster
□ Copy from class dialog opens, allows selecting source class, copies active students

5.6. Manage Students Module
□ ManageStudentsScreen loads with global student list
□ Search bar filters students by name, registration number, roll number, email, phone
□ Tap student card → opens StudentEditorDialog in EDIT mode
□ EDIT mode shows all fields, registration number disabled (immutable)
□ Save updates student (validation prevents duplicate name+regNumber)
□ Tap FAB → opens StudentEditorDialog in ADD mode
□ ADD mode allows entering all fields including registration number
□ Save creates student (validation prevents duplicate name+regNumber)
□ Active toggle updates student.isActive globally
□ Inactive students excluded from class rosters and attendance

5.7. Attendance Module
□ TakeAttendanceScreen loads with class, schedule, and student list
□ Students default to Present (green card background)
□ Tap student card toggles Present/Absent (green/red background)
□ Lesson notes field allows optional HTML input (or plain text)
□ If attendance session already exists for (classId, scheduleId, date), load existing records
□ Save button creates/updates attendance session
□ Save idempotent (duplicate saves do not create duplicate sessions)
□ Save navigates back to Dashboard
□ Reopening same attendance shows saved state

5.8. Notes Module
□ AddNoteScreen loads with date from route param
□ Create mode (noteId = -1): all fields empty
□ Edit mode (noteId > 0): loads existing note with title, content, date, savedMedia
□ Date picker allows changing note date
□ Title field optional
□ RichTextEditor allows formatting (bold, italic, underline, strikethrough, alignment, lists, headings)
□ RichTextToolbar tabs (Basic, Paragraph, Style) show correct buttons
□ Active formatting buttons highlighted
□ "Attach Image" button opens image picker
□ Selected images appear in "Pending Media" with thumbnails
□ Pending media thumbnails have remove button (X icon)
□ Saved media thumbnails shown separately, no remove button
□ Save button creates/updates note with HTML content
□ Save copies pending media to internal storage (note_media folder)
□ Save navigates back to Dashboard
□ Exit without save (back button) triggers autosave IF hasUnsavedChanges AND (title or content not blank)
□ Empty note (blank title and content) does NOT save on exit
□ Reopening same note shows saved content, media, date
□ Delete note (if implemented) deletes note and NoteMediaEntity rows via CASCADE
□ Immediate cleanup deletes unreferenced media files

5.9. Settings Module
□ SettingsScreen loads with current preferences
□ Profile name and institute editable
□ Profile image picker opens, saves image to internal storage with timestamp
□ Old profile image deleted on replacement
□ Profile image remove button deletes image and clears path
□ Biometric toggle only enabled if device lock set
□ Biometric toggle disabled with helper text if no device lock
□ If biometric was enabled but device lock removed, warning dialog shows on app launch
□ Warning dialog dismissal disables biometric preference
□ Session format radio buttons update preview text
□ Session format change affects Create Class screen's auto-filled session
□ FAB position radio buttons update Dashboard FAB position
□ Save profile button persists name and institute
□ Export backup button shows confirmation dialog
□ Export confirmation creates ZIP in Downloads with manifest and checksums
□ Export success shows file path
□ Import backup button opens file picker
□ Import validates manifest (applicationId, schemaVersion, checksums)
□ Import rejected if validation fails (shows error)
□ Import success triggers staged restore and app restart
□ After import, all data restored correctly
□ Interrupted import (app killed) leaves restore_journal.json
□ Next launch detects journal and completes restore or rolls back

## 6. Testing Checklist Summary

Run through all modules above on physical device and mark pass/fail for each item.

Critical Blockers (MUST pass):
□ Android navigation buttons visible on ALL screens
□ No app crash during any normal workflow
□ Setup completes and persists profile
□ Dashboard loads and shows scheduled classes
□ Create class saves and appears in dashboard
□ Attendance save works and reloads correctly
□ Notes save with rich text and media
□ Backup export creates valid ZIP
□ Backup import restores data correctly

High Priority (SHOULD pass):
□ All themed components used consistently
□ All navigation routes work correctly
□ All back navigation works
□ All input validation prevents invalid saves
□ All error messages display to user
□ All loading states disable action buttons

Medium Priority (NICE to pass):
□ Typography consistent across screens
□ Spacing consistent across screens
□ Color usage consistent with theme
□ All search/filter features work
□ All CSV import edge cases handled

## 7. Known Gaps and Future Work

Document any remaining gaps or deferred features:

Examples:
- Advanced analytics dashboard (out of scope for v1)
- Cloud sync (out of scope for v1)
- Bulk student operations (deferred)
- Schedule conflict detection across classes (deferred)
- Attendance reports/export (deferred)
- Note search/filter (deferred)

Done Criteria:
- All critical blockers pass
- All high priority items pass
- Android navigation buttons visible on ALL screens
- Full smoke test passes on physical device
- No known crashes during normal workflows

Device Gate Commands:
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell monkey -p com.uteacher.attendancetracker -c android.intent.category.LAUNCHER 1

Manual Verification:
1. Full smoke test: Launch → Setup → Dashboard → Create Class → Manage Students → Take Attendance → Add Note → Settings → Backup/Restore
2. Verify Android nav buttons visible at every step
3. Test back navigation from every screen
4. Test all validation rules (empty fields, duplicates, overlaps)
5. Test all error scenarios (invalid dates, failed saves, import errors)
6. Test interrupted restore recovery (kill app during import)

Output format:
1. Changed files list (fixes applied).
2. Fixed issues list (UI consistency, navigation, reliability).
3. Device gate command results (build/install/launch).
4. Full module-by-module checklist with pass/fail for each item above.
5. Remaining known gaps (document only, do not block).
```

### Git Commit Message (Step 14)
`chore(step-14): align ui consistency and fix cross-module regressions`

## Prompt 15 - Code Review and Edge Case Hardening
```text
Conduct a comprehensive code review as a strict reviewer to identify bugs, data integrity risks, edge cases, and UX/accessibility gaps before release.

## 1. Data Integrity Review

1.1. Database Constraint Enforcement

Review all Room entities for proper constraints:

**ClassEntity** ([data/local/entity/ClassEntity.kt](app/src/main/java/com/uteacher/attendancetracker/data/local/entity/ClassEntity.kt)):
□ Unique constraint on (instituteName, session, department, semester, subject, section) exists
□ section field is NOT NULL (empty string "" for no section, not null)
□ startDate and endDate are NOT NULL
□ Foreign key relationships defined correctly

**StudentEntity** ([data/local/entity/StudentEntity.kt](app/src/main/java/com/uteacher/attendancetracker/data/local/entity/StudentEntity.kt)):
□ Unique constraint on (name, registrationNumber) exists
□ name and registrationNumber are NOT NULL
□ Optional fields (rollNumber, email, phone) allow null

**ClassStudentCrossRef** ([data/local/entity/ClassStudentCrossRef.kt](app/src/main/java/com/uteacher/attendancetracker/data/local/entity/ClassStudentCrossRef.kt)):
□ Composite primary key on (classId, studentId)
□ Foreign keys with CASCADE delete on both classId and studentId
□ isActiveInClass has default value (true)

**ScheduleEntity** ([data/local/entity/ScheduleEntity.kt](app/src/main/java/com/uteacher/attendancetracker/data/local/entity/ScheduleEntity.kt)):
□ Foreign key to classId with appropriate delete behavior
□ No unique constraint (multiple schedules per class allowed)
□ dayOfWeek, startTime, endTime are NOT NULL

**AttendanceSessionEntity** ([data/local/entity/AttendanceSessionEntity.kt](app/src/main/java/com/uteacher/attendancetracker/data/local/entity/AttendanceSessionEntity.kt)):
□ Unique constraint on (classId, scheduleId, date) exists
□ Foreign key to scheduleId with ON DELETE RESTRICT (cannot delete schedule if attendance exists)
□ lessonNotes allows null

**AttendanceRecordEntity** ([data/local/entity/AttendanceRecordEntity.kt](app/src/main/java/com/uteacher/attendancetracker/data/local/entity/AttendanceRecordEntity.kt)):
□ Unique constraint on (sessionId, studentId) exists
□ Foreign keys with appropriate delete behavior
□ isPresent has default value (true)

**NoteEntity** ([data/local/entity/NoteEntity.kt](app/src/main/java/com/uteacher/attendancetracker/data/local/entity/NoteEntity.kt)):
□ date is NOT NULL
□ title allows null (optional)
□ content is NOT NULL (empty string for blank notes)

**NoteMediaEntity** ([data/local/entity/NoteMediaEntity.kt](app/src/main/java/com/uteacher/attendancetracker/data/local/entity/NoteMediaEntity.kt)):
□ Foreign key to noteId with ON DELETE CASCADE
□ filePath and mimeType are NOT NULL

1.2. Input Normalization Review

Check all repositories apply normalization before INSERT/UPDATE:

Normalization function (should exist in `util/` or `data/`):
```kotlin
fun String.normalize(): String {
    return this.trim()
        .replace("\\s+".toRegex(), " ") // Collapse multiple spaces to single space
        .lowercase() // Case-insensitive comparison
}
```

Review usage in:
□ ClassRepository: Normalize instituteName, session, department, semester, subject, section before save
□ StudentRepository: Normalize name, registrationNumber before save
□ Duplicate checks: Use normalized values in conflict queries

1.3. Transaction Safety Review

All multi-table writes MUST use `db.withTransaction {}`:

□ ClassRepository.createClass (class + schedules) → wrapped in transaction
□ AttendanceRepository.saveAttendance (session + records) → wrapped in transaction
□ NoteRepository.saveNote (note + media) → wrapped in transaction
□ BackupRepository.importBackup (all tables) → wrapped in transaction

Check for any multi-table writes NOT wrapped in transactions (data integrity risk).

1.4. Date/Time Handling Review

All date/time conversions MUST be consistent:

□ LocalDate stored as ISO-8601 string (yyyy-MM-dd) in Room
□ LocalTime stored as ISO-8601 string (HH:mm:ss) in Room
□ DayOfWeek stored as Int (1=Monday, 7=Sunday) in Room
□ Route date parameters parsed with `LocalDate.parse(dateString)` (validates format)
□ No timezone-aware date operations (all LocalDate, no ZonedDateTime)

Check for:
- Inconsistent date formats (MM-dd-yyyy, dd/MM/yyyy, etc.) → must be yyyy-MM-dd
- Timezone conversions (should not exist for LocalDate)
- Date arithmetic errors (e.g., adding days without checking bounds)

## 2. Backup/Restore Edge Case Review

2.1. Export Edge Cases

□ **Empty database export**: If no classes/students/notes exist, export should still create valid ZIP with empty manifest
□ **Large database export**: If database > 100MB, export may timeout → add progress indicator
□ **Concurrent export attempts**: Multiple export button taps should be prevented (button disabled during export)
□ **WAL checkpoint failure**: If PRAGMA wal_checkpoint fails, what happens? → must handle gracefully
□ **File permission errors**: If Downloads folder not writable, show clear error message
□ **Checksum calculation errors**: If file read fails during checksum, must abort export

Check BackupSupportRepository.exportBackup() handles all above cases.

2.2. Import Edge Cases

□ **Corrupted ZIP**: If ZIP extraction fails, must abort without modifying existing data
□ **Missing manifest**: If backup_manifest.json not found in ZIP, reject with clear error
□ **Manifest schema mismatch**: If schemaVersion > current app version, reject (cannot restore future schema)
□ **Checksum mismatch**: If ANY file checksum fails validation, reject entire import
□ **Partial extraction failure**: If extraction fails mid-way, cleanup staged files and abort
□ **Disk space exhaustion**: If not enough space for staged files, abort before swap
□ **Database restore failure**: If staged DB cannot open (corrupt), must rollback
□ **App kill during swap**: If app killed DURING rename operations, journal must handle recovery

Check BackupSupportRepository.importBackup() handles all above cases.

2.3. Restore Recovery Review

Review checkAndRecoverInterruptedRestore() logic:

□ If restore_journal.json phase = "extracting":
  - Staged files may be incomplete → delete staged directory, keep existing data, delete journal
□ If restore_journal.json phase = "swapping":
  - Swap may be incomplete → attempt to complete swap if _backup_old exists, else rollback
□ If restore_journal.json phase = "completed":
  - Swap succeeded but journal not deleted → delete _backup_old and journal
□ If restore_journal.json phase = "rollback_attempted":
  - Rollback attempted but may have failed → log warning, keep current state, delete journal

Edge case: What if both live data AND _backup_old exist after app kill?
- Priority: Keep _backup_old (known good state), rename to live if current data corrupt

2.4. Backup Manifest Validation

Check manifest validation logic catches:
□ applicationId mismatch (e.g., restoring from different app)
□ buildVariant mismatch (e.g., debug backup on release build) → warn but allow
□ appVersion too old (e.g., v0.9 backup on v1.0 app) → warn but allow if schemaVersion matches
□ schemaVersion mismatch (e.g., schema v2 backup on schema v1 app) → reject
□ Missing required files in manifest.fileChecksums → reject
□ Extra files in ZIP not in manifest → warn but ignore

## 3. Media Cleanup Edge Case Review

3.1. Immediate Cleanup on Note Delete

Review NoteRepository.deleteNote():
□ DB delete with CASCADE removes NoteMediaEntity rows
□ After DB delete, attempt to delete files from note_media/ directory
□ If file delete fails (file in use, permission denied), log warning but do NOT fail entire operation
□ Return success if DB delete succeeded (file cleanup is best-effort)

Edge cases:
□ Note deleted but media file still in use by another process → cleanup fails, worker will retry
□ Note deleted but file path invalid or file already deleted → log and continue
□ Note deleted but note_media directory deleted by user → handle FileNotFoundException

3.2. Orphan Media Worker Review

Review OrphanMediaCleanupWorker.doWork():
□ Query all filePath values from note_media table
□ Scan note_media/ directory for all files
□ Identify orphans (files not in DB)
□ Attempt to delete each orphan
□ If delete fails for one file, continue with remaining files (don't abort entire job)
□ Return Result.success() even if some deletes failed
□ Return Result.failure() only if entire scan/query failed

Edge cases:
□ note_media/ directory does not exist → return success (nothing to clean)
□ note_media/ directory not readable → log error, return failure
□ Files in note_media/ subdirectories → worker should scan recursively or ignore subdirs?
□ Concurrent note save while worker running → file added after scan may be flagged as orphan → must be resilient

Race condition check:
- Worker queries DB at time T1
- User saves note with media at time T2 (after query)
- Worker scans filesystem at time T3 → new media file NOT in query results → flagged as orphan
- **Mitigation**: Worker should re-query DB right before deleting each file to confirm still orphaned

3.3. Media File Path Consistency

Review all media save operations:
□ Profile image: Saved to `app_images/profile_<timestamp>.jpg` → path stored in DataStore
□ Note media: Saved to `note_media/note_<noteId>_<timestamp>.jpg` → path stored in NoteMediaEntity.filePath

Check:
□ All file paths are absolute paths (not relative)
□ All file paths use context.filesDir as root (not external storage)
□ Timestamp in filename prevents Coil cache staleness
□ Old files deleted when replaced (profile image)

Edge case:
□ User saves note with media, app killed before DB commit → media file saved but no DB record → orphan created
  - Worker will clean up on next run (acceptable)

## 4. Schedule and Attendance Edge Case Review

4.1. Schedule Overlap Detection

Review CreateClassViewModel and EditClassViewModel schedule validation:

Overlap scenarios to test:
□ Same day, exact same time range → must reject
□ Same day, overlapping start time → must reject
□ Same day, overlapping end time → must reject
□ Same day, one schedule contains another → must reject
□ Same day, back-to-back schedules (end time = next start time) → should ALLOW (no overlap)
□ Different days, same time → should ALLOW

Check overlap detection logic:
```kotlin
fun List<ScheduleSlotDraft>.hasOverlap(newSlot: ScheduleSlotDraft): Boolean {
    return this.any { existing ->
        existing.dayOfWeek == newSlot.dayOfWeek &&
        (newSlot.startTime < existing.endTime && newSlot.endTime > existing.startTime)
    }
}
```

Test: Mon 10:00-11:00 and Mon 11:00-12:00 → should NOT overlap (boundary case).

4.2. Schedule Validation

Review ScheduleSlotDraft validation:
□ endTime > startTime (not >=, must be strictly greater)
□ dayOfWeek is valid (DayOfWeek enum, Monday-Sunday)
□ startTime and endTime are valid LocalTime values

Edge cases:
□ Overnight schedules (e.g., 23:00-01:00) → NOT supported in v1 (must reject)
□ Zero-duration schedules (e.g., 10:00-10:00) → must reject
□ Midnight schedules (e.g., 00:00-01:00) → should work

4.3. Attendance Session Idempotency

Review AttendanceRepository.saveAttendance():

Idempotent behavior:
1. Check if session exists: `findSessionByClassScheduleDate(classId, scheduleId, date)`
2. If exists: Load existing sessionId, DELETE all attendance_records for that session, INSERT new records
3. If not exists: INSERT session, INSERT records

Race condition:
- User taps Save twice rapidly
- Thread 1: Check session → not exists → insert session (sessionId = 101)
- Thread 2: Check session → not exists → insert session (FAILS due to unique constraint)
- **Mitigation**: Unique constraint on (classId, scheduleId, date) prevents duplicate sessions
- Thread 2 must catch ConstraintViolationException, retry with SELECT to get existing sessionId

Check exception handling in saveAttendance().

4.4. Scheduled-Only Attendance Enforcement

Review TakeAttendanceViewModel:

Loading validation:
□ Verify scheduleId matches a real schedule for the given classId
□ Verify date falls within class startDate/endDate range
□ Verify date's dayOfWeek matches schedule.dayOfWeek

If validation fails:
□ Show error message: "This class is not scheduled for the selected date"
□ Navigate back to Dashboard

Check these validations exist in ViewModel.loadData() or init.

4.5. Date Range Shrink Warning

Review EditClassViewModel:

When user changes startDate or endDate:
□ Query AttendanceSessionEntity for sessions with date < new startDate OR date > new endDate
□ If any sessions found outside new range:
  - Show warning dialog: "X attendance sessions fall outside the new date range. These records will be preserved but hidden until you expand the range again."
  - Allow user to Cancel or Proceed
□ On Proceed: Save new date range (do NOT delete out-of-range sessions)

Check:
□ Warning triggers correctly when date range shrinks
□ No warning when date range expands
□ Out-of-range sessions remain in DB (not deleted)
□ Dashboard correctly filters attendance by class date range (only shows sessions within range)

## 5. CSV Import Edge Case Review

5.1. CSV Parsing Edge Cases

Review CSVImportHelper or equivalent:

Edge cases:
□ Empty CSV file (no rows) → reject with error "File is empty"
□ CSV with only header row (no data rows) → reject with error "No students to import"
□ Missing required columns (Name, Registration Number) → generate placeholders
□ Extra columns not in schema → ignore
□ Rows with all blank values → skip row with warning
□ Malformed CSV (unclosed quotes, mismatched columns) → parsing error, reject with clear message
□ CSV with BOM (Byte Order Mark) → handle gracefully (kotlin-csv should handle)
□ CSV with different line endings (CRLF vs LF) → handle gracefully

5.2. Placeholder Generation Review

Review placeholder logic:

□ Missing Name → `"UNKNOWN_<batchTimestamp>_<rowIndex>"`
□ Missing Registration Number → `"UNREG_<batchTimestamp>_<rowIndex>"`
□ batchTimestamp = System.currentTimeMillis() at import START (not per row)
□ rowIndex = row number in CSV (1-based or 0-based? must be consistent)

Check uniqueness:
- If CSV has 2 rows with missing Name → "UNKNOWN_1234567890_1" and "UNKNOWN_1234567890_2" → unique
- If user imports same CSV twice → different batchTimestamp → unique placeholders

5.3. Duplicate Detection Review

Review duplicate detection in CSV import:

Within same CSV:
□ Detect duplicate (name, regNumber) rows in CSV itself
□ Show warning: "Row X is duplicate of Row Y, will be deduplicated"
□ Import only first occurrence

Across existing DB:
□ Query StudentEntity for matching (normalized name, normalized regNumber)
□ If exists: Link existing student to class (do NOT create duplicate)
□ Show info: "Row X matched existing student, linked to class"

Check normalization applied before comparison:
- "John Doe" vs "john doe" → same student (case-insensitive)
- "John  Doe" vs "John Doe" → same student (whitespace collapsed)

5.4. CSV Preview Screen Review

Review CSV import preview:

Display:
□ Show parsed data in table (Name, Reg Number, Roll Number, Email, Phone)
□ Highlight rows with warnings (missing required fields, duplicates)
□ Show placeholders in preview (e.g., "UNKNOWN_1234567890_1")
□ Show summary: "X students to import, Y duplicates, Z warnings"
□ Confirm and Cancel buttons

Validation:
□ User must review and confirm before import
□ If user cancels, no data written to DB

## 6. Rich Text Editor Edge Case Review

6.1. RichTextState Lifecycle

Review AddNoteViewModel richTextState handling:

□ RichTextState initialized in UiState (not ViewModel field)
□ Loading existing note: Use `richTextState.setHtml(note.content)` in ViewModel.loadNote()
□ Baseline tracking: Capture `richTextState.toHtml()` after load as baseline
□ Change detection: Compare `richTextState.toHtml()` with baseline

Edge cases:
□ User types text then immediately deletes → HTML may differ from baseline (empty paragraph tags)
□ User applies formatting then removes → HTML structure may differ even if text same
□ Empty content: `richTextState.toHtml()` returns `""` or `"<p></p>"` → normalize before comparison

Normalization:
```kotlin
fun String.normalizeHtml(): String {
    return this.replace("<p></p>", "")
        .replace("<p><br></p>", "")
        .trim()
}
```

6.2. Autosave Edge Cases

Review AddNoteViewModel.onAutoSave():

Triggers:
□ Back navigation via back button
□ System back gesture
□ DisposableEffect cleanup in AddNoteScreen

Conditions for autosave:
□ hasUnsavedChanges = true (title, content, date, or pending media changed)
□ NOT both title AND content blank (empty notes should not save)

Edge cases:
□ User types content, then deletes all → content blank, should NOT autosave
□ User adds title only, no content → should autosave (title is sufficient)
□ User adds content only, no title → should autosave (content is sufficient)
□ User adds media only, no title/content → should autosave (media is sufficient)
□ User changes date only, no other changes → should autosave (date change is sufficient)
□ User opens note, makes no changes, exits → should NOT autosave

Check logic:
```kotlin
if (!state.hasUnsavedChanges) return
if (state.title.isBlank() && state.richTextState.toHtml().normalizeHtml().isBlank()) {
    // Empty note, do not save
    return
}
onSaveClicked()
```

6.3. Media Attachment Edge Cases

Review AddNoteViewModel media handling:

Pending media:
□ User selects image → added to pendingMedia list with URI
□ User removes pending image → removed from pendingMedia list
□ On save: Copy each pending image to note_media/ with `note_<noteId>_<timestamp>.jpg`
□ After save: Move from pendingMedia to savedMedia in UiState

Saved media:
□ Loaded from NoteMediaEntity on edit
□ Displayed in UI (thumbnails, no remove button)
□ Cannot be removed from AddNoteScreen (would require separate delete feature)

Edge cases:
□ User selects same image twice → should deduplicate or allow duplicates?
□ User selects image, removes, selects again → should work
□ User selects 10+ images → UI should handle (scrollable list)
□ Image file deleted from source after selection → URI invalid when copying → show error
□ Image copy fails (disk full, permission denied) → show error, do NOT save note
□ User exits without saving after selecting images → pending images NOT copied (no orphans created)

## 7. Biometric/Auth Edge Case Review

7.1. BiometricPrompt Edge Cases

Review MainActivity BiometricPrompt handling:

□ Device has no biometric hardware → fall back to device PIN/pattern/password
□ Device has biometric but none enrolled → fall back to device credential
□ User cancels biometric prompt → show retry button
□ User fails biometric 3 times → prompt device credential
□ BiometricPrompt not available (Android < API 28) → skip auth gate (not supported)

Check BiometricHelper.canAuthenticate():
- Returns true only if BIOMETRIC_STRONG or DEVICE_CREDENTIAL available
- If returns false, do NOT show AuthGate (proceed to Dashboard)

7.2. Device Lock Removal Edge Case

Review Settings biometric toggle and MainActivity:

Scenario:
1. User enables biometric lock (with device lock set)
2. User removes device lock from Android settings
3. User relaunches app

Expected behavior:
□ MainActivity onCreate() → checkDeviceSecurity() detects isDeviceSecure = false
□ If biometricEnabled = true in DataStore → show warning dialog
□ Dialog: "Device lock has been removed. Biometric lock has been automatically disabled for security."
□ On dismiss: Set biometricEnabled = false in DataStore
□ Do NOT show AuthGate (proceed to Dashboard)

Check this flow exists in MainActivity or SettingsViewModel.

## 8. Navigation Edge Case Review

8.1. Deep Link/Route Parameter Validation

Review all route parameters:

TakeAttendance(classId: Long, scheduleId: Long, date: String):
□ Validate classId > 0
□ Validate scheduleId > 0
□ Validate date matches yyyy-MM-dd (use try-catch on LocalDate.parse)
□ If invalid → show error toast, navigate back to Dashboard

EditClass(classId: Long):
□ Validate classId > 0
□ If class not found in DB → show error, navigate back

AddNote(date: String, noteId: Long):
□ Validate date matches yyyy-MM-dd
□ Validate noteId >= -1 (-1 for new, >= 0 for edit)
□ If noteId > 0 but note not found in DB → show error, navigate back to Dashboard

8.2. Back Stack Edge Cases

Review navigation back stack:

□ Splash → Setup → Dashboard (no back from Dashboard to Setup)
□ AuthGate → Dashboard (no back from Dashboard to AuthGate)
□ Dashboard → CreateClass → (back) → Dashboard
□ Dashboard → ManageClassList → EditClass → (back) → ManageClassList → (back) → Dashboard
□ Dashboard → TakeAttendance → (back with autosave) → Dashboard

Ensure:
- popBackStack() used correctly (not navigate())
- No back stack loops
- System back button behaves same as top bar back button

## 9. Accessibility Review

9.1. Content Descriptions

Review all icon buttons and images for contentDescription:

□ Back button: "Navigate back"
□ FAB menu button: "Open menu"
□ FAB menu close button: "Close menu"
□ Profile image: "Profile picture"
□ Calendar expand button: "Expand calendar"
□ Date picker button: "Select date"
□ Attendance toggle card: "Mark {studentName} as {present/absent}"
□ Remove pending media button: "Remove image"

Missing content descriptions will cause accessibility warnings.

9.2. Color Contrast

Review all text/background combinations for WCAG AA compliance:

□ Primary text on surface: Must have contrast ratio >= 4.5:1
□ Secondary text on surface: Must have contrast ratio >= 4.5:1
□ Button text on button background: Must have contrast ratio >= 4.5:1

Use Material Theme default colors (should pass automatically).

9.3. Touch Target Sizes

Review all interactive elements:

□ Buttons: Minimum 48dp height
□ Icon buttons: Minimum 48dp x 48dp
□ Toggle switches: Minimum 48dp touch target
□ Calendar date cells: Minimum 48dp x 48dp

Check for any small touch targets (< 48dp).

## 10. Performance Review

10.1. Database Query Optimization

Review DAOs for N+1 query problems:

□ Dashboard: Load scheduled classes with JOINs (not separate queries per class)
□ EditClass: Load students with JOIN (not separate queries per student)
□ TakeAttendance: Load students with JOIN (not separate queries)

Check for:
- Queries inside loops → refactor to single query with WHERE IN
- Missing @Transaction on multi-table queries
- Missing indexes on foreign keys

10.2. Image Loading Optimization

Review Coil usage:

□ Use `crossfade(true)` for smooth transitions
□ Use `placeholder()` for loading state
□ Use `error()` for failure state
□ Use `size()` to load appropriate resolution (don't load 4K image for 100dp thumbnail)

Check:
- Large images loaded without size constraints → memory issues
- No caching configured → reloads on every composition

10.3. Recomposition Optimization

Review Composable functions:

□ Use `remember` for expensive calculations
□ Use `derivedStateOf` for derived values
□ Use `key()` in lists to minimize recomposition
□ Avoid lambda recreations in Composable parameters

Scan for:
- `List.map {}` in Composable body → move to ViewModel
- Heavy calculations in Composable → move to ViewModel or remember

## Output Format

Return findings in the following structure:

### Critical Severity (P0 - Must Fix Before Release)
1. **[Category] Finding Title**
   - **File**: [path/to/file.kt](app/src/main/java/.../file.kt)
   - **Description**: Detailed description of the issue
   - **Impact**: What happens if not fixed (data loss, crash, security issue)
   - **Fix**: Concrete code change or approach to fix
   - **Example**:
     ```kotlin
     // Current (broken)
     fun saveClass(class: ClassDomain): Long { ... }

     // Fixed
     fun saveClass(class: ClassDomain): RepositoryResult<Long> {
         return try {
             RepositoryResult.Success(...)
         } catch (e: Exception) {
             RepositoryResult.Error(e.message)
         }
     }
     ```

### High Severity (P1 - Fix Before Launch)
[Same structure as Critical]

### Medium Severity (P2 - Should Fix)
[Same structure as Critical]

### Low Severity (P3 - Nice to Fix)
[Same structure as Critical]

### Residual Risks
Document any known issues that cannot be quickly fixed:
1. **Risk Description**: Scenario that could cause issues
2. **Likelihood**: Low/Medium/High
3. **Mitigation**: What can be done to reduce risk (even if not eliminated)
4. **Workaround**: How users can avoid the issue

### Testing Recommendations
List specific test cases that should be added:
1. **Test Name**: Brief description
2. **Type**: Unit / Integration / UI
3. **Priority**: Critical / High / Medium

Done Criteria:
- All Critical (P0) findings fixed
- All High (P1) findings fixed or documented as accepted risk
- No known crashes during normal workflows
- No known data loss scenarios

Device Gate Commands:
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell monkey -p com.uteacher.attendancetracker -c android.intent.category.LAUNCHER 1

Manual Verification:
1. Run through all edge cases listed above on physical device
2. Verify all fixes applied successfully
3. Test all high-risk flows (backup/restore, CSV import, attendance save)
4. Verify no regressions introduced by fixes
```

### Git Commit Message (Step 15)
`fix(step-15): apply review findings and harden edge cases before release`

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

## Prompt 16 - Daily Summary Timeline (Notes + Attendance)

```text
You are working in the existing Attenote Android project. Implement a new "Daily Summary" screen that shows notes and attendance together in a minimal newest-first list.

Scope:
1. Add a new typed route:
   - `AppRoute.DailySummary`
   - ActionBar title: `Daily Summary`
   - Back enabled.

2. Add dashboard ActionBar right-side icon navigation:
   - Show an icon button on Dashboard ActionBar right side.
   - Tapping icon navigates to `DailySummary`.
   - Keep existing dashboard branding in title area unchanged.

3. Build Daily Summary screen:
   - Minimal list UI.
   - Combine Notes and Attendance entries in one list.
   - Sort newest first.
   - Attendance item must show:
     - class name
     - students present count
     - students absent count
     - `Edit` action that opens existing attendance editor flow.
   - Note item must show:
     - title
     - one-line text preview
     - `Edit` action that opens existing note editor flow.

4. Data + ViewModel behavior:
   - Use existing repositories and typed navigation.
   - Add attendance observation support if needed for all sessions.
   - Keep implementation reactive with Flow + ViewModel state.
   - Handle empty state and load/error states.

5. Keep architecture and style constraints:
   - Compose + Material3 + Koin + Room.
   - No raw route strings.
   - Keep top ActionBar as global title/back surface.
   - Keep system bars/nav buttons visible.

Deliverables:
- Updated routes/policy/nav host.
- New summary screen + ui state + viewmodel.
- Dashboard ActionBar icon wiring.
- Any required DAO/repository additions.
- Buildable project.
```

### Git Commit Message (Step 16)
`feat(step-16): add daily summary timeline with notes and attendance edit shortcuts`

Device Gate Commands:
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell monkey -p com.uteacher.attendancetracker -c android.intent.category.LAUNCHER 1

Manual Verification:
1. Open Dashboard and verify right-side summary icon appears in ActionBar.
2. Tap icon and verify navigation to Daily Summary screen.
3. Verify list shows mixed notes and attendance newest first.
4. Verify attendance row shows class name, present count, absent count, and Edit action.
5. Verify note row shows title, one-line preview, and Edit action.
6. Tap Edit on attendance and verify Take Attendance opens with correct class/schedule/date.
7. Tap Edit on note and verify Add/Edit Note opens with correct note/date.
8. Verify back navigation from Daily Summary returns to Dashboard.
