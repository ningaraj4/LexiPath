package com.example.lexipath.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "daily_content")
@TypeConverters(Converters::class)
data class DailyContentEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val date: String,
    val word: String,
    val meaning: String,
    val examplesTarget: List<String>,
    val examplesBase: List<String>,
    val createdAt: String,
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val goalType: String,
    val targetLang: String?,
    val baseLang: String?,
    val level: String,
    val industrySector: String?,
    val createdAt: String,
    val updatedAt: String
)

@Entity(tableName = "quiz_logs")
@TypeConverters(Converters::class)
data class QuizLogEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val contentId: String,
    val quizType: String,
    val question: String,
    val options: List<String>,
    val correctAnswer: String,
    val userAnswer: String,
    val isCorrect: Boolean,
    val createdAt: String
)

@Entity(tableName = "weekly_plans")
@TypeConverters(Converters::class)
data class WeeklyPlanEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val weekStart: String,
    val planJson: String, // Store as JSON string
    val createdAt: String
)

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listType)
    }
}
