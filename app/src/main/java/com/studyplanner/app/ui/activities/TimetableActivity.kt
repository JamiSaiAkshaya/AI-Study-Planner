package com.studyplanner.app.ui.activities

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.studyplanner.app.data.models.DaySchedule
import com.studyplanner.app.data.models.ScheduleItem
import com.studyplanner.app.databinding.ActivityTimetableBinding
import com.studyplanner.app.ui.adapters.TimetableAdapter
import com.studyplanner.app.ui.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * TimetableActivity – shows the full generated schedule grouped by day.
 */
class TimetableActivity : AppCompatActivity() {

    private lateinit var b: ActivityTimetableBinding
    private val vm: TaskViewModel by viewModels()
    private lateinit var adapter: TimetableAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityTimetableBinding.inflate(layoutInflater)
        setContentView(b.root)

        setSupportActionBar(b.toolbar)
        supportActionBar?.title = "My Timetable"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        b.toolbar.setNavigationOnClickListener { finish() }

        adapter = TimetableAdapter()
        b.rvTimetable.layoutManager = LinearLayoutManager(this)
        b.rvTimetable.adapter = adapter

        vm.fullSchedule.observe(this) { items ->
            val days = groupByDay(items)
            adapter.submitList(days)
            b.tvEmpty.visibility      = if (days.isEmpty()) View.VISIBLE else View.GONE
            b.rvTimetable.visibility  = if (days.isEmpty()) View.GONE   else View.VISIBLE
        }

        vm.isLoading.observe(this) { loading ->
            b.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }
    }

    private fun groupByDay(items: List<ScheduleItem>): List<DaySchedule> {
        val today    = midnightOf(System.currentTimeMillis())
        val tomorrow = today + 86_400_000L
        val fmt      = SimpleDateFormat("EEE, MMM d", Locale.getDefault())

        return items
            .groupBy { it.date }
            .map { (date, dayItems) ->
                val label = when {
                    date in today until tomorrow             -> "Today"
                    date in tomorrow until tomorrow + 86_400_000L -> "Tomorrow"
                    else -> fmt.format(Date(date))
                }
                DaySchedule(date, label, dayItems)
            }
            .sortedBy { it.date }
    }

    private fun midnightOf(ms: Long): Long {
        val c = java.util.Calendar.getInstance()
        c.timeInMillis = ms
        c.set(java.util.Calendar.HOUR_OF_DAY, 0)
        c.set(java.util.Calendar.MINUTE, 0)
        c.set(java.util.Calendar.SECOND, 0)
        c.set(java.util.Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }
}
