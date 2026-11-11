package org.nehuatl.sample

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

data class ChatUser(
    val uid: String,
    val name: String,
    val lastSeen: Long = 0L,
    val online: Boolean = false
)

data class ChatMessageItem(
    val id: String,
    val senderId: String,
    val senderName: String,
    val text: String,
    val imageUrl: String? = null,
    val timestamp: Long = 0L
)

object FirebaseChatManager {
    private const val TAG = "FirebaseChat"
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private var initialized = false

    // Слушатели, чтобы можно было отписаться
    private var publicListener: ChildEventListener? = null
    private var usersListener: ValueEventListener? = null
    private var privateListeners = mutableMapOf<String, ChildEventListener>()

    fun init() {
        if (initialized) return
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        storage = FirebaseStorage.getInstance()
        if (auth.currentUser == null) {
            auth.signInAnonymously()
                .addOnSuccessListener { Log.d(TAG, "Anonymous auth OK: ${it.user?.uid}") }
                .addOnFailureListener { Log.e(TAG, "Auth failed: ${it.message}") }
        }
        initialized = true
    }

    fun getMyUid(): String? = auth.currentUser?.uid

    // ========== ИМЯ ПОЛЬЗОВАТЕЛЯ ==========

    fun saveMyName(name: String, onDone: () -> Unit = {}) {
        val uid = getMyUid()
        if (uid != null) {
            database.child("users").child(uid).child("name").setValue(name)
                .addOnSuccessListener { onDone() }
                .addOnFailureListener { Log.e(TAG, "saveMyName failed: ${it.message}") }
            return
        }
        // Анонимный вход ещё не завершился — ждём его, потом сохраняем
        auth.signInAnonymously()
            .addOnSuccessListener { result ->
                val newUid = result.user?.uid ?: return@addOnSuccessListener
                database.child("users").child(newUid).child("name").setValue(name)
                    .addOnSuccessListener { onDone() }
                    .addOnFailureListener { Log.e(TAG, "saveMyName failed: ${it.message}") }
            }
            .addOnFailureListener { Log.e(TAG, "saveMyName auth failed: ${it.message}") }
    }

    fun loadMyName(onLoaded: (String?) -> Unit) {
        val uid = getMyUid()
        if (uid != null) {
            database.child("users").child(uid).child("name").get()
                .addOnSuccessListener { snapshot ->
                    onLoaded(snapshot.getValue(String::class.java))
                }
                .addOnFailureListener { onLoaded(null) }
            return
        }
        // Анонимный вход ещё не завершился — ждём его, потом читаем имя
        auth.signInAnonymously()
            .addOnSuccessListener { result ->
                val newUid = result.user?.uid
                if (newUid == null) { onLoaded(null); return@addOnSuccessListener }
                database.child("users").child(newUid).child("name").get()
                    .addOnSuccessListener { snapshot ->
                        onLoaded(snapshot.getValue(String::class.java))
                    }
                    .addOnFailureListener { onLoaded(null) }
            }
            .addOnFailureListener { onLoaded(null) }
    }

    // ========== ОНЛАЙН / ОФЛАЙН ==========

    fun setOnline(online: Boolean) {
        val uid = getMyUid() ?: return
        val ref = database.child("users").child(uid)
        ref.child("online").setValue(online)
        ref.child("lastSeen").setValue(ServerValue.TIMESTAMP)
        if (online) {
            // При отключении Firebase сам поставит online = false
            ref.child("online").onDisconnect().setValue(false)
            ref.child("lastSeen").onDisconnect().setValue(ServerValue.TIMESTAMP)
        }
    }

    // ========== СПИСОК ПОЛЬЗОВАТЕЛЕЙ ==========

    fun listenUsers(onUsers: (List<ChatUser>) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<ChatUser>()
                snapshot.children.forEach { child ->
                    val uid = child.key ?: return@forEach
                    val name = child.child("name").getValue(String::class.java) ?: return@forEach
                    val lastSeen = child.child("lastSeen").getValue(Long::class.java) ?: 0L
                    val online = child.child("online").getValue(Boolean::class.java) ?: false
                    list.add(ChatUser(uid, name, lastSeen, online))
                }
                onUsers(list.sortedByDescending { it.online })
            }
            override fun onCancelled(error: DatabaseError) { Log.e(TAG, "users listen: ${error.message}") }
        }
        database.child("users").addValueEventListener(listener)
        usersListener = listener
        return listener
    }

    fun removeUsersListener() {
        usersListener?.let { database.child("users").removeEventListener(it) }
        usersListener = null
    }

    // ========== ОБЩИЙ ЧАТ ==========

    fun sendPublicMessage(text: String, imageUrl: String? = null) {
        val uid = getMyUid() ?: return
        val ref = database.child("public_messages").push()
        ref.setValue(mapOf(
            "senderId" to uid,
            "senderName" to (myNameCache ?: "Аноним"),
            "text" to text,
            "imageUrl" to imageUrl,
            "timestamp" to ServerValue.TIMESTAMP
        ))
    }

    fun listenPublicMessages(onMessage: (ChatMessageItem) -> Unit): ChildEventListener {
        val listener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val item = parseMessage(snapshot) ?: return
                onMessage(item)
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) { Log.e(TAG, "public listen: ${error.message}") }
        }
        database.child("public_messages").orderByChild("timestamp").limitToLast(100)
            .addChildEventListener(listener)
        publicListener = listener
        return listener
    }

    fun removePublicListener() {
        publicListener?.let { database.child("public_messages").removeEventListener(it) }
        publicListener = null
    }

    // ========== ЛИЧНЫЕ СООБЩЕНИЯ ==========

    fun sendPrivateMessage(otherUid: String, text: String, imageUrl: String? = null) {
        val myUid = getMyUid() ?: return
        val key = database.child("user_messages").push().key ?: return
        val msg = mapOf(
            "senderId" to myUid,
            "senderName" to (myNameCache ?: "Аноним"),
            "text" to text,
            "imageUrl" to imageUrl,
            "timestamp" to ServerValue.TIMESTAMP
        )
        // Дублируем в оба узла, чтобы каждый видел переписку у себя
        database.updateChildren(mapOf(
            "/user_messages/$myUid/$otherUid/$key" to msg,
            "/user_messages/$otherUid/$myUid/$key" to msg,
            "/user_chats/$myUid/$otherUid" to mapOf(
                "lastMessage" to text,
                "timestamp" to ServerValue.TIMESTAMP,
                "otherName" to (otherUserNameCache ?: "")
            ),
            "/user_chats/$otherUid/$myUid" to mapOf(
                "lastMessage" to text,
                "timestamp" to ServerValue.TIMESTAMP,
                "otherName" to (myNameCache ?: "Аноним")
            )
        ))
    }

    fun listenPrivateMessages(otherUid: String, onMessage: (ChatMessageItem) -> Unit): ChildEventListener {
        val myUid = getMyUid() ?: return object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        }
        val listener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val item = parseMessage(snapshot) ?: return
                onMessage(item)
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) { Log.e(TAG, "private listen: ${error.message}") }
        }
        database.child("user_messages").child(myUid).child(otherUid)
            .orderByChild("timestamp").limitToLast(100)
            .addChildEventListener(listener)
        privateListeners[otherUid] = listener
        return listener
    }

    fun removePrivateListener(otherUid: String) {
        val myUid = getMyUid() ?: return
        privateListeners[otherUid]?.let {
            database.child("user_messages").child(myUid).child(otherUid).removeEventListener(it)
        }
        privateListeners.remove(otherUid)
    }

    fun removeAllListeners() {
        removePublicListener()
        removeUsersListener()
        privateListeners.keys.toList().forEach { removePrivateListener(it) }
    }

    // ========== КАРТИНКИ ==========

    fun uploadImage(uri: Uri, onResult: (String?) -> Unit) {
        try {
            val uid = getMyUid() ?: run { onResult(null); return }
            val ref = storage.reference.child("chat_images/$uid/${System.currentTimeMillis()}.jpg")
            ref.putFile(uri)
                .addOnSuccessListener {
                    ref.downloadUrl
                        .addOnSuccessListener { url -> onResult(url.toString()) }
                        .addOnFailureListener { onResult(null) }
                }
                .addOnFailureListener {
                    Log.e(TAG, "uploadImage failed: ${it.message}")
                    onResult(null)
                }
        } catch (e: Exception) {
            // Storage может быть не настроен (Spark plan) — не крашим приложение,
            // просто сообщение без картинки не отправится.
            Log.e(TAG, "uploadImage exception: ${e.message}")
            onResult(null)
        }
    }

    // ========== ВНУТРЕННЕЕ ==========

    var myNameCache: String? = null
        private set
    var otherUserNameCache: String? = null
        private set

    fun cacheMyName(name: String) { myNameCache = name }
    fun cacheOtherName(name: String) { otherUserNameCache = name }

    private fun parseMessage(snapshot: DataSnapshot): ChatMessageItem? {
        val id = snapshot.key ?: return null
        val senderId = snapshot.child("senderId").getValue(String::class.java) ?: return null
        val senderName = snapshot.child("senderName").getValue(String::class.java) ?: "Аноним"
        val text = snapshot.child("text").getValue(String::class.java) ?: ""
        val imageUrl = snapshot.child("imageUrl").getValue(String::class.java)
        val timestamp = snapshot.child("timestamp").getValue(Long::class.java) ?: 0L
        return ChatMessageItem(id, senderId, senderName, text, imageUrl, timestamp)
    }

    // ========== СПИСОК ЛИЧНЫХ ЧАТОВ ==========

    data class ChatPreview(
        val otherUid: String,
        val otherName: String,
        val lastMessage: String,
        val timestamp: Long
    )

    fun listenMyChats(onChats: (List<ChatPreview>) -> Unit) {
        val myUid = getMyUid() ?: return
        database.child("user_chats").child(myUid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<ChatPreview>()
                snapshot.children.forEach { child ->
                    val otherUid = child.key ?: return@forEach
                    val lastMessage = child.child("lastMessage").getValue(String::class.java) ?: return@forEach
                    val timestamp = child.child("timestamp").getValue(Long::class.java) ?: 0L
                    val otherName = child.child("otherName").getValue(String::class.java) ?: otherUid
                    list.add(ChatPreview(otherUid, otherName, lastMessage, timestamp))
                }
                onChats(list.sortedByDescending { it.timestamp })
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // ========== ИНДИКАТОР «ПЕЧАТАЕТ...» ==========

    fun setTyping(otherUid: String, typing: Boolean) {
        val myUid = getMyUid() ?: return
        val ref = database.child("user_typing").child(otherUid).child(myUid)
        if (typing) {
            ref.setValue(true)
            ref.onDisconnect().removeValue()
        } else {
            ref.removeValue()
        }
    }

    fun listenTyping(otherUid: String, onTyping: (Boolean) -> Unit): ValueEventListener {
        val myUid = getMyUid() ?: return object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {}
            override fun onCancelled(error: DatabaseError) {}
        }
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                onTyping(snapshot.getValue(Boolean::class.java) ?: false)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        database.child("user_typing").child(myUid).child(otherUid).addValueEventListener(listener)
        return listener
    }

    fun removeTypingListener(otherUid: String, listener: ValueEventListener) {
        val myUid = getMyUid() ?: return
        database.child("user_typing").child(myUid).child(otherUid).removeEventListener(listener)
    }
}
