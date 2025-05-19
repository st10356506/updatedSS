package com.example.spendsense20.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.spendsense20.data.AppDatabase
import com.example.spendsense20.model.User
import com.example.spendsense20.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: UserRepository
    init {
        val userDao = AppDatabase.getDatabase(application).userDao()
        repository = UserRepository(userDao)
    }
    // Register user method
    fun registerUser(user: User, onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val existingUser = repository.getUserByEmail(user.email)
                if (existingUser == null) {
                    repository.insertUser(user)
                    launch(Dispatchers.Main) {
                        onSuccess()
                    }
                } else {
                    launch(Dispatchers.Main) {
                        onError(Exception("Email already exists"))
                    }
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    onError(e)
                }
            }

        }
    }
    // Method to fetch user by email asynchronously with callback
    fun getUserByEmail(email: String, callback: (User?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val fetchedUser = repository.getUserByEmail(email)
                launch(Dispatchers.Main) {
                    callback(fetchedUser) // Callback with the fetched user data
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    callback(null) // Return null if there's an error
                }
            }
        }
    }
}