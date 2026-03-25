package com.stealthvault.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.stealthvault.app.data.local.entities.ChatContact
import com.stealthvault.app.data.local.entities.SecureMessage
import com.stealthvault.app.data.local.entities.VaultDao
import com.stealthvault.app.data.security.ChatEncryptionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val dao: VaultDao,
    private val encryptionManager: ChatEncryptionManager
) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseDatabase = FirebaseDatabase.getInstance()

    /**
     * Send an E2E Encrypted message to a partner.
     */
    suspend fun sendMessage(partnerId: String, partnerPublicKey: String, text: String, selfDestructSec: Int = 0) {
        val encrypted = encryptionManager.encryptMessage(text, partnerPublicKey)
        val myId = auth.currentUser?.uid ?: return
        
        val messageId = db.getReference("messages").push().key ?: return
        val payload = mapOf(
            "fromId" to myId,
            "content" to encrypted.content,
            "encryptedKey" to encrypted.encryptedKey,
            "iv" to encrypted.iv,
            "timestamp" to System.currentTimeMillis(),
            "selfDestruct" to (selfDestructSec > 0),
            "expirySec" to selfDestructSec
        )

        // Relay via Firebase
        db.getReference("messages/$partnerId/$myId/$messageId").setValue(payload).await()
        
        // Save locally in our vault
        val localMsg = SecureMessage(
            messageId = messageId,
            chatPartnerId = partnerId,
            content = text, // Cache decrypted locally
            isFromMe = true,
            isSelfDestruct = selfDestructSec > 0,
            expiryTime = if (selfDestructSec > 0) System.currentTimeMillis() + (selfDestructSec * 1000L) else 0
        )
        dao.insertMessage(localMsg)
    }

    /**
     * Register self on the relay with public identity key.
     */
    suspend fun registerSelf(username: String) {
        val user = auth.currentUser ?: return
        val keyPair = encryptionManager.getIdentityKeyPair()
        val pubKeyBase64 = android.util.Base64.encodeToString(keyPair.public.encoded, android.util.Base64.NO_WRAP)
        
        val userProfile = mapOf(
            "username" to username,
            "publicKey" to pubKeyBase64
        )
        db.getReference("users/${user.uid}").setValue(userProfile).await()
    }

    fun getMessages(partnerId: String): Flow<List<SecureMessage>> = dao.getMessagesForPartner(partnerId)
    fun getContacts(): Flow<List<ChatContact>> = dao.getAllContacts()

    suspend fun addContact(contact: ChatContact) = dao.insertContact(contact)
    
    suspend fun cleanupExpired() {
        dao.cleanExpiredMessages(System.currentTimeMillis())
    }
}
