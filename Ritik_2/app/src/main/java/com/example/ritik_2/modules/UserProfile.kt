package com.example.ritik_2.modules

import android.net.Uri

data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val imageUrl: Uri? = null,
    val jobTitle: String = "IT Professional",
    val skills: List<String> = emptyList(),
    val experience: Int = 0,
    val completedProjects: Int = 0,
    val activeProjects: Int = 0
)