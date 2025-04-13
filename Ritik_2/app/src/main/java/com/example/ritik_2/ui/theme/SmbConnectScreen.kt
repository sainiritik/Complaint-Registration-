package com.example.ritik_2.ui.theme

import android.os.Environment
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.ritik_2.SmbConnect
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun SmbConnectScreen(smbConnect: SmbConnect) {
    val scope = rememberCoroutineScope()
    var ip by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var files by remember { mutableStateOf(listOf<String>()) }
    val context = LocalContext.current

    Column(modifier = Modifier.padding(36.dp)) {
        OutlinedTextField(value = ip, onValueChange = { ip = it }, label = { Text("Server IP") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(26.dp))

        Button(onClick = {
            scope.launch {
                try {
                    files = smbConnect.listFiles(ip, username, password)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }) {
            Text("Connect & List Files")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(files) { fileName ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(fileName, modifier = Modifier.weight(1f))

                    Button(onClick = {
                        scope.launch {
                            val destinationFile = File(
                                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                                fileName
                            )
                            smbConnect.downloadFile(ip, username, password, fileName, destinationFile)
                        }
                    }) {
                        Text("Download")
                    }

                    Button(onClick = {
                        scope.launch {
                            smbConnect.deleteFile(ip, username, password, fileName)
                            files = smbConnect.listFiles(ip, username, password)
                        }
                    }) {
                        Text("Delete")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            scope.launch {
                val testFile = File(context.filesDir, "example.txt").apply {
                    writeText("Hello SMB World!")
                }
                smbConnect.uploadFile(ip, username, password, "", testFile)
                files = smbConnect.listFiles(ip, username, password)
            }
        }) {
            Text("Upload Test File")
        }

        Button(onClick = {
            scope.launch {
                smbConnect.createDirectory(ip, username, password, "NewSharedFolder")
                files = smbConnect.listFiles(ip, username, password)
            }
        }) {
            Text("Create New Folder")
        }
    }
}
