package com.example.jacobgraves.myapplication.view

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.location.*
import android.opengl.Visibility
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.support.v4.app.NotificationCompat
import android.util.Log
import android.widget.Switch
import android.widget.Toast
import com.example.jacobgraves.myapplication.R
import kotlinx.android.synthetic.main.activity_main.*

private var locationManager:LocationManager? = null

class MainActivity : AppCompatActivity() {
    private val requestSendSms: Int = 2
    public var ifEnableSms: Boolean = false;

    companion object {          //Equivalent of public static var.
        var ravenID: Int = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        offsign.alpha = 0f
        //Persistent LocationManager reference
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager?

        try {
            locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 10f, locationListener)
        }catch (ex: SecurityException){
            Log.d("myTag", "Security Exception, no location available")
        }

        addContactfab.setOnClickListener {

            val addNewRavenIntent = Intent(applicationContext, NewRaven::class.java)
            startActivity(addNewRavenIntent)


        }

        //switch control
        switchonoff.isChecked = true
        val toggle = findViewById<Switch>(R.id.switchonoff) as Switch
        toggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Toast.makeText(applicationContext, "App resumed!", Toast.LENGTH_LONG).show()
                addContactfab.isEnabled = true
                mainView.alpha = 1f;
                offsign.alpha = 0f;
            } else {
                Toast.makeText(applicationContext, "App paused!", Toast.LENGTH_LONG).show()
                addContactfab.isEnabled = false
                mainView.alpha = 0.5f;
                offsign.alpha = 1f;

            }
        }
        //notification
        var builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle("")
                .setContentText("Message sent")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), requestSendSms)
        }



    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == requestSendSms) ifEnableSms = true;

    }



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

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
