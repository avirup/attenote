package com.uteacher.attenote.domain.mapper

import com.uteacher.attenote.data.local.entity.AttendanceRecordEntity
import com.uteacher.attenote.data.local.entity.AttendanceSessionEntity
import com.uteacher.attenote.data.local.entity.ClassEntity
import com.uteacher.attenote.data.local.entity.ClassStudentCrossRef
import com.uteacher.attenote.data.local.entity.NoteEntity
import com.uteacher.attenote.data.local.entity.NoteMediaEntity
import com.uteacher.attenote.data.local.entity.ScheduleEntity
import com.uteacher.attenote.data.local.entity.StudentEntity
import com.uteacher.attenote.domain.model.AttendanceRecord
import com.uteacher.attenote.domain.model.AttendanceSession
import com.uteacher.attenote.domain.model.Class as DomainClass
import com.uteacher.attenote.domain.model.ClassStudentLink
import com.uteacher.attenote.domain.model.Note
import com.uteacher.attenote.domain.model.NoteMedia
import com.uteacher.attenote.domain.model.Schedule
import com.uteacher.attenote.domain.model.Student
import kotlin.jvm.JvmName

fun ClassEntity.toDomain() = DomainClass(
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

fun DomainClass.toEntity() = ClassEntity(
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

@JvmName("classEntityListToDomain")
fun List<ClassEntity>.toDomain() = map { it.toDomain() }
@JvmName("classDomainListToEntity")
fun List<DomainClass>.toEntity() = map { it.toEntity() }

@JvmName("studentEntityListToDomain")
fun List<StudentEntity>.toDomain() = map { it.toDomain() }
@JvmName("studentDomainListToEntity")
fun List<Student>.toEntity() = map { it.toEntity() }

@JvmName("classStudentCrossRefListToDomain")
fun List<ClassStudentCrossRef>.toDomain() = map { it.toDomain() }
@JvmName("classStudentLinkListToEntity")
fun List<ClassStudentLink>.toEntity() = map { it.toEntity() }

@JvmName("scheduleEntityListToDomain")
fun List<ScheduleEntity>.toDomain() = map { it.toDomain() }
@JvmName("scheduleDomainListToEntity")
fun List<Schedule>.toEntity() = map { it.toEntity() }

@JvmName("attendanceSessionEntityListToDomain")
fun List<AttendanceSessionEntity>.toDomain() = map { it.toDomain() }
@JvmName("attendanceSessionDomainListToEntity")
fun List<AttendanceSession>.toEntity() = map { it.toEntity() }

@JvmName("attendanceRecordEntityListToDomain")
fun List<AttendanceRecordEntity>.toDomain() = map { it.toDomain() }
@JvmName("attendanceRecordDomainListToEntity")
fun List<AttendanceRecord>.toEntity() = map { it.toEntity() }

@JvmName("noteEntityListToDomain")
fun List<NoteEntity>.toDomain() = map { it.toDomain() }
@JvmName("noteDomainListToEntity")
fun List<Note>.toEntity() = map { it.toEntity() }

@JvmName("noteMediaEntityListToDomain")
fun List<NoteMediaEntity>.toDomain() = map { it.toDomain() }
@JvmName("noteMediaDomainListToEntity")
fun List<NoteMedia>.toEntity() = map { it.toEntity() }
