// GeeksforGeeks. (2020). Custom SimpleAdapter in Android with Example. Retrieved May 2, 2025, from https://www.geeksforgeeks.org/custom-simpleadapter-in-android-with-example/
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
    // Android Developers. (n.d.). ListAdapter. Retrieved June 7, 2025, from https://developer.android.com/reference/androidx/recyclerview/widget/ListAdapter

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FinanceViewHolder {
        val binding = ItemFinanceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        // Android Developers. (n.d.). LayoutInflater. Retrieved June 7, 2025, from https://developer.android.com/reference/android/view/LayoutInflater
        return FinanceViewHolder(binding)
    }
//bind the views
    override fun onBindViewHolder(holder: FinanceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FinanceViewHolder(private val binding: ItemFinanceBinding) :
        RecyclerView.ViewHolder(binding.root) {
        // Android Developers. (n.d.). RecyclerView.ViewHolder. Retrieved June 7, 2025, from https://developer.android.com/reference/androidx/recyclerview/widget/RecyclerView.ViewHolder

        fun bind(finance: FinanceEntity) {
            binding.finance = finance
            // Android Developers. (n.d.). Data Binding Library. Retrieved June 7, 2025, from https://developer.android.com/topic/libraries/data-binding

            finance.imageUri?.let { uriString ->
                val uri = android.net.Uri.parse(uriString)
                // Android Developers. (n.d.). Uri. Retrieved June 7, 2025, from https://developer.android.com/reference/android/net/Uri
                binding.imageView.setImageURI(uri)
                binding.imageView.setOnClickListener {
                    onImageClick(uriString)
                }
            }
//bind delete button
            binding.btnDelete.setOnClickListener {
                onDeleteClick(finance)
            }

            binding.executePendingBindings()
        }
    }

    class FinanceDiffCallback : DiffUtil.ItemCallback<FinanceEntity>() {
        override fun areItemsTheSame(oldItem: FinanceEntity, newItem: FinanceEntity) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: FinanceEntity, newItem: FinanceEntity) = oldItem == newItem
        // Android Developers. (n.d.). DiffUtil.ItemCallback. Retrieved June 7, 2025, from https://developer.android.com/reference/androidx/recyclerview/widget/DiffUtil.ItemCallback
    }
}
