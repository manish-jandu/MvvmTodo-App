package com.manishjandu.mvvmtodo.ui.tasks

import androidx.lifecycle.ViewModel
import com.manishjandu.mvvmtodo.data.TaskDao
import javax.inject.Inject

 class TasksViewModel @Inject constructor(
    private val taskDao: TaskDao
) : ViewModel() {


 }