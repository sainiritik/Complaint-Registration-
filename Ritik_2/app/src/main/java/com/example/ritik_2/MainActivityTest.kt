/*package com.example.ritik_2

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ritik_2.ui.theme.GridLayout
import com.example.ritik_2.ui.theme.ViewComplaintsActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay

class MainActivityTest : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreenTest(
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    navigateToLogin()
                },
                onCardClick = { cardId -> handleCardClick(cardId) }
            )
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun handleCardClick(cardId: Int) {
        when (cardId) {
            1 -> startActivity(Intent(this, RegisterComplain::class.java))
            2 -> startActivity(Intent(this, ViewComplaintsActivity::class.java))
        }
    }
}

@Composable
fun MainScreenTest(
    onLogout: () -> Unit,
    onCardClick: (Int) -> Unit
) {
    val context = LocalContext.current
    var userName by remember { mutableStateOf("Loading...") }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var complaintCount by remember { mutableStateOf(0) }
    val firebaseAuth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val userId = firebaseAuth.currentUser?.uid

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

    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)  // Refresh complaint count every 5 seconds
            firestore.collection("complaints").get()
                .addOnSuccessListener { documents ->
                    complaintCount = documents.size()
                }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF1E293B))))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // **Welcome Text**
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Welcome,",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFFBFDBFE)
                    )
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // **Profile Image with Glow**
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3B82F6).copy(alpha = 0.2f))
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
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(Color.Gray)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // **Logout Button**
                IconButton(onClick = onLogout) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Logout",
                        tint = Color(0xFFF87171)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // **Complaint Notification**
            AnimatedVisibility(
                visible = complaintCount > 0,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF60A5FA)
                ) {
                    Text(
                        text = "🚀 You have $complaintCount new complaints!",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // **Card Menu Grid**
            val cardItems = listOf(
                Pair(1, "📌 Register a Complaint"),
                Pair(2, "📂 View Complaints"),
                Pair(3, "⚙️ Settings"),
                Pair(4, "❓ Help & Support")
            )

            GridLayout(
                items = cardItems,
                onCardClick = onCardClick
            )
        }
    }
}
*/