package com.toualbiapps.aminetoualbi.raven.view.application

import android.app.Application
import com.toualbiapps.aminetoualbi.raven.view.application.dependencyModules.ContextModule

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