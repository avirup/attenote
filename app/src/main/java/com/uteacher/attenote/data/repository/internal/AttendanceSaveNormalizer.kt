package com.uteacher.attenote.data.repository.internal

import com.uteacher.attenote.data.repository.AttendanceStatusInput
import com.uteacher.attenote.domain.model.AttendanceStatus

object AttendanceSaveNormalizer {

    fun normalizeRecordsForSession(
        isClassTaken: Boolean,
        records: List<AttendanceStatusInput>
    ): List<AttendanceStatusInput> {
        return if (isClassTaken) {
            records
        } else {
            records.map { it.copy(status = AttendanceStatus.SKIPPED) }
        }
    }
}
