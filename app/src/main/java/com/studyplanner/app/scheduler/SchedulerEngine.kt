package com.studyplanner.app.scheduler

import com.studyplanner.app.data.models.ScheduleItem
import com.studyplanner.app.data.models.Task
import com.studyplanner.app.data.models.TaskType
import com.studyplanner.app.data.models.UserSettings
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * SchedulerEngine – Pure Kotlin object that contains the entire scheduling algorithm.
 *
 * HOW IT WORKS (step-by-step):
 *  1. Filter out already-completed tasks.
 *  2. Score every task with a priority formula.
 *  3. Sort tasks: highest score first.
 *  4. Walk through calendar days starting from today.
 *     For each day, fill available hours with the highest-priority tasks,
 *     respecting stress-free constraints (max tasks/day, max hours/day, breaks).
 *  5. Return the resulting list of ScheduleItems for Firestore.
 *
 * PRIORITY FORMULA:
 *   score = (deadlineUrgency × 0.6) + (taskTypeWeight × 0.4)
 *
 *   deadlineUrgency : 10 → overdue, 9 → due today, … 1 → 8+ days away or no deadline
 *   taskTypeWeight  : EXAM=4, ASSIGNMENT=3, CODING=2, PROJECT=1
 */
object SchedulerEngine {

    private const val MAX_DAYS_AHEAD = 14  // Schedule at most 2 weeks out

    // ── Public API ──────────────────────────────────────────────────────────

    fun generateSchedule(tasks: List<Task>, settings: UserSettings): List<ScheduleItem> {
        val pending = tasks.filter { !it.isCompleted }
        if (pending.isEmpty()) return emptyList()

        // Score & sort
        val sorted = pending
            .map { it.copy(priority = score(it)) }
            .sortedByDescending { it.priority }

        return allocate(sorted, settings)
    }

    // ── Priority scoring ─────────────────────────────────────────────────────

    private fun score(task: Task): Float {
        val urgency = deadlineUrgency(task.deadline)
        val weight  = TaskType.getWeight(task.taskType)
        return (urgency * 0.6f) + (weight * 0.4f)
    }

    private fun deadlineUrgency(deadline: Long): Float {
        if (deadline == 0L) return 1.0f          // No deadline → lowest urgency
        val daysLeft = TimeUnit.MILLISECONDS.toDays(deadline - System.currentTimeMillis())
        return when {
            daysLeft < 0  -> 10.0f               // Overdue – must act now
            daysLeft == 0L -> 9.0f               // Due today
            daysLeft == 1L -> 8.0f
            daysLeft == 2L -> 7.0f
            daysLeft == 3L -> 6.0f
            daysLeft == 4L -> 5.0f
            daysLeft == 5L -> 4.0f
            daysLeft == 6L -> 3.0f
            daysLeft == 7L -> 2.0f
            else           -> 1.0f               // 8+ days → low urgency
        }
    }

    // ── Time-slot allocation ──────────────────────────────────────────────────

    private fun allocate(sorted: List<Task>, s: UserSettings): List<ScheduleItem> {
        val items   = mutableListOf<ScheduleItem>()
        // Track remaining hours for every task (a task may span multiple days)
        val remaining = sorted.associate { it.id to effectiveHours(it) }.toMutableMap()

        val todayStart = dayStart(System.currentTimeMillis())

        for (dayOffset in 0 until MAX_DAYS_AHEAD) {
            val dayMs = todayStart + dayOffset * 86_400_000L

            // Any work still pending?
            val stillPending = sorted.filter { (remaining[it.id] ?: 0f) > 0.01f }
            if (stillPending.isEmpty()) break

            var hoursUsed      = 0f
            var tasksScheduled = 0
            var currentHour    = s.studyStartHour

            for (task in stillPending) {
                if (tasksScheduled >= s.maxTasksPerDay) break          // Stress-free cap
                val avail = s.availableHoursPerDay - hoursUsed
                if (avail < 0.5f) break                                // Not enough time left today

                val rem     = remaining[task.id] ?: 0f
                val session = minOf(rem, avail, maxSession(task.taskType))
                if (session < 0.5f) continue

                val startStr = fmtTime(currentHour, 0)
                val endMin   = (session * 60).toInt()
                val endStr   = fmtTime(currentHour + endMin / 60, endMin % 60)

                items += ScheduleItem(
                    taskId        = task.id,
                    taskTitle     = task.title,
                    taskType      = task.taskType,
                    date          = dayMs,
                    startTime     = startStr,
                    endTime       = endStr,
                    durationHours = session
                )

                remaining[task.id] = rem - session
                hoursUsed          += session
                tasksScheduled     += 1

                // Advance clock: session length + buffer
                val totalMins = endMin + s.bufferMinutes
                currentHour  += totalMins / 60
            }
        }
        return items
    }

    // CODING is a daily habit → cap at 1.5 h regardless of what the student entered
    private fun effectiveHours(task: Task): Float =
        if (task.taskType == TaskType.CODING) minOf(task.estimatedHours, 1.5f)
        else task.estimatedHours

    // Prevent marathon sessions – max 2 h per task per day
    private fun maxSession(type: String): Float = when (type) {
        TaskType.CODING -> 1.5f
        else            -> 2.0f
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Midnight of the day containing [ts] */
    private fun dayStart(ts: Long): Long =
        Calendar.getInstance().apply {
            timeInMillis = ts
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0);      set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    /** Format hour + minute as "hh:mm AM/PM" */
    private fun fmtTime(hour: Int, minute: Int): String {
        val h    = hour % 24
        val ampm = if (h < 12) "AM" else "PM"
        val disp = when { h == 0 -> 12; h > 12 -> h - 12; else -> h }
        return "%02d:%02d %s".format(disp, minute, ampm)
    }
}
