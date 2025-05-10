package com.example.ritik_2.modules

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MainViewModel : ViewModel() {

    val userProfileState = MutableLiveData<UserProfile?>()
    val isLoadingState = MutableLiveData<Boolean>(true)
    val errorMessageState = MutableLiveData<String?>(null)

    private val firestore = FirebaseFirestore.getInstance()

    fun loadUserProfile(userId: String) {
        viewModelScope.launch {
            isLoadingState.value = true
            try {
                val profile = withContext(Dispatchers.IO) {
                    val document = firestore.collection("users").document(userId).get().await()
                    if (document.exists()) {
                        UserProfile(
                            id = userId,
                            name = document.getString("name") ?: "Unknown User",
                            email = document.getString("email") ?: "",
                            imageUrl = document.getString("imageUrl")?.let { Uri.parse(it) },
                            jobTitle = document.getString("jobTitle") ?: "IT Professional",
                            skills = document.get("skills") as? List<String> ?: listOf(),
                            experience = document.getLong("experience")?.toInt() ?: 0,
                            completedProjects = document.getLong("completedProjects")?.toInt() ?: 0,
                            activeProjects = document.getLong("activeProjects")?.toInt() ?: 0
                        )
                    } else {
                        null
                    }
                }

                userProfileState.value = profile ?: UserProfile(
                    id = userId,
                    name = "Unknown User",
                    email = "",
                    imageUrl = null
                )
            } catch (e: Exception) {
                errorMessageState.value = "Failed to load profile: ${e.message}"
            } finally {
                isLoadingState.value = false
            }
        }
    }

    fun clearError() {
        errorMessageState.value = null
    }

    fun setError(message: String) {
        errorMessageState.value = message
    }
}