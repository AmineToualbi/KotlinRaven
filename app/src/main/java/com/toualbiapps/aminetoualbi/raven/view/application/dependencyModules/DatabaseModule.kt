package com.toualbiapps.aminetoualbi.raven.view.application.dependencyModules

import android.arch.persistence.room.Room
import android.content.Context
import com.toualbiapps.aminetoualbi.raven.view.database.AppDatabase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [ContextModule::class])              //Tells Dagger to use this for dependency injection by including ContextModule
class DatabaseModule {

    @Provides
    @Singleton
    fun appDatabase(context: Context): AppDatabase {

        return Room
                .databaseBuilder(context, AppDatabase::class.java, "RavenDB.db")
                .allowMainThreadQueries()           //Slow down UI, think of alternative.
                .fallbackToDestructiveMigration()       //If db doesn't work => remake it.
                .build()

    }

    @Provides
    fun ravenDao(database: AppDatabase) = database.ravenDao()

}