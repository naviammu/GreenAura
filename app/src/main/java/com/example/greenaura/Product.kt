package com.example.greenaura

import android.os.Parcel
import android.os.Parcelable

data class Product(
    val id: String = "",
    val productName: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val description: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",           // id
        parcel.readString() ?: "",           // productName
        parcel.readDouble(),                 // price
        parcel.readString() ?: "",           // imageUrl
        parcel.readString() ?: ""            // description
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(productName)
        parcel.writeDouble(price)
        parcel.writeString(imageUrl)
        parcel.writeString(description)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Product> {
        override fun createFromParcel(parcel: Parcel): Product {
            return Product(parcel)
        }

        override fun newArray(size: Int): Array<Product?> {
            return arrayOfNulls(size)
        }
    }
}
