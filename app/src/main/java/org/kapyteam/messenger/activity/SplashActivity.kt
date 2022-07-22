package org.kapyteam.messenger.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.kapyteam.messenger.R
import org.kapyteam.messenger.database.FirebaseAuthAgent
import org.kapyteam.messenger.util.FileUtil

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        verifyAccount()
    }

    private fun verifyAccount() {
        if (FirebaseAuthAgent.getCurrentUser() != null) {
            val meta = FileUtil.loadData(this)
            val intent = Intent(
                this,
                MessengerActivity::class.java
            )
            try {
                intent.putExtra("phone", meta.get("phone").asString)
                startActivity(intent)
            } catch (e: Exception) {
                FirebaseAuthAgent.getInstance().signOut()
                startActivity(Intent(
                    this,
                    GreetingActivity::class.java
                ))
            }

        } else {
            val intent = Intent(
                this,
                GreetingActivity::class.java
            )
            startActivity(intent)
        }
    }
}