package com.example.ritik_2.ui.theme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ritik_2.modules.UserProfile
import com.example.ritik_2.ui.theme.ui.theme.ITConnectTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    userProfile: UserProfile? = null,
    isLoading: Boolean = false,
    onLogout: () -> Unit,
    onCardClick: (Int) -> Unit,
    onProfileClick: () -> Unit
) {
    val userName = userProfile?.name ?: "IT Engineer"
    val jobTitle = userProfile?.jobTitle ?: "IT Professional"
    var showProfileDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "IT Connect",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Loading indicator
            AnimatedVisibility(
                visible = isLoading,
                enter = fadeIn(animationSpec = spring()),
                exit = fadeOut(animationSpec = spring())
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(60.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Main content
            AnimatedVisibility(
                visible = !isLoading,
                enter = fadeIn(animationSpec = spring()),
                exit = fadeOut(animationSpec = spring())
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // User Profile Card
                    UserProfileCard(
                        userName = userName,
                        jobTitle = jobTitle,
                        imageUri = userProfile?.imageUrl,
                        experienceYears = userProfile?.experience ?: 0,
                        completedProjects = userProfile?.completedProjects ?: 0,
                        activeProjects = userProfile?.activeProjects ?: 0,
                        onProfileClick = onProfileClick
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Dashboard Title
                    Text(
                        text = "Dashboard",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                    )

                    // Feature Grid
                    FeatureGrid(onCardClick = onCardClick)
                }
            }

            // Profile Dialog
            if (showProfileDialog) {
                ProfileDialog(
                    onDismiss = { showProfileDialog = false },
                    onSeeProfile = {
                        onProfileClick()
                        showProfileDialog = false
                    },
                    onLogout = {
                        onLogout()
                        showProfileDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun UserProfileCard(
    userName: String,
    jobTitle: String,
    imageUri: android.net.Uri?,
    experienceYears: Int,
    completedProjects: Int,
    activeProjects: Int,
    onProfileClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .clickable { onProfileClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Profile Image
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = jobTitle,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Profile",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onProfileClick() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    value = experienceYears,
                    label = "Years",
                    icon = Icons.Outlined.Work
                )
                StatItem(
                    value = completedProjects,
                    label = "Completed",
                    icon = Icons.Outlined.CheckCircle
                )
                StatItem(
                    value = activeProjects,
                    label = "Active",
                    icon = Icons.Outlined.Pending
                )
            }
        }
    }
}

@Composable
fun StatItem(value: Int, label: String, icon: ImageVector) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = value.toString(),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun FeatureGrid(onCardClick: (Int) -> Unit) {
    val features = listOf(
        FeatureItem(1, "Register Complaint", Icons.Outlined.ReportProblem, MaterialTheme.colorScheme.error),
        FeatureItem(2, "View Complaints", Icons.Outlined.List, MaterialTheme.colorScheme.secondary),
        FeatureItem(3, "Settings", Icons.Outlined.Settings, MaterialTheme.colorScheme.tertiary),
        FeatureItem(4, "Help & Support", Icons.Outlined.SupportAgent, MaterialTheme.colorScheme.primary),
        FeatureItem(5, "Knowledge Base", Icons.Outlined.MenuBook, Color(0xFF00796B)),
        FeatureItem(6, "Tech Resources", Icons.Outlined.Code, Color(0xFF6200EA)),
        FeatureItem(7, "Collaboration", Icons.Outlined.Groups, Color(0xFFC51162)),
        FeatureItem(8, "Tech News", Icons.Outlined.NewReleases, Color(0xFFFF6F00))
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(features) { feature ->
            FeatureCard(
                title = feature.title,
                icon = feature.icon,
                color = feature.color,
                onClick = { onCardClick(feature.id) }
            )
        }
    }
}

@Composable
fun FeatureCard(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

data class FeatureItem(
    val id: Int,
    val title: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun ProfileDialog(onDismiss: () -> Unit, onSeeProfile: () -> Unit, onLogout: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Profile Options") },
        text = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSeeProfile() }
                        .padding(vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        "View Profile",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Divider()

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLogout() }
                        .padding(vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        "Logout",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewMainScreen() {
    val sampleProfile = UserProfile(
        id = "sample",
        name = "John Doe",
        email = "john.doe@example.com",
        jobTitle = "Senior IT Engineer",
        experience = 5,
        completedProjects = 23,
        activeProjects = 3
    )

    ITConnectTheme {
        MainScreen(
            userProfile = sampleProfile,
            isLoading = false,
            onLogout = {},
            onCardClick = {},
            onProfileClick = {}
        )
    }
}