/*
 * This file is a part of Kapy Messenger project.
 * Original link: https://github.com/kapymessenger/Kapy
 */

package org.kapyteam.messenger.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.hdodenhof.circleimageview.CircleImageView
import org.kapyteam.messenger.R
import org.kapyteam.messenger.database.CallAgent
import org.kapyteam.messenger.model.Call
import org.kapyteam.messenger.model.Profile

class IncomingCallActivity : AppCompatActivity() {
    private lateinit var closeCall: FloatingActionButton
    private lateinit var acceptCall: FloatingActionButton
    private lateinit var phoneNumber: TextView
    private lateinit var profileName: TextView
    private lateinit var profileImage: CircleImageView
    private lateinit var incomingProfile: Profile
    private lateinit var call: Call

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incoming_call)

        closeCall = findViewById(R.id.close_call)
        acceptCall = findViewById(R.id.accept_call)
        phoneNumber = findViewById(R.id.profile_phone_number)
        profileImage = findViewById(R.id.profile_image)
        profileName = findViewById(R.id.profile_name)

        incomingProfile = intent.getSerializableExtra("incomingProfile") as Profile
        call = intent.getSerializableExtra("call") as Call

        acceptCall.setOnClickListener {
            handleCall()
        }

        closeCall.setOnClickListener {
            closeCall()
        }
    }

    private fun closeCall() {
        CallAgent.updateCall(call.id)
        finish()
    }

    private fun handleCall() {
        if (call.callType == "VIDEO_CALL") {
            val intent = Intent(
                this,
                VideoCallActivity::class.java
            )
            intent.putExtra("channelName", call.id)
            intent.putExtra("phone", call.receiver)
            startActivity(intent)
        }
    }

    override fun onBackPressed() {}
}