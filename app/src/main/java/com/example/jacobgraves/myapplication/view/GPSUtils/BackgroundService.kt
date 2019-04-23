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
import android.util.Log
import java.lang.Exception
import java.lang.IllegalArgumentException
import android.support.v4.app.NotificationCompat
import android.R.attr.visibility
import android.R
import android.annotation.TargetApi
import android.graphics.Color
import android.support.annotation.RequiresApi
import android.telephony.SmsManager
import com.example.jacobgraves.myapplication.view.SMSUtils.SMSManager
import android.app.PendingIntent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.*
import android.support.v4.content.ContextCompat
import android.view.View
import com.example.jacobgraves.myapplication.view.MainActivity
import com.example.jacobgraves.myapplication.view.application.DatabaseApp
import com.example.jacobgraves.myapplication.view.model.Raven
import com.example.jacobgraves.myapplication.view.providers.IRavenProvider
import kotlinx.android.synthetic.main.activity_loading.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class BackgroundService : Service() {

    val binder : LocationServiceBinder = LocationServiceBinder()
    val TAG = "BackgroundService"
    var mLocationListener : LocationListener? = null
    var mLocationManager : LocationManager? = null
    var notificationManager : NotificationManager? = null
    var smsManager = SMSManager()

    val LOCATION_INTERVAL : Long = 1000
    val LOCATION_DISTANCE : Float = 10f

    companion object {
        var ravenLockedTimeLeft : Long = 36000000       //Original value corresponds to 10h in ms.
        var pref : SharedPreferences? = null
        var editor : SharedPreferences.Editor? = null
    }
    @Inject           //For next update = Raven shut down for certain time.
    lateinit var ravenProvider: IRavenProvider


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

            val roundedLongitude = roundCoordinatesToOneDecimal(location.longitude)
            val roundedLatitude = roundCoordinatesToOneDecimal(location.latitude)

            Log.i(TAG, "Location: " + roundedLongitude + " - " + roundedLatitude)
            Log.i(TAG, "Raven Location: " + roundCoordinatesToOneDecimal(MainActivity.ravenArray[0].longitude) + " - " +
                    roundCoordinatesToOneDecimal(MainActivity.ravenArray[0].latitude))

            for(i in 0 .. (MainActivity.ravenArray.size-1)) {

                //MaxValue is used as a placeholder notifying empty ravens.
                if(MainActivity.ravenArray[i].id != Int.MAX_VALUE) {

                    val distanceToRavenLocation = distanceBetweenLocations(MainActivity.currentLatitude,
                            MainActivity.currentLongitude, MainActivity.ravenArray[i].latitude,
                            MainActivity.ravenArray[i].longitude)

                    if(userInRange(distanceToRavenLocation) == true) {


                        val intent = Intent(applicationContext, BackgroundService::class.java)
                        val pi = PendingIntent.getActivity(applicationContext, 0, intent, 0)

                        Log.i(TAG, "SENDMSG TO " + MainActivity.ravenArray[i].phoneNo)
                        smsManager.sendSMS(MainActivity.ravenArray[i].phoneNo,
                                MainActivity.ravenArray[i].message, pi)

                        getRavenSentNotification(MainActivity.ravenArray[i].name)

                        lockRaven(MainActivity.ravenArray[i])  //For next update = Raven shut down for certain time.

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

    //Returns distance in KM.
    private fun distanceBetweenLocations(lat1: Double, long1: Double, lat2: Double, long2: Double) : Double {

        val theta: Double = long1 - long2
        var dist : Double =Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta))
        dist = Math.acos(dist)
        dist = rad2deg(dist)
        dist = dist * 60 * 1.1515
        dist = dist * 1.609344     //Conversion from Miles to KM.
        return dist

    }

    private fun deg2rad(deg: Double) : Double {
        return (deg * Math.PI / 180.0)
    }

    private fun rad2deg(rad: Double): Double {
        return rad * 180.0 / Math.PI
    }

    private fun userInRange(distance: Double) : Boolean {

        if(distance <= .5) {            //Radius = 500m.
            return true
        }
        return false
    }

    //This function sets a timer for 10 hours & makes the Raven not usable.
    private fun lockRaven(raven: Raven) {         //For next update = Raven shut down for certain time.

        var lockedRaven = raven
        lockedRaven.usable = false
        ravenProvider.update(lockedRaven)

        startCountDownLockedRaven(raven)

    }

    private fun startCountDownLockedRaven(raven: Raven) {

        //Retrieve lockedRavenTimeLeft from SharedPrefs. If doesn't exist, set to 10 hours.
        val lockedRavenTimeLeft = pref!!.getLong("lockedRavenTimeLeft", 36000000)

        val timer = object: CountDownTimer(30000,1000) {
            override fun onTick(millisUntilFinished: Long) {
                Log.i(TAG, "LockedRavenTimeLeft: " + millisUntilFinished)
                editor!!.putLong("lockedRavenTimeLeft", millisUntilFinished)
                editor!!.commit()
            }

            override fun onFinish() {
                editor!!.putLong("lockedRavenTimeLeft", 0)
                editor!!.commit()
                ravenLockedTimeLeft = 36000000      //10h in ms.
                unlockRaven(raven)
                Log.i(TAG, "Raven Unlocked.")

            }
        }

        timer.start()
    }

    private fun unlockRaven(raven: Raven) {

        var lockedRaven = raven
        lockedRaven.usable = true
        ravenProvider.update(lockedRaven)
        getRavenUnlockedNotification(raven.name)

    }

    /*//Timer function for lockRaven()
    private fun timer(millisInFuture:Long,countDownInterval:Long, raven: Raven):CountDownTimer {

        return object : CountDownTimer(millisInFuture, countDownInterval) {     //For next update = Raven shut down for certain time.
            override fun onTick(millisUntilFinished: Long) {
                val timeRemaining = timeString(millisUntilFinished)

                Log.i(TAG, "Time Remaining : " + timeRemaining)

            }

            override fun onFinish() {
                Log.i(TAG, "Time Remaining : DONE")
                unlockRaven(raven)

            }
        }

    }*/

    //For next update = Raven shut down for certain time.
    /*private fun unlockRaven(raven: Raven) {

        var lockedRaven = raven
        lockedRaven.usable = true
        ravenProvider.update(lockedRaven)

    }*/

    //For next update = Raven shut down for certain time.
    //Testing fct to print time during timer.
    /*private fun timeString(millisUntilFinished:Long):String{
        var millisUntilFinished:Long = millisUntilFinished
        val days = TimeUnit.MILLISECONDS.toDays(millisUntilFinished)
        millisUntilFinished -= TimeUnit.DAYS.toMillis(days)

        val hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
        millisUntilFinished -= TimeUnit.HOURS.toMillis(hours)

        val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
        millisUntilFinished -= TimeUnit.MINUTES.toMillis(minutes)

        val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished)

        // Format the string
        return String.format(
                Locale.getDefault(),
                "%02d day: %02d hour: %02d min: %02d sec",
                days,hours, minutes,seconds
        )
    }*/

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        super.onStartCommand(intent, flags, startId)
        return START_NOT_STICKY

    }

    override fun onCreate() {
        Log.i(TAG, "OnCreate")

        startForeground(12345678, getNotification())

        pref = applicationContext.getSharedPreferences("MyPref", Context.MODE_PRIVATE)
        editor = pref!!.edit()

        DatabaseApp.component.injectService(this)     //For next update = Raven shut down for certain time.

        for(i in 0 .. (MainActivity.ravenArray.size-1)) {

            //MaxValue is used as a placeholder notifying empty ravens.
            if (MainActivity.ravenArray[i].usable == false) {
                startCountDownLockedRaven(MainActivity.ravenArray[i])
            }
        }


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

    @TargetApi(Build.VERSION_CODES.O)
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
                .setSubText("Switch the app to OFF to save battery")

        return mBuilder.build()

    }

    @TargetApi(Build.VERSION_CODES.O)
    fun getRavenSentNotification(name: String) {

        var channel = NotificationChannel("channel_02", "My Channel1", NotificationManager.IMPORTANCE_HIGH)

        notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager!!.createNotificationChannel(channel)

        var notifBuilder = NotificationCompat.Builder(this, "channel_02")

        var mBuilder = notifBuilder.setOngoing(false)
                .setSmallIcon(R.drawable.stat_notify_chat)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(Notification.CATEGORY_MESSAGE)
                .setContentTitle("Raven")
                .setContentText("A raven was sent to " + name + ".")
                .setSubText("The Raven is now disabled for 10 hours")
                .build()

         notificationManager!!.notify(101, mBuilder)

    }

    @TargetApi(Build.VERSION_CODES.O)
    fun getRavenUnlockedNotification(name: String) {

        var channel = NotificationChannel("channel_03", "My Channel2", NotificationManager.IMPORTANCE_HIGH)

        notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager!!.createNotificationChannel(channel)

        var notifBuilder = NotificationCompat.Builder(this, "channel_03")

        var mBuilder = notifBuilder.setOngoing(false)
                .setSmallIcon(R.drawable.alert_light_frame)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(Notification.CATEGORY_REMINDER)
                .setContentTitle("Raven")
                .setContentText(name + "'s Raven is unlocked and usable.")
                .build()

        notificationManager!!.notify(101, mBuilder)

    }

    fun roundCoordinatesToOneDecimal(coordinate: Double) : Double{
        val number3digits:Double = String.format("%.3f", coordinate).toDouble()
        val number2digits:Double = String.format("%.2f", number3digits).toDouble()
        val solution:Double = String.format("%.1f", number2digits).toDouble()
        return solution
    }


    inner class LocationServiceBinder : Binder() {
        fun getService() : BackgroundService {
            return this@BackgroundService
        }
    }


}