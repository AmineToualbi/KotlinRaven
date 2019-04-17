package com.example.jacobgraves.myapplication.view.GPSUtils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import java.lang.Exception
import java.lang.IllegalArgumentException
import android.support.v4.app.NotificationCompat
import android.R.attr.visibility
import android.R
import android.graphics.Color
import android.os.Build
import android.support.annotation.RequiresApi
import android.telephony.SmsManager
import com.example.jacobgraves.myapplication.view.SMSUtils.SMSManager
import android.app.PendingIntent
import com.example.jacobgraves.myapplication.view.MainActivity


class BackgroundService : Service() {

    val binder : LocationServiceBinder = LocationServiceBinder()
    val TAG = "BackgroundService"
    var mLocationListener : LocationListener? = null
    var mLocationManager : LocationManager? = null
    var notificationManager : NotificationManager? = null
    var smsManager = SMSManager()

    val LOCATION_INTERVAL : Long = 1000
    val LOCATION_DISTANCE : Float = 10f

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    inner class LocationListener(provider: String) : android.location.LocationListener {

        var lastLocation: Location? = null
        var mLastLocation: Location? = null

        init {
            mLastLocation = Location(provider)
            Log.i(TAG, "LOCATION LISTENER CREATED.")
        }

        override fun onLocationChanged(location: Location?) {
            mLastLocation = location!!
            Log.i(TAG, "LocationChanged " + location)
            MainActivity.currentLongitude = location.longitude
            MainActivity.currentLatitude = location.latitude

            for(i in 0 .. (MainActivity.ravenArray.size-1)) {

                //MaxValue is used as a placeholder notifying empty ravens.
                if(MainActivity.ravenArray[i].id != Int.MAX_VALUE) {

                    if(location.longitude == MainActivity.ravenArray[i].longitude
                    && location.latitude == MainActivity.ravenArray[i].latitude) {


                        val intent = Intent(applicationContext, BackgroundService::class.java)
                        val pi = PendingIntent.getActivity(applicationContext, 0, intent, 0)

                        Log.i(TAG, "SENDMSG TO " + MainActivity.ravenArray[i].phoneNo)
                        smsManager.sendSMS(MainActivity.ravenArray[i].phoneNo,
                                MainActivity.ravenArray[i].message, pi)

                    }
                }
            }
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            Log.i(TAG, "onStatusChanged " + status)
        }

        override fun onProviderEnabled(provider: String?) {
            Log.i(TAG, "onProviderEnabled " + provider)
        }

        override fun onProviderDisabled(provider: String?) {
            Log.i(TAG, "onProviderDisabled " + provider)
        }

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        super.onStartCommand(intent, flags, startId)
        return START_NOT_STICKY

    }

    override fun onCreate() {
        Log.i(TAG, "OnCreate")

       /* val channelId =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    createNotificationChannel("my_service", "My Background Service")
                } else {
                    // If earlier version channel ID is not used
                    // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
                    ""
                }

        val notificationBuilder = NotificationCompat.Builder(this, channelId )
        val notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_lock_idle_alarm)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build()*/

        startForeground(12345678, getNotification())

    }

    override fun onDestroy() {
        super.onDestroy()
        if(mLocationManager != null) {
            try {
                mLocationManager!!.removeUpdates(mLocationListener)
            }
            catch(e: Exception) {
                Log.i(TAG, "Fail to remove location listeners, ignore.", e)
            }
        }
    }

    private fun initializeManager() {
        if(mLocationManager == null) {
            mLocationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        }
    }

    fun startTracking() {
        initializeManager()
        mLocationListener = LocationListener(LocationManager.GPS_PROVIDER)

        try {
            mLocationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    LOCATION_INTERVAL, LOCATION_DISTANCE, mLocationListener)
        }
        catch (e: SecurityException) {

        }
        catch (e: IllegalArgumentException) {

        }
    }

    fun stopTracking() {
        this.onDestroy()
    }

    fun getNotification() : Notification {

        var channel  = NotificationChannel("channel_01", "My Channel", NotificationManager.IMPORTANCE_DEFAULT)

        notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager!!.createNotificationChannel(channel)

        var notifBuilder =  NotificationCompat.Builder(this, "channel_01")

        var mBuilder = notifBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_menu_mylocation)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setContentTitle("Raven")
                .setContentText("Raven is currently using your location.")

        return mBuilder.build()

    }


    inner class LocationServiceBinder : Binder() {
        fun getService() : BackgroundService {
            return this@BackgroundService
        }
    }


}