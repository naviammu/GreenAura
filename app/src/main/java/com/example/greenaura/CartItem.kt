package com.example.greenaura


data class CartItem(
    var id: String? = null,  // Firestore document ID for identifying the item
    var productName: String = "",
    var price: Double = 0.0,
    var quantity: Int = 0,
    var imageUrl: String = ""
) {
    // Function to calculate the total price for the quantity of the product
    fun getTotalPrice(): Double {
        return price * quantity
    }


    // Function to convert the CartItem into a Map for saving to Firestore
    fun toMap(): Map<String, Any> {
        return mapOf(
            "productName" to productName,
            "price" to price,
            "quantity" to quantity,
            "imageUrl" to imageUrl
        )
    }
}
