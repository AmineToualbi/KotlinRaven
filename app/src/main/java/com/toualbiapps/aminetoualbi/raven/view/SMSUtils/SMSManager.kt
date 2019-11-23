package com.toualbiapps.aminetoualbi.raven.view.SMSUtils

import android.app.PendingIntent
import android.content.Context
import android.support.v4.view.ViewPager
import android.telephony.SmsManager
import android.util.Log
import com.creativityapps.gmailbackgroundlibrary.BackgroundMail
import com.toualbiapps.aminetoualbi.raven.view.Common.Common
import java.lang.Exception


//Class to send a SMS to a specified number.
class SMSManager() {

     fun sendSMS(mName: String, mNumber: String, mCarrier: String, mMessage: String, context: Context, pendingIntent: PendingIntent) {

          //  SmsManager.getDefault().sendTextMessage(mNumber, null, mMessage, null, null)
         BackgroundMail.newBuilder(context)
                 .withUsername(Common.appEmailUsername)
                 .withPassword(Common.appEmailPassword)
                 .withSenderName(mName)
                 .withMailTo(mNumber+mCarrier)
                 .withType(BackgroundMail.TYPE_PLAIN)
                 .withSubject("From $mName through Raven")
                 .withBody(mMessage)
                 .withOnSuccessCallback(onSendingCallback)

         Log.e("SMS Manager", "SendSMS() called to $mName at $mNumber & $mCarrier")

     }

    private val onSendingCallback: BackgroundMail.OnSendingCallback = object : BackgroundMail.OnSendingCallback {
        override fun onSuccess() {
            Log.e("SMS Manager", "Message sent.")
        }

        override fun onFail(p0: Exception?) {
            Log.e("SMS Manager", "Failed to send the message.")
        }
    }

}