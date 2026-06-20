package com.focusnotes.app.data

import kotlinx.coroutines.flow.Flow

class TaskRepository(private val dao: TaskDao) {

    fun observeTasks(): Flow<List<TaskEntity>> = dao.observeTasks()

    fun observeTask(id: String): Flow<TaskEntity?> = dao.observeTask(id)

    fun observeSessions(taskId: String): Flow<List<TimerSessionEntity>> =
        dao.observeSessions(taskId)

    suspend fun getTask(id: String): TaskEntity? = dao.getTask(id)

    suspend fun upsert(task: TaskEntity) = dao.upsertTask(task)

    suspend fun delete(task: TaskEntity) = dao.deleteTask(task)

    suspend fun deleteById(id: String) = dao.deleteTaskById(id)

    suspend fun insertSession(session: TimerSessionEntity) = dao.insertSession(session)
}
