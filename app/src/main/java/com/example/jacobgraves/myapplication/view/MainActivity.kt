package com.example.jacobgraves.myapplication.view

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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
import kotlinx.android.synthetic.main.activity_main.*
import com.example.jacobgraves.myapplication.view.permissions.RequestPermission
import com.example.jacobgraves.myapplication.view.providers.IRavenProvider
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.gson.Gson
import com.karumi.dexter.Dexter
import org.json.JSONArray
import javax.inject.Inject


private var locationManager:LocationManager? = null
private val PermissionsRequestCode = 234


class MainActivity : AppCompatActivity() {

    private val requestSendSms: Int = 2
    public var ifEnableSms: Boolean = false

    private lateinit var req_permission: RequestPermission

    private var notificationManager:NotificationManager ?= null

    @Inject
    lateinit var ravenProvider: IRavenProvider

    companion object {          //Equivalent of public static var.
        var ravenID: Int = 0
    }

    lateinit var deletePopupDialog: Dialog

    val TAG = "PrimaryLog"

    var gpsService: BackgroundService? = null
    var mTracking : Boolean = false
    var connectionEstablished : Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
      //  notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val serviceIntent = Intent(this, BackgroundService::class.java)
        this.startService(serviceIntent)
        this.bindService(serviceIntent, myConnection, Context.BIND_AUTO_CREATE)




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

        //Persistent LocationManager reference
        //locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager?

       /* try {

            //req_permission.processPermissionsResult(PermissionRequestCode,permissionList,)
            locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 10f, locationListener)
        } catch (ex: SecurityException) {
            Log.d("myTag", "Security Exception, no location available")
        }*/

        addContactfab.setOnClickListener {

            val addNewRavenIntent = Intent(applicationContext, NewRaven::class.java)
            startActivity(addNewRavenIntent)


        }

        //switch control
        var toggle = findViewById(R.id.switchonoff) as Switch
        toggle.isChecked = true

        toggle.setOnCheckedChangeListener() { buttonView, isChecked ->

            if (isChecked) {

                Toast.makeText(applicationContext, "App turned on!", Toast.LENGTH_LONG).show()
                this.deleteDatabase("RavenDB.db")
                turnScreenOn()
                gpsService!!.startTracking()
                mTracking = true

            }
            else {

                Toast.makeText(applicationContext, "App turned off!", Toast.LENGTH_LONG).show()
                // FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.addContactfab)
                turnScreenOff()

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

        if(toggle.isChecked == true && connectionEstablished == true) {
            gpsService!!.startTracking()
            mTracking = true
        }

    }

    private val myConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            var name = name!!.className
            if(name.endsWith("BackgroundService")) {
                val binder = service as BackgroundService.LocationServiceBinder
                gpsService = binder.getService()
                Log.i(TAG, "GPS READY.")
                connectionEstablished = true
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            if(name!!.className.equals("BackgroundService")) {
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

        var closePopupBtn : ImageButton = deletePopupDialog.findViewById(R.id.closePopup)
        var deleteBtn : Button = deletePopupDialog.findViewById(R.id.deleteBtn)

        closePopupBtn.setOnClickListener {

            deletePopupDialog.dismiss()

        }

        deleteBtn.setOnClickListener {

            val ravenData = ravenProvider.getAll()

            Log.d("RAVEN NO : ", "" + ravenNo)

            if(ravenData.lastIndex >= ravenNo) {
                ravenProvider.delete(ravenData[ravenNo])
                Toast.makeText(this, "Raven deleted.", Toast.LENGTH_SHORT).show()
                updateUI()
                deletePopupDialog.dismiss()
            }

        }

    }



    /*private val locationListener:LocationListener = object:LocationListener {

        override fun onLocationChanged(location:Location){
            myLongitude.text = getString(R.string.myLatitude, location.longitude)
            myLatitude.text = getString(R.string.myLatitude, location.latitude)
        }

        override fun onProviderEnabled(provider: String?) {
        }

        override fun onProviderDisabled(provider: String?) {
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        }

    }*/


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            PermissionsRequestCode ->{
                val isPermissionsGranted = req_permission.processPermissionsResult(requestCode,permissions,grantResults)

                if(isPermissionsGranted){
                    // Do the task now
                    //toast("Permissions granted.")
                    gpsService!!.startTracking()
                    mTracking = true
                    toast("Start Tracking")
                }else{
                    toast("Permissions denied.")
                }
                return
            }
        }
    }

    private fun updateUI() {

        val ravenData = ravenProvider.getAll()

        var jsonData = Gson().toJson(ravenData)
        var jsonArray = JSONArray(jsonData)

        var nameArray = arrayOfNulls<String>(3)

        for(jsonIndex in 0 .. (jsonArray.length()-1)) {

            if (jsonIndex < NewRaven.MAX_NBR_OF_RAVENS) {

                Log.d("JSON", jsonArray.getJSONObject(jsonIndex).getString("name"))
                nameArray[jsonIndex] = jsonArray.getJSONObject(jsonIndex).getString("name")

            }

        }

        currentName.text = nameArray[0]
        if(currentName.text.equals("")) {
            currentName.text = "No Raven"
        }
        currentName2.text = nameArray[1]
        if(currentName2.text.equals("")) {
            currentName2.text = "No Raven"
        }
        currentName3.text = nameArray[2]
        if(currentName3.text.equals("")) {
            currentName3.text = "No Raven"
        }

      //  myLongitude.text = com.example.jacobgraves.myapplication.view.GPSUtils.LocationListener.longitude.toString() + "\n" +
                //com.example.jacobgraves.myapplication.view.GPSUtils.LocationListener.latitude.toString()

    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }


    fun Context.toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

   /* private fun createNotificationChannel(id: String, name: String, description: String) {
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(id, name, importance)
        channel.description = description
        channel.enableLights(true)
        channel.lightColor = Color.RED
        channel.enableVibration(true)
        channel.vibrationPattern =
                longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
        notificationManager?.createNotificationChannel(channel)
    }*/

    /*fun sendNotification(){
        val notificationID = 101
        val resultIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val channelID = "com.example.jacobgraves.myapplication.view"
        val notification = Notification.Builder(this, channelID)
                .setContentTitle("Raven")
                .setContentText("Message Sent")
                .setSmallIcon(R.drawable.close)
                .setContentIntent(pendingIntent)
                .setNumber(10)
                .build()
        notificationManager?.notify(notificationID, notification)


    }*/



}
