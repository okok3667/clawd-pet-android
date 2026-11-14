package com.clawd.pet

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.slider.Slider

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var btnToggle: MaterialButton
    private lateinit var cardPermission: MaterialCardView
    private lateinit var btnGrantPermission: MaterialButton
    private lateinit var sliderSize: Slider
    private lateinit var sliderSpeed: Slider
    private lateinit var tvSizeLabel: TextView
    private lateinit var tvSpeedLabel: TextView

    private val prefs by lazy { getSharedPreferences("clawd_prefs", Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tv_status)
        btnToggle = findViewById(R.id.btn_toggle_pet)
        cardPermission = findViewById(R.id.card_permission)
        btnGrantPermission = findViewById(R.id.btn_grant_permission)
        sliderSize = findViewById(R.id.slider_size)
        sliderSpeed = findViewById(R.id.slider_speed)
        tvSizeLabel = findViewById(R.id.tv_size_label)
        tvSpeedLabel = findViewById(R.id.tv_speed_label)

        // Load saved preferences
        val savedSize = prefs.getInt("pet_size", 80)
        val savedSpeed = prefs.getFloat("pet_speed", 1.0f)

        sliderSize.value = savedSize.toFloat().coerceIn(sliderSize.valueFrom, sliderSize.valueTo)
        sliderSpeed.value = savedSpeed.coerceIn(sliderSpeed.valueFrom, sliderSpeed.valueTo)
        tvSizeLabel.text = "Kích thước: $savedSize dp"
        tvSpeedLabel.text = "Tốc độ bò: ${savedSpeed}x"

        btnGrantPermission.setOnClickListener {
            requestOverlayPermission()
        }

        btnToggle.setOnClickListener {
            if (!hasOverlayPermission()) {
                requestOverlayPermission()
                return@setOnClickListener
            }
            if (ClawdPetService.isRunning) {
                stopPetService()
            } else {
                startPetService()
            }
            updateUiState()
        }

        sliderSize.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                val size = value.toInt()
                tvSizeLabel.text = "Kích thước: $size dp"
                prefs.edit().putInt("pet_size", size).apply()
                sendConfigUpdate(size, -1f)
            }
        }

        sliderSpeed.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                tvSpeedLabel.text = "Tốc độ bò: ${value}x"
                prefs.edit().putFloat("pet_speed", value).apply()
                sendConfigUpdate(-1, value)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateUiState()
    }

    private fun hasOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }
    }

    private fun startPetService() {
        val intent = Intent(this, ClawdPetService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }

    private fun stopPetService() {
        val intent = Intent(this, ClawdPetService::class.java).apply {
            action = ClawdPetService.ACTION_STOP
        }
        startService(intent)
    }

    private fun sendConfigUpdate(size: Int, speed: Float) {
        if (ClawdPetService.isRunning) {
            val intent = Intent(this, ClawdPetService::class.java).apply {
                action = ClawdPetService.ACTION_UPDATE_CONFIG
                if (size > 0) putExtra(ClawdPetService.EXTRA_SIZE, size)
                if (speed > 0f) putExtra(ClawdPetService.EXTRA_SPEED, speed)
            }
            startService(intent)
        }
    }

    private fun updateUiState() {
        val hasPerm = hasOverlayPermission()
        cardPermission.visibility = if (hasPerm) View.GONE else View.VISIBLE

        if (ClawdPetService.isRunning) {
            tvStatus.text = getString(R.string.status_running)
            btnToggle.text = getString(R.string.btn_stop)
            btnToggle.setBackgroundColor(ContextCompat.getColor(this, R.color.clawd_dark))
        } else {
            tvStatus.text = getString(R.string.status_stopped)
            btnToggle.text = getString(R.string.btn_start)
            btnToggle.setBackgroundColor(ContextCompat.getColor(this, R.color.clawd_orange))
        }
    }
}
