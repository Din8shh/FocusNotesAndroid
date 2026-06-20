package com.focusnotes.app.ui.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.focusnotes.app.data.TaskEntity
import com.focusnotes.app.data.isTimerRunning
import com.focusnotes.app.data.status
import com.focusnotes.app.model.TaskStatus
import com.focusnotes.app.timer.TimerCalc
import com.focusnotes.app.ui.theme.OverdueRed
import com.focusnotes.app.ui.theme.SuccessGreen
import com.focusnotes.app.util.DateFormatting

@Composable
fun StatusChip(status: TaskStatus) {
    val (bg, fg) = when (status) {
        TaskStatus.TODO -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f) to
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        TaskStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) to
                MaterialTheme.colorScheme.primary
        TaskStatus.DONE -> SuccessGreen.copy(alpha = 0.15f) to SuccessGreen
    }
    Text(
        text = status.label,
        style = MaterialTheme.typography.labelSmall,
        color = fg,
        modifier = Modifier
            .background(bg, RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

@Composable
fun TaskRow(
    task: TaskEntity,
    now: Long,
    modifier: Modifier = Modifier,
    showsFocusIndicator: Boolean = true,
) {
    val isActive = TimerCalc.isActive(task)
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (task.status == TaskStatus.DONE)
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                else MaterialTheme.colorScheme.onSurface,
                textDecoration = if (task.status == TaskStatus.DONE) TextDecoration.LineThrough else null,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusChip(task.status)

                task.dueDate?.let { due ->
                    val overdue = DateFormatting.isOverdue(due) && task.status != TaskStatus.DONE
                    Text(
                        text = DateFormatting.formatDueDate(due),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (overdue) OverdueRed
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                if (task.totalElapsedSeconds > 0 || task.isTimerRunning) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            Icons.Filled.Timer,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = DateFormatting.formatDuration(
                                TimerCalc.totalDisplaySeconds(task, now)
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.width(4.dp))

        if (showsFocusIndicator && task.isFocused) {
            Icon(
                Icons.Filled.CenterFocusStrong,
                contentDescription = "Pinned to Focus",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }

        if (isActive) {
            Icon(
                Icons.Filled.PlayCircle,
                contentDescription = "Timer running",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
