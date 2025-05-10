package com.example.ritik_2

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.ritik_2.modules.MainViewModel
import com.example.ritik_2.modules.UserProfile
import com.example.ritik_2.smb.SMBActivity
import com.example.ritik_2.ui.theme.ui.theme.ITConnectTheme
import com.example.ritik_2.ui.theme.MainScreen
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

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        // Check authentication status
        if (firebaseAuth.currentUser == null) {
            navigateToLogin()
            return
        }

        // Load user data
        loadUserData()

        setContent {
            ITConnectTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Use state holders from ViewModel
                    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
                    var isLoading by remember { mutableStateOf(true) }

                    // Update state when ViewModel data changes
                    LaunchedEffect(Unit) {
                        viewModel.userProfileState.observe(this@MainActivity) { profile ->
                            userProfile = profile
                        }
                        viewModel.isLoadingState.observe(this@MainActivity) { loading ->
                            isLoading = loading
                        }
                        viewModel.errorMessageState.observe(this@MainActivity) { errorMsg ->
                            errorMsg?.let {
                                Toast.makeText(this@MainActivity, it, Toast.LENGTH_SHORT).show()
                                viewModel.clearError()
                            }
                        }
                    }

                    MainScreen(
                        userProfile = userProfile,
                        isLoading = isLoading,
                        onLogout = {
                            firebaseAuth.signOut()
                            navigateToLogin()
                        },
                        onCardClick = { cardId -> handleCardClick(cardId) },
                        onProfileClick = { navigateToProfile() }
                    )
                }
            }
        }
    }

    private fun loadUserData() {
        viewModel.isLoadingState.value = true
        firebaseAuth.currentUser?.uid?.let { userId ->
            viewModel.loadUserProfile(userId)
        } ?: run {
            viewModel.setError("User not authenticated")
            viewModel.isLoadingState.value = false
        }
    }

    private fun handleCardClick(cardId: Int) {
        when (cardId) {
            1 -> startActivity(Intent(this, RegisterComplain::class.java))
            2 -> startActivity(Intent(this, ViewComplaintsActivity::class.java))
            //3 -> startActivity(Intent(this, SettingsActivity::class.java))
            //4 -> startActivity(Intent(this, HelpSupportActivity::class.java))
            5 -> startActivity(Intent(this, SMBActivity::class.java))
            //6 -> startActivity(Intent(this, TechResourcesActivity::class.java))
            //7 -> startActivity(Intent(this, ProjectCollaborationActivity::class.java))
            //8 -> startActivity(Intent(this, TechNewsActivity::class.java))
        }
    }

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