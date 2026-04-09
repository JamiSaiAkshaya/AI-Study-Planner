package com.studyplanner.app.ui.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.studyplanner.app.R
import com.studyplanner.app.data.models.Task
import com.studyplanner.app.data.models.TaskType
import com.studyplanner.app.databinding.ActivityAddTaskBinding
import com.studyplanner.app.ui.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * AddTaskActivity – form screen where the student enters a new task.
 * After saving, TaskRepository automatically regenerates the timetable.
 */
class AddTaskActivity : AppCompatActivity() {

    private lateinit var b: ActivityAddTaskBinding
    private val vm: TaskViewModel by viewModels()
    private var deadlineMs: Long = 0L
    private val cal = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityAddTaskBinding.inflate(layoutInflater)
        setContentView(b.root)

        setupToolbar()
        setupTypeDropdown()
        setupDatePicker()
        setupSaveButton()
        observe()
    }

    private fun setupToolbar() {
        setSupportActionBar(b.toolbar)
        supportActionBar?.title = "Add New Task"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        b.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupTypeDropdown() {
        val types   = TaskType.getAllTypes()
        val adapter = ArrayAdapter(this, R.layout.item_dropdown, types)
        b.actvTaskType.setAdapter(adapter)
        b.actvTaskType.setText(types[0], false)          // default: CODING
    }

    private fun setupDatePicker() {
        b.btnPickDate.setOnClickListener {
            DatePickerDialog(
                this,
                { _, y, m, d ->
                    cal.set(y, m, d, 23, 59, 59)
                    deadlineMs = cal.timeInMillis
                    b.tvSelectedDate.text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        .format(cal.time)
                    b.tvSelectedDate.visibility = View.VISIBLE
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).apply { datePicker.minDate = System.currentTimeMillis() }.show()
        }
    }

    private fun setupSaveButton() {
        b.btnAddTask.setOnClickListener {
            if (validate()) vm.addTask(buildTask())
        }
    }

    private fun validate(): Boolean {
        val title    = b.etTitle.text.toString().trim()
        val type     = b.actvTaskType.text.toString().trim()
        val durText  = b.etDuration.text.toString().trim()

        if (title.isEmpty())  { b.tilTitle.error    = "Enter a task title";   return false }
        b.tilTitle.error = null
        if (type.isEmpty())   { b.tilTaskType.error = "Select a task type";   return false }
        b.tilTaskType.error = null
        if (durText.isEmpty()) { b.tilDuration.error = "Enter estimated hours"; return false }
        b.tilDuration.error = null
        val dur = durText.toFloatOrNull()
        if (dur == null || dur <= 0) { b.tilDuration.error = "Enter a valid number (e.g. 2 or 1.5)"; return false }

        if ((type == TaskType.ASSIGNMENT || type == TaskType.EXAM) && deadlineMs == 0L) {
            Snackbar.make(b.root, "Please set a deadline for $type", Snackbar.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun buildTask() = Task(
        title          = b.etTitle.text.toString().trim(),
        taskType       = b.actvTaskType.text.toString().trim(),
        deadline       = deadlineMs,
        estimatedHours = b.etDuration.text.toString().toFloat(),
        notes          = b.etNotes.text.toString().trim()
    )

    private fun observe() {
        vm.isLoading.observe(this) { loading ->
            b.btnAddTask.isEnabled  = !loading
            b.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }
        vm.toast.observe(this) { msg ->
            msg?.let {
                Snackbar.make(b.root, it, Snackbar.LENGTH_SHORT).show()
                vm.clearToast()
                finish()
            }
        }
    }
}
