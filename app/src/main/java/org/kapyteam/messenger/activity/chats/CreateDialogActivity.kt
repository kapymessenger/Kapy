/*
 * This file is a part of Kapy Messenger project.
 * Original link: https://github.com/kapymessenger/Kapy
 */

package org.kapyteam.messenger.activity.chats

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import org.kapyteam.messenger.R
import org.kapyteam.messenger.activity.profile.ProfileActivity
import org.kapyteam.messenger.model.Profile
import org.kapyteam.messenger.util.SerializableObject

class CreateDialogActivity : AppCompatActivity() {
    private lateinit var phone: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_dialog)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val contactList: ListView = findViewById(R.id.contact_view)

        val profiles =
            (intent.getSerializableExtra("profiles") as SerializableObject).obj as MutableList<Profile>

        phone = intent.getStringExtra("phone")!!

        contactList.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            profiles.map { it.nickname }
        )

        contactList.setOnItemClickListener { _, item, _, _ ->
            val textView = item as TextView
            val character = profiles.first { it.nickname == textView.text.toString() }
            val intent = Intent(
                this,
                ChatActivity::class.java
            )
            intent.putExtra("member", character)
            intent.putExtra("phone", phone)
            startActivity(intent)
        }

        initMenu()
    }

    private fun initMenu() {
        val menu: NavigationView = findViewById(R.id.new_dialog_menu_view)
        menu.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.scan_qr -> {
                    scanCode()
                }
                R.id.share_qr -> {
                    val intent = Intent(
                        this,
                        ShareQRActivity::class.java
                    )
                    intent.putExtra("phone", phone)
                    startActivity(intent)
                }
            }
            true
        }
    }

    private fun scanCode() {
        val options = ScanOptions()
        options.setPrompt("Point the camera at the QR-code")
        options.setBeepEnabled(false)
        options.setOrientationLocked(true)
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
                        if (snapshot.hasChild(result.contents)) {
                            try {
                                val intent = Intent(
                                    this@CreateDialogActivity,
                                    ProfileActivity::class.java
                                )
                                intent.putExtra("phone", phone)
                                intent.putExtra(
                                    "profile", Profile(
                                        firstname = snapshot.child(result.contents)
                                            .child("firstname").value.toString(),
                                        lastname = snapshot.child(result.contents)
                                            .child("lastname").value.toString(),
                                        phone = snapshot.child(result.contents)
                                            .child("phone").value.toString(),
                                        nickname = snapshot.child(result.contents)
                                            .child("nickname").value.toString(),
                                        photo = snapshot.child(result.contents)
                                            .child("photo").value.toString(),
                                        lastSeen = snapshot.child(result.contents)
                                            .child("lastSeen").value.toString(),
                                        online = snapshot.child(result.contents).child("online")
                                            .getValue(Boolean::class.java)!!
                                    )
                                )
                                startActivity(intent)
                            } catch (e: Exception) {
                                finish()
                            }
                        } else {
                            val builder =
                                AlertDialog.Builder(this@CreateDialogActivity)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            else -> super.onOptionsItemSelected(item)
        }
        return true
    }
}