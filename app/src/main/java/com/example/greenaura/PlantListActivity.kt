package com.example.greenaura

import android.content.Intent
import android.os.Bundle
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore

class PlantListActivity : AppCompatActivity() {

    private lateinit var adapter: PlantAdapter
    private val plantList = mutableListOf<Plant>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plant_list)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    // Already in PlantListActivity (home)
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
                R.id.remainder_home -> {
                    startActivity(Intent(this, PlantRemainder::class.java))
                    true
                }
                else -> false
            }
        }

        val recyclerView = findViewById<RecyclerView>(R.id.plantRecyclerView)
        val searchView = findViewById<SearchView>(R.id.searchEditText)

        adapter = PlantAdapter(this, plantList) { plant ->
            val intent = Intent(this, PlantDetailActivity::class.java)
            intent.putExtra("plant", plant)
            startActivity(intent)
        }

        // Use Grid layout for 2 cards in a row
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = adapter

        fetchPlantData()

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                val query = newText?.lowercase() ?: ""
                val filtered = plantList.filter {
                    it.common_name.lowercase().contains(query)
                }
                adapter.filterList(filtered)
                return true
            }
        })
    }

    private fun fetchPlantData() {
        FirebaseFirestore.getInstance().collection("plants")
            .get()
            .addOnSuccessListener { result ->
                plantList.clear()
                for (doc in result) {
                    val plant = doc.toObject(Plant::class.java)
                    plantList.add(plant)
                }
                adapter.filterList(plantList) // initialize with full list
            }
            .addOnFailureListener {
                // handle error if needed
            }
    }
}
