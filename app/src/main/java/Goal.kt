package com.example.spendsense20.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val amount: Int,
    val targetDate: String,
    val minContribution: Int,
    val maxContribution: Int
)
