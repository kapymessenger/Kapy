/*
 * This file is a part of Kapy Messenger project.
 * Original link: https://github.com/kapymessenger/Kapy
 */

package org.kapyteam.messenger.activity.misc

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import com.dolatkia.animatedThemeManager.AppTheme
import com.dolatkia.animatedThemeManager.ThemeActivity
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import org.kapyteam.messenger.LightTheme
import org.kapyteam.messenger.MyAppTheme
import org.kapyteam.messenger.MyThemeAnimationListener
import org.kapyteam.messenger.R
import org.kapyteam.messenger.activity.init.GreetingActivity
import org.kapyteam.messenger.activity.profile.ProfileEditingActivity
import org.kapyteam.messenger.database.FirebaseAuthAgent
import org.kapyteam.messenger.databinding.ActivitySettingsBinding

class SettingsActivity : ThemeActivity() {
    private lateinit var avatar: CircleImageView
    private lateinit var name: TextView
    private lateinit var phone: TextView
    private lateinit var phoneNum: String
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binder = ActivitySettingsBinding.inflate(LayoutInflater.from(this))
        setContentView(binder.root)
        avatar = findViewById(R.id.settings_profile_image)
        name = findViewById(R.id.settings_profile_name)
        phone = findViewById(R.id.settings_profile_phone_number)

        setThemeAnimationListener(MyThemeAnimationListener(this))

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        phoneNum = intent.getStringExtra("phone")!!

        FirebaseAuthAgent
            .getReference()
            .child("users")
            .child(phoneNum)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    name.text = snapshot.child("nickname").value.toString()
                    phone.text = phoneNum

                    snapshot.child("photo").value.toString().let {
                        if (it != "") Picasso.get().load(it).into(avatar)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })

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
                        ed.putString("Theme", "1")
                        ed.commit()
                        Toast.makeText(this, "Default theme was changed to light", Toast.LENGTH_SHORT).show()
                    } else {
                        ed.putString("Theme", "0")
                        ed.commit()
                        Toast.makeText(this, "Default theme was changed to dark", Toast.LENGTH_SHORT).show()
                    }

                    overridePendingTransition(0, 0)
                    startActivity(intent)
                }
                R.id.settings_profile_edit -> {
                    val intent = Intent(
                        this,
                        ProfileEditingActivity::class.java
                    )
                    intent.putExtra("phone", phoneNum)
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