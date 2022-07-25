/*
 * This file is a part of Kapy Messenger project.
 * Original link: https://github.com/kapymessenger/Kapy
 */

package org.kapyteam.messenger.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions

import org.kapyteam.messenger.R
import org.kapyteam.messenger.util.ActionOnQRScanned


class QRScanActivity : AppCompatActivity() {
    private lateinit var scanButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrscan)
        scanButton = findViewById(R.id.scan_qr_activity_button)
        scanButton.setOnClickListener { scanCode() }
    }

    private fun scanCode() {
        val options = ScanOptions()
        options.setPrompt("Point the camera at the QR-code")
        options.setBeepEnabled(true)
        options.setOrientationLocked(true)
        options.captureActivity = ActionOnQRScanned::class.java
        barLauncher.launch(options)
    }

    private var barLauncher = registerForActivityResult(
        ScanContract()
    ) { result: ScanIntentResult ->
        if (result.contents != null) {
            val builder =
                AlertDialog.Builder(this@QRScanActivity)
            builder.setTitle("Result")
            builder.setMessage(result.contents)
            builder.setPositiveButton(
                "OK"
            ) { dialogInterface, i -> dialogInterface.dismiss() }.show()

            // write all scripts inside this if
        }
    }
}