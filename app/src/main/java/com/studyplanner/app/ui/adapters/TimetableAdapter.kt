package com.studyplanner.app.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.studyplanner.app.data.models.DaySchedule
import com.studyplanner.app.databinding.ItemDayHeaderBinding

/**
 * TimetableAdapter – one section per day.
 * Each section header shows the date label; below it is a nested
 * RecyclerView of ScheduleItems for that day (reuses ScheduleAdapter).
 */
class TimetableAdapter : ListAdapter<DaySchedule, TimetableAdapter.VH>(DIFF) {

    inner class VH(private val b: ItemDayHeaderBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(day: DaySchedule) {
            b.tvDayLabel.text = day.dateLabel
            b.tvItemCount.text = "${day.items.size} task(s)"

            // Nested RecyclerView for the items of this day
            val innerAdapter = ScheduleAdapter { /* read-only in timetable view */ }
            b.rvDayItems.layoutManager = LinearLayoutManager(b.root.context)
            b.rvDayItems.adapter = innerAdapter
            innerAdapter.submitList(day.items)

            // Highlight "Today" header
            if (day.dateLabel == "Today") {
                b.tvDayLabel.setTextColor(Color.parseColor("#2196F3"))
            } else {
                b.tvDayLabel.setTextColor(Color.parseColor("#212121"))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemDayHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<DaySchedule>() {
            override fun areItemsTheSame(a: DaySchedule, b: DaySchedule) = a.date == b.date
            override fun areContentsTheSame(a: DaySchedule, b: DaySchedule) = a == b
        }
    }
}
