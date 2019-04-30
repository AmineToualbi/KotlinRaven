package com.toualbiapps.aminetoualbi.raven.view.application

import com.toualbiapps.aminetoualbi.raven.view.GPSUtils.BackgroundService
import com.toualbiapps.aminetoualbi.raven.view.MainActivity
import com.toualbiapps.aminetoualbi.raven.view.NewRaven
import com.toualbiapps.aminetoualbi.raven.view.application.dependencyModules.ProviderModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ProviderModule::class])           //Tells Dagger this is where the magic happens & where we control our injected services.
interface DatabaseAppComponent {

    fun inject(newRavenActivity: NewRaven)
    fun injectMain(mainActivity: MainActivity)
    fun injectService(backgroundService: BackgroundService)

}