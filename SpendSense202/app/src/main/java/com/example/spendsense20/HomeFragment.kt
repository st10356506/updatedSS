package com.example.spendsense20

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.xml.KonfettiView
import java.util.concurrent.TimeUnit

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
    private lateinit var konfettiView: KonfettiView

    private val goal = 15000.0  // No longer used for progress bar

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
        konfettiView = view.findViewById(R.id.konfettiView)

        ivRankImage.setOnClickListener {
            konfettiView.start(
                Party(
                    speed = 10f,
                    maxSpeed = 30f,
                    damping = 0.9f,
                    spread = 360,
                    colors = listOf(
                        0xFF006400.toInt(), // Dark green
                        0xFF228B22.toInt(), // Forest green
                        0xFF32CD32.toInt(), // Lime green
                        0xFF7CFC00.toInt()  // Lawn green
                    ),
                    emitter = Emitter(duration = 2, TimeUnit.SECONDS).perSecond(40),
                    position = Position.Relative(0.5, 0.85) // Lower on the screen
                )
            )
        }

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

        totalBalanceTextView.text = "R %.2f".format(balance)

        val points = (balance / 100).toInt().coerceAtLeast(0)
        tvPointsEarned.text = "$points pts"

        // Determine current rank, next rank threshold, and next rank label
        val (currentRank, nextThreshold, nextRank) = when {
            points >= 200 -> Triple("Platinum", null, "Max Rank Achieved")
            points >= 100 -> Triple("Gold", 200, "Platinum")
            points >= 50 -> Triple("Silver", 100, "Gold")
            else -> Triple("Bronze", 50, "Silver")
        }

        tvUserRank.text = currentRank
        tvNextRankLabel.text = if (nextThreshold != null) {
            "Progress to $nextRank"
        } else {
            "Max Rank Achieved"
        }

        // Calculate progress toward next rank
        val lowerBound = when (currentRank) {
            "Bronze" -> 0
            "Silver" -> 50
            "Gold" -> 100
            "Platinum" -> 200
            else -> 0
        }

        val upperBound = nextThreshold ?: lowerBound // For max rank, progress is full
        val range = (upperBound - lowerBound).coerceAtLeast(1)
        val progressRatio = ((points - lowerBound).toDouble() / range).coerceIn(0.0, 1.0)

        // Update progress bar fill width
        progressBarFill.post {
            val parentWidth = (progressBarFill.parent as View).width
            val newWidth = (parentWidth * progressRatio).toInt()
            val params = progressBarFill.layoutParams
            params.width = newWidth
            progressBarFill.layoutParams = params
        }

        // Update progress text with percent or max rank message
        if (nextThreshold != null) {
            val percent = (progressRatio * 100).toInt()
            tvProgressText.text = "$percent% to $nextRank"
        } else {
            tvProgressText.text = "Rank Maxed!"
        }

        // Update rank image
        val rankDrawableRes = when (currentRank) {
            "Bronze" -> R.drawable.bronze
            "Silver" -> R.drawable.silver
            "Gold" -> R.drawable.gold
            "Platinum" -> R.drawable.platinum
            else -> R.drawable.bronze
        }
        ivRankImage.setImageResource(rankDrawableRes)

        // Show most frequent expense category
        val categoryMap = finances.groupingBy { it.name }.eachCount()
        val mostFrequent = categoryMap.maxByOrNull { it.value }?.key ?: "None"
        tvFrequentCategory.text = mostFrequent
    }
}