package com.example.ritik_2

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.ritik_2.ui.theme.FileExplorerActivity
import com.example.ritik_2.ui.theme.Ritik_2Theme

class SmbConnectActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Ritik_2Theme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ServerConnectionScreen { config ->
                        val intent = Intent(this, FileExplorerActivity::class.java).apply {
                            putExtra("ip", config.ip)
                            putExtra("username", config.username)
                            putExtra("password", config.password)
                        }
                        startActivity(intent)
                    }
                }
            }
        }
    }
}

@Composable
fun ServerConnectionScreen(onConnect: (ServerConfig) -> Unit) {
    var ip by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(value = ip, onValueChange = { ip = it }, label = { Text("Server IP") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation())
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            onConnect(ServerConfig(ip, username, password))
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Connect")
        }
    }
}

data class ServerConfig(val ip: String, val username: String, val password: String)
