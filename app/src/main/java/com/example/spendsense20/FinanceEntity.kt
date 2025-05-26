<<<<<<< HEAD
package com.example.spendsense20

data class FinanceEntity(
    var id: String = "", // Firebase document key
    var amount: Double = 0.0,
    var name: String = "",
    var description: String = "",
    var type: String = "", // "income" or "expense"
    var date: String = "",
    var imageUri: String? = null
)
=======
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
>>>>>>> 5591ece3884d05b8bdb7780a5870feabd4ac6446
