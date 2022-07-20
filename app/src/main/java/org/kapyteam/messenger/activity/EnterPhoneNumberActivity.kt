package org.kapyteam.messenger.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import org.kapyteam.messenger.R

class EnterPhoneNumberActivity : AppCompatActivity() {

    private lateinit var continueButton :Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter_phone_number)

        continueButton = findViewById(R.id.register_continue)

        continueButton.setOnClickListener {
            val intent = Intent(this, EnterVerificationCodeActivity::class.java)
            startActivity(intent)
        }
    }
}