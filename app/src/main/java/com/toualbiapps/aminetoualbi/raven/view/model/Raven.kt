package com.toualbiapps.aminetoualbi.raven.view.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

//Model of our app.
@Entity(tableName = "Ravens")
class Raven(
            @PrimaryKey var id: Int,         //id of the raven.
            var name: String,
            var phoneNo: String,
            var message: String,
            var longitude: Double,
            var latitude: Double,
            var usable: Boolean) {

}