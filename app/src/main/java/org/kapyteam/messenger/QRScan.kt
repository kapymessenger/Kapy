package org.kapyteam.messenger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import android.content.DialogInterface

import org.kapyteam.messenger.R
import android.view.View


class QRScan : AppCompatActivity() {
    private lateinit var scan_button: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrscan)
        scan_button = findViewById(R.id.scan_qr_activity_button)
        scan_button.setOnClickListener(View.OnClickListener { v: View? -> scanCode() })
    }

    private fun scanCode() {
        val options = ScanOptions()
        options.setPrompt("Point the camera at the QR-code")
        options.setBeepEnabled(true)
        options.setOrientationLocked(true)
        options.captureActivity = actionOnQRScanned::class.java
        barLaucher.launch(options)
    }

    var barLaucher = registerForActivityResult(
        ScanContract()
    ) { result: ScanIntentResult ->
        if (result.contents != null) {
            val builder =
                AlertDialog.Builder(this@QRScan)
            builder.setTitle("Result")
            builder.setMessage(result.contents)
            builder.setPositiveButton(
                "OK"
            ) { dialogInterface, i -> dialogInterface.dismiss() }.show()

            // write all scripts inside this if
        }
    }
}