/*
 * This file is a part of Kapy Messenger project.
 * Original link: https://github.com/kapymessenger/Kapy
 */

package org.kapyteam.messenger.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import org.kapyteam.messenger.R
import org.kapyteam.messenger.model.Profile
import org.kapyteam.messenger.util.SerializableObject

class CreateDialogActivity : AppCompatActivity() {
    private lateinit var profiles: MutableList<Profile>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_dialog)

        val contactList: ListView = findViewById(R.id.contact_view)

        if (intent.hasExtra("profiles")) {
            profiles = (intent.getSerializableExtra("profiles") as SerializableObject).obj as MutableList<Profile>
        }

        val phone = intent.getStringExtra("phone")

        contactList.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, convertToString(profiles))

        contactList.setOnItemClickListener { _, item, _, _ ->
            val textView = item as TextView
            val charName = textView.text.toString()
            val character = getProfile(charName)
            val intent = Intent(
                this,
                ChatActivity::class.java
            )
            intent.putExtra("member", character)
            intent.putExtra("phone", phone)
            startActivity(intent)
        }
    }

    private fun convertToString(list: List<Profile>): MutableList<String> {
        val stringList: MutableList<String> = mutableListOf()
        for (profile in list) {
            stringList.add(profile.nickname)
        }
        return stringList
    }

    private fun getProfile(nickname: String): Profile {
        return profiles.first { it.nickname == nickname }
    }
}