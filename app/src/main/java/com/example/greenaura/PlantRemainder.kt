package com.example.greenaura


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView


class PlantRemainder : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remainder)


        val addButton = findViewById<Button>(R.id.addButton)
        addButton.setOnClickListener {
            // Show a Toast message or perform an action
            startActivity(Intent(this, AddPlantActivity::class.java)) // Navigate to AddPlantActivity
        }


        // Set up BottomNavigationView
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    startActivity(Intent(this, PlantListActivity::class.java))
                    true
                }
                R.id.remainder_home -> {
                    // Prevent re-navigation if already on this page
                    true
                }
                R.id.menu_cart -> {
                    startActivity(Intent(this, CartActivity::class.java))
                    true
                }
                R.id.menu_account -> {
                    startActivity(Intent(this, AccountActivity::class.java))
                    true
                }
                R.id.menu_products -> {
                    startActivity(Intent(this, ProductListActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}
