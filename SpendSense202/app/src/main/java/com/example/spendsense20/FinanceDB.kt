/*package com.example.spendsense20

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [FinanceEntity::class], version = 1)
abstract class FinanceDB : RoomDatabase() {
    abstract fun FinanceDao(): FinanceDao

    companion object {
        @Volatile
        private var INSTANCE: FinanceDB? = null

        fun getDatabase(context: Context): FinanceDB {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FinanceDB::class.java,
                    "finances_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
*/