package com.ssba.pantrychef.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ssba.pantrychef.R
import com.ssba.pantrychef.data.recipe_models.Instruction

class InstructionViewAdapter : ListAdapter<Instruction, InstructionViewAdapter.InstructionViewHolder>(InstructionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InstructionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_instruction_view, parent, false)
        return InstructionViewHolder(view)
    }

    override fun onBindViewHolder(holder: InstructionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class InstructionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val stepNumber: TextView = itemView.findViewById(R.id.tv_step_number)
        private val instructionText: TextView = itemView.findViewById(R.id.tv_instruction_text)

        fun bind(instruction: Instruction) {
            stepNumber.text = instruction.stepNumber.toString()
            instructionText.text = instruction.instruction
        }
    }

    private class InstructionDiffCallback : DiffUtil.ItemCallback<Instruction>() {
        override fun areItemsTheSame(oldItem: Instruction, newItem: Instruction): Boolean {
            return oldItem.stepNumber == newItem.stepNumber
        }

        override fun areContentsTheSame(oldItem: Instruction, newItem: Instruction): Boolean {
            return oldItem == newItem
        }
    }
}