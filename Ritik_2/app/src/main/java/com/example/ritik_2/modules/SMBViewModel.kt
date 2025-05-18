package com.example.ritik_2.modules

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jcifs.CIFSContext
import jcifs.config.PropertyConfiguration
import jcifs.context.BaseContext
import jcifs.smb.NtlmPasswordAuthenticator
import jcifs.smb.SmbException
import jcifs.smb.SmbFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Properties

data class SMBFileItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: Long
) {
    val formattedSize: String
        get() {
            return when {
                size < 1024 -> "$size B"
                size < 1024 * 1024 -> "${size / 1024} KB"
                size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)} MB"
                else -> "${size / (1024 * 1024 * 1024)} GB"
            }
        }

    val formattedDate: String
        get() {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            return formatter.format(Date(lastModified))
        }
}

data class SMBUiState(
    val isConnected: Boolean = false,
    val isLoading: Boolean = false,
    val currentServer: String = "",
    val currentShare: String = "",
    val currentPath: String = "",
    val fileList: List<SMBFileItem> = emptyList(),
    val showConnectionDialog: Boolean = false,
    val serverAddress: String = "",
    val username: String = "",
    val password: String = "",
    val shareName: String = ""
)

class SMBViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SMBUiState())
    val uiState: StateFlow<SMBUiState> = _uiState.asStateFlow()

    private val _errorMessages = MutableSharedFlow<String>()
    val errorMessages: SharedFlow<String> = _errorMessages

    private var cifsContext: CIFSContext? = null
    private val pathStack = mutableListOf<String>()

    // Connection Dialog Functions
    fun showConnectionDialog() {
        _uiState.update { it.copy(showConnectionDialog = true) }
    }

    fun hideConnectionDialog() {
        _uiState.update { it.copy(showConnectionDialog = false) }
    }

    fun updateServerAddress(address: String) {
        _uiState.update { it.copy(serverAddress = address) }
    }

    fun updateUsername(username: String) {
        _uiState.update { it.copy(username = username) }
    }

    fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun updateShareName(share: String) {
        _uiState.update { it.copy(shareName = share) }
    }

    // SMB Connection Functions
    suspend fun connectToServer() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, showConnectionDialog = false) }

                val server = _uiState.value.serverAddress
                val username = _uiState.value.username
                val password = _uiState.value.password
                val share = _uiState.value.shareName

                // Create SMB context
                withContext(Dispatchers.IO) {
                    val props = Properties()
                    props.setProperty("jcifs.smb.client.responseTimeout", "30000")
                    props.setProperty("jcifs.smb.client.soTimeout", "35000")
                    props.setProperty("jcifs.smb.client.connTimeout", "60000")

                    val baseContext = BaseContext(PropertyConfiguration(props))
                    cifsContext = baseContext.withCredentials(
                        NtlmPasswordAuthenticator(null, username, password)
                    )

                    // Try to connect
                    val url = "smb://$server/$share/"
                    val smbFile = SmbFile(url, cifsContext)
                    smbFile.connect()

                    // Clear path stack and add root
                    pathStack.clear()
                    pathStack.add("")

                    // List files
                    val fileList = listFiles(server, share, "")

                    _uiState.update {
                        it.copy(
                            isConnected = true,
                            isLoading = false,
                            currentServer = server,
                            currentShare = share,
                            currentPath = "",
                            fileList = fileList
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("SMBViewModel", "Connection error", e)
                _uiState.update { it.copy(isLoading = false) }
                _errorMessages.emit("Connection error: ${e.message}")
            }
        }
    }

    fun navigateToDirectory(directoryName: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                val currentPath = if (_uiState.value.currentPath.isEmpty()) {
                    directoryName
                } else {
                    "${_uiState.value.currentPath}/$directoryName"
                }

                pathStack.add(currentPath)

                val fileList = listFiles(
                    _uiState.value.currentServer,
                    _uiState.value.currentShare,
                    currentPath
                )

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentPath = currentPath,
                        fileList = fileList
                    )
                }
            } catch (e: Exception) {
                Log.e("SMBViewModel", "Navigation error", e)
                _uiState.update { it.copy(isLoading = false) }
                _errorMessages.emit("Navigation error: ${e.message}")
            }
        }
    }

    fun navigateUp() {
        viewModelScope.launch {
            try {
                if (pathStack.size <= 1) {
                    return@launch
                }

                _uiState.update { it.copy(isLoading = true) }

                // Remove current path
                pathStack.removeAt(pathStack.size - 1)

                // Get parent path
                val parentPath = pathStack.last()

                val fileList = listFiles(
                    _uiState.value.currentServer,
                    _uiState.value.currentShare,
                    parentPath
                )

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentPath = parentPath,
                        fileList = fileList
                    )
                }
            } catch (e: Exception) {
                Log.e("SMBViewModel", "Navigation error", e)
                _uiState.update { it.copy(isLoading = false) }
                _errorMessages.emit("Navigation error: ${e.message}")
            }
        }
    }

    fun refreshFiles() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                val fileList = listFiles(
                    _uiState.value.currentServer,
                    _uiState.value.currentShare,
                    _uiState.value.currentPath
                )

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        fileList = fileList
                    )
                }
            } catch (e: Exception) {
                Log.e("SMBViewModel", "Refresh error", e)
                _uiState.update { it.copy(isLoading = false) }
                _errorMessages.emit("Refresh error: ${e.message}")
            }
        }
    }

    private suspend fun listFiles(server: String, share: String, path: String): List<SMBFileItem> {
        return withContext(Dispatchers.IO) {
            try {
                val url = "smb://$server/$share/${if (path.isNotEmpty()) "$path/" else ""}"
                val smbFile = SmbFile(url, cifsContext)

                smbFile.listFiles().map { file ->
                    SMBFileItem(
                        name = file.name.removeSuffix("/"),
                        path = file.path,
                        isDirectory = file.isDirectory,
                        size = if (file.isDirectory) 0 else file.length(),
                        lastModified = file.lastModified()
                    )
                }.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
            } catch (e: SmbException) {
                Log.e("SMBViewModel", "List files error", e)
                throw e
            }
        }
    }

    suspend fun downloadFile(fileItem: SMBFileItem, context: Context): Uri? {
        return withContext(Dispatchers.IO) {
            try {
                val url = "smb://${_uiState.value.currentServer}/${_uiState.value.currentShare}/${_uiState.value.currentPath}/${fileItem.name}"
                val smbFile = SmbFile(url, cifsContext)

                // Create local file
                val downloadsDir = File(context.cacheDir, "smb_downloads")
                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs()
                }

                val localFile = File(downloadsDir, fileItem.name)

                // Download file
                val inputStream = smbFile.inputStream
                val outputStream = FileOutputStream(localFile)

                inputStream.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }

                // Create content URI using FileProvider
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    localFile
                )

                viewModelScope.launch {
                    _errorMessages.emit("File downloaded: ${fileItem.name}")
                }

                return@withContext uri
            } catch (e: Exception) {
                Log.e("SMBViewModel", "Download error", e)
                viewModelScope.launch {
                    _errorMessages.emit("Download error: ${e.message}")
                }
                null
            }
        }
    }
}