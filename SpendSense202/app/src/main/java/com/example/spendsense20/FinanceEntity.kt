/*Jenik2205 (2021). How to implement login with Room database in Kotlin. [online] Stack Overflow. Available at: https://stackoverflow.com/questions/68178857/how-to-implement-login-with-room-database-in-kotlin.
(Jenik2205, 2021)*/

package com.example.spendsense20

import androidx.room.Entity
import androidx.room.PrimaryKey

//entity data class for finances
@Entity(tableName = "finance_table")
data class FinanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val name: String,
    val description: String,
    val type: String, //Income or Expense
    val date: String,
    val imageUri: String? = null
)