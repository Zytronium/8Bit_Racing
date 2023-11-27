package com.zytronium.a8bitracing

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.forEach
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var screen: ConstraintLayout
    private lateinit var player: ImageView
    private lateinit var life1: ImageView
    private lateinit var life2: ImageView
    private lateinit var life3: ImageView
    private lateinit var scoreText: TextView
    private lateinit var playButton: Button
    private lateinit var themeSwitch: Switch
    private lateinit var finalScoreText: TextView

    private var gamePlaying: Boolean = true
    private var lane: Int = 2
    private var gameSpeed: Int = 1000
    private var noSpawnLane: Int = 0
    private var previousSpawnLane: Int = 0
    private var playerLives: Int = 3
    private var score: Int = 0
    private var spaceMode: Boolean = true

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        screen = findViewById(R.id.main)
        player = findViewById(R.id.player)
        life1 = findViewById(R.id.life1)
        life2 = findViewById(R.id.life2)
        life3 = findViewById(R.id.life3)
        scoreText = findViewById(R.id.scoreTxt)
        finalScoreText = findViewById(R.id.finalScoreTxt)
        playButton = findViewById(R.id.playBtn)
        themeSwitch = findViewById(R.id.theme_switch)
        fs()

        if(spaceMode) {
            screen.background = getDrawable(R.drawable.race_space)
            player.setImageDrawable(getDrawable(R.drawable.blue_raceship))
            life1.setImageDrawable(getDrawable(R.drawable.blue_raceship))
            life2.setImageDrawable(getDrawable(R.drawable.blue_raceship))
            life3.setImageDrawable(getDrawable(R.drawable.blue_raceship))
            player.layoutParams.width = (pixelWidth() * 12).toInt()
            player.layoutParams.height = (pixelHeight() * 18).toInt()
        }

        screen.setOnTouchListener { _, event ->
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    if (event.getX() > (screen.measuredWidth/2)) moveRight() else moveLeft()
                }
            }
            true
        }

        play()
//        keepScore()

    }

    private fun updateLives() {
        life1.visibility = if(playerLives >= 1) View.VISIBLE else View.GONE
        life2.visibility = if(playerLives >= 2) View.VISIBLE else View.GONE
        life3.visibility = if(playerLives >= 3) View.VISIBLE else View.GONE
    }

    private fun keepScore() {
        scoreText.text = getString(R.string.score, score.toString())
        when(score) {
            0 -> gameSpeed = 1000
            1 -> gameSpeed = 1500
            2 -> gameSpeed = 2000
            5 -> gameSpeed = 1875
            10 -> gameSpeed = 1750
            15 -> gameSpeed = 1500
            20 -> gameSpeed = 1250
            25 -> gameSpeed = 1125
            30 -> gameSpeed = 1000
            else -> {
                if((score.toString().endsWith("0") && score <= 150) || ((score.toString().endsWith("00") || score.toString().endsWith("20") || score.toString().endsWith("40") || score.toString().endsWith("60") || score.toString().endsWith("80")) && score <= 220) || ((score.toString().endsWith("00") || score.toString().endsWith("50")) && score >= 250) )
                gameSpeed = (gameSpeed * if(score <= 120) (7.0/8.0) else if(score <= 220) (15.0/16.0) else (31.0/32.0)).toInt()
            }
//            15 -> gameSpeed = 1333
//            25 -> gameSpeed = 1125
//            45 -> gameSpeed = 1000
//            60 -> gameSpeed = 750
//            90 -> gameSpeed = 550
//            100 -> gameSpeed = 375
//            125 -> gameSpeed = 250
//            155 -> gameSpeed = 225
//            285 -> gameSpeed = 195
//            210 -> gameSpeed = 170
//            245 -> gameSpeed = 125
//            280 -> gameSpeed = 100
//            300 -> gameSpeed = 90
//            315 -> gameSpeed = 75
//            360 -> gameSpeed = 70
//            390 -> gameSpeed = 65
//            425 -> gameSpeed = 60
//            460 -> gameSpeed = 55
//            500 -> gameSpeed = 50

        }
//        Handler().postDelayed({
//            if(gamePlaying) {
//                score++
//                keepScore()
//            }
//        }, gameSpeed.toLong())
    }

    private fun play() {
        scoreText.text = getString(R.string.score, score.toString())
        Handler().postDelayed({
            if(gamePlaying) {
                moveCars()
                generateCars()
                if(gamePlaying) {
                    score++
                    scoreText.text = getString(R.string.score, score.toString())
                }
                keepScore()
                play()
            }
        }, gameSpeed.toLong() )
    }

    private fun moveCars() {
        var cars: Array<ImageView> = arrayOf()
        screen.forEach { view: View ->
            if(view.tag != null && view is ImageView) cars = cars.plusElement(view)
        }

        cars.forEach { car: ImageView ->
              car.tag = when(car.tag) {
                  "1" -> "2"
                  "2" -> "3"
                  "3" -> "4"
                  "4" -> "5"
                  "5" -> null
                   else -> car.tag
              }

            if(car.tag == null) {
                car.visibility = View.GONE
                screen.removeView(car)
            }

            car.y += (screen.measuredHeight / 4)

        }

        checkCollisions(cars)

    }

    @SuppressLint("ClickableViewAccessibility")
    private fun checkCollisions(cars: Array<ImageView>) {
        cars.forEach { car: ImageView ->
            if(collided(player, car)) {
                playerLives --
                updateLives()
                car.tag = null
                car.visibility = View.GONE
                screen.removeView(car)
                vibrate(150)
                if(playerLives <= 0) {
                    screen.setOnTouchListener(null)
                    playButton.visibility = View.VISIBLE
//                    themeSwitch.visibility = View.VISIBLE
                    finalScoreText.visibility = View.VISIBLE
                    gamePlaying = false
                    finalScoreText.text = getString(R.string.score, score.toString())
                    vibrate(333)
                }
            }
        }
    }

        private fun collided(object1: View, object2: View): Boolean {
            val rect1 = Rect()
            object1.getHitRect(rect1)
            val rect2 = Rect()
            object2.getHitRect(rect2)
            return rect1.intersect(rect2)
        }

        private fun vibrate(time: Long) {
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if(vibrator.hasVibrator()) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(
                        VibrationEffect.createOneShot(
                            time,
                            VibrationEffect.DEFAULT_AMPLITUDE
                        )
                    )
                } else {
                    vibrator.vibrate(time)
                }
            }
        }

    private fun generateCars() {
        when ((1..7).random()) {
            1 -> {
                if(noSpawnLane != 3) {
                    spawnCar(3)
                    noSpawnLane = 0
                    previousSpawnLane = 3
                } else when ((1..20).random()) {
                    in 3..20 -> generateCars()
                    else -> {
                        noSpawnLane = 0
                        previousSpawnLane = 0
                    }
                }
            }
            2 -> {
                if(noSpawnLane != 2) {
                    if(previousSpawnLane == 1)  noSpawnLane = 3
                    if(previousSpawnLane == 3)  noSpawnLane = 1
                    spawnCar(2)
                    previousSpawnLane = 2
                } else when ((1..20).random()) {
                    in 3..20 -> generateCars()
                    else -> {
                        noSpawnLane = 0
                        previousSpawnLane = 0
                    }
                }
            }
            3 -> {
                if(noSpawnLane != 1) {
                    spawnCar(1)
                    noSpawnLane = 0
                    previousSpawnLane = 1
                } else when ((1..20).random()) {
                    in 3..20 -> generateCars()
                    else -> {
                        noSpawnLane = 0
                        previousSpawnLane = 0
                    }
                }
            }
            4 -> {
                if(Random.nextBoolean()) {

                    if(noSpawnLane != 2 && noSpawnLane != 1 && previousSpawnLane != 3) {
                        spawnCar(2)
                        spawnCar(1)
                        noSpawnLane = 3
                    } else when ((1..30).random()) {
                        in 1..29 -> generateCars()
                        else -> {
                            noSpawnLane = 0
                            previousSpawnLane = 0
                        }
                    }
                } else generateCars()
            }
            5 -> {
                if(Random.nextBoolean()) {

                    if(noSpawnLane != 1 && noSpawnLane != 3 && previousSpawnLane != 2) {
                        spawnCar(1)
                        spawnCar(3)
                        noSpawnLane = 2
                    } else when ((1..30).random()) {
                        in 1..29 -> generateCars()
                        else -> {
                            noSpawnLane = 0
                            previousSpawnLane = 0
                        }
                    }
                } else generateCars()
            }
            6 -> {
                if(Random.nextBoolean()) {

                    if(noSpawnLane != 2 && noSpawnLane != 3 && previousSpawnLane != 1) {
                        spawnCar(2)
                        spawnCar(3)
                        noSpawnLane = 1
                    } else when ((1..30).random()) {
                        in 1..29 -> generateCars()
                        else -> {
                            noSpawnLane = 0
                            previousSpawnLane = 0
                        }
                    }
                } else generateCars()
            }
            7 -> when ((1..3).random()) {
                in 1..2 -> generateCars()
                else -> {
                    noSpawnLane = 0
                    previousSpawnLane = 0
                }
            }
        }
    }

    private fun spawnCar(lane: Int) {
        val newCar = ImageView(this)
        newCar.layoutParams = ConstraintLayout.LayoutParams(
            player.measuredWidth,
            player.measuredHeight
        )
        val lParams = newCar.layoutParams as ConstraintLayout.LayoutParams
        lParams.startToStart = screen.id
        lParams.endToEnd = screen.id
        lParams.topToTop = screen.id
        lParams.bottomToBottom = screen.id
        lParams.horizontalBias = 0.5f
        lParams.verticalBias = 0.0125f

        newCar.setImageDrawable(if(spaceMode) {
            if (Random.nextBoolean()) getDrawable(R.drawable.red_raceship) else getDrawable(R.drawable.green_raceship)
        } else if(Random.nextBoolean()) getDrawable(R.drawable.red_car) else getDrawable(R.drawable.green_car)) //{
        //    true -> getDrawable(R.drawable.red_car)
        //    false -> getDrawable(R.drawable.green_car)
        //})

        if(lane == 1) newCar.x -= (pixelWidth() * 11)
        if(lane == 3) newCar.x += (pixelWidth() * 11)

        newCar.tag = "1"

        screen.addView(newCar)
    }

    private fun moveLeft() {
        if (lane != 1) {
            player.x -= (pixelWidth() * 11)
            lane --
            checkCollisions(getCars())
        }
    }

    private fun getCars(): Array<ImageView> {
        var cars: Array<ImageView> = arrayOf()
        screen.forEach { i: View ->
            if(i.tag != null && i is ImageView) cars = cars.plusElement(i)
        }
        return cars
    }

    private fun moveRight() {
        if (lane != 3) {
            player.x += (pixelWidth() * 11)
            lane ++
            checkCollisions(getCars())
        }
    }

    private fun pixelWidth(): Float {
        return (screen.measuredWidth/38f)
    }

    private fun pixelHeight(): Float {
        return (screen.measuredHeight / 80f)
    }


    fun startGame(view: View) {
        vibrate(25)
        recreate()
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


}