package com.example.spendsense20.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.spendsense20.data.Goal
import com.google.firebase.database.*

class GoalViewModel : ViewModel() {

    private val databaseRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("goals")

    // LiveData now holds list of GoalWithId
    private val _goals = MutableLiveData<List<GoalWithId>>()
    val allGoals: LiveData<List<GoalWithId>> = _goals

    init {
        fetchGoals()
    }

    // Insert a new goal and auto-generate its ID
    fun insertGoal(goal: Goal) {
        val key = databaseRef.push().key
        if (key != null) {
            databaseRef.child(key).setValue(goal)
        }
    }

    // Fetch all goals from the database with their IDs
    private fun fetchGoals() {
        val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null) {
            Log.w("GoalViewModel", "User not logged in.")
            _goals.value = emptyList()
            return
        }

        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val goalList = mutableListOf<GoalWithId>()
                for (goalSnap in snapshot.children) {
                    val goal = goalSnap.getValue(Goal::class.java)
                    val id = goalSnap.key
                    if (goal != null && id != null && goal.id == currentUserId) {
                        goalList.add(GoalWithId(id, goal))
                    }
                }
                _goals.value = goalList
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("GoalViewModel", "Failed to fetch goals: ${error.message}")
            }
        })
    }


    // Delete a goal using its ID
    fun deleteGoal(id: String) {
        databaseRef.child(id).removeValue()
            .addOnSuccessListener {
                Log.d("GoalViewModel", "Successfully deleted goal entry with ID: $id")
            }
            .addOnFailureListener {
                Log.e("GoalViewModel", "Failed to delete goal entry: ${it.message}")
            }
    }

    // Wrapper class to include the Firebase ID with each goal
    data class GoalWithId(
        val id: String = "",
        val goal: Goal = Goal()
    )
}
