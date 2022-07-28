package org.kapyteam.messenger

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.Switch
import com.dolatkia.animatedThemeManager.AppTheme
import com.dolatkia.animatedThemeManager.ThemeActivity
import com.dolatkia.animatedThemeManager.ThemeManager
import org.kapyteam.messenger.databinding.ActivitySettingsBinding


class SettingsActivity : ThemeActivity() {

    private lateinit var themeSwitch : Switch
    private lateinit var binder: ActivitySettingsBinding

    override fun syncTheme(appTheme: AppTheme) {
        // change ui colors with new appThem here

        val myAppTheme = appTheme as MyAppTheme

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        // set background color
        binder.root.setBackgroundColor(myAppTheme.firstActivityBackgroundColor(this))
        binder.settingsSwitchTheme.setTextColor(myAppTheme.firstActivityTextColor(this))
        binder.settingsProfileName.setTextColor(myAppTheme.firstActivityTextColor(this))
        binder.settingsProfilePhoneNumber.setTextColor(myAppTheme.firstActivityTextColor(this))
        //set text color


    }



    override fun getStartTheme(): AppTheme {
        return LightTheme()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binder = ActivitySettingsBinding.inflate(LayoutInflater.from(this))
        setContentView(binder.root)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        setThemeAnimationListener(MyThemeAnimationListener(this))

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        themeSwitch = findViewById(R.id.settings_switch_theme)

        themeSwitch.isChecked = ThemeManager.instance.getCurrentTheme()?.id() == 1

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            val intent = intent
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            finish()
            if (isChecked){
                ThemeManager.instance.changeTheme(DarkTheme(), themeSwitch)
            } else{
                ThemeManager.instance.changeTheme(LightTheme(), themeSwitch)
            }
            overridePendingTransition(0, 0)
            startActivity(intent)
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