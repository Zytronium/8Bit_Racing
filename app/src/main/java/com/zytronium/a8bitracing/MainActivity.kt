package com.zytronium.a8bitracing

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Rect
import android.media.MediaPlayer
import android.os.*
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.forEach
import com.zytronium.a8bitracing.MainMenuActivity.CurrentTheme
import com.zytronium.a8bitracing.Themes.themes
import java.text.DecimalFormat
import kotlin.math.pow
import kotlin.random.Random

var slowMusic: MediaPlayer? = null
var fastMusic: MediaPlayer? = null

class MainActivity : AppCompatActivity(), Application.ActivityLifecycleCallbacks {
    /* uninitialized things */
    private lateinit var screen: ConstraintLayout
    private lateinit var player: ImageView
    private lateinit var life1: ImageView
    private lateinit var life2: ImageView
    private lateinit var life3: ImageView
    private lateinit var life4: ImageView
    private lateinit var life5: ImageView
    private lateinit var scoreText: TextView
    private lateinit var speedometerText: TextView
    private lateinit var recordScoreText: TextView
    private lateinit var highScoreText: TextView
    private lateinit var finalScoreText: TextView
    private lateinit var playButton: Button
    private lateinit var resumeButton: Button
    private lateinit var menuButton: Button
    private lateinit var themeText: TextView
    private lateinit var themeSelector: LinearLayout
    private lateinit var shared: SharedPreferences // saved data

    /* rotation variables */
    private var rot1start =  0f
    private var rot1end =  (-6..6).random().toFloat()
    private var rot2start =  0f
    private var rot2end =  (-6..6).random().toFloat()
    private var rot3start =  0f
    private var rot3end =  (-6..6).random().toFloat()

    /* misc game conditions */
    private var difficulty = MainMenuActivity.Difficulty.difficulty
//    private var spaceMode = false
    private var isAppInForeground = true
    private var musicFaded = false
    private var gamePlaying = true
    private var progressing = false
    private var superSpeed: Boolean = false
    private var paused = false // only if paused from pause button. Intentional
    private var playing = 0 // this should prevent it from playing twice, causing the game to progress twice as fast after resuming. Playing should always either be 1 or 0. If it's not, then the game realizes it's progressing twice as fast, and fixes it.
    private var pauseCooldown = false
    private var gameOverTimeout: Long = 0L
    private var scoreSinceLastHit: Int = 0
    private var slowMusicVol = 1F
    private var gameSpeed: Double = 1000.0
    private var playerLives: Int = 3

    /* lane variables */
    private var noTrapFailsafe = false
    private var previousSpawnLane: Int = 0
    private var noSpawnLane: Int = 0
    private var lane: Int = 2

    /* score variables */
    private var hiScoreBeat: Boolean = false
    private var score: Int = 0
    private var highScore: Int = 0

    /* other saved data variables */
    private var highScore1: Int = 0
    private var highScore2: Int = 0
    private var highScore3: Int = 0
    private var highScore4: Int = 0
    private var personalFastest: Float = Float.POSITIVE_INFINITY
    private var averageScore1: Int = 0
    private var averageScore2: Int = 0
    private var averageScore3: Int = 0
    private var averageScore4: Int = 0
    private var totalRacesCompleted: Int = 0
    private var totalRacesDiff1: Int = 0
    private var totalRacesDiff2: Int = 0
    private var totalRacesDiff3: Int = 0
    private var totalRacesDiff4: Int = 0
    private var extraLivesCollected: Int = 0

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        shared = getSharedPreferences("Zytron8BitRaceData", Context.MODE_PRIVATE) // saved data
        screen = findViewById(R.id.display)
        player = findViewById(R.id.player)
        life1 = findViewById(R.id.life1)
        life2 = findViewById(R.id.life2)
        life3 = findViewById(R.id.life3)
        life4 = findViewById(R.id.life4)
        life5 = findViewById(R.id.life5)
        scoreText = findViewById(R.id.scoreTxt)
        speedometerText = findViewById(R.id.speedometer)
        recordScoreText = findViewById(R.id.newRecordTxt)
        highScoreText = findViewById(R.id.highScoreTxt)
        finalScoreText = findViewById(R.id.finalScoreTxt)
        playButton = findViewById(R.id.btn_play)
        resumeButton = findViewById(R.id.btn_resume)
        menuButton = findViewById(R.id.btn_menu)
        themeText = findViewById(R.id.theme_txt2)
        themeSelector = findViewById(R.id.theme_selector)
        slowMusic = MediaPlayer.create(this, R.raw.racemusicjetanger)
        fastMusic = MediaPlayer.create(this, R.raw.fastspaceracemusicsuccubus)

        if (CurrentTheme.theme?.name == "Rainbow Kart")
            slowMusic = MediaPlayer.create(this, R.raw.rainbow_road_8_bit)

        fs()
        application.registerActivityLifecycleCallbacks(this)
        readData()

        themeText.text = getCurrentThemeName()
        setTextures()

        screen.setOnTouchListener { _, event ->
            if(!paused) {
                when (event.action and MotionEvent.ACTION_MASK) {
                    MotionEvent.ACTION_DOWN -> {
                        if(event.x > (screen.measuredWidth / 2)) moveRight() else moveLeft()
                    }
                }
            }
            true
        }
        play()
        if(difficulty != 4)
            playSlowMusic()
        else
            playFastMusic()
    }

    private fun setTextures() {
        val backgroundTexture = CurrentTheme.theme?.backgroundTexture ?: R.drawable.race_road
        val playerTexture = CurrentTheme.theme?.blueVehicleTexture ?: R.drawable.blue_car
        val redVehicleTexture = CurrentTheme.theme?.redVehicleTexture ?: R.drawable.red_car
        val greenVehicleTexture = CurrentTheme.theme?.greenVehicleTexture ?: R.drawable.green_car

        screen.background = AppCompatResources.getDrawable(this, backgroundTexture)
        player.setImageDrawable(AppCompatResources.getDrawable(this, playerTexture))
        getCars().forEach { car: ImageView ->
            car.setImageDrawable(
                AppCompatResources.getDrawable(
                    this,
                    if (car.tag.toString().contains("red"))
                        redVehicleTexture
                    else if (car.tag.toString().contains("green"))
                        greenVehicleTexture
                    else
                        R.drawable.plus_one_life
                )
            )
        }
    }

    private fun updateLives() {
        life1.visibility = if(playerLives >= 1) View.VISIBLE else View.GONE
        life2.visibility = if(playerLives >= 2) View.VISIBLE else View.GONE
        life3.visibility = if(playerLives >= 3) View.VISIBLE else View.GONE
        life4.visibility = if(playerLives >= 4) View.VISIBLE else View.GONE
        life5.visibility = if(playerLives >= 5) View.VISIBLE else View.GONE
    }

    private fun incrementGameSpeed() {
        updateScore()

        val initialSpeed = 1500.0  // Starting speed (ms per tick)
        val speedCap = 100.0        // Minimum game speed limit
        val baseDecayRate = 0.0225   // Base rate at which speed decreases (lower to slow the rate more over time)
        val adjustmentFactor = 0.9975  // Multiplier to decrease the rate of speed change (closer to 1 means slower decrease over time)

        // Calculate the decay based on score
        gameSpeed = initialSpeed / (1.0 + baseDecayRate * score) * adjustmentFactor.pow(score.toDouble())

        // Ensure the gameSpeed doesn't fall below the speedCap
        if (gameSpeed < speedCap) {
            gameSpeed = speedCap
        }
    }


    private fun updateScore() {
        val df = DecimalFormat("#.#")
        scoreText.text = getString(R.string.score, score.toString())
        speedometerText.text = getString(R.string.speed, df.format((1.0 / (adjustedGameSpeed() / 1000.0)) * 10))
    }

    private fun play() {
        playing++
        if(playing == 1) {
//            spaceMode = themeText.isChecked
            updateScore()
            progressing = true
            progressRace()
        } else if(playing > 1) {
            playing--
        } else if(playing < 0) {
            playing = 0
        }
    }

    private fun progressRace() {
        Handler().postDelayed({
            if(gamePlaying && isAppInForeground && progressing && !paused && playing == 1) {
                moveCars()
                generateCars()

                if(gamePlaying) { // repeated if statement here in case player died during the last 2 lines
                    score++
                    scoreSinceLastHit++
                    updateScore() // may be redundant due to being used in incrementGameSpeed()
                }

                incrementGameSpeed()

                if(scoreSinceLastHit >= ((100.0 + score / 4.0) / 2.0).toInt())
                    superSpeed = true

                if(playing > 0)
                    playing--
                if(playing == 0)
                    play()
            }
        }, adjustedGameSpeed())
//        println("speed: ${adjustedGameSpeed()}")
        if(CurrentTheme.theme?.name != "Rainbow Kart" && (adjustedGameSpeed() <= 310 && (fastMusic == null || !fastMusic!!.isPlaying) && score > 3)) {
            if(slowMusic!!.isPlaying && !musicFaded) {
                fadeSlowMusic()
            }
            else if(!slowMusic!!.isPlaying) playFastMusic()
        }
        if(score == highScore) rainbowText(scoreText, 1f, 14)
    }

    private fun shakeScreen() {
        rot1end = (-1..1).random().toFloat()
        rot2end = (-1..1).random().toFloat()
        rot3end = (-1..1).random().toFloat()
        rot1start = (-1..1).random().toFloat()
        rot2start = (-1..1).random().toFloat()
        rot3start = (-1..1).random().toFloat()

        val animator = ObjectAnimator.ofFloat(screen, View.ROTATION, (rot1end / 5), (rot1start / 5))
        animator.duration = ((73..76).random()).toLong()
        animator.start()
        val animator2 = ObjectAnimator.ofFloat(screen, View.ROTATION_X, (rot2end / 5), (rot2start / 5))
        animator2.duration = ((73..76).random()).toLong()
        animator2.start()
        val animator3 = ObjectAnimator.ofFloat(screen, View.ROTATION_Y, (rot3end / 5), (rot3start / 5))
        animator3.duration = ((73..76).random()).toLong()
        animator3.start()
        Handler().postDelayed({
            val animator4 = ObjectAnimator.ofFloat(screen, View.ROTATION, (rot1start / 5), 0f)
            animator4.duration = 55L
            animator4.start()
            val animator5 = ObjectAnimator.ofFloat(screen, View.ROTATION_X, (rot2start / 5), 0f)
            animator5.duration = 55L
            animator5.start()
            val animator6 = ObjectAnimator.ofFloat(screen, View.ROTATION_Y, (rot3start / 5), 0f)
            animator6.duration = 55L
            animator6.start()
        },78L)
    }

    private fun fadeSlowMusic() { //(and then play fast music)
        musicFaded = true
        if(slowMusicVol > 0F) {
            slowMusicVol -= 0.025F
            slowMusic!!.setVolume(slowMusicVol, slowMusicVol)
            if(slowMusicVol == 0.35F) {
                if(!paused && progressing && (fastMusic == null || !fastMusic!!.isPlaying)) playFastMusic()
            }
            Handler().postDelayed({
                fadeSlowMusic()
            }, 90)
        }
        else slowMusic!!.stop()
    }

    private fun adjustedGameSpeed(): Long { //NOTE: SuperSpeed can mess with when music fades to fast music, and COULD (but probably won't due to certain failsafes) cause it to play fast music more than once. Consider moving the superSpeed multiplier to outside of this function.
        return ((if(superSpeed) gameSpeed * 0.75 else gameSpeed).toDouble() / when (difficulty) {
            1 -> 1.0
            2 -> 1.5
            3 -> 2.5
            4 -> 6.0
            else -> 1.0
        }).toLong()
    }

    private fun moveCars() {
        var cars: Array<ImageView> = arrayOf()
        screen.forEach { view: View ->
            if(view.tag != null && view is ImageView) cars = cars.plusElement(view)
        }

        cars.forEach { car: ImageView ->
              car.tag = when(car.tag) {
                  "1red" -> "2red"
                  "2red" -> "3red"
                  "3red" -> "4red"
                  "4red" -> "5red"
                  "5red" -> null
                  "1green" -> "2green"
                  "2green" -> "3green"
                  "3green" -> "4green"
                  "4green" -> "5green"
                  "5green" -> null
                  "1life" -> "2life"
                  "2life" -> "3life"
                  "3life" -> "4life"
                  "4life" -> "5life"
                  "5life" -> null
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
            if (collided(player, car)) {

                if (car.tag.toString().contains("life"))
                    collectLife()
                else
                    takeDamage()

                updateLives()
                car.tag = null
                car.visibility = View.GONE
                screen.removeView(car)

                if(playerLives <= 0)
                    gameOver()
            }
        }
    }

    private fun takeDamage() {
        playerLives--
        scoreSinceLastHit = 0
        superSpeed = false
        shakeScreen()
        vibrate(175)
    }

    private fun collectLife() {
        extraLivesCollected++
        if (playerLives < 5)
            playerLives++
        else
            score += (5 + (5 * (roundDownToInt(score / 100.0))))

        vibrate(20)
    }

    private fun gameOver() {
        screen.setOnTouchListener(null)
        if (gameOverTimeout == 0L) {
            gameOverTimeout = 350L
            doGameOverTimeout()
        }

        menuButton.visibility = View.VISIBLE
        findViewById<ImageButton>(R.id.btn_pause).visibility = View.GONE
        playButton.visibility = View.VISIBLE
        themeSelector.visibility = View.VISIBLE
        finalScoreText.visibility = View.VISIBLE
        highScoreText.visibility = View.VISIBLE
        scoreText.visibility = View.INVISIBLE
        gamePlaying = false
        if (score > highScore) {
            highScore = score
            hiScoreBeat = true
        }
        finalScoreText.text = getString(R.string.score, score.toString())
        highScoreText.text = getString(R.string.hi_score, highScore.toString())
        if (hiScoreBeat) {
            recordScoreText.visibility = View.VISIBLE
            rainbowText(finalScoreText, 350f, 15)
            rainbowText(highScoreText, 350f, 15)
            animateRecordScoreText()
        }
        vibrate(333)
        totalRacesCompleted ++
        when (difficulty) {
            1 -> totalRacesDiff1++
            2 -> totalRacesDiff2++
            3 -> totalRacesDiff3++
            4 -> totalRacesDiff4++
            else -> throw IllegalArgumentException("Invalid difficulty level: $difficulty. Expected values are 1 through 4.")
        }
        if (adjustedGameSpeed().toFloat() < personalFastest)
            personalFastest = adjustedGameSpeed().toFloat()

        saveData()
    }

    private fun doGameOverTimeout() {
        if(gameOverTimeout > 0L) {
            gameOverTimeout -= 50L
            Handler().postDelayed({
                doGameOverTimeout()
            }, 50L)
        }
    }

    private fun roundDownToInt(input: Double): Int {
        return input.toString().split(".").first().toInt()
    }

    private fun animateRecordScoreText() {
        val animator = ObjectAnimator.ofFloat(recordScoreText, View.SCALE_Y, 0.95f, 1.15f)
        animator.duration = 650L
        animator.start()
        val animator2 = ObjectAnimator.ofFloat(recordScoreText, View.SCALE_X, 0.95f, 1.15f)
        animator2.duration = 650L
        animator2.start()

        Handler().postDelayed({
            val animator3 = ObjectAnimator.ofFloat(recordScoreText, View.SCALE_Y, 1.15f, 0.95f)
            animator3.duration = 650L
            animator3.start()
            val animator4 = ObjectAnimator.ofFloat(recordScoreText, View.SCALE_X, 1.15f, 0.95f)
            animator4.duration = 650L
            animator4.start()
            Handler().postDelayed({
                animateRecordScoreText()
            },651L)
        },651L)
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
        if (vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        time,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else vibrator.vibrate(time)
        }
    }

    fun themeLeft(view: View) {
        if(gameOverTimeout <= 0L) {
            val size = themes.size - 1
            val id = CurrentTheme.theme?.id?: 0

            // set the theme
            CurrentTheme.theme = getThemeByID(if (id == 0) size else id - 1)
            // update theme text
            themeText.text = getCurrentThemeName()

            saveData()
            setTextures()
        }
    }

    fun themeRight(view: View) {
        if(gameOverTimeout <= 0L) {
            val size = themes.size - 1
            val id = CurrentTheme.theme?.id?: 0

            // set the theme
            CurrentTheme.theme = getThemeByID(if (id == size) 0 else id + 1)
            // update theme text
            themeText.text = getCurrentThemeName()

            saveData()
            setTextures()
        }
    }

    private fun getThemeByID(id: Int): Theme? {
        for (theme in Themes.themes) {
            if (theme.component2().id == id)
                return theme.component2()
        }
        // if no theme was found:
        return null
    }

    private fun getCurrentThemeName(): String {
        return CurrentTheme.theme?.name ?: "Unknown"
    }
    
    private fun generateCars() {
        when ((1..7).random()) {
            1 -> {
                if(noSpawnLane != 3 && !(noTrapFailsafe && lane == 3)) {
                    if(!superSpeed || !((1..25).random() == 25 && lane == 3 && superSpeed)) {
                        spawnCar(3)
                        noSpawnLane = 0
                        previousSpawnLane = 3
                        noTrapFailsafe = false
                    } else {
                        noSpawnLane = 0
                        previousSpawnLane = 0
                        noTrapFailsafe = false
                    }
                } else when ((1..20).random()) {
                    in 3..20 -> generateCars()
                    else -> {
                        noSpawnLane = 0
                        previousSpawnLane = 0
                        noTrapFailsafe = false
                    }
                }
            }
            2 -> {
                if(noSpawnLane != 2) {
                    if(!superSpeed || !((1..25).random() == 25 && lane == 2 && superSpeed)) {
                        if(previousSpawnLane == 1) noSpawnLane = 3
                        if(previousSpawnLane == 3) noSpawnLane = 1
                        if(previousSpawnLane == 2 && noSpawnLane == 0) noTrapFailsafe = true
                        spawnCar(2)
                        previousSpawnLane = 2
                    } else {
                        noSpawnLane = 0
                        previousSpawnLane = 0
                        noTrapFailsafe = false
                    }
                } else when ((1..20).random()) {
                    in 3..20 -> generateCars()
                    else -> {
                        noSpawnLane = 0
                        previousSpawnLane = 0
                        noTrapFailsafe = false
                    }
                }
            }
            3 -> {
                if(noSpawnLane != 1 && !(noTrapFailsafe && lane == 1)) {
                    if(!superSpeed || !((1..25).random() == 25 && lane == 1 && superSpeed)) {
                        spawnCar(1)
                        noSpawnLane = 0
                        previousSpawnLane = 1
                        noTrapFailsafe = false
                    } else {
                        noSpawnLane = 0
                        previousSpawnLane = 0
                        noTrapFailsafe = false
                    }
                } else when ((1..20).random()) {
                    in 3..20 -> generateCars()
                    else -> {
                        noSpawnLane = 0
                        previousSpawnLane = 0
                        noTrapFailsafe = false
                    }
                }
            }
            4 -> {
                if(Random.nextBoolean()) {
                    if(noSpawnLane != 2 && noSpawnLane != 1 && previousSpawnLane != 3 && !(noTrapFailsafe && lane == 1)) {
                        spawnCar(2)
                        spawnCar(1)
                        noSpawnLane = 3
                        noTrapFailsafe = false
                    } else when ((1..30).random()) {
                        in 1..29 -> generateCars()
                        else -> {
                            noSpawnLane = 0
                            previousSpawnLane = 0
                            noTrapFailsafe = false
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
                            noTrapFailsafe = false
                        }
                    }
                } else generateCars()
            }
            6 -> {
                if(Random.nextBoolean()) {
                    if(noSpawnLane != 2 && noSpawnLane != 3 && previousSpawnLane != 1 && !(noTrapFailsafe && lane == 3)) { // !(noTrapFailsafe && lane == 3) is the same as (!noTrapFailsafe || lane != 3)
                        spawnCar(2)
                        spawnCar(3)
                        noSpawnLane = 1
                        noTrapFailsafe = false
                    } else when ((1..30).random()) {
                        in 1..29 -> generateCars()
                        else -> {
                            noSpawnLane = 0
                            previousSpawnLane = 0
                            noTrapFailsafe = false
                        }
                    }
                } else generateCars()
            }
            7 -> when ((1..3).random()) {
                in 1..2 -> generateCars()
                else -> {
                    noSpawnLane = 0
                    previousSpawnLane = 0
                    noTrapFailsafe = false
                }
            }
        }
        if((0..80).random() == 44 && (noSpawnLane != 0 || previousSpawnLane == 0))
            spawnLife(noSpawnLane)
        else if((0..81).random() == 44 && difficulty == 4 && (noSpawnLane != 0 || previousSpawnLane == 0))
            spawnLife(noSpawnLane)
    }

    private fun spawnLife(lane: Int) {
        val newLane = if(lane == 0) (1..3).random() else lane

        var newLife: ImageView? = ImageView(this)
        newLife!!.layoutParams = ConstraintLayout.LayoutParams(
            player.measuredWidth,
            player.measuredHeight
        )
        val lParams = newLife.layoutParams as ConstraintLayout.LayoutParams
        lParams.startToStart = screen.id
        lParams.endToEnd = screen.id
        lParams.topToTop = screen.id
        lParams.bottomToBottom = screen.id
        lParams.horizontalBias = 0.5f
        lParams.verticalBias = 0.0125f

        newLife.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.plus_one_life))

        if(newLane == 1) newLife.x -= (pixelWidth() * 11)
        if(newLane == 3) newLife.x += (pixelWidth() * 11)
//        if(newLane == 0) if(Random.nextBoolean()) newLife.x += (pixelWidth() * 11) else newLife.x -= (pixelWidth() * 11)
        newLife.z = newLane.toFloat() // the z coordinate will be used to more easily tell what lane it is in, since the tag shows how high on the screen, and x might differ between car and life, as the textures are different sizes and X is always on the left side of the image, not the middle.

        getCars().forEach { car: ImageView ->
            if(car.tag == "1" && car.z == newLife!!.z) {
                    newLife = null
            }
        }

        if(newLife != null) {
            newLife!!.tag = "1life"
            screen.addView(newLife)
        }
    }

    private fun spawnCar(lane: Int) {
        val redVehicleTexture = CurrentTheme.theme?.redVehicleTexture ?: R.drawable.red_car
        val greenVehicleTexture = CurrentTheme.theme?.greenVehicleTexture ?: R.drawable.green_car
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

        val colorBool = !Random.nextBoolean()
        newCar.setImageDrawable(
            if (colorBool) AppCompatResources.getDrawable(
                this,
                redVehicleTexture
            ) else AppCompatResources.getDrawable(this, greenVehicleTexture)
        )

        if(lane == 1) newCar.x -= (pixelWidth() * 11)
        if(lane == 3) newCar.x += (pixelWidth() * 11)
        newCar.z = lane.toFloat()

        getCars().forEach { car: ImageView ->
            if(car.tag == "1life" && car.z == newCar.z) {
                car.tag = null
                car.visibility = View.GONE
                screen.removeView(car)
            }
        }
        newCar.tag = "1${if(colorBool) "red" else "green"}"
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
        return (screen.measuredWidth / 38f)
    }

    private fun pixelHeight(): Float {
        return (screen.measuredHeight / 80f)
    }

    private fun playSlowMusic() {
        if(slowMusic == null) {
            slowMusic?.isLooping = true
            slowMusic!!.start()

        } else {
            slowMusic!!.isLooping = true
            slowMusic!!.start()
        }
//        music!!.isLooping = true
    }

    private fun playFastMusic() {
        if(fastMusic == null) {
            fastMusic?.isLooping = true
            fastMusic!!.start()

        } else if(!fastMusic!!.isLooping) {
            fastMusic!!.isLooping = true
            fastMusic !!.start()
        }
        else fastMusic!!.start()
    }

    fun startGame(view: View) {
        if(gameOverTimeout <= 0L) { //just in case it somehow goes below 0 milliseconds
            if(score > highScore) highScore = score
            vibrate(25)
            saveData()
            fastMusic!!.isLooping = false // why is this here???
            slowMusic!!.stop()
            fastMusic!!.stop()
            recreate()
        }
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
        difficulty = shared.getInt("Difficulty", difficulty)

        personalFastest = shared.getFloat("Personal Fastest", personalFastest)

        highScore1 = shared.getInt("Personal Best Lvl1", highScore1)
        highScore2 = shared.getInt("Personal Best Lvl2", highScore2)
        highScore3 = shared.getInt("Personal Best Lvl3", highScore3)
        highScore4 = shared.getInt("Personal Best Lvl4", highScore4)

        averageScore1 = shared.getInt("Average Score Lvl1", averageScore1)
        averageScore2 = shared.getInt("Average Score Lvl2", averageScore2)
        averageScore3 = shared.getInt("Average Score Lvl3", averageScore3)
        averageScore4 = shared.getInt("Average Score Lvl4", averageScore4)

        extraLivesCollected = shared.getInt("Lives Collected", extraLivesCollected)

        totalRacesCompleted = shared.getInt("Total Races", totalRacesCompleted)

        totalRacesDiff1 = shared.getInt("Total Races Lvl1", totalRacesDiff1)
        totalRacesDiff2 = shared.getInt("Total Races Lvl2", totalRacesDiff2)
        totalRacesDiff3 = shared.getInt("Total Races Lvl3", totalRacesDiff3)
        totalRacesDiff4 = shared.getInt("Total Races Lvl4", totalRacesDiff4)

        highScore = when(difficulty) {
            1 -> highScore1
            2 -> highScore2
            3 -> highScore3
            4 -> highScore4
            else -> throw Exception("Unknown difficulty. Unable to correctly read high score.")
        }

        CurrentTheme.theme = themes[(shared.getString(
            "CurrentTheme", CurrentTheme.theme?.name
                ?: when (shared.getBoolean("SpaceMode", false)) {
                    true -> "Space Race"
                    else -> "Race Track" // "false" or if not found, default to RaceTrack
                }
        ))]

        difficulty = shared.getInt("Difficulty", difficulty)
    }

    private fun saveData() {
        val edit = shared.edit()
        edit.putString("CurrentTheme", CurrentTheme.theme?.name)
        when(difficulty) {
            1 -> {
                highScore1 = highScore
                averageScore1 = calculateAverageScore()
            }
            2 -> {
                highScore2 = highScore
                averageScore2 = calculateAverageScore()
            }
            3 -> {
                highScore3 = highScore
                averageScore3 = calculateAverageScore()
            }
            4 -> {
                highScore4 = highScore
                averageScore4 = calculateAverageScore()
            }
            else -> {}
        }
        edit.putInt("Difficulty", difficulty)
        edit.putInt("Personal Best Lvl1", highScore1)
        edit.putInt("Personal Best Lvl2", highScore2)
        edit.putInt("Personal Best Lvl3", highScore3)
        edit.putInt("Personal Best Lvl4", highScore4)
        edit.putFloat("Personal Fastest", personalFastest)
        edit.putInt("Average Score Lvl1", averageScore1)
        edit.putInt("Average Score Lvl2", averageScore2)
        edit.putInt("Average Score Lvl3", averageScore3)
        edit.putInt("Average Score Lvl4", averageScore4)
        edit.putInt("Lives Collected", extraLivesCollected)
        edit.putInt("Total Races", totalRacesCompleted)
        edit.putInt("Total Races Lvl1", totalRacesDiff1)
        edit.putInt("Total Races Lvl2", totalRacesDiff2)
        edit.putInt("Total Races Lvl3", totalRacesDiff3)
        edit.putInt("Total Races Lvl4", totalRacesDiff4)
        edit.apply()
    }

    private fun calculateAverageScore(): Int {
        val oldTotalRaces = when (difficulty) {
            1 -> totalRacesDiff1 - 1
            2 -> totalRacesDiff2 - 1
            3 -> totalRacesDiff3 - 1
            4 -> totalRacesDiff4 - 1
            else -> throw IllegalArgumentException("Invalid difficulty level: $difficulty. Expected values are 1 through 4.")
        }
        var oldAverage = when (difficulty) {
            1 -> averageScore1
            2 -> averageScore2
            3 -> averageScore3
            4 -> averageScore4
            else -> throw IllegalArgumentException("Invalid difficulty level: $difficulty. Expected values are 1 through 4.")
        }
        return ((oldAverage * oldTotalRaces) + score) / (oldTotalRaces + 1)
    }

    private fun rainbowText(target: TextView, hue: Float, speed: Long) {
        var h = hue
        if (h >= 360f) h = 0f
        target.setTextColor(Color.HSVToColor(floatArrayOf(h,100f,100f)))
        target.setShadowLayer(target.shadowRadius, target.shadowDx, target.shadowDy, (Color.HSVToColor(floatArrayOf(h,100f,0.3f))))
        Handler(Looper.getMainLooper()).postDelayed({ rainbowText(target, (h + 1f), speed) }, (speed))
    }

    fun menuButton(view: View) {
        if(gamePlaying) {
            if(paused && playing == 0) resume(view)
            else if(!paused && playing == 1) pause(view)
        }
    }

    private fun pause(view: View) {
        if(!pauseCooldown) {
            pauseCooldown = true
            view.backgroundTintList = getColorStateList(R.color.pause_btn_inactive)
            menuButton.visibility = View.VISIBLE
            themeSelector.visibility = View.VISIBLE
            scoreText.visibility = View.GONE
            resumeButton.visibility = View.VISIBLE
            resumeButton.backgroundTintList = getColorStateList(R.color.light_blue)
            finalScoreText.visibility = View.VISIBLE
            finalScoreText.text = getString(R.string.score, score.toString())
            playing--
            paused = true
            progressing = false
            if(fastMusic!!.isPlaying) fastMusic!!.pause()
            slowMusic!!.pause()
            if(gamePlaying) Toast.makeText(this, "Game Paused.", Toast.LENGTH_SHORT).show()
            Handler().postDelayed({
                pauseCooldown = false
                view.backgroundTintList = getColorStateList(R.color.pause_btn_active)
            }, 500L)
        } //else Toast.makeText(this,"Unable to pause. Please wait a moment after resuming.", Toast.LENGTH_SHORT).show()
    }

    private fun resume(view: View) {
        if(!pauseCooldown) {
            if(paused) {
                paused = false
                pauseCooldown = true
                view.backgroundTintList = getColorStateList(R.color.pause_btn_inactive)
                menuButton.visibility = View.GONE
                themeSelector.visibility = View.GONE
                scoreText.visibility = View.VISIBLE
                findViewById<Button>(R.id.btn_resume).visibility = View.GONE
                finalScoreText.visibility = View.GONE
                if(adjustedGameSpeed() > 310 && difficulty != 4) {
                    slowMusic!!.start()
                } else if(score > 5 || difficulty == 4){
                    playFastMusic()
                }
                if(!progressing && gamePlaying) {
                    progressing = true
                    Handler().postDelayed({
                        if(!paused && playing == 0) {
                            play()
                            Toast.makeText(this, "Game Resumed.", Toast.LENGTH_SHORT).show()
                        }
                    }, 250L)
                }
                Handler().postDelayed({
                    pauseCooldown = false
                    view.backgroundTintList = getColorStateList(R.color.pause_btn_active)
                }, 550L)
            }
        } //else Toast.makeText(this,"Unable to resume. Please wait a moment after pausing.", Toast.LENGTH_SHORT).show()
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
//        TODO("Not yet implemented")
    }

    override fun onActivityStarted(activity: Activity) {
//        TODO("Not yet implemented")
    }

    override fun onActivityResumed(activity: Activity) {
        isAppInForeground = true
        println("Activity Resumed")
//        if(!paused) {
            if(!progressing && gamePlaying && !paused) {
                if(CurrentTheme.theme?.name == "Rainbow Kart" || (adjustedGameSpeed() > 310 && difficulty != 4)) {
                    slowMusic!!.start()
                } else if(score > 5 || difficulty == 4) {
                    playFastMusic()
                }
                progressing = true
                Handler().postDelayed({
                    if(!paused) {
                        play()
                        Toast.makeText(this, "Game Resumed.", Toast.LENGTH_SHORT).show()
                    }
                }, 250L)
            }
//        }
    }


    override fun onActivityPaused(activity: Activity) {
        isAppInForeground = false
        println("Activity Paused")
        if(!paused && playing > 0) {
            playing --
            progressing = false
            if(fastMusic!!.isPlaying) fastMusic!!.pause()
            slowMusic!!.pause()
            if(gamePlaying) Toast.makeText(this,"Game Paused.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityStopped(activity: Activity) {
//        TODO("Not yet implemented")
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
//        TODO("Not yet implemented")
    }

    override fun onActivityDestroyed(activity: Activity) {
//        TODO("Not yet implemented")
    }

    fun mainMenu(view: View) {
        if(gameOverTimeout <= 0L) {
            saveData()
            startActivity(Intent(this@MainActivity, MainMenuActivity::class.java))
            finish()
        }
    }

}