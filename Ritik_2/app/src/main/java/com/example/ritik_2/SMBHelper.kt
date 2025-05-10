package com.example.ritik_2

import jcifs.smb.NtlmPasswordAuthentication
import jcifs.smb.SmbFile
import jcifs.smb.SmbFileInputStream
import jcifs.smb.SmbFileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.net.MalformedURLException

class SMBHelper {
    /**
     * Lists all files and folders in the specified SMB path.
     *
     * @param path The SMB path to list contents from
     * @param username SMB username
     * @param password SMB password
     * @return List of SmbFile objects representing files and folders
     */
    suspend fun listFolderContents(path: String, username: String, password: String): List<SmbFile> =
        withContext(Dispatchers.IO) {
            try {
                val auth = NtlmPasswordAuthentication(null, username, password)
                val smbFolder = SmbFile(path, auth)

                if (!smbFolder.exists()) {
                    throw Exception("Folder not found: $path")
                }

                if (!smbFolder.isDirectory()) {
                    throw Exception("Path is not a directory: $path")
                }

                val files = smbFolder.listFiles()

                // Sort directories first, then files alphabetically
                files.sortWith(compareBy<SmbFile> { !it.isDirectory() }.thenBy { it.name })

                return@withContext files.toList()
            } catch (e: MalformedURLException) {
                throw Exception("Invalid SMB URL: ${e.message}")
            } catch (e: Exception) {
                throw Exception("SMB error: ${e.message}")
            }
        }

    /**
     * Uploads a file to the specified SMB path.
     *
     * @param destination The full SMB path including filename
     * @param username SMB username
     * @param password SMB password
     * @param file The local file to upload
     */
    suspend fun uploadFile(destination: String, username: String, password: String, file: File) =
        withContext(Dispatchers.IO) {
            try {
                val auth = NtlmPasswordAuthentication(null, username, password)
                val smbFile = SmbFile(destination, auth)

                // Create parent directories if they don't exist
                val parent = smbFile.parent
                val parentDir = SmbFile(parent, auth)
                if (!parentDir.exists()) {
                    parentDir.mkdirs()
                }

                // Upload the file
                FileInputStream(file).use { inputStream ->
                    SmbFileOutputStream(smbFile).use { outputStream ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int

                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                        }
                        outputStream.flush()
                    }
                }
            } catch (e: MalformedURLException) {
                throw Exception("Invalid SMB URL: ${e.message}")
            } catch (e: Exception) {
                throw Exception("Upload failed: ${e.message}")
            }
        }

    /**
     * Downloads a file from the specified SMB path.
     *
     * @param source The full SMB path of the file to download
     * @param username SMB username
     * @param password SMB password
     * @param destination The local file to write to
     */
    suspend fun downloadFile(source: String, username: String, password: String, destination: File) =
        withContext(Dispatchers.IO) {
            try {
                val auth = NtlmPasswordAuthentication(null, username, password)
                val smbFile = SmbFile(source, auth)

                if (!smbFile.exists()) {
                    throw Exception("File not found: $source")
                }

                if (smbFile.isDirectory()) {
                    throw Exception("Source is a directory, not a file")
                }

                SmbFileInputStream(smbFile).use { inputStream ->
                    destination.outputStream().use { outputStream ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int

                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                        }
                        outputStream.flush()
                    }
                }
            } catch (e: MalformedURLException) {
                throw Exception("Invalid SMB URL: ${e.message}")
            } catch (e: Exception) {
                throw Exception("Download failed: ${e.message}")
            }
        }

    /**
     * Deletes a file or folder from the specified SMB path.
     *
     * @param path The full SMB path to delete
     * @param username SMB username
     * @param password SMB password
     */
    suspend fun deleteFile(path: String, username: String, password: String) =
        withContext(Dispatchers.IO) {
            try {
                val auth = NtlmPasswordAuthentication(null, username, password)
                val smbFile = SmbFile(path, auth)

                if (!smbFile.exists()) {
                    throw Exception("File not found: $path")
                }

                smbFile.delete()
            } catch (e: MalformedURLException) {
                throw Exception("Invalid SMB URL: ${e.message}")
            } catch (e: Exception) {
                throw Exception("Delete failed: ${e.message}")
            }
        }

    /**
     * Creates a new folder at the specified SMB path.
     *
     * @param path The full SMB path for the new folder
     * @param username SMB username
     * @param password SMB password
     */
    suspend fun createFolder(path: String, username: String, password: String) =
        withContext(Dispatchers.IO) {
            try {
                val auth = NtlmPasswordAuthentication(null, username, password)
                val smbFile = SmbFile(path, auth)

                if (smbFile.exists()) {
                    throw Exception("Path already exists: $path")
                }

                smbFile.mkdir()
            } catch (e: MalformedURLException) {
                throw Exception("Invalid SMB URL: ${e.message}")
            } catch (e: Exception) {
                throw Exception("Failed to create folder: ${e.message}")
            }
        }
}