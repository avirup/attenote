package com.uteacher.attenote.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.uteacher.attenote.data.local.converter.RoomTypeConverters
import com.uteacher.attenote.data.local.dao.AttendanceRecordDao
import com.uteacher.attenote.data.local.dao.AttendanceSessionDao
import com.uteacher.attenote.data.local.dao.ClassDao
import com.uteacher.attenote.data.local.dao.ClassStudentCrossRefDao
import com.uteacher.attenote.data.local.dao.NoteDao
import com.uteacher.attenote.data.local.dao.NoteMediaDao
import com.uteacher.attenote.data.local.dao.ScheduleDao
import com.uteacher.attenote.data.local.dao.StudentDao
import com.uteacher.attenote.data.local.entity.AttendanceRecordEntity
import com.uteacher.attenote.data.local.entity.AttendanceSessionEntity
import com.uteacher.attenote.data.local.entity.ClassEntity
import com.uteacher.attenote.data.local.entity.ClassStudentCrossRef
import com.uteacher.attenote.data.local.entity.NoteEntity
import com.uteacher.attenote.data.local.entity.NoteMediaEntity
import com.uteacher.attenote.data.local.entity.ScheduleEntity
import com.uteacher.attenote.data.local.entity.StudentEntity

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
    version = 3,
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
