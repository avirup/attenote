# UI Consistency Audit Report

**Date:** 2026-02-15
**Branch:** v2-features

---

## What's Done Well

### Colors — Excellent
All colors are 100% centralized in `app/src/main/java/com/uteacher/attenote/ui/theme/Color.kt`. No hardcoded hex values found anywhere in screen or component files. All screens access colors via `MaterialTheme.colorScheme.*`.

### Typography — Excellent
All text uses `MaterialTheme.typography.*` consistently. Zero hardcoded font sizes found. Typography is centrally defined in `app/src/main/java/com/uteacher/attenote/ui/theme/Type.kt`.

### Shapes — Good
Centrally defined in `app/src/main/java/com/uteacher/attenote/ui/theme/Shape.kt` with a clear scale:
- extraSmall: 6dp
- small: 12dp
- medium: 20dp
- large: 24dp
- extraLarge: 32dp

### Shared Component Library — Good
Well-built component library in `app/src/main/java/com/uteacher/attenote/ui/theme/component/AttenoteComponents.kt` providing:
- `AttenoteTopAppBar`, `AttenoteButton`, `AttenoteSecondaryButton`
- `AttenoteTextField`, `AttenoteSectionCard`, `AttenoteFloatingActionButton`
- `AttenoteDialog`, `AttenoteDatePicker`, `AttenoteTimePicker`, `AttenoteLoadingIndicator`

---

## Issues Found

### 1. Card Padding is Inconsistent — HIGH

Cards use 4 different padding values with no clear reasoning:

| Card | Padding | File |
|------|---------|------|
| NoteCard | `16.dp` | `ui/screen/dashboard/components/NoteCard.kt:42` |
| ScheduledClassCard | `16.dp` | `ui/screen/dashboard/components/ScheduledClassCard.kt:44` |
| AttendanceRecordCard | `16.dp` | `ui/screen/attendance/components/AttendanceRecordCard.kt:56` |
| ClassCard | `10.dp h, 8.dp v` | `ui/screen/manageclass/components/ClassCard.kt:42` |
| StudentCard | `10.dp h, 8.dp v` | `ui/screen/managestudents/components/StudentCard.kt:36` |
| StudentRosterCard | `12.dp` | `ui/screen/manageclass/components/StudentRosterCard.kt:34` |
| ScheduleSlotCard | `12.dp` | `ui/screen/createclass/components/ScheduleSlotCard.kt:44` |

**Recommendation:** Standardize all card padding to `16.dp`.

---

### 2. Screen-Level Content Padding Varies — HIGH

| Screen | Padding | File |
|--------|---------|------|
| DashboardScreen | `16.dp` | `ui/screen/dashboard/DashboardScreen.kt:123` |
| ViewNotesMediaScreen | `16.dp` | `ui/screen/viewnotesmedia/ViewNotesMediaScreen.kt:66` |
| ManageClassListScreen | `10.dp` | `ui/screen/manageclass/ManageClassListScreen.kt:61` |
| DailySummaryScreen | `12.dp h, 10.dp v` | `ui/screen/dailysummary/DailySummaryScreen.kt:42` |

**Recommendation:** Standardize screen content padding to `16.dp`.

---

### 3. Card Elevation Inconsistency — MEDIUM

Some cards have explicit `6.dp` elevation, others rely on defaults:

| Elevation | Cards |
|-----------|-------|
| Explicit `6.dp` | NoteCard, ScheduledClassCard, ClassCard, StudentCard |
| No explicit elevation | AttendanceRecordCard, StudentRosterCard, ScheduleSlotCard |

**Recommendation:** Set `defaultElevation = 6.dp` on all cards.

---

### 4. Card Container Color Split — MEDIUM

- **`surface`**: NoteCard, ScheduledClassCard, ClassCard, AttendanceRecordCard
- **`surfaceVariant`**: StudentRosterCard, ScheduleSlotCard

No clear design rationale for the difference.

**Recommendation:** Use `surface` for all primary cards; reserve `surfaceVariant` for nested or secondary elements only.

---

### 5. List Item Spacing Varies — MEDIUM

`LazyColumn` spacing differs across screens:

| Screen | Item Spacing |
|--------|-------------|
| DashboardScreen | `spacedBy(16.dp)` |
| DailySummaryScreen | `spacedBy(8.dp)` |
| ManageClassListScreen | `spacedBy(6.dp)` |

Inner card text spacing also varies:
- `2.dp`: StudentCard, StudentRosterCard
- `4.dp`: ClassCard, ScheduledClassCard, AttendanceRecordCard

**Recommendation:** Standardize list spacing to `12.dp` or `16.dp`; inner card text spacing to `4.dp`.

---

### 6. Hardcoded Corner Radii Bypass Theme — MEDIUM

| Component | Value | Should Be |
|-----------|-------|-----------|
| HamburgerFabMenu label background (lines 131, 148) | `RoundedCornerShape(4.dp)` | `MaterialTheme.shapes.extraSmall` (6.dp) |
| MediaThumbnail clip | `RoundedCornerShape(8.dp)` | `MaterialTheme.shapes.small` (12.dp) |

**File:** `ui/screen/dashboard/components/HamburgerFabMenu.kt`
**File:** `ui/screen/notes/components/MediaThumbnail.kt`

**Recommendation:** Replace hardcoded shapes with `MaterialTheme.shapes.*`.

---

### 7. Custom Components Defined but Not Used Everywhere — MEDIUM

`AttenoteButton` and `AttenoteTextField` exist but several screens bypass them:

| Location | Uses | Should Use |
|----------|------|------------|
| `ScheduledClassCard.kt:75` | Raw `Button()` | `AttenoteButton` |
| `ManageStudentsScreen.kt:59` | Raw `OutlinedTextField()` | `AttenoteTextField` |
| `EditClassScreen.kt` | Raw `OutlinedTextField()` | `AttenoteTextField` |
| `AddStudentDialog.kt` | Raw `AlertDialog` | `AttenoteDialog` |
| `CreateClassScreen.kt` | Raw `Button()` / `OutlinedButton()` | `AttenoteButton` / `AttenoteSecondaryButton` |
| `AddNoteScreen.kt` | Raw `Button()` | `AttenoteButton` |

**Recommendation:** Replace raw Material components with project wrappers for consistency.

---

### 8. Magic Numbers in DashboardScreen — LOW

`ui/screen/dashboard/DashboardScreen.kt` lines 72–75 contain hardcoded layout constants:

```kotlin
val fabSwipeThreshold = 24.dp
val calendarSwipeThreshold = 32.dp
val contentBottomPadding = if (uiState.calendarExpanded) 408.dp else 228.dp
val snackbarBottomPadding = if (uiState.calendarExpanded) 360.dp else 196.dp
```

Also at line 296: drag offset bounds `(-72f, 72f)`.

**Recommendation:** Extract to named constants in a companion object or dimensions file.

---

## Summary

| Category | Rating |
|----------|--------|
| Colors | Excellent |
| Typography | Excellent |
| Shapes | Good (2 bypasses) |
| Card Padding | Inconsistent |
| Screen Padding | Inconsistent |
| Elevation | Partially Inconsistent |
| Component Reuse | Partially Inconsistent |
| Spacing Scale | No formal scale defined |

**Overall:** The theme system is well-built. The main gap is inconsistent application of spacing, padding, and elevation across cards and screens, plus some components not using the shared wrappers.
