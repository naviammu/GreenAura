package com.example.greenaura


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.razorpay.Checkout
import org.json.JSONObject
import com.razorpay.PaymentResultListener


class CartActivity : AppCompatActivity(), PaymentResultListener {


    private lateinit var recyclerView: RecyclerView
    private lateinit var cartList: ArrayList<CartItem>
    private lateinit var cartAdapter: CartAdapter
    private lateinit var totalPriceTextView: TextView
    private lateinit var addressContainer: LinearLayout
    private lateinit var emptyCartImageView: ImageView
    private lateinit var buttonPlaceOrder: Button
    private val db = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private var selectedCardView: View? = null // Selected address card
    private var totalPrice = 0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)


        recyclerView = findViewById(R.id.recyclerViewCart)
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)


        totalPriceTextView = findViewById(R.id.textViewTotalPrice)
        emptyCartImageView = findViewById(R.id.imageViewEmptyCart)
        addressContainer = findViewById(R.id.addressContainer)
        buttonPlaceOrder = findViewById(R.id.buttonPlaceOrder)


        cartList = ArrayList()


        cartAdapter = CartAdapter(
            cartList,
            onRemoveClick = { cartItem ->
                removeCartItem(cartItem)
                updateTotalPrice()
            },
            onQuantityChange = { updateTotalPrice() }
        )


        recyclerView.adapter = cartAdapter


        fetchCartItems()


        currentUser?.uid?.let { userId ->
            fetchUserAddresses(userId)
        }
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    startActivity(Intent(this, PlantListActivity::class.java))
                    true
                }
                R.id.menu_cart -> true
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



        buttonPlaceOrder.setOnClickListener {
            if (selectedCardView == null) {
                Toast.makeText(this, "Please select a delivery address", Toast.LENGTH_SHORT).show()
            } else {
                initiatePayment()
            }
        }
    }


    private fun fetchCartItems() {
        currentUser?.uid?.let { userId ->
            db.collection("cart")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { result ->
                    cartList.clear()
                    for (document in result) {
                        val cartItem = document.toObject(CartItem::class.java).apply {
                            id = document.id
                        }
                        cartList.add(cartItem)
                    }
                    cartAdapter.notifyDataSetChanged()
                    updateTotalPrice()
                    checkIfCartIsEmpty()


                    if (cartList.isEmpty()) {
                        emptyCartImageView.visibility = View.VISIBLE
                    } else {
                        emptyCartImageView.visibility = View.GONE
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("CartActivity", "Error fetching cart items: ", exception)
                }
        }
    }


    private fun removeCartItem(cartItem: CartItem) {
        cartItem.id?.let { id ->
            db.collection("cart").document(id)
                .delete()
                .addOnSuccessListener {
                    cartList.remove(cartItem)
                    cartAdapter.notifyDataSetChanged()
                    checkIfCartIsEmpty()
                    Log.d("CartActivity", "Item removed successfully")
                }
                .addOnFailureListener { exception ->
                    Log.e("CartActivity", "Error removing item: ", exception)
                }
        }
    }


    private fun updateTotalPrice() {
        totalPrice = 0.0
        for (item in cartList) {
            totalPrice += item.getTotalPrice()
        }
        totalPriceTextView.text = "Total: ₹${"%.2f".format(totalPrice)}"
    }


    private fun checkIfCartIsEmpty() {
        if (cartList.isEmpty()) {
            emptyCartImageView.visibility = View.VISIBLE
        } else {
            emptyCartImageView.visibility = View.GONE
        }
    }


    private fun fetchUserAddresses(userId: String) {
        db.collection("users").document(userId).collection("addresses")
            .get()
            .addOnSuccessListener { result ->
                addressContainer.removeAllViews()
                for (document in result) {
                    val address = document.toObject(Address::class.java).copy(id = document.id)
                    addAddressCard(address)
                }
            }
            .addOnFailureListener {
                Log.e("CartActivity", "Failed to fetch addresses", it)
            }
    }


    private fun addAddressCard(address: Address) {
        val addressView = layoutInflater.inflate(R.layout.address_card_layout, addressContainer, false)


        val textViewAddress = addressView.findViewById<TextView>(R.id.textViewAddress)
        val buttonDeliverHere = addressView.findViewById<Button>(R.id.buttonDeliverHere)
        val buttonChange = addressView.findViewById<Button>(R.id.buttonChangeAddress)


        textViewAddress.text = "${address.houseNumber}, ${address.StreetName}, ${address.city}, ${address.state} - ${address.postalCode}\nPhone: ${address.phoneNumber}"


        buttonDeliverHere.setOnClickListener {
            for (i in 0 until addressContainer.childCount) {
                val child = addressContainer.getChildAt(i)
                if (child != addressView) {
                    child.visibility = View.GONE
                } else {
                    child.visibility = View.VISIBLE
                }
            }
            buttonDeliverHere.visibility = View.GONE
            buttonChange.visibility = View.VISIBLE
            selectedCardView = addressView
        }


        buttonChange.setOnClickListener {
            for (i in 0 until addressContainer.childCount) {
                val child = addressContainer.getChildAt(i)
                child.visibility = View.VISIBLE


                val deliverHereBtn = child.findViewById<Button>(R.id.buttonDeliverHere)
                val changeBtn = child.findViewById<Button>(R.id.buttonChangeAddress)


                deliverHereBtn.visibility = View.VISIBLE
                changeBtn.visibility = View.GONE
            }
            selectedCardView = null
        }


        addressContainer.addView(addressView)
    }


    private fun initiatePayment() {
        // Convert the total price to paise (100 paise = 1 INR)
        val amountInPaise = (totalPrice * 100).toInt()


        val checkout = Checkout()
        checkout.setKeyID("rzp_test_1EQyeSHCwNoCy5")  // Use your actual Razorpay key here


        try {
            val options = JSONObject()
            options.put("name", "Greenaura")
            options.put("description", "Cart items purchase")
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


    // Razorpay Payment Result Listener methods
    override fun onPaymentSuccess(razorpayPaymentID: String?) {
        // Save the order details to Firebase
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val orderDetails = hashMapOf(
                "cartItems" to cartList.map { it.toMap() },  // Assuming CartItem has a toMap() method
                "totalPrice" to "₹${"%.2f".format(totalPrice)}",
                "address" to selectedCardView?.findViewById<TextView>(R.id.textViewAddress)?.text.toString(),
                "razorpayPaymentID" to razorpayPaymentID
            )


            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("orders")
                .add(orderDetails)
                .addOnSuccessListener {
                    Toast.makeText(this, "Order placed successfully!\nThank you for your purchase!", Toast.LENGTH_LONG).show()
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
