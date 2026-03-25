package com.stealthvault.app.data.local.entities

import androidx.room.*

@Entity(tableName = "vault_files")
data class VaultFile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileName: String,
    val encryptedPath: String,
    val originalPath: String,
    val fileType: String,
    val dateAdded: Long = System.currentTimeMillis(),
    val size: Long
)

@Entity(tableName = "locked_apps")
data class LockedApp(
    @PrimaryKey val packageName: String,
    val appName: String,
    val isLocked: Boolean = true
)

@Entity(tableName = "intruder_logs")
data class IntruderLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val photoPath: String,
    val timestamp: Long = System.currentTimeMillis(),
    val attemptType: String
)

@Entity(tableName = "vault_notes")
data class VaultNote(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface VaultDao {
    // Files
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: VaultFile)
    
    @Query("SELECT * FROM vault_files WHERE fileType = :type")
    fun getFilesByType(type: String): kotlinx.coroutines.flow.Flow<List<VaultFile>>
    
    @Delete
    suspend fun deleteFile(file: VaultFile)

    // Apps
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun lockApp(app: LockedApp)
    
    @Query("SELECT * FROM locked_apps WHERE packageName = :pkg")
    suspend fun getLockedApp(pkg: String): LockedApp?
    
    @Query("SELECT * FROM locked_apps")
    fun getAllLockedApps(): kotlinx.coroutines.flow.Flow<List<LockedApp>>
    
    @Delete
    suspend fun unlockApp(app: LockedApp)

    // Intruder Logs
    @Insert
    suspend fun logIntruder(log: IntruderLog)
    
    @Query("SELECT * FROM intruder_logs ORDER BY timestamp DESC")
    fun getIntruderLogs(): kotlinx.coroutines.flow.Flow<List<IntruderLog>>

    // Notes
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveNote(note: VaultNote)
    
    @Query("SELECT * FROM vault_notes ORDER BY timestamp DESC")
    fun getAllNotes(): kotlinx.coroutines.flow.Flow<List<VaultNote>>
    
    @Delete
    suspend fun deleteNote(note: VaultNote)
}

@Database(entities = [VaultFile::class, LockedApp::class, IntruderLog::class, VaultNote::class], version = 1)
abstract class VaultDatabase : RoomDatabase() {
    abstract fun vaultDao(): VaultDao
}
