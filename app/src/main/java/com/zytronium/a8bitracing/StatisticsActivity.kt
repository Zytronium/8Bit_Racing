package com.zytronium.a8bitracing

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import java.text.DecimalFormat

class StatisticsActivity : AppCompatActivity(), Application.ActivityLifecycleCallbacks {

    private lateinit var shared : SharedPreferences // saved data
    private var personalFastest = 0F
    private var highScore1 = 0.0
    private var highScore2 = 0.0
    private var highScore3 = 0.0
    private var highScore4 = 0.0
    private var difficulty = 0
    private val df = DecimalFormat("#.#")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        shared = getSharedPreferences("Zytron8BitRaceData", Context.MODE_PRIVATE) // saved data
        personalFastest = shared.getFloat("Personal Fastest", personalFastest)
        highScore1 = df.format(shared.getInt("Personal Best Lvl1", 0)).toDouble()
        highScore2 = df.format(shared.getInt("Personal Best Lvl2", 0)).toDouble()
        highScore3 = df.format(shared.getInt("Personal Best Lvl3", 0)).toDouble()
        highScore4 = df.format(shared.getInt("Personal Best Lvl4", 0)).toDouble()
        difficulty = shared.getInt("Difficulty", difficulty)

        enableEdgeToEdge()
        setContentView(R.layout.activity_statistics)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        fs()
        application.registerActivityLifecycleCallbacks(this)

        setText()
    }

    private fun setText() {
        val allData = shared.all
        var fastest = 0.0
        try {
            fastest = 1.0 / (personalFastest / 1000.0) * 10.0
        } catch (e: NullPointerException) {
            fastest = 0.0
        }

        // Set text for statistics

        // High Scores
        findViewById<TextView>(R.id.StatHiScore1).text = fancyNumb(highScore1.toString())
        findViewById<TextView>(R.id.StatHiScore2).text = fancyNumb(highScore2.toString())
        findViewById<TextView>(R.id.StatHiScore3).text = fancyNumb(highScore3.toString())
        findViewById<TextView>(R.id.StatHiScore4).text = fancyNumb(highScore4.toString())
        // Personal Fastest
        findViewById<TextView>(R.id.StatPersonalFastest).text =
            getString(R.string.personal_fastest, fancyNumb(fastest.toString()))
        // Average Scores
        findViewById<TextView>(R.id.StatAvrgScore1).text =
            fancyNumb(allData["Average Score Lvl1"].toString())
        findViewById<TextView>(R.id.StatAvrgScore2).text =
            fancyNumb(allData["Average Score Lvl2"].toString())
        findViewById<TextView>(R.id.StatAvrgScore3).text =
            fancyNumb(allData["Average Score Lvl3"].toString())
        findViewById<TextView>(R.id.StatAvrgScore4).text =
            fancyNumb(allData["Average Score Lvl4"].toString())
        // Total Races Completed
        findViewById<TextView>(R.id.StatTotalRaces).text =
            fancyNumb(allData["Total Races"].toString())
        // Lives Collected
        findViewById<TextView>(R.id.StatExtraLives).text =
            fancyNumb(allData["Lives Collected"].toString())
        // App Version Name
        findViewById<TextView>(R.id.StatAppVersionName).text = BuildConfig.VERSION_NAME
        
        // Set background based on current theme
        findViewById<ConstraintLayout>(R.id.main).setBackgroundResource(MainMenuActivity.CurrentTheme.theme?.backgroundTextureBlurred ?: R.drawable.race_road_blur)
    }

    private fun fs() {
        val windowInsetsController =
            ViewCompat.getWindowInsetsController(window.decorView) ?: return
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.hide(WindowInsetsCompat.Type.displayCutout())
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
    }

    fun fancyNumb(number: String): String {
        return df.format(number.replace("null", "0.0").toFloat()).removeSuffix(".0")
    }

    fun viewRawData(view: View) {
        startActivity(Intent(this@StatisticsActivity, LocalDataViewActivity::class.java))
    }

    fun back(view: View) {
        finish()
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {}

    override fun onActivityResumed(activity: Activity) {
        println("Activity Resumed")
    }


    override fun onActivityPaused(activity: Activity) {
        println("Activity Paused")
    }

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {}
}