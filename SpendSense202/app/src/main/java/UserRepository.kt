package com.example.spendsense20.repository
import android.util.Log
import com.example.spendsense20.data.UserDao
import com.example.spendsense20.model.User

class UserRepository(private val userDao: UserDao) {
    suspend fun insertUser(user: User) = userDao.insertUser(user)
    suspend fun getUserByEmail(email: String): User? {
        val user = userDao.getUserByEmail(email)

        return user
    }
    suspend fun login(email: String, password: String) = userDao.login(email, password)
}