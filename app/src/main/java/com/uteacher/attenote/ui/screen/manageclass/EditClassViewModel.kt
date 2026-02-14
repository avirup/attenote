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
                    saveError = "Invalid class route parameters"
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
                        saveError = "Class not found"
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
                    saveError = null
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
                val rows = parseCsv(csvContent)
                if (rows.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            csvPreviewData = emptyList(),
                            csvImportError = "No CSV rows found"
                        )
                    }
                    return@launch
                }

                val seenKeys = mutableSetOf<String>()
                val previewData = rows.map { row ->
                    val key = "${row.name.lowercase()}|${row.registrationNumber.lowercase()}"
                    val isDuplicate = !seenKeys.add(key)
                    val alreadyExists = _uiState.value.allStudents.any { existing ->
                        existing.name.equals(row.name, ignoreCase = true) &&
                            existing.registrationNumber.equals(row.registrationNumber, ignoreCase = true)
                    }

                    row.copy(
                        isDuplicate = isDuplicate,
                        alreadyExists = alreadyExists,
                        hasWarning = isDuplicate ||
                            row.name.startsWith("UNKNOWN_") ||
                            row.registrationNumber.startsWith("UNREG_")
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
                        csvPreviewData = emptyList(),
                        csvImportError = "Failed to parse CSV: ${e.message}"
                    )
                }
            }
        }
    }

    private fun parseCsv(content: String): List<CsvStudentRow> {
        val batchTimestamp = System.currentTimeMillis()
        val rows = mutableListOf<CsvStudentRow>()
        val csvData = csvReader {
            charset = "UTF-8"
            quoteChar = '"'
            delimiter = ','
            escapeChar = '\\'
        }.readAllWithHeader(content.byteInputStream())

        csvData.forEachIndexed { index, row ->
            val name = row.readValue(
                "name"
            )
            val regNumber = row.readValue(
                "registrationnumber",
                "registration_number",
                "registration number",
                "regnumber",
                "reg_number",
                "reg number"
            )
            val rollNumber = row.readValue("rollnumber", "roll_number", "roll number")
            val email = row.readValue("email")
            val phone = row.readValue("phone", "phone_number", "phonenumber")

            val finalName = if (name.isNullOrBlank()) {
                "UNKNOWN_${batchTimestamp}_${index + 1}"
            } else {
                normalizeText(name)
            }
            val finalReg = if (regNumber.isNullOrBlank()) {
                "UNREG_${batchTimestamp}_${index + 1}"
            } else {
                normalizeText(regNumber)
            }

            rows += CsvStudentRow(
                name = finalName,
                registrationNumber = finalReg,
                rollNumber = normalizeOptional(rollNumber),
                email = normalizeOptional(email),
                phone = normalizeOptional(phone)
            )
        }

        return rows
    }

    fun onConfirmCsvImport() {
        val rows = _uiState.value.csvPreviewData.filter { !it.isDuplicate }
        if (rows.isEmpty()) {
            _uiState.update { it.copy(csvImportError = "No rows available for import") }
            return
        }

        viewModelScope.launch {
            rows.forEach { row ->
                if (row.alreadyExists) {
                    val existing = findExistingStudent(row.name, row.registrationNumber)
                    if (existing != null) {
                        studentRepository.addStudentToClass(classId, existing.studentId)
                        studentRepository.updateStudentActiveInClass(classId, existing.studentId, true)
                    }
                } else {
                    val createResult = studentRepository.createStudent(
                        Student(
                            studentId = 0L,
                            name = row.name,
                            registrationNumber = row.registrationNumber,
                            rollNumber = row.rollNumber,
                            email = row.email,
                            phone = row.phone,
                            isActive = true,
                            createdAt = LocalDate.now()
                        )
                    )

                    when (createResult) {
                        is RepositoryResult.Success -> {
                            studentRepository.addStudentToClass(classId, createResult.data)
                            studentRepository.updateStudentActiveInClass(classId, createResult.data, true)
                        }

                        is RepositoryResult.Error -> {
                            val existing = findExistingStudent(row.name, row.registrationNumber)
                            if (existing != null) {
                                studentRepository.addStudentToClass(classId, existing.studentId)
                                studentRepository.updateStudentActiveInClass(
                                    classId,
                                    existing.studentId,
                                    true
                                )
                            }
                        }
                    }
                }
            }

            _uiState.update {
                it.copy(
                    showCsvImportDialog = false,
                    csvPreviewData = emptyList(),
                    csvImportError = null,
                    operationMessage = "CSV import completed"
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

    private fun findExistingStudent(name: String, regNumber: String): Student? {
        return _uiState.value.allStudents.firstOrNull { existing ->
            existing.name.equals(name, ignoreCase = true) &&
                existing.registrationNumber.equals(regNumber, ignoreCase = true)
        }
    }

    private fun Map<String, String>.readValue(vararg aliases: String): String? {
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
}
