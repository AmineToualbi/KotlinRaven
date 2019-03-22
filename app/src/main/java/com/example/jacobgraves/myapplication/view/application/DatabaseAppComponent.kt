package com.example.jacobgraves.myapplication.view.application

import com.example.jacobgraves.myapplication.view.NewRaven
import com.example.jacobgraves.myapplication.view.application.dependencyModules.ProviderModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ProviderModule::class])           //Tells Dagger this is where the magic happens & where we control our injected services.
interface DatabaseAppComponent {

    fun inject(newRavenActivity: NewRaven)

}