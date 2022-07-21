/*
 * This file is a part of Kapy Messenger project.
 * Original link: https://github.com/kapymessenger/Kapy
 */

package org.kapyteam.messenger.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
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

        contactList.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, convertToString(profiles))
    }

    private fun convertToString(list: List<Profile>): MutableList<String> {
        val stringList: MutableList<String> = mutableListOf()
        for (profile in list) {
            stringList.add("${profile.firstname} ${profile.lastname} (${profile.nickname})")
        }
        return stringList
    }
}