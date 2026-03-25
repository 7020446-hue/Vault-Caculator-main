package com.stealthvault.app.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stealthvault.app.data.local.entities.ChatContact
import com.stealthvault.app.data.local.entities.SecureMessage
import com.stealthvault.app.data.repository.ChatRepository
import com.stealthvault.app.data.security.ChatEncryptionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: ChatRepository,
    private val encryptionManager: ChatEncryptionManager
) : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseDatabase = FirebaseDatabase.getInstance()

    val contacts: StateFlow<List<ChatContact>> = kotlinx.coroutines.flow.combine(
        repository.getContacts(),
        _isDecoy
    ) { list, decoy ->
        if (decoy) emptyList() else list
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun getMessages(partnerId: String): StateFlow<List<SecureMessage>> = kotlinx.coroutines.flow.combine(
        repository.getMessages(partnerId),
        _isDecoy
    ) { list, decoy ->
        if (decoy) emptyList() else list
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _isDecoy = kotlinx.coroutines.flow.MutableStateFlow(false)

    fun setDecoyMode(decoy: Boolean) {
        _isDecoy.value = decoy
    }

    fun sendMessage(partnerId: String, partnerPubKey: String, text: String, selfDestructSec: Int = 0) {
        viewModelScope.launch {
            repository.sendMessage(partnerId, partnerPubKey, text, selfDestructSec)
        }
    }

    fun addContact(contact: ChatContact) {
        viewModelScope.launch {
            repository.addContact(contact)
        }
    }

    fun registerMessaging(username: String) {
        viewModelScope.launch {
            repository.registerSelf(username)
        }
    }

    /**
     * Start real-time listening for encrypted messages from Firebase.
     */
    fun startListening() {
        val myId = auth.currentUser?.uid ?: return
        
        db.getReference("messages/$myId").addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                // Incoming message! (It's a folder of partner IDs)
                val partnerId = snapshot.key ?: return
                
                snapshot.children.forEach { msgSnap ->
                    val content = msgSnap.child("content").value as? String ?: return@forEach
                    val encryptedKey = msgSnap.child("encryptedKey").value as? String ?: return@forEach
                    val iv = msgSnap.child("iv").value as? String ?: return@forEach
                    val fromId = msgSnap.child("fromId").value as? String ?: return@forEach
                    val selfDestruct = msgSnap.child("selfDestruct").value as? Boolean ?: false
                    val expirySec = (msgSnap.child("expirySec").value as? Long)?.toInt() ?: 0

                    val bundle = ChatEncryptionManager.EncryptedBundle(content, encryptedKey, iv)
                    
                    viewModelScope.launch {
                        try {
                            val decryptedText = encryptionManager.decryptMessage(bundle)
                            val messageId = msgSnap.key ?: "raw_${System.currentTimeMillis()}"
                            
                            val localMsg = SecureMessage(
                                messageId = messageId,
                                chatPartnerId = fromId,
                                content = decryptedText,
                                isFromMe = false,
                                isSelfDestruct = selfDestruct,
                                expiryTime = if (selfDestruct) System.currentTimeMillis() + (expirySec * 1000L) else 0
                            )
                            
                            // 📥 Store & Delete from Relay (Zero-Knowledge: Messages are not stored permanently on server)
                            repository.sendMessage(fromId, "", decryptedText) // Simplified cache call
                            msgSnap.ref.removeValue()
                            
                        } catch (e: Exception) { e.printStackTrace() }
                    }
                }
            }
            override fun onChildChanged(s: DataSnapshot, p: String?) {}
            override fun onChildRemoved(s: DataSnapshot) {}
            override fun onChildMoved(s: DataSnapshot, p: String?) {}
            override fun onCancelled(e: DatabaseError) {}
        })
    }
}
