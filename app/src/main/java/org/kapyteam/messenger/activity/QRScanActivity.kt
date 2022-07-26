/*
 * This file is a part of Kapy Messenger project.
 * Original link: https://github.com/kapymessenger/Kapy
 */

package org.kapyteam.messenger.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import org.kapyteam.messenger.R
import org.kapyteam.messenger.model.Profile
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
        options.setBeepEnabled(false)
        options.setOrientationLocked(true)
        options.captureActivity = ActionOnQRScanned::class.java
        barLauncher.launch(options)
    }

    private var barLauncher = registerForActivityResult(
        ScanContract()
    ) { result: ScanIntentResult ->
        if (result.contents != null) {
            FirebaseDatabase
                .getInstance()
                .getReference("users")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (child in snapshot.children) {
                            if (child.child("nickname").value.toString() == result.contents) {
                                val intent = Intent(
                                    this@QRScanActivity,
                                    ProfileActivity::class.java
                                )
                                intent.putExtra("profile", Profile(
                                    firstname = child.child("firstname").value.toString(),
                                    lastname = child.child("lastname").value.toString(),
                                    phone = child.child("phone").value.toString(),
                                    nickname = child.child("nickname").value.toString(),
                                    photo = child.child("photo").value.toString(),
                                    lastSeen = child.child("lastSeen").value.toString(),
                                    online = child.child("online").getValue(Boolean::class.java)!!
                                ))
                                startActivity(intent)
                                finish()
                                return
                            }
                        }
                        val builder =
                            AlertDialog.Builder(this@QRScanActivity)
                        builder.setTitle("Error")
                        builder.setMessage("Profile not recognized. Try again.")
                        builder.setPositiveButton(
                            "OK"
                        ) { dialogInterface, _ -> dialogInterface.dismiss() }.show()

                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }
    }
}