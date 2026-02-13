package com.uteacher.attendancetracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.uteacher.attendancetracker.data.local.converter.RoomTypeConverters
import com.uteacher.attendancetracker.data.local.dao.AttendanceRecordDao
import com.uteacher.attendancetracker.data.local.dao.AttendanceSessionDao
import com.uteacher.attendancetracker.data.local.dao.ClassDao
import com.uteacher.attendancetracker.data.local.dao.ClassStudentCrossRefDao
import com.uteacher.attendancetracker.data.local.dao.NoteDao
import com.uteacher.attendancetracker.data.local.dao.NoteMediaDao
import com.uteacher.attendancetracker.data.local.dao.ScheduleDao
import com.uteacher.attendancetracker.data.local.dao.StudentDao
import com.uteacher.attendancetracker.data.local.entity.AttendanceRecordEntity
import com.uteacher.attendancetracker.data.local.entity.AttendanceSessionEntity
import com.uteacher.attendancetracker.data.local.entity.ClassEntity
import com.uteacher.attendancetracker.data.local.entity.ClassStudentCrossRef
import com.uteacher.attendancetracker.data.local.entity.NoteEntity
import com.uteacher.attendancetracker.data.local.entity.NoteMediaEntity
import com.uteacher.attendancetracker.data.local.entity.ScheduleEntity
import com.uteacher.attendancetracker.data.local.entity.StudentEntity

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
    exportSchema = true
)
@TypeConverters(RoomTypeConverters::class)
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
