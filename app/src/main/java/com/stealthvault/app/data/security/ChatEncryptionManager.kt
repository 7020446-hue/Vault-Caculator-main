package com.stealthvault.app.data.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.*
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatEncryptionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val KEY_ALGORITHM_RSA = "RSA"
    private val KEY_ALGORITHM_AES = "AES"
    private val RSA_TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"
    private val AES_TRANSFORMATION = "AES/GCM/NoPadding"
    private val KEY_ALIAS_RSA = "stealth_chat_identity_key"
    private val KEY_STORE_TYPE = "AndroidKeyStore"

    private val keyStore: KeyStore = KeyStore.getInstance(KEY_STORE_TYPE).apply { load(null) }

    /**
     * Get or Generate RSA Identity KeyPair (Public/Private)
     */
    fun getIdentityKeyPair(): KeyPair {
        if (!keyStore.containsAlias(KEY_ALIAS_RSA)) {
            generateIdentityKey()
        }

        val privateKey = keyStore.getKey(KEY_ALIAS_RSA, null) as PrivateKey
        val publicKey = keyStore.getCertificate(KEY_ALIAS_RSA).publicKey
        return KeyPair(publicKey, privateKey)
    }

    private fun generateIdentityKey() {
        val keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORITHM_RSA, KEY_STORE_TYPE)
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS_RSA,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
            .setDigests(KeyProperties.DIGEST_SHA256)
            .setKeySize(2048)
            .build()
        
        keyPairGenerator.initialize(spec)
        keyPairGenerator.generateKeyPair()
    }

    /**
     * Encrypt a message using a Receiver's Public Key.
     * Returns a Bundle of {EncryptedMessage, EncryptedAESKey, IV}
     */
    fun encryptMessage(plainText: String, receiverPublicKeyBase64: String): EncryptedBundle {
        // 1. Generate a random AES key for this message
        val aesKey = KeyGenerator.getInstance(KEY_ALGORITHM_AES).apply { init(256) }.generateKey()
        
        // 2. Encrypt the Message with AES-GCM
        val aesCipher = Cipher.getInstance(AES_TRANSFORMATION)
        aesCipher.init(Cipher.ENCRYPT_MODE, aesKey)
        val iv = aesCipher.iv
        val encryptedBytes = aesCipher.doFinal(plainText.toByteArray())
        
        // 3. Encrypt the AES Key with Receiver's Public RSA Key
        val publicKey = getPublicKeyFromBase64(receiverPublicKeyBase64)
        val rsaCipher = Cipher.getInstance(RSA_TRANSFORMATION)
        rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val encryptedAesKey = rsaCipher.doFinal(aesKey.encoded)
        
        return EncryptedBundle(
            content = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP),
            encryptedKey = Base64.encodeToString(encryptedAesKey, Base64.NO_WRAP),
            iv = Base64.encodeToString(iv, Base64.NO_WRAP)
        )
    }

    /**
     * Decrypt an incoming message using our Private RSA Key.
     */
    fun decryptMessage(bundle: EncryptedBundle): String {
        val privateKey = keyStore.getKey(KEY_ALIAS_RSA, null) as PrivateKey
        
        // 1. Decrypt the AES Key using our Private RSA Key
        val rsaCipher = Cipher.getInstance(RSA_TRANSFORMATION)
        rsaCipher.init(Cipher.DECRYPT_MODE, privateKey)
        val aesKeyBytes = rsaCipher.doFinal(Base64.decode(bundle.encryptedKey, Base64.NO_WRAP))
        val aesKey = SecretKeySpec(aesKeyBytes, KEY_ALGORITHM_AES)
        
        // 2. Decrypt the Message using the AES Key
        val aesCipher = Cipher.getInstance(AES_TRANSFORMATION)
        val iv = Base64.decode(bundle.iv, Base64.NO_WRAP)
        val spec = GCMParameterSpec(128, iv)
        aesCipher.init(Cipher.DECRYPT_MODE, aesKey, spec)
        
        val decryptedBytes = aesCipher.doFinal(Base64.decode(bundle.content, Base64.NO_WRAP))
        return String(decryptedBytes)
    }

    private fun getPublicKeyFromBase64(base64: String): PublicKey {
        val bytes = Base64.decode(base64, Base64.NO_WRAP)
        val keySpec = X509EncodedKeySpec(bytes)
        return KeyFactory.getInstance("RSA").generatePublic(keySpec)
    }

    data class EncryptedBundle(
        val content: String,
        val encryptedKey: String,
        val iv: String
    )
}
