//Jenik2205 (2021). How to implement login with Room database in Kotlin. [online] Stack Overflow. Available at: https://stackoverflow.com/questions/68178857/how-to-implement-login-with-room-database-in-kotlin.
//(Jenik2205, 2021)
/*package com.example.spendsense20

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface FinanceDao {

    //FinanceEntity Methods
    @Insert
    fun insertFinance(finance: FinanceEntity)

    @Query("SELECT * FROM finance_table WHERE date = :selectedDate")
    fun getFinanceByDate(selectedDate: String): LiveData<List<FinanceEntity>>

    @Query("SELECT * FROM finance_table")
    suspend fun getAllFinances(): List<FinanceEntity>

    @Query("DELETE FROM finance_table WHERE id = :financeId")
    suspend fun deleteFinance(financeId: Int)

    @Query("SELECT * FROM finance_table WHERE type = 'income'")
    suspend fun getAllIncome(): List<FinanceEntity>

    @Query("SELECT * FROM finance_table WHERE type = 'expense'")
    suspend fun getAllExpenses(): List<FinanceEntity>

    @Query("SELECT SUM(amount) FROM finance_table WHERE type = 'income'")
    suspend fun getTotalIncome(): Double

    @Query("SELECT SUM(amount) FROM finance_table WHERE type = 'expense'")
    suspend fun getTotalExpense(): Double

    @Query("SELECT * FROM finance_table WHERE type = 'expense' AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getExpensesBetweenDates(startDate: String, endDate: String): List<FinanceEntity>

    @Query("""
        SELECT name AS categoryName, SUM(amount) AS totalAmount
        FROM finance_table
        WHERE type = 'expense' AND date BETWEEN :startDate AND :endDate
        GROUP BY name
    """)
    suspend fun getCategoryTotalsBetweenDates(startDate: String, endDate: String): List<CategorySummary>
}*/