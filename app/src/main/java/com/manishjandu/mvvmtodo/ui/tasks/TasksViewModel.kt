package com.manishjandu.mvvmtodo.ui.tasks

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.manishjandu.mvvmtodo.data.TaskDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

 class TasksViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao
) : ViewModel() {

     val searchQuery = MutableStateFlow("")

     val tasksFlow = searchQuery.flatMapLatest {
         taskDao.getTasks(it)
     }

    val tasks = tasksFlow.asLiveData()

 }