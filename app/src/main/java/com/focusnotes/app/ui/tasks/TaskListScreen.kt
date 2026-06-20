package com.focusnotes.app.ui.tasks

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.focusnotes.app.data.TaskEntity
import com.focusnotes.app.data.status
import com.focusnotes.app.model.TaskStatus
import com.focusnotes.app.ui.TasksViewModel
import com.focusnotes.app.ui.theme.OverdueRed
import com.focusnotes.app.ui.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    tasks: List<TaskEntity>,
    now: Long,
    viewModel: TasksViewModel,
    onOpenTask: (String) -> Unit,
) {
    var newTaskTitle by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("All Tasks") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp)
        ) {
            item {
                QuickAddBar(
                    value = newTaskTitle,
                    onValueChange = { newTaskTitle = it },
                    onSubmit = {
                        viewModel.addTask(newTaskTitle)
                        newTaskTitle = ""
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }

            if (tasks.isEmpty()) {
                item { EmptyTasksState() }
            } else {
                items(tasks, key = { it.id }) { task ->
                    SwipeableTaskItem(
                        task = task,
                        now = now,
                        onOpen = { onOpenTask(task.id) },
                        onToggleDone = { viewModel.toggleDone(task) },
                        onToggleFocus = { viewModel.toggleFocus(task) },
                        onDelete = { viewModel.delete(task) }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 16.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun SwipeableTaskItem(
    task: TaskEntity,
    now: Long,
    onOpen: () -> Unit,
    onToggleDone: () -> Unit,
    onToggleFocus: () -> Unit,
    onDelete: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onToggleDone()
                    false // snap back; the row stays
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete()
                    true // dismiss; the row leaves the list
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = { SwipeBackground(dismissState.targetValue) }
    ) {
        var menuExpanded by remember { mutableStateOf(false) }
        Box {
            TaskRow(
                task = task,
                now = now,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .combinedClickable(
                        onClick = onOpen,
                        onLongClick = { menuExpanded = true }
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )
            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                DropdownMenuItem(
                    text = {
                        Text(if (task.isFocused) "Remove from Focus" else "Add to Focus")
                    },
                    leadingIcon = { Icon(Icons.Filled.CenterFocusStrong, contentDescription = null) },
                    onClick = {
                        menuExpanded = false
                        onToggleFocus()
                    }
                )
                DropdownMenuItem(
                    text = {
                        Text(if (task.status == TaskStatus.DONE) "Mark To Do" else "Mark Done")
                    },
                    leadingIcon = { Icon(Icons.Filled.Check, contentDescription = null) },
                    onClick = {
                        menuExpanded = false
                        onToggleDone()
                    }
                )
            }
        }
    }
}

@Composable
private fun SwipeBackground(target: SwipeToDismissBoxValue) {
    val (color, alignment, icon, label) = when (target) {
        SwipeToDismissBoxValue.StartToEnd -> Quad(SuccessGreen, Alignment.CenterStart, Icons.Filled.Check, "Done")
        SwipeToDismissBoxValue.EndToStart -> Quad(OverdueRed, Alignment.CenterEnd, Icons.Filled.Delete, "Delete")
        SwipeToDismissBoxValue.Settled -> Quad(Color.Transparent, Alignment.CenterStart, Icons.Filled.Check, "")
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color)
            .padding(horizontal = 24.dp),
        contentAlignment = alignment
    ) {
        if (target != SwipeToDismissBoxValue.Settled) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(icon, contentDescription = label, tint = Color.White)
                Text(label, color = Color.White, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

private data class Quad(
    val color: Color,
    val alignment: Alignment,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String,
)

@Composable
private fun EmptyTasksState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("No tasks yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(
            "Add your first task above.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}
