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

    private lateinit var adapter: FinanceAdapter
    private lateinit var databaseRef: DatabaseReference
    private val incomeList = mutableListOf<FinanceEntity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_income)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewAllIncome)
        adapter = FinanceAdapter(
            onImageClick = { imageUri ->
                startActivity(Intent(this, ImageViewActivity::class.java).apply {
                    putExtra("imageUri", imageUri)
                })
            },
            onDeleteClick = { finance ->
                databaseRef.child(finance.id).removeValue()
                incomeList.removeIf { it.id == finance.id }
                adapter.submitList(incomeList.toList())
            }
        )

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            databaseRef = FirebaseDatabase.getInstance()
                .getReference("finances")
                .child(currentUser.uid)

            databaseRef.addValueEventListener(object : ValueEventListener {
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

                override fun onCancelled(error: DatabaseError) {
                    Log.e("AllIncomeActivity", "Database error: ${error.message}")
                }
            })
        }
        findViewById<Button>(R.id.btnBackIncome).setOnClickListener {
            finish()  // closes the activity
        }
    }
}