package com.example.ritik_2.ui.theme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ritik_2.ComplaintWithId
import com.example.ritik_2.R
import androidx.compose.ui.res.colorResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplaintViewScreen(
    complaints: List<ComplaintWithId>,
    onDeleteComplaint: (String) -> Unit,
    onUpdateComplaint: (String, String, String, String, String) -> Unit,
    onBackClick: () -> Unit
) {
    var selectedComplaintId by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var complaintToDelete by remember { mutableStateOf<String?>(null) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var complaintToUpdate by remember { mutableStateOf<ComplaintWithId?>(null) }

    // Colors
    val primaryColor = Color(0xFF6200EE)
    val surfaceColor = Color(0xFFF5F5F5)
    val lightTextColor = Color(0xFF747474)

    // Colors from the original code
    val black = colorResource(id = R.color.black)
    val offWhite = colorResource(id = R.color.off_white)
    val warmBeige = colorResource(id = R.color.warm_beige)
    val softTan = colorResource(id = R.color.soft_tan)
    val warmGold = colorResource(id = R.color.warm_gold)
    val brightOrange = colorResource(id = R.color.bright_orange)
    val deepOrange = colorResource(id = R.color.deep_orange)
    val boldRed = colorResource(id = R.color.bold_red)
    val emerald = colorResource(id = R.color.emerald)
    val platinum = colorResource(id = R.color.platinum)
    val bitterSweet = colorResource(id = R.color.bittersweet)
    val aero = colorResource(id = R.color.aero)
    val snow = colorResource(id = R.color.snow)

    // Urgency level colors
    val urgencyColors = mapOf(
        "Low" to Color(0xFF4CAF50),
        "Medium" to Color(0xFFFFC107),
        "High" to Color(0xFFFF5722),
        "Critical" to Color(0xFFF44336)
    )

    // Category icons
    val categoryIcons = mapOf(
        "IT" to Icons.Filled.Computer,
        "HR" to Icons.Filled.People,
        "Facilities" to Icons.Filled.Apartment,
        "Security" to Icons.Filled.Security,
        "Finance" to Icons.Filled.AttachMoney,
        "Others" to Icons.Filled.MoreHoriz,
        "General" to Icons.Filled.List
    )

    // Status colors
    val statusColors = mapOf(
        "Open" to Color(0xFF2196F3),
        "In Progress" to Color(0xFFFF9800),
        "Pending" to Color(0xFFFF9800),
        "Resolved" to Color(0xFF4CAF50),
        "Closed" to Color(0xFF9E9E9E)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Complaints",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = bitterSweet
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Badge(
                        containerColor = primaryColor,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Text(
                            text = complaints.size.toString(),
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = platinum
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(snow)
        ) {
            if (complaints.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Inbox,
                        contentDescription = null,
                        modifier = Modifier.size(100.dp),
                        tint = lightTextColor.copy(alpha = 0.5f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "No complaints yet",
                        style = MaterialTheme.typography.titleLarge,
                        color = lightTextColor
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Your submitted complaints will appear here",
                        style = MaterialTheme.typography.bodyMedium,
                        color = lightTextColor.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(complaints) { complaint ->
                        val isExpanded = selectedComplaintId == complaint.id
                        val animatedProgress by animateFloatAsState(
                            targetValue = if (isExpanded) 1f else 0f,
                            animationSpec = tween(durationMillis = 300)
                        )

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable {
                                    selectedComplaintId = if (isExpanded) null else complaint.id
                                },
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 4.dp
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = offWhite
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                // Top Section with category, date, and status
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Category with icon
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(Color(0xFFEDE7F6))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = categoryIcons[complaint.category] ?: Icons.Filled.Help,
                                            contentDescription = null,
                                            tint = primaryColor,
                                            modifier = Modifier.size(16.dp)
                                        )

                                        Spacer(modifier = Modifier.width(4.dp))

                                        Text(
                                            text = complaint.category,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = primaryColor
                                        )
                                    }

                                    // Date
                                    Text(
                                        text = complaint.getFormattedDate(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = lightTextColor
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Title and status badge
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = complaint.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f),
                                        color = aero
                                    )

                                    // Status pill
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(statusColors[complaint.status] ?: Color.Gray)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = complaint.status,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Urgency indicator
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Flag,
                                        contentDescription = null,
                                        tint = urgencyColors[complaint.urgency] ?: Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )

                                    Spacer(modifier = Modifier.width(4.dp))

                                    Text(
                                        text = "${complaint.urgency} Priority",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = urgencyColors[complaint.urgency] ?: Color.Gray
                                    )

                                    if (complaint.hasAttachment) {
                                        Spacer(modifier = Modifier.width(8.dp))

                                        Icon(
                                            imageVector = Icons.Filled.AttachFile,
                                            contentDescription = null,
                                            tint = lightTextColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                // Expanded content
                                AnimatedVisibility(
                                    visible = isExpanded,
                                    enter = fadeIn() + slideInVertically(),
                                    exit = fadeOut()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(top = 16.dp)
                                    ) {
                                        Divider()

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Text(
                                            text = "Description",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = black
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = complaint.description,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = black
                                        )

                                        if (complaint.contactInfo.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(16.dp))

                                            Text(
                                                text = "Contact Information",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = black
                                            )

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Text(
                                                text = complaint.contactInfo,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = black
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        // Action buttons
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Button(
                                                onClick = {
                                                    complaintToUpdate = complaint
                                                    showUpdateDialog = true
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = deepOrange
                                                )
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Edit,
                                                    contentDescription = "Edit"
                                                )

                                                Spacer(modifier = Modifier.width(4.dp))

                                                Text("Update", color = black)
                                            }

                                            OutlinedButton(
                                                onClick = {
                                                    complaintToDelete = complaint.id
                                                    showDeleteDialog = true
                                                },
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    contentColor = Color.Red
                                                )
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Delete,
                                                    contentDescription = "Delete"
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
                }
            }

            // Delete confirmation dialog
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    icon = { Icon(Icons.Filled.Warning, contentDescription = null, tint = Color.Red) },
                    title = { Text("Delete Complaint") },
                    text = { Text("Are you sure you want to delete this complaint? This action cannot be undone.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                complaintToDelete?.let { id ->
                                    onDeleteComplaint(id)
                                    if (selectedComplaintId == id) {
                                        selectedComplaintId = null
                                    }
                                }
                                showDeleteDialog = false
                                complaintToDelete = null
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red
                            )
                        ) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = { showDeleteDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // Update complaint dialog
            if (showUpdateDialog && complaintToUpdate != null) {
                UpdateComplaintDialog(
                    complaint = complaintToUpdate!!,
                    onDismiss = {
                        showUpdateDialog = false
                        complaintToUpdate = null
                    },
                    onUpdateComplaint = { id, title, status, urgency, category ->
                        onUpdateComplaint(id, title, status, urgency, category)
                        showUpdateDialog = false
                        complaintToUpdate = null
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateComplaintDialog(
    complaint: ComplaintWithId,
    onDismiss: () -> Unit,
    onUpdateComplaint: (String, String, String, String, String) -> Unit
) {
    var newTitle by remember { mutableStateOf(complaint.title) }
    var newStatus by remember { mutableStateOf(complaint.status) }
    var newUrgency by remember { mutableStateOf(complaint.urgency) }
    var newCategory by remember { mutableStateOf(complaint.category) }

    // Colors
    val black = colorResource(id = R.color.black)
    val offWhite = colorResource(id = R.color.off_white)
    val warmBeige = colorResource(id = R.color.warm_beige)
    val softTan = colorResource(id = R.color.soft_tan)
    val warmGold = colorResource(id = R.color.warm_gold)
    val brightOrange = colorResource(id = R.color.bright_orange)
    val deepOrange = colorResource(id = R.color.deep_orange)
    val boldRed = colorResource(id = R.color.bold_red)
    val snow = colorResource(id = R.color.snow)

    val categories = listOf("IT", "HR", "Facilities", "Security", "Finance", "General", "Others")
    val urgencyLevels = listOf("Low", "Medium", "High", "Critical")
    val statusOptions = listOf("Open", "In Progress", "Pending", "Resolved", "Closed")

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.background(
            brush = Brush.verticalGradient(
                colors = listOf(warmGold, brightOrange, deepOrange, boldRed)
            ),
            shape = RoundedCornerShape(16.dp)
        ),
        title = {
            Text(
                text = "Update Complaint",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp),
                color = black
            )
        },
        text = {
            Column(modifier = Modifier.padding(8.dp)) {
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    label = { Text("Complaint Title") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp, max = 120.dp),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = snow,
                        focusedContainerColor = snow,
                        unfocusedTextColor = black,
                        focusedTextColor = black,
                        focusedIndicatorColor = brightOrange,
                        unfocusedIndicatorColor = deepOrange
                    ),
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions.Default,
                    keyboardActions = KeyboardActions.Default,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Category", style = MaterialTheme.typography.bodyLarge, color = black)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categories.chunked(4).forEach { categoryGroup ->
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            categoryGroup.forEach { category ->
                                FilterChip(
                                    selected = newCategory == category,
                                    onClick = { newCategory = category },
                                    label = {
                                        Text(
                                            text = category,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    leadingIcon = if (newCategory == category) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = softTan,
                                        selectedContainerColor = offWhite,
                                        labelColor = black,
                                        selectedLabelColor = warmBeige
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Urgency", style = MaterialTheme.typography.bodyLarge, color = black)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    urgencyLevels.forEach { urgency ->
                        FilterChip(
                            selected = newUrgency == urgency,
                            onClick = { newUrgency = urgency },
                            label = { Text(urgency) },
                            leadingIcon = if (newUrgency == urgency) {
                                { Icon(Icons.Default.Check, contentDescription = null, tint = Color.White) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = softTan,
                                selectedContainerColor = offWhite,
                                labelColor = black,
                                selectedLabelColor = warmBeige
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Status", style = MaterialTheme.typography.bodyLarge, color = black)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(statusOptions) { status ->
                        FilterChip(
                            selected = newStatus == status,
                            onClick = { newStatus = status },
                            label = { Text(status) },
                            leadingIcon = if (newStatus == status) {
                                { Icon(Icons.Default.Check, contentDescription = null, tint = Color.White) }
                            } else null,
                            modifier = Modifier.fillMaxWidth(),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = softTan,
                                selectedContainerColor = offWhite,
                                labelColor = black,
                                selectedLabelColor = warmBeige
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onUpdateComplaint(complaint.id, newTitle, newStatus, newUrgency, newCategory)
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = deepOrange)
            ) {
                Text("Save", color = black)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text("Cancel", color = black)
            }
        }
    )
}