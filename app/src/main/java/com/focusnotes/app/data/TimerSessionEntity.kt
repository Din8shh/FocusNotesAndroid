package com.focusnotes.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.focusnotes.app.model.TimerMode
import java.util.UUID

/**
 * Room equivalent of the iOS SwiftData `TimerSession` model. Deleting the parent
 * task cascades to its sessions, matching the SwiftData `.cascade` delete rule.
 */
@Entity(
    tableName = "sessions",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("taskId")]
)
data class TimerSessionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val taskId: String,
    val modeRaw: String,
    val durationSeconds: Int,
    val startedAt: Long,
    val endedAt: Long? = null,
)

val TimerSessionEntity.mode: TimerMode
    get() = TimerMode.fromRaw(modeRaw) ?: TimerMode.ELAPSED
