package com.manishjandu.mvvmtodo.ui.tasks

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.manishjandu.mvvmtodo.R
import com.manishjandu.mvvmtodo.utils.onQueryTextChanged
import com.manishjandu.mvvmtodo.data.SortOrder
import com.manishjandu.mvvmtodo.data.Task
import com.manishjandu.mvvmtodo.databinding.FragmentTasksBinding
import com.manishjandu.mvvmtodo.ui.ADD_TASK_RESULT_OK
import com.manishjandu.mvvmtodo.ui.addedittask.AddEditTaskFragment.Companion.ADD_EDIT_RESULT
import com.manishjandu.mvvmtodo.ui.addedittask.AddEditTaskFragment.Companion.FRAGMENT_RESULT_REQUEST_KEY
import com.manishjandu.mvvmtodo.utils.exhaustive
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private const val TAG = "TasksFragment"
@AndroidEntryPoint
class TasksFragment : Fragment(R.layout.fragment_tasks), TasksAdapter.OnItemClickListener {
    private val tasksViewModel: TasksViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentTasksBinding.bind(view)

        val tasksAdapter = TasksAdapter(this)

        binding.apply {
            recyclerViewTaks.apply {
                adapter = tasksAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }

            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val task = tasksAdapter.currentList[viewHolder.adapterPosition]
                    tasksViewModel.onTaskSwiped(task)
                }
            }).attachToRecyclerView(recyclerViewTaks)

            fabAddTask.setOnClickListener {
                tasksViewModel.onAddNewTaskClick()
            }
        }

        setFragmentResultListener(FRAGMENT_RESULT_REQUEST_KEY){_,bundle ->
            val result = bundle.getInt(ADD_EDIT_RESULT)
            Log.e(TAG, "fragment listener: $result", )
            tasksViewModel.onAddEditResult(result)
        }

        tasksViewModel.tasks.observe(viewLifecycleOwner) {
            tasksAdapter.submitList(it)
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            tasksViewModel.taskEvent.collect { event ->
                when (event) {
                    is TasksViewModel.TaskEvent.ShowUndoDeleteTaskMessage -> {
                        Snackbar.make(requireView(), "Task Deleted", Snackbar.LENGTH_LONG)
                            .setAction("Undo") {
                                tasksViewModel.onUndoDeleteClick(event.task)
                            }
                            .show()
                    }
                    is TasksViewModel.TaskEvent.NavigateToAddTaskScreen -> {
                        val action = TasksFragmentDirections.actionTasksFragmentToAddEditTaskFragment(null,"New Task")
                        findNavController().navigate(action)
                    }
                    is TasksViewModel.TaskEvent.NavigateToEditTaskScreen -> {
                        val action = TasksFragmentDirections.actionTasksFragmentToAddEditTaskFragment(event.task,"Edit Task")
                        findNavController().navigate(action)
                    }
                    is TasksViewModel.TaskEvent.ShowTaskSavedConfirmationMessage -> {
                        Log.e(TAG, "channel listener: ${event.msg}", )
                        Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_LONG).show()
                    }
                }.exhaustive
            }
        }

        setHasOptionsMenu(true)
    }

    override fun onItemClick(task: Task) {
        tasksViewModel.onTaskSelected(task)
    }

    override fun onCheckBoxClick(task: Task, isChecked: Boolean) {
        tasksViewModel.onTaskCheckedChanged(task, isChecked)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_tasks, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.onQueryTextChanged {
            tasksViewModel.searchQuery.value = it
        }

        viewLifecycleOwner.lifecycleScope.launch {
            menu.findItem(R.id.action_hide_completed_tasks).isChecked =
                tasksViewModel.preferencesFlow.first().hideCompleted
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sort_by_name -> {
                tasksViewModel.onSortOrderSelected(SortOrder.BY_NAME)
                true
            }
            R.id.action_sort_by_date_created -> {
                tasksViewModel.onSortOrderSelected(SortOrder.BY_DATE)
                true
            }
            R.id.action_hide_completed_tasks -> {
                item.isChecked = item.isChecked
                tasksViewModel.onHideCompletedClick(item.isChecked)
                true
            }
            R.id.action_delete_all_completed_tasks -> {

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}