package org.kapyteam.messenger.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import org.kapyteam.messenger.R
import org.kapyteam.messenger.database.FirebaseAuthAgent
import org.kapyteam.messenger.model.Profile

class SetupProfileActivity : AppCompatActivity() {
    private lateinit var firstnameEdit: EditText
    private lateinit var lastnameEdit: EditText
    private lateinit var finishRegistrationButton: Button
    private lateinit var phoneNumber: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup_profile)

        firstnameEdit = findViewById(R.id.firstname_edit)
        lastnameEdit = findViewById(R.id.lastname_edit)
        finishRegistrationButton = findViewById(R.id.register_finish)
        phoneNumber = intent.getStringExtra("phone")!!

        finishRegistrationButton.setOnClickListener {
            register()
        }
    }

    private fun register() {
        val profile = Profile(
            firstname = firstnameEdit.text.toString(),
            lastname = lastnameEdit.text.toString(),
            phone = phoneNumber,
            nickname = "test111"
        )
        FirebaseAuthAgent.registerProfile(profile)
    }
}