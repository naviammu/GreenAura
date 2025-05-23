package com.example.greenaura

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var productImageView: ImageView
    private lateinit var productNameTextView: TextView
    private lateinit var productPriceTextView: TextView
    private lateinit var productDescriptionTextView: TextView
    private lateinit var buyNowButton: Button
    private lateinit var addToCartButton: Button

    private val db = FirebaseFirestore.getInstance()
    private lateinit var product: Product

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        // Initialize views
        productImageView = findViewById(R.id.imageViewProduct)
        productNameTextView = findViewById(R.id.textViewProductName)
        productPriceTextView = findViewById(R.id.textViewProductPrice)
        productDescriptionTextView = findViewById(R.id.textViewProductDescription)
        buyNowButton = findViewById(R.id.buttonBuyNow)
        addToCartButton = findViewById(R.id.buttonAddToCart)

        // Get the passed product object from the Intent
        product = intent.getParcelableExtra("product") ?: run {
            Toast.makeText(this, "Product data is missing!", Toast.LENGTH_SHORT).show()
            finish() // Exit if no product is found
            return
        }

        // Load product details
        product?.let {
            productNameTextView.text = it.productName
            productPriceTextView.text = "₹${it.price}"
            productDescriptionTextView.text = it.description

            // Load product image using Glide
            Glide.with(this)
                .load(it.imageUrl)
                .into(productImageView)
        }

        // Buy Now button click handler
        buyNowButton.setOnClickListener {
            val intent = Intent(this, BuyNowActivity::class.java)
            intent.putExtra("productImage", product.imageUrl)
            intent.putExtra("productName", product.productName)
            intent.putExtra("productPrice", product.price)
            startActivity(intent)
        }

        // Add to Cart button click handler
        addToCartButton.setOnClickListener {
            addToCart(product)
        }
    }

    // Function to add the product to Firestore cart
    private fun addToCart(product: Product) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            val cartItem = hashMapOf(
                "productName" to product.productName,
                "price" to product.price,
                "quantity" to 1, // Default quantity set to 1
                "imageUrl" to product.imageUrl, // Include the imageUrl in the cart data
                "userId" to userId // Include the userId for permission checking
            )

            db.collection("cart")
                .add(cartItem)
                .addOnSuccessListener {
                    Toast.makeText(this, "${product.productName} added to cart!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to add to cart: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "You need to be logged in to add items to the cart.", Toast.LENGTH_SHORT).show()
        }
    }
}
