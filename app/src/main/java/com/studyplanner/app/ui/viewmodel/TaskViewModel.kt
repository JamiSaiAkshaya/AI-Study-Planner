package com.studyplanner.app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.studyplanner.app.data.models.Task
import com.studyplanner.app.data.models.UserSettings
import com.studyplanner.app.data.repository.TaskRepository
import kotlinx.coroutines.launch

/**
 * TaskViewModel – the single ViewModel shared across all screens.
 *
 * Exposes:
 *   • allTasks        – LiveData<List<Task>>       – all tasks (real-time)
 *   • todaySchedule   – LiveData<List<ScheduleItem>> – today's slots (real-time)
 *   • fullSchedule    – LiveData<List<ScheduleItem>> – all future slots (real-time)
 *   • isLoading       – show/hide progress bars
 *   • toastMessage    – one-shot messages (success / error)
 */
class TaskViewModel : ViewModel() {

    private val repo = TaskRepository()

    val allTasks      = repo.allTasksFlow().asLiveData()
    val todaySchedule = repo.todayScheduleFlow().asLiveData()
    val fullSchedule  = repo.fullScheduleFlow().asLiveData()

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _toast = MutableLiveData<String?>()
    val toast: LiveData<String?> = _toast

    // ── Actions ───────────────────────────────────────────────────────────────

    fun addTask(task: Task) = launch {
        repo.addTask(task)
            .onSuccess { _toast.value = "Task added! Timetable updated ✅" }
            .onFailure { _toast.value = "Failed to add task. Try again." }
    }

    fun completeTask(id: String) = launch {
        repo.completeTask(id)
            .onSuccess { _toast.value = "Task marked complete 🎉" }
    }

    fun deleteTask(id: String) = launch { repo.deleteTask(id) }

    fun completeScheduleItem(id: String) =
        viewModelScope.launch { repo.completeScheduleItem(id) }

    fun saveSettings(s: UserSettings) = launch {
        repo.saveSettings(s)
            .onSuccess { _toast.value = "Settings saved! Timetable refreshed 🗓" }
    }

    fun regenerate() = launch {
        repo.regenerate()
            .onSuccess { _toast.value = "Timetable regenerated 🔄" }
    }

    fun clearToast() { _toast.value = null }

    // ── Private helpers ───────────────────────────────────────────────────────

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            block()
            _isLoading.value = false
        }
    }
}
