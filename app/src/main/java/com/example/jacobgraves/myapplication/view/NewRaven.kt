package com.example.jacobgraves.myapplication.view

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.example.jacobgraves.myapplication.R
import com.example.jacobgraves.myapplication.view.application.DatabaseApp
import com.example.jacobgraves.myapplication.view.model.Raven
import com.example.jacobgraves.myapplication.view.permissions.RequestPermission
import com.example.jacobgraves.myapplication.view.providers.IRavenProvider
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_new_raven.*
import kotlinx.android.synthetic.main.popup_delete_raven.*
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
    private lateinit var req_permission: RequestPermission

    @Inject
    lateinit var ravenProvider: IRavenProvider

    lateinit var overwritePopupDialog: Dialog


    companion object {
        val MAX_NBR_OF_RAVENS = 3
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_raven)

        DatabaseApp.component.inject(this)

        overwritePopupDialog = Dialog(this)
        overwritePopupDialog.findViewById<View>(R.layout.popup_delete_raven)

        val goBackToMainActivity: Intent = Intent(applicationContext, MainActivity::class.java)


        val permissionList = listOf<String>(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.SEND_SMS
        )

        req_permission = RequestPermission(this,permissionList,PermissionsRequestCode)


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

                val ravenData = ravenProvider.getAll()

                //Check if 3 ravens are already stored.
                //If not, retrieve index of last raven & add new raven right after.
                //If 3 ravens stored, popup to ask if they want to overwrite oldest raven.

                if(checkRavenOverwrite(ravenData) == false) {

                    var newRavenID = 0

                    if(ravenData.isNotEmpty()) {
                        newRavenID = ravenData.lastIndex + 1
                    }

                    saveRaven(newRavenID, goBackToMainActivity)

                }

                else {

                    var overwrittenRavenID = MainActivity.ravenID%3
                    showOverwriteRavenPopup(overwrittenRavenID, ravenData, goBackToMainActivity)

                }

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

    private fun saveRaven(newRavenID: Int, goBackToMainActivity: Intent) {

        val raven: Raven = Raven(newRavenID, ravenName.toString(),
                ravenPhoneNo.toString(), ravenMessage.toString(), ravenLongitude, ravenLatitude)

        MainActivity.ravenID++

        ravenProvider.save(raven)

        Toast.makeText(this, "Raven created.", Toast.LENGTH_SHORT).show()

        startActivity(goBackToMainActivity)
    }

    private fun checkRavenOverwrite(ravenData: List<Raven>) : Boolean {

        if(ravenData.size > 2) {
            return true
        }
        return false

    }

    private fun showOverwriteRavenPopup(overwrittenRavenID: Int, ravenData: List<Raven>, goBackToMainActivity: Intent) {

        overwritePopupDialog.setContentView(R.layout.popup_delete_raven)

        var transparentColor = ColorDrawable(Color.TRANSPARENT)
        overwritePopupDialog.window.setBackgroundDrawable(transparentColor)
        overwritePopupDialog.show()

        var closePopupBtn: ImageButton = overwritePopupDialog.findViewById(R.id.closePopup)
        var deleteBtn: Button = overwritePopupDialog.findViewById(R.id.deleteBtn)
        deleteBtn.text = "Overwrite"
        var popupTextView: TextView = overwritePopupDialog.findViewById(R.id.popupText)
        popupTextView.text = "Three Ravens are already stored. Are you sure you want to override " +
                ravenData[overwrittenRavenID].name + "'s Raven?"


        closePopupBtn.setOnClickListener {

            overwritePopupDialog.dismiss()

        }

        deleteBtn.setOnClickListener {

            ravenProvider.delete(ravenData[overwrittenRavenID])

            saveRaven(overwrittenRavenID, goBackToMainActivity)

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
