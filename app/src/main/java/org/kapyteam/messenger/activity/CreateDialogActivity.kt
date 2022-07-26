/*
 * This file is a part of Kapy Messenger project.
 * Original link: https://github.com/kapymessenger/Kapy
 */

package org.kapyteam.messenger.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import com.google.android.material.navigation.NavigationView
import org.kapyteam.messenger.R
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
                    val intent = Intent(
                        this,
                        QRScanActivity::class.java
                    )
                    startActivity(intent)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            else -> super.onOptionsItemSelected(item)
        }
        return true
    }
}