package com.example.jacobgraves.myapplication

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.ProgressBar
import kotlinx.android.synthetic.main.activity_loading.*

class Loading : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        val timer = object: CountDownTimer(3000,1000) {
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
