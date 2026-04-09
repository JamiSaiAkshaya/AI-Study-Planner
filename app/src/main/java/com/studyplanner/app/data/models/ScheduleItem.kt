package com.studyplanner.app.data.models

/**
 * ScheduleItem - One time-block in the generated timetable.
 * Stored in the "schedule" Firestore collection.
 */
data class ScheduleItem(
    var id: String = "",
    val taskId: String = "",
    val taskTitle: String = "",
    val taskType: String = "",
    val date: Long = 0L,            // Midnight timestamp of the day
    val startTime: String = "",     // e.g. "09:00 AM"
    val endTime: String = "",       // e.g. "11:00 AM"
    val durationHours: Float = 1f,
    val isCompleted: Boolean = false,
    val generatedAt: Long = System.currentTimeMillis()
) {
    constructor() : this("", "", "", "", 0L, "", "", 1f, false, 0L)
}

/**
 * DaySchedule - Convenience wrapper used only in the UI.
 * Groups all ScheduleItems that fall on the same day.
 */
data class DaySchedule(
    val date: Long,
    val dateLabel: String,          // "Today", "Tomorrow", "Mon, Apr 14"
    val items: List<ScheduleItem>
)
