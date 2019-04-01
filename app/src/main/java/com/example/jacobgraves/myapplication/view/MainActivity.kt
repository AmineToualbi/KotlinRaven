package com.example.jacobgraves.myapplication.view

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ClipDescription
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
import android.view.View
import android.widget.Switch
import android.widget.Toast
import com.example.jacobgraves.myapplication.R
import kotlinx.android.synthetic.main.activity_main.*

private var locationManager:LocationManager? = null

class MainActivity : AppCompatActivity() {
    private val requestSendSms: Int = 2
    public var ifEnableSms: Boolean = false;
    private var notificationManager:NotificationManager ?= null

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
                sendNotification()
            } else {
                Toast.makeText(applicationContext, "App paused!", Toast.LENGTH_LONG).show()
                addContactfab.isEnabled = false
                mainView.alpha = 0.5f;
                offsign.alpha = 1f;

            }
        }
        //notification
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel("com.example.jacobgraves.myapplication.view","Message Status", "Check send message" )



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

    private fun createNotificationChannel(id: String, name: String, description: String) {
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(id, name, importance)
        channel.description = description
        channel.enableLights(true)
        channel.lightColor = Color.RED
        channel.enableVibration(true)
        channel.vibrationPattern =
                longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
        notificationManager?.createNotificationChannel(channel)
    }

    fun sendNotification(){
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


    }


}
