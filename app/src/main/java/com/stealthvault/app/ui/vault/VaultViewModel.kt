package com.stealthvault.app.ui.vault

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stealthvault.app.data.local.entities.*
import com.stealthvault.app.data.repository.VaultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class VaultViewModel @Inject constructor(
    private val repository: VaultRepository
) : ViewModel() {

    val photos: StateFlow<List<VaultFile>> = repository.getFilesByType("Photo")
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val videos: StateFlow<List<VaultFile>> = repository.getFilesByType("Video")
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val files: StateFlow<List<VaultFile>> = repository.getFilesByType("Document")
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val lockedApps = repository.getAllLockedApps()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val intruderLogs: StateFlow<List<IntruderLog>> = repository.getIntruderLogs()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val notes: StateFlow<List<VaultNote>> = repository.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun importFile(context: Context, uri: Uri) = viewModelScope.launch {
        val type = context.contentResolver.getType(uri) ?: "Document"
        val category = when {
            type.startsWith("image/") -> "Photo"
            type.startsWith("video/") -> "Video"
            else -> "Document"
        }
        
        // Copy to temp file then hide
        val tempFile = File(context.cacheDir, "temp_import_${System.currentTimeMillis()}")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }
        
        if (tempFile.exists()) {
            repository.hideFile(tempFile, category)
        }
    }

    fun hideFile(file: File, type: String) = viewModelScope.launch { repository.hideFile(file, type) }
    fun restoreFile(vaultFile: VaultFile) = viewModelScope.launch { repository.restoreFile(vaultFile) }
    
    fun lockApp(pkgName: String, name: String) = viewModelScope.launch { repository.lockApp(pkgName, name) }
    fun unlockApp(pkgName: String, name: String) = viewModelScope.launch { repository.unlockApp(LockedApp(pkgName, name)) }

    fun saveNote(title: String, content: String) = viewModelScope.launch { 
        repository.saveNote(VaultNote(title = title, content = content))
    }
    fun deleteNote(note: VaultNote) = viewModelScope.launch { repository.deleteNote(note) }
}
