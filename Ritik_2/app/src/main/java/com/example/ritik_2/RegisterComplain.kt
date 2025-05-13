package com.example.ritik_2

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ritik_2.modules.ComplaintWithId
import com.example.ritik_2.ui.theme.RegisterComplaintScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

// Data class for complaint
class RegisterComplain : ComponentActivity() {
    // Firebase instances
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    // File picker for attachments
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { selectedUri ->
            // Handle the selected file
            selectedFileUri = selectedUri
            // You could upload it to Firebase Storage here or store the URI for later use
        }
    }

    // State for selected file
    private var selectedFileUri: Uri? = null

    // Flow for complaints list
    private val _complaints = MutableStateFlow<List<ComplaintWithId>>(emptyList())
    val complaints: StateFlow<List<ComplaintWithId>> = _complaints

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if user is logged in
        val currentUser = auth.currentUser
        if (currentUser == null) {
            // You might want to redirect to login screen
            // For this example, we'll auto-sign in anonymously
            signInAnonymously()
        } else {
            // Load user's complaints
            loadComplaints()
        }

        setContent {
            ComplaintAppTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AppNavigation()
                }
            }
        }
    }

    private fun signInAnonymously() {
        auth.signInAnonymously()
            .addOnSuccessListener {
                Toast.makeText(this, "Signed in anonymously", Toast.LENGTH_SHORT).show()
                loadComplaints()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Authentication failed: ${e.message}", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this, "Error loading complaints: ${e.message}", Toast.LENGTH_SHORT).show()
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

    private fun saveComplaint(complaintData: ComplaintData) {
        val user = auth.currentUser

        if (user == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
            return
        }

        if (complaintData.title.isBlank() || complaintData.description.isBlank()) {
            Toast.makeText(this, "Please fill out required fields", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                // Create a unique ID for potential file attachment
                val complaintId = UUID.randomUUID().toString()

                // Upload attachment if exists
                var attachmentUrl: String? = null
                if (complaintData.hasAttachment && selectedFileUri != null) {
                    val storageRef = storage.reference
                        .child("users")
                        .child(user.uid)
                        .child("attachments")
                        .child(complaintId)

                    attachmentUrl = try {
                        storageRef.putFile(selectedFileUri!!).await()
                        storageRef.downloadUrl.await().toString()
                    } catch (e: Exception) {
                        null
                    }
                }

                // Create complaint data map
                val data = hashMapOf(
                    "title" to complaintData.title,
                    "description" to complaintData.description,
                    "category" to complaintData.category,
                    "urgency" to complaintData.urgency,
                    "status" to "Open",
                    "timestamp" to System.currentTimeMillis(),
                    "userId" to user.uid,
                    "userEmail" to (user.email ?: "Anonymous"),
                    "contactInfo" to complaintData.contactInfo,
                    "hasAttachment" to complaintData.hasAttachment,
                    "attachmentUrl" to attachmentUrl
                )

                // Save to Firestore
                firestore.collection("users")
                    .document(user.uid)
                    .collection("complaints")
                    .document(complaintId)
                    .set(data)
                    .await()

                // Also save to a global collection for admin access
                firestore.collection("all_complaints")
                    .document(complaintId)
                    .set(data)
                    .await()

                // Reset selected file
                selectedFileUri = null

                // Show success message
                runOnUiThread {
                    Toast.makeText(this@RegisterComplain, "Complaint submitted successfully!", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this@RegisterComplain,
                        "Error saving complaint: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun openFilePicker() {
        getContent.launch("*/*")
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

                runOnUiThread {
                    Toast.makeText(this@RegisterComplain, "Complaint deleted", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this@RegisterComplain,
                        "Error deleting complaint: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    @Composable
    fun AppNavigation() {
        val navController = rememberNavController()

        NavHost(navController = navController, startDestination = "complaint_form") {
            composable("complaint_form") {
                RegisterComplaintScreen(
                    onSaveClick = { complaintData ->
                        // Pass the complaint data to the saveComplaint function
                        saveComplaint(complaintData)
                    },
                    onResetClick = {
                        // Reset any state if needed
                        selectedFileUri = null
                    },
                    onViewComplaintsClick = {
                        navController.navigate("view_complaints")
                    }
                )
            }

//            composable("view_complaints") {
//                ViewComplaintsScreen(
//                    complaints = complaints,
//                    onBackClick = {
//                        navController.navigateUp()
//                    },
//                    onDeleteClick = { complaintId ->
//                        deleteComplaint(complaintId)
//                    }
//                )
//            }
        }
    }

    @Composable
    fun ComplaintAppTheme(content: @Composable () -> Unit) {
        val isDark = isSystemInDarkTheme()

        val colors = if (isDark) {
            darkColorScheme(
                primary = Color(0xFF6200EE),
                secondary = Color(0xFF03DAC5),
                surface = Color(0xFF121212)
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF6200EE),
                secondary = Color(0xFF03DAC5),
                surface = Color(0xFFFAFAFA)
            )
        }

        MaterialTheme(
            colorScheme = colors,
            content = content
        )
    }
}

// Data classes
data class ComplaintData(
    val title: String,
    val description: String,
    val category: String,
    val urgency: String,
    val contactInfo: String = "",
    val hasAttachment: Boolean = false
)
