package com.example.ritik_2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.example.ritik_2.modules.SMBFileItem
import com.example.ritik_2.modules.SMBViewModel
import com.example.ritik_2.ui.theme.ui.theme.Ritik_2Theme
import kotlinx.coroutines.launch

class SMBActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Ritik_2Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SMBExplorerScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SMBExplorerScreen(
    viewModel: SMBViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.errorMessages.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("SMB Explorer") },
                actions = {
                    IconButton(onClick = { viewModel.refreshFiles() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = { viewModel.navigateUp() }) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = "Navigate Up")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showConnectionDialog() }) {
                Icon(Icons.Default.Add, contentDescription = "Connect to SMB")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Connection status
            ConnectionStatusBar(
                isConnected = uiState.isConnected,
                currentServer = uiState.currentServer,
                currentShare = uiState.currentShare,
                currentPath = uiState.currentPath
            )

            // Main content
            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.fileList.isEmpty() && uiState.isConnected -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No files found in this directory")
                    }
                }
                !uiState.isConnected -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Not connected to any SMB share")
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.showConnectionDialog() }) {
                                Text("Connect to Server")
                            }
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.fileList) { fileItem ->
                            FileListItem(
                                fileItem = fileItem,
                                onClick = {
                                    if (fileItem.isDirectory) {
                                        viewModel.navigateToDirectory(fileItem.name)
                                    } else {
                                        scope.launch {
                                            viewModel.downloadFile(fileItem, context)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // Connection Dialog
        if (uiState.showConnectionDialog) {
            ConnectionDialog(
                serverAddress = uiState.serverAddress,
                username = uiState.username,
                password = uiState.password,
                shareName = uiState.shareName,
                onServerAddressChange = { viewModel.updateServerAddress(it) },
                onUsernameChange = { viewModel.updateUsername(it) },
                onPasswordChange = { viewModel.updatePassword(it) },
                onShareNameChange = { viewModel.updateShareName(it) },
                onDismiss = { viewModel.hideConnectionDialog() },
                onConnect = {
                    scope.launch {
                        viewModel.connectToServer()
                    }
                }
            )
        }
    }
}

@Composable
fun ConnectionStatusBar(
    isConnected: Boolean,
    currentServer: String,
    currentShare: String,
    currentPath: String
) {
    Surface(
        color = if (isConnected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.errorContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isConnected) Icons.Default.CloudDone else Icons.Default.CloudOff,
                contentDescription = if (isConnected) "Connected" else "Disconnected",
                tint = if (isConnected) Color.Green else Color.Red
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isConnected) "\\\\$currentServer\\$currentShare\\$currentPath"
                else "Not connected",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun FileListItem(
    fileItem: SMBFileItem,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (fileItem.isDirectory) Icons.Default.Folder else Icons.Default.InsertDriveFile,
                contentDescription = if (fileItem.isDirectory) "Folder" else "File",
                tint = if (fileItem.isDirectory) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = fileItem.name,
                    style = MaterialTheme.typography.bodyLarge
                )
                if (!fileItem.isDirectory) {
                    Text(
                        text = fileItem.formattedSize,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            if (fileItem.isDirectory) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Navigate into folder"
                )
            } else {
                IconButton(onClick = onClick) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download file"
                    )
                }
            }
        }
    }
    Divider()
}

@Composable
fun ConnectionDialog(
    serverAddress: String,
    username: String,
    password: String,
    shareName: String,
    onServerAddressChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onShareNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConnect: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Connect to SMB Server") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = serverAddress,
                    onValueChange = onServerAddressChange,
                    label = { Text("Server Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = username,
                    onValueChange = onUsernameChange,
                    label = { Text("Username") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = { Text("Password") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = shareName,
                    onValueChange = onShareNameChange,
                    label = { Text("Share Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConnect
            ) {
                Text("Connect")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}