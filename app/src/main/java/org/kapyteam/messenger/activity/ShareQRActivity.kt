package org.kapyteam.messenger.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import net.glxn.qrgen.android.QRCode
import org.kapyteam.messenger.R

class ShareQRActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share_qr)

        val qr: ImageView = findViewById(R.id.idIVQrcode)
        val nickIntent = intent.getStringExtra("phone")!!
        val nickname: TextView = findViewById(R.id.nickname_qr)

        nickname.text = nickIntent

        qr.setImageBitmap(QRCode.from(nickIntent).bitmap())
    }
}