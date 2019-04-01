package com.example.jacobgraves.myapplication.view.application.dependencyModules

import com.example.jacobgraves.myapplication.view.providers.IRavenProvider
import com.example.jacobgraves.myapplication.view.providers.RavenProvider
import dagger.Binds
import dagger.Module

@Module(includes = [DatabaseModule::class])
abstract class ProviderModule {

    @Binds          //Binds abstract classes & modules.
    abstract fun bindRavenProvider(implementation: RavenProvider): IRavenProvider

}