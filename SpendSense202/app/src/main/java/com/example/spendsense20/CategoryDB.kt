/*Jenik2205 (2021). How to implement login with Room database in Kotlin. [online] Stack Overflow. Available at: https://stackoverflow.com/questions/68178857/how-to-implement-login-with-room-database-in-kotlin.
(Jenik2205, 2021)*/

package com.example.spendsense20

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

//entities for the category db
@Database(entities = [CategoryEntity::class], version = 1)
abstract class CategoryDB : RoomDatabase() {
    abstract fun CategoryDao(): CategoryDao

    companion object{
        @Volatile
        private var INSTANCE: CategoryDB? = null

        fun getDatabase(context: Context): CategoryDB{
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CategoryDB::class.java,
                    "finances_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}