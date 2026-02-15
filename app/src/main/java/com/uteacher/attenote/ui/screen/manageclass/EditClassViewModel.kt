package com.uteacher.attenote.ui.screen.manageclass

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.uteacher.attenote.data.repository.AttendanceRepository
import com.uteacher.attenote.data.repository.ClassRepository
import com.uteacher.attenote.data.repository.StudentRepository
import com.uteacher.attenote.data.repository.internal.RepositoryResult
import com.uteacher.attenote.domain.model.Student
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditClassViewModel(
    private val classId: Long,
    private val classRepository: ClassRepository,
    private val studentRepository: StudentRepository,
    private val attendanceRepository: AttendanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditClassUiState())
    val uiState: StateFlow<EditClassUiState> = _uiState.asStateFlow()

    init {
        if (classId <= 0L) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    classNotFoundMessage = "Invalid class route parameters"
                )
            }
        } else {
            loadClassData()
        }
    }

    private fun loadClassData() {
        viewModelScope.launch {
            val classItem = classRepository.getClassById(classId)
            if (classItem == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        classNotFoundMessage = "Class not found"
                    )
                }
                return@launch
            }

            _uiState.update {
                it.copy(
                    classItem = classItem,
                    startDate = classItem.startDate,
                    endDate = classItem.endDate,
                    isLoading = true,
                    saveError = null,
                    classNotFoundMessage = null,
                    shouldNavigateBack = false
                )
            }

            combine(
                studentRepository.observeStudentsForClass(classId),
                studentRepository.observeAllStudents(),
                classRepository.observeAllClasses()
            ) { roster, allStudents, allClasses ->
                Triple(roster, allStudents, allClasses)
            }
                .catch { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            saveError = "Failed to load class: ${throwable.message}"
                        )
                    }
                }
                .collect { (roster, allStudents, allClasses) ->
                    _uiState.update { state ->
                        val classExists = allClasses.any { it.classId == classId }
                        if (!classExists) {
                            state.copy(
                                classItem = null,
                                isLoading = false,
                                showDeleteClassConfirmation = false,
                                classNotFoundMessage = "Class not found",
                                shouldNavigateBack = true
                            )
                        } else {
                            state.copy(
                                students = roster.map { studentWithStatus ->
                                    StudentInClass(
                                        student = studentWithStatus.student,
                                        isActiveInClass = studentWithStatus.isActiveInClass
                                    )
                                },
                                allStudents = allStudents,
                                availableClasses = allClasses.filter { it.classId != classId },
                                isLoading = false
                            )
                        }
                    }
                }
        }
    }

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
                    outOfRangeWarning = null,
                    saveError = null
                )

                DatePickerTarget.END_DATE -> state.copy(
                    endDate = date,
                    showDatePicker = false,
                    datePickerTarget = null,
                    dateRangeError = null,
                    outOfRangeWarning = null,
                    saveError = null
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
        if (state.isSaving) return

        val startDate = state.startDate
        val endDate = state.endDate
        if (startDate == null || endDate == null) {
            _uiState.update { it.copy(dateRangeError = "Both dates are required") }
            return
        }
        if (startDate.isAfter(endDate)) {
            _uiState.update {
                it.copy(dateRangeError = "Start date must be before or equal to end date")
            }
            return
        }

        _uiState.update {
            it.copy(
                isSaving = true,
                dateRangeError = null,
                saveError = null,
                operationMessage = null
            )
        }

        viewModelScope.launch {
            val outOfRangeCount = attendanceRepository.countSessionsOutsideDateRange(
                classId = classId,
                startDate = startDate,
                endDate = endDate
            )

            when (val result = classRepository.updateClassDateRange(classId, startDate, endDate)) {
                is RepositoryResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            classItem = it.classItem?.copy(
                                startDate = startDate,
                                endDate = endDate
                            ),
                            startDate = startDate,
                            endDate = endDate,
                            outOfRangeWarning = if (outOfRangeCount > 0) {
                                "$outOfRangeCount attendance records fall outside new date range"
                            } else {
                                null
                            },
                            operationMessage = "Date range updated"
                        )
                    }
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
        }
    }

    fun onToggleStudentActiveInClass(studentId: Long, isActive: Boolean) {
        viewModelScope.launch {
            when (val result = studentRepository.updateStudentActiveInClass(classId, studentId, isActive)) {
                is RepositoryResult.Success -> Unit
                is RepositoryResult.Error -> {
                    _uiState.update { it.copy(saveError = result.message) }
                }
            }
        }
    }

    fun onShowAddStudentDialog() {
        _uiState.update { it.copy(showAddStudentDialog = true, saveError = null) }
    }

    fun onDismissAddStudentDialog() {
        _uiState.update { it.copy(showAddStudentDialog = false) }
    }

    fun onShowCsvImportDialog() {
        _uiState.update { it.copy(showCsvImportDialog = true, csvImportError = null) }
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

    fun onDeleteClassRequested() {
        val state = _uiState.value
        if (state.classItem == null || state.isSaving) {
            return
        }
        _uiState.update { it.copy(showDeleteClassConfirmation = true, saveError = null) }
    }

    fun onDismissDeleteClassDialog() {
        _uiState.update { it.copy(showDeleteClassConfirmation = false) }
    }

    fun onConfirmDeleteClass() {
        val state = _uiState.value
        val targetClassId = state.classItem?.classId ?: return
        if (state.isSaving) return

        _uiState.update {
            it.copy(
                isSaving = true,
                showDeleteClassConfirmation = false,
                saveError = null
            )
        }

        viewModelScope.launch {
            when (val result = classRepository.deleteClassPermanently(targetClassId)) {
                is RepositoryResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            shouldNavigateBack = true
                        )
                    }
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
        }
    }

    fun onNavigationHandled() {
        _uiState.update { it.copy(shouldNavigateBack = false) }
    }

    fun onAddStudent(name: String, regNumber: String, rollNumber: String?) {
        val normalizedName = normalizeText(name)
        val normalizedRegNumber = normalizeText(regNumber)
        val normalizedRoll = normalizeOptional(rollNumber)

        if (normalizedName.isBlank()) {
            _uiState.update { it.copy(saveError = "Name is required") }
            return
        }

        if (normalizedRegNumber.isBlank()) {
            _uiState.update { it.copy(saveError = "Registration number is required") }
            return
        }

        viewModelScope.launch {
            val existingStudent = studentRepository.findStudentByNameAndRegistration(
                name = normalizedName,
                registrationNumber = normalizedRegNumber
            )

            if (existingStudent != null) {
                when (val linkResult = studentRepository.addStudentToClass(classId, existingStudent.studentId)) {
                    is RepositoryResult.Success -> {
                        studentRepository.updateStudentActiveInClass(
                            classId = classId,
                            studentId = existingStudent.studentId,
                            isActive = true
                        )
                        _uiState.update {
                            it.copy(
                                showAddStudentDialog = false,
                                saveError = null,
                                operationMessage = "Existing student linked to class"
                            )
                        }
                    }

                    is RepositoryResult.Error -> {
                        _uiState.update { it.copy(saveError = linkResult.message) }
                    }
                }
                return@launch
            }

            val student = Student(
                studentId = 0L,
                name = normalizedName,
                registrationNumber = normalizedRegNumber,
                rollNumber = normalizedRoll,
                email = null,
                phone = null,
                isActive = true,
                createdAt = LocalDate.now()
            )

            when (val result = studentRepository.createStudent(student)) {
                is RepositoryResult.Success -> {
                    when (val linkResult = studentRepository.addStudentToClass(classId, result.data)) {
                        is RepositoryResult.Success -> {
                            _uiState.update {
                                it.copy(
                                    showAddStudentDialog = false,
                                    saveError = null,
                                    operationMessage = "Student added to roster"
                                )
                            }
                        }

                        is RepositoryResult.Error -> {
                            _uiState.update { it.copy(saveError = linkResult.message) }
                        }
                    }
                }

                is RepositoryResult.Error -> {
                    _uiState.update { it.copy(saveError = result.message) }
                }
            }
        }
    }

    fun onCsvFileSelected(csvContent: String) {
        viewModelScope.launch {
            try {
                val rows = parseCsvPreviewRows(csvContent)
                if (rows.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            csvPreviewData = emptyList(),
                            csvImportError = "No CSV rows found"
                        )
                    }
                    return@launch
                }

                _uiState.update {
                    it.copy(
                        csvPreviewData = rows,
                        csvImportError = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        csvPreviewData = emptyList(),
                        csvImportError = "Failed to parse CSV: ${e.message}"
                    )
                }
            }
        }
    }

    fun onToggleCsvRowSelection(index: Int, selectedForImport: Boolean) {
        _uiState.update { state ->
            if (index !in state.csvPreviewData.indices) {
                return@update state
            }
            val updatedRows = state.csvPreviewData.mapIndexed { rowIndex, row ->
                if (rowIndex == index && row.canImport) {
                    row.copy(selectedForImport = selectedForImport)
                } else {
                    row
                }
            }
            state.copy(csvPreviewData = updatedRows)
        }
    }

    fun onSelectAllCsvRows() {
        _uiState.update { state ->
            state.copy(
                csvPreviewData = state.csvPreviewData.map { row ->
                    if (row.canImport) row.copy(selectedForImport = true) else row
                }
            )
        }
    }

    fun onDeselectAllCsvRows() {
        _uiState.update { state ->
            state.copy(
                csvPreviewData = state.csvPreviewData.map { row ->
                    row.copy(selectedForImport = false)
                }
            )
        }
    }

    private fun parseCsvPreviewRows(content: String): List<CsvStudentRow> {
        val csvData = csvReader {
            charset = "UTF-8"
            quoteChar = '"'
            delimiter = ','
            escapeChar = '\\'
        }.readAllWithHeader(content.byteInputStream())

        val parsedRows = csvData.mapIndexed { index, row ->
            val name = normalizeOptional(row.readValue(CSV_NAME_HEADER_ALIASES))
            val registrationNumber = normalizeOptional(row.readValue(CSV_REGISTRATION_HEADER_ALIASES))
            val department = normalizeOptional(row.readValue(CSV_DEPARTMENT_HEADER_ALIASES))
            val rollNumber = normalizeOptional(row.readValue(CSV_ROLL_HEADER_ALIASES))
            val email = normalizeOptional(row.readValue(CSV_EMAIL_HEADER_ALIASES))
            val phone = normalizeOptional(row.readValue(CSV_PHONE_HEADER_ALIASES))

            ParsedCsvStudentRow(
                sourceRowNumber = index + 2,
                name = name,
                registrationNumber = registrationNumber,
                department = department,
                rollNumber = rollNumber,
                email = email,
                phone = phone
            )
        }

        val deduplicatedRows = mutableListOf<ParsedCsvStudentRow>()
        val seenIdentityKeys = linkedSetOf<String>()

        parsedRows.forEach { row ->
            val identityKey = buildIdentityKey(row.name, row.registrationNumber)
            if (identityKey == null) {
                deduplicatedRows += row
                return@forEach
            }
            if (seenIdentityKeys.add(identityKey)) {
                deduplicatedRows += row
            }
        }

        return deduplicatedRows.map { row ->
            val matchedExisting = findExistingStudent(row.name, row.registrationNumber)
            val canImport = !row.name.isNullOrBlank() && !row.registrationNumber.isNullOrBlank()
            CsvStudentRow(
                sourceRowNumber = row.sourceRowNumber,
                name = row.name,
                registrationNumber = row.registrationNumber,
                department = row.department,
                rollNumber = row.rollNumber,
                email = row.email,
                phone = row.phone,
                matchedExistingStudentId = matchedExisting?.studentId,
                matchedExistingStudentInactive = matchedExisting?.isActive == false,
                canImport = canImport,
                selectedForImport = canImport,
                eligibilityMessage = when {
                    row.name.isNullOrBlank() && row.registrationNumber.isNullOrBlank() -> {
                        "Missing required fields: name and registration number"
                    }

                    row.name.isNullOrBlank() -> "Missing required field: name"
                    row.registrationNumber.isNullOrBlank() -> "Missing required field: registration number"
                    else -> null
                }
            )
        }
    }

    fun onConfirmCsvImport() {
        val state = _uiState.value
        val rows = state.csvPreviewData.filter { it.canImport && it.selectedForImport }
        if (rows.isEmpty()) {
            _uiState.update {
                it.copy(csvImportError = "No import-eligible rows selected")
            }
            return
        }

        viewModelScope.launch {
            var linkedExistingCount = 0
            var createdNewCount = 0
            var skippedCount = 0
            val rejectedCount = state.csvPreviewData.count { it.canImport && !it.selectedForImport }

            rows.forEach { row ->
                val name = row.name ?: run {
                    skippedCount += 1
                    return@forEach
                }
                val registrationNumber = row.registrationNumber ?: run {
                    skippedCount += 1
                    return@forEach
                }

                val existing = studentRepository.findStudentByNameAndRegistration(
                    name = name,
                    registrationNumber = registrationNumber
                )

                if (existing != null) {
                    studentRepository.addStudentToClass(classId, existing.studentId)
                    if (!existing.isActive) {
                        studentRepository.updateStudentActiveInClass(
                            classId = classId,
                            studentId = existing.studentId,
                            isActive = false
                        )
                    }
                    linkedExistingCount += 1
                } else {
                    val createResult = studentRepository.createStudent(
                        Student(
                            studentId = 0L,
                            name = name,
                            registrationNumber = registrationNumber,
                            rollNumber = row.rollNumber,
                            email = row.email,
                            phone = row.phone,
                            department = row.department.orEmpty(),
                            isActive = true,
                            createdAt = LocalDate.now()
                        )
                    )

                    when (createResult) {
                        is RepositoryResult.Success -> {
                            studentRepository.addStudentToClass(classId, createResult.data)
                            createdNewCount += 1
                        }

                        is RepositoryResult.Error -> {
                            val existingAfterFailure = studentRepository.findStudentByNameAndRegistration(
                                name = name,
                                registrationNumber = registrationNumber
                            )
                            if (existingAfterFailure != null) {
                                studentRepository.addStudentToClass(classId, existingAfterFailure.studentId)
                                if (!existingAfterFailure.isActive) {
                                    studentRepository.updateStudentActiveInClass(
                                        classId = classId,
                                        studentId = existingAfterFailure.studentId,
                                        isActive = false
                                    )
                                }
                                linkedExistingCount += 1
                            } else {
                                skippedCount += 1
                            }
                        }
                    }
                }
            }

            val message = buildString {
                append("CSV import completed")
                append(" • Linked existing: $linkedExistingCount")
                append(" • Created new: $createdNewCount")
                if (rejectedCount > 0) {
                    append(" • Rejected: $rejectedCount")
                }
                if (skippedCount > 0) {
                    append(" • Skipped: $skippedCount")
                }
            }

            _uiState.update {
                it.copy(
                    showCsvImportDialog = false,
                    csvPreviewData = emptyList(),
                    csvImportError = null,
                    operationMessage = message
                )
            }
        }
    }

    fun onCopyFromClass(sourceClassId: Long) {
        viewModelScope.launch {
            val sourceStudents = studentRepository.getStudentsForClass(sourceClassId)
            sourceStudents.forEach { source ->
                studentRepository.addStudentToClass(classId, source.student.studentId)
                studentRepository.updateStudentActiveInClass(
                    classId = classId,
                    studentId = source.student.studentId,
                    isActive = source.isActiveInClass
                )
            }

            _uiState.update {
                it.copy(
                    showCopyFromClassDialog = false,
                    operationMessage = "Copied ${sourceStudents.size} students from class"
                )
            }
        }
    }

    fun onMessageShown() {
        _uiState.update {
            it.copy(
                saveError = null,
                operationMessage = null
            )
        }
    }

    private fun findExistingStudent(name: String?, regNumber: String?): Student? {
        val identityKey = buildIdentityKey(name, regNumber) ?: return null
        return _uiState.value.allStudents.firstOrNull { existing ->
            buildIdentityKey(existing.name, existing.registrationNumber) == identityKey
        }
    }

    private fun buildIdentityKey(name: String?, regNumber: String?): String? {
        val normalizedName = normalizeOptional(name) ?: return null
        val normalizedReg = normalizeOptional(regNumber) ?: return null
        return "${normalizedName.lowercase()}|${normalizedReg.lowercase()}"
    }

    private fun Map<String, String>.readValue(aliases: Set<String>): String? {
        val aliasSet = aliases.map(::normalizeHeader).toSet()
        val matched = entries.firstOrNull { normalizeHeader(it.key) in aliasSet }
        return matched?.value?.trim()
    }

    private fun normalizeHeader(header: String): String {
        return header
            .lowercase()
            .replace(Regex("[^a-z0-9]"), "")
    }

    private fun normalizeText(value: String): String {
        return value.trim().replace(Regex("\\s+"), " ")
    }

    private fun normalizeOptional(value: String?): String? {
        if (value == null) return null
        return normalizeText(value).ifBlank { null }
    }

    private data class ParsedCsvStudentRow(
        val sourceRowNumber: Int,
        val name: String?,
        val registrationNumber: String?,
        val department: String?,
        val rollNumber: String?,
        val email: String?,
        val phone: String?
    )

    private companion object {
        val CSV_NAME_HEADER_ALIASES = setOf(
            "name",
            "student name",
            "student_name",
            "full name",
            "full_name"
        )
        val CSV_REGISTRATION_HEADER_ALIASES = setOf(
            "registration number",
            "registration_number",
            "registrationnumber",
            "registration no",
            "registration_no",
            "reg no",
            "reg_no",
            "reg number",
            "reg_number"
        )
        val CSV_DEPARTMENT_HEADER_ALIASES = setOf(
            "department",
            "dept"
        )
        val CSV_ROLL_HEADER_ALIASES = setOf(
            "roll",
            "roll no",
            "roll_no",
            "roll number",
            "roll_number",
            "rollnumber"
        )
        val CSV_EMAIL_HEADER_ALIASES = setOf(
            "email",
            "email address",
            "email_address"
        )
        val CSV_PHONE_HEADER_ALIASES = setOf(
            "phone",
            "phone number",
            "phone_number",
            "mobile",
            "mobile number",
            "mobile_number"
        )
    }
}
