package com.example.greenaura

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AccountActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var logoutButton: Button
    private lateinit var addAddressButton: Button
    private lateinit var addressAdapter: AddressAdapter
    private val addressList = mutableListOf<Address>()
    private lateinit var firestore: FirebaseFirestore
    private lateinit var userNameTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.account_activity)

        initFirebase()
        setUpUI()
        setUpRecyclerView()
        loadAddressesFromFirestore()

        // Bottom Navigation
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    startActivity(Intent(this, PlantListActivity::class.java))
                    true
                }
                R.id.menu_cart -> {
                    startActivity(Intent(this, CartActivity::class.java))
                    true
                }
                R.id.menu_account -> {
                    // Already in AccountActivity, no action needed
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

    private fun initFirebase() {
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
    }

    private fun setUpUI() {
        userNameTextView = findViewById(R.id.textViewUserName)
        val userEmailTextView = findViewById<TextView>(R.id.textViewUserEmail)
        val currentUser = auth.currentUser

        currentUser?.let {
            userNameTextView.text = it.displayName ?: "No Name Provided"
            userEmailTextView.text = it.email ?: "No Email Provided"
        }

        // Logout Button
        logoutButton = findViewById(R.id.logout_button)
        logoutButton.setOnClickListener {
            handleLogout()
        }

        // Add Address Button
        addAddressButton = findViewById(R.id.buttonAddAddress)
        addAddressButton.setOnClickListener {
            showAddAddressDialog() // Open dialog to add new address
        }

        val privacySettingsButton = findViewById<Button>(R.id.buttonPrivacySettings)
        privacySettingsButton.setOnClickListener {
            startActivity(Intent(this, PrivacySettingsActivity::class.java))
        }

    }

    private fun setUpRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewAddresses)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Instantiate the AddressAdapter with edit and delete callbacks
        addressAdapter = AddressAdapter(
            addressList,
            onAddressEdit = { address -> showEditAddressDialog(address) },
            onAddressDelete = { address -> deleteAddress(address) }
        )
        recyclerView.adapter = addressAdapter
    }

    private fun handleLogout() {
        // Sign out the user from Firebase
        auth.signOut()

        // Set the flag so that account chooser doesn't show again
        val prefs = getSharedPreferences("greenaura_prefs", MODE_PRIVATE)
        prefs.edit().putBoolean("show_chooser", false).apply()

        // Show logout success toast
        Toast.makeText(this, "Logged out successfully!", Toast.LENGTH_SHORT).show()

        // Redirect to LoginActivity and clear the back stack
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }


    private fun loadAddressesFromFirestore() {
        val currentUser = auth.currentUser
        currentUser?.let {
            val userId = it.uid

            firestore.collection("users").document(userId).collection("addresses")
                .get()
                .addOnSuccessListener { documents ->
                    addressList.clear()
                    for (document in documents) {
                        val address = document.toObject(Address::class.java).copy(id = document.id) // Assign Firestore document ID
                        if (isValidAddress(address)) {
                            addressList.add(address)
                        }
                    }
                    addressAdapter.notifyDataSetChanged()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to load addresses.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun isValidAddress(address: Address): Boolean {
        return address.phoneNumber.isNotEmpty() &&
                address.houseNumber.isNotEmpty() &&
                address.StreetName.isNotEmpty() &&
                address.city.isNotEmpty() &&
                address.state.isNotEmpty() &&
                address.postalCode.isNotEmpty()
    }

    // Show dialog to add new address
    private fun showAddAddressDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_address, null)

        val phoneNumberField = dialogView.findViewById<TextView>(R.id.editTextPhoneNumber)
        val houseNumberField = dialogView.findViewById<TextView>(R.id.editTextHouseNo)
        val streetNameField = dialogView.findViewById<TextView>(R.id.editTextStreetName)
        val cityField = dialogView.findViewById<TextView>(R.id.editTextCity)
        val stateField = dialogView.findViewById<TextView>(R.id.editTextState)
        val postalCodeField = dialogView.findViewById<TextView>(R.id.editTextPincode)

        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Add Address")
            .setPositiveButton("Save") { dialog, _ ->
                val phoneNumber = phoneNumberField.text.toString().trim()
                val houseNumber = houseNumberField.text.toString().trim()
                val streetName = streetNameField.text.toString().trim()
                val city = cityField.text.toString().trim()
                val state = stateField.text.toString().trim()
                val postalCode = postalCodeField.text.toString().trim()

                if (isInputValid(phoneNumber, houseNumber, streetName, city, state, postalCode)) {
                    saveAddressToFirestore(phoneNumber, houseNumber, streetName, city, state, postalCode)
                    dialog.dismiss()
                } else {
                    Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        dialogBuilder.create().show()
    }

    // Input validation
    private fun isInputValid(vararg inputs: String): Boolean {
        return inputs.all { it.isNotEmpty() }
    }

    // Save new address to Firestore
    private fun saveAddressToFirestore(
        phoneNumber: String, houseNumber: String, streetName: String,
        city: String, state: String, postalCode: String
    ) {
        val currentUser = auth.currentUser
        currentUser?.let {
            val userId = it.uid
            val addressData = hashMapOf(
                "phoneNumber" to phoneNumber,
                "houseNumber" to houseNumber,
                "StreetName" to streetName,
                "city" to city,
                "state" to state,
                "postalCode" to postalCode
            )

            firestore.collection("users").document(userId).collection("addresses")
                .add(addressData)
                .addOnSuccessListener { documentReference ->
                    addressList.add(
                        Address(phoneNumber, houseNumber, streetName, city, state, postalCode, documentReference.id)
                    )
                    addressAdapter.notifyDataSetChanged()
                    Toast.makeText(this, "Address added successfully!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to add address.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Show edit address dialog
    private fun showEditAddressDialog(address: Address) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_address, null)

        val phoneNumberField = dialogView.findViewById<TextView>(R.id.editTextPhoneNumber)
        val houseNumberField = dialogView.findViewById<TextView>(R.id.editTextHouseNo)
        val streetNameField = dialogView.findViewById<TextView>(R.id.editTextStreetName)
        val cityField = dialogView.findViewById<TextView>(R.id.editTextCity)
        val stateField = dialogView.findViewById<TextView>(R.id.editTextState)
        val postalCodeField = dialogView.findViewById<TextView>(R.id.editTextPincode)

        phoneNumberField.text = address.phoneNumber
        houseNumberField.text = address.houseNumber
        streetNameField.text = address.StreetName
        cityField.text = address.city
        stateField.text = address.state
        postalCodeField.text = address.postalCode

        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Edit Address")
            .setPositiveButton("Save") { dialog, _ ->
                val updatedPhoneNumber = phoneNumberField.text.toString().trim()
                val updatedHouseNumber = houseNumberField.text.toString().trim()
                val updatedStreetName = streetNameField.text.toString().trim()
                val updatedCity = cityField.text.toString().trim()
                val updatedState = stateField.text.toString().trim()
                val updatedPostalCode = postalCodeField.text.toString().trim()

                if (isInputValid(updatedPhoneNumber, updatedHouseNumber, updatedStreetName, updatedCity, updatedState, updatedPostalCode)) {
                    updateAddressInFirestore(address, updatedPhoneNumber, updatedHouseNumber, updatedStreetName, updatedCity, updatedState, updatedPostalCode)
                    dialog.dismiss()
                } else {
                    Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        dialogBuilder.create().show()
    }

    // Update address in Firestore
    private fun updateAddressInFirestore(
        address: Address, phoneNumber: String, houseNumber: String, streetName: String,
        city: String, state: String, postalCode: String
    ) {
        val currentUser = auth.currentUser
        currentUser?.let {
            val userId = it.uid

            val updatedAddressData = hashMapOf(
                "phoneNumber" to phoneNumber,
                "houseNumber" to houseNumber,
                "StreetName" to streetName,
                "city" to city,
                "state" to state,
                "postalCode" to postalCode
            )

            firestore.collection("users").document(userId).collection("addresses").document(address.id)
                .set(updatedAddressData)
                .addOnSuccessListener {
                    address.phoneNumber = phoneNumber
                    address.houseNumber = houseNumber
                    address.StreetName = streetName
                    address.city = city
                    address.state = state
                    address.postalCode = postalCode

                    addressAdapter.notifyDataSetChanged()
                    Toast.makeText(this, "Address updated successfully!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to update address.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Delete address from Firestore
    private fun deleteAddress(address: Address) {
        val currentUser = auth.currentUser
        currentUser?.let {
            val userId = it.uid

            firestore.collection("users").document(userId).collection("addresses").document(address.id)
                .delete()
                .addOnSuccessListener {
                    addressList.remove(address)
                    addressAdapter.notifyDataSetChanged()
                    Toast.makeText(this, "Address deleted successfully!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to delete address.", Toast.LENGTH_SHORT).show()
                }
        }
    }

}
