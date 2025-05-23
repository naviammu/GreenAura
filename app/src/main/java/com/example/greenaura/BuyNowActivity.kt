package com.example.greenaura

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.razorpay.Checkout
import org.json.JSONObject
import com.razorpay.PaymentResultListener


class BuyNowActivity : AppCompatActivity() , PaymentResultListener{

    private lateinit var imageProduct: ImageView
    private lateinit var textProductName: TextView
    private lateinit var textProductPrice: TextView
    private lateinit var textQuantity: TextView
    private lateinit var addAddressText: TextView
    private lateinit var buttonAddAddress: Button
    private lateinit var buttonChangeAddress: Button
    private lateinit var buttonPlaceOrder: Button
    private lateinit var addressCardContainer: LinearLayout
    private var selectedAddressCard: View? = null
    private var selectedDeliverButton: Button? = null

    private var quantity = 1
    private var unitPrice = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buy_now)

        imageProduct = findViewById(R.id.imageProduct)
        textProductName = findViewById(R.id.textProductName)
        textProductPrice = findViewById(R.id.textProductPrice)
        textQuantity = findViewById(R.id.textQuantity)
        addressCardContainer = findViewById(R.id.addressCardContainer)
        addAddressText = findViewById(R.id.textAddAddress)
        buttonAddAddress = findViewById(R.id.buttonAddAddress)
        buttonChangeAddress = findViewById(R.id.buttonChangeAddress)
        buttonPlaceOrder = findViewById(R.id.buttonPlaceOrder)

        val imageUrl = intent.getStringExtra("productImage")
        val name = intent.getStringExtra("productName")
        unitPrice = intent.getDoubleExtra("productPrice", 0.0)

        Glide.with(this).load(imageUrl).into(imageProduct)
        textProductName.text = name
        textQuantity.text = quantity.toString()
        updateTotalPrice()

        findViewById<Button>(R.id.btnIncrease).setOnClickListener {
            quantity++
            textQuantity.text = quantity.toString()
            updateTotalPrice()
        }

        findViewById<Button>(R.id.btnDecrease).setOnClickListener {
            if (quantity > 1) {
                quantity--
                textQuantity.text = quantity.toString()
                updateTotalPrice()
            }
        }

        loadUserAddresses()
        setupChangeAddressButton()

        buttonAddAddress.setOnClickListener {
            val intent = Intent(this, AccountActivity::class.java)
            intent.putExtra("fromBuyNow", true)
            startActivity(intent)
        }

        buttonPlaceOrder.setOnClickListener {
            if (selectedAddressCard == null) {
                Toast.makeText(this, "Please select a delivery address", Toast.LENGTH_SHORT).show()
            } else {
                val selectedAddress = selectedAddressCard?.findViewById<TextView>(R.id.textViewAddress)?.text.toString()
                val productName = textProductName.text.toString()
                val productPrice = textProductPrice.text.toString()
                val selectedQuantity = quantity


                // Convert price to paise (100 paise = 1 INR)
                val amountInPaise = (productPrice.replace("₹", "").toDouble() * 100).toInt()


                // Razorpay payment initiation
                val checkout = Checkout()
                checkout.setKeyID("rzp_test_1EQyeSHCwNoCy5")  // Add your Razorpay key here


                try {
                    val options = JSONObject()
                    options.put("name", "Greenaura")
                    options.put("description", productName)
                    options.put("image", "https://your_image_url.com")  // Optional product image URL
                    options.put("currency", "INR")
                    options.put("amount", amountInPaise)  // Amount in paise
                    options.put("prefill.email", FirebaseAuth.getInstance().currentUser?.email)
                    options.put("prefill.contact", "1234567890")  // Optional, use actual contact number


                    checkout.open(this, options)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Payment initialization failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    private fun updateTotalPrice() {
        val totalPrice = unitPrice * quantity
        textProductPrice.text = "₹${String.format("%.2f", totalPrice)}"
    }

    private fun loadUserAddresses() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("addresses")
                .get()
                .addOnSuccessListener { result ->
                    if (result.isEmpty) {
                        addAddressText.visibility = View.VISIBLE
                        buttonAddAddress.visibility = View.VISIBLE
                    } else {
                        for (doc in result) {
                            val address = doc.getString("houseNumber") + ", " +
                                    doc.getString("StreetName") + ", " +
                                    doc.getString("city") + ", " +
                                    doc.getString("state") + " - " +
                                    doc.getString("postalCode")

                            val phoneNumber = doc.getString("phoneNumber") ?: "N/A"

                            val cardView = layoutInflater.inflate(R.layout.item_address_card, null)
                            val textViewAddress = cardView.findViewById<TextView>(R.id.textViewAddress)
                            val textViewPhone = cardView.findViewById<TextView>(R.id.textViewPhoneNumber)
                            val buttonDeliverHere = cardView.findViewById<Button>(R.id.buttonDeliverHere)

                            textViewAddress.text = address
                            textViewPhone.text = "Phone: $phoneNumber"

                            buttonDeliverHere.setOnClickListener {
                                selectedAddressCard?.setBackgroundResource(android.R.color.transparent)
                                selectedDeliverButton?.visibility = View.VISIBLE

                                selectedAddressCard = cardView
                                selectedDeliverButton = buttonDeliverHere

                                buttonDeliverHere.visibility = View.GONE

                                for (i in 0 until addressCardContainer.childCount) {
                                    val child = addressCardContainer.getChildAt(i)
                                    if (child != cardView) child.visibility = View.GONE
                                }

                                buttonChangeAddress.visibility = View.VISIBLE
                            }

                            addressCardContainer.addView(cardView)
                        }
                    }
                }
        }
    }

    private fun setupChangeAddressButton() {
        buttonChangeAddress.setOnClickListener {
            for (i in 0 until addressCardContainer.childCount) {
                addressCardContainer.getChildAt(i).visibility = View.VISIBLE
            }

            selectedAddressCard?.setBackgroundResource(android.R.color.transparent)
            selectedDeliverButton?.visibility = View.VISIBLE

            selectedAddressCard = null
            selectedDeliverButton = null

            buttonChangeAddress.visibility = View.GONE
        }
    }

    // Razorpay Payment Result Listener methods
    override fun onPaymentSuccess(razorpayPaymentID: String?) {
        // Save the order details to Firebase
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val orderDetails = hashMapOf(
                "productName" to textProductName.text.toString(),
                "productPrice" to textProductPrice.text.toString(),
                "quantity" to quantity,
                "address" to selectedAddressCard?.findViewById<TextView>(R.id.textViewAddress)?.text.toString(),
                "razorpayPaymentID" to razorpayPaymentID
            )


            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("orders")  // Automatically creates 'orders' subcollection if it doesn't exist
                .add(orderDetails)
                .addOnSuccessListener {
                    Toast.makeText(this, "Order placed successfully!\nThank you for your purchase!", Toast.LENGTH_LONG).show()
                    // Optionally, navigate to Order Success screen
                    // startActivity(Intent(this, OrderSuccessActivity::class.java))
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error placing order: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }


    override fun onPaymentError(errorCode: Int, response: String?) {
        Toast.makeText(this, "Payment failed: $response", Toast.LENGTH_SHORT).show()
    }
}
