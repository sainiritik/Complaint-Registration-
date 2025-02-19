package com.example.ritik_2

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import com.example.ritik_2.ui.theme.ProfileScreen
import com.example.ritik_2.ui.theme.Ritik_2Theme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ProfileActivity : ComponentActivity() {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private var profileImageUri by mutableStateOf<Uri?>(null)
    private var name by mutableStateOf("Loading...")
    private var email by mutableStateOf("")
    private var phoneNumber by mutableStateOf("")
    private var designation by mutableStateOf("")
    private val userId = firebaseAuth.currentUser?.uid

    // Image picker
    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            profileImageUri = it
            uploadProfilePicture(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loadUserProfile()

        setContent {
            Ritik_2Theme {
                ProfileScreen(
                    profileImageUrl = profileImageUri,
                    name = name,
                    email = email,
                    phoneNumber = phoneNumber,
                    designation = designation,
                    onLogoutClick = { logoutUser() },
                    onEditClick = { field, newValue -> updateUserData(field, newValue) },
                    onChangeProfilePic = { imagePicker.launch("image/*") }
                )
            }
        }
    }

    private fun loadUserProfile() {
        userId?.let { uid ->
            firestore.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        name = document.getString("name") ?: "Unknown"
                        email = document.getString("email") ?: ""
                        phoneNumber = document.getString("phoneNumber") ?: ""
                        designation = document.getString("designation") ?: ""
                        val imageUrl = document.getString("imageUrl")
                        profileImageUri = imageUrl?.let { Uri.parse(it) }
                    } else {
                        showToast("User data not found")
                    }
                }
                .addOnFailureListener {
                    showToast("Failed to load profile data")
                }
        }
    }

    private fun updateUserData(field: String, newValue: String) {
        userId?.let { uid ->
            firestore.collection("users").document(uid).update(field, newValue)
                .addOnSuccessListener {
                    showToast("$field updated!")
                    when (field) {
                        "name" -> name = newValue
                        "email" -> email = newValue
                        "phoneNumber" -> phoneNumber = newValue
                        "designation" -> designation = newValue
                    }
                }
                .addOnFailureListener { showToast("Failed to update $field") }
        }
    }

    private fun uploadProfilePicture(imageUri: Uri) {
        val storageRef = storage.reference.child("users/$userId/profile.jpg")
        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    updateUserProfilePicture(uri)
                }
            }
            .addOnFailureListener { showToast("Failed to upload image") }
    }

    private fun updateUserProfilePicture(photoUrl: Uri) {
        firebaseAuth.currentUser?.updateProfile(userProfileChangeRequest { photoUri = photoUrl })
            ?.addOnSuccessListener {
                firestore.collection("users").document(userId!!).update("imageUrl", photoUrl.toString())
                    .addOnSuccessListener {
                        profileImageUri = photoUrl
                        showToast("Profile picture updated!")
                    }
                    .addOnFailureListener { showToast("Failed to update Firestore image URL") }
            }
    }

    private fun logoutUser() {
        firebaseAuth.signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
