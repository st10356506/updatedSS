package com.example.spendsense20.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.spendsense20.R
import com.example.spendsense20.data.Goal

class GoalAdapter(private var goals: List<Goal>, private val deleteGoal: (Goal) -> Unit) :
    RecyclerView.Adapter<GoalAdapter.GoalViewHolder>() {
//bind views
    inner class GoalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val category: TextView = itemView.findViewById(R.id.tvGoalCategory) // Android Developers (n.d.). findViewById. Available at: https://developer.android.com/reference/android/view/View#findViewById(int)
        val goalStartDate: TextView = itemView.findViewById(R.id.tvStartGoalDate)
        val goalEndDate: TextView = itemView.findViewById(R.id.tvEndGoalDate)
        val MinContribution: TextView = itemView.findViewById(R.id.tvMinMonthlyContribution)
        val MaxContribution: TextView = itemView.findViewById(R.id.tvMaxMonthlyContribution)
        val deleteButton: ImageButton = itemView.findViewById(R.id.btnDelete) // Android Developers (n.d.). ImageButton. Available at: https://developer.android.com/reference/android/widget/ImageButton
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val view = LayoutInflater.from(parent.context) // Android Developers (n.d.). LayoutInflater.from. Available at: https://developer.android.com/reference/android/view/LayoutInflater#inflate(int,android.view.ViewGroup)
            .inflate(R.layout.item_goal, parent, false) // Android Developers (n.d.). inflate(). Available at: https://developer.android.com/reference/android/view/LayoutInflater#inflate(int,android.view.ViewGroup)
        return GoalViewHolder(view) // Kotlin Documentation (n.d.). Return statement. Available at: https://kotlinlang.org/docs/functions.html#return
    }

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        val goal = goals[position] // Kotlin Documentation (n.d.). List indexing. Available at: https://kotlinlang.org/docs/collections-overview.html
        holder.category.text = "Category: ${goal.category}" // Kotlin String Interpolation. Available at: https://kotlinlang.org/docs/basic-types.html#string-templates
        holder.goalStartDate.text = "Start Date: ${goal.startDate}"
        holder.goalEndDate.text = "End Date: ${goal.endDate}"
        holder.MinContribution.text = "Contribution: R${goal.minContribution}"
        holder.MaxContribution.text = "Contribution: R${goal.maxContribution}"

        holder.deleteButton.setOnClickListener {
            deleteGoal(goal) // Lambda expression for deletion. Kotlin Documentation (n.d.). Available at: https://kotlinlang.org/docs/lambdas.html
        }
    }
//fetch goals
    override fun getItemCount(): Int = goals.size // Kotlin shorthand syntax for property return. Available at: https://kotlinlang.org/docs/properties.html
//update goals
    fun updateGoals(newGoals: List<Goal>) {
        this.goals = newGoals // Kotlin property setter. Available at: https://kotlinlang.org/docs/properties.html
        notifyDataSetChanged() // Android Developers (n.d.). notifyDataSetChanged. Available at: https://developer.android.com/reference/androidx/recyclerview/widget/RecyclerView.Adapter#notifyDataSetChanged
    }
}
