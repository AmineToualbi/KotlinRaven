package com.toualbiapps.aminetoualbi.raven.view.providers

import com.toualbiapps.aminetoualbi.raven.view.model.Raven

interface IRavenProvider {

    fun getAll(): List<Raven>

    fun save(raven: Raven)

    fun update(raven: Raven)

    fun delete(raven: Raven)

}