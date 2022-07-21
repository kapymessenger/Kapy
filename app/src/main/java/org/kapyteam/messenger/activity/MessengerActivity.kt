/*
 * This file is a part of Kapy Messenger project.
 * Original link: https://github.com/kapymessenger/Kapy
 */

package org.kapyteam.messenger.activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.DataSnapshot
import org.kapyteam.messenger.DrawerActivity
import org.kapyteam.messenger.R
import org.kapyteam.messenger.database.DBAgent
import org.kapyteam.messenger.database.FirebaseAuthAgent
import org.kapyteam.messenger.databinding.ActivityMessengerBinding
import org.kapyteam.messenger.util.IWait

class MessengerActivity : AppCompatActivity() {
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var binding: ActivityMessengerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBottomDrawer()
        initNavDrawer()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return toggle.onOptionsItemSelected(item)
    }

    private fun initBottomDrawer() {
        binding = ActivityMessengerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_messenger)

        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.navigation_chats)
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    private fun initNavDrawer() {
        val drawerLayout: DrawerLayout = findViewById(R.id.container)
        val navigationView: NavigationView = findViewById(R.id.navigation_view)

        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.drawer_settings -> println("Settings")
                R.id.drawer_contact -> println("Contact")
                R.id.drawer_logout -> println("Log Out")
                R.id.drawer_qr -> println("QR code")
            }
            true
        }
    }

    override fun onBackPressed() {
        DBAgent.getChild(
            FirebaseAuthAgent.getReference().child("users"),
            "+12345678900",
            object : IWait {
                override fun onSuccess(snapshot: DataSnapshot) {
                    println(snapshot.value.toString())
                }

                override fun onFail() {
                    TODO("Not yet implemented")
                }

                override fun onStart() {
                    // ok
                }

            }
        )
    }
}