package org.kapyteam.messenger.activity.chats.group

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.kapyteam.messenger.R
import org.kapyteam.messenger.model.Profile
import org.kapyteam.messenger.util.SerializableObject

class AddMembersActivity : AppCompatActivity() {
    private lateinit var profiles: MutableList<Profile>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_members)

        profiles = (intent.getSerializableExtra("profiles") as SerializableObject).obj as MutableList<Profile>

    }
}