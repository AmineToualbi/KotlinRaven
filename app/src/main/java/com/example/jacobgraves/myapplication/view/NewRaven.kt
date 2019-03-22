package com.example.jacobgraves.myapplication.view

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import com.example.jacobgraves.myapplication.R
import com.example.jacobgraves.myapplication.view.application.DatabaseApp
import com.example.jacobgraves.myapplication.view.model.Raven
import com.example.jacobgraves.myapplication.view.providers.IRavenProvider
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_new_raven.*
import javax.inject.Inject

class NewRaven : AppCompatActivity() {

    var ravenName: String = ""
    var ravenPhoneNo: String = ""
    var ravenAddress: String = ""
    var ravenLongitude: Double = 0.0
    var ravenLatitude: Double = 0.0
    var ravenMessage: String = ""

    @Inject
    lateinit var ravenProvider: IRavenProvider


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_raven)

        DatabaseApp.component.inject(this)

        val goBackToMainActivity: Intent = Intent(applicationContext, MainActivity::class.java)

        validButton.setOnClickListener {

            ravenName = enteredName.text.toString()
            ravenPhoneNo = enteredNumber.text.toString()
            ravenAddress = enteredAddress.text.toString()
            ravenMessage = enteredMessage.text.toString()

            //TODO - REVERSE GEOCODING TO GET LONG & LAT FROM ADDRESS.

            val raven: Raven = Raven(MainActivity.ravenID++, ravenName,ravenPhoneNo, ravenMessage, ravenLongitude, ravenLatitude)

            ravenProvider.save(raven)
            showMessage("Raven saved.")


            //TODO - SAVE RAVEN INTO DATABASE.

            //startActivity(goBackToMainActivity)

        }

        cancelButton.setOnClickListener {

            val ravenData = ravenProvider.getAll()
            if(ravenData == null || ravenData.isEmpty()) {
                showMessage("No data!")
            }
            else {
                showMessage(Gson().toJson(ravenData))
            }
           // startActivity(goBackToMainActivity)

        }

    }

    private fun showMessage(message: String) {

        AlertDialog.Builder(this).setMessage(message).create().show()

    }
}
