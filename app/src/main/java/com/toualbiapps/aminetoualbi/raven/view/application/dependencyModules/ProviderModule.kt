package com.toualbiapps.aminetoualbi.raven.view.application.dependencyModules

import com.toualbiapps.aminetoualbi.raven.view.providers.IRavenProvider
import com.toualbiapps.aminetoualbi.raven.view.providers.RavenProvider
import dagger.Binds
import dagger.Module

@Module(includes = [DatabaseModule::class])
abstract class ProviderModule {

    @Binds          //Binds abstract classes & modules.
    abstract fun bindRavenProvider(implementation: RavenProvider): IRavenProvider

}