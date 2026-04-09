package com.studyplanner.app.ui.adapters

import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.studyplanner.app.data.models.ScheduleItem
import com.studyplanner.app.data.models.TaskType
import com.studyplanner.app.databinding.ItemScheduleBinding

/**
 * ScheduleAdapter – shows time-slot cards in the Dashboard's "Today" list.
 * Tapping a card marks that slot as complete.
 */
class ScheduleAdapter(
    private val onItemClick: (ScheduleItem) -> Unit
) : ListAdapter<ScheduleItem, ScheduleAdapter.VH>(DIFF) {

    inner class VH(private val b: ItemScheduleBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: ScheduleItem) {
            b.tvTaskTitle.text = "${TaskType.getEmoji(item.taskType)}  ${item.taskTitle}"
            b.tvTimeSlot.text  = "${item.startTime} – ${item.endTime}"
            b.tvTaskType.text  = item.taskType
            b.tvDuration.text  = "${item.durationHours}h"

            // Color the left accent stripe by task type
            try {
                b.viewAccent.setBackgroundColor(Color.parseColor(TaskType.getColorHex(item.taskType)))
                b.tvTaskType.setTextColor(Color.parseColor(TaskType.getColorHex(item.taskType)))
            } catch (_: Exception) {}

            // Strike-through if completed
            if (item.isCompleted) {
                b.tvTaskTitle.paintFlags = b.tvTaskTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                b.root.alpha = 0.5f
            } else {
                b.tvTaskTitle.paintFlags = b.tvTaskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                b.root.alpha = 1.0f
            }

            b.root.setOnClickListener { if (!item.isCompleted) onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemScheduleBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ScheduleItem>() {
            override fun areItemsTheSame(a: ScheduleItem, b: ScheduleItem) = a.id == b.id
            override fun areContentsTheSame(a: ScheduleItem, b: ScheduleItem) = a == b
        }
    }
}
