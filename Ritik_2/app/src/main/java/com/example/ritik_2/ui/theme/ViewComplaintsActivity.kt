package com.example.ritik_2.ui.theme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay

data class Complaint(
    val id: String = "",
    val complainText: String = "",
    val urgency: String = "⚪ Normal",
    val status: String = "Open",
    val timestamp: Long = 0L
)

class ViewComplaintsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ComplaintsListScreen() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplaintsListScreen() {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    var complaints by remember { mutableStateOf<List<Complaint>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        auth.currentUser?.let { user ->
            firestore.collection("users").document(user.uid).collection("complaints")
                .get()
                .addOnSuccessListener { result ->
                    complaints = result.documents.mapNotNull { doc ->
                        doc.toObject(Complaint::class.java)?.copy(id = doc.id)
                    }.sortedByDescending { it.timestamp }
                    isLoading = false
                }
                .addOnFailureListener { isLoading = false }
        } ?: run { isLoading = false }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📌 My Complaints", fontSize = 22.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1976D2), titleContentColor = Color.White)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.Blue)
            } else if (complaints.isEmpty()) {
                Text("No complaints found!", fontSize = 18.sp)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(complaints) { complaint ->
                        ComplaintCard(complaint, firestore, auth.currentUser?.uid ?: "", complaints) { newList ->
                            complaints = newList
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ComplaintCard(complaint: Complaint, firestore: FirebaseFirestore, userId: String, complaints: List<Complaint>, onComplaintsChange: (List<Complaint>) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = !isDeleting,
        exit = scaleOut(targetScale = 0.1f) + fadeOut(animationSpec = tween(600))
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(complaint.complainText, fontSize = 18.sp)
                Text("Urgency: ${complaint.urgency}", fontSize = 16.sp)
                Text("Status: ${complaint.status}", fontSize = 16.sp, color = Color(0xFF1976D2))

                Spacer(modifier = Modifier.height(8.dp))

                Row {
                    Button(onClick = { showDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                        Text("Update")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        isDeleting = true
                        deleteComplaint(firestore, userId, complaint.id) {
                            onComplaintsChange(complaints - complaint)
                        }
                    }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                        Text("Delete")
                    }
                }
            }
        }
    }

    if (showDialog) {
        ComplaintUpdateDialog(firestore, userId, complaint) { showDialog = false }
    }
}

@Composable
fun ComplaintUpdateDialog(firestore: FirebaseFirestore, userId: String, complaint: Complaint, onDismiss: () -> Unit) {
    var showSuccessAnimation by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Complaint") },
        text = {
            if (showSuccessAnimation) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Updated", tint = Color.Green, modifier = Modifier.size(50.dp))
            } else {
                Column {
                    Text("Change Status:", fontSize = 16.sp)
                    Button(onClick = {
                        updateComplaintStatus(firestore, userId, complaint.id, "Resolved") {
                            showSuccessAnimation = true
                        }
                    }) {
                        Text("Mark as Resolved")
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Close") }
        }
    )
}

fun updateComplaintStatus(firestore: FirebaseFirestore, userId: String, complaintId: String, newStatus: String, onComplete: () -> Unit) {
    firestore.collection("users").document(userId).collection("complaints")
        .document(complaintId)
        .update("status", newStatus)
        .addOnSuccessListener { onComplete() }
}

fun deleteComplaint(firestore: FirebaseFirestore, userId: String, complaintId: String, onComplete: () -> Unit) {
    firestore.collection("users").document(userId).collection("complaints")
        .document(complaintId)
        .delete()
        .addOnSuccessListener { onComplete() }
}
