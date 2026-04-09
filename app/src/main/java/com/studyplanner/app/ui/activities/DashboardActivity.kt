package com.studyplanner.app.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.studyplanner.app.databinding.ActivityDashboardBinding
import com.studyplanner.app.ui.adapters.ScheduleAdapter
import com.studyplanner.app.ui.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * DashboardActivity – the home screen.
 * Shows today's timetable, summary stats, and navigation buttons.
 */
class DashboardActivity : AppCompatActivity() {

    private lateinit var b: ActivityDashboardBinding
    private val vm: TaskViewModel by viewModels()
    private lateinit var scheduleAdapter: ScheduleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(b.root)

        setupDate()
        setupRecyclerView()
        setupButtons()
        observe()
    }

    private fun setupDate() {
        b.tvDate.text = SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date())
    }

    private fun setupRecyclerView() {
        scheduleAdapter = ScheduleAdapter { item ->
            vm.completeScheduleItem(item.id)
        }
        b.rvTodaySchedule.apply {
            layoutManager = LinearLayoutManager(this@DashboardActivity)
            adapter = scheduleAdapter
        }
    }

    private fun setupButtons() {
        b.fabAddTask.setOnClickListener {
            startActivity(Intent(this, AddTaskActivity::class.java))
        }
        b.btnViewTimetable.setOnClickListener {
            startActivity(Intent(this, TimetableActivity::class.java))
        }
        b.btnAllTasks.setOnClickListener {
            startActivity(Intent(this, AllTasksActivity::class.java))
        }
        b.btnRegenerate.setOnClickListener {
            vm.regenerate()
        }
    }

    private fun observe() {
        vm.todaySchedule.observe(this) { items ->
            scheduleAdapter.submitList(items)
            b.tvTaskCount.text = "${items.size} task(s) today"
            b.tvEmptySchedule.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            b.rvTodaySchedule.visibility  = if (items.isEmpty()) View.GONE  else View.VISIBLE
        }

        vm.allTasks.observe(this) { tasks ->
            b.tvPendingCount.text   = "${tasks.count { !it.isCompleted }}"
            b.tvCompletedCount.text = "${tasks.count { it.isCompleted }}"
        }

        vm.isLoading.observe(this) { loading ->
            b.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        vm.toast.observe(this) { msg ->
            msg?.let { Snackbar.make(b.root, it, Snackbar.LENGTH_SHORT).show(); vm.clearToast() }
        }
    }
}
