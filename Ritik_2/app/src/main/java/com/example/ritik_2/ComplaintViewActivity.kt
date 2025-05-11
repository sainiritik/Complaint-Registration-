package com.example.ritik_2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.example.ritik_2.ui.theme.ComplaintViewScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class ComplaintViewActivity : ComponentActivity() {
    // Firebase instances
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // Flow for complaints list
    private val _complaints = MutableStateFlow<List<ComplaintWithId>>(emptyList())
    val complaints: StateFlow<List<ComplaintWithId>> = _complaints

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if user is logged in
        val currentUser = auth.currentUser
        if (currentUser == null) {
            // Auto-sign in anonymously
            signInAnonymously()
        } else {
            // Load user's complaints
            loadComplaints()
        }

        setContent {
            MainContent()
        }
    }

    @Composable
    private fun MainContent() {
        val complaintsList by complaints.collectAsState()

        ComplaintViewScreen(
            complaints = complaintsList,
            onDeleteComplaint = { complaintId -> deleteComplaint(complaintId) },
            onUpdateComplaint = { complaintId, newText, newStatus, newUrgency, newCategory ->
                updateComplaint(complaintId, newText, newStatus, newUrgency, newCategory)
            },
            onBackClick = { finish() }
        )
    }

    private fun signInAnonymously() {
        auth.signInAnonymously()
            .addOnSuccessListener {
                loadComplaints()
            }
            .addOnFailureListener { e ->
                // Handle failure
            }
    }

    private fun loadComplaints() {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users")
            .document(userId)
            .collection("complaints")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val complaintsList = snapshot.documents.mapNotNull { doc ->
                        val data = doc.data
                        if (data != null) {
                            ComplaintWithId(
                                id = doc.id,
                                title = data["title"] as? String ?: "",
                                description = data["description"] as? String ?: "",
                                category = data["category"] as? String ?: "",
                                urgency = data["urgency"] as? String ?: "",
                                status = data["status"] as? String ?: "Open",
                                timestamp = data["timestamp"] as? Long ?: 0L,
                                contactInfo = data["contactInfo"] as? String ?: "",
                                hasAttachment = data["hasAttachment"] as? Boolean ?: false
                            )
                        } else {
                            null
                        }
                    }

                    lifecycleScope.launch {
                        _complaints.emit(complaintsList)
                    }
                }
            }
    }

    private fun updateComplaint(
        complaintId: String,
        newText: String,
        newStatus: String,
        newUrgency: String,
        newCategory: String
    ) {
        val userId = auth.currentUser?.uid ?: return

        lifecycleScope.launch {
            try {
                // Update in user's collection
                val updates = hashMapOf<String, Any>(
                    "title" to newText,
                    "status" to newStatus,
                    "urgency" to newUrgency,
                    "category" to newCategory
                )

                firestore.collection("users")
                    .document(userId)
                    .collection("complaints")
                    .document(complaintId)
                    .update(updates)
                    .await()

                // Also update in global collection
                firestore.collection("all_complaints")
                    .document(complaintId)
                    .update(updates)
                    .await()

            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private fun deleteComplaint(complaintId: String) {
        val userId = auth.currentUser?.uid ?: return

        lifecycleScope.launch {
            try {
                // Delete from user's collection
                firestore.collection("users")
                    .document(userId)
                    .collection("complaints")
                    .document(complaintId)
                    .delete()
                    .await()

                // Delete from global collection
                firestore.collection("all_complaints")
                    .document(complaintId)
                    .delete()
                    .await()

            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}