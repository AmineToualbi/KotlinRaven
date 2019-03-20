package com.example.jacobgraves.myapplication.view.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import com.example.jacobgraves.myapplication.view.database.daos.RavenDao
import com.example.jacobgraves.myapplication.view.model.Raven

@Database(
        entities = [
        Raven::class
        ],
        version = 2,
        exportSchema = false
)

abstract class AppDatabase : RoomDatabase() {

    abstract fun ravenDao(): RavenDao

}