package com.zytronium.a8bitracing

import android.app.Activity
import android.app.Application
import android.content.Context
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
import kotlin.random.Random

class LocalDataViewActivity : AppCompatActivity(), Application.ActivityLifecycleCallbacks  {

    private lateinit var shared : SharedPreferences // saved data

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        shared = getSharedPreferences("Zytron8BitRaceData", Context.MODE_PRIVATE) // saved data
        enableEdgeToEdge()
        setContentView(R.layout.activity_local_data_view)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        fs()
        application.registerActivityLifecycleCallbacks(this)

        setText()
        findViewById<ConstraintLayout>(R.id.main).setBackgroundResource(MainMenuActivity.CurrentTheme.theme?.backgroundTextureBlurred ?: R.drawable.race_road_blur)

    }

    private fun setText() {
        val formattedString = shared.all.toString().replace(", ", ",\n\n")
        findViewById<TextView>(R.id.statsTxt).text =
            formattedString.substring(1, formattedString.length - 1)
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

    fun cleanData(view: View) {
        val legalDataKeys = arrayOf(
            "Total Races",
            "Total Races Lvl1",
            "Total Races Lvl2",
            "Total Races Lvl3",
            "Total Races Lvl4",
            "Average Score Lvl1",
            "Average Score Lvl2",
            "Average Score Lvl3",
            "Average Score Lvl4",
            "Personal Best Lvl1",
            "Personal Best Lvl2",
            "Personal Best Lvl3",
            "Personal Best Lvl4",
            "Personal Fastest",
            "Lives Collected",
            "Difficulty",
            "CurrentTheme"
            )
        val edit = shared.edit()

        shared.all.keys.forEach { key: String ->
            if (key !in legalDataKeys) {
                println("key \"$key\" deleted. Its value was ${shared.all[key]}")
                edit.remove(key)
            }
        }

        edit.apply()
        setText()
    }

}