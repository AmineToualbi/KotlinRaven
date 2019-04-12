package com.example.jacobgraves.myapplication.view.SMSUtils

import android.telephony.SmsManager

class SMSManager(number: String, message: String) {

    var mNumber: String = number
    var mMessage: String = message

    private fun sendSMS() {

            SmsManager.getDefault().sendTextMessage(mNumber, null, mMessage, null, null)
            //Toast.makeText(this, "SMS sent.", Toast.LENGTH_SHORT).show()

    }

}