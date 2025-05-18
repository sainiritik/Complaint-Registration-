package com.example.ritik_2.ui.theme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ritik_2.R
import com.example.ritik_2.SortOption
import com.example.ritik_2.modules.ComplaintWithId
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplaintViewScreen(
    complaints: List<ComplaintWithId>,
    isRefreshing: Boolean,
    showRefreshAnimation: Boolean,
    searchQuery: String,
    currentSortOption: SortOption,
    currentFilterOption: String?,
    hasMoreData: Boolean,
    onDeleteComplaint: (String) -> Unit,
    onUpdateComplaint: (String, String, String, String, String) -> Unit,
    onLoadMore: () -> Unit,
    onRefresh: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSortOptionChange: (SortOption) -> Unit,
    onFilterOptionChange: (String?) -> Unit,
    onBackClick: () -> Unit
) {
    val listState = rememberLazyListState()

    // Custom pull-to-refresh implementation
    var pullDistance by remember { mutableStateOf(0f) }
    val pullThreshold = 100f
    var isRefreshTriggered by remember { mutableStateOf(false) }

    // Reset refresh triggered state when refreshing is done
    LaunchedEffect(isRefreshing) {
        if (!isRefreshing) {
            isRefreshTriggered = false
        }
    }

    // Monitor end of list for pagination
    val endReached by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItemsNumber = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1

            lastVisibleItemIndex > 0 && lastVisibleItemIndex >= totalItemsNumber - 2
        }
    }

    // Load more items when reaching end of list
    LaunchedEffect(endReached) {
        if (endReached && hasMoreData && !isRefreshing) {
            onLoadMore()
        }
    }

    // Animation for refresh icon
    val infiniteTransition = rememberInfiniteTransition(label = "refreshTransition")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "refreshRotation"
    )

    // Filter dropdown
    val categories = listOf("All", "IT", "HR", "Facilities", "Security", "Finance", "Other")
    var showFilterDropdown by remember { mutableStateOf(false) }

    // Sort dropdown
    var showSortDropdown by remember { mutableStateOf(false) }

    // Edit dialog state
    var showEditDialog by remember { mutableStateOf(false) }
    var complaintToEdit by remember { mutableStateOf<ComplaintWithId?>(null) }

    // Delete confirmation dialog
    var showDeleteDialog by remember { mutableStateOf(false) }
    var complaintToDelete by remember { mutableStateOf<ComplaintWithId?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Complaints", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    AnimatedVisibility(visible = showRefreshAnimation) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refreshing",
                            tint = Color.White,
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .rotate(rotation)
                        )
                    }
                    IconButton(onClick = { showSortDropdown = true }) {
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = "Sort",
                            tint = Color.White
                        )
                        DropdownMenu(
                            expanded = showSortDropdown,
                            onDismissRequest = { showSortDropdown = false }
                        ) {
                            Text(
                                "Sort By:",
                                modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 4.dp),
                                fontWeight = FontWeight.Bold
                            )
                            DropdownMenuItem(
                                text = { Text("Newest First") },
                                onClick = {
                                    onSortOptionChange(SortOption.DATE_DESC)
                                    showSortDropdown = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Oldest First") },
                                onClick = {
                                    onSortOptionChange(SortOption.DATE_ASC)
                                    showSortDropdown = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("By Urgency") },
                                onClick = {
                                    onSortOptionChange(SortOption.URGENCY)
                                    showSortDropdown = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("By Status") },
                                onClick = {
                                    onSortOptionChange(SortOption.STATUS)
                                    showSortDropdown = false
                                }
                            )
                        }
                    }

                    IconButton(onClick = { showFilterDropdown = true }) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Filter",
                            tint = Color.White
                        )
                        DropdownMenu(
                            expanded = showFilterDropdown,
                            onDismissRequest = { showFilterDropdown = false }
                        ) {
                            Text(
                                "Filter By Category:",
                                modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 4.dp),
                                fontWeight = FontWeight.Bold
                            )
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category) },
                                    onClick = {
                                        onFilterOptionChange(if (category == "All") null else category)
                                        showFilterDropdown = false
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.bright_orange)
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onVerticalDrag = { change, dragAmount ->
                            // Only allow pull down when at the top of the list
                            if (listState.firstVisibleItemIndex == 0 &&
                                (listState.firstVisibleItemScrollOffset == 0 || pullDistance > 0)) {
                                // Add drag distance with some resistance
                                pullDistance += dragAmount * 0.5f
                                // Keep only positive values (pulling down)
                                pullDistance = pullDistance.coerceAtLeast(0f)
                            }
                            change.consume()
                        },
                        onDragEnd = {
                            if (pullDistance > pullThreshold && !isRefreshing && !isRefreshTriggered) {
                                isRefreshTriggered = true
                                onRefresh()
                            }
                            // Reset pull distance with animation
                            pullDistance = 0f
                        },
                        onDragCancel = {
                            pullDistance = 0f
                        }
                    )
                }
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Custom Pull to refresh indicator
                if (pullDistance > 0 || isRefreshing) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((pullDistance * 0.5f).coerceAtMost(50f).dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                color = colorResource(id = R.color.bright_orange),
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Pull to refresh",
                                tint = colorResource(id = R.color.bright_orange),
                                modifier = Modifier
                                    .size(24.dp)
                                    .rotate(pullDistance * 0.2f)
                            )
                        }
                    }
                }

                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search complaints...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear search")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )

                // Filter chips display
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                ) {
                    if (currentFilterOption != null) {
                        FilterChip(
                            text = "Category: $currentFilterOption",
                            onClear = { onFilterOptionChange(null) }
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    val sortText = when (currentSortOption) {
                        SortOption.DATE_DESC -> "Newest First"
                        SortOption.DATE_ASC -> "Oldest First"
                        SortOption.URGENCY -> "By Urgency"
                        SortOption.STATUS -> "By Status"
                    }
                    Text(
                        text = "Sorting: $sortText",
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // Empty state
                if (complaints.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No complaints found",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        if (searchQuery.isNotEmpty() || currentFilterOption != null) {
                            Text(
                                text = "Try changing your search or filter settings",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }
                }

                // Complaints list
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(complaints) { index, complaint ->
                        ComplaintCard(
                            complaint = complaint,
                            onEditClick = {
                                complaintToEdit = complaint
                                showEditDialog = true
                            },
                            onDeleteClick = {
                                complaintToDelete = complaint
                                showDeleteDialog = true
                            }
                        )

                        if (index < complaints.size - 1) {
                            Divider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = Color.LightGray
                            )
                        }
                    }

                    // Bottom loader for pagination
                    if (hasMoreData) {
                        item {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = colorResource(id = R.color.bright_orange),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit dialog
    if (showEditDialog && complaintToEdit != null) {
        EditComplaintDialog(
            complaint = complaintToEdit!!,
            onDismiss = { showEditDialog = false },
            onSave = { title, status, urgency, category ->
                onUpdateComplaint(
                    complaintToEdit!!.id,
                    title,
                    status,
                    urgency,
                    category
                )
                showEditDialog = false
            }
        )
    }

    // Delete confirmation dialog
    if (showDeleteDialog && complaintToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Complaint") },
            text = { Text("Are you sure you want to delete this complaint?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteComplaint(complaintToDelete!!.id)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun FilterChip(
    text: String,
    onClear: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(colorResource(id = R.color.bright_orange).copy(alpha = 0.1f))
            .padding(start = 12.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
    ) {
        Text(text = text, color = colorResource(id = R.color.bright_orange), fontSize = 12.sp)
        Spacer(modifier = Modifier.width(4.dp))
        IconButton(
            onClick = onClear,
            modifier = Modifier.size(16.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Clear filter",
                tint = colorResource(id = R.color.bright_orange),
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

@Composable
fun ComplaintCard(
    complaint: ComplaintWithId,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { expanded = !expanded },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = complaint.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = complaint.getFormattedDate(),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                Box(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .border(
                            width = 1.dp,
                            color = when (complaint.status) {
                                "Open" -> Color.Red
                                "In Progress" -> Color(0xFFFFA500) // Orange
                                "Resolved" -> Color.Green
                                else -> Color.Gray
                            },
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = complaint.status,
                        fontSize = 12.sp,
                        color = when (complaint.status) {
                            "Open" -> Color.Red
                            "In Progress" -> Color(0xFFFFA500) // Orange
                            "Resolved" -> Color.Green
                            else -> Color.Gray
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryPill(category = complaint.category)
                Spacer(modifier = Modifier.width(8.dp))
                UrgencyIndicator(urgency = complaint.urgency)
                if (complaint.hasAttachment) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.Default.AttachFile,
                        contentDescription = "Has attachment",
                        modifier = Modifier.size(16.dp),
                        tint = Color.Gray
                    )
                }
            }

            // Expandable content
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = complaint.description,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (complaint.contactInfo.isNotBlank()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Contact: ${complaint.contactInfo}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = onEditClick,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = colorResource(id = R.color.bright_orange)
                            )
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit")
                        }

                        TextButton(
                            onClick = onDeleteClick,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color.Red
                            )
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryPill(category: String) {
    val backgroundColor = when (category) {
        "IT" -> Color(0xFFE6F0FF)
        "HR" -> Color(0xFFFFF9E6)
        "Facilities" -> Color(0xFFE6F6FF)
        "Security" -> Color(0xFFF2E6FF)
        "Finance" -> Color(0xFFE6FFFA)
        "Other" -> Color(0xFFFFE6E6)
        else -> Color(0xFFF0F0F0)
    }

    val textColor = when (category) {
        "IT" -> Color(0xFF0066CC)
        "HR" -> Color(0xFFCC8500)
        "Facilities" -> Color(0xFF0099CC)
        "Security" -> Color(0xFF6600CC)
        "Finance" -> Color(0xFF00CC99)
        "Other" -> Color(0xFFCC0000)
        else -> Color.Gray
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = category,
            fontSize = 10.sp,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun UrgencyIndicator(urgency: String) {
    val color = when (urgency) {
        "Critical" -> Color.Red
        "High" -> Color(0xFFFFA500) // Orange
        "Medium" -> Color(0xFFFFCC00) // Yellow
        "Low" -> Color.Green
        else -> Color.Gray
    }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = urgency,
            fontSize = 10.sp,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditComplaintDialog(
    complaint: ComplaintWithId,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf(complaint.title) }
    var status by remember { mutableStateOf(complaint.status) }
    var urgency by remember { mutableStateOf(complaint.urgency) }
    var category by remember { mutableStateOf(complaint.category) }

    // Validation state
    var isTitleValid by remember { mutableStateOf(true) }

    // Dropdowns
    var showStatusDropdown by remember { mutableStateOf(false) }
    var showUrgencyDropdown by remember { mutableStateOf(false) }
    var showCategoryDropdown by remember { mutableStateOf(false) }

    val statusOptions = listOf("Open", "In Progress", "Resolved", "Closed")
    val urgencyOptions = listOf("Critical", "High", "Medium", "Low")
    val categoryOptions = listOf("IT", "HR", "Facilities", "Security", "Finance", "Other")

    // Colors for status indicators
    val statusColors = mapOf(
        "Open" to Color.Red,
        "In Progress" to Color(0xFFFFA500), // Orange
        "Resolved" to Color.Green,
        "Closed" to Color.Gray
    )

    // Colors for urgency indicators
    val urgencyColors = mapOf(
        "Critical" to Color.Red,
        "High" to Color(0xFFFFA500), // Orange
        "Medium" to Color(0xFFFFCC00), // Yellow
        "Low" to Color.Green
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 8.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                colorResource(id = R.color.bright_orange).copy(alpha = 0.05f),
                                Color.White
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    // Header with close button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Edit Complaint",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(id = R.color.bright_orange)
                        )

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.DarkGray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Title field with validation
                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            isTitleValid = it.trim().isNotEmpty()
                        },
                        label = { Text("Title") },
                        placeholder = { Text("Enter complaint title") },
                        isError = !isTitleValid,
                        supportingText = {
                            if (!isTitleValid) {
                                Text("Title cannot be empty", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorResource(id = R.color.bright_orange),
                            focusedLabelColor = colorResource(id = R.color.bright_orange),
                            cursorColor = colorResource(id = R.color.bright_orange)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Status dropdown with colored indicator
                    Text(
                        text = "Status",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                width = 1.dp,
                                color = Color.LightGray,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { showStatusDropdown = true }
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(statusColors[status] ?: Color.Gray)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = status,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = "Select status"
                            )
                        }

                        DropdownMenu(
                            expanded = showStatusDropdown,
                            onDismissRequest = { showStatusDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.95f)
                        ) {
                            statusOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .clip(CircleShape)
                                                    .background(statusColors[option] ?: Color.Gray)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(option)
                                        }
                                    },
                                    onClick = {
                                        status = option
                                        showStatusDropdown = false
                                    },
                                    colors = MenuDefaults.itemColors(
                                        textColor = if (status == option)
                                            colorResource(id = R.color.bright_orange) else Color.Black
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Urgency dropdown with colored indicator
                    Text(
                        text = "Urgency",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                width = 1.dp,
                                color = Color.LightGray,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { showUrgencyDropdown = true }
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(urgencyColors[urgency] ?: Color.Gray)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = urgency,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = "Select urgency"
                            )
                        }

                        DropdownMenu(
                            expanded = showUrgencyDropdown,
                            onDismissRequest = { showUrgencyDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.95f)
                        ) {
                            urgencyOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .clip(CircleShape)
                                                    .background(urgencyColors[option] ?: Color.Gray)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(option)
                                        }
                                    },
                                    onClick = {
                                        urgency = option
                                        showUrgencyDropdown = false
                                    },
                                    colors = MenuDefaults.itemColors(
                                        textColor = if (urgency == option)
                                            colorResource(id = R.color.bright_orange) else Color.Black
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Category dropdown with colored indicators
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                width = 1.dp,
                                color = Color.LightGray,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { showCategoryDropdown = true }
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CategoryPill(category = category)
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = "Select category"
                            )
                        }

                        DropdownMenu(
                            expanded = showCategoryDropdown,
                            onDismissRequest = { showCategoryDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.95f)
                        ) {
                            categoryOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { CategoryPill(category = option) },
                                    onClick = {
                                        category = option
                                        showCategoryDropdown = false
                                    },
                                    colors = MenuDefaults.itemColors(
                                        textColor = if (category == option)
                                            colorResource(id = R.color.bright_orange) else Color.Black
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.LightGray,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                if (title.trim().isNotEmpty()) {
                                    onSave(title, status, urgency, category)
                                } else {
                                    isTitleValid = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(id = R.color.bright_orange)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            enabled = title.trim().isNotEmpty()
                        ) {
                            Text("Save Changes")
                        }
                    }
                }
            }
        }
    }
}