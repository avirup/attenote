package com.uteacher.attenote.data.repository.internal

import com.uteacher.attenote.data.repository.AttendanceStatusInput
import com.uteacher.attenote.domain.model.AttendanceStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class AttendanceSaveNormalizerTest {

    @Test
    fun `normalizes all statuses to skipped when class is not taken`() {
        val input = listOf(
            AttendanceStatusInput(studentId = 1L, status = AttendanceStatus.PRESENT),
            AttendanceStatusInput(studentId = 2L, status = AttendanceStatus.ABSENT),
            AttendanceStatusInput(studentId = 3L, status = AttendanceStatus.SKIPPED)
        )

        val normalized = AttendanceSaveNormalizer.normalizeRecordsForSession(
            isClassTaken = false,
            records = input
        )

        assertEquals(
            listOf(
                AttendanceStatus.SKIPPED,
                AttendanceStatus.SKIPPED,
                AttendanceStatus.SKIPPED
            ),
            normalized.map { it.status }
        )
    }
}
