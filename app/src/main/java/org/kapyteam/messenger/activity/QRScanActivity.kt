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
            println(result.contents)
            FirebaseDatabase
                .getInstance()
                .getReference("users")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.hasChild(result.contents)) {
                            val intent = Intent(
                                this@QRScanActivity,
                                ProfileActivity::class.java
                            )
                            intent.putExtra("profile", Profile(
                                firstname = snapshot.child(result.contents).child("firstname").value.toString(),
                                lastname = snapshot.child(result.contents).child("lastname").value.toString(),
                                phone = snapshot.child(result.contents).child("phone").value.toString(),
                                nickname = snapshot.child(result.contents).child("nickname").value.toString(),
                                photo = snapshot.child(result.contents).child("photo").value.toString(),
                                lastSeen = snapshot.child(result.contents).child("lastSeen").value.toString(),
                                online = snapshot.child(result.contents).child("online").getValue(Boolean::class.java)!!
                            ))
                            startActivity(intent)
                        } else {
                            val builder =
                                AlertDialog.Builder(this@QRScanActivity)
                            builder.setTitle("Error")
                            builder.setMessage("Profile not recognized. Try again.")
                            builder.setPositiveButton(
                                "OK"
                            ) { dialogInterface, _ -> dialogInterface.dismiss() }.show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }
    }
}