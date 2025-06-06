package com.example.spendsense20.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.spendsense20.data.Goal
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class GoalViewModel : ViewModel() {

    private val databaseRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("goals")
    private val auth = FirebaseAuth.getInstance()

    private val _goals = MutableLiveData<List<GoalWithId>>()
    val allGoals: LiveData<List<GoalWithId>> = _goals

    init {
        fetchGoals()
    }

    // Insert goal under the current user's UID and assign the Firebase key to goal.id
    fun insertGoal(goal: Goal) {
        val currentUserId = auth.currentUser?.uid ?: return
        val userRef = databaseRef.child(currentUserId)
        val key = userRef.push().key ?: return

        val goalWithKey = goal.copy(id = key)
        userRef.child(key).setValue(goalWithKey)
    }

    // Fetch all goals under the current user's UID
    private fun fetchGoals() {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            Log.w("GoalViewModel", "User not logged in.")
            _goals.value = emptyList()
            return
        }

        val userRef = databaseRef.child(currentUserId)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val goalList = mutableListOf<GoalWithId>()
                for (goalSnap in snapshot.children) {
                    val goal = goalSnap.getValue(Goal::class.java)
                    val id = goalSnap.key
                    if (goal != null && id != null) {
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

    // Delete a goal under the current user's UID
    fun deleteGoal(goalId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        databaseRef.child(currentUserId).child(goalId).removeValue()
            .addOnSuccessListener {
                Log.d("GoalViewModel", "Successfully deleted goal with ID: $goalId")
            }
            .addOnFailureListener {
                Log.e("GoalViewModel", "Failed to delete goal: ${it.message}")
            }
    }

    // Wrapper to hold Firebase key + Goal
    data class GoalWithId(
        val id: String = "",
        val goal: Goal = Goal()
    )
}