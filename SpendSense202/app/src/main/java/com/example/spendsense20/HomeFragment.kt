package com.example.spendsense20

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class HomeFragment : Fragment() {

    private lateinit var databaseRef: DatabaseReference
    private var userId: String? = null

    // UI Elements
    private lateinit var progressBarFill: View
    private lateinit var tvFrequentCategory: TextView
    private lateinit var tvPointsEarned: TextView
    private lateinit var tvUserRank: TextView
    private lateinit var tvProgressText: TextView
    private lateinit var totalBalanceTextView: TextView
    private lateinit var tvNextRankLabel: TextView
    private lateinit var ivRankImage: ImageView

    private val goal = 15000.0  // Target savings or earnings

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return view
        }

        databaseRef = FirebaseDatabase.getInstance().getReference("finances").child(userId!!)

        // Bind views
        progressBarFill = view.findViewById(R.id.progressBarFill)
        tvFrequentCategory = view.findViewById(R.id.tvFrequentCategory)
        tvPointsEarned = view.findViewById(R.id.tvPointsEarned)
        tvUserRank = view.findViewById(R.id.tvUserRank)
        tvProgressText = view.findViewById(R.id.tvProgressText)
        totalBalanceTextView = view.findViewById(R.id.totalBalanceTextView)
        tvNextRankLabel = view.findViewById(R.id.tvNextRankLabel)
        ivRankImage = view.findViewById(R.id.ivRankImage)

        listenToFinanceChanges()
        return view
    }

    private fun listenToFinanceChanges() {
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val finances = mutableListOf<FinanceEntity>()
                for (child in snapshot.children) {
                    val entity = child.getValue(FinanceEntity::class.java)
                    if (entity != null) {
                        finances.add(entity)
                    }
                }
                updateUI(finances)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HomeFragment", "Database error: ${error.message}")
            }
        })
    }

    private fun updateUI(finances: List<FinanceEntity>) {
        val income = finances.filter { it.type == "income" }.sumOf { it.amount }
        val expenses = finances.filter { it.type == "expense" }.sumOf { it.amount }
        val balance = income - expenses
        val progressRatio = (balance / goal).coerceIn(0.0, 1.0)

        totalBalanceTextView.text = "R %.2f".format(balance)

        progressBarFill.post {
            val parentWidth = (progressBarFill.parent as View).width
            val newWidth = (parentWidth * progressRatio).toInt()
            val params = progressBarFill.layoutParams
            params.width = newWidth
            progressBarFill.layoutParams = params
        }

        val percent = (progressRatio * 100).toInt()
        tvProgressText.text = "$percent% of R${goal.toInt()}"

        val categoryMap = finances.groupingBy { it.name }.eachCount()
        val mostFrequent = categoryMap.maxByOrNull { it.value }?.key ?: "None"
        tvFrequentCategory.text = mostFrequent

        val points = (balance / 100).toInt().coerceAtLeast(0)
        tvPointsEarned.text = "$points pts"

        val currentRank = when {
            points >= 200 -> "Platinum"
            points >= 100 -> "Gold"
            points >= 50 -> "Silver"
            else -> "Bronze"
        }
        tvUserRank.text = currentRank


        val rankDrawableRes = when (currentRank) {
            "Bronze" -> R.drawable.bronze
            "Silver" -> R.drawable.silver
            "Gold" -> R.drawable.gold
            "Platinum" -> R.drawable.platinum
            else -> R.drawable.bronze
        }
        ivRankImage.setImageResource(rankDrawableRes)

        val nextRank = when {
            points < 50 -> "Silver"
            points < 100 -> "Gold"
            points < 200 -> "Platinum"
            else -> "Max Rank Achieved"
        }
        tvNextRankLabel.text = "Progress to $nextRank"
    }
}