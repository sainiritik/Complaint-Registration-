package com.example.ritik_2

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.ritik_2.ui.theme.MainScreen
import com.example.ritik_2.ui.theme.Ritik_2Theme
import com.example.ritik_2.ui.theme.ViewComplaintsActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: MainViewModel
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        // ✅ Redirect to Login if not logged in
        if (firebaseAuth.currentUser == null) {
            navigateToLogin()
            return
        }

        setContent {
            Ritik_2Theme {
                MainScreen(
                    onLogout = {
                        firebaseAuth.signOut()
                        navigateToLogin()
                    },
                    onCardClick = { cardId -> handleCardClick(cardId) }
                )
            }
        }
    }

    // Fetch User Details from Firestore
    private fun getUserDetailsFromFirestore(userId: String, onSuccess: (String, String?) -> Unit, onFailure: (Exception) -> Unit) {
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val userName = document.getString("name") ?: "Unknown User"
                    val userImageUrl = document.getString("imageUrl")
                    onSuccess(userName, userImageUrl)
                } else {
                    onFailure(Exception("User not found"))
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    private fun handleCardClick(cardId: Int) {
        when (cardId) {
            1 -> startActivity(Intent(this, RegisterComplain::class.java))
            2 -> startActivity(Intent(this, ViewComplaintsActivity::class.java))
            // Optionally uncomment the below if you wish to handle the profile card click
            3 -> startActivity(Intent(this, MainActivityTest::class.java))
        }
    }

    // ✅ Redirect to Login
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun navigateToProfile() {
        startActivity(Intent(this, ProfileActivity::class.java))
    }
}
