package com.manishjandu.mvvmtodo.ui.tasks

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.manishjandu.mvvmtodo.data.TaskDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

class TasksViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao
) : ViewModel() {

    val searchQuery = MutableStateFlow("")
    val sortOrder = MutableStateFlow(SortOrder.BY_DATE)
    val hideCompleted = MutableStateFlow(false)

    val tasksFlow = combine(
        searchQuery,
        sortOrder,
        hideCompleted
    ){
        query,sortOrder,hideComplted ->
        Triple(query,sortOrder,hideComplted)
    }.flatMapLatest {(query,sortOrder,hideCompleted) ->
        taskDao.getTasks(query,sortOrder,hideCompleted)
    }

    val tasks = tasksFlow.asLiveData()

}

enum class SortOrder { BY_NAME, BY_DATE }