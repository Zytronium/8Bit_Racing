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
    private var highScore1 = 0
    private var highScore2 = 0
    private var highScore3 = 0
    private var highScore4 = 0
    private var difficulty = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        shared = getSharedPreferences("Zytron8BitRaceData", Context.MODE_PRIVATE) // saved data
        personalFastest = shared.getFloat("Personal Fastest", personalFastest)
        highScore1 = shared.getInt("Personal Best Lvl1", highScore1)
        highScore2 = shared.getInt("Personal Best Lvl2", highScore2)
        highScore3 = shared.getInt("Personal Best Lvl3", highScore3)
        highScore4 = shared.getInt("Personal Best Lvl4", highScore4)
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
        val df = DecimalFormat("#.#")
        var fastest = 0.0
        try {
            fastest = 1.0 / (allData["Personal Fastest"] as Float / 1000.0) * 10.0
        } catch (e: NullPointerException) {
            fastest = 0.0
        }

        // set text for statistics
        findViewById<TextView>(R.id.StatPersonalFastest).text =
            "${df.format(fastest)} SU"
        findViewById<TextView>(R.id.StatHiScore1).text =
            allData["Personal Best Lvl1"].toString().replace("null", "0")
        findViewById<TextView>(R.id.StatHiScore2).text =
            allData["Personal Best Lvl2"].toString().replace("null", "0")
        findViewById<TextView>(R.id.StatHiScore3).text =
            allData["Personal Best Lvl3"].toString().replace("null", "0")
        findViewById<TextView>(R.id.StatHiScore4).text =
            allData["Personal Best Lvl4"].toString().replace("null", "0")
        findViewById<TextView>(R.id.StatAppVersionName).text =
            "App Version Name:\n${BuildConfig.VERSION_NAME}"
        
        // set background based on current theme
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