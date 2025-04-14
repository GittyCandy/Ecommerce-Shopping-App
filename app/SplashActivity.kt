package com.example.myecommerceapp2

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SplashActivity : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Apply edge-to-edge effect using the root view with ID "main"
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Setup the advanced animation for the logo
        val logoImage = findViewById<ImageView>(R.id.splashImage)
        val splashAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in_logo)
        logoImage.startAnimation(splashAnimation)

        // Play the sound effect
        mediaPlayer = MediaPlayer.create(this, R.raw.splash_sound)
        mediaPlayer.start()

        // Navigate to LoginActivity after a delay of 3 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, 5000)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release() // Release media player resources
    }
}
