package org.kapyteam.messenger.activity.profile

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import org.kapyteam.messenger.R
import org.kapyteam.messenger.activity.calls.AudioCallActivity
import org.kapyteam.messenger.activity.calls.VideoCallActivity
import org.kapyteam.messenger.activity.chats.ChatActivity
import org.kapyteam.messenger.database.CallAgent
import org.kapyteam.messenger.database.FirebaseAuthAgent
import org.kapyteam.messenger.model.Call
import org.kapyteam.messenger.model.Profile
import kotlin.random.Random

class ProfileActivity : AppCompatActivity() {
    private lateinit var archiveListButton: ImageView
    private lateinit var audioCallButton: ImageView
    private lateinit var videoCallButton: ImageView
    private lateinit var messageButton: ImageView
    private lateinit var profile: Profile
    private lateinit var avatar: ImageView
    private lateinit var name: TextView
    private lateinit var phone: String
    private lateinit var phoneNum: TextView
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        if (intent.hasExtra("profile")) {
            profile = intent.getSerializableExtra("profile") as Profile
        } else {
            finish()
        }

        avatar = findViewById(R.id.profile_image)
        name = findViewById(R.id.profile_name)
        phoneNum = findViewById(R.id.profile_phone_number)
        phone = intent.getStringExtra("phone")!!
        status = findViewById(R.id.profile_status)
        audioCallButton = findViewById(R.id.audio_call_button)
        videoCallButton = findViewById(R.id.video_call_button)
        archiveListButton = findViewById(R.id.to_archive_button)
        messageButton = findViewById(R.id.message_button)

        if (profile.photo != "") Picasso.get().load(profile.photo).into(avatar)

        initClickListeners()

        name.text = "${profile.firstname} ${profile.lastname} (@${profile.nickname})"
        phoneNum.text = profile.phone
        status.text = if (profile.online) "Online" else profile.lastSeen
    }

    private fun archive() {
        FirebaseAuthAgent
            .getReference()
            .child("users")
            .child(phone)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.hasChild("archiveList")) {
                        val archiveList = snapshot.child("archiveList").value as MutableList<String>
                        var remove = false
                        if (archiveList.contains(profile.phone)) {
                            archiveList.remove(profile.phone)
                            remove = true
                        } else {
                            archiveList.add(profile.phone)
                        }
                        FirebaseAuthAgent
                            .getReference()
                            .child("users")
                            .child(phone)
                            .child("archiveList")
                            .setValue(archiveList)
                            .addOnCompleteListener {
                                Toast.makeText(
                                    this@ProfileActivity,
                                    if (remove) "User was removed from ignore list" else "User was added to ignore list",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    } else {
                        val archiveList = listOf(profile.phone)
                        FirebaseAuthAgent
                            .getReference()
                            .child("users")
                            .child(phone)
                            .child("archiveList")
                            .setValue(archiveList)
                            .addOnCompleteListener {
                                Toast.makeText(
                                    this@ProfileActivity,
                                    "User was added to ignore list",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun initClickListeners() {
        audioCallButton.setOnClickListener {
            val call = Call(
                phone,
                profile.phone,
                Random.nextInt(1, 100000000).toString(),
                "PENDING",
                "AUDIO_CALL"
            )
            CallAgent.sendCall(call)
            val intent = Intent(
                this,
                AudioCallActivity::class.java
            )
            intent.putExtra("call", call)
            intent.putExtra("phone", phone)
            intent.putExtra("isOutgoing", true)
            intent.putExtra("profile", profile)
            intent.putExtra("userRole", 1)
            startActivity(intent)
        }

        videoCallButton.setOnClickListener {
            val call = Call(
                phone,
                profile.phone,
                Random.nextInt(1, 100000000).toString(),
                "PENDING",
                "VIDEO_CALL"
            )
            CallAgent.sendCall(call)
            val intent = Intent(
                this,
                VideoCallActivity::class.java
            )
            intent.putExtra("phone", phone)
            intent.putExtra("channelName", call.id)
            intent.putExtra("userRole", 1)
            startActivity(intent)
        }

        messageButton.setOnClickListener {
            val intent = Intent(
                this,
                ChatActivity::class.java
            )
            intent.putExtra("member", profile)
            intent.putExtra("phone", phone)
            startActivity(intent)
        }

        archiveListButton.setOnClickListener {
            archive()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            else -> super.onOptionsItemSelected(item)
        }
        return true
    }
}