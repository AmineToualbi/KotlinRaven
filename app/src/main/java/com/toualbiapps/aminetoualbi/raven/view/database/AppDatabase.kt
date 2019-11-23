package com.toualbiapps.aminetoualbi.raven.view.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import com.toualbiapps.aminetoualbi.raven.view.database.daos.RavenDao
import com.toualbiapps.aminetoualbi.raven.view.model.Raven

@Database(
        entities = [
        Raven::class
        ],
        version = 4,
        exportSchema = false
)

abstract class AppDatabase : RoomDatabase() {

    abstract fun ravenDao(): RavenDao

}