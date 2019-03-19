package com.example.jacobgraves.myapplication.view

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.example.jacobgraves.myapplication.R
import com.example.jacobgraves.myapplication.view.model.Raven
import kotlinx.android.synthetic.main.activity_new_raven.*

class NewRaven : AppCompatActivity() {

    var ravenName: String = ""
    var ravenPhoneNo: String = ""
    var ravenAddress: String = ""
    var ravenLongitude: Double = 0.0
    var ravenLatitude: Double = 0.0
    var ravenMessage: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_raven)

        val goBackToMainActivity: Intent = Intent(applicationContext, MainActivity::class.java)

        validButton.setOnClickListener {

            ravenName = enteredName.text.toString()
            ravenPhoneNo = enteredNumber.text.toString()
            ravenAddress = enteredAddress.text.toString()
            ravenMessage = enteredMessage.text.toString()

            //TODO - REVERSE GEOCODING TO GET LONG & LAT FROM ADDRESS.

            val raven: Raven = Raven(ravenName, ravenPhoneNo, ravenMessage, ravenLongitude, ravenLatitude)

            //TODO - SAVE RAVEN INTO DATABASE.

            startActivity(goBackToMainActivity)

        }

        cancelButton.setOnClickListener {

            startActivity(goBackToMainActivity)

        }

    }
}
