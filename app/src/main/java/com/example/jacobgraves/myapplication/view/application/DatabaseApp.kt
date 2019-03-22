package com.example.jacobgraves.myapplication.view.application

import android.app.Application
import com.example.jacobgraves.myapplication.view.application.dependencyModules.ContextModule

class DatabaseApp : Application() {

    companion object {
        lateinit var component: DatabaseAppComponent        //Use this bc Dagger doesn't initialize variables at compile-time.
    }

    override fun onCreate() {
        super.onCreate()

        component = DaggerDatabaseAppComponent
                .builder()
                .contextModule(
                        ContextModule(applicationContext)
                )
                .build()
    }


}