package com.example.greenaura

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class PlantDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plant_detail)

        val plant = intent.getSerializableExtra("plant") as? Plant ?: return

        val imageView = findViewById<ImageView>(R.id.imageView)
        val nameText = findViewById<TextView>(R.id.nameText)
        val descText = findViewById<TextView>(R.id.descText)
        val tempText = findViewById<TextView>(R.id.tempText)
        val phText = findViewById<TextView>(R.id.phText)
        val soilText = findViewById<TextView>(R.id.soilText)
        val waterText = findViewById<TextView>(R.id.waterText)
        val spaceText = findViewById<TextView>(R.id.spaceText)

        // Set plant details
        nameText.text = plant.common_name
        descText.text = plant.Description
        tempText.text = "Temperature: ${plant.Temperature}°C"
        phText.text = "PH: ${plant.PH}"
        soilText.text = "Soil: ${plant.Soil}"
        waterText.text = "Water Level: ${plant.Waterlevel}"
        spaceText.text = "Space: ${plant.Space_} m²"

        // Log the image URL for debugging
        Log.d("PlantDetailActivity", "Image URL: ${plant.Image_Src}")

        // Load image using Glide with placeholder and error image
        Glide.with(this)
            .load(plant.Image_Src)
            .placeholder(R.drawable.ic_placeholder) // Make sure this drawable exists
            .error(R.drawable.ic_error) // Make sure this drawable exists
            .into(imageView)
    }
}
