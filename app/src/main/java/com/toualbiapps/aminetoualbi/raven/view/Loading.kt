package com.toualbiapps.aminetoualbi.raven.view

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import com.toualbiapps.aminetoualbi.raven.R
import kotlinx.android.synthetic.main.activity_loading.*

class Loading : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        val timer = object: CountDownTimer(1500,1000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                loadingCircle.visibility = View.GONE

                val mainActivityIntent =  Intent(applicationContext, MainActivity::class.java);
                startActivity(mainActivityIntent)

            }
        }

        timer.start()

    }
}
