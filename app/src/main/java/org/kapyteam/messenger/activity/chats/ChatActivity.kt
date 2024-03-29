/*
 * This file is a part of Kapy Messenger project.
 * Original link: https://github.com/kapymessenger/Kapy
 */

package org.kapyteam.messenger.activity.chats

import android.content.Intent
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import org.kapyteam.messenger.R
import org.kapyteam.messenger.activity.calls.AudioCallActivity
import org.kapyteam.messenger.activity.calls.VideoCallActivity
import org.kapyteam.messenger.activity.profile.ProfileActivity
import org.kapyteam.messenger.ai.Recognizer
import org.kapyteam.messenger.component.ChatAdapter
import org.kapyteam.messenger.database.CallAgent
import org.kapyteam.messenger.database.FirebaseAuthAgent
import org.kapyteam.messenger.model.Call
import org.kapyteam.messenger.model.Message
import org.kapyteam.messenger.model.Profile
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.min
import kotlin.random.Random

class ChatActivity : AppCompatActivity() {
    private lateinit var backBtn: ImageView
    private lateinit var aiBtn: ImageView
    private lateinit var sendBtn: ImageView
    private lateinit var usernameText: TextView
    private lateinit var statusText: TextView
    lateinit var member: Profile
    private lateinit var chatRecView: RecyclerView
    private lateinit var msgEdit: EditText
    private lateinit var avatar: ImageView
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var callBtn: ImageView
    private lateinit var videoCallBtn: ImageView
    private lateinit var dbReference: DatabaseReference
    private lateinit var chatId: String
    lateinit var phone: String

    private val imageSize = 224

    val requestedPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                takePicturePreview.launch(null)
            }
        }

    val takePicturePreview =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            if (bitmap != null) {
                val dimension = min(bitmap.width, bitmap.height)
                var bitmapFin = ThumbnailUtils.extractThumbnail(bitmap, dimension, dimension)

                bitmapFin = Bitmap.createScaledBitmap(
                    bitmapFin,
                    imageSize,
                    imageSize, false
                )
                Recognizer.outputGenerator(bitmapFin, this)
            }
        }

    private val messages = mutableListOf<Message>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        backBtn = findViewById(R.id.back_btn)
        sendBtn = findViewById(R.id.send_btn)
        usernameText = findViewById(R.id.user_name)
        statusText = findViewById(R.id.user_status)
        msgEdit = findViewById(R.id.message_edit)
        aiBtn = findViewById(R.id.ai_btn)
        avatar = findViewById(R.id.profile_avatar)

        member = intent.getSerializableExtra("member") as Profile

        phone = intent.getStringExtra("phone")!!

        chatAdapter = ChatAdapter(messages, phone)

        dbReference = FirebaseDatabase.getInstance().getReference("chats")

        chatRecView = findViewById(R.id.chat_recycler_view)
        chatRecView.setHasFixedSize(true)
        chatRecView.layoutManager = LinearLayoutManager(this@ChatActivity)
        chatRecView.adapter = chatAdapter
        callBtn = findViewById(R.id.call_btn)
        videoCallBtn = findViewById(R.id.video_call_btn)

        FirebaseAuthAgent
            .getReference()
            .child("users")
            .child(member.phone)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        member.firstname = snapshot.child("firstname").value.toString()
                        member.lastSeen = snapshot.child("lastSeen").value.toString()
                        member.nickname = snapshot.child("nickname").value.toString()
                        member.online = snapshot.child("online").getValue(Boolean::class.java)!!
                        member.photo = snapshot.child("photo").value.toString()
                        member.lastname = snapshot.child("lastname").value.toString()

                        snapshot.child("photo").value.toString().let {
                            if (it != "") Picasso.get().load(it).into(avatar)
                        }

                        initUpPanel()
                    } catch (e: Exception) {
                        finish()
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        initClickListeners()
        initUpPanel()

        setupMsgListener()
    }

    private fun setupMsgListener() {
        FirebaseAuthAgent
            .getReference()
            .child("chats")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    chatId = if (snapshot.hasChild("${member.phone}&${phone}")) {
                        "${member.phone}&${phone}"
                    } else {
                        "${phone}&${member.phone}"
                    }

                    receiveMessage(chatId)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun initClickListeners() {
        backBtn.setOnClickListener {
            finish()
        }
        sendBtn.setOnClickListener {
            msgEdit.text.toString().let {
                if (it != "") {
                    sendMessage(buildModel(it))
                    msgEdit.text.clear()
                }
            }
        }
        avatar.setOnClickListener {
            val intent = Intent(
                this,
                ProfileActivity::class.java
            )
            intent.putExtra("phone", phone)
            intent.putExtra("profile", member)
            startActivity(intent)
        }
        videoCallBtn.setOnClickListener {
            val call = Call(
                phone,
                member.phone,
                Random.nextInt(1, 100000000).toString(),
                "PENDING",
                "VIDEO_CALL"
            )
            CallAgent.sendCall(call)
            val intent = Intent(
                this@ChatActivity,
                VideoCallActivity::class.java
            )
            intent.putExtra("phone", phone)
            intent.putExtra("channelName", call.id)
            intent.putExtra("userRole", 1)
            startActivity(intent)
        }
        callBtn.setOnClickListener {
            val call = Call(
                phone,
                member.phone,
                Random.nextInt(1, 100000000).toString(),
                "PENDING",
                "AUDIO_CALL"
            )
            CallAgent.sendCall(call)
            val intent = Intent(
                this@ChatActivity,
                AudioCallActivity::class.java
            )
            intent.putExtra("call", call)
            intent.putExtra("phone", phone)
            intent.putExtra("isOutgoing", true)
            intent.putExtra("profile", member)
            intent.putExtra("userRole", 1)
            startActivity(intent)
        }
        aiBtn.setOnClickListener {
            Recognizer.takePicture(this)
        }
    }

    private fun buildModel(content: String): Message {
        return Message(
            sender = phone,
            createTime = DateTimeFormatter.ofPattern("E, dd MMM yyyy HH:mm").format(LocalDateTime.now()),
            content = content,
            metadata = ""
        )
    }

    private fun initUpPanel() {
        usernameText.text = "${member.firstname} ${member.lastname}"
        statusText.text = if (member.online) "Online" else member.lastSeen
        // TODO: add avatar
    }

    private fun receiveMessage(child: String) {
        dbReference.child(child).child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messages.clear()
                    for (message in snapshot.children) {
                        messages.add(
                            Message(
                                message.child("sender").value.toString(),
                                message.child("createTime").value.toString(),
                                message.child("content").value.toString(),
                                message.child("metadata").value.toString()
                            )
                        )
                    }
                    chatAdapter.update(messages)
                    chatAdapter.notifyDataSetChanged()
                    if (messages.size > 0) {
                        chatRecView.smoothScrollToPosition(messages.size - 1)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun sendMessage(model: Message) {
        dbReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                dbReference
                    .child(chatId).let {
                        it
                            .child("members")
                            .setValue(listOf(model.sender, member.phone))
                        it
                            .child("messages")
                            .push()
                            .setValue(model)
                    }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}