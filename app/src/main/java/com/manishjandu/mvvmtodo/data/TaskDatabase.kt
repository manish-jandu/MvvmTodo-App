package com.manishjandu.mvvmtodo.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.manishjandu.mvvmtodo.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [Task::class], version = 1)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao


    class Callback @Inject constructor(
        private val database: Provider<TaskDatabase>,
        @ApplicationScope private val applicationScope: CoroutineScope,
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            //db operations
            val dao = database.get().taskDao()

            applicationScope.launch {
                dao.insert(Task("Wash the Dishes"))
                dao.insert(Task("zoom with mlon eusk",important = true))
                dao.insert(Task("call beff jezoz",important = true,completed = true))
                dao.insert(Task("repair my bike"))
                dao.insert(Task("Do laundry",completed = true))
                dao.insert(Task("get grocery from the store"))
            }
        }
    }
}