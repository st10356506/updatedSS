package com.example.spendsense20

import androidx.room.Entity
import androidx.room.PrimaryKey

//database entities for categories
//“The FULL Beginner Guide for Room in Android | Local Database Tutorial for Android.” Www.youtube.com, www.youtube.com/watch?v=bOd3wO0uFr8. Accessed 26 June 2023.
@Entity(tableName = "category_table")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String, // "Groceries", "Salary", "Entertainment", etc.
    val type: String  // "Income" or "Expense"
)
