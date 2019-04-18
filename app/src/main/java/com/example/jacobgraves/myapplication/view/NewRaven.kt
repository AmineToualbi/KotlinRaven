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
    var ravenLongitude: Double = -113.07
    var ravenLatitude: Double = 37.68
    var ravenMessage: String? = null

    private val PermissionsRequestCode = 456
    private lateinit var req_permission: RequestPermission

    @Inject
    lateinit var ravenProvider: IRavenProvider

    lateinit var overwritePopupDialog: Dialog


    companion object {
        val MAX_NBR_OF_RAVENS = 3
        var appRunning = false
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

                //Check if 3 ravens are already stored. False => No, we have less than 3 stored.
                if(checkRavenOverwrite(ravenData) == false) {

                    //Get correct ID for new Raven in DB.
                    var newRavenID = updatedRavenID(ravenData)

                    //Save Raven with correct ID.
                    saveRaven(newRavenID, goBackToMainActivity)

                }

                else {

                    //Overwrite oldest Raven.
                    var overwrittenRavenID = MainActivity.ravenID%3
                    showOverwriteRavenPopup(overwrittenRavenID, ravenData, goBackToMainActivity)

                }

            }

        }

        cancelButton.setOnClickListener {

           /* val ravenData = ravenProvider.getAll()

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
            }*/

            startActivity(goBackToMainActivity)

        }

        appRunning = true

    }


    //Function to get correct Raven ID for new object to be saved in the Database.
    private fun updatedRavenID(ravenData: List<Raven>) : Int {

        var updatedRavenID : Int = 0
        var currentRavenIDs : Array<Int?> = arrayOfNulls(3)
        var arrayIndexCount : Int = 0

        //If there's only 1 Raven stored -> add the Raven right after.
        if(ravenData.size == 1) {
            updatedRavenID = ravenData.lastIndex + 1
            return updatedRavenID
        }

        //If 2 ravens are stored, check the ordering of the IDs.
        //Iterate through the 2 ravens & retrieve their IDs in currentRavenIDs.
        for(ravenIndex in 0..(ravenData.size-1)) {
            currentRavenIDs[arrayIndexCount] = ravenData[ravenIndex].id
            Log.d("CurrentRavenIDs", "Logging in ID " + ravenIndex + " @ " + arrayIndexCount)
            arrayIndexCount++
        }

        for(i in 0..(currentRavenIDs.size-1)) {
            Log.d("CurrentRavenIDs", "Raven ID = " + currentRavenIDs[i])
        }

        //Get correct ID.
        if(currentRavenIDs[0] == 0 && currentRavenIDs[1] == 1) {
            updatedRavenID = 2
        }
        else if(currentRavenIDs[0] == 0 && currentRavenIDs[1] == 2) {
            updatedRavenID = 1
        }
        else if(currentRavenIDs[0] == 1 && currentRavenIDs[1] == 2) {
            updatedRavenID = 0
        }

        return updatedRavenID

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
