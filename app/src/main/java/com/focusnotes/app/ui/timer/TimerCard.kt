package com.focusnotes.app.ui.timer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.focusnotes.app.data.TaskEntity
import com.focusnotes.app.data.activeTimerMode
import com.focusnotes.app.data.pomodoroPhase
import com.focusnotes.app.model.TimerMode
import com.focusnotes.app.timer.TimerCalc
import com.focusnotes.app.ui.TasksViewModel
import com.focusnotes.app.util.DateFormatting

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerCard(
    task: TaskEntity,
    now: Long,
    viewModel: TasksViewModel,
    modifier: Modifier = Modifier,
) {
    var selectedMode by remember(task.id) {
        mutableStateOf(task.activeTimerMode ?: TimerMode.ELAPSED)
    }
    val isActive = TimerCalc.isActive(task)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                task.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (isActive) {
                Text(
                    "Running",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            RoundedCornerShape(50)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            TimerMode.entries.forEachIndexed { index, mode ->
                SegmentedButton(
                    selected = selectedMode == mode,
                    onClick = { selectedMode = mode },
                    shape = SegmentedButtonDefaults.itemShape(index, TimerMode.entries.size),
                    label = { Text(mode.label) }
                )
            }
        }

        val showRing = selectedMode == TimerMode.POMODORO &&
                isActive &&
                task.activeTimerMode == TimerMode.POMODORO

        if (showRing) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                PomodoroRing(
                    progress = TimerCalc.pomodoroProgress(task, now, viewModel.workSeconds, viewModel.breakSeconds),
                    remainingSeconds = TimerCalc.pomodoroRemainingSeconds(task, now, viewModel.workSeconds, viewModel.breakSeconds),
                    phaseLabel = task.pomodoroPhase?.label ?: "Focus"
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    DateFormatting.formatDuration(TimerCalc.sessionElapsedSeconds(task, now)),
                    style = MaterialTheme.typography.displaySmall
                )
                Text(
                    "Total ${DateFormatting.formatDuration(TimerCalc.totalDisplaySeconds(task, now))}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        Button(
            onClick = { viewModel.toggleTimer(task, selectedMode) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (isActive) "Pause" else "Start",
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}
