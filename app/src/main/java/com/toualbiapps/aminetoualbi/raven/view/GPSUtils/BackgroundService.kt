package com.toualbiapps.aminetoualbi.raven.view.GPSUtils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.util.Log
import java.lang.Exception
import java.lang.IllegalArgumentException
import android.support.v4.app.NotificationCompat
import android.R
import android.annotation.TargetApi
import com.toualbiapps.aminetoualbi.raven.view.SMSUtils.SMSManager
import android.app.PendingIntent
import android.content.SharedPreferences
import android.net.Uri
import android.os.*
import com.toualbiapps.aminetoualbi.raven.view.MainActivity
import com.toualbiapps.aminetoualbi.raven.view.application.DatabaseApp
import com.toualbiapps.aminetoualbi.raven.view.providers.IRavenProvider
import javax.inject.Inject
import android.net.Uri.fromParts
import android.support.v4.content.ContextCompat.startActivity
import android.support.v4.content.ContextCompat.startActivity
import android.support.v4.content.ContextCompat.startActivity
import android.support.v4.content.ContextCompat.startActivity










//Class handling the Foreground Service used to update GPS location in the background of the app & act in function.
class BackgroundService : Service() {


    val binder : LocationServiceBinder = LocationServiceBinder()

    val TAG = "BackgroundService"

    var mLocationListener : LocationListener? = null
    var mLocationManager : LocationManager? = null

    var notificationManager : NotificationManager? = null

    var smsManager = SMSManager()

    val LOCATION_INTERVAL : Long = 1000         //Update every second.
    val LOCATION_DISTANCE : Float = 10f         //Update every 10 meters.

    companion object {
        var ravenLockedTimeLeft : Long = 36000000       //Original value corresponds to 10h in ms.
        var pref : SharedPreferences? = null            //Persistent storage of locked time.
        var editor : SharedPreferences.Editor? = null       //Editor for pref.
        var persistentNameArray: Array<String?> = arrayOfNulls(3)
    }

    //Inject database provider.
    @Inject
    lateinit var ravenProvider: IRavenProvider

    val tenHoursInMin : Long = 30000


    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }


    //Specialized LocationListener for the Service.
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

            //Get rounded value of GPS coordinates.
            val roundedLongitude = roundCoordinates(location.longitude)
            val roundedLatitude = roundCoordinates(location.latitude)

            //Notify MainActivity of GPS change.
            MainActivity.currentLongitude = roundedLongitude
            MainActivity.currentLatitude = roundedLatitude

            Log.i(TAG, "Location: " + roundedLongitude + " - " + roundedLatitude)
            Log.i(TAG, "Raven Location: " + roundCoordinates(MainActivity.ravenArray[0].longitude) + " - " +
                    roundCoordinates(MainActivity.ravenArray[0].latitude))

            //Go through the Ravens stored in ravenArray.
            for(i in 0 .. (MainActivity.ravenArray.size-1)) {

                //MaxValue is used as a placeholder notifying empty ravens.
                if(MainActivity.ravenArray[i].id != Int.MAX_VALUE) {

                    //Get distance between user location & final location of raven.
                    val distanceToRavenLocation = distanceBetweenLocations(location.latitude,
                            location.longitude, MainActivity.ravenArray[i].latitude,
                            MainActivity.ravenArray[i].longitude)

                    //If user is close to final location -> send SMS, push notification, & lock raven.
                    if(userInRange(distanceToRavenLocation) == true) {

                        val intent = Intent(applicationContext, BackgroundService::class.java)
                        val pi = PendingIntent.getActivity(applicationContext, 0, intent, 0)

                        Log.i(TAG, "SENDMSG TO " + MainActivity.ravenArray[i].phoneNo)

                        smsManager.sendSMS(MainActivity.ravenArray[i].phoneNo,
                                MainActivity.ravenArray[i].message, pi)
                      //  composeMmsMessage(MainActivity.ravenArray[i].message)

                        getRavenSentNotification(MainActivity.ravenArray[i].name)

                        ravenProvider.delete(MainActivity.ravenArray[i])

                     //   lockRaven(MainActivity.ravenArray[i], i)

                    }
                }
            }
        }


    /*    fun composeMmsMessage(message: String) {
            val intent = Intent(Intent.ACTION_SEND).apply {
                data = Uri.parse("smsto:")  // This ensures only SMS apps respond
                putExtra("sms_body", message)
            }
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            }
        }*/

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
                .setSubText("The raven is now deleted.")
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

   /* fun roundCoordinatesToOneDecimal(coordinate: Double) : Double{
        val number3digits:Double = String.format("%.3f", coordinate).toDouble()
        val number2digits:Double = String.format("%.2f", number3digits).toDouble()
        val solution:Double = String.format("%.1f", number2digits).toDouble()
        return solution
    }*/

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



    inner class LocationServiceBinder : Binder() {
        fun getService() : BackgroundService {
            return this@BackgroundService
        }
    }


}




/*
    //This function sets a timer for 10 hours & makes the Raven not usable/
    private fun lockRaven(raven: Raven, index: Int) {

        var lockedRaven = raven
        lockedRaven.usable = false
        ravenProvider.update(lockedRaven)

        if(index == 0)
        startCountDownLockedRaven(raven, index)
        else if(index == 1)
            startCountDownLockedRaven1(raven, index)

        persistentNameArray[0] = "Amine"
        persistentNameArray[1] = "Jeffrey"

    }

    //Countdown for 10 hours for raven corresponding to index.
    private fun startCountDownLockedRaven(raven: Raven, index: Int) {

        //Retrieve lockedRavenTimeLeft from SharedPrefs. If doesn't exist, set to 10 hours.
        //editor!!.putLong("lockedRavenTimeLeft" + index, 60000)
        //editor!!.commit()
        //val lockedRavenTimeLeft = pref!!.getLong("lockedRavenTimeLeft" + index, 30000)
        val lockedRavenTimeLeft : Long = 60000
        Log.i(TAG, "LockedRavenTimeLeft @ start " + index + ": " + lockedRavenTimeLeft)

//36000000
        val timer = object: CountDownTimer(lockedRavenTimeLeft,1000) {    //Change 30s to lockedRavenTimeLeft
            override fun onTick(millisUntilFinished: Long) {
               // if(MainActivity.ravenArray[index].name != raven.name) {
                if(persistentNameArray[index] != raven.name) {
                    Log.i(TAG, "Raven " + raven.name + " was deleted.")
                    editor!!.putLong("lockedRavenTimeLeft" + index, tenHoursInMin)
                    editor!!.commit()
                    Log.i(TAG, "LockedRavenTimeLeft deleted " + index + ": " + pref!!.getLong("lockedRavenTimeLeft" + index,0))
                    for(i in 0..(MainActivity.ravenArray.size-1)) {
                        if(MainActivity.ravenArray[i].id != Int.MAX_VALUE) {
                            Log.i(TAG, "Raven " + i + ": " + MainActivity.ravenArray[i].name)
                        }
                    }
                    cancel()
                    for(i in 0..(MainActivity.ravenArray.size-1)) {
                        if(MainActivity.ravenArray[i].id != Int.MAX_VALUE) {
                            Log.i(TAG, "Raven " + i + ": " + MainActivity.ravenArray[i].name)
                        }
                    }

                }
                Log.i(TAG, "ravenArray[index] = " + MainActivity.ravenArray[index].name)
                Log.i(TAG, "LockedRavenTimeLeft" + index + ": " + millisUntilFinished)
                editor!!.putLong("lockedRavenTimeLeft" + index, millisUntilFinished)
                editor!!.commit()
                for(i in 0..(MainActivity.ravenArray.size-1)) {
                    if(MainActivity.ravenArray[i].id != Int.MAX_VALUE) {
                        Log.i(TAG, "Raven " + i + ": " + MainActivity.ravenArray[i].name)
                    }
                }
            }

            override fun onFinish() {
                editor!!.remove("lockedRavenTimeLeft" + index)
                editor!!.commit()
              //  ravenLockedTimeLeft = 36000000      //10h in ms.
                unlockRaven(raven)
                Log.i(TAG, "Raven Unlocked.")

            }
        }

        timer.start()


    }




    //Countdown for 10 hours for raven corresponding to index.
    private fun startCountDownLockedRaven1(raven: Raven, index: Int) {

        //Retrieve lockedRavenTimeLeft from SharedPrefs. If doesn't exist, set to 10 hours.
       // editor!!.putLong("lockedRavenTimeLeft" + index, 60000)
       // editor!!.commit()
      //  val lockedRavenTimeLeft = pref!!.getLong("lockedRavenTimeLeft" + index, 30000)
        val lockedRavenTimeLeft : Long = 60000
        Log.i(TAG, "LockedRavenTimeLeft @ start " + index + ": " + lockedRavenTimeLeft)

//36000000
        val timer = object: CountDownTimer(lockedRavenTimeLeft,1000) {    //Change 30s to lockedRavenTimeLeft
            override fun onTick(millisUntilFinished: Long) {
                //if(MainActivity.ravenArray[index].name != raven.name) {
                if(persistentNameArray[index] != raven.name) {
                    Log.i(TAG, "Raven " + raven.name + " was deleted.")
                    editor!!.putLong("lockedRavenTimeLeft" + index, tenHoursInMin)
                    editor!!.commit()
                    for(i in 0..(MainActivity.ravenArray.size-1)) {
                        if(MainActivity.ravenArray[i].id != Int.MAX_VALUE) {
                            Log.i(TAG, "Raven " + i + ": " + MainActivity.ravenArray[i].name)
                        }
                    }
                    Log.i(TAG, "LockedRavenTimeLeft deleted " + index + ": " + pref!!.getLong("lockedRavenTimeLeft" + index,0))
                    cancel()
                    for(i in 0..(MainActivity.ravenArray.size-1)) {
                        if(MainActivity.ravenArray[i].id != Int.MAX_VALUE) {
                            Log.i(TAG, "Raven " + i + ": " + MainActivity.ravenArray[i].name)
                        }
                    }

                }
                Log.i(TAG, "ravenArray[index] = " + MainActivity.ravenArray[index].name)
                Log.i(TAG, "LockedRavenTimeLeft" + index + ": " + millisUntilFinished)
                editor!!.putLong("lockedRavenTimeLeft" + index, millisUntilFinished)
                editor!!.commit()
                for(i in 0..(MainActivity.ravenArray.size-1)) {
                    if(MainActivity.ravenArray[i].id != Int.MAX_VALUE) {
                        Log.i(TAG, "Raven " + i + ": " + MainActivity.ravenArray[i].name)
                    }
                }
            }

            override fun onFinish() {
                editor!!.remove("lockedRavenTimeLeft" + index)
                editor!!.commit()
                //  ravenLockedTimeLeft = 36000000      //10h in ms.
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
*/