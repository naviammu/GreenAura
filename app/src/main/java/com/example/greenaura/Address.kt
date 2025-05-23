package com.example.greenaura

data class Address(
    var phoneNumber: String = "",   // Default values for all fields
    var houseNumber: String = "",
    var StreetName: String = "",
    var city: String = "",
    var state: String = "",
    var postalCode: String = "",
    var id: String = ""             // Firestore document ID, if applicable
) {
    // No-argument constructor for Firebase
    constructor() : this("", "", "", "", "", "", "")
}
