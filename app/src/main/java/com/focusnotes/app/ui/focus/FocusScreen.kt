package com.focusnotes.app.ui.focus

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.focusnotes.app.data.TaskEntity
import com.focusnotes.app.data.activeTimerMode
import com.focusnotes.app.data.status
import com.focusnotes.app.model.TaskStatus
import com.focusnotes.app.model.TimerMode
import com.focusnotes.app.timer.TimerCalc
import com.focusnotes.app.ui.TasksViewModel
import com.focusnotes.app.ui.tasks.TaskRow
import com.focusnotes.app.ui.timer.TimerCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusScreen(
    tasks: List<TaskEntity>,
    now: Long,
    viewModel: TasksViewModel,
    onOpenTask: (String) -> Unit,
) {
    val focusTasks = tasks
        .filter { it.isFocused }
        .sortedWith(
            compareByDescending<TaskEntity> { TimerCalc.isActive(it) }
                .thenByDescending { it.status == TaskStatus.IN_PROGRESS }
                .thenByDescending { it.updatedAt }
        )
    val activeTask = focusTasks.firstOrNull { TimerCalc.isActive(it) } ?: focusTasks.firstOrNull()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Focus") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            activeTask?.let {
                TimerCard(task = it, now = now, viewModel = viewModel)
            }

            if (focusTasks.isEmpty()) {
                EmptyFocusState()
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Pinned Tasks",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                    ) {
                        focusTasks.forEachIndexed { index, task ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TaskRow(
                                    task = task,
                                    now = now,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onOpenTask(task.id) }
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                                )
                                IconButton(
                                    onClick = {
                                        viewModel.toggleTimer(
                                            task,
                                            task.activeTimerMode ?: TimerMode.ELAPSED
                                        )
                                    },
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = if (TimerCalc.isActive(task))
                                            Icons.Filled.PauseCircle else Icons.Filled.PlayCircle,
                                        contentDescription = if (TimerCalc.isActive(task)) "Pause" else "Start",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                            if (index != focusTasks.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 16.dp),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyFocusState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            Icons.Filled.CenterFocusStrong,
            contentDescription = null,
            modifier = Modifier.size(36.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Text("Nothing in Focus", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(
            "Pin tasks from All Tasks to work on them here.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}
