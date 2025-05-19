package com.example.spendsense20.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.spendsense20.data.Goal
import com.example.spendsense20.data.AppDatabase
import kotlinx.coroutines.launch


class GoalViewModel(application: Application) : AndroidViewModel(application) {

    private val goalDao = AppDatabase.getDatabase(application).goalDao()
    val allGoals: LiveData<List<Goal>> = goalDao.getAllGoals()

    fun insertGoal(goal: Goal) {
        viewModelScope.launch {
            goalDao.insertGoal(goal)
        }
    }
}
