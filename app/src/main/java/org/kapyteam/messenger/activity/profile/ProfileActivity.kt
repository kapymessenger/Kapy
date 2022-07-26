package org.kapyteam.messenger.activity.profile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import org.kapyteam.messenger.R
import org.kapyteam.messenger.database.DBAgent
import org.kapyteam.messenger.model.Profile

class ProfileActivity : AppCompatActivity() {
    private lateinit var profile: Profile
    private lateinit var avatar: ImageView
    private lateinit var name: TextView
    private lateinit var phoneNum: TextView
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        profile = intent.getSerializableExtra("profile") as Profile
        avatar = findViewById(R.id.profile_image)
        name = findViewById(R.id.profile_name)
        phoneNum = findViewById(R.id.profile_phone_number)
        status = findViewById(R.id.profile_status)

        name.text = "${profile.firstname} ${profile.lastname} (@${profile.nickname})"
        phoneNum.text = profile.phone
        status.text = if (profile.online) "Online" else profile.lastSeen
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            else -> super.onOptionsItemSelected(item)
        }
        return true
    }
}