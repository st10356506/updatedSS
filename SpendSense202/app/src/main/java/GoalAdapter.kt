package com.example.spendsense20.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.spendsense20.R
import com.example.spendsense20.data.Goal

class GoalAdapter(private var goals: List<Goal>) : RecyclerView.Adapter<GoalAdapter.GoalViewHolder>() {

    inner class GoalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val goalName: TextView = itemView.findViewById(R.id.tvGoalName)
        val goalAmount: TextView = itemView.findViewById(R.id.tvGoalAmount)
        val goalDate: TextView = itemView.findViewById(R.id.tvGoalDate)
        val monthlyContribution: TextView = itemView.findViewById(R.id.tvMonthlyContribution)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_goal, parent, false)
        return GoalViewHolder(view)
    }

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        val goal = goals[position]
        holder.goalName.text = goal.name
        holder.goalAmount.text = "Target: R${goal.amount}"
        holder.goalDate.text = "Due: ${goal.targetDate}"
        holder.monthlyContribution.text =
            "Contribution: R${goal.minContribution} - R${goal.maxContribution}"
    }

    override fun getItemCount(): Int = goals.size

    fun updateGoals(newGoals: List<Goal>) {
        this.goals = newGoals
        notifyDataSetChanged()
    }
}
