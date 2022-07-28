package org.kapyteam.messenger.activity.init

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatDelegate
import com.dolatkia.animatedThemeManager.AppTheme
import com.dolatkia.animatedThemeManager.ThemeActivity
import com.dolatkia.animatedThemeManager.ThemeManager
import org.kapyteam.messenger.*
import org.kapyteam.messenger.activity.chats.MessengerActivity
import org.kapyteam.messenger.database.FirebaseAuthAgent
import org.kapyteam.messenger.databinding.ActivitySettingsBinding
import org.kapyteam.messenger.databinding.ActivitySplashBinding
import org.kapyteam.messenger.util.FileUtil
import kotlin.system.exitProcess

@SuppressLint("CustomSplashScreen")
class SplashActivity : ThemeActivity() {

    private lateinit var binder: ActivitySplashBinding

    override fun getStartTheme(): AppTheme {
        val sPref = getSharedPreferences("MyPref", MODE_PRIVATE)
        val savedText: String? = sPref.getString("Theme", "")
        println(savedText)
        return if (savedText == "0") DarkTheme()
        else LightTheme()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binder = ActivitySplashBinding.inflate(LayoutInflater.from(this))
        setContentView(binder.root)

        setThemeAnimationListener(MyThemeAnimationListener(this))



        val sPref = getSharedPreferences("MyPref", MODE_PRIVATE)
        val savedText: String? = sPref.getString("Theme", "")
        val intent = intent
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        finish()
        if (savedText == "0") {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        overridePendingTransition(0, 0)
        startActivity(intent)


        verifyAccount()
    }

    override fun syncTheme(appTheme: AppTheme) {

        val myAppTheme = appTheme as MyAppTheme

        // set background color
        binder.root.setBackgroundColor(myAppTheme.firstActivityBackgroundColor(this))
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