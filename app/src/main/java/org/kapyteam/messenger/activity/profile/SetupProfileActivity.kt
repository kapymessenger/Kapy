package org.kapyteam.messenger.activity.profile

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import org.kapyteam.messenger.R
import org.kapyteam.messenger.activity.chats.MessengerActivity
import org.kapyteam.messenger.database.FirebaseAuthAgent
import org.kapyteam.messenger.model.Profile
import org.kapyteam.messenger.util.FileUtil

class SetupProfileActivity : AppCompatActivity() {
    private lateinit var firstnameEdit: EditText
    private lateinit var lastnameEdit: EditText
    private lateinit var nicknameEdit: EditText
    private lateinit var finishRegistrationButton: Button
    private lateinit var phoneNumber: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup_profile)

        firstnameEdit = findViewById(R.id.firstname_edit)
        lastnameEdit = findViewById(R.id.lastname_edit)
        nicknameEdit = findViewById(R.id.nickname_edit)
        finishRegistrationButton = findViewById(R.id.register_finish)
        phoneNumber = intent.getStringExtra("phone")!!

        finishRegistrationButton.setOnClickListener {
            register()
        }
    }

    private fun register() {
        if (firstnameEdit.text.isEmpty() || lastnameEdit.text.isEmpty() || nicknameEdit.text.isEmpty()) {
            Toast.makeText(
                applicationContext,
                "Please fill all fields",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val profile = Profile(
            firstname = firstnameEdit.text.toString(),
            lastname = lastnameEdit.text.toString(),
            phone = phoneNumber,
            nickname = nicknameEdit.text.toString()
        )
        FirebaseAuthAgent.registerProfile(profile)
        FileUtil.saveData("{\"phone\": \"$phoneNumber\"}", this)

        val intent = Intent(this, MessengerActivity::class.java)
        intent.putExtra("phone", phoneNumber)
        startActivity(intent)
    }
}