package com.toualbiapps.aminetoualbi.raven.view.SMSUtils

import android.app.PendingIntent
import android.telephony.SmsManager


//Class to send a SMS to a specified number.
class SMSManager() {

     fun sendSMS(mNumber: String, mMessage: String, pendingIntent: PendingIntent) {

            SmsManager.getDefault().sendTextMessage(mNumber, null, mMessage, null, null)

    }

}