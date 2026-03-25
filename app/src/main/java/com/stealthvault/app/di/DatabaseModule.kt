package com.stealthvault.app.di

import android.content.Context
import androidx.room.Room
import com.stealthvault.app.data.local.entities.VaultDao
import com.stealthvault.app.data.local.entities.VaultDatabase
import dagger.Module
import net.sqlcipher.database.SQLiteDatabase
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): VaultDatabase {
        // Master passphrase for SQLCipher encrypted database
        val passphrase = SQLiteDatabase.getBytes("stealth_vault_db_passphrase".toCharArray())
        val factory = SupportFactory(passphrase)
        
        return Room.databaseBuilder(
            context,
            VaultDatabase::class.java,
            "vault_database"
        )
            .openHelperFactory(factory)
            .build()
    }

    @Provides
    fun provideVaultDao(db: VaultDatabase): VaultDao {
        return db.vaultDao()
    }
}
