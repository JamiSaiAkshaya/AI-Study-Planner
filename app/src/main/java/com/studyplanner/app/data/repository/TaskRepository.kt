package com.studyplanner.app.data.repository

import com.studyplanner.app.data.firebase.FirebaseRepository
import com.studyplanner.app.data.models.ScheduleItem
import com.studyplanner.app.data.models.Task
import com.studyplanner.app.data.models.UserSettings
import com.studyplanner.app.scheduler.SchedulerEngine
import kotlinx.coroutines.flow.Flow

/**
 * TaskRepository – bridges the ViewModel and the Firebase layer.
 *
 * Every mutating operation (add / complete / delete / settings-save)
 * ends with a call to [regenerate], which re-runs the scheduler and
 * writes the fresh timetable to Firestore.  That Firestore write is
 * picked up by the real-time Flows, so the UI updates automatically.
 */
class TaskRepository {

    private val fb = FirebaseRepository()

    // ── Exposed Flows (UI observes these) ────────────────────────────────────

    fun allTasksFlow(): Flow<List<Task>>             = fb.allTasksFlow()
    fun todayScheduleFlow(): Flow<List<ScheduleItem>> = fb.todayScheduleFlow()
    fun fullScheduleFlow(): Flow<List<ScheduleItem>>  = fb.fullScheduleFlow()

    // ── Task operations ───────────────────────────────────────────────────────

    suspend fun addTask(task: Task): Result<Unit> {
        val r = fb.addTask(task)
        if (r.isSuccess) regenerate()
        return r.map { }
    }

    suspend fun completeTask(id: String): Result<Unit> {
        val r = fb.markTaskCompleted(id)
        if (r.isSuccess) regenerate()
        return r
    }

    suspend fun deleteTask(id: String): Result<Unit> {
        val r = fb.deleteTask(id)
        if (r.isSuccess) regenerate()
        return r
    }

    suspend fun completeScheduleItem(id: String) = fb.markScheduleItemDone(id)

    // ── Settings ──────────────────────────────────────────────────────────────

    suspend fun saveSettings(s: UserSettings): Result<Unit> {
        val r = fb.saveSettings(s)
        if (r.isSuccess) regenerate()
        return r
    }

    suspend fun getSettings(): UserSettings = fb.getSettings()

    // ── Core: dynamic timetable regeneration ──────────────────────────────────

    /**
     * Fetches all tasks + settings, runs the SchedulerEngine,
     * then overwrites the "schedule" collection in Firestore.
     *
     * Because todayScheduleFlow / fullScheduleFlow are Firestore
     * snapshot listeners, the UI receives the new timetable instantly.
     */
    suspend fun regenerate(): Result<Unit> = runCatching {
        val tasks    = fb.allTasksOnce()
        val settings = fb.getSettings()
        val schedule = SchedulerEngine.generateSchedule(tasks, settings)
        fb.saveSchedule(schedule).getOrThrow()
    }
}
