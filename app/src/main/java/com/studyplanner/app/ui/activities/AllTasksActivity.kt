package com.studyplanner.app.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.studyplanner.app.databinding.ActivityAllTasksBinding
import com.studyplanner.app.ui.adapters.TaskAdapter
import com.studyplanner.app.ui.viewmodel.TaskViewModel

/**
 * AllTasksActivity – lists every task with complete and delete actions.
 */
class AllTasksActivity : AppCompatActivity() {

    private lateinit var b: ActivityAllTasksBinding
    private val vm: TaskViewModel by viewModels()
    private lateinit var adapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityAllTasksBinding.inflate(layoutInflater)
        setContentView(b.root)

        setSupportActionBar(b.toolbar)
        supportActionBar?.title = "All Tasks"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        b.toolbar.setNavigationOnClickListener { finish() }

        adapter = TaskAdapter(
            onComplete = { task ->
                if (!task.isCompleted) {
                    MaterialAlertDialogBuilder(this)
                        .setTitle("Mark Complete?")
                        .setMessage("Mark \"${task.title}\" as done?")
                        .setPositiveButton("Yes") { _, _ -> vm.completeTask(task.id) }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            },
            onDelete = { task ->
                MaterialAlertDialogBuilder(this)
                    .setTitle("Delete Task?")
                    .setMessage("Delete \"${task.title}\"? The timetable will update.")
                    .setPositiveButton("Delete") { _, _ -> vm.deleteTask(task.id) }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )
        b.rvTasks.layoutManager = LinearLayoutManager(this)
        b.rvTasks.adapter = adapter

        b.fabAddTask.setOnClickListener {
            startActivity(Intent(this, AddTaskActivity::class.java))
        }

        vm.allTasks.observe(this) { tasks ->
            adapter.submitList(tasks)
            b.tvEmpty.visibility  = if (tasks.isEmpty()) View.VISIBLE else View.GONE
            b.rvTasks.visibility  = if (tasks.isEmpty()) View.GONE   else View.VISIBLE
        }

        vm.isLoading.observe(this) { loading ->
            b.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        vm.toast.observe(this) { msg ->
            msg?.let { Snackbar.make(b.root, it, Snackbar.LENGTH_SHORT).show(); vm.clearToast() }
        }
    }
}
