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

    inner class GoalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val category: TextView = itemView.findViewById(R.id.tvGoalCategory)
        val goalStartDate: TextView = itemView.findViewById(R.id.tvStartGoalDate)
        val goalEndDate: TextView = itemView.findViewById(R.id.tvEndGoalDate)
        val MinContribution: TextView = itemView.findViewById(R.id.tvMinMonthlyContribution)
        val MaxContribution: TextView = itemView.findViewById(R.id.tvMaxMonthlyContribution)
        val deleteButton: ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_goal, parent, false)
        return GoalViewHolder(view)
    }

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        val goal = goals[position]
        holder.category.text = "Category: ${goal.category}"
        holder.goalStartDate.text = "Start Date: ${goal.startDate}"
        holder.goalEndDate.text = "End Date: ${goal.endDate}"
        holder.MinContribution.text = "Contribution: R${goal.minContribution}"
        holder.MaxContribution.text = "Contribution: R${goal.maxContribution}"

        // Set up the delete button click listener
        holder.deleteButton.setOnClickListener {
            // Trigger the delete action passed from the Fragment or Activity
            deleteGoal(goal)
        }
    }

    override fun getItemCount(): Int = goals.size

    fun updateGoals(newGoals: List<Goal>) {
        this.goals = newGoals
        notifyDataSetChanged()
    }

}
