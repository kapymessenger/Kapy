package org.kapyteam.messenger.activity.calls

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.widget.Chronometer
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.JsonParser
import de.hdodenhof.circleimageview.CircleImageView
import io.agora.rtc.Constants
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import okhttp3.OkHttpClient
import okhttp3.Request
import org.kapyteam.messenger.R
import org.kapyteam.messenger.activity.chats.MessengerActivity
import org.kapyteam.messenger.database.CallAgent
import org.kapyteam.messenger.model.Call
import org.kapyteam.messenger.model.Profile
import kotlin.random.Random

class AudioCallActivity : AppCompatActivity() {
    private val appID = "f86dfc376b1047288576893f4874ae71"
    private lateinit var rtcEngine: RtcEngine
    private lateinit var profileImage: CircleImageView
    private lateinit var profileName: TextView
    private lateinit var callType: TextView
    private lateinit var phoneNumber: TextView
    private lateinit var timeStamp: Chronometer
    private lateinit var closeCall: FloatingActionButton
    private lateinit var speakerChange: FloatingActionButton
    private lateinit var muteMic: FloatingActionButton
    private lateinit var profile: Profile
    private lateinit var call: Call
    private lateinit var phone: String
    private var isOutgoing = true
    private var mutedMicro = false

    private val rtcEventHandler: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {
        override fun onUserJoined(uid: Int, elapsed: Int) {
            runOnUiThread {
                timeStamp.base = SystemClock.elapsedRealtime()
                timeStamp.start()
            }
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            runOnUiThread {
                closeCall()
            }
        }

        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            runOnUiThread {
                timeStamp.text = "RTC connected. Waiting for another user..."
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_outgoing_call)

        profileImage = findViewById(R.id.profile_image_audio)
        profileName = findViewById(R.id.profile_name_audio)
        callType = findViewById(R.id.call_type_audio)
        phoneNumber = findViewById(R.id.profile_phone_number_audio)
        timeStamp = findViewById(R.id.time_stamp_audio)
        closeCall = findViewById(R.id.close_call_audio)
        speakerChange = findViewById(R.id.speaker_change_audio)
        muteMic = findViewById(R.id.mute_mic_audio)
        profile = intent.getSerializableExtra("profile") as Profile
        phone = intent.getStringExtra("phone")!!
        call = intent.getSerializableExtra("call") as Call
        isOutgoing = intent.getBooleanExtra("isOutgoing", true)
        rtcEngine = RtcEngine.create(baseContext, appID, rtcEventHandler)

        setupDbListener()
        setupClickListeners()
        preInitMetadataViews()
        joinChannel()
    }

    private fun joinChannel() {
        rtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING)
        rtcEngine.setClientRole(1)
        rtcEngine.disableVideo()

        val thread = Thread {
            val uid = Random.nextInt(0, 100)

            val request = Request.Builder()
                .url("https://kapy-messenger.herokuapp.com/token?channel_name=${call.id}&uid=$uid")
                .build()

            val client = OkHttpClient()
            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val json = response.body.string()
                val jsonObj = JsonParser.parseString(json).asJsonObject
                val token = jsonObj.get("token").asString

                runOnUiThread {
                    rtcEngine.joinChannel(
                        token,
                        call.id,
                        null,
                        uid
                    )
                }
            }
        }
        thread.start()
    }

    private fun preInitMetadataViews() {
        // TODO: IMG IMPLEMENT
        profileName.text = "${profile.firstname} ${profile.lastname}"
        callType.text = if (isOutgoing) "Outgoing audio call" else "Incoming audio call"
        phoneNumber.text = profile.phone
        timeStamp.text = "Calling..."
    }

    private fun setupClickListeners() {
        closeCall.setOnClickListener {
            CallAgent.updateCall(call.id)
            closeCall()
        }
        muteMic.setOnClickListener {
            mutedMicro = !mutedMicro
            rtcEngine.muteLocalAudioStream(mutedMicro)
            muteMic.setImageResource(if (mutedMicro) R.drawable.mic_off_icon else R.drawable.mic_icon)
        }
        speakerChange.setOnClickListener {
            // TODO: Speakers
        }
    }

    private fun setupDbListener() {
        FirebaseDatabase
            .getInstance()
            .getReference("calls")
            .child(call.id)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.child("callState").value.toString() == "COMPLETED") {
                        closeCall()
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun closeCall() {
        rtcEngine.leaveChannel()
        RtcEngine.destroy()
        val intent = Intent(
            this,
            MessengerActivity::class.java
        )
        intent.putExtra("phone", phone)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        rtcEngine.leaveChannel()
        RtcEngine.destroy()
    }
}