package org.kapyteam.messenger

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.Toast
import com.dolatkia.animatedThemeManager.AppTheme
import com.dolatkia.animatedThemeManager.ThemeActivity
import com.dolatkia.animatedThemeManager.ThemeManager
import com.google.android.material.navigation.NavigationView
import org.kapyteam.messenger.activity.chats.ShareQRActivity
import org.kapyteam.messenger.activity.init.GreetingActivity
import org.kapyteam.messenger.database.FirebaseAuthAgent
import org.kapyteam.messenger.databinding.ActivitySettingsBinding


class SettingsActivity : ThemeActivity() {

    private lateinit var binder: ActivitySettingsBinding

    override fun syncTheme(appTheme: AppTheme) {
        // change ui colors with new appThem here

        val myAppTheme = appTheme as MyAppTheme

        // set background color
        binder.root.setBackgroundColor(myAppTheme.firstActivityBackgroundColor(this))
        binder.settingsProfileName.setTextColor(myAppTheme.firstActivityTextColor(this))
        binder.settingsProfilePhoneNumber.setTextColor(myAppTheme.firstActivityTextColor(this))
        //set text color
    }



    override fun getStartTheme(): AppTheme {
        return LightTheme()
    }

    fun logOut(view: View){
        FirebaseAuthAgent.getInstance().signOut()
        val intent = Intent(
            this,
            GreetingActivity::class.java
        )
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binder = ActivitySettingsBinding.inflate(LayoutInflater.from(this))
        setContentView(binder.root)

        setThemeAnimationListener(MyThemeAnimationListener(this))

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        initMenu()
    }

    private fun initMenu() {
        val menu: NavigationView = findViewById(R.id.setting_menu)
        menu.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.settings_switch_theme -> {
                    val intent = intent
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    finish()
                    val sPref = getSharedPreferences("MyPref", MODE_PRIVATE)
                    val ed = sPref.edit()
                    if (sPref.getString("Theme", "") == "0") {
                        ed.putString("Theme", "1");
                        ed.commit();
                        Toast.makeText(this, "Default theme was changed to light", Toast.LENGTH_SHORT).show()
                    } else {
                        ed.putString("Theme", "0");
                        ed.commit();
                        Toast.makeText(this, "Default theme was changed to dark", Toast.LENGTH_SHORT).show()
                    }

                    overridePendingTransition(0, 0)
                    startActivity(intent)
                }
                R.id.settings_profile_edit -> {
                    val intent = Intent(
                        this,
                        ShareQRActivity::class.java
                    )
//                    intent.putExtra("phone", phone)    Max, please complete this
                    startActivity(intent)
                }
                R.id.settings_log_out -> {
                    FirebaseAuthAgent.getInstance().signOut()
                    val intent = Intent(
                        this,
                        GreetingActivity::class.java
                    )
                    startActivity(intent)
                }
            }
            true
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            else -> super.onOptionsItemSelected(item)
        }
        return true
    }


}