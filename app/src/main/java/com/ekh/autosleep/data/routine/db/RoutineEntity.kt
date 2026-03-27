package com.ekh.autosleep.data.routine.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hour: Int,
    val minute: Int,
    /** 반복 요일. 콤마 구분 문자열 "1,3,5". 빈 문자열 = 1회성 */
    val days: String,
    val label: String,
    val isEnabled: Boolean,
)
