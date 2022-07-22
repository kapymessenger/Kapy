package org.kapyteam.messenger.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.kapyteam.messenger.R
import org.kapyteam.messenger.database.DBAgent
import org.kapyteam.messenger.database.FirebaseAuthAgent

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        verifyAccount()
    }

    private fun verifyAccount() {
        if (FirebaseAuthAgent.getCurrentUser() != null) {
            val intent = Intent(
                this,
                MessengerActivity::class.java
            )
            startActivity(intent)
        } else {
            val intent = Intent(
                this,
                GreetingActivity::class.java
            )
            startActivity(intent)
        }
    }
}