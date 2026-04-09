package com.studyplanner.app.data.models

/**
 * Task - Represents a single academic task added by the student.
 * Stored as a Firestore document in the "tasks" collection.
 */
data class Task(
    var id: String = "",
    val title: String = "",
    val taskType: String = "",          // CODING | ASSIGNMENT | PROJECT | EXAM
    val deadline: Long = 0L,            // Epoch ms; 0 = no deadline
    val estimatedHours: Float = 1f,     // Student's estimate of total work
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val priority: Float = 0f,           // Computed by SchedulerEngine
    val notes: String = ""
) {
    // Firestore requires a no-arg constructor
    constructor() : this("", "", "", 0L, 1f, false, 0L, 0f, "")
}

/** Central definitions for task categories used throughout the app */
object TaskType {
    const val CODING     = "CODING"
    const val ASSIGNMENT = "ASSIGNMENT"
    const val PROJECT    = "PROJECT"
    const val EXAM       = "EXAM"

    /** Higher weight = more urgent in the scheduler */
    fun getWeight(type: String): Float = when (type) {
        EXAM       -> 4.0f
        ASSIGNMENT -> 3.0f
        CODING     -> 2.0f
        PROJECT    -> 1.0f
        else       -> 1.0f
    }

    fun getEmoji(type: String): String = when (type) {
        EXAM       -> "📝"
        ASSIGNMENT -> "📋"
        CODING     -> "💻"
        PROJECT    -> "🚀"
        else       -> "📌"
    }

    /** Hex color for each type – used in RecyclerView item chips */
    fun getColorHex(type: String): String = when (type) {
        EXAM       -> "#F44336"   // Red
        ASSIGNMENT -> "#FF9800"   // Orange
        CODING     -> "#2196F3"   // Blue
        PROJECT    -> "#4CAF50"   // Green
        else       -> "#9E9E9E"
    }

    fun getAllTypes(): List<String> = listOf(CODING, ASSIGNMENT, PROJECT, EXAM)
}
