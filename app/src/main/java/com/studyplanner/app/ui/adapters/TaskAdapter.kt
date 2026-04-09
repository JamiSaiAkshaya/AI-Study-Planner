package com.studyplanner.app.ui.adapters

import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.studyplanner.app.data.models.Task
import com.studyplanner.app.data.models.TaskType
import com.studyplanner.app.databinding.ItemTaskBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * TaskAdapter – shows every task card in AllTasksActivity.
 * Each card has a ✓ complete button and a 🗑 delete button.
 */
class TaskAdapter(
    private val onComplete: (Task) -> Unit,
    private val onDelete:   (Task) -> Unit
) : ListAdapter<Task, TaskAdapter.VH>(DIFF) {

    inner class VH(private val b: ItemTaskBinding) : RecyclerView.ViewHolder(b.root) {
        private val dateFmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        fun bind(task: Task) {
            b.tvTitle.text    = "${TaskType.getEmoji(task.taskType)}  ${task.title}"
            b.tvType.text     = task.taskType
            b.tvDuration.text = "${task.estimatedHours}h"

            b.tvDeadline.text = if (task.deadline > 0)
                "Due: ${dateFmt.format(Date(task.deadline))}"
            else "No deadline"

            try {
                val color = Color.parseColor(TaskType.getColorHex(task.taskType))
                b.viewAccent.setBackgroundColor(color)
                b.tvType.setTextColor(color)
            } catch (_: Exception) {}

            if (task.isCompleted) {
                b.tvTitle.paintFlags = b.tvTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                b.root.alpha = 0.5f
                b.btnComplete.isEnabled = false
            } else {
                b.tvTitle.paintFlags = b.tvTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                b.root.alpha = 1.0f
                b.btnComplete.isEnabled = true
            }

            b.btnComplete.setOnClickListener { onComplete(task) }
            b.btnDelete.setOnClickListener   { onDelete(task) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Task>() {
            override fun areItemsTheSame(a: Task, b: Task) = a.id == b.id
            override fun areContentsTheSame(a: Task, b: Task) = a == b
        }
    }
}
