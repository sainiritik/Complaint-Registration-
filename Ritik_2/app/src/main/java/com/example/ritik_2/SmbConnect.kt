package com.example.ritik_2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.ritik_2.ui.theme.SmbConnectScreen
import com.example.ritik_2.ui.theme.ui.theme.Ritik_2Theme
import jcifs.CIFSContext
import jcifs.config.PropertyConfiguration
import jcifs.context.BaseContext
import jcifs.smb.NtlmPasswordAuthenticator
import jcifs.smb.SmbFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

class SmbConnect {

    private suspend fun createContext(username: String, password: String): CIFSContext = withContext(Dispatchers.IO) {
        val prop = Properties().apply {
            setProperty("jcifs.smb.client.enableSMB2", "true")
            setProperty("jcifs.smb.client.useExtendedSecurity", "true")
        }
        val baseContext: BaseContext = BaseContext(PropertyConfiguration(prop))
        baseContext.withCredentials(NtlmPasswordAuthenticator("", username, password))
    }

    suspend fun listFiles(ip: String, username: String, password: String): List<String> = withContext(Dispatchers.IO) {
        val context = createContext(username, password)
        val path = "smb://$ip/"
        val dir = SmbFile(path, context)
        dir.listFiles().map { it.name }
    }

    suspend fun uploadFile(ip: String, username: String, password: String, remotePath: String, localFile: File): Boolean =
        withContext(Dispatchers.IO) {
            val context = createContext(username, password)
            val smbFile = SmbFile("smb://$ip/$remotePath/${localFile.name}", context)
            smbFile.outputStream.use { smbOut ->
                FileInputStream(localFile).use { localIn ->
                    localIn.copyTo(smbOut)
                }
            }
            true
        }

    suspend fun downloadFile(ip: String, username: String, password: String, remoteFilePath: String, destination: File): Boolean =
        withContext(Dispatchers.IO) {
            val context = createContext(username, password)
            val smbFile = SmbFile("smb://$ip/$remoteFilePath", context)
            smbFile.inputStream.use { smbIn ->
                FileOutputStream(destination).use { localOut ->
                    smbIn.copyTo(localOut)
                }
            }
            true
        }

    suspend fun deleteFile(ip: String, username: String, password: String, remoteFilePath: String): Boolean =
        withContext(Dispatchers.IO) {
            val context = createContext(username, password)
            val smbFile = SmbFile("smb://$ip/$remoteFilePath", context)
            smbFile.delete()
            true
        }

    suspend fun createDirectory(ip: String, username: String, password: String, newFolderPath: String): Boolean =
        withContext(Dispatchers.IO) {
            val context = createContext(username, password)
            val dir = SmbFile("smb://$ip/$newFolderPath/", context)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            true
        }
}

class SmbConnectActivity : ComponentActivity() {

    private lateinit var smbConnect: SmbConnect

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        smbConnect = SmbConnect()

        setContent {
            Ritik_2Theme {
                SmbConnectScreen(smbConnect = smbConnect)
            }
        }
    }
}
