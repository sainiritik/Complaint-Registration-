package com.example.ritik_2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.lifecycle.lifecycleScope
import com.example.ritik_2.modules.ComplaintWithId
import com.example.ritik_2.ui.theme.ComplaintViewScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ComplaintViewActivity : ComponentActivity() {
    // Firebase instances
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // Flow for complaints list and loading state
    private val _complaints = MutableStateFlow<List<ComplaintWithId>>(emptyList())
    val complaints: StateFlow<List<ComplaintWithId>> = _complaints
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery
    private val _sortOption = MutableStateFlow(SortOption.DATE_DESC)
    val sortOption: StateFlow<SortOption> = _sortOption
    private val _filterOption = MutableStateFlow<String?>(null)
    val filterOption: StateFlow<String?> = _filterOption

    // Pagination
    private var lastVisibleDocument: DocumentSnapshot? = null
    private val batchSize = 10
    private val _hasMoreData = MutableStateFlow(true)
    val hasMoreData: StateFlow<Boolean> = _hasMoreData
    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing

    // Animation state
    private val _showRefreshAnimation = MutableStateFlow(false)
    val showRefreshAnimation: StateFlow<Boolean> = _showRefreshAnimation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if user is logged in
        val currentUser = auth.currentUser
        if (currentUser == null) {
            // Auto-sign in anonymously
            signInAnonymously()
        } else {
            // Load user's complaints
            loadInitialComplaints()
        }

        setContent {
            MaterialTheme {
                val complaintsList by complaints.collectAsState()
                val loading by isLoading.collectAsState()
                val query by searchQuery.collectAsState()
                val sort by sortOption.collectAsState()
                val filter by filterOption.collectAsState()
                val isRefreshing by refreshing.collectAsState()
                val moreData by hasMoreData.collectAsState()
                val showAnimation by showRefreshAnimation.collectAsState()

                if (loading && complaintsList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colorResource(id = R.color.bright_orange))
                    }
                } else {
                    ComplaintViewScreen(
                        complaints = complaintsList,
                        isRefreshing = isRefreshing,
                        showRefreshAnimation = showAnimation,
                        searchQuery = query,
                        currentSortOption = sort,
                        currentFilterOption = filter,
                        hasMoreData = moreData,
                        onDeleteComplaint = { complaintId -> deleteComplaint(complaintId) },
                        onUpdateComplaint = { complaintId, newTitle, newStatus, newUrgency, newCategory ->
                            updateComplaint(complaintId, newTitle, newStatus, newUrgency, newCategory)
                        },
                        onLoadMore = { loadMoreComplaints() },
                        onRefresh = { refreshComplaints() },
                        onSearchQueryChange = { updateSearchQuery(it) },
                        onSortOptionChange = { updateSortOption(it) },
                        onFilterOptionChange = { updateFilterOption(it) },
                        onBackClick = { finish() }
                    )
                }
            }
        }
    }

    private fun signInAnonymously() {
        auth.signInAnonymously()
            .addOnSuccessListener {
                loadInitialComplaints()
            }
            .addOnFailureListener { e ->
                // Handle failure
                _isLoading.value = false
            }
    }

    private fun loadInitialComplaints() {
        _isLoading.value = true
        lastVisibleDocument = null
        _hasMoreData.value = true

        loadComplaints(true)
    }

    private fun loadMoreComplaints() {
        if (_hasMoreData.value && !_isLoading.value) {
            loadComplaints(false)
        }
    }

    private fun refreshComplaints() {
        _refreshing.value = true
        _showRefreshAnimation.value = true

        // Reset pagination
        lastVisibleDocument = null
        _hasMoreData.value = true

        loadComplaints(true) {
            // After 1 second, hide the animation
            lifecycleScope.launch {
                kotlinx.coroutines.delay(1000)
                _refreshing.value = false
                _showRefreshAnimation.value = false
            }
        }
    }

    private fun loadComplaints(isInitialLoad: Boolean, onComplete: () -> Unit = {}) {
        val userId = auth.currentUser?.uid ?: return

        lifecycleScope.launch {
            try {
                _isLoading.value = true

                // Start building the query
                var query = firestore.collection("users")
                    .document(userId)
                    .collection("complaints")

                // Apply sorting
                // Original code with error
//                when (_sortOption.value) {
//                    SortOption.DATE_DESC -> query = query.orderBy("timestamp", Query.Direction.DESCENDING)
//                    SortOption.DATE_ASC -> query = query.orderBy("timestamp", Query.Direction.ASCENDING)
//                    SortOption.URGENCY -> {
//                        // Custom urgency ordering
//                        query = query.orderBy("urgencyOrder", Query.Direction.DESCENDING)
//                            .orderBy("timestamp", Query.Direction.DESCENDING)
//                    }
//                    SortOption.STATUS -> query = query.orderBy("status", Query.Direction.ASCENDING)
//                        .orderBy("timestamp", Query.Direction.DESCENDING)
//                }
//
//// Apply filtering if needed
//                if (_filterOption.value != null && _filterOption.value != "All") {
//                    query = query.whereEqualTo("category", _filterOption.value)
//                }
//
//// Apply pagination
//                if (lastVisibleDocument != null && !isInitialLoad) {
//                    query = query.startAfter(lastVisibleDocument!!)
//                }
//
//// Set limit
//                query = query.limit(batchSize.toLong()) // Error occurs here


// CORRECTED CODE:
// Build the query step by step
                var queryBase = firestore.collection("users")
                    .document(userId)
                    .collection("complaints")

// Apply sorting
                val querySorted = when (_sortOption.value) {
                    SortOption.DATE_DESC -> queryBase.orderBy("timestamp", Query.Direction.DESCENDING)
                    SortOption.DATE_ASC -> queryBase.orderBy("timestamp", Query.Direction.ASCENDING)
                    SortOption.URGENCY -> {
                        // Custom urgency ordering
                        queryBase.orderBy("urgencyOrder", Query.Direction.DESCENDING)
                            .orderBy("timestamp", Query.Direction.DESCENDING)
                    }
                    SortOption.STATUS -> queryBase.orderBy("status", Query.Direction.ASCENDING)
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                }

// Apply filtering if needed
                val queryFiltered = if (_filterOption.value != null && _filterOption.value != "All") {
                    querySorted.whereEqualTo("category", _filterOption.value)
                } else {
                    querySorted
                }

// Apply pagination
                val queryPaginated = if (lastVisibleDocument != null && !isInitialLoad) {
                    queryFiltered.startAfter(lastVisibleDocument!!)
                } else {
                    queryFiltered
                }

// Set limit and execute
                val finalQuery = queryPaginated.limit(batchSize.toLong())
                val querySnapshot = finalQuery.get().await()

                // Update last document for pagination
                if (querySnapshot.documents.isNotEmpty()) {
                    lastVisibleDocument = querySnapshot.documents.last()
                }

                // Check if we have more data
                _hasMoreData.value = querySnapshot.documents.size >= batchSize

                // Process results
                val complaintsList = querySnapshot.documents.mapNotNull { doc ->
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
                    } else null
                }

                // Filter by search query if needed
                val filteredList = if (_searchQuery.value.isNotBlank()) {
                    complaintsList.filter { complaint ->
                        complaint.title.contains(_searchQuery.value, ignoreCase = true) ||
                                complaint.description.contains(_searchQuery.value, ignoreCase = true) ||
                                complaint.category.contains(_searchQuery.value, ignoreCase = true)
                    }
                } else {
                    complaintsList
                }

                // Update the list
                if (isInitialLoad) {
                    _complaints.value = filteredList
                } else {
                    _complaints.value = _complaints.value + filteredList
                }

            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
                onComplete()
            }
        }
    }

    private fun updateComplaint(
        complaintId: String,
        newTitle: String,
        newStatus: String,
        newUrgency: String,
        newCategory: String
    ) {
        val userId = auth.currentUser?.uid ?: return
        _showRefreshAnimation.value = true

        lifecycleScope.launch {
            try {
                // Calculate urgency order for sorting
                val urgencyOrder = when(newUrgency) {
                    "Critical" -> 4
                    "High" -> 3
                    "Medium" -> 2
                    "Low" -> 1
                    else -> 0
                }

                // Update in user's collection
                val updates = hashMapOf<String, Any>(
                    "title" to newTitle,
                    "status" to newStatus,
                    "urgency" to newUrgency,
                    "urgencyOrder" to urgencyOrder,
                    "category" to newCategory,
                    "lastUpdated" to System.currentTimeMillis()
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

                // Show animation and refresh data
                refreshComplaints()

            } catch (e: Exception) {
                // Handle error
                _showRefreshAnimation.value = false
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

                // Update the local list
                _complaints.value = _complaints.value.filter { it.id != complaintId }

            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        // Refresh the data with the new query
        loadInitialComplaints()
    }

    private fun updateSortOption(option: SortOption) {
        _sortOption.value = option
        // Refresh the data with the new sort option
        loadInitialComplaints()
    }

    private fun updateFilterOption(category: String?) {
        _filterOption.value = category
        // Refresh the data with the new filter
        loadInitialComplaints()
    }
}

enum class SortOption {
    DATE_DESC,
    DATE_ASC,
    URGENCY,
    STATUS
}

// Extension function for ComplaintWithId
fun ComplaintWithId.getFormattedDate(): String {
    val date = Date(this.timestamp)
    val format = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
    return format.format(date)
}
