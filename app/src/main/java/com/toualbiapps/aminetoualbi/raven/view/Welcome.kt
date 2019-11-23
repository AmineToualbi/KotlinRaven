package com.toualbiapps.aminetoualbi.raven.view

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.toualbiapps.aminetoualbi.raven.R
import android.text.Html
import kotlinx.android.synthetic.main.activity_welcome.*



class Welcome : AppCompatActivity() {

    var sharedPrefs: SharedPreferences? = null
    var layout_bars: LinearLayout? = null
    var bottomBars: ArrayList<TextView> = arrayListOf()
    var screens: IntArray = intArrayOf(
            R.layout.intro_screen1,
            R.layout.intro_screen2,
            R.layout.intro_screen3
    )
    var nextButton: Button? = null
    var viewPager: ViewPager? = null
    var myViewPagerAdapter: MyViewPagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        viewPager = findViewById(R.id.viewPager) as ViewPager
        layout_bars = findViewById(R.id.layoutBars) as LinearLayout
        nextButton = findViewById(R.id.nextButton) as Button

        myViewPagerAdapter = MyViewPagerAdapter()
        viewPager!!.adapter = myViewPagerAdapter
        viewPager!!.addOnPageChangeListener(viewPagerPageChangeListener)

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)

        if (!sharedPrefs!!.getBoolean("firstLaunch", true)) {
            launchMain()
            finish()
        }
        coloredBars(0)
    }

    public fun next(v: View) {
        val i = getItem(+1)
        if (i < screens.size)
            viewPager!!.setCurrentItem(i)
        else
            launchMain()
    }

    public fun skip(v: View) {
        launchMain()
    }

    private fun coloredBars(thisScreen: Int) {
        val colorsInactive = resources.getIntArray(R.array.dot_on_page_not_active)
        val colorsActive = resources.getIntArray(R.array.dot_on_page_active)

        layoutBars.removeAllViews()
        for (i in 0 until screens.size) {
            bottomBars.add(TextView(this))
            bottomBars[i].setTextSize(100f)
            bottomBars[i].text = Html.fromHtml("¯")
            layoutBars.addView(bottomBars[i])
            bottomBars[i].setTextColor(colorsInactive[thisScreen])
        }
        if (bottomBars.isNotEmpty())
            bottomBars[thisScreen].setTextColor(colorsActive[thisScreen])
    }

    private fun getItem(i: Int): Int {
        return viewPager!!.currentItem + i
    }

    private fun launchMain() {
        sharedPrefs!!.edit().putBoolean("firstLaunch", false).apply()
        val mainScreen = Intent(applicationContext, MainActivity::class.java)
        startActivity(mainScreen)
        finish()
    }

    var viewPagerPageChangeListener: ViewPager.OnPageChangeListener = object : ViewPager.OnPageChangeListener {
        override fun onPageSelected(position: Int) {
            coloredBars(position)
            if (position == screens.size - 1) {
                nextButton!!.setText("start")
            } else {
                nextButton!!.setText("next")
            }
        }

        override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {

        }

        override fun onPageScrollStateChanged(arg0: Int) {

        }
    }

    inner class MyViewPagerAdapter : PagerAdapter() {
        private var inflater: LayoutInflater? = null

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            inflater = applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view: View = inflater!!.inflate(screens!![position], container, false)
            container.addView(view)
            return view
        }

        override fun getCount(): Int {
            return screens!!.size
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            val v: View = `object` as View
            container.removeView(v)
        }

        override fun isViewFromObject(p0: View, p1: Any): Boolean {
            return p0 == p1
        }
    }
}
