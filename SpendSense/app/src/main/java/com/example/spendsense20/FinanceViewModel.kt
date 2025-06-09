package com.example.spendsense20

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.*

class FinanceViewModel(application: Application) : AndroidViewModel(application) {
    // Android Developers. (n.d.). AndroidViewModel. Retrieved June 7, 2025, from https://developer.android.com/reference/androidx/lifecycle/AndroidViewModel

    // Helper to sanitize email for Firebase keys (replace '.' with ',')
    private fun sanitizeEmail(email: String): String = email.replace(".", ",")

    // Reference scoped to the current user's finance node
    private val databaseRef: DatabaseReference
        get() {
            val email = SessionManager.userEmail ?: ""
            val userKey = sanitizeEmail(email)
            return FirebaseDatabase.getInstance().getReference("finances").child(userKey)
            // Firebase Documentation. (n.d.). Realtime Database. Retrieved June 7, 2025, from https://firebase.google.com/docs/database
        }

    // Insert a new finance entry under current user
    fun insertFinance(finance: FinanceEntity) {
        val id = databaseRef.push().key ?: return
        finance.id = id
        databaseRef.child(id).setValue(finance)
        // Firebase Documentation. (n.d.). Writing Data. Retrieved June 7, 2025, from https://firebase.google.com/docs/database/android/write-data
    }
//delete logic
    fun deleteFinance(id: String) {
        databaseRef.child(id).removeValue()
            .addOnSuccessListener {
                Log.d("FinanceViewModel", "Successfully deleted finance entry with ID: $id")
                // Android Developers. (n.d.). Log. Retrieved June 7, 2025, from https://developer.android.com/reference/android/util/Log
            }
            .addOnFailureListener {
                Log.e("FinanceViewModel", "Failed to delete finance entry: ${it.message}")
            }
    }

    // view finances for a specific date under current user
    fun getFinanceByDate(selectedDate: String): LiveData<List<FinanceEntity>> {
        val liveData = MutableLiveData<List<FinanceEntity>>()

        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val filteredList = mutableListOf<FinanceEntity>()
                for (child in snapshot.children) {
                    val item = child.getValue(FinanceEntity::class.java)
                    if (item?.date == selectedDate) {
                        filteredList.add(item)
                    }
                }
                liveData.postValue(filteredList)
            }

            override fun onCancelled(error: DatabaseError) {
                liveData.postValue(emptyList())
            }
        })
        // Firebase Documentation. (n.d.). Reading Data. Retrieved June 7, 2025, from https://firebase.google.com/docs/database/android/read-and-write
        // Android Developers. (n.d.). LiveData. Retrieved June 7, 2025, from https://developer.android.com/topic/libraries/architecture/livedata

        return liveData
    }

    // Get total amount per category between dates under current user
    fun getCategoryTotals(startDate: String, endDate: String): LiveData<List<CategorySummary>> {
        val liveData = MutableLiveData<List<CategorySummary>>()

        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val categoryMap = mutableMapOf<String, Double>()
                for (child in snapshot.children) {
                    val item = child.getValue(FinanceEntity::class.java) ?: continue
                    if (item.date >= startDate && item.date <= endDate) {
                        val amount = item.amount
                        val category = item.name
                        categoryMap[category] = categoryMap.getOrDefault(category, 0.0) + amount
                    }
                }
                val summaryList = categoryMap.map { (category, total) ->
                    CategorySummary(category, total)
                }
                liveData.postValue(summaryList)
            }

            override fun onCancelled(error: DatabaseError) {
                liveData.postValue(emptyList())
            }
        })

        return liveData
    }
}
