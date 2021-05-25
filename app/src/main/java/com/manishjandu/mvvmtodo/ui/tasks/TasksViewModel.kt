package com.manishjandu.mvvmtodo.ui.tasks

import android.util.Log
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.manishjandu.mvvmtodo.data.PreferencesManager
import com.manishjandu.mvvmtodo.data.SortOrder
import com.manishjandu.mvvmtodo.data.TaskDao
import com.manishjandu.mvvmtodo.data.Task
import com.manishjandu.mvvmtodo.ui.ADD_TASK_RESULT_OK
import com.manishjandu.mvvmtodo.ui.EDIT_TASK_RESULT_OK
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

private const val TAG = "TasksViewModel"

class TasksViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao,
    private val preferencesManager: PreferencesManager,
    @Assisted private val state: SavedStateHandle
) : ViewModel() {

    val searchQuery = state.getLiveData("searchQuery", "")

    val preferencesFlow = preferencesManager.preferencesFlow

    private val taskEventChannel = Channel<TaskEvent>() //not expose outside for change
    val taskEvent = taskEventChannel.receiveAsFlow() //on open to receive

    val tasksFlow = combine(
        searchQuery.asFlow(),
        preferencesFlow,
    ) { query, filterPreferences ->
        Pair(query, filterPreferences)
    }.flatMapLatest { (query, filterPreferences) ->
        taskDao.getTasks(query, filterPreferences.sortOrder, filterPreferences.hideCompleted)
    }

    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        preferencesManager.updateSortOrder(sortOrder)
    }

    fun onHideCompletedClick(hideCompleted: Boolean) = viewModelScope.launch {
        preferencesManager.updateHideCompleted(hideCompleted)
    }

    fun onTaskSelected(task: Task) = viewModelScope.launch {
        taskEventChannel.send(TaskEvent.NavigateToEditTaskScreen(task))
    }

    fun onTaskCheckedChanged(task: Task, isChecked: Boolean) = viewModelScope.launch {
        taskDao.insert(task.copy(completed = isChecked))
    }

    fun onTaskSwiped(task: Task) = viewModelScope.launch {
        taskDao.delete(task)
        taskEventChannel.send(TaskEvent.ShowUndoDeleteTaskMessage(task))
    }

    fun onUndoDeleteClick(task: Task) = viewModelScope.launch {
        taskDao.insert(task)
    }

    fun onAddNewTaskClick() = viewModelScope.launch {
        taskEventChannel.send(TaskEvent.NavigateToAddTaskScreen)
    }

    fun onAddEditResult(result: Int) {
        Log.e(TAG, "onAddEditResult: $result", )
        when(result){
            ADD_TASK_RESULT_OK -> showTaskSavedConfirmationMessage("Task Added")
            EDIT_TASK_RESULT_OK -> showTaskSavedConfirmationMessage("Task Updated")
        }
    }

    private fun showTaskSavedConfirmationMessage(message: String) = viewModelScope.launch {
        Log.e(TAG, "showTaskSavedConfirmationMessage: $message", )
        taskEventChannel.send(TaskEvent.ShowTaskSavedConfirmationMessage(msg = message))
    }

    val tasks = tasksFlow.asLiveData()

    sealed class TaskEvent {
        object NavigateToAddTaskScreen : TaskEvent()
        data class NavigateToEditTaskScreen(val task:Task) : TaskEvent()
        data class ShowUndoDeleteTaskMessage(val task: Task) : TaskEvent()
        data class ShowTaskSavedConfirmationMessage(val msg:String) : TaskEvent()
    }

}

