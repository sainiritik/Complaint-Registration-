package com.example.ritik_2.ui.theme

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun ProfileScreen(
    profileImageUrl: Uri?,
    name: String,
    email: String,
    phoneNumber: String,
    designation: String,
    onLogoutClick: () -> Unit,
    onEditClick: (String, String) -> Unit,
    onChangeProfilePic: () -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedField by remember { mutableStateOf("") }
    var selectedValue by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 100.dp, start = 20.dp, end = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Picture
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(profileImageUrl ?: "")
                .crossfade(true)
                .build(),
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .clickable { onChangeProfilePic() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Profile Info Fields (Clickable)
        ProfileField(label = "Name", value = name) { showEditDialog = true; selectedField = "Name"; selectedValue = name }
        ProfileField(label = "Email", value = email) { showEditDialog = true; selectedField = "Email"; selectedValue = email }
        ProfileField(label = "Phone", value = phoneNumber) { showEditDialog = true; selectedField = "Phone"; selectedValue = phoneNumber }
        ProfileField(label = "Designation", value = designation) { showEditDialog = true; selectedField = "Designation"; selectedValue = designation }

        Spacer(modifier = Modifier.height(24.dp))

        // Logout Button
        Button(
            onClick = onLogoutClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) {
            Text(text = "Logout", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }

    // Mini Window for Editing
    if (showEditDialog) {
        EditDialog(
            field = selectedField,
            value = selectedValue,
            onSave = { newValue ->
                onEditClick(selectedField, newValue)
                showEditDialog = false
            },
            onClose = { showEditDialog = false }
        )
    }
}

@Composable
fun ProfileField(label: String, value: String, onEditClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .clickable { onEditClick() },
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "$label:", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
    }
}

@Composable
fun EditDialog(field: String, value: String, onSave: (String) -> Unit, onClose: () -> Unit) {
    var newValue by remember { mutableStateOf(value) }

    AlertDialog(
        onDismissRequest = { onClose() },
        title = { Text("Edit $field", fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = newValue,
                onValueChange = { newValue = it },
                label = { Text("Enter new $field") }
            )
        },
        confirmButton = {
            Button(onClick = { onSave(newValue) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = { onClose() }) {
                Text("Close", color = Color.Red)
            }
        }
    )
}
