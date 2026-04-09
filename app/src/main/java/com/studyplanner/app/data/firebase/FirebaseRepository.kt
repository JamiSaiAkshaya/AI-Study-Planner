package com.studyplanner.app.data.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.studyplanner.app.data.models.ScheduleItem
import com.studyplanner.app.data.models.Task
import com.studyplanner.app.data.models.UserSettings
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * FirebaseRepository – all Firestore read/write operations.
 *
 * Collections:
 *   "tasks"    → one document per Task
 *   "schedule" → one document per ScheduleItem (replaced on every regeneration)
 *   "settings" → single document "user_settings"
 */
class FirebaseRepository {

    private val db         = FirebaseFirestore.getInstance()
    private val tasksColl  = db.collection("tasks")
    private val schedColl  = db.collection("schedule")
    private val settColl   = db.collection("settings")

    // ── Tasks ────────────────────────────────────────────────────────────────

    suspend fun addTask(task: Task): Result<String> = runCatching {
        val ref = tasksColl.add(task).await()
        tasksColl.document(ref.id).update("id", ref.id).await()
        ref.id
    }

    /** Real-time stream of all tasks (newest first). */
    fun allTasksFlow(): Flow<List<Task>> = callbackFlow {
        val sub = tasksColl
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                trySend(snap?.documents?.mapNotNull {
                    it.toObject(Task::class.java)?.copy(id = it.id)
                } ?: emptyList())
            }
        awaitClose { sub.remove() }
    }

    /** One-shot fetch used by the scheduler. */
    suspend fun allTasksOnce(): List<Task> = runCatching {
        tasksColl.get().await().documents.mapNotNull {
            it.toObject(Task::class.java)?.copy(id = it.id)
        }
    }.getOrDefault(emptyList())

    suspend fun markTaskCompleted(id: String): Result<Unit> = runCatching {
        tasksColl.document(id).update("isCompleted", true).await()
    }

    suspend fun deleteTask(id: String): Result<Unit> = runCatching {
        tasksColl.document(id).delete().await()
    }

    // ── Schedule ─────────────────────────────────────────────────────────────

    /**
     * Atomically delete the old schedule and write the new one.
     * Called every time the SchedulerEngine produces a new plan.
     */
    suspend fun saveSchedule(items: List<ScheduleItem>): Result<Unit> = runCatching {
        // Delete existing schedule
        val old = schedColl.get().await()
        val batch = db.batch()
        old.documents.forEach { batch.delete(it.reference) }
        batch.commit().await()

        // Insert new items
        for (item in items) {
            val ref  = schedColl.document()
            val copy = item.copy(id = ref.id)
            ref.set(copy).await()
        }
    }

    /** Real-time stream of today's schedule items. */
    fun todayScheduleFlow(): Flow<List<ScheduleItem>> = callbackFlow {
        val start = todayMidnight()
        val end   = start + 86_400_000L
        val sub = schedColl
            .whereGreaterThanOrEqualTo("date", start)
            .whereLessThan("date", end)
            .orderBy("date").orderBy("startTime")
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                trySend(snap?.documents?.mapNotNull {
                    it.toObject(ScheduleItem::class.java)
                } ?: emptyList())
            }
        awaitClose { sub.remove() }
    }

    /** Real-time stream of the full timetable (all days). */
    fun fullScheduleFlow(): Flow<List<ScheduleItem>> = callbackFlow {
        val sub = schedColl
            .orderBy("date").orderBy("startTime")
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                trySend(snap?.documents?.mapNotNull {
                    it.toObject(ScheduleItem::class.java)
                } ?: emptyList())
            }
        awaitClose { sub.remove() }
    }

    suspend fun markScheduleItemDone(id: String): Result<Unit> = runCatching {
        schedColl.document(id).update("isCompleted", true).await()
    }

    // ── Settings ─────────────────────────────────────────────────────────────

    suspend fun saveSettings(s: UserSettings): Result<Unit> = runCatching {
        settColl.document("user_settings").set(s).await()
    }

    suspend fun getSettings(): UserSettings = runCatching {
        settColl.document("user_settings").get().await()
            .toObject(UserSettings::class.java) ?: UserSettings()
    }.getOrDefault(UserSettings())

    // ── Helper ────────────────────────────────────────────────────────────────

    private fun todayMidnight(): Long {
        val c = java.util.Calendar.getInstance()
        c.set(java.util.Calendar.HOUR_OF_DAY, 0)
        c.set(java.util.Calendar.MINUTE, 0)
        c.set(java.util.Calendar.SECOND, 0)
        c.set(java.util.Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }
}
