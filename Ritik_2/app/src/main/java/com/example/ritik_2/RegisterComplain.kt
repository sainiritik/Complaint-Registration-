package com.example.ritik_2

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.ritik_2.ui.theme.RegisterComplainScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterComplain : ComponentActivity() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RegisterComplainScreen(
                onSaveClick = { complainText, urgency, category ->
                    saveComplaint(complainText, urgency, category)
                },
                onResetClick = {
                    // You can add additional reset logic here if needed
                }
            )
        }
    }

    private fun saveComplaint(complainText: String, urgency: String, category: String) {
        val user = auth.currentUser

        if (user == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
            return
        }

        if (complainText.isBlank()) {
            Toast.makeText(this, "Please enter a complaint before saving.", Toast.LENGTH_SHORT).show()
            return
        }

        val data = hashMapOf(
            "complainText" to complainText,
            "urgency" to urgency,
            "category" to category,
            "status" to "Open",
            "timestamp" to System.currentTimeMillis(),
            "userId" to user.uid,
            "userEmail" to (user.email ?: "Unknown")
        )

        firestore.collection("users").document(user.uid)
            .collection("complaints")
            .add(data)
            .addOnSuccessListener {
                Toast.makeText(this, "Complaint submitted successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error saving complaint: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}