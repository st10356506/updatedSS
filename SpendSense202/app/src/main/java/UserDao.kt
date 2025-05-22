/*package com.example.spendsense20.data
import androidx.room.*
import com.example.spendsense20.model.User
@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: User)
    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
            suspend fun login(email: String, password: String): User?
        @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
        suspend fun getUserByEmail(email: String): User?
        @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
        suspend fun getUserById(id: Int): User?
}

 */