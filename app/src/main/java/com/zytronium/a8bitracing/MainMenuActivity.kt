package com.zytronium.a8bitracing

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import java.lang.Exception

class MainMenuActivity : AppCompatActivity(), Application.ActivityLifecycleCallbacks {

    var menuMusic: MediaPlayer? = null
    private var musicFaded = false
    private var musicVol = 0.85F

    private var spaceMode: Boolean = false
    private lateinit var difficultyBar: SeekBar
    private lateinit var themeSwitch: Switch
    private lateinit var difficultyNameText: TextView

    private lateinit var shared : SharedPreferences // saved data
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main_menu)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        menuMusic = MediaPlayer.create(this, R.raw.menubackgroundmusicelevators)
        shared = getSharedPreferences("Zytron8BitRaceData", Context.MODE_PRIVATE) // saved data
        difficultyBar = findViewById(R.id.difficulty_bar)
        themeSwitch = findViewById(R.id.theme_switch)
        difficultyNameText = findViewById(R.id.difficulty_name_txt)
        themeSwitch.text = if(themeSwitch.isChecked) themeSwitch.textOn else themeSwitch.textOff

        fs()

        readData()

        application.registerActivityLifecycleCallbacks(this)

        difficultyBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                setDifficultyText(progress + 1)
                Difficulty.difficulty = progress + 1
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                Difficulty.difficulty = difficultyBar.progress + 1
                saveData()
            }
        })
        findViewById<TextView>(R.id.difficultySelectTxt).text = when((0..44).random()) {
            in 1..2 -> "Maniac Driver Skill Level:"
            else -> "Speed Level:"
        }
        difficultyBar.progress = Difficulty.difficulty - 1
        setDifficultyText()
        saveData()

        themeSwitch.text = if(themeSwitch.isChecked) themeSwitch.textOn else themeSwitch.textOff
        findViewById<ConstraintLayout>(R.id.main).setBackgroundResource(if(!spaceMode) R.drawable.race_road_blur else R.drawable.race_space_blur)

        themeSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            spaceMode = isChecked
            themeSwitch.text = if(isChecked) themeSwitch.textOn else themeSwitch.textOff
            findViewById<ConstraintLayout>(R.id.main).setBackgroundResource(if(!spaceMode) R.drawable.race_road_blur else R.drawable.race_space_blur)
            if(spaceMode) difficultyNameText.text = difficultyNameText.text.toString().replace("Driver", "Pilot") else difficultyNameText.text = difficultyNameText.text.toString().replace("Pilot", "Driver")
            saveData()
        }
    }

    private fun fadeMusic() {
        musicFaded = true
        if(musicVol > 0F) {
            musicVol -= 0.05F
            menuMusic!!.setVolume(musicVol, musicVol)
            Handler().postDelayed({
                fadeMusic()
            }, 42)
        } else menuMusic!!.stop()
    }
    private fun playMusic() {
        if(menuMusic == null) {
            menuMusic!!.isLooping = true
            menuMusic!!.start()

        } else {
            menuMusic!!.isLooping = true
            menuMusic!!.start()
        }
        menuMusic!!.setVolume(0.85F, 0.85F)
    }

    private fun setDifficultyText(progress: Int = difficultyBar.progress + 1) {
        difficultyNameText.text = when(progress) {
            1 -> getString(R.string.skill_level_1)
            2 -> getString(R.string.skill_level_2)
            3 -> getString(R.string.skill_level_3)
            4 -> getString(R.string.skill_level_4)
            else -> "Unknown Difficulty"
        }
        if(spaceMode) difficultyNameText.text = difficultyNameText.text.toString().replace("Driver", "Pilot")
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

    private fun readData() {
        spaceMode = shared.getBoolean("SpaceMode", spaceMode)
        themeSwitch.isChecked = spaceMode
        Difficulty.difficulty = shared.getInt("Difficulty", Difficulty.difficulty)
    }

    private fun saveData() {
        val edit = shared.edit()
        edit.putBoolean("SpaceMode", spaceMode)
        edit.putInt("Difficulty", Difficulty.difficulty)
        edit.apply()
    }

    fun startGame(view: View) {
        fadeMusic()
        Difficulty.difficulty = difficultyBar.progress + 1
        saveData()
        startActivity(Intent(this@MainMenuActivity, MainActivity::class.java))
        finish()
    }

    fun viewStats(view: View) {
        startActivity(Intent(this@MainMenuActivity, Statistics::class.java))
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
//        TODO("Not yet implemented")
    }

    override fun onActivityStarted(activity: Activity) {
        playMusic()
    }

    override fun onActivityResumed(activity: Activity) {
        println("Activity Resumed")
        menuMusic!!.start()
    }


    override fun onActivityPaused(activity: Activity) {
        println("Activity Paused")
        menuMusic!!.pause()
    }

    override fun onActivityStopped(activity: Activity) {
//        menuMusic!!.stop()
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
//        TODO("Not yet implemented")
    }

    override fun onActivityDestroyed(activity: Activity) {
//        TODO("Not yet implemented")
    }

    object Difficulty {
        var difficulty = 0
    }

    fun difficultyUp(view: View) {
        if(difficultyBar.progress != difficultyBar.max) {
            difficultyBar.progress ++
        }
        saveData()
    }
    fun difficultyDown(view: View) {
        if(difficultyBar.progress != 0) {
            difficultyBar.progress --
        }
        saveData()
    }

    fun credits(view: View) {
        findViewById<TextView>(R.id.credits_txt).visibility = if(findViewById<TextView>(R.id.credits_txt).visibility == View.GONE) View.VISIBLE else View.GONE
        findViewById<Button>(R.id.btn_credits).text = if(findViewById<Button>(R.id.btn_credits).text == "Show\nCredits") "Hide\nCredits" else "Show\nCredits"
    }

}