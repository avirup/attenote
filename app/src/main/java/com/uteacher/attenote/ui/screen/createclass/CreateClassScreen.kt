package com.uteacher.attenote.ui.screen.createclass

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.uteacher.attenote.ui.navigation.ActionBarPrimaryAction
import com.uteacher.attenote.ui.components.AttenoteDatePickerDialog
import com.uteacher.attenote.ui.components.AttenoteTimePickerDialog
import com.uteacher.attenote.ui.screen.createclass.components.ScheduleSlotCard
import com.uteacher.attenote.ui.theme.component.AttenoteButton
import com.uteacher.attenote.ui.theme.component.AttenoteSectionCard
import com.uteacher.attenote.ui.theme.component.AttenoteTextField
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import org.koin.androidx.compose.koinViewModel

private val TimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
private val DateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
private val SemesterOptions = listOf("1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th")

@Composable
fun CreateClassScreen(
    onNavigateBack: () -> Unit,
    onSetActionBarPrimaryAction: (ActionBarPrimaryAction?) -> Unit,
    viewModel: CreateClassViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var semesterMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var dayMenuExpanded by rememberSaveable { mutableStateOf(false) }
    val onSaveClick = remember(viewModel) { { viewModel.onSaveClicked() } }
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onNavigateBack()
        }
    }

    SideEffect {
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            onSetActionBarPrimaryAction(
                ActionBarPrimaryAction(
                    title = "Save",
                    enabled = !uiState.isLoading,
                    onClick = onSaveClick
                )
            )
        }
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onSetActionBarPrimaryAction(
                    ActionBarPrimaryAction(
                        title = "Save",
                        enabled = true,
                        onClick = onSaveClick
                    )
                )
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            onSetActionBarPrimaryAction(null)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AttenoteSectionCard(title = "Class Information") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    AttenoteTextField(
                        value = uiState.instituteName,
                        onValueChange = viewModel::onInstituteChanged,
                        label = "Institute Name *",
                        enabled = !uiState.isLoading,
                        errorMessage = uiState.instituteError
                    )
                    Text(
                        text = "Institute is auto-filled from settings and can be edited",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    AttenoteTextField(
                        value = uiState.session,
                        onValueChange = viewModel::onSessionChanged,
                        label = "Session *",
                        enabled = !uiState.isLoading,
                        errorMessage = uiState.sessionError
                    )
                    Text(
                        text = "Session is auto-filled from settings and can be edited",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    AttenoteTextField(
                        value = uiState.department,
                        onValueChange = viewModel::onDepartmentChanged,
                        label = "Department *",
                        enabled = !uiState.isLoading,
                        errorMessage = uiState.departmentError
                    )

                    Box {
                        PickerTextField(
                            value = if (uiState.semester.isBlank()) "" else uiState.semester,
                            label = "Semester *",
                            placeholder = "Select Semester",
                            enabled = !uiState.isLoading,
                            errorMessage = uiState.semesterError,
                            onClick = { semesterMenuExpanded = true },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.ArrowDropDown,
                                    contentDescription = "Select semester"
                                )
                            }
                        )
                        DropdownMenu(
                            expanded = semesterMenuExpanded,
                            onDismissRequest = { semesterMenuExpanded = false }
                        ) {
                            SemesterOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        viewModel.onSemesterChanged(option)
                                        semesterMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    AttenoteTextField(
                        value = uiState.section,
                        onValueChange = viewModel::onSectionChanged,
                        label = "Section (optional)",
                        enabled = !uiState.isLoading
                    )

                    AttenoteTextField(
                        value = uiState.subject,
                        onValueChange = viewModel::onSubjectChanged,
                        label = "Subject *",
                        enabled = !uiState.isLoading,
                        errorMessage = uiState.subjectError
                    )

                    AttenoteTextField(
                        value = uiState.className,
                        onValueChange = viewModel::onClassNameChanged,
                        label = "Class Name *",
                        enabled = !uiState.isLoading,
                        errorMessage = uiState.classNameError
                    )
                }
            }

            AttenoteSectionCard(title = "Date Range") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PickerTextField(
                            value = uiState.startDate?.let(::formatDate) ?: "",
                            label = "Start Date *",
                            placeholder = "Pick date",
                            enabled = !uiState.isLoading,
                            modifier = Modifier.weight(1f),
                            onClick = viewModel::onStartDatePickerRequested,
                            trailingIcon = {
                                Text(text = "\uD83D\uDCC5")
                            }
                        )

                        PickerTextField(
                            value = uiState.endDate?.let(::formatDate) ?: "",
                            label = "End Date *",
                            placeholder = "Pick date",
                            enabled = !uiState.isLoading,
                            modifier = Modifier.weight(1f),
                            onClick = viewModel::onEndDatePickerRequested,
                            trailingIcon = {
                                Text(text = "\uD83D\uDCC5")
                            }
                        )
                    }

                    if (!uiState.dateRangeError.isNullOrBlank()) {
                        Text(
                            text = uiState.dateRangeError!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            AttenoteSectionCard(title = "Weekly Schedule") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Add Schedule Slot",
                        style = MaterialTheme.typography.labelLarge
                    )

                    Box {
                        PickerTextField(
                            value = uiState.currentSlot.dayOfWeek.getDisplayName(
                                TextStyle.FULL,
                                Locale.getDefault()
                            ),
                            label = "Day",
                            enabled = !uiState.isLoading,
                            onClick = { dayMenuExpanded = true },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.ArrowDropDown,
                                    contentDescription = "Select day"
                                )
                            }
                        )
                        DropdownMenu(
                            expanded = dayMenuExpanded,
                            onDismissRequest = { dayMenuExpanded = false }
                        ) {
                            DayOfWeek.entries.forEach { day ->
                                DropdownMenuItem(
                                    text = {
                                        Text(day.getDisplayName(TextStyle.FULL, Locale.getDefault()))
                                    },
                                    onClick = {
                                        viewModel.onSlotDayChanged(day)
                                        dayMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PickerTextField(
                            value = uiState.currentSlot.startTime.format(TimeFormatter),
                            label = "Start Time",
                            enabled = !uiState.isLoading,
                            modifier = Modifier.weight(1f),
                            onClick = viewModel::onSlotStartTimePickerRequested,
                            trailingIcon = {
                                Text(text = "\u23F0")
                            }
                        )

                        PickerTextField(
                            value = uiState.currentSlot.endTime.format(TimeFormatter),
                            label = "End Time",
                            enabled = !uiState.isLoading,
                            modifier = Modifier.weight(1f),
                            onClick = viewModel::onSlotEndTimePickerRequested,
                            trailingIcon = {
                                Text(text = "\u23F0")
                            }
                        )
                    }

                    if (!uiState.currentSlot.validationError.isNullOrBlank()) {
                        Text(
                            text = uiState.currentSlot.validationError!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    AttenoteButton(
                        text = "Add Slot",
                        onClick = viewModel::onAddScheduleSlot,
                        enabled = !uiState.isLoading
                    )

                    HorizontalDivider()

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

                    if (!uiState.schedulesError.isNullOrBlank()) {
                        Text(
                            text = uiState.schedulesError!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            if (!uiState.saveError.isNullOrBlank()) {
                Text(
                    text = uiState.saveError!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    if (uiState.showDatePicker) {
        val initialDate = remember(uiState.datePickerTarget, uiState.startDate, uiState.endDate) {
            when (uiState.datePickerTarget) {
                DatePickerTarget.START_DATE -> uiState.startDate
                DatePickerTarget.END_DATE -> uiState.endDate
                null -> null
            }
        }
        AttenoteDatePickerDialog(
            initialDate = initialDate,
            onDateSelected = viewModel::onDateSelected,
            onDismiss = viewModel::onDatePickerDismissed
        )
    }

    if (uiState.showTimePicker) {
        val initialTime = when (uiState.timePickerTarget) {
            TimePickerTarget.SLOT_START -> uiState.currentSlot.startTime
            TimePickerTarget.SLOT_END -> uiState.currentSlot.endTime
            null -> LocalTime.now()
        }
        AttenoteTimePickerDialog(
            initialTime = initialTime,
            onTimeSelected = viewModel::onTimeSelected,
            onDismiss = viewModel::onTimePickerDismissed
        )
    }
}

@Composable
private fun PickerTextField(
    value: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    placeholder: String? = null,
    errorMessage: String? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelMedium,
            color = if (!errorMessage.isNullOrBlank()) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 56.dp)
                .clickable(enabled = enabled, onClick = onClick),
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (!errorMessage.isNullOrBlank()) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.outline
                }
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (value.isBlank() && !placeholder.isNullOrBlank()) placeholder else value,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Start,
                    color = if (value.isBlank()) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                if (trailingIcon != null) {
                    Box(modifier = Modifier.padding(start = 8.dp)) {
                        trailingIcon()
                    }
                }
            }
        }
        if (!errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun formatDate(date: LocalDate): String = date.format(DateFormatter)
