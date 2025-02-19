package com.example.ritik_2

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class MainViewModel : ViewModel() {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    // 🔥 LiveData to observe auth state changes
    private val _isUserLoggedIn = MutableLiveData<Boolean>(firebaseAuth.currentUser != null)
    val isUserLoggedIn: LiveData<Boolean> = _isUserLoggedIn

    init {
        // ✅ Listen for auth state changes
        firebaseAuth.addAuthStateListener {
            _isUserLoggedIn.value = it.currentUser != null
        }
    }

    // ✅ Check if user is logged in
    fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    // ✅ Logout User and update state
    fun logout() {
        firebaseAuth.signOut()
        _isUserLoggedIn.value = false
    }
}
