plugins {
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
    id("com.android.application")
}

android {
    namespace = "com.example.greenaura"
    compileSdk = 33 // Set compileSdk to 33

    defaultConfig {
        applicationId = "com.example.greenaura"
        minSdk = 24
        targetSdk = 33 // Set targetSdk to 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
        languageVersion = "1.8"
    }
}

dependencies {
    // Core AndroidX libraries
    implementation("androidx.core:core-ktx:1.10.1") // Latest core library compatible with API 33
    implementation("androidx.appcompat:appcompat:1.6.1")

    // Material Components compatible with API 33
    implementation("com.google.android.material:material:1.8.0")

    // Kotlin Android Extensions
    implementation("androidx.activity:activity-ktx:1.7.2")

    // Firebase BoM - Manages Firebase versions automatically
    implementation(platform("com.google.firebase:firebase-bom:31.5.0")) // Updated Firebase BoM for API 33

    // Firestore SDK
    implementation("com.google.firebase:firebase-firestore")

    // Firebase Analytics (excluding ads-adservices for API 33)
    implementation("com.google.firebase:firebase-analytics") {
        exclude(group = "androidx.privacysandbox.ads", module = "ads-adservices")
    }

    // Firebase Storage SDK
    implementation("com.google.firebase:firebase-storage")

    // Firebase Authentication
    implementation("com.google.firebase:firebase-auth-ktx:21.1.0")

    // Firebase Firestore with Kotlin extensions
    implementation("com.google.firebase:firebase-firestore-ktx:24.3.0")

    // Firebase Storage with Kotlin extensions
    implementation("com.google.firebase:firebase-storage-ktx:20.1.0")

    // Test dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Kotlin Standard Library
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.8.20")
    //implementation ("org.apache.santuario:xmlsec:2.1.7")
    //implementation ("javax.xml.crypto:xmlsec:2.1.7") // Add this for javax.crypto classes support

    implementation ("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.15.1")

    implementation ("com.google.android.gms:play-services-auth:20.5.0")
    implementation ("com.google.firebase:firebase-auth")

    // RecyclerView library
    implementation ("androidx.recyclerview:recyclerview:1.2.1")

    implementation ("com.squareup.picasso:picasso:2.71828")

    implementation ("androidx.cardview:cardview:1.0.0")

    implementation ("com.google.android.gms:play-services-base:18.3.0")

    //razorpay sdk
    implementation ("com.razorpay:checkout:1.6.8")
//for weather api
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.okhttp3:okhttp:4.10.0")
    implementation ("com.google.code.gson:gson:2.10.1")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")

}
