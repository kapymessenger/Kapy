/*
 * This file is a part of Kapy Messenger project.
 * Original link: https://github.com/kapymessenger/Kapy
 */

package org.kapyteam.messenger.activity.calls

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.JsonParser
import io.agora.rtc.Constants
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import io.agora.rtc.video.VideoCanvas
import okhttp3.OkHttpClient
import okhttp3.Request
import org.kapyteam.messenger.R
import org.kapyteam.messenger.activity.chats.MessengerActivity
import org.kapyteam.messenger.database.CallAgent
import kotlin.random.Random

class VideoCallActivity : AppCompatActivity() {
    private val appId = "f86dfc376b1047288576893f4874ae71"
    private lateinit var channelName: String
    private lateinit var mRtcEngine: RtcEngine
    private lateinit var localVideoContainer: View
    private lateinit var remoteVideoContainer: View
    private lateinit var toggleMicro: ImageView
    private lateinit var toggleCamera: ImageView
    private lateinit var changeCamera: ImageView
    private lateinit var phone: String
    private lateinit var endCall: ImageView

    private var mutedMicro = false
    private var mutedCam = false
    private var userRole = 0

    private val mRtcEventHandler: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {
        override fun onUserJoined(uid: Int, elapsed: Int) {
            runOnUiThread {
                setupRemoteVideo(uid)
                setupLocalVideo()
            }
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            runOnUiThread {
                onRemoteUserLeft()
            }
        }

        override fun onRemoteVideoStateChanged(uid: Int, state: Int, reason: Int, elapsed: Int) {
            super.onRemoteVideoStateChanged(uid, state, reason, elapsed)
            runOnUiThread {
                remoteVideoContainer.isVisible = state != 0
            }
        }
    }
    val requestedPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_call)
        if (ContextCompat.checkSelfPermission(this.applicationContext, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestedPermission.launch(android.Manifest.permission.CAMERA)
        }
        channelName = intent.getStringExtra("channelName")!!
        userRole = intent.getIntExtra("userRole", 1)
        localVideoContainer = findViewById(R.id.local_video_container)
        remoteVideoContainer = findViewById(R.id.remote_video_container)
        toggleMicro = findViewById(R.id.micro_button)
        phone = intent.getStringExtra("phone")!!
        endCall = findViewById(R.id.phone_button)
        toggleCamera = findViewById(R.id.video_button)
        changeCamera = findViewById(R.id.switch_camera_button)

        setupDbListener()
        initClickListeners()
        initAgora()
        joinChannel()


    }

    private fun initClickListeners() {
        toggleMicro.setOnClickListener {
            mutedMicro = !mutedMicro
            mRtcEngine.muteLocalAudioStream(mutedMicro)
            if (mutedMicro) toggleMicro.setImageResource(R.drawable.mic_off_icon)
            else toggleMicro.setImageResource(R.drawable.mic_icon)
        }

        toggleCamera.setOnClickListener {
            localVideoContainer.isVisible = mutedCam
            mutedCam = !mutedCam
            mRtcEngine.muteLocalVideoStream(mutedCam)
            if (mutedCam) toggleCamera.setImageResource(R.drawable.videocam_off_icon)
            else toggleCamera.setImageResource(R.drawable.videocam_icon)
        }

        changeCamera.setOnClickListener {
            mRtcEngine.switchCamera()
        }

        endCall.setOnClickListener {
            CallAgent.updateCall(channelName)
            onRemoteUserLeft()
        }
    }

    private fun setupDbListener() {
        FirebaseDatabase
            .getInstance()
            .getReference("calls")
            .child(channelName)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.child("callState").value.toString() == "COMPLETED") {
                        onCallEnd()
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun onCallEnd() {
        (localVideoContainer as FrameLayout).removeAllViews()
        mRtcEngine.leaveChannel()
        RtcEngine.destroy()
        val intent = Intent(
            this,
            MessengerActivity::class.java
        )
        intent.putExtra("phone", phone)
        startActivity(intent)
    }

    override fun onBackPressed() {
        Toast.makeText(applicationContext, "Please end your call to exit", Toast.LENGTH_SHORT)
    }

    private fun initAgora() {
        mRtcEngine = RtcEngine.create(baseContext, appId, mRtcEventHandler)
    }

    private fun joinChannel() {
        mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING)
        mRtcEngine.setClientRole(userRole)
        mRtcEngine.enableVideo()

        val thread = Thread {
            val uid = Random.nextInt(0, 100)

            val request = Request.Builder()
                .url("https://kapy-messenger.herokuapp.com/token?channel_name=$channelName&uid=$uid")
                .build()

            val client = OkHttpClient()
            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val json = response.body.string()
                val jsonObj = JsonParser.parseString(json).asJsonObject
                val token = jsonObj.get("token").asString

                runOnUiThread {
                    mRtcEngine.joinChannel(
                        token,
                        channelName,
                        null,
                        uid
                    )
                    setupLocalVideo()
                }

            }
        }
        thread.start()
    }

    private fun setupLocalVideo() {
        val surfaceView = RtcEngine.CreateRendererView(baseContext)
        (localVideoContainer as FrameLayout).addView(surfaceView)
        mRtcEngine.setupLocalVideo(
            VideoCanvas(
                surfaceView,
                VideoCanvas.RENDER_MODE_FIT,
                0
            )
        )
    }

    private fun setupRemoteVideo(uid: Int) {
        val surfaceView = RtcEngine.CreateRendererView(baseContext)
        surfaceView.setZOrderMediaOverlay(false)
        (remoteVideoContainer as FrameLayout).addView(surfaceView)
        mRtcEngine.setupRemoteVideo(
            VideoCanvas(
                surfaceView,
                VideoCanvas.RENDER_MODE_FIT,
                uid
            )
        )
    }

    private fun onRemoteUserLeft() {
        (localVideoContainer as FrameLayout).removeAllViews()
        mRtcEngine.leaveChannel()
        RtcEngine.destroy()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        mRtcEngine.leaveChannel()
        RtcEngine.destroy()
    }
}