package com.example.greenaura

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)

        val welcomeText = findViewById<TextView>(R.id.welcomeText)
        val imageView = findViewById<ImageView>(R.id.plantGif)

        // Initially set both TextView and ImageView to invisible in the XML

        // Load GIF using Glide
        Glide.with(this)
            .asGif()
            .load(R.raw.plant) // Your GIF in res/raw/plant_gif.gif
            .into(imageView)

        // Show the text after 2 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            welcomeText.visibility = View.VISIBLE
            welcomeText.alpha = 0f
            welcomeText.animate().alpha(1f).setDuration(500).start() // Fade-in effect
        }, 1000) // Delay of 2 seconds

        // Show the GIF after 4 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            imageView.visibility = View.VISIBLE
            imageView.alpha = 0f
            imageView.animate().alpha(1f).setDuration(500).start() // Fade-in effect
        }, 2000) // Delay of 4 seconds

        // Add a delay to show the splash screen for 3 more seconds after the GIF
        Handler(Looper.getMainLooper()).postDelayed({
            // Navigate to MainActivity after 3 seconds
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Finish splash activity to prevent back navigation
        }, 4000) // Total delay of 7 seconds (3 seconds after the GIF appears)
    }
}
