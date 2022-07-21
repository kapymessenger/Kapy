/*
 * This file is a part of Kapy Messenger project.
 * Original link: https://github.com/kapymessenger/Kapy
 */

/*
 * This file is a part of Kapy Messenger project.
 * Original link: https://github.com/kapymessenger/Kapy
 */

package org.kapyteam.messenger.activity

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import org.kapyteam.messenger.R
import org.kapyteam.messenger.databinding.ActivityMessengerBinding

class MessengerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMessengerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMessengerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_messenger)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_chats
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }
}