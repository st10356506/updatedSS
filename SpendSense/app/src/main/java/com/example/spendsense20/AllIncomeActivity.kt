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

class AllIncomeActivity : AppCompatActivity() {
//initialize the views
    private lateinit var adapter: FinanceAdapter
    private lateinit var databaseRef: DatabaseReference
    private val incomeList = mutableListOf<FinanceEntity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_income)
            //inflate the view income view
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewAllIncome)
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
                incomeList.removeIf { it.id == finance.id }
                adapter.submitList(incomeList.toList())
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

            databaseRef.addValueEventListener(object : ValueEventListener {
                // Google Firebase. (n.d.). Listen for value events. Retrieved June 7, 2025, from https://firebase.google.com/docs/database/android/read-and-write#read_data
                override fun onDataChange(snapshot: DataSnapshot) {
                    incomeList.clear()
                    for (child in snapshot.children) {
                        if (child.key == "balance" || child.key == "budget_editable") continue
                        val data = child.getValue(FinanceEntity::class.java)
                        if (data != null && data.type.equals("income", ignoreCase = true)) {
                            incomeList.add(data.copy(id = child.key ?: ""))
                        }
                    }
                    adapter.submitList(incomeList.toList())
                }
//handle database failures
                override fun onCancelled(error: DatabaseError) {
                    Log.e("AllIncomeActivity", "Database error: ${error.message}")
                }
            })
        }
//back button to go back to finances
        findViewById<Button>(R.id.btnBackIncome).setOnClickListener {
            finish()  // closes the activity
        }
    }
}
