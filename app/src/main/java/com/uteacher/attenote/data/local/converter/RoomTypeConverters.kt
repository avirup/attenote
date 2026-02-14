package com.uteacher.attenote.data.local.converter

import androidx.room.TypeConverter
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class RoomTypeConverters {

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? =
        date?.format(DateTimeFormatter.ISO_LOCAL_DATE)

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? =
        value?.let { LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE) }

    @TypeConverter
    fun fromLocalTime(time: LocalTime?): String? =
        time?.format(timeFormatter)

    @TypeConverter
    fun toLocalTime(value: String?): LocalTime? =
        value?.let { LocalTime.parse(it, timeFormatter) }

    @TypeConverter
    fun fromDayOfWeek(dayOfWeek: DayOfWeek?): Int? =
        dayOfWeek?.value

    @TypeConverter
    fun toDayOfWeek(value: Int?): DayOfWeek? =
        value?.let { DayOfWeek.of(it) }
}
