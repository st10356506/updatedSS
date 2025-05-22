package com.example.spendsense20.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.spendsense20.data.Goal
import com.google.firebase.database.*

class GoalViewModel : ViewModel() {

    private val databaseRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("goals")

    private val _goals = MutableLiveData<List<Goal>>()
    val allGoals: LiveData<List<Goal>> = _goals

    init {
        fetchGoals()
    }

    fun insertGoal(goal: Goal) {
        val key = databaseRef.push().key
        if (key != null) {
            databaseRef.child(key).setValue(goal)
        }
    }

    private fun fetchGoals() {
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val goalList = mutableListOf<Goal>()
                for (goalSnap in snapshot.children) {
                    val goal = goalSnap.getValue(Goal::class.java)
                    goal?.let { goalList.add(it) }
                }
                _goals.value = goalList
            }

            override fun onCancelled(error: DatabaseError) {
                // Optionally log error
            }
        })
    }
}
