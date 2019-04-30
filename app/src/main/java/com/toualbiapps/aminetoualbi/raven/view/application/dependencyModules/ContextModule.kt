package com.toualbiapps.aminetoualbi.raven.view.application.dependencyModules

import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module                 //Tells Dagger to use this for dependency injection.
class ContextModule(private val context: Context) {

    @Provides
    @Singleton
    fun context(): Context = context
}