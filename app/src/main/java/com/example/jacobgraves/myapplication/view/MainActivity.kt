package com.example.jacobgraves.myapplication.view

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.location.*
import android.support.v4.app.ActivityCompat
import android.util.Log
import com.example.jacobgraves.myapplication.R
import kotlinx.android.synthetic.main.activity_main.*
import android.widget.Toast
import android.widget.CompoundButton
import android.widget.Switch
import com.example.jacobgraves.myapplication.view.permissions.requestPermission


private var locationManager:LocationManager? = null

class MainActivity : AppCompatActivity() {
    private val requestSendSms: Int = 2
    public var ifEnableSms: Boolean = false;
    private val PermissionRequestCode = 23
    private lateinit var req_permission: requestPermission

    companion object {          //Equivalent of public static var.
        var ravenID: Int = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val permissionList = listOf<String>(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.SEND_SMS
        )

        req_permission = requestPermission(this,permissionList,PermissionRequestCode)

        //Persistent LocationManager reference
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager?

        try {
            req_permission.checkPermissions()
            locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 10f, locationListener)
        }catch (ex: SecurityException){
            Log.d("myTag", "Security Exception, no location available")
        }

        addContactfab.setOnClickListener {

            val addNewRavenIntent = Intent(applicationContext, NewRaven::class.java)
            startActivity(addNewRavenIntent)


        }
        //switch control
        val toggle = findViewById (R.id.switchonoff) as Switch
        toggle.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                Toast.makeText(applicationContext, "Switch on!", Toast.LENGTH_LONG).show()
                addContactfab.isEnabled = true
            } else {
                Toast.makeText(applicationContext, "Switch off!", Toast.LENGTH_LONG).show()
               // FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.addContactfab)
                addContactfab.isEnabled = false
            }
        }


        /*if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), requestSendSms)
        }*/



    }

    /*override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == requestSendSms) ifEnableSms = true;

    }*/



    private val locationListener:LocationListener = object:LocationListener {

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

    }
}
