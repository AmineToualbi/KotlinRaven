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

    private lateinit var req_permission: RequestPermission

    private var notificationManager: NotificationManager? = null

    @Inject
    lateinit var ravenProvider: IRavenProvider

    var addresses: List<Address> = emptyList()

    companion object {          //Equivalent of public static var.
        var ravenID: Int = 0
        var currentLongitude: Double = 100.0
        var currentLatitude: Double = 100.0
        val emptyRaven = Raven(Int.MAX_VALUE, "0", "0", "0", 0.0, 0.0, true)
        var ravenArray: Array<Raven> = arrayOf<Raven>(emptyRaven, emptyRaven, emptyRaven)
        var mTracking = false
    }

    lateinit var deletePopupDialog: Dialog

    val TAG = "PrimaryLog"

    var gpsService: BackgroundService? = null
    var connectionEstablished: Boolean = false
    var appIsOn = false

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //  notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val serviceIntent = Intent(this, BackgroundService::class.java)
        this.startService(serviceIntent)
        this.bindService(serviceIntent, myConnection, Context.BIND_AUTO_CREATE)


        Log.i(TAG, "ONCREATE")


        deletePopupDialog = Dialog(this)
        deletePopupDialog.findViewById<View>(R.layout.popup_delete_raven)

        DatabaseApp.component.injectMain(this)


        val permissionList = listOf<String>(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.SEND_SMS
        )

        req_permission = RequestPermission(this, permissionList, PermissionsRequestCode)
        req_permission.checkPermissions()


        offsign.alpha = 0f

        addContactfab.setOnClickListener {

            val addNewRavenIntent = Intent(applicationContext, NewRaven::class.java)
            startActivity(addNewRavenIntent)


        }

        //switch control
        var toggle = findViewById(R.id.switchonoff) as Switch

        if (NewRaven.appRunning == null || NewRaven.appRunning == false) {
            toggle.isChecked = false
            turnScreenOff()
        } else if (NewRaven.appRunning == true) {
            toggle.isChecked = true
            turnScreenOn()
        }

        toggle.setOnCheckedChangeListener() { buttonView, isChecked ->

            if (isChecked) {

                Toast.makeText(applicationContext, "App turned on!", Toast.LENGTH_LONG).show()
                turnScreenOn()
                gpsService!!.startTracking()
                mTracking = true
                appIsOn = true

            } else {

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

        //To continuously updateUI every 10s.
        fixedRateTimer("timer", false, 0, 2000) {
            this@MainActivity.runOnUiThread {
                if (appIsOn == true) {
                    updateUI()
                    Log.i(TAG, "UPDATEUI")
                }
            }
        }

    }

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

    private fun showDeleteRavenPopup(ravenNo: Int) {

        deletePopupDialog.setContentView(R.layout.popup_delete_raven)
        var transparentColor = ColorDrawable(Color.TRANSPARENT)
        deletePopupDialog.window.setBackgroundDrawable(transparentColor)
        deletePopupDialog.show()

        var closePopupBtn: ImageButton = deletePopupDialog.findViewById(R.id.closePopup)
        var deleteBtn: Button = deletePopupDialog.findViewById(R.id.deleteBtn)

        closePopupBtn.setOnClickListener {

            deletePopupDialog.dismiss()

        }

        deleteBtn.setOnClickListener {

            val ravenData = ravenProvider.getAll()

            Log.d("RAVEN NO : ", "" + ravenNo)

            if (ravenData.lastIndex >= ravenNo) {
                ravenProvider.delete(ravenData[ravenNo])
                Toast.makeText(this, "Raven deleted.", Toast.LENGTH_SHORT).show()
                updateUI()
                deletePopupDialog.dismiss()
            }

        }

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

    private fun updateUI() {

        val ravenData = ravenProvider.getAll()

        if (ravenData.isNotEmpty()) {
            populateRavenArray(ravenData)
        }

        var jsonData = Gson().toJson(ravenData)
        var jsonArray = JSONArray(jsonData)

        var nameArray = arrayOfNulls<String>(3)
        var usableArray = arrayOfNulls<Boolean>(3)        //For next update = Raven shut down for certain time.

        for (jsonIndex in 0..(jsonArray.length() - 1)) {

            if (jsonIndex < NewRaven.MAX_NBR_OF_RAVENS) {

                Log.d("JSON", jsonArray.getJSONObject(jsonIndex).getString("name"))
                nameArray[jsonIndex] = jsonArray.getJSONObject(jsonIndex).getString("name")
                usableArray[jsonIndex] = jsonArray.getJSONObject(jsonIndex).getBoolean("usable") //For next update = Raven shut down for certain time.

            }

        }

        currentName.text = nameArray[0]
        if (currentName.text.equals("")) {
            currentName.text = "No Raven"
        }
        else if(usableArray[0] == false) {        //For next update = Raven shut down for certain time.
            currentName.text = nameArray[0] + " (OFF)"
            Log.i(TAG, "Raven is set to RED.")
        }
        currentName2.text = nameArray[1]
        if (currentName2.text.equals("")) {
            currentName2.text = "No Raven"
        }
       else if(usableArray[1] == false) {    //For next update = Raven shut down for certain time.
            currentName.setTextColor(Color.RED)
        }
        currentName3.text = nameArray[2]
        if (currentName3.text.equals("")) {
            currentName3.text = "No Raven"
        }
        else if(usableArray[2] == false) {        //For next update = Raven shut down for certain time.
            currentName.setTextColor(Color.RED)
        }

        Log.i(TAG, "CurrentLongitude: " + currentLongitude + " CurrentLatitude: " + currentLatitude)
        myLongitude.text = "Longitude: " + roundCoordinates(currentLongitude) + "°"
        myLatitude.text = "Latitude: " + roundCoordinates(currentLatitude) + "°"

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

        // var ravArr : Array<Raven> = arrayOf(emptyRaven, emptyRaven, emptyRaven)
        for (i in 0..(ravenData.size - 1)) {
            ravenArray[i] = ravenData[i]
            if (ravenArray[i].id != Int.MAX_VALUE) {
                Log.i("POPULATE", "Raven " + ravenArray!![i].name + " - " + ravenArray!![i].phoneNo)
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

    private fun roundCoordinates(coordinate: Double?): Double {    //function to round to 2 decimal places our GPS coordinates.

        val result = String.format("%.2f", coordinate)
        var roundedValue = 0.0
        try {
            roundedValue = java.lang.Double.parseDouble(result)
        } catch (ex: NumberFormatException) {
        }

        return roundedValue

    }

    //This function disables the back button.
    override fun onBackPressed() {
        // Do Here what ever you want do on back press;
    }


}
