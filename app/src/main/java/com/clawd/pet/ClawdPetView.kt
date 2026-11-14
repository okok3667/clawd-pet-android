package com.clawd.pet

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import kotlin.math.abs
import kotlin.random.Random

@SuppressLint("ClickableViewAccessibility")
class ClawdPetView(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val rootView: View = LayoutInflater.from(context).inflate(R.layout.layout_clawd_overlay, null)
    private val imageView: ImageView = rootView.findViewById(R.id.clawd_image)
    private val bubbleView: TextView = rootView.findViewById(R.id.clawd_bubble)

    private val imageLoader: ImageLoader = ImageLoader.Builder(context)
        .components {
            if (Build.VERSION.SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }
        .build()

    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = false

    // Window layout params
    val layoutParams = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        },
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        PixelFormat.TRANSLUCENT
    ).apply {
        gravity = Gravity.TOP or Gravity.START
    }

    // Configurable properties
    var crabSizeDp: Int = 80
        set(value) {
            field = value
            updateSize()
        }
    var walkSpeedMultiplier: Float = 1.0f

    // Movement state
    private var posX: Float = 100f
    private var posY: Float = 1000f
    private var velocityY: Float = 0f
    private val gravity: Float = 1.8f

    private var currentBehavior: PetBehavior = PetBehavior.FALLING
    private var currentAnimation: ClawdAnimation? = null
    private var behaviorTicksLeft: Int = 0

    // Touch handling
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var initialPosX = 0f
    private var initialPosY = 0f
    private var hasMoved = false

    private val cuteThoughts = listOf(
        "🦀 Chào bạn!",
        "thinking...",
        "all done! 🎉",
        "writing code...",
        "running a command...",
        "clack clack 🦀",
        "Chúc bạn ngày mới vui vẻ!",
        "Antigravity Watch 🚀",
        "Có gì khó cứ để Clawd lo!"
    )

    init {
        updateSize()
        setupTouchListener()
    }

    private fun getScreenWidth(): Int {
        val dm = DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getMetrics(dm)
        return dm.widthPixels
    }

    private fun getScreenHeight(): Int {
        val dm = DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getMetrics(dm)
        return dm.heightPixels
    }

    private fun updateSize() {
        val density = context.resources.displayMetrics.density
        val sizePx = (crabSizeDp * density).toInt()
        val params = imageView.layoutParams
        params.width = sizePx
        params.height = sizePx
        imageView.layoutParams = params
    }

    fun show() {
        if (rootView.windowToken == null) {
            val screenW = getScreenWidth()
            val screenH = getScreenHeight()
            posX = (screenW / 2 - 80).toFloat()
            posY = (screenH - 300).toFloat()
            layoutParams.x = posX.toInt()
            layoutParams.y = posY.toInt()

            windowManager.addView(rootView, layoutParams)
            isRunning = true
            setAnimation(ClawdAnimation.WALKING)
            startLoop()
        }
    }

    fun hide() {
        isRunning = false
        handler.removeCallbacksAndMessages(null)
        if (rootView.windowToken != null) {
            windowManager.removeView(rootView)
        }
    }

    fun setBubbleText(text: String?, durationMs: Long = 4000) {
        handler.post {
            if (text.isNullOrBlank()) {
                bubbleView.visibility = View.GONE
            } else {
                bubbleView.text = text
                bubbleView.visibility = View.VISIBLE
                handler.removeCallbacks(hideBubbleRunnable)
                handler.postDelayed(hideBubbleRunnable, durationMs)
            }
        }
    }

    private val hideBubbleRunnable = Runnable {
        bubbleView.visibility = View.GONE
    }

    fun setAnimation(animation: ClawdAnimation) {
        if (currentAnimation == animation) return
        currentAnimation = animation

        val request = ImageRequest.Builder(context)
            .data("file:///android_asset/${animation.assetFile}")
            .target(imageView)
            .build()
        imageLoader.enqueue(request)
    }

    private fun setupTouchListener() {
        rootView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    initialPosX = posX
                    initialPosY = posY
                    hasMoved = false
                    currentBehavior = PetBehavior.DRAGGED
                    setAnimation(ClawdAnimation.LURKING)
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - initialTouchX
                    val dy = event.rawY - initialTouchY
                    if (abs(dx) > 10 || abs(dy) > 10) {
                        hasMoved = true
                    }
                    posX = initialPosX + dx
                    posY = initialPosY + dy
                    layoutParams.x = posX.toInt()
                    layoutParams.y = posY.toInt()
                    if (rootView.windowToken != null) {
                        windowManager.updateViewLayout(rootView, layoutParams)
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!hasMoved) {
                        // Click / Tap on the crab
                        onPetTapped()
                    } else {
                        // Dragged and released in air
                        currentBehavior = PetBehavior.FALLING
                        velocityY = 0f
                        setAnimation(ClawdAnimation.JUMPING)
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun onPetTapped() {
        currentBehavior = PetBehavior.PERFORMING
        behaviorTicksLeft = 60 // ~2 seconds
        val tapAnims = listOf(ClawdAnimation.DANCING, ClawdAnimation.JUMPING, ClawdAnimation.WAVING)
        setAnimation(tapAnims.random())
        setBubbleText(cuteThoughts.random(), 3000)
    }

    private fun startLoop() {
        handler.post(object : Runnable {
            override fun run() {
                if (!isRunning) return
                tickPhysics()
                if (rootView.windowToken != null) {
                    layoutParams.x = posX.toInt()
                    layoutParams.y = posY.toInt()
                    windowManager.updateViewLayout(rootView, layoutParams)
                }
                handler.postDelayed(this, 30) // ~33 FPS
            }
        })
    }

    private fun tickPhysics() {
        val screenW = getScreenWidth()
        val screenH = getScreenHeight()
        val density = context.resources.displayMetrics.density
        val crabWidthPx = crabSizeDp * density
        val crabHeightPx = crabSizeDp * density
        val floorY = screenH - crabHeightPx - (20 * density) // Navigation bar offset

        if (currentBehavior == PetBehavior.DRAGGED) {
            return
        }

        // Apply gravity if in the air
        if (posY < floorY) {
            velocityY += gravity
            posY += velocityY
            if (posY >= floorY) {
                posY = floorY
                velocityY = 0f
                currentBehavior = if (Random.nextBoolean()) PetBehavior.WALKING_LEFT else PetBehavior.WALKING_RIGHT
                setAnimation(ClawdAnimation.WALKING)
            }
            return
        } else {
            posY = floorY
        }

        // Behavior decision logic
        if (behaviorTicksLeft > 0) {
            behaviorTicksLeft--
            if (behaviorTicksLeft == 0) {
                pickNextBehavior()
            }
        } else {
            pickNextBehavior()
        }

        val step = 3f * walkSpeedMultiplier

        when (currentBehavior) {
            PetBehavior.WALKING_LEFT -> {
                posX -= step
                imageView.scaleX = 1f // Face left
                if (posX <= 10) {
                    posX = 10f
                    currentBehavior = PetBehavior.WALKING_RIGHT
                    imageView.scaleX = -1f // Face right
                }
            }
            PetBehavior.WALKING_RIGHT -> {
                posX += step
                imageView.scaleX = -1f // Face right
                val maxX = screenW - crabWidthPx - 10
                if (posX >= maxX) {
                    posX = maxX
                    currentBehavior = PetBehavior.WALKING_LEFT
                    imageView.scaleX = 1f // Face left
                }
            }
            PetBehavior.IDLE -> {
                // Standing still
            }
            PetBehavior.PERFORMING -> {
                // Dancing or waving
            }
            else -> {}
        }
    }

    private fun pickNextBehavior() {
        val rand = Random.nextInt(100)
        when {
            rand < 55 -> {
                // 55% Walk
                currentBehavior = if (Random.nextBoolean()) PetBehavior.WALKING_LEFT else PetBehavior.WALKING_RIGHT
                behaviorTicksLeft = Random.nextInt(90, 200) // 3 - 6 seconds
                setAnimation(ClawdAnimation.WALKING)
            }
            rand < 75 -> {
                // 20% Idle
                currentBehavior = PetBehavior.IDLE
                behaviorTicksLeft = Random.nextInt(60, 150)
                setAnimation(ClawdAnimation.STILL)
            }
            rand < 90 -> {
                // 15% Cute animation
                currentBehavior = PetBehavior.PERFORMING
                behaviorTicksLeft = Random.nextInt(60, 120)
                val special = listOf(ClawdAnimation.DANCING, ClawdAnimation.WAVING, ClawdAnimation.COOKING)
                setAnimation(special.random())
            }
            else -> {
                // 10% Speak random thought
                currentBehavior = PetBehavior.IDLE
                behaviorTicksLeft = 90
                setAnimation(ClawdAnimation.WAVING)
                setBubbleText(cuteThoughts.random(), 3500)
            }
        }
    }
}
