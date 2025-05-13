//package com.example.ritik_2.modules
//
//import androidx.compose.animation.AnimatedVisibility
//import androidx.compose.animation.core.animateFloatAsState
//import androidx.compose.animation.core.tween
//import androidx.compose.animation.fadeIn
//import androidx.compose.animation.fadeOut
//import androidx.compose.animation.slideInVertically
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.itemsIndexed
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.*
//import androidx.compose.material.icons.outlined.Delete
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.unit.dp
//import com.example.ritik_2.ComplaintWithId
//import kotlinx.coroutines.flow.StateFlow
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun ViewComplaintsScreen(
//    complaints: StateFlow<List<ComplaintWithId>>,
//    onBackClick: () -> Unit,
//    onDeleteClick: (String) -> Unit
//) {
//    val complaintsList by complaints.collectAsState()
//    var selectedComplaintId by remember { mutableStateOf<String?>(null) }
//    var showDeleteDialog by remember { mutableStateOf(false) }
//    var complaintToDelete by remember { mutableStateOf<String?>(null) }
//
//    val primaryColor = Color(0xFF6200EE)
//    val surfaceColor = Color(0xFFF5F5F5)
//    val lightTextColor = Color(0xFF747474)
//
//    // Urgency level colors
//    val urgencyColors = mapOf(
//        "Low" to Color(0xFF4CAF50),
//        "Medium" to Color(0xFFFFC107),
//        "High" to Color(0xFFFF5722),
//        "Critical" to Color(0xFFF44336)
//    )
//
//    // Category icons
//    val categoryIcons = mapOf(
//        "IT" to Icons.Filled.Computer,
//        "HR" to Icons.Filled.People,
//        "Facilities" to Icons.Filled.Apartment,
//        "Security" to Icons.Filled.Security,
//        "Finance" to Icons.Filled.AttachMoney,
//        "Others" to Icons.Filled.MoreHoriz
//    )
//
//    // Status colors
//    val statusColors = mapOf(
//        "Open" to Color(0xFF2196F3),
//        "In Progress" to Color(0xFFFF9800),
//        "Resolved" to Color(0xFF4CAF50),
//        "Closed" to Color(0xFF9E9E9E)
//    )
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = {
//                    Text(
//                        text = "My Complaints",
//                        style = MaterialTheme.typography.headlineSmall.copy(
//                            fontWeight = FontWeight.Bold
//                        )
//                    )
//                },
//                navigationIcon = {
//                    IconButton(onClick = onBackClick) {
//                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
//                    }
//                },
//                actions = {
//                    Badge(
//                        containerColor = primaryColor,
//                        modifier = Modifier.padding(end = 16.dp)
//                    ) {
//                        Text(
//                            text = complaintsList.size.toString(),
//                            color = Color.White,
//                            style = MaterialTheme.typography.labelSmall
//                        )
//                    }
//                },
//                colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor = MaterialTheme.colorScheme.surface
//                )
//            )
//        }
//    ) { paddingValues ->
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(paddingValues)
//                .background(surfaceColor)
//        ) {
//            if (complaintsList.isEmpty()) {
//                // Empty state
//                Column(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(16.dp),
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                    verticalArrangement = Arrangement.Center
//                ) {
//                    Icon(
//                        imageVector = Icons.Filled.Inbox,
//                        contentDescription = null,
//                        modifier = Modifier.size(100.dp),
//                        tint = lightTextColor.copy(alpha = 0.5f)
//                    )
//
//                    Spacer(modifier = Modifier.height(16.dp))
//
//                    Text(
//                        text = "No complaints yet",
//                        style = MaterialTheme.typography.titleLarge,
//                        color = lightTextColor
//                    )
//
//                    Spacer(modifier = Modifier.height(8.dp))
//
//                    Text(
//                        text = "Your submitted complaints will appear here",
//                        style = MaterialTheme.typography.bodyMedium,
//                        color = lightTextColor.copy(alpha = 0.7f),
//                        textAlign = TextAlign.Center
//                    )
//                }
//            } else {
//                LazyColumn(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(horizontal = 16.dp, vertical = 8.dp)
//                ) {
//                    itemsIndexed(complaintsList) { index, complaint ->
//                        val isExpanded = selectedComplaintId == complaint.id
//                        val animatedProgress by animateFloatAsState(
//                            targetValue = if (isExpanded) 1f else 0f,
//                            animationSpec = tween(durationMillis = 300)
//                        )
//
//                        Card(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(vertical = 8.dp)
//                                .clickable {
//                                    selectedComplaintId = if (isExpanded) null else complaint.id
//                                },
//                            shape = RoundedCornerShape(16.dp),
//                            elevation = CardDefaults.cardElevation(
//                                defaultElevation = 4.dp
//                            ),
//                            colors = CardDefaults.cardColors(
//                                containerColor = MaterialTheme.colorScheme.surface
//                            )
//                        ) {
//                            Column(
//                                modifier = Modifier.padding(16.dp)
//                            ) {
//                                // Top Section with category, date, and status
//                                Row(
//                                    modifier = Modifier.fillMaxWidth(),
//                                    horizontalArrangement = Arrangement.SpaceBetween,
//                                    verticalAlignment = Alignment.CenterVertically
//                                ) {
//                                    // Category with icon
//                                    Row(
//                                        verticalAlignment = Alignment.CenterVertically,
//                                        modifier = Modifier
//                                            .clip(RoundedCornerShape(16.dp))
//                                            .background(Color(0xFFEDE7F6))
//                                            .padding(horizontal = 8.dp, vertical = 4.dp)
//                                    ) {
//                                        Icon(
//                                            imageVector = categoryIcons[complaint.category] ?: Icons.Filled.Help,
//                                            contentDescription = null,
//                                            tint = primaryColor,
//                                            modifier = Modifier.size(16.dp)
//                                        )
//
//                                        Spacer(modifier = Modifier.width(4.dp))
//
//                                        Text(
//                                            text = complaint.category,
//                                            style = MaterialTheme.typography.labelSmall,
//                                            color = primaryColor
//                                        )
//                                    }
//
//                                    // Date
//                                    Text(
//                                        text = complaint.getFormattedDate(),
//                                        style = MaterialTheme.typography.labelSmall,
//                                        color = lightTextColor
//                                    )
//                                }
//
//                                Spacer(modifier = Modifier.height(8.dp))
//
//                                // Title and status badge
//                                Row(
//                                    modifier = Modifier.fillMaxWidth(),
//                                    horizontalArrangement = Arrangement.SpaceBetween,
//                                    verticalAlignment = Alignment.CenterVertically
//                                ) {
//                                    Text(
//                                        text = complaint.title,
//                                        style = MaterialTheme.typography.titleMedium,
//                                        fontWeight = FontWeight.Bold,
//                                        maxLines = 1,
//                                        overflow = TextOverflow.Ellipsis,
//                                        modifier = Modifier.weight(1f)
//                                    )
//
//                                    // Status pill
//                                    Box(
//                                        modifier = Modifier
//                                            .clip(RoundedCornerShape(16.dp))
//                                            .background(statusColors[complaint.status] ?: Color.Gray)
//                                            .padding(horizontal = 8.dp, vertical = 4.dp)
//                                    ) {
//                                        Text(
//                                            text = complaint.status,
//                                            style = MaterialTheme.typography.labelSmall,
//                                            color = Color.White
//                                        )
//                                    }
//                                }
//
//                                Spacer(modifier = Modifier.height(8.dp))
//
//                                // Urgency indicator
//                                Row(
//                                    verticalAlignment = Alignment.CenterVertically
//                                ) {
//                                    Icon(
//                                        imageVector = Icons.Filled.Flag,
//                                        contentDescription = null,
//                                        tint = urgencyColors[complaint.urgency] ?: Color.Gray,
//                                        modifier = Modifier.size(16.dp)
//                                    )
//
//                                    Spacer(modifier = Modifier.width(4.dp))
//
//                                    Text(
//                                        text = "${complaint.urgency} Priority",
//                                        style = MaterialTheme.typography.bodySmall,
//                                        color = urgencyColors[complaint.urgency] ?: Color.Gray
//                                    )
//
//                                    if (complaint.hasAttachment) {
//                                        Spacer(modifier = Modifier.width(8.dp))
//
//                                        Icon(
//                                            imageVector = Icons.Filled.AttachFile,
//                                            contentDescription = null,
//                                            tint = lightTextColor,
//                                            modifier = Modifier.size(16.dp)
//                                        )
//                                    }
//                                }
//
//                                // Expanded content
//                                AnimatedVisibility(
//                                    visible = isExpanded,
//                                    enter = fadeIn() + slideInVertically(),
//                                    exit = fadeOut()
//                                ) {
//                                    Column(
//                                        modifier = Modifier.padding(top = 16.dp)
//                                    ) {
//                                        Divider()
//
//                                        Spacer(modifier = Modifier.height(16.dp))
//
//                                        Text(
//                                            text = "Description",
//                                            style = MaterialTheme.typography.titleSmall,
//                                            fontWeight = FontWeight.Bold
//                                        )
//
//                                        Spacer(modifier = Modifier.height(4.dp))
//
//                                        Text(
//                                            text = complaint.description,
//                                            style = MaterialTheme.typography.bodyMedium
//                                        )
//
//                                        if (complaint.contactInfo.isNotBlank()) {
//                                            Spacer(modifier = Modifier.height(16.dp))
//
//                                            Text(
//                                                text = "Contact Information",
//                                                style = MaterialTheme.typography.titleSmall,
//                                                fontWeight = FontWeight.Bold
//                                            )
//
//                                            Spacer(modifier = Modifier.height(4.dp))
//
//                                            Text(
//                                                text = complaint.contactInfo,
//                                                style = MaterialTheme.typography.bodyMedium
//                                            )
//                                        }
//
//                                        Spacer(modifier = Modifier.height(16.dp))
//
//                                        // Action buttons
//                                        Row(
//                                            modifier = Modifier.fillMaxWidth(),
//                                            horizontalArrangement = Arrangement.End
//                                        ) {
//                                            OutlinedButton(
//                                                onClick = {
//                                                    complaintToDelete = complaint.id
//                                                    showDeleteDialog = true
//                                                },
//                                                colors = ButtonDefaults.outlinedButtonColors(
//                                                    contentColor = Color.Red
//                                                )
//                                            ) {
//                                                Icon(
//                                                    imageVector = Icons.Outlined.Delete,
//                                                    contentDescription = "Delete"
//                                                )
//
//                                                Spacer(modifier = Modifier.width(4.dp))
//
//                                                Text("Delete")
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//
//            // Delete confirmation dialog
//            if (showDeleteDialog) {
//                AlertDialog(
//                    onDismissRequest = { showDeleteDialog = false },
//                    icon = { Icon(Icons.Filled.Warning, contentDescription = null, tint = Color.Red) },
//                    title = { Text("Delete Complaint") },
//                    text = { Text("Are you sure you want to delete this complaint? This action cannot be undone.") },
//                    confirmButton = {
//                        Button(
//                            onClick = {
//                                complaintToDelete?.let { id ->
//                                    onDeleteClick(id)
//                                    if (selectedComplaintId == id) {
//                                        selectedComplaintId = null
//                                    }
//                                }
//                                showDeleteDialog = false
//                                complaintToDelete = null
//                            },
//                            colors = ButtonDefaults.buttonColors(
//                                containerColor = Color.Red
//                            )
//                        ) {
//                            Text("Delete")
//                        }
//                    },
//                    dismissButton = {
//                        OutlinedButton(onClick = { showDeleteDialog = false }) {
//                            Text("Cancel")
//                        }
//                    }
//                )
//            }
//        }
//    }
//}