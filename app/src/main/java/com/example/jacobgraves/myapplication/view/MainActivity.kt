package com.example.jacobgraves.myapplication.view

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.LocationManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.Switch
import android.widget.Toast
import com.example.jacobgraves.myapplication.R
import com.example.jacobgraves.myapplication.view.GPSUtils.MyLocationService
import com.example.jacobgraves.myapplication.view.application.DatabaseApp
import kotlinx.android.synthetic.main.activity_main.*
import com.example.jacobgraves.myapplication.view.permissions.RequestPermission
import com.example.jacobgraves.myapplication.view.providers.IRavenProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import org.json.JSONArray
import java.net.CacheRequest
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
        var instance: MainActivity? = null

        fun getMainInstance(): MainActivity {
            return instance!!
        }
    }

    fun updateTextView(value: String) {
        this@MainActivity.runOnUiThread {
            myLongitude.text = value
        }
    }

    lateinit var deletePopupDialog: Dialog

    val TAG = "LOGGING DATA"

    lateinit var locationRequest: LocationRequest
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
      //  notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        instance = this

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(object:PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                        updateLocation()
                    }

                    override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken?) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                        Toast.makeText(this@MainActivity, "You must accept this permission", Toast.LENGTH_SHORT).show()
                    }
                }).check()

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
        toggle.setOnCheckedChangeListener { buttonView, isChecked ->

            if (isChecked) {

                Toast.makeText(applicationContext, "App turned on!", Toast.LENGTH_LONG).show()
                this.deleteDatabase("RavenDB.db")
                turnScreenOn()

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

    }

    private fun updateLocation() {
        buildLocationRequest()

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            return
        }
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, getPendingIntent())

    }

    private fun getPendingIntent() : PendingIntent? {
        val intent = Intent(this@MainActivity, MyLocationService::class.java)
        intent.setAction(MyLocationService.ACTION_PROCESS_UPDATE)
        return PendingIntent.getBroadcast(this@MainActivity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun buildLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 3000
        locationRequest.smallestDisplacement = 10f
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
                    toast("Permissions granted.")
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

    }

    override fun onResume() {
        super.onResume()
        updateLocation()
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
