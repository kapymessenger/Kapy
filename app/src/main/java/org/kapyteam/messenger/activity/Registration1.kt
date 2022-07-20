package org.kapyteam.messenger.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import org.kapyteam.messenger.R

class Registration1 : AppCompatActivity() {

    private lateinit var continue_button :Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration1)

        continue_button = findViewById(R.id.register_continue)

        continue_button.setOnClickListener {
            val intent = Intent(this, Registration2::class.java)
            startActivity(intent)
        }
    }
}