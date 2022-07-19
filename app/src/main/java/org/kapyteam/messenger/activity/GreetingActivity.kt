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

    private lateinit var sign_up_button :Button;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_greeting)

        sign_up_button = findViewById(R.id.sign_up_button)
        sign_up_button.setOnClickListener{
            val intent = Intent(this, Registration1::class.java)
            startActivity(intent)
        }
    }
}