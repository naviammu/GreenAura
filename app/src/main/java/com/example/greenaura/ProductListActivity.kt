package com.example.greenaura

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore

class ProductListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var productList: ArrayList<Product>
    private lateinit var filteredList: ArrayList<Product>
    private lateinit var adapter: ProductListAdapter
    private lateinit var bottomNavigationView: BottomNavigationView
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_list)

        recyclerView = findViewById(R.id.recyclerViewProducts)
        searchView = findViewById(R.id.searchViewProducts)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        // Set up GridLayoutManager with 2 columns
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        productList = ArrayList()
        filteredList = ArrayList()

        adapter = ProductListAdapter(filteredList) { product ->
            // Handle product click (navigate to ProductDetailActivity)
            val intent = Intent(this, ProductDetailActivity::class.java)
            intent.putExtra("product", product)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        fetchProducts()

        // Set up the search functionality
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterProducts(newText.orEmpty())
                return true
            }
        })

        // Set up bottom navigation functionality
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    // Handle home navigation
                    startActivity(Intent(this, PlantListActivity::class.java))
                    true
                }
                R.id.menu_cart -> {
                    // Handle cart navigation
                    startActivity(Intent(this, CartActivity::class.java)) // Assuming you have a CartActivity
                    true
                }
                R.id.menu_account -> {
                    // Handle account navigation
                    startActivity(Intent(this, AccountActivity::class.java)) // Assuming you have an AccountActivity
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
    }

    private fun fetchProducts() {
        db.collection("products")
            .get()
            .addOnSuccessListener { result ->
                productList.clear()
                for (document in result) {
                    val product = document.toObject(Product::class.java)
                    productList.add(product)
                }
                // Initially show the full product list
                filteredList.addAll(productList)
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.w("ProductList", "Error getting products: ", exception)
            }
    }

    private fun filterProducts(query: String) {
        filteredList.clear()
        if (query.isEmpty()) {
            // Show all products if the query is empty
            filteredList.addAll(productList)
        } else {
            // Filter based on product name or description
            for (product in productList) {
                if (product.productName.contains(query, ignoreCase = true) ||
                    product.description.contains(query, ignoreCase = true)) {
                    filteredList.add(product)
                }
            }
        }
        adapter.notifyDataSetChanged()
    }
}
