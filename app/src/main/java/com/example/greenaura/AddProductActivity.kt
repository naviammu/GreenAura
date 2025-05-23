package com.example.greenaura

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class AddProductActivity : AppCompatActivity() {

    private lateinit var productNameEditText: EditText
    private lateinit var priceEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var selectImageButton: Button
    private lateinit var addProductButton: Button

    private var selectedImageUrl: String = ""  // Store the image URL from PostImage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)

        productNameEditText = findViewById(R.id.productNameEditText)
        priceEditText = findViewById(R.id.priceEditText)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        selectImageButton = findViewById(R.id.selectImageButton)
        addProductButton = findViewById(R.id.addProductButton)

        // Select Image Button Click Listener
        selectImageButton.setOnClickListener {
            // Open a gallery or an image picker
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galleryIntent, 1001)
        }

        // Add Product Button Click Listener
        addProductButton.setOnClickListener {
            // Validate and add product to Firestore
            val productName = productNameEditText.text.toString()
            val price = priceEditText.text.toString().toDoubleOrNull()
            val description = descriptionEditText.text.toString()

            if (productName.isNotEmpty() && price != null && description.isNotEmpty() && selectedImageUrl.isNotEmpty()) {
                addProductToFirestore(productName, price, description, selectedImageUrl)
            } else {
                Toast.makeText(this, "Please fill all fields and select an image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Function to add product to Firestore
    private fun addProductToFirestore(productName: String, price: Double, description: String, imageUrl: String) {
        val db = FirebaseFirestore.getInstance()

        // Example product data
        val product = hashMapOf(
            "productName" to productName,
            "price" to price,
            "imageUrl" to imageUrl,  // Use the Direct URL from PostImage
            "description" to description,
            "category" to "seeds"  // Example category
        )

        // Add the product to Firestore under the "products" collection
        db.collection("products")
            .add(product)
            .addOnSuccessListener { documentReference ->
                Log.d("Firestore", "Product added with ID: ${documentReference.id}")
                Toast.makeText(this, "Product added successfully!", Toast.LENGTH_SHORT).show()
                finish()  // Close the activity
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error adding product", e)
                Toast.makeText(this, "Failed to add product", Toast.LENGTH_SHORT).show()
            }
    }

    // Handle image selection result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            // Simulate getting the selected image URL (replace this with actual image URL from PostImage)
            selectedImageUrl = "https://i.postimg.cc/yourimage.jpg"  // Simulate the URL here
            Toast.makeText(this, "Image selected successfully", Toast.LENGTH_SHORT).show()
        }
    }
}
