package com.example.jacobgraves.myapplication.view

import android.Manifest
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.widget.Toast
import com.example.jacobgraves.myapplication.R
import com.example.jacobgraves.myapplication.view.application.DatabaseApp
import com.example.jacobgraves.myapplication.view.model.Raven
import com.example.jacobgraves.myapplication.view.permissions.requestPermission
import com.example.jacobgraves.myapplication.view.providers.IRavenProvider
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_new_raven.*
import org.json.JSONArray
import javax.inject.Inject

class NewRaven : AppCompatActivity() {

    var ravenName: String? = null
    var ravenPhoneNo: String? = null
    var ravenAddress: String? = null
    var ravenLongitude: Double = 0.0
    var ravenLatitude: Double = 0.0
    var ravenMessage: String? = null

    private val PermissionsRequestCode = 456
    private lateinit var req_permission: requestPermission

    @Inject
    lateinit var ravenProvider: IRavenProvider

    companion object {
        val MAX_NBR_OF_RAVENS = 3
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_raven)

        DatabaseApp.component.inject(this)

        val goBackToMainActivity: Intent = Intent(applicationContext, MainActivity::class.java)

        val permissionList = listOf<String>(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.SEND_SMS
        )

        req_permission = requestPermission(this,permissionList,PermissionsRequestCode)


        validButton.setOnClickListener {

            ravenName = enteredName.text.toString()
            ravenPhoneNo = enteredNumber.text.toString()
            ravenAddress = enteredAddress.text.toString()
            ravenMessage = enteredMessage.text.toString()

            //TODO - REVERSE GEOCODING TO GET LONG & LAT FROM ADDRESS.


            if(isValid(ravenName, ravenPhoneNo, ravenAddress, ravenMessage) == false) {
                Toast.makeText(this, "Please fill all the field.", Toast.LENGTH_LONG).show()
            }
            else {

                val raven: Raven = Raven((MainActivity.ravenID++), ravenName.toString(),
                        ravenPhoneNo.toString(), ravenMessage.toString(), ravenLongitude, ravenLatitude)

                ravenProvider.save(raven)

                Toast.makeText(this, "Raven created.", Toast.LENGTH_SHORT).show()

                startActivity(goBackToMainActivity)

            }

        }

        cancelButton.setOnClickListener {

            val ravenData = ravenProvider.getAll()

            if(ravenData == null || ravenData.isEmpty()) {
                showMessage("No data!")
            }
            else {

                var jsonData = Gson().toJson(ravenData)
                showMessage(jsonData)
                var jsonArray = JSONArray(jsonData)

                for(jsonIndex in 0..(jsonArray.length()-1)) {
                    if(jsonIndex < MAX_NBR_OF_RAVENS) {
                        Log.d("JSON", jsonArray.getJSONObject(jsonIndex).getString("name"))
                    }
                }
            }

        }

    }

    private fun isValid(name: String?, phoneNo: String?, address: String?, message: String?): Boolean {

        return !name.isNullOrEmpty() &&
                !phoneNo.isNullOrEmpty() &&
                !address.isNullOrEmpty() &&
                !message.isNullOrEmpty()

    }

    private fun showMessage(message: String) {

        AlertDialog.Builder(this).setMessage(message).create().show()

    }
}
