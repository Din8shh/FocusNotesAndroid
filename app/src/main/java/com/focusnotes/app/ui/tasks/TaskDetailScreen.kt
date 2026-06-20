package com.focusnotes.app.ui.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.focusnotes.app.data.TimerSessionEntity
import com.focusnotes.app.data.mode
import com.focusnotes.app.data.status
import com.focusnotes.app.model.TaskStatus
import com.focusnotes.app.ui.TasksViewModel
import com.focusnotes.app.ui.timer.TimerCard
import com.focusnotes.app.util.DateFormatting

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: String,
    now: Long,
    viewModel: TasksViewModel,
    onBack: () -> Unit,
) {
    val task by viewModel.observeTask(taskId).collectAsStateWithLifecycle(initialValue = null)
    val sessions by viewModel.observeSessions(taskId)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    val currentTask = task

    // Editable text kept in local state, keyed on task id so DB re-emissions
    // don't move the cursor.
    var titleText by remember(taskId) { mutableStateOf(currentTask?.title ?: "") }
    var notesText by remember(taskId) { mutableStateOf(currentTask?.notes ?: "") }
    LaunchedEffect(currentTask?.id) {
        currentTask?.let {
            titleText = it.title
            notesText = it.notes
        }
    }

    // Auto-delete empty task / persist trimmed title when leaving.
    val latestTitle by rememberUpdatedState(titleText)
    androidx.compose.runtime.DisposableEffect(taskId) {
        onDispose {
            val trimmed = latestTitle.trim()
            if (trimmed.isEmpty()) {
                viewModel.deleteById(taskId)
            } else {
                viewModel.updateTitle(taskId, trimmed)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.deleteById(taskId)
                        onBack()
                    }) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (currentTask == null) {
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Title
            FieldLabel("Title")
            OutlinedTextField(
                value = titleText,
                onValueChange = {
                    titleText = it
                    viewModel.updateTitle(taskId, it)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = MaterialTheme.typography.titleMedium
            )

            // Status
            FieldLabel("Status")
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                TaskStatus.entries.forEachIndexed { index, status ->
                    SegmentedButton(
                        selected = currentTask.status == status,
                        onClick = { viewModel.setStatus(taskId, status) },
                        shape = SegmentedButtonDefaults.itemShape(index, TaskStatus.entries.size),
                        label = { Text(status.label) }
                    )
                }
            }

            // Add to Focus
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Add to Focus", modifier = Modifier.weight(1f))
                Switch(
                    checked = currentTask.isFocused,
                    onCheckedChange = { viewModel.setFocused(taskId, it) }
                )
            }

            // Due date
            val hasDueDate = currentTask.dueDate != null
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Due date", modifier = Modifier.weight(1f))
                Switch(
                    checked = hasDueDate,
                    onCheckedChange = { enabled ->
                        viewModel.setDueDate(
                            taskId,
                            if (enabled) (currentTask.dueDate ?: System.currentTimeMillis()) else null
                        )
                    }
                )
            }

            if (hasDueDate) {
                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = currentTask.dueDate
                )
                LaunchedEffect(datePickerState) {
                    snapshotFlow { datePickerState.selectedDateMillis }
                        .collect { millis ->
                            if (millis != null && millis != currentTask.dueDate) {
                                viewModel.setDueDate(taskId, millis)
                            }
                        }
                }
                DatePicker(state = datePickerState, title = null, headline = null, showModeToggle = false)
            }

            // Timer
            TimerCard(task = currentTask, now = now, viewModel = viewModel)

            // Notes
            FieldLabel("Notes")
            OutlinedTextField(
                value = notesText,
                onValueChange = {
                    notesText = it
                    viewModel.updateNotes(taskId, it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 140.dp)
            )

            // Sessions
            if (sessions.isNotEmpty()) {
                FieldLabel("Sessions")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    sessions.forEach { session -> SessionRow(session) }
                }
            }
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        fontWeight = FontWeight.Medium
    )
}

@Composable
private fun SessionRow(session: TimerSessionEntity) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            session.mode.label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.weight(1f)
        )
        Text(
            DateFormatting.formatDuration(session.durationSeconds),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}
