package com.ssba.pantrychef.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ssba.pantrychef.R
import com.ssba.pantrychef.data.api_data_models.RecipeInstruction

class SpoonacularInstructionAdapter : ListAdapter<RecipeInstruction, SpoonacularInstructionAdapter.InstructionViewHolder>(InstructionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InstructionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_spoonacular_instruction, parent, false)
        return InstructionViewHolder(view)
    }

    override fun onBindViewHolder(holder: InstructionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class InstructionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvStepNumber: TextView = itemView.findViewById(R.id.tv_step_number)
        private val tvInstruction: TextView = itemView.findViewById(R.id.tv_instruction)

        fun bind(instruction: RecipeInstruction) {
            tvStepNumber.text = instruction.stepNumber.toString()
            tvInstruction.text = instruction.instruction
        }
    }

    private class InstructionDiffCallback : DiffUtil.ItemCallback<RecipeInstruction>() {
        override fun areItemsTheSame(oldItem: RecipeInstruction, newItem: RecipeInstruction): Boolean {
            return oldItem.stepNumber == newItem.stepNumber
        }

        override fun areContentsTheSame(oldItem: RecipeInstruction, newItem: RecipeInstruction): Boolean {
            return oldItem == newItem
        }
    }
}