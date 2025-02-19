package com.example.ritik_2.ui.theme

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ritik_2.ProfileActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun MainScreen(
    onLogout: () -> Unit,
    onCardClick: (Int) -> Unit
) {
    val context = LocalContext.current
    var userName by remember { mutableStateOf("Loading...") }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var showProfileDialog by remember { mutableStateOf(false) }

    val firebaseAuth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val userId = firebaseAuth.currentUser?.uid

    // Fetch user data from Firestore
    LaunchedEffect(userId) {
        userId?.let {
            firestore.collection("users").document(it).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        userName = document.getString("name") ?: "Unknown User"
                        profileImageUri = document.getString("imageUrl")?.let { Uri.parse(it) }
                    }
                }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 50.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 🔹 Profile Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Welcome, $userName",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                // 🔹 Profile Image (Click to Open ProfileActivity)
                Box(
                    modifier = Modifier
                        .size(64.dp) // Slightly larger for better UI
                        .clip(CircleShape)
                        .background(Color.Gray)
                        .clickable {
                            val intent = Intent(context, ProfileActivity::class.java)
                            context.startActivity(intent)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(profileImageUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape),
                        alignment = Alignment.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 🔹 Example Grid Layout
            val cardItems = listOf(
                Pair(1, "Register a Complaint"),
                Pair(2, "View Complaints"),
                Pair(3, "Settings"),
                Pair(4, "Help & Support")
            )
            GridLayout(items = cardItems, onCardClick = onCardClick)
        }

        // 🔹 Profile Menu Dialog
        if (showProfileDialog) {
            ProfileDialog(
                onDismiss = { showProfileDialog = false },
                onSeeProfile = {
                    val intent = Intent(context, ProfileActivity::class.java)
                    context.startActivity(intent)
                    showProfileDialog = false
                },
                onLogout = {
                    onLogout()
                    showProfileDialog = false
                }
            )
        }
    }
}

// 🔹 Profile Options Popup
@Composable
fun ProfileDialog(onDismiss: () -> Unit, onSeeProfile: () -> Unit, onLogout: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Profile Options") },
        text = {
            Column {
                Text(
                    "See Profile",
                    fontSize = 20.sp, // Increased Size
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .clickable { onSeeProfile() }
                )

                Spacer(modifier = Modifier.height(12.dp)) // Increased Space

                Text(
                    "Logout",
                    fontSize = 20.sp, // Increased Size
                    color = Color.Red,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .clickable { onLogout() }
                )
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text("Close") } }
    )
}

// 🔹 Preview Support
@Preview(showBackground = true)
@Composable
fun PreviewMainScreen() {
    MainScreen(
        onLogout = {},
        onCardClick = {}
    )
}
