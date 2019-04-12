package com.example.jacobgraves.myapplication.view.providers

import com.example.jacobgraves.myapplication.view.model.Raven

interface IRavenProvider {

    fun getAll(): List<Raven>

    fun save(raven: Raven)

    fun update(raven: Raven)

    fun delete(raven: Raven)

}