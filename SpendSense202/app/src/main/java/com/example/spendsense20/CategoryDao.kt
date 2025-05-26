/*Jenik2205 (2021). How to implement login with Room database in Kotlin. [online] Stack Overflow. Available at: https://stackoverflow.com/questions/68178857/how-to-implement-login-with-room-database-in-kotlin.
(Jenik2205, 2021)*/

package com.example.spendsense20

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CategoryDao {

    //inserts data to db
    @Insert
    suspend fun insertCategory(category: CategoryEntity)

    @Query("SELECT * FROM category_table WHERE type = :categoryType")
    suspend fun getCategoriesByType(categoryType: String): List<CategoryEntity>

    @Query("SELECT * FROM category_table")
    suspend fun getAllCategories(): List<CategoryEntity>
}
