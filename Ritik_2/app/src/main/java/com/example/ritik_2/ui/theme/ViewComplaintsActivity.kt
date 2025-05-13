//package com.example.ritik_2.ui.theme
//
//import android.os.Bundle
//import android.text.format.DateFormat
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.compose.animation.*
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.gestures.detectHorizontalDragGestures
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.text.KeyboardActions
//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.input.pointer.pointerInput
//import androidx.compose.ui.res.colorResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.wear.compose.material.ChipDefaults
//import com.example.ritik_2.R
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//import java.util.Date
//
//
//// Complaint Data Class
//data class Complaint(
//    val id: String = "",
//    val complainText: String = "",
//    val urgency: String = "Normal",
//    val status: String = "Open",
//    val category: String = "General",
//    val timestamp: Long = 0L
//)
//
//class ViewComplaintsActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent { ComplaintsListScreen() }
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun ComplaintsListScreen() {
//    val auth = FirebaseAuth.getInstance()
//    val firestore = FirebaseFirestore.getInstance()
//    var complaints by remember { mutableStateOf<List<Complaint>>(emptyList()) }
//    var isLoading by remember { mutableStateOf(true) }
//
//    val black = colorResource(id = R.color.black)
//    val grayMuted = colorResource(id = R.color.gray_muted)
//    val offWhite = colorResource(id = R.color.off_white)
//    val warmBeige = colorResource(id = R.color.warm_beige)
//    val softTan = colorResource(id = R.color.soft_tan)
//    val warmGold = colorResource(id = R.color.warm_gold)
//    val brightOrange = colorResource(id = R.color.bright_orange)
//    val deepOrange = colorResource(id = R.color.deep_orange)
//    val boldRed = colorResource(id = R.color.bold_red)
//    val emerald = colorResource(id = R.color.emerald)
//    val platinum = colorResource(id = R.color.platinum)
//    val bitterSweet = colorResource(id = R.color.bittersweet)
//    val aero = colorResource(id = R.color.aero)
//    val snow = colorResource(id = R.color.snow)
//
//
//    fun loadComplaints() {
//        isLoading = true
//        auth.currentUser?.let { user ->
//            firestore.collection("users").document(user.uid).collection("complaints")
//                .get()
//                .addOnSuccessListener { result ->
//                    complaints = result.documents.mapNotNull { doc ->
//                        doc.toObject(Complaint::class.java)?.copy(id = doc.id)
//                    }.sortedByDescending { it.timestamp }
//                    isLoading = false
//                }
//                .addOnFailureListener { isLoading = false }
//        } ?: run { isLoading = false }
//    }
//
//    LaunchedEffect(Unit) {
//        loadComplaints()
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("📌 My Complaints", fontSize = 22.sp) },
//                colors = TopAppBarDefaults.topAppBarColors(containerColor = platinum, titleContentColor = bitterSweet)
//            )
//        }
//    ) { paddingValues ->
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(snow)
//                .padding(paddingValues)
//                .padding(16.dp),
//            contentAlignment = Alignment.Center
//        ) {
//            if (isLoading) {
//                CircularProgressIndicator(color = emerald)
//            } else if (complaints.isEmpty()) {
//                Text("No complaints found!", fontSize = 18.sp, color = black)
//            } else {
//                LazyColumn(
//                    verticalArrangement = Arrangement.spacedBy(10.dp),
//                    modifier = Modifier.fillMaxSize()
//                ) {
//                    items(complaints) { complaint ->
//                        ComplaintCard(complaint, firestore, auth.currentUser?.uid ?: "", onComplaintsChange = { loadComplaints() })
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun ComplaintCard(
//    complaint: Complaint,
//    firestore: FirebaseFirestore,
//    userId: String,
//    onComplaintsChange: () -> Unit
//) {
//    var showUpdateDialog by remember { mutableStateOf(false) }
//    var showDeleteDialog by remember { mutableStateOf(false) }
//    val formattedDate = DateFormat.format("dd/MM/yyyy HH:mm", Date(complaint.timestamp)).toString()
//    val black = colorResource(id = R.color.black)
//    val grayMuted = colorResource(id = R.color.gray_muted)
//    val offWhite = colorResource(id = R.color.off_white)
//    val warmBeige = colorResource(id = R.color.warm_beige)
//    val softTan = colorResource(id = R.color.soft_tan)
//    val warmGold = colorResource(id = R.color.warm_gold)
//    val brightOrange = colorResource(id = R.color.bright_orange)
//    val deepOrange = colorResource(id = R.color.deep_orange)
//    val boldRed = colorResource(id = R.color.bold_red)
//    val emerald = colorResource(id = R.color.emerald)
//    val platinum = colorResource(id = R.color.platinum)
//    val bitterSweet = colorResource(id = R.color.bittersweet)
//    val aero = colorResource(id = R.color.aero)
//    val snow = colorResource(id = R.color.snow)
//
//    Box(
//        modifier = Modifier
//            .fillMaxWidth()
//            .pointerInput(Unit) {
//                detectHorizontalDragGestures { change, dragAmount ->
//                    change.consume()
//                    if (dragAmount < -100) { // Swipe left
//                        showDeleteDialog = true
//                    }
//                }
//            }
//    ) {
//        Card(
//            modifier = Modifier.fillMaxWidth().clickable { showUpdateDialog = true },
//            colors = CardDefaults.cardColors(containerColor = offWhite),
//            elevation = CardDefaults.cardElevation(6.dp)
//        ) {
//            Column(modifier = Modifier.padding(16.dp)) {
//                Text(complaint.complainText, fontSize = 18.sp, color = aero, fontWeight = FontWeight.Bold)
//                Text("Date: $formattedDate", fontSize = 14.sp, color = black)
//                Text("Category: ${complaint.category}", fontSize = 14.sp, color = black)
//                Text("Urgency: ${complaint.urgency}", fontSize = 14.sp, color = black)
//                Text("Status: ${complaint.status}", fontSize = 14.sp, color = black)
//            }
//        }
//    }
//
//    if (showUpdateDialog) {
//        UpdateComplaintDialog(firestore, userId, complaint) {
//            showUpdateDialog = false
//            onComplaintsChange()
//        }
//    }
//
//    if (showDeleteDialog) {
//        DeleteComplaintDialog(firestore, userId, complaint, onDismiss = { showDeleteDialog = false }, onDelete = {
//            onComplaintsChange()
//            showDeleteDialog = false
//        })
//    }
//}
//
//@Composable
//fun DeleteComplaintDialog(
//    firestore: FirebaseFirestore,
//    userId: String,
//    complaint: Complaint,
//    onDismiss: () -> Unit,
//    onDelete: () -> Unit
//) {
//    AlertDialog(
//        onDismissRequest = onDismiss,
//        title = { Text("Delete Complaint", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
//        text = {
//            Column {
//                Text("Are you sure you want to delete this complaint?", fontSize = 16.sp)
//                Spacer(modifier = Modifier.height(8.dp))
//                Text("📝 ${complaint.complainText}", fontSize = 14.sp, color = Color.Gray)
//                Text("Status: ${complaint.status}", fontSize = 14.sp, color = Color(0xFF1976D2))
//                Text("Urgency: ${complaint.urgency}", fontSize = 14.sp, color = Color.Red)
//            }
//        },
//        confirmButton = {
//            Button(
//                onClick = {
//                    deleteComplaint(firestore, userId, complaint.id, onDelete)
//                },
//                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
//            ) {
//                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
//                Spacer(modifier = Modifier.width(4.dp))
//                Text("Delete", color = Color.White)
//            }
//        },
//        dismissButton = {
//            OutlinedButton(onClick = onDismiss) {
//                Icon(Icons.Default.Close, contentDescription = "Cancel")
//                Spacer(modifier = Modifier.width(4.dp))
//                Text("Cancel")
//            }
//        }
//    )
//}
//
//
//@Composable
//fun UpdateComplaintDialog(
//    firestore: FirebaseFirestore,
//    userId: String,
//    complaint: Complaint,
//    onDismiss: () -> Unit
//) {
//    var newText by remember { mutableStateOf(complaint.complainText) }
//    var newUrgency by remember { mutableStateOf(complaint.urgency) }
//    var newStatus by remember { mutableStateOf(complaint.status) }
//    val black = colorResource(id = R.color.black)
//    val grayMuted = colorResource(id = R.color.gray_muted)
//    val offWhite = colorResource(id = R.color.off_white)
//    val warmBeige = colorResource(id = R.color.warm_beige)
//    val softTan = colorResource(id = R.color.soft_tan)
//    val warmGold = colorResource(id = R.color.warm_gold)
//    val brightOrange = colorResource(id = R.color.bright_orange)
//    val deepOrange = colorResource(id = R.color.deep_orange)
//    val boldRed = colorResource(id = R.color.bold_red)
//    val emerald = colorResource(id = R.color.emerald)
//    val platinum = colorResource(id = R.color.platinum)
//    val bitterSweet = colorResource(id = R.color.bittersweet)
//    val aero = colorResource(id = R.color.aero)
//    val snow = colorResource(id = R.color.snow)
//    val ghostwhite = colorResource(id = R.color.ghostwhite)
//
//
//    AlertDialog(
//        onDismissRequest = onDismiss,
//        modifier = Modifier.background(
//            brush = Brush.verticalGradient(
//                colors = listOf(warmGold, brightOrange, deepOrange, boldRed)
//            ),
//            shape = RoundedCornerShape(16.dp)
//        ),
//        title = {
//            Text(
//                text = "Update Complaint",
//                style = MaterialTheme.typography.headlineSmall,
//                modifier = Modifier.padding(bottom = 8.dp),
//                color = black
//            )
//        },
//        text = {
//            Column(modifier = Modifier.padding(8.dp)) {
//                OutlinedTextField(
//                    value = newText,
//                    onValueChange = { newText = it },
//                    label = { Text("Complaint Text") },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .heightIn(min = 56.dp, max = 120.dp),
//                    textStyle = LocalTextStyle.current.copy(black),
//                    colors = TextFieldDefaults.colors( // FIXED
//                        unfocusedContainerColor = snow,
//                        focusedContainerColor = snow,
//                        unfocusedTextColor = black,
//                        focusedTextColor = black,
//                        focusedIndicatorColor = brightOrange,
//                        unfocusedIndicatorColor = deepOrange
//                    ),
//                    maxLines = 3,
//                    keyboardOptions = KeyboardOptions.Default,
//                    keyboardActions = KeyboardActions.Default,
//                    shape = RoundedCornerShape(12.dp)
//                )
//
//                Spacer(modifier = Modifier.height(12.dp))
//
//                Text("Urgency", style = MaterialTheme.typography.bodyLarge, color = black)
//                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
//                    listOf("High", "Medium", "Normal").forEach { urgency ->
//                        FilterChip(
//                            selected = newUrgency == urgency,
//                            onClick = { newUrgency = urgency },
//                            label = { Text(urgency) },
//                            leadingIcon = if (newUrgency == urgency) {
//                                { Icon(Icons.Default.Check, contentDescription = null, tint = Color.White) }
//                            } else null,
//                            colors = FilterChipDefaults.filterChipColors( // FIXED
//                                containerColor = softTan,
//                                selectedContainerColor = offWhite,
//                                labelColor = black,
//                                selectedLabelColor = warmBeige
//                            )
//                        )
//
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(12.dp))
//
//                Text("Status", style = MaterialTheme.typography.bodyLarge, color = black)
//                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
//                    listOf("Open", "Pending", "Closed").forEach { status ->
//                        FilterChip(
//                            selected = newStatus == status,
//                            onClick = { newStatus = status },
//                            label = { Text(status) },
//                            leadingIcon = if (newStatus == status) {
//                                { Icon(Icons.Default.Check, contentDescription = null, tint = Color.White) }
//                            } else null,
//                            colors = FilterChipDefaults.filterChipColors(
//                                containerColor = softTan,
//                                selectedContainerColor = offWhite,
//                                labelColor = black,
//                                selectedLabelColor = warmBeige
//                            )
//                        )
//                    }
//                }
//            }
//        },
//        confirmButton = {
//            Button(
//                onClick = {
//                    updateComplaint(firestore, userId, complaint.id, newText, newStatus, newUrgency, onDismiss)
//                },
//                shape = RoundedCornerShape(10.dp),
//                colors = ButtonDefaults.buttonColors(containerColor = deepOrange)
//            ) {
//                Text("Save", color = black)
//            }
//        },
//        dismissButton = {
//            Button(
//                onClick = onDismiss,
//                shape = RoundedCornerShape(10.dp),
//                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
//            ) {
//                Text("Cancel", color = black)
//            }
//        }
//    )
//}
//
//fun updateComplaint(
//    firestore: FirebaseFirestore,
//    userId: String,
//    complaintId: String,
//    newText: String,
//    newStatus: String,
//    newUrgency: String,
//    onComplete: () -> Unit
//) {
//    firestore.collection("users").document(userId).collection("complaints")
//        .document(complaintId)
//        .update("complainText", newText, "status", newStatus, "urgency", newUrgency)
//        .addOnSuccessListener { onComplete() }
//}
//
//fun deleteComplaint(
//    firestore: FirebaseFirestore,
//    userId: String,
//    complaintId: String,
//    onComplete: () -> Unit
//) {
//    firestore.collection("users").document(userId).collection("complaints")
//        .document(complaintId)
//        .delete()
//        .addOnSuccessListener { onComplete() }
//}
//
//@Preview(showBackground = true)
//@Composable
//fun PreviewComplaintsScreen() {
//    ComplaintsListScreen()
//}
