package com.example.jacobgraves.myapplication.view

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.location.Geocoder
import android.location.LocationListener
import android.location.LocationManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.IBinder
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.LocalBroadcastManager
import android.telephony.SmsManager
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.Switch
import android.widget.Toast
import com.example.jacobgraves.myapplication.BuildConfig
import com.example.jacobgraves.myapplication.R
import com.example.jacobgraves.myapplication.view.GPSUtils.BackgroundService
import com.example.jacobgraves.myapplication.view.application.DatabaseApp
import com.example.jacobgraves.myapplication.view.model.Raven
import kotlinx.android.synthetic.main.activity_main.*
import com.example.jacobgraves.myapplication.view.permissions.RequestPermission
import com.example.jacobgraves.myapplication.view.providers.IRavenProvider
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.gson.Gson
import com.karumi.dexter.Dexter
import org.json.JSONArray
import java.io.IOException
import java.lang.IllegalArgumentException
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer


private var locationManager: LocationManager? = null
private val PermissionsRequestCode = 234


class MainActivity : AppCompatActivity() {

    //RequestPermission object to handle permissions in the app.
    private lateinit var req_permission: RequestPermission

    //Database provider.
    @Inject
    lateinit var ravenProvider: IRavenProvider

    companion object {          //Equivalent of public static var.
        var ravenID: Int = 0
        var currentLongitude: Double = 100.0
        var currentLatitude: Double = 100.0
        //emptyRaven works as a placeholder to avoid NullPointerExceptions when checking for Ravens in ravenArray.
        val emptyRaven = Raven(Int.MAX_VALUE, "0", "0", "0", 0.0, 0.0, true)
        //ravenArray is continuously updated w latest Ravens in db accessible from everywhere in the app.
        var ravenArray: Array<Raven> = arrayOf<Raven>(emptyRaven, emptyRaven, emptyRaven)
        var mTracking = false
    }

    //DialogBox for the popup to delete a Raven.
    lateinit var deletePopupDialog: Dialog

    //Tag String used for Logging & Testing.
    val TAG = "PrimaryLog"

    //BackgroundService object to handle most of the operations of our app.
    var gpsService: BackgroundService? = null
    //Flag to ensure our app is connected to the Service.
    var connectionEstablished: Boolean = false

    //Flag notifying when app is ON.
    var appIsOn = false

    //List that will contain the current address "reverse geocoded" by the geocoder.
    var addresses: List<Address> = emptyList()



    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Create, start, & bind foreground service to app.
        val serviceIntent = Intent(this, BackgroundService::class.java)
        this.startService(serviceIntent)
        this.bindService(serviceIntent, myConnection, Context.BIND_AUTO_CREATE)

        Log.i(TAG, "ONCREATE")

        //Initialize popup to delete a Raven.
        deletePopupDialog = Dialog(this)
        deletePopupDialog.findViewById<View>(R.layout.popup_delete_raven)

        //Inject the Database Component to make it usable in this view.
        DatabaseApp.component.injectMain(this)

        //List of permissions needed in our app.
        val permissionList = listOf<String>(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.SEND_SMS
        )

        //Initialize RequestPermission object & check for the permissions.
        req_permission = RequestPermission(this, permissionList, PermissionsRequestCode)
        req_permission.checkPermissions()

        //Hide OFF sign.
        offsign.alpha = 0f


        //If user clicks on fab, go to New Raven screen.
        addContactfab.setOnClickListener {

            val addNewRavenIntent = Intent(applicationContext, NewRaven::class.java)
            startActivity(addNewRavenIntent)

        }


        //Switch turning OFF & ON the app.
        var toggle = findViewById(R.id.switchonoff) as Switch

        //Check if app is running or was running to either keep switch ON or turn it off when app starts at beginning of lifecycle.
        if (NewRaven.appRunning == null || NewRaven.appRunning == false) {
            toggle.isChecked = false
            turnScreenOff()
        } else if (NewRaven.appRunning == true) {
            toggle.isChecked = true
            turnScreenOn()
        }


        //Switch Control:
        toggle.setOnCheckedChangeListener() { buttonView, isChecked ->

            if (isChecked) {    //App ON -> start tracking GPS.

                Toast.makeText(applicationContext, "App turned on!", Toast.LENGTH_LONG).show()
                turnScreenOn()
                gpsService!!.startTracking()
                mTracking = true
                appIsOn = true

            }

            else {              //App OFF -> stop tracking GPS.

                Toast.makeText(applicationContext, "App turned off!", Toast.LENGTH_LONG).show()
                turnScreenOff()
                gpsService!!.stopTracking()
                mTracking = false
                appIsOn = false

            }

        }


        editRaven.setOnClickListener {
            showDeleteRavenPopup(0)
        }

        editRaven2.setOnClickListener {
            showDeleteRavenPopup(1)
        }

        editRaven3.setOnClickListener {
            showDeleteRavenPopup(2)
        }


        //Continuously call updateUI() when app is ON every period.
        fixedRateTimer("timer", false, 0, 2000) {
            this@MainActivity.runOnUiThread {
                if (appIsOn == true) {
                    updateUI()
                    Log.i(TAG, "UPDATEUI")
                }
            }
        }

    }


    //ServiceConnection object to bind GPS Service to our app.
    private val myConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            var name = name!!.className
            if (name.endsWith("BackgroundService")) {
                val binder = service as BackgroundService.LocationServiceBinder
                gpsService = binder.getService()
                Log.i(TAG, "GPS READY.")
                connectionEstablished = true
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            if (name!!.className.equals("BackgroundService")) {
                gpsService = null
            }
        }

    }


    //Turn screen OFF when switch is OFF.
    private fun turnScreenOff() {

        addContactfab.isEnabled = false
        offsign.alpha = 1f
        editRaven.isClickable = false
        editRaven2.isClickable = false
        editRaven2.isClickable = false
        ravenImage.alpha = .3f
        currentName.alpha = .3f
        currentName2.alpha = .3f
        currentName3.alpha = .3f
        currentAddress.alpha = .3f
        myLongitude.alpha = .3f
        myLatitude.alpha = .3f
        addContactfab.alpha = .3f
        editRaven.alpha = .3f
        editRaven2.alpha = .3f
        editRaven3.alpha = .3f
        ravenDestination.alpha = .3f

    }

    //Turn screen ON when switch is ON.
    private fun turnScreenOn() {

        addContactfab.isEnabled = true
        offsign.alpha = 0f
        editRaven.isClickable = true
        editRaven2.isClickable = true
        editRaven2.isClickable = true
        ravenImage.alpha = 1f
        currentName.alpha = 1f
        currentName2.alpha = 1f
        currentName3.alpha = 1f
        currentAddress.alpha = 1f
        myLongitude.alpha = 1f
        myLatitude.alpha = 1f
        addContactfab.alpha = 1f
        editRaven.alpha = 1f
        editRaven2.alpha = 1f
        editRaven3.alpha = 1f
        ravenDestination.alpha = 1f

    }


    //Function to display Delete Raven Popup. RavenNo indicates which Raven needs to be deleted.
    private fun showDeleteRavenPopup(ravenNo: Int) {

        //Show the popup.
        deletePopupDialog.setContentView(R.layout.popup_delete_raven)
        var transparentColor = ColorDrawable(Color.TRANSPARENT)
        deletePopupDialog.window.setBackgroundDrawable(transparentColor)
        deletePopupDialog.show()

        //Initialize buttons to avoid NPE.
        var closePopupBtn: ImageButton = deletePopupDialog.findViewById(R.id.closePopup)
        var deleteBtn: Button = deletePopupDialog.findViewById(R.id.deleteBtn)

        //If close -> dismiss the popup.
        closePopupBtn.setOnClickListener {
            deletePopupDialog.dismiss()
        }

        //If delete -> remove Raven from db & update UI.
        deleteBtn.setOnClickListener {

            val ravenData = ravenProvider.getAll()
            Log.d("RAVEN NO : ", "" + ravenNo)

            //Safe check: If the current Raven to delete has a correct id (0 to 2), delete it.
            if (ravenData.lastIndex >= ravenNo) {
                ravenProvider.delete(ravenData[ravenNo])
                Toast.makeText(this, "Raven deleted.", Toast.LENGTH_SHORT).show()
                updateUI()
                deletePopupDialog.dismiss()
            }

        }

    }


    //Function to update the UI in the main view. This is continuously called from OnCreate().
    private fun updateUI() {

        val ravenData = ravenProvider.getAll()

        populateRavenArray(ravenData)       //Continuously update ravenArray to have the latest Ravens stored in the db.

        var jsonData = Gson().toJson(ravenData)
        var jsonArray = JSONArray(jsonData)

        var nameArray = arrayOfNulls<String>(3)     //Array storing the names of the Ravens.
        var usableArray = arrayOfNulls<Boolean>(3)  //Array storing whether a Raven is usable or not.

        //Populate nameArray & usableArray.
        for (jsonIndex in 0..(jsonArray.length() - 1)) {
            if (jsonIndex < NewRaven.MAX_NBR_OF_RAVENS) {       //Safe check.

                Log.d("JSON", jsonArray.getJSONObject(jsonIndex).getString("name"))
                nameArray[jsonIndex] = jsonArray.getJSONObject(jsonIndex).getString("name")
                usableArray[jsonIndex] = jsonArray.getJSONObject(jsonIndex).getBoolean("usable") //For next update = Raven shut down for certain time.

            }
        }

        currentName.text = nameArray[0]
        currentName.setTextColor(R.color.color2)
        if (currentName.text.equals("")) {
            currentName.text = "No Raven"
        }
        else if(usableArray[0] == false) {
            currentName.text = nameArray[0] + " (OFF)"
            currentName.setTextColor(Color.RED)
            Log.i(TAG, "Raven is set to RED.")
        }
        currentName2.text = nameArray[1]
        currentName2.setTextColor(R.color.color2)
        if (currentName2.text.equals("")) {
            currentName2.text = "No Raven"

        }
       else if(usableArray[1] == false) {
            currentName2.text = nameArray[1] + " (OFF)"
            currentName2.setTextColor(Color.RED)
        }
        currentName3.text = nameArray[2]
        currentName3.setTextColor(R.color.color2)
        if (currentName3.text.equals("")) {
            currentName3.text = "No Raven"
        }
        else if(usableArray[2] == false) {
            currentName3.text = nameArray[2] + " (OFF)"
            currentName3.setTextColor(Color.RED)
        }

        Log.i(TAG, "CurrentLongitude: " + currentLongitude + " CurrentLatitude: " + currentLatitude)
        myLongitude.text = "Longitude: " + roundCoordinates(currentLongitude) + "°"
        myLatitude.text = "Latitude: " + roundCoordinates(currentLatitude) + "°"

        //Reverse GeoCoding, GPS Coordinates -> Address:
        try {

            addresses = Geocoder(this).getFromLocation(currentLatitude, currentLongitude, 1)
            if (addresses.isNotEmpty()) {
                val city = addresses.get(0).locality
                Log.i(TAG, "Address: " + addresses.get(0).getAddressLine(0))
                if(city == null) {
                    currentAddress.text = "Location: No City Found"
                }
                else {
                    currentAddress.text = "Location: " + city
                }
            }

        }
        catch(e: IOException) {
            Toast.makeText(this, "Service not available.", Toast.LENGTH_SHORT).show()
        }
        catch(e: IllegalArgumentException) {
            Toast.makeText(this, "Invalid lat/long", Toast.LENGTH_SHORT).show()
        }

    }

    //Function to populate the companion object with the Ravens used in the service to compare coordinates.
    private fun populateRavenArray(ravenData : List<Raven>) {

        //Placeholder array. Add Ravens created to this & keep emptyRaven if free spot for Raven.
        val ravArr : Array<Raven> = arrayOf(emptyRaven, emptyRaven, emptyRaven)

        //Populate the placeholder array with ravenData in the db.
        for (i in 0..(ravenData.size - 1)) {
            ravArr[i] = ravenData[i]
        }

        ravenArray = ravArr         //Copy placeholder array into ravenArray.

        for(i in 0..(ravenArray.size-1)) {
            Log.i("POPULATE", "Raven " + ravenArray!![i].name + " - " + ravenArray!![i].phoneNo)
        }

    }


    //Function to round coordinates to 2 decimal points.
    private fun roundCoordinates(coordinate: Double?): Double {

        val result = String.format("%.2f", coordinate)
        var roundedValue = 0.0
        try {
            roundedValue = java.lang.Double.parseDouble(result)
        }
        catch (ex: NumberFormatException) {
        }

        return roundedValue

    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            PermissionsRequestCode -> {
                val isPermissionsGranted = req_permission.processPermissionsResult(requestCode, permissions, grantResults)

                if (isPermissionsGranted) {
                    // Do the task now
                    //toast("Permissions granted.")
                    gpsService!!.startTracking()
                    mTracking = true
                    toast("Start Tracking")
                } else {
                    toast("Permissions denied.")
                }
                return
            }
        }
    }



    override fun onResume() {
        super.onResume()
        updateUI()
        Log.i(TAG, "ONRESUME")
    }


    private fun Context.toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }



    //This function disables the back button.
    override fun onBackPressed() {
        // Do Here what ever you want do on back press;
    }



}
