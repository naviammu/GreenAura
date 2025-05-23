package com.example.greenaura

import com.google.firebase.firestore.PropertyName
import java.io.Serializable

data class Plant(
    @get:PropertyName("Temperature")
    @set:PropertyName("Temperature")
    var Temperature: String = "",

    @get:PropertyName("PH")
    @set:PropertyName("PH")
    var PH: String = "",

    @get:PropertyName("Soil")
    @set:PropertyName("Soil")
    var Soil: String = "",

    @get:PropertyName("Waterlevel")
    @set:PropertyName("Waterlevel")
    var Waterlevel: String = "",

    @get:PropertyName("Space  ") // ← exactly as in your Firestore JSON
    @set:PropertyName("Space  ")
    var Space_: String = "",

    @get:PropertyName("Label")
    @set:PropertyName("Label")
    var Label: String = "",

    @get:PropertyName("common name")
    @set:PropertyName("common name")
    var common_name: String = "",

    @get:PropertyName("Image Src")
    @set:PropertyName("Image Src")
    var Image_Src: String = "",

    @get:PropertyName("Description")
    @set:PropertyName("Description")
    var Description: String = ""
) : Serializable
