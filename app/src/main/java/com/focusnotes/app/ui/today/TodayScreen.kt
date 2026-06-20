package com.focusnotes.app.ui.today

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import com.focusnotes.app.data.status
import com.focusnotes.app.model.TaskStatus
import com.focusnotes.app.ui.tasks.TaskRow
import com.focusnotes.app.util.DateFormatting

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    tasks: List<TaskEntity>,
    now: Long,
    onOpenTask: (String) -> Unit,
) {
    val endOfToday = DateFormatting.endOfTodayMillis()
    val todayTasks = tasks
        .filter { task ->
            task.status != TaskStatus.DONE && task.dueDate != null && task.dueDate <= endOfToday
        }
        .sortedBy { it.dueDate }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Today") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            if (todayTasks.isEmpty()) {
                EmptyTodayState()
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                ) {
                    todayTasks.forEachIndexed { index, task ->
                        TaskRow(
                            task = task,
                            now = now,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenTask(task.id) }
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                        if (index != todayTasks.lastIndex) {
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

@Composable
private fun EmptyTodayState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            Icons.Filled.CalendarToday,
            contentDescription = null,
            modifier = Modifier.size(36.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Text("Nothing due today", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(
            "Tasks with due dates on or before today will appear here.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}
