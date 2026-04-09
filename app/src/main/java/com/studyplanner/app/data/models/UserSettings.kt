package com.studyplanner.app.data.models

/**
 * UserSettings - Student preferences that drive the scheduler.
 * Stored as a single document "user_settings" in the "settings" collection.
 */
data class UserSettings(
    val availableHoursPerDay: Float = 4f,   // Total study hours the student has per day
    val studyStartHour: Int = 8,            // 8 = 8 AM
    val maxTasksPerDay: Int = 3,            // Stress-free cap: never more than 3 tasks/day
    val bufferMinutes: Int = 30             // Break between consecutive tasks
) {
    constructor() : this(4f, 8, 3, 30)
}
