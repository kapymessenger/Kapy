/*
 * This file is a part of Kapy Messenger project.
 * Original link: https://github.com/kapymessenger/Kapy
 */

package org.kapyteam.messenger.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import org.kapyteam.messenger.R
import org.kapyteam.messenger.database.FirebaseAuthAgent
import org.kapyteam.messenger.model.Message
import org.kapyteam.messenger.model.Profile

class ChatActivity : AppCompatActivity() {
    private lateinit var backBtn: ImageView
    private lateinit var sendBtn: ImageView
    private lateinit var usernameText: TextView
    private lateinit var statusText: TextView
    private lateinit var member: Profile
    private lateinit var chatRecView: RecyclerView
    private lateinit var msgEdit: EditText
    private lateinit var dbReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        backBtn = findViewById(R.id.back_btn)
        sendBtn = findViewById(R.id.send_btn)
        usernameText = findViewById(R.id.user_name)
        statusText = findViewById(R.id.user_status)
        msgEdit = findViewById(R.id.message_edit)

        dbReference = FirebaseDatabase.getInstance().getReference("chats")

        member = intent.getSerializableExtra("member") as Profile

        FirebaseAuthAgent
            .getReference()
            .child("users")
            .child(member.phone)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    println("EVENT!")
                    member.firstname = snapshot.child("firstname").getValue(String::class.java)!!
                    member.lastSeen = snapshot.child("lastSeen").getValue(String::class.java)!!
                    member.nickname = snapshot.child("nickname").getValue(String::class.java)!!
                    member.online = snapshot.child("online").getValue(Boolean::class.java)!!
                    member.photo = snapshot.child("photo").getValue(String::class.java)!!
                    member.lastname = snapshot.child("lastname").getValue(String::class.java)!!
                    initUpPanel()
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

        initClickListeners()
        initUpPanel()
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
    }

    private fun buildModel(content: String): Message {
        return Message(
            sender = "+12345678900",
            receiver = member.phone,
            createTime = "testTime",
            content = content
        )
    }

    private fun initUpPanel() {
        usernameText.text = "${member.firstname} ${member.lastname}"
        statusText.text = if (member.online) "Online" else "Offline"
        // TODO: add avatar
    }

    private fun sendMessage(model: Message) {
        dbReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.hasChild("${model.sender}&${model.receiver}")) {
                    dbReference
                        .child("${model.sender}&${model.receiver}")
                        .child("messages")
                        .push()
                        .setValue(model)
                } else if (snapshot.hasChild("${model.receiver}&${model.sender}")) {
                    dbReference
                        .child("${model.receiver}&${model.sender}")
                        .child("messages")
                        .push()
                        .setValue(model)
                } else {
                    dbReference
                        .child("${model.sender}&${model.receiver}").let {
                            it
                                .child("members")
                                .setValue(listOf(model.sender, model.receiver))
                            it
                                .child("messages")
                                .push()
                                .setValue(model)
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

//    if (dbReference.ha("${model.sender}&${model.receiver}").)
//    dbReference.child("${model.sender}&${model.receiver}").c.push()
//    .setValue(model)
}