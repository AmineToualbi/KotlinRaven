package com.example.jacobgraves.myapplication.view.SMSUtils

import android.app.PendingIntent
import android.telephony.SmsManager

class SMSManager() {

     fun sendSMS(mNumber: String, mMessage: String, pendingIntent: PendingIntent) {

            SmsManager.getDefault().sendTextMessage(mNumber, null, mMessage, null, null)
            //Toast.makeText(this, "SMS sent.", Toast.LENGTH_SHORT).show()

    }

}