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

    private val _isDecoy = MutableStateFlow(false)
    val isDecoy: StateFlow<Boolean> = _isDecoy.asStateFlow()

    fun setDecoyMode(decoy: Boolean) {
        _isDecoy.value = decoy
    }

    // Wrap flows to return empty list if decoy
    val photos: StateFlow<List<VaultFile>> = combine(
        repository.getFilesByType("Photo"),
        _isDecoy
    ) { files, isDecoyMode ->
        if (isDecoyMode) emptyList() else files
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val videos: StateFlow<List<VaultFile>> = combine(
        repository.getFilesByType("Video"),
        _isDecoy
    ) { files, isDecoyMode ->
        if (isDecoyMode) emptyList() else files
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val files: StateFlow<List<VaultFile>> = combine(
        repository.getFilesByType("Document"),
        _isDecoy
    ) { files, isDecoyMode ->
        if (isDecoyMode) emptyList() else files
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val lockedApps = combine(
        repository.getAllLockedApps(),
        _isDecoy
    ) { apps, isDecoyMode ->
        if (isDecoyMode) emptyList() else apps
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val intruderLogs: StateFlow<List<IntruderLog>> = combine(
        repository.getIntruderLogs(),
        _isDecoy
    ) { logs, isDecoyMode ->
        if (isDecoyMode) emptyList() else logs
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val notes: StateFlow<List<VaultNote>> = combine(
        repository.getAllNotes(),
        _isDecoy
    ) { notes, isDecoyMode ->
        if (isDecoyMode) emptyList() else notes
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        
    val clonedApps: StateFlow<List<ClonedApp>> = combine(
        repository.getAllClonedApps(),
        _isDecoy
    ) { apps, isDecoyMode ->
        if (isDecoyMode) emptyList() else apps
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun importFile(context: Context, uri: Uri) = viewModelScope.launch {
        val type = context.contentResolver.getType(uri) ?: "Document"
        val category = when {
            type.startsWith("image/") -> "Photo"
            type.startsWith("video/") -> "Video"
            else -> "Document"
        }
        
        // Try to get original path and name
        val originalName = queryFileName(context, uri)
        val originalPath = getUriPath(context, uri)
        
        // Copy to temp file then hide
        val tempFile = File(context.cacheDir, "temp_import_${System.currentTimeMillis()}")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }
        
        if (tempFile.exists()) {
            repository.hideFile(tempFile, category, originalName, originalPath)
        }
    }

    private fun queryFileName(context: Context, uri: Uri): String? {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) it.getString(nameIndex) else null
            } else null
        }
    }

    private fun getUriPath(context: Context, uri: Uri): String? {
        if (uri.scheme == "file") return uri.path
        // For content URIs, we'll store a sensible default restore loc if we can't find better
        val fileName = queryFileName(context, uri) ?: "restored_file"
        val downloadDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
        return File(downloadDir, fileName).absolutePath
    }

    fun hideFile(file: File, type: String) = viewModelScope.launch { repository.hideFile(file, type) }
    fun restoreFile(vaultFile: VaultFile) = viewModelScope.launch { repository.restoreFile(vaultFile) }
    
    fun lockApp(pkgName: String, name: String) = viewModelScope.launch { repository.lockApp(pkgName, name) }
    fun unlockApp(pkgName: String, name: String) = viewModelScope.launch { repository.unlockApp(LockedApp(pkgName, name)) }

    fun saveNote(title: String, content: String, category: String = "General", id: Long = 0) = viewModelScope.launch { 
        repository.saveNote(VaultNote(id = id, title = title, content = content, category = category))
    }
    fun deleteNote(note: VaultNote) = viewModelScope.launch { repository.deleteNote(note) }
    
    fun cloneApp(packageName: String, label: String) = viewModelScope.launch {
        repository.saveClonedApp(ClonedApp(packageName, label, label))
    }
    
    fun deleteClonedApp(app: ClonedApp) = viewModelScope.launch { repository.deleteClonedApp(app) }
}
