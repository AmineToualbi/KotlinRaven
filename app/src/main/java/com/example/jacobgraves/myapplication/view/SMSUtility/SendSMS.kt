package com.example.jacobgraves.myapplication.view.SMSUtility

import android.Manifest
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.telephony.SmsManager
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_new_raven.*

class SendSMS {

    private fun sendSms() {

            var number = enteredNumber.text.toString()
            var text = enteredMessage.text.toString()

            SmsManager.getDefault().sendTextMessage(number, null, text, null, null)
            //Toast.makeText(this, "SMS sent.", Toast.LENGTH_SHORT).show()

    }

}