package com.focusnotes.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.focusnotes.app.ui.focus.FocusScreen
import com.focusnotes.app.ui.tasks.TaskDetailScreen
import com.focusnotes.app.ui.tasks.TaskListScreen
import com.focusnotes.app.ui.today.TodayScreen

private sealed class Tab(val route: String, val label: String, val icon: ImageVector) {
    data object Tasks : Tab("tasks", "Tasks", Icons.Filled.Checklist)
    data object Focus : Tab("focus", "Focus", Icons.Filled.CenterFocusStrong)
    data object Today : Tab("today", "Today", Icons.Filled.CalendarToday)
}

private val tabs = listOf(Tab.Tasks, Tab.Focus, Tab.Today)

@Composable
fun FocusNotesApp(
    viewModel: TasksViewModel = viewModel(factory = TasksViewModel.Factory),
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val now by viewModel.now.collectAsStateWithLifecycle()

    val showBottomBar = currentRoute in tabs.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    tabs.forEach { tab ->
                        val selected = backStackEntry?.destination?.hierarchy
                            ?.any { it.route == tab.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Tab.Tasks.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Tab.Tasks.route) {
                TaskListScreen(
                    tasks = tasks,
                    now = now,
                    viewModel = viewModel,
                    onOpenTask = { navController.navigate("detail/$it") }
                )
            }
            composable(Tab.Focus.route) {
                FocusScreen(
                    tasks = tasks,
                    now = now,
                    viewModel = viewModel,
                    onOpenTask = { navController.navigate("detail/$it") }
                )
            }
            composable(Tab.Today.route) {
                TodayScreen(
                    tasks = tasks,
                    now = now,
                    onOpenTask = { navController.navigate("detail/$it") }
                )
            }
            composable("detail/{taskId}") { entry ->
                val taskId = entry.arguments?.getString("taskId").orEmpty()
                TaskDetailScreen(
                    taskId = taskId,
                    now = now,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
