package com.example.spendsense20

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class AllExpensesActivity : AppCompatActivity() {
//initialize views
    private lateinit var adapter: FinanceAdapter
    private lateinit var databaseRef: DatabaseReference
    private val expenseList = mutableListOf<FinanceEntity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_expenses)
//setup recycler view for showing expenses
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewAllExpenses)
        adapter = FinanceAdapter(
            onImageClick = { imageUri ->
                startActivity(Intent(this, ImageViewActivity::class.java).apply {
                    putExtra("imageUri", imageUri)
                })
            },
            //delete logic
            onDeleteClick = { finance ->
                databaseRef.child(finance.id).removeValue()
                // Google Firebase. (n.d.). Delete data. Retrieved June 7, 2025, from https://firebase.google.com/docs/database/android/delete-data
                expenseList.removeIf { it.id == finance.id }
                adapter.submitList(expenseList.toList())
            }
        )

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        // Android Developers. (n.d.). RecyclerView and LinearLayoutManager. Retrieved June 7, 2025, from https://developer.android.com/guide/topics/ui/layout/recyclerview

        val currentUser = FirebaseAuth.getInstance().currentUser
        // Google Firebase. (n.d.). Firebase Authentication documentation. Retrieved June 7, 2025, from https://firebase.google.com/docs/auth

        if (currentUser != null) {
            databaseRef = FirebaseDatabase.getInstance()
                .getReference("finances")
                .child(currentUser.uid)
            // Google Firebase. (n.d.). Firebase Realtime Database documentation. Retrieved June 7, 2025, from https://firebase.google.com/docs/database
//listen for database changes
            databaseRef.addValueEventListener(object : ValueEventListener {
                // Google Firebase. (n.d.). Listen for value events. Retrieved June 7, 2025, from https://firebase.google.com/docs/database/android/read-and-write#read_data
                override fun onDataChange(snapshot: DataSnapshot) {
                    expenseList.clear()
                    for (child in snapshot.children) {
                        if (child.key == "balance" || child.key == "budget_editable") continue
                        val data = child.getValue(FinanceEntity::class.java)
                        if (data != null && data.type.equals("expense", ignoreCase = true)) {
                            expenseList.add(data.copy(id = child.key ?: ""))
                        }
                    }
                    adapter.submitList(expenseList.toList())
                }
//handle firebase exceptions
                override fun onCancelled(error: DatabaseError) {
                    Log.e("AllExpensesActivity", "Database error: ${error.message}")
                }
            })
        }

        findViewById<Button>(R.id.btnBackExpenses).setOnClickListener {
            finish()  // closes the activity
        }
    }
}
