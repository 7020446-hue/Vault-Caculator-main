package com.stealthvault.app.data.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptionManager @Inject constructor() {

    private val KEY_ALIAS = "stealth_vault_master_key"
    private val ANDROID_KEY_STORE = "AndroidKeyStore"
    private val TRANSFORMATION = "AES/GCM/NoPadding"

    private val secretKey: SecretKey by lazy {
        getOrCreateSecretKey()
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
        
        try {
            if (keyStore.containsAlias(KEY_ALIAS)) {
                val entry = keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry
                return entry.secretKey
            }
        } catch (e: Exception) {
            try {
                keyStore.deleteEntry(KEY_ALIAS)
            } catch (ignored: Exception) {}
        }

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build()
        
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    /**
     * Encrypt a file and save it to the target location.
     */
    fun encryptFile(inputFile: File, outputFile: File) {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        
        val iv = cipher.iv
        outputFile.outputStream().use { fos ->
            // Write IV first so we can read it during decryption
            fos.write(iv.size)
            fos.write(iv)
            
            CipherOutputStream(fos, cipher).use { cos ->
                inputFile.inputStream().use { fis ->
                    fis.copyTo(cos)
                }
            }
        }
    }

    /**
     * Decrypt a file and save it to the target location.
     */
    fun decryptFile(inputFile: File, outputFile: File) {
        val fis = FileInputStream(inputFile)
        val ivSize = fis.read()
        val iv = ByteArray(ivSize)
        fis.read(iv)
        
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        
        CipherInputStream(fis, cipher).use { cis ->
            FileOutputStream(outputFile).use { fos ->
                cis.copyTo(fos)
            }
        }
    }
    
    /**
     * Get an InputStream for a decrypted file (useful for streaming media).
     */
    fun getDecryptedStream(file: File): InputStream {
        val fis = FileInputStream(file)
        val ivSize = fis.read()
        val iv = ByteArray(ivSize)
        fis.read(iv)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        
        return CipherInputStream(fis, cipher)
    }
}
