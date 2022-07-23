/*
 * This file is a part of Kapy Messenger project.
 * Original link: https://github.com/kapymessenger/Kapy
 */

package org.kapyteam.messenger.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.isVisible
import io.agora.rtc.Constants
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import io.agora.rtc.video.VideoCanvas
import org.kapyteam.messenger.R

class VideoCallActivity : AppCompatActivity() {
    private val APP_ID = "f86dfc376b1047288576893f4874ae71"
    private lateinit var channelName: String
    private lateinit var mRtcEngine: RtcEngine
    private lateinit var localVideoContainer: View
    private lateinit var remoteVideoContainer: View
    private lateinit var toggleMicro: ImageView
    private lateinit var toggleCamera: ImageView
    private lateinit var changeCamera: ImageView
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

        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            runOnUiThread { println("success!!") }
        }

        override fun onRemoteVideoStateChanged(uid: Int, state: Int, reason: Int, elapsed: Int) {
            super.onRemoteVideoStateChanged(uid, state, reason, elapsed)
            runOnUiThread {
                remoteVideoContainer.isVisible = state != 0
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_call)

        channelName = intent.getStringExtra("channelName")!!
        userRole = intent.getIntExtra("userRole", 0)
        localVideoContainer = findViewById(R.id.local_video_container)
        remoteVideoContainer = findViewById(R.id.remote_video_container)
        toggleMicro = findViewById(R.id.micro_button)
        endCall = findViewById(R.id.phone_button)
        toggleCamera = findViewById(R.id.video_button)
        changeCamera = findViewById(R.id.switch_camera_button)

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
            onRemoteUserLeft()
        }
    }

    override fun onBackPressed() {
        Toast.makeText(applicationContext, "Please end your call to exit", 2)
    }

    private fun initAgora() {
        mRtcEngine = RtcEngine.create(baseContext, APP_ID, mRtcEventHandler)
    }

    private fun joinChannel() {
        mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING)
        mRtcEngine.setClientRole(userRole)
        mRtcEngine.enableVideo()

        mRtcEngine.joinChannel(
            "006f86dfc376b1047288576893f4874ae71IADvdVtXeZ9HX3IDCan4G23yZTHofmC7mpQWyUfcFTGl7Ax+f9gAAAAAEAAtDEjTfW3dYgEAAQB9bd1i",
            channelName,
            null,
            0
        )
        setupLocalVideo()
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