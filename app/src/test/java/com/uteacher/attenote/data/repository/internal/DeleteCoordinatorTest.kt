package com.uteacher.attenote.data.repository.internal

import java.io.File
import java.nio.file.Files
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DeleteCoordinatorTest {

    @Test
    fun `class cascade delete removes dependent rows and only unreferenced media files`() = runBlocking {
        val tempRoot = Files.createTempDirectory("class-cascade-delete").toFile()
        try {
            val mediaA = File(tempRoot, "media-a.jpg").apply { writeText("a") }
            val sharedMedia = File(tempRoot, "media-shared.jpg").apply { writeText("shared") }
            val unrelatedMedia = File(tempRoot, "media-unrelated.jpg").apply { writeText("unrelated") }

            val gateway = FakeClassCascadeDeleteGateway().apply {
                classes += 11L
                classes += 42L
                schedulesByClass[42L] = mutableSetOf(7L, 8L)
                sessionsByClass[42L] = mutableSetOf(100L, 101L)
                recordsBySession[100L] = mutableSetOf(1L, 2L)
                recordsBySession[101L] = mutableSetOf(3L)

                notesByClass[42L] = mutableSetOf(500L)
                noteMediaByNote[500L] = mutableSetOf(mediaA.absolutePath, sharedMedia.absolutePath)

                notesByClass[11L] = mutableSetOf(600L)
                noteMediaByNote[600L] = mutableSetOf(sharedMedia.absolutePath, unrelatedMedia.absolutePath)
            }

            val result = performClassCascadeDelete(
                classId = 42L,
                gateway = gateway,
                cleaner = LocalMediaFileCleaner(tempRoot)
            )

            assertTrue(result.classDeleted)
            assertFalse(gateway.classes.contains(42L))
            assertFalse(gateway.schedulesByClass.containsKey(42L))
            assertFalse(gateway.sessionsByClass.containsKey(42L))
            assertFalse(gateway.recordsBySession.containsKey(100L))
            assertFalse(gateway.recordsBySession.containsKey(101L))
            assertFalse(gateway.notesByClass.containsKey(42L))
            assertFalse(gateway.noteMediaByNote.containsKey(500L))

            assertFalse(mediaA.exists())
            assertTrue(sharedMedia.exists())
            assertTrue(unrelatedMedia.exists())

            assertTrue(gateway.classes.contains(11L))
            assertNotNull(gateway.notesByClass[11L])
        } finally {
            tempRoot.deleteRecursively()
        }
    }

    @Test
    fun `student cascade delete removes student and attendance records`() = runBlocking {
        val gateway = FakeStudentCascadeDeleteGateway().apply {
            students += 77L
            attendanceRecordsByStudent[77L] = 5
        }

        val result = performStudentCascadeDelete(
            studentId = 77L,
            gateway = gateway
        )

        assertTrue(result.studentDeleted)
        assertFalse(gateway.students.contains(77L))
        assertFalse(gateway.attendanceRecordsByStudent.containsKey(77L))
    }

    @Test
    fun `permanent note delete removes note rows and media files`() = runBlocking {
        val tempRoot = Files.createTempDirectory("note-delete").toFile()
        try {
            val noteOnlyMedia = File(tempRoot, "note-only.jpg").apply { writeText("note") }
            val sharedMedia = File(tempRoot, "shared.jpg").apply { writeText("shared") }

            val gateway = FakePermanentNoteDeleteGateway().apply {
                notes += 90L
                notes += 91L
                noteMediaByNote[90L] = mutableSetOf(
                    noteOnlyMedia.absolutePath,
                    sharedMedia.absolutePath,
                    File(tempRoot, "missing.jpg").absolutePath
                )
                noteMediaByNote[91L] = mutableSetOf(sharedMedia.absolutePath)
            }

            val result = performPermanentNoteDelete(
                noteId = 90L,
                gateway = gateway,
                cleaner = LocalMediaFileCleaner(tempRoot)
            )

            assertTrue(result.noteDeleted)
            assertFalse(gateway.notes.contains(90L))
            assertFalse(gateway.noteMediaByNote.containsKey(90L))

            assertFalse(noteOnlyMedia.exists())
            assertTrue(sharedMedia.exists())
            assertTrue(
                result.mediaCleanupResults.any { it.status == MediaCleanupStatus.MISSING }
            )
        } finally {
            tempRoot.deleteRecursively()
        }
    }

    @Test
    fun `single media delete removes only target media row and file`() = runBlocking {
        val tempRoot = Files.createTempDirectory("single-media-delete").toFile()
        try {
            val target = File(tempRoot, "target.jpg").apply { writeText("target") }
            val keep = File(tempRoot, "keep.jpg").apply { writeText("keep") }

            val gateway = FakePermanentMediaDeleteGateway().apply {
                mediaById[1L] = target.absolutePath
                mediaById[2L] = keep.absolutePath
            }

            val result = performPermanentMediaDelete(
                mediaId = 1L,
                gateway = gateway,
                cleaner = LocalMediaFileCleaner(tempRoot)
            )

            assertTrue(result.mediaDeleted)
            assertFalse(gateway.mediaById.containsKey(1L))
            assertTrue(gateway.mediaById.containsKey(2L))
            assertFalse(target.exists())
            assertTrue(keep.exists())

            val secondDelete = performPermanentMediaDelete(
                mediaId = 1L,
                gateway = gateway,
                cleaner = LocalMediaFileCleaner(tempRoot)
            )
            assertFalse(secondDelete.mediaDeleted)
            assertEquals(emptyList<MediaCleanupResult>(), secondDelete.mediaCleanupResults)
        } finally {
            tempRoot.deleteRecursively()
        }
    }
}

private class FakeClassCascadeDeleteGateway : ClassCascadeDeleteGateway {
    val classes = mutableSetOf<Long>()
    val schedulesByClass = mutableMapOf<Long, MutableSet<Long>>()
    val sessionsByClass = mutableMapOf<Long, MutableSet<Long>>()
    val recordsBySession = mutableMapOf<Long, MutableSet<Long>>()
    val notesByClass = mutableMapOf<Long, MutableSet<Long>>()
    val noteMediaByNote = mutableMapOf<Long, MutableSet<String>>()

    override suspend fun <T> runInTransaction(block: suspend () -> T): T = block()

    override suspend fun getNoteIdsForClass(classId: Long): List<Long> {
        return notesByClass[classId].orEmpty().toList()
    }

    override suspend fun getMediaPathsForNoteIds(noteIds: List<Long>): List<String> {
        return noteIds.flatMap { noteId -> noteMediaByNote[noteId].orEmpty() }
    }

    override suspend fun countMediaReferences(filePath: String): Int {
        return noteMediaByNote.values.sumOf { mediaPaths -> mediaPaths.count { it == filePath } }
    }

    override suspend fun deleteAttendanceRecordsForClass(classId: Long) {
        sessionsByClass[classId].orEmpty().forEach { sessionId ->
            recordsBySession.remove(sessionId)
        }
    }

    override suspend fun deleteAttendanceSessionsForClass(classId: Long) {
        sessionsByClass.remove(classId)?.forEach { sessionId ->
            recordsBySession.remove(sessionId)
        }
    }

    override suspend fun deleteSchedulesForClass(classId: Long) {
        schedulesByClass.remove(classId)
    }

    override suspend fun deleteNoteMediaForNoteIds(noteIds: List<Long>) {
        noteIds.forEach { noteId ->
            noteMediaByNote.remove(noteId)
        }
    }

    override suspend fun deleteNotesForClass(classId: Long) {
        notesByClass.remove(classId)?.forEach { noteId ->
            noteMediaByNote.remove(noteId)
        }
    }

    override suspend fun deleteClass(classId: Long): Int {
        return if (classes.remove(classId)) 1 else 0
    }
}

private class FakeStudentCascadeDeleteGateway : StudentCascadeDeleteGateway {
    val students = mutableSetOf<Long>()
    val attendanceRecordsByStudent = mutableMapOf<Long, Int>()

    override suspend fun <T> runInTransaction(block: suspend () -> T): T = block()

    override suspend fun deleteAttendanceRecordsForStudent(studentId: Long) {
        attendanceRecordsByStudent.remove(studentId)
    }

    override suspend fun deleteStudent(studentId: Long): Int {
        return if (students.remove(studentId)) 1 else 0
    }
}

private class FakePermanentNoteDeleteGateway : PermanentNoteDeleteGateway {
    val notes = mutableSetOf<Long>()
    val noteMediaByNote = mutableMapOf<Long, MutableSet<String>>()

    override suspend fun <T> runInTransaction(block: suspend () -> T): T = block()

    override suspend fun getNoteMediaPaths(noteId: Long): List<String> {
        return noteMediaByNote[noteId].orEmpty().toList()
    }

    override suspend fun countMediaReferences(filePath: String): Int {
        return noteMediaByNote.values.sumOf { mediaPaths -> mediaPaths.count { it == filePath } }
    }

    override suspend fun deleteAllNoteMedia(noteId: Long) {
        noteMediaByNote.remove(noteId)
    }

    override suspend fun deleteNote(noteId: Long): Int {
        return if (notes.remove(noteId)) 1 else 0
    }
}

private class FakePermanentMediaDeleteGateway : PermanentMediaDeleteGateway {
    val mediaById = mutableMapOf<Long, String>()

    override suspend fun <T> runInTransaction(block: suspend () -> T): T = block()

    override suspend fun getMediaPath(mediaId: Long): String? {
        return mediaById[mediaId]
    }

    override suspend fun countMediaReferences(filePath: String): Int {
        return mediaById.values.count { it == filePath }
    }

    override suspend fun deleteMedia(mediaId: Long): Int {
        return if (mediaById.remove(mediaId) != null) 1 else 0
    }
}
