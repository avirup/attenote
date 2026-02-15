# Code Review Report

**Date:** 2026-02-15
**Branch:** v2-features

---

## Overview

The project is a well-architected Jetpack Compose Android app using MVVM, Koin DI, Room, and DataStore. The codebase is clean with no TODO/FIXME comments, proper repository abstractions, and good separation of concerns. The issues below are organized by severity.

---

## CRITICAL Issues

### 1. Destructive Database Migration in Production

**File:** `di/AppModule.kt:24`

```kotlin
.fallbackToDestructiveMigration(dropAllTables = true)
```

This silently **deletes all user data** whenever the database schema changes. Users will lose attendance records, notes, classes, and students with no warning or recovery option.

**Fix:** Implement proper Room migration strategies. Reserve destructive fallback for debug builds only.

---

### 2. `runBlocking` in ViewModel.onCleared()

**File:** `ui/screen/attendance/TakeAttendanceViewModel.kt:272-281`

```kotlin
override fun onCleared() {
    lessonNoteAutoSaveJob?.cancel()
    val state = _uiState.value
    if (state.hasPendingChanges && canPersistAttendance(state)) {
        runBlocking {
            persistAttendance(state)
        }
    }
    super.onCleared()
}
```

Blocks the main thread during ViewModel destruction. If `persistAttendance` takes time (network/disk), this causes an ANR (Application Not Responding).

**Fix:** Use `NonCancellable` context with `applicationScope` or schedule via WorkManager. The auto-save mechanism (`scheduleLessonNoteAutoSave`) should handle most cases before `onCleared` is even reached.

---

### 3. Context Stored in ViewModel

**File:** `ui/screen/settings/SettingsViewModel.kt:28-33`

```kotlin
class SettingsViewModel(
    private val settingsRepository: SettingsPreferencesRepository,
    private val backupRepository: BackupSupportRepository,
    private val biometricHelper: BiometricHelper,
    private val context: Context  // <-- Potential memory leak
) : ViewModel()
```

If an Activity Context is injected, the Activity cannot be garbage collected while the ViewModel lives. ViewModels survive configuration changes and can outlive Activities.

**Fix:** Use `Application` context (`androidApplication()` in Koin) or pass Context only to individual functions that need it.

---

## HIGH Severity Issues

### 4. File I/O and Bitmap Processing on Main Thread

**File:** `ui/screen/settings/SettingsViewModel.kt:213+`

File operations (`mkdirs`, `FileOutputStream`, `BitmapFactory.decodeStream`, `Bitmap.compress`) run inside `viewModelScope.launch {}` without specifying `Dispatchers.IO`. The default dispatcher is `Dispatchers.Main`.

**Also affects:**
- `ui/screen/notes/AddNoteViewModel.kt` — image copy/compress operations

**Fix:** Wrap all file/bitmap operations in `withContext(Dispatchers.IO) { ... }`.

---

### 5. N+1 Query in DailySummaryViewModel

**File:** `ui/screen/dailysummary/DailySummaryViewModel.kt:107-137`

```kotlin
val attendanceIndexed = snapshot.sessions.map { session ->
    val records = attendanceRepository.getRecordsForSession(session.sessionId)
    // ...
}
```

Executes one database query per session. With 100 sessions, this issues 100 separate queries.

**Fix:** Add a batch DAO query: `getRecordsForSessions(sessionIds: List<Long>)` and fetch all records in a single query.

---

### 6. Inefficient In-Memory Duplicate Check

**File:** `data/repository/ClassRepositoryImpl.kt:215-229`

```kotlin
private suspend fun hasDuplicateIdentity(...): Boolean {
    val allClasses = classDao.observeAllClasses().first()  // Loads ALL classes
    return allClasses.any { existing -> ... }
}
```

Loads the entire class table into memory for a comparison that could be a targeted SQL query.

**Fix:** Write a DAO query with `WHERE` clauses matching the identity fields.

---

### 7. Shared Mutable State Without Synchronization

**File:** `ui/screen/dailysummary/DailySummaryViewModel.kt:35-37`

```kotlin
private var indexedEntries: List<SearchIndexedSummaryEntry> = emptyList()
private var requestedFilter: DailySummaryContentFilter = DailySummaryContentFilter.BOTH
private var notesOnlyModeEnabled: Boolean = false
```

These mutable properties are written from a `combine(...).collect {}` flow and read from other functions. Multiple concurrent emissions can cause race conditions.

**Fix:** Fold these into the single `_uiState` `MutableStateFlow` and use `.update {}` for atomic modifications.

---

### 8. Missing `flowOn(Dispatchers.IO)` on Database Queries

**File:** `data/repository/ClassRepositoryImpl.kt:215-229` (and other repositories)

```kotlin
val allClasses = classDao.observeAllClasses().first()
```

Room DAO `Flow` emissions happen on the query dispatcher, but `.first()` suspends on the caller's dispatcher. Without `flowOn(Dispatchers.IO)`, heavy query processing can block the main thread.

**Fix:** Add `.flowOn(Dispatchers.IO)` to all DAO Flow chains, or ensure callers are on IO.

---

## MEDIUM Severity Issues

### 9. Bitmap Not Recycled on Exception

**File:** `ui/screen/notes/AddNoteViewModel.kt:393-402`

```kotlin
val decodedBitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
    BitmapFactory.decodeStream(stream)
} ?: throw IllegalStateException(...)

FileOutputStream(target).use { output ->
    if (!decodedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)) {
        throw IllegalStateException("Failed to encode selected image")
    }
}
decodedBitmap.recycle()
```

If `compress()` throws or `FileOutputStream` fails, the bitmap is never recycled — leaking potentially several MB of native memory.

**Fix:** Wrap in `try { ... } finally { decodedBitmap.recycle() }`.

---

### 10. Missing List Keys Causing Inefficient Recomposition

**File:** `ui/screen/dashboard/DashboardScreen.kt:152-163, 204-211`

```kotlin
uiState.scheduledClasses.forEach { scheduledClass ->
    ScheduledClassCard(...)
}
```

`forEach` inside a `LazyColumn` does not provide stable keys. When list items change, Compose cannot identify which items moved/changed and recomposes everything.

**Fix:** Use `items(items = list, key = { it.id }) { ... }` instead.

---

### 11. Swallowed Exception Details

**File:** `ui/screen/viewnotesmedia/ViewNotesMediaViewModel.kt:54-106`

```kotlin
runCatching {
    noteRepository.observeAllNotes().collectLatest { ... }
}.onFailure { throwable ->
    _uiState.update {
        it.copy(error = "Failed to load notes viewer: ${throwable.message}")
    }
}
```

Only the message string is preserved. The exception type and stack trace are lost, making production debugging very difficult.

**Also affects:** `data/repository/ClassRepositoryImpl.kt:83-86` and other repositories.

**Fix:** Log the full exception with `Log.e(TAG, "...", throwable)` before updating UI state.

---

### 12. Guard Navigation During Composition

**File:** `ui/navigation/AppNavHost.kt:349-369`

```kotlin
private fun GuardNotesOnlyClassAttendanceRoute(...): Boolean {
    if (!notesOnlyModeEnabled) return false
    val guardedDestinationId = navController.currentBackStackEntry?.destination?.id
    LaunchedEffect(guardedDestinationId) {
        navController.navigate(AppRoute.Dashboard) { ... }
    }
    return true
}
```

`LaunchedEffect` inside a guard function that runs during composition can cause race conditions between rendering and navigation.

**Fix:** Move guard logic to ViewModel or navigation middleware, outside of composition.

---

### 13. CSV Parsing on Main Thread

**File:** `ui/screen/manageclass/EditClassViewModel.kt:474-480`

CSV parsing via `csvReader` is CPU-intensive but runs in `viewModelScope.launch` without specifying a dispatcher.

**Fix:** Wrap in `withContext(Dispatchers.Default)`.

---

### 14. Force Unwrap (!!) Without Adjacent Guard

**File:** `ui/screen/createclass/CreateClassViewModel.kt:332-333`

```kotlin
startDate = state.startDate!!,
endDate = state.endDate!!,
```

While there's a guard earlier in the function, the `!!` operators are several lines away from it. If the function is refactored, these become NPE risks.

**Fix:** Use `val startDate = state.startDate ?: return` immediately before usage.

---

### 15. Multiple Flow Collectors Without Cancellation

**File:** `ui/screen/dashboard/DashboardViewModel.kt:39-65`

```kotlin
private fun loadDashboardData() {
    viewModelScope.launch {
        combine(...).collect { ... }
    }
}
```

If `loadDashboardData()` is ever called more than once, previous collectors are not cancelled, leading to duplicate database listeners.

**Fix:** Store the Job and cancel it before relaunching: `loadJob?.cancel(); loadJob = viewModelScope.launch { ... }`.

---

## LOW Severity Issues

### 16. Unused Function

**File:** `MainActivity.kt:346-359`

```kotlin
private fun createDashboardWordmarkView(): ImageView { ... }
```

This function is defined but never called anywhere in the codebase.

**Fix:** Delete it.

---

### 17. Duplicate Biometric Prompt Logic

**File 1:** `MainActivity.kt:210-236` — `showBiometricPrompt()`
**File 2:** `ui/screen/auth/AuthGateScreen.kt:95-129` — `showBiometricPrompt()`

Nearly identical biometric prompt creation logic is implemented in two places.

**Fix:** Extract into a shared utility function (e.g., `BiometricHelper.showPrompt()`).

---

### 18. Hardcoded Day Names (Not Locale-Aware)

**File:** `ui/screen/dashboard/components/MonthCalendarView.kt:78`

```kotlin
listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { dayName ->
```

These English abbreviations won't adapt to user locale.

**Fix:** Use `DayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())`.

---

### 19. Duplicate String Literals

Common UI strings are hardcoded repeatedly instead of using string resources:

| String | Occurrences | Files |
|--------|-------------|-------|
| "Cancel" | ~16 | SettingsScreen, EditClassScreen, CsvImportDialog, AddStudentDialog, ManageStudentsScreen, AddNoteScreen, AttenoteComponents |
| "Save" | ~9 | SettingsScreen, SetupScreen, TakeAttendanceScreen, CreateClassScreen, AddNoteScreen |
| "Delete" | ~6 | Multiple screens and dialogs |

**Fix:** Move to `strings.xml` or a shared constants object for i18n readiness.

---

### 20. Regex on Every Recomposition

**File:** `ui/screen/dashboard/components/NoteCard.kt:80`

```kotlin
val stripped = content.replace(Regex("<[^>]*>"), "").trim()
```

A new `Regex` object is compiled on every recomposition of every NoteCard.

**Fix:** Extract regex to a companion object `val HTML_TAG_REGEX = Regex("<[^>]*>")` or memoize with `remember`.

---

### 21. `mkdirs()` Return Value Ignored

**File:** `ui/screen/notes/AddNoteViewModel.kt:385-387`

```kotlin
val mediaDir = File(context.filesDir, "note_media")
if (!mediaDir.exists()) {
    mediaDir.mkdirs()  // Return value not checked
}
```

If directory creation fails (permissions, disk full), downstream operations fail with a confusing error.

**Fix:** Check return value and surface an appropriate error.

---

### 22. TakeAttendanceViewModel Is Too Large

**File:** `ui/screen/attendance/TakeAttendanceViewModel.kt` — 436 lines

This ViewModel handles loading, state management, persistence, and auto-save logic. Multiple responsibilities make it harder to test and maintain.

**Fix:** Extract auto-save and persistence logic into a separate `AttendancePersistenceManager` class.

---

### 23. MainActivity Is Too Large

**File:** `MainActivity.kt` — 441 lines

Handles action bar customization, biometric auth, navigation setup, and menu configuration in one class.

**Fix:** Extract action bar management into a dedicated `ActionBarManager` class.

---

## Summary

| Severity | Count | Key Areas |
|----------|-------|-----------|
| CRITICAL | 3 | Destructive DB migration, main thread blocking, memory leak |
| HIGH | 5 | I/O threading, N+1 queries, race conditions |
| MEDIUM | 7 | Resource leaks, recomposition, error handling, navigation |
| LOW | 8 | Dead code, duplicates, locale, code organization |

### Strengths

- Clean MVVM architecture with proper repository abstractions
- Excellent DI setup with Koin — no manual instantiation
- All colors, typography, and shapes centralized in theme
- Comprehensive input validation in ViewModels
- Good use of sealed `RepositoryResult` for error propagation
- No sensitive data logging found
- No TODO/FIXME technical debt comments
- Proper biometric authentication implementation
