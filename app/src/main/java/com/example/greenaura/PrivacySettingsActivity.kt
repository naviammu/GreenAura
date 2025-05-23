package com.example.greenaura

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.EmailAuthProvider

class PrivacySettingsActivity : AppCompatActivity() {

    private lateinit var deleteAccountButton: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_settings)

        deleteAccountButton = findViewById(R.id.buttonDeleteAccount)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Configure Google Sign-In with requestIdToken
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // ✅ Required!
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        deleteAccountButton.setOnClickListener {
            showDeleteConfirmation()
        }
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to permanently delete your account?")
            .setPositiveButton("Yes") { _, _ -> reauthenticateAndDeleteAccount() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun reauthenticateAndDeleteAccount() {
        val user = auth.currentUser
        user?.let { currentUser ->
            val googleAccount = GoogleSignIn.getLastSignedInAccount(this)
            if (googleAccount != null && currentUser.providerData.any { it.providerId == GoogleAuthProvider.PROVIDER_ID }) {
                reauthenticateGoogleUser(currentUser)
            } else {
                showEmailPasswordDialog(currentUser)
            }
        } ?: showToast("User not found")
    }

    private fun reauthenticateGoogleUser(currentUser: FirebaseUser) {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, 1001)
    }

    private fun showEmailPasswordDialog(currentUser: FirebaseUser) {
        val input = EditText(this).apply {
            hint = "Enter your password"
        }

        AlertDialog.Builder(this)
            .setTitle("Reauthenticate")
            .setMessage("Please enter your password to continue.")
            .setView(input)
            .setPositiveButton("Confirm") { _, _ ->
                val email = currentUser.email ?: return@setPositiveButton
                val password = input.text.toString()

                if (password.isEmpty()) {
                    showToast("Password cannot be empty")
                    return@setPositiveButton
                }

                val credential = EmailAuthProvider.getCredential(email, password)
                currentUser.reauthenticate(credential)
                    .addOnSuccessListener {
                        deleteUserFromFirestoreAndAuth(currentUser.uid)
                    }
                    .addOnFailureListener {
                        showToast("Incorrect password")
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (idToken != null) {
                    val credential = GoogleAuthProvider.getCredential(idToken, null)
                    val user = auth.currentUser
                    user?.reauthenticate(credential)
                        ?.addOnSuccessListener {
                            deleteUserFromFirestoreAndAuth(user.uid)
                        }
                        ?.addOnFailureListener {
                            showToast("Google reauthentication failed")
                            Log.e("PrivacySettings", "Google reauth failed", it)
                        }
                } else {
                    showToast("ID Token is null")
                }
            } catch (e: ApiException) {
                showToast("Google sign-in failed")
                Log.e("PrivacySettings", "Google sign-in error", e)
            }
        }
    }

    private fun deleteUserFromFirestoreAndAuth(userId: String) {
        firestore.collection("users").document(userId).delete()
            .addOnSuccessListener {
                auth.currentUser?.delete()
                    ?.addOnSuccessListener {
                        // 👇 Set to true so chooser shows up again
                        val prefs = getSharedPreferences("greenaura_prefs", MODE_PRIVATE)
                        prefs.edit().putBoolean("show_chooser", true).apply()

                        showToast("Account deleted")
                        navigateToLogin()
                    }
                    ?.addOnFailureListener {
                        showToast("Failed to delete authentication")
                    }
            }
            .addOnFailureListener {
                showToast("Failed to delete Firestore data")
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
