package com.example.ritik_2

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.ritik_2.ui.theme.Ritik_2Theme
import com.example.ritik_2.ui.theme.SMBHelper
import jcifs.smb.SmbFile
import kotlinx.coroutines.launch

class FolderDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val ip = intent.getStringExtra("ip") ?: ""
        val path = intent.getStringExtra("path") ?: ""
        val username = intent.getStringExtra("username") ?: ""
        val password = intent.getStringExtra("password") ?: ""

        val smbHelper = SMBHelper()

        setContent {
            Ritik_2Theme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    FolderFilesScreen(ip, path, username, password, smbHelper)
                }
            }
        }
    }
}

@Composable
fun FolderFilesScreen(ip: String, path: String, username: String, password: String, smbHelper: SMBHelper) {
    val context = LocalContext.current
    var files by remember { mutableStateOf<List<SmbFile>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                files = smbHelper.listFolderContents(ip, path, username, password)
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to load folder: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Files in Folder", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))

        LazyColumn {
            items(files) { file ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFECECEC))
                ) {
                    Text(file.name, modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}