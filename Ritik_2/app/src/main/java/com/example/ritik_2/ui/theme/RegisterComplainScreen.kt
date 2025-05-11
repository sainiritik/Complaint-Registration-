package com.example.ritik_2.ui.theme

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.ritik_2.ComplaintData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterComplaintScreen(
    onSaveClick: (ComplaintData) -> Unit,
    onResetClick: () -> Unit,
    onViewComplaintsClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // State variables
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedUrgency by remember { mutableStateOf("Medium") }
    var selectedCategory by remember { mutableStateOf("IT") }
    var contactInfo by remember { mutableStateOf("") }
    var attachmentAdded by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showSuccessAnimation by remember { mutableStateOf(false) }
    var expandedSection by remember { mutableStateOf(0) } // 0: none, 1: categories, 2: urgency, 3: more

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val snackbarHostState = remember { SnackbarHostState() }

    // UI Colors
    val primaryColor = Color(0xFF6200EE)
    val secondaryColor = Color(0xFF03DAC5)
    val errorColor = Color(0xFFB00020)
    val surfaceColor = Color(0xFFF5F5F5)
    val darkTextColor = Color(0xFF121212)
    val lightTextColor = Color(0xFF747474)

    // Urgency level colors
    val urgencyColors = mapOf(
        "Low" to Color(0xFF4CAF50),
        "Medium" to Color(0xFFFFC107),
        "High" to Color(0xFFFF5722),
        "Critical" to Color(0xFFF44336)
    )

    // Categories with icons
    val categories = listOf(
        "IT" to Icons.Filled.Computer,
        "HR" to Icons.Filled.People,
        "Facilities" to Icons.Filled.Apartment,
        "Security" to Icons.Filled.Security,
        "Finance" to Icons.Filled.AttachMoney,
        "Others" to Icons.Filled.MoreHoriz
    )

    // Urgency levels
    val urgencyLevels = listOf("Low", "Medium", "High", "Critical")

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        /*topBar = {
            SmallTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "New Complaint",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Badge(
                            containerColor = primaryColor,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text("New", color = Color.White, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { onViewComplaintsClick() }) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "View Complaints"
                        )
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },*/
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(surfaceColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp)
                ) {
                    // Header Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 4.dp
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Submit Your Feedback",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = darkTextColor
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "We value your input and are committed to addressing your concerns promptly.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = lightTextColor
                            )
                        }
                    }

                    // Title Input
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Complaint Title") },
                        placeholder = { Text("Brief title for your complaint") },
                        leadingIcon = { Icon(Icons.Filled.Title, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .focusRequester(focusRequester),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = lightTextColor.copy(alpha = 0.5f),
                            focusedLabelColor = primaryColor
                        ),
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
                    )

                    // Description Input
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        placeholder = { Text("Detailed description of your issue") },
                        leadingIcon = { Icon(Icons.Filled.Description, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = lightTextColor.copy(alpha = 0.5f),
                            focusedLabelColor = primaryColor
                        ),
                        maxLines = 5
                    )

                    // Category Section
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .clickable { expandedSection = if (expandedSection == 1) 0 else 1 },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Category,
                                        contentDescription = null,
                                        tint = primaryColor
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Column {
                                        Text(
                                            text = "Category",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Medium
                                        )

                                        Text(
                                            text = selectedCategory,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = lightTextColor
                                        )
                                    }
                                }

                                IconButton(onClick = { expandedSection = if (expandedSection == 1) 0 else 1 }) {
                                    Icon(
                                        imageVector = if (expandedSection == 1) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                        contentDescription = "Expand"
                                    )
                                }
                            }

                            AnimatedVisibility(
                                visible = expandedSection == 1,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column(modifier = Modifier.padding(top = 8.dp)) {
                                    LazyRow(
                                        content = {
                                            categories.forEach { (category, icon) ->
                                                item {
                                                    Column(
                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                        modifier = Modifier
                                                            .padding(end = 16.dp)
                                                            .clickable { selectedCategory = category }
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(56.dp)
                                                                .background(
                                                                    if (selectedCategory == category) primaryColor else lightTextColor.copy(
                                                                        alpha = 0.1f
                                                                    ),
                                                                    CircleShape
                                                                ),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(
                                                                imageVector = icon,
                                                                contentDescription = category,
                                                                tint = if (selectedCategory == category) Color.White else darkTextColor
                                                            )
                                                        }

                                                        Spacer(modifier = Modifier.height(4.dp))

                                                        Text(
                                                            text = category,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = if (selectedCategory == category) primaryColor else darkTextColor
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Urgency Section
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .clickable { expandedSection = if (expandedSection == 2) 0 else 2 },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Warning,
                                        contentDescription = null,
                                        tint = urgencyColors[selectedUrgency] ?: primaryColor
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Column {
                                        Text(
                                            text = "Urgency Level",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Medium
                                        )

                                        Text(
                                            text = selectedUrgency,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = urgencyColors[selectedUrgency] ?: lightTextColor
                                        )
                                    }
                                }

                                IconButton(onClick = { expandedSection = if (expandedSection == 2) 0 else 2 }) {
                                    Icon(
                                        imageVector = if (expandedSection == 2) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                        contentDescription = "Expand"
                                    )
                                }
                            }

                            AnimatedVisibility(
                                visible = expandedSection == 2,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column(modifier = Modifier.padding(top = 8.dp)) {
                                    Slider(
                                        value = urgencyLevels.indexOf(selectedUrgency).toFloat(),
                                        onValueChange = { selectedUrgency = urgencyLevels[it.toInt()] },
                                        valueRange = 0f..3f,
                                        steps = 2,
                                        colors = SliderDefaults.colors(
                                            thumbColor = urgencyColors[selectedUrgency] ?: primaryColor,
                                            activeTrackColor = urgencyColors[selectedUrgency] ?: primaryColor,
                                            inactiveTrackColor = lightTextColor.copy(alpha = 0.2f)
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        urgencyLevels.forEach { level ->
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier.clickable { selectedUrgency = level }
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(12.dp)
                                                        .background(
                                                            urgencyColors[level] ?: Color.Gray,
                                                            CircleShape
                                                        )
                                                )

                                                Text(
                                                    text = level,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = if (selectedUrgency == level) urgencyColors[level] ?: darkTextColor else lightTextColor,
                                                    fontWeight = if (selectedUrgency == level) FontWeight.Bold else FontWeight.Normal
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Additional Information Section
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .clickable { expandedSection = if (expandedSection == 3) 0 else 3 },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.Info,
                                        contentDescription = null,
                                        tint = primaryColor
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = "Additional Information",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                IconButton(onClick = { expandedSection = if (expandedSection == 3) 0 else 3 }) {
                                    Icon(
                                        imageVector = if (expandedSection == 3) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                        contentDescription = "Expand"
                                    )
                                }
                            }

                            AnimatedVisibility(
                                visible = expandedSection == 3,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column(modifier = Modifier.padding(top = 8.dp)) {
                                    OutlinedTextField(
                                        value = contactInfo,
                                        onValueChange = { contactInfo = it },
                                        label = { Text("Contact Information") },
                                        placeholder = { Text("Phone or alternate email (optional)") },
                                        leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 16.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = primaryColor,
                                            unfocusedBorderColor = lightTextColor.copy(alpha = 0.5f),
                                            focusedLabelColor = primaryColor
                                        )
                                    )

                                    // Attachment Section
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(
                                                width = 1.dp,
                                                color = lightTextColor.copy(alpha = 0.5f),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable {
                                                attachmentAdded = !attachmentAdded
                                                scope.launch {
                                                    if (attachmentAdded) {
                                                        snackbarHostState.showSnackbar(
                                                            message = "File attachment simulation activated",
                                                            duration = SnackbarDuration.Short
                                                        )
                                                    }
                                                }
                                            }
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = if (attachmentAdded) Icons.Filled.AttachFile else Icons.Outlined.CloudUpload,
                                            contentDescription = "Attach File",
                                            tint = if (attachmentAdded) primaryColor else lightTextColor
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = if (attachmentAdded) "1 file attached" else "Add Attachments (optional)",
                                            color = if (attachmentAdded) primaryColor else lightTextColor
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = {
                                title = ""
                                description = ""
                                selectedUrgency = "Medium"
                                selectedCategory = "IT"
                                contactInfo = ""
                                attachmentAdded = false
                                onResetClick()
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, lightTextColor.copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = "Reset",
                                tint = lightTextColor
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text("Clear", color = lightTextColor)
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Button(
                            onClick = {
                                if (title.isBlank() || description.isBlank()) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "Please fill out the required fields",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                } else {
                                    showConfirmDialog = true
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryColor
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Send,
                                contentDescription = "Submit"
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text("Submit")
                        }
                    }
                }

                // Success Animation Overlay
                AnimatedVisibility(
                    visible = showSuccessAnimation,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.7f))
                            .clickable(enabled = false) { },
                        contentAlignment = Alignment.Center
                    ) {
                        val infiniteTransition = rememberInfiniteTransition()
                        val scale by infiniteTransition.animateFloat(
                            initialValue = 0.8f,
                            targetValue = 1.2f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(800),
                                repeatMode = RepeatMode.Reverse
                            )
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Success",
                                tint = Color.Green,
                                modifier = Modifier
                                    .size(100.dp)
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                    }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Complaint Submitted",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Thank you for your feedback",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    )

    // Confirmation Dialog
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            icon = { Icon(Icons.Filled.HelpOutline, contentDescription = null) },
            title = {
                Text(
                    text = "Confirm Submission",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Are you sure you want to submit this complaint?",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Summary of the complaint
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = surfaceColor
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Title: ",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodySmall
                                )

                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Category: ",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodySmall
                                )

                                Text(
                                    text = selectedCategory,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Urgency: ",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodySmall
                                )

                                Text(
                                    text = selectedUrgency,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = urgencyColors[selectedUrgency] ?: lightTextColor
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false

                        // Create complaint data object
                        val complaintData = ComplaintData(
                            title = title,
                            description = description,
                            category = selectedCategory,
                            urgency = selectedUrgency,
                            contactInfo = contactInfo,
                            hasAttachment = attachmentAdded
                        )

                        // Call the save function
                        onSaveClick(complaintData)

                        // Show success animation
                        showSuccessAnimation = true

                        // Clear fields after a delay
                        scope.launch {
                            delay(2500)
                            showSuccessAnimation = false

                            // Reset all fields
                            title = ""
                            description = ""
                            selectedUrgency = "Medium"
                            selectedCategory = "IT"
                            contactInfo = ""
                            attachmentAdded = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Filled.Send, contentDescription = "Submit")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Submit Complaint")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showConfirmDialog = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Filled.Close, contentDescription = "Cancel")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cancel")
                }
            }
        )
    }

    // Focus on the title field when the screen is first shown
    LaunchedEffect(Unit) {
        delay(300)
        focusRequester.requestFocus()
    }
}