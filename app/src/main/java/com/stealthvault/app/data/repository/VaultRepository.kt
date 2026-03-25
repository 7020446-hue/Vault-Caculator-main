package com.stealthvault.app.data.repository

import android.content.Context
import com.stealthvault.app.data.local.entities.*
import com.stealthvault.app.data.security.EncryptionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaultRepository @Inject constructor(
    private val dao: VaultDao,
    private val encryptionManager: EncryptionManager,
    @ApplicationContext private val context: Context
) {

    suspend fun hideFile(originalFile: File, fileType: String) = withContext(Dispatchers.IO) {
        val encryptedFileName = "vault_${UUID.randomUUID()}"
        val encryptedFile = File(context.filesDir, "vault/$encryptedFileName")
        if (!encryptedFile.parentFile!!.exists()) encryptedFile.parentFile!!.mkdirs()
        encryptionManager.encryptFile(originalFile, encryptedFile)
        dao.insertFile(VaultFile(fileName = originalFile.name, encryptedPath = encryptedFile.absolutePath, originalPath = originalFile.absolutePath, fileType = fileType, size = originalFile.length()))
        if (originalFile.exists()) originalFile.delete()
    }

    suspend fun restoreFile(vaultFile: VaultFile) = withContext(Dispatchers.IO) {
        val encryptedFile = File(vaultFile.encryptedPath)
        val originalFile = File(vaultFile.originalPath)
        encryptionManager.decryptFile(encryptedFile, originalFile)
        dao.deleteFile(vaultFile)
        if (encryptedFile.exists()) encryptedFile.delete()
    }

    fun getFilesByType(type: String): Flow<List<VaultFile>> = dao.getFilesByType(type)
    suspend fun lockApp(pkgName: String, name: String) = dao.lockApp(LockedApp(pkgName, name))
    suspend fun unlockApp(app: LockedApp) = dao.unlockApp(app)
    fun getAllLockedApps(): Flow<List<LockedApp>> = dao.getAllLockedApps()
    suspend fun isAppLocked(pkgName: String): Boolean = dao.getLockedApp(pkgName) != null

    suspend fun logIntruderAttempt(photoPath: String, type: String) = dao.logIntruder(IntruderLog(photoPath = photoPath, attemptType = type))
    fun getIntruderLogs(): Flow<List<IntruderLog>> = dao.getIntruderLogs()
    
    suspend fun saveNote(note: VaultNote) = dao.saveNote(note)
    suspend fun deleteNote(note: VaultNote) = dao.deleteNote(note)
    fun getAllNotes(): Flow<List<VaultNote>> = dao.getAllNotes()

    suspend fun wipeAllData() = withContext(Dispatchers.IO) {
        val vaultDir = File(context.filesDir, "vault")
        if (vaultDir.exists()) vaultDir.deleteRecursively()
    }
}
