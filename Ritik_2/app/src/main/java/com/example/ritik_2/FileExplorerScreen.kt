package com.example.ritik_2

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import jcifs.smb.SmbFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

// State holder for the screen
data class FileExplorerState(
    val currentPath: String = "",
    val files: List<SmbFile> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val parentPath: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileExplorerScreen(
    ip: String,
    username: String,
    password: String,
    initialPath: String = "smb://$ip/"
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val smbHelper = remember { SMBHelper() }

    var state by remember { mutableStateOf(FileExplorerState(currentPath = initialPath)) }

    // File picker for uploads
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                uploadFile(context, smbHelper, uri, state.currentPath, username, password) { success, message ->
                    if (success) {
                        loadFolderContents(smbHelper, state.currentPath, username, password) { newState ->
                            state = newState
                        }
                        Toast.makeText(context, "Upload successful", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Upload failed: $message", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // Load folder contents on path change
    LaunchedEffect(state.currentPath) {
        loadFolderContents(smbHelper, state.currentPath, username, password) { newState ->
            state = newState
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "SMB Explorer",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                navigationIcon = {
                    state.parentPath?.let {
                        IconButton(onClick = {
                            state = state.copy(currentPath = it, isLoading = true)
                        }) {
                            Icon(Icons.Default.ArrowBack, "Go back")
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {
                        state = state.copy(isLoading = true)
                        scope.launch {
                            loadFolderContents(smbHelper, state.currentPath, username, password) { newState ->
                                state = newState
                            }
                        }
                    }) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { filePickerLauncher.launch("*/*") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Upload, "Upload file")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Path breadcrumb
            Text(
                text = "Path: ${state.currentPath.removePrefix("smb://")}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Error message
            AnimatedVisibility(visible = state.error != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = state.error ?: "",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Loading indicator
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                }
            }

            // File list
            if (state.files.isEmpty() && !state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.FolderOff,
                            contentDescription = "Empty folder",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "This folder is empty",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.files) { file ->
                        FileItem(
                            file = file,
                            onClick = {
                                if (file.isDirectory) {
                                    val newPath = file.url.toString()
                                    state = state.copy(
                                        currentPath = newPath,
                                        parentPath = state.currentPath,
                                        isLoading = true
                                    )
                                } else {
                                    // Handle file click - download/preview
                                    scope.launch {
                                        try {
                                            val fileSize = withContext(Dispatchers.IO) {
                                                file.length() / 1024 // Size in KB
                                            }
                                            Toast.makeText(
                                                context,
                                                "${file.name}: $fileSize KB",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            // Additional file actions can be implemented here
                                        } catch (e: Exception) {
                                            Toast.makeText(
                                                context,
                                                "Error accessing file: ${e.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            },
                            onDelete = {
                                scope.launch {
                                    try {
                                        withContext(Dispatchers.IO) {
                                            smbHelper.deleteFile(file.url.toString(), username, password)
                                        }
                                        loadFolderContents(smbHelper, state.currentPath, username, password) { newState ->
                                            state = newState
                                        }
                                        Toast.makeText(context, "File deleted", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        Toast.makeText(
                                            context,
                                            "Delete failed: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FileItem(
    file: SmbFile,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (file.isDirectory)
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (file.isDirectory) Icons.Default.Folder else Icons.Default.InsertDriveFile,
                    contentDescription = null,
                    tint = if (file.isDirectory)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = file.name.removeSuffix("/"),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (!file.isDirectory) {
                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirm Delete") },
            text = { Text("Are you sure you want to delete '${file.name.removeSuffix("/")}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
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

// Helper functions for loading folder contents
private suspend fun loadFolderContents(
    smbHelper: SMBHelper,
    path: String,
    username: String,
    password: String,
    onComplete: (FileExplorerState) -> Unit
) {
    try {
        val parentPath = getParentPath(path)
        val files = withContext(Dispatchers.IO) {
            smbHelper.listFolderContents(path, username, password)
        }

        onComplete(
            FileExplorerState(
                currentPath = path,
                files = files,
                isLoading = false,
                parentPath = parentPath
            )
        )
    } catch (e: Exception) {
        onComplete(
            FileExplorerState(
                currentPath = path,
                files = emptyList(),
                isLoading = false,
                error = "Failed to load: ${e.message}",
                parentPath = getParentPath(path)
            )
        )
    }
}

private fun getParentPath(path: String): String? {
    val smbPrefix = "smb://"
    if (path == smbPrefix || path.count { it == '/' } <= 3) return null

    val trimmedPath = path.removeSuffix("/")
    val lastSlashIndex = trimmedPath.lastIndexOf('/')
    if (lastSlashIndex <= smbPrefix.length) return null

    return trimmedPath.substring(0, lastSlashIndex + 1)
}

private suspend fun uploadFile(
    context: android.content.Context,
    smbHelper: SMBHelper,
    uri: Uri,
    currentPath: String,
    username: String,
    password: String,
    callback: (Boolean, String?) -> Unit
) {
    try {
        withContext(Dispatchers.IO) {
            val contentResolver = context.contentResolver
            val fileName = getFileName(context, uri) ?: "unknown_file_${System.currentTimeMillis()}"

            contentResolver.openInputStream(uri)?.use { inputStream ->
                val tempFile = File(context.cacheDir, "temp_upload_$fileName")
                tempFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }

                smbHelper.uploadFile("$currentPath$fileName", username, password, tempFile)
                tempFile.delete()
            } ?: throw Exception("Could not open file stream")

            callback(true, null)
        }
    } catch (e: Exception) {
        callback(false, e.message)
    }
}

private fun getFileName(context: android.content.Context, uri: Uri): String? {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    result = it.getString(nameIndex)
                }
            }
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/')
        if (cut != -1) {
            result = result?.substring(cut!! + 1)
        }
    }
    return result
}

// Note: This is a placeholder assuming your SMBHelper class has these methods
// You'll need to make sure your actual SMBHelper implementation has these methods
class SMBHelper {
    suspend fun listFolderContents(path: String, username: String, password: String): List<SmbFile> {
        // Implementation from your existing code
        return emptyList() // Replace with actual implementation
    }

    suspend fun uploadFile(destination: String, username: String, password: String, file: File) {
        // Implementation from your existing code
    }

    suspend fun deleteFile(path: String, username: String, password: String) {
        // Implementation from your existing code
    }
}