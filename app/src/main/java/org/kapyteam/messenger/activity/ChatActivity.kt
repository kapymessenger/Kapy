/*
 * This file is a part of Kapy Messenger project.
 * Original link: https://github.com/kapymessenger/Kapy
 */

package org.kapyteam.messenger.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.kapyteam.messenger.R
import org.kapyteam.messenger.model.Profile

class ChatActivity : AppCompatActivity() {
    private lateinit var backBtn: ImageView
    private lateinit var sendBtn: ImageView
    private lateinit var usernameText: TextView
    private lateinit var statusText: TextView
    private lateinit var member: Profile
    private lateinit var chatRecView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        backBtn = findViewById(R.id.back_btn)
        sendBtn = findViewById(R.id.send_btn)
        usernameText = findViewById(R.id.user_name)
        statusText = findViewById(R.id.user_status)

        member = intent.getSerializableExtra("member") as Profile

        initClickListeners()
        initUpPanel()
    }

    private fun initClickListeners() {
        backBtn.setOnClickListener {
            finish()
        }
    }

    private fun initUpPanel() {
        usernameText.text = "${member.firstname} ${member.lastname}"
        statusText.text = if (member.online) "Online" else member.lastSeen
        // TODO: add avatar
    }


}