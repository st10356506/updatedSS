//GeeksforGeeks. “Custom SimpleAdapter in Android with Example.” GeeksforGeeks, 28 Nov. 2020, www.geeksforgeeks.org/custom-simpleadapter-in-android-with-example/. Accessed 2 May 2025.
package com.example.spendsense20

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.spendsense20.databinding.ItemFinanceBinding

class FinanceAdapter(
    private val onImageClick: (String) -> Unit,
    private val onDeleteClick: (FinanceEntity) -> Unit
) : ListAdapter<FinanceEntity, FinanceAdapter.FinanceViewHolder>(FinanceDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FinanceViewHolder {
        val binding = ItemFinanceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FinanceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FinanceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FinanceViewHolder(private val binding: ItemFinanceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(finance: FinanceEntity) {
            binding.finance = finance

            // Image click
            finance.imageUri?.let { uriString ->
                val uri = android.net.Uri.parse(uriString)
                binding.imageView.setImageURI(uri)
                binding.imageView.setOnClickListener {
                    onImageClick(uriString)
                }
            }

            // Delete button
            binding.btnDelete.setOnClickListener {
                onDeleteClick(finance)
            }

            binding.executePendingBindings()
        }
    }

    class FinanceDiffCallback : DiffUtil.ItemCallback<FinanceEntity>() {
        override fun areItemsTheSame(oldItem: FinanceEntity, newItem: FinanceEntity) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: FinanceEntity, newItem: FinanceEntity) = oldItem == newItem
    }
}