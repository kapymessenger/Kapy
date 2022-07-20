/*
 * This file is a part of Kapy Messenger project.
 * Original link: https://github.com/kapymessenger/Kapy
 */

package org.kapyteam.messenger.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import org.kapyteam.messenger.R

class GreetingActivity : AppCompatActivity() {

    private lateinit var signUpButton :Button;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_greeting)

        signUpButton = findViewById(R.id.sign_up_button)
        signUpButton.setOnClickListener{
            val intent = Intent(this, EnterPhoneNumberActivity::class.java)
            startActivity(intent)
        }
    }
}