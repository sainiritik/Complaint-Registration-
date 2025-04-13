package com.example.ritik_2.ui.theme

import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ritik_2.ui.theme.SMBHelper
import jcifs.smb.SmbFile
import kotlinx.coroutines.launch
import java.io.File

import jcifs.CIFSContext
import jcifs.Configuration
import jcifs.context.BaseContext
import jcifs.smb.NtlmPasswordAuthenticator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class FileExplorerActivity : ComponentActivity() {
    private lateinit var smbHelper: SMBHelper
    private lateinit var ip: String
    private lateinit var username: String
    private lateinit var password: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ip = intent.getStringExtra("ip") ?: ""
        username = intent.getStringExtra("username") ?: ""
        password = intent.getStringExtra("password") ?: ""
        smbHelper = SMBHelper()

        setContent {
            Ritik_2Theme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    FileExplorerScreen(ip, username, password, smbHelper)
                }
            }
        }
    }
}

@Composable
fun FileExplorerScreen(ip: String, username: String, password: String, smbHelper: SMBHelper) {
    var files by remember { mutableStateOf<List<SmbFile>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            files = smbHelper.listFiles(ip, username, password)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(files) { file ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { }
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = file.name)
                    Row {
                        TextButton(onClick = {
                            scope.launch {
                                val destFile = File(
                                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                                    file.name
                                )
                                smbHelper.downloadFile(ip, username, password, file.path.removePrefix("smb://$ip/"), destFile)
                            }
                        }) {
                            Text("Download")
                        }
                        TextButton(onClick = {
                            scope.launch {
                                smbHelper.deleteFile(ip, username, password, file.path.removePrefix("smb://$ip/"))
                                files = smbHelper.listFiles(ip, username, password)
                            }
                        }) {
                            Text("Delete")
                        }
                    }
                }
            }
        }

        Button(
            onClick = {
                scope.launch {
                    val testFile = File(Environment.getExternalStorageDirectory(), "test_upload.txt").apply {
                        writeText("Test upload content")
                    }
                    smbHelper.uploadFile(ip, username, password, "", testFile)
                    files = smbHelper.listFiles(ip, username, password)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Upload Test File")
        }
    }
}


class SMBHelper {

    private suspend fun createContext(username: String, password: String): CIFSContext = withContext(Dispatchers.IO) {
        val props = Properties().apply {
            setProperty("jcifs.smb.client.enableSMB2", "true")
            setProperty("jcifs.smb.client.useExtendedSecurity", "true")
        }
        val baseContext = BaseContext(props as Configuration?)
        baseContext.withCredentials(NtlmPasswordAuthenticator("", username, password))
    }

    suspend fun listFiles(ip: String, username: String, password: String): List<SmbFile> = withContext(Dispatchers.IO) {
        val context = createContext(username, password)
        val path = "smb://$ip/"
        val dir = SmbFile(path, context)
        dir.listFiles()?.toList() ?: emptyList()
    }

    suspend fun uploadFile(ip: String, username: String, password: String, remoteDir: String, localFile: File) = withContext(Dispatchers.IO) {
        val context = createContext(username, password)
        val targetPath = "smb://$ip/$remoteDir/${localFile.name}"
        val smbFile = SmbFile(targetPath, context)

        localFile.inputStream().use { inputStream ->
            smbFile.outputStream.use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }

    suspend fun downloadFile(ip: String, username: String, password: String, remoteFilePath: String, localFile: File) = withContext(Dispatchers.IO) {
        val context = createContext(username, password)
        val smbFile = SmbFile("smb://$ip/$remoteFilePath", context)

        smbFile.inputStream.use { inputStream ->
            localFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }

    suspend fun deleteFile(ip: String, username: String, password: String, remoteFilePath: String) = withContext(Dispatchers.IO) {
        val context = createContext(username, password)
        val smbFile = SmbFile("smb://$ip/$remoteFilePath", context)
        smbFile.delete()
    }
}