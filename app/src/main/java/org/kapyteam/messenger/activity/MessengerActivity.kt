/*
 * This file is a part of Kapy Messenger project.
 * Original link: https://github.com/kapymessenger/Kapy
 */

package org.kapyteam.messenger.activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.*
import org.kapyteam.messenger.R
import org.kapyteam.messenger.component.ChatsRecyclerAdapter
import org.kapyteam.messenger.database.CallAgent
import org.kapyteam.messenger.database.DBAgent
import org.kapyteam.messenger.database.FirebaseAuthAgent
import org.kapyteam.messenger.databinding.ActivityMessengerBinding
import org.kapyteam.messenger.model.Profile
import org.kapyteam.messenger.util.SerializableObject

class MessengerActivity : AppCompatActivity() {
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var binding: ActivityMessengerBinding
    private lateinit var addChatBtn: FloatingActionButton
    private lateinit var dbReference: DatabaseReference
    private lateinit var dbReferenceUsers: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var phone: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBottomDrawer()

        dbReference = FirebaseDatabase.getInstance().getReference("chats")
        dbReferenceUsers = FirebaseDatabase.getInstance().getReference("users")
        phone = intent.getStringExtra("phone")!!

        initNavDrawer()

        addChatBtn = findViewById(R.id.addChat)

        addChatBtn.setOnClickListener {
            // TODO: get contact list from device
            val contacts = mutableListOf(
                "+12345678900",
                "+12345678902",
                "+12345678901",
                "+12345678902",
                "+12345678903"
            )

            val profiles = mutableListOf<Profile>()

            FirebaseAuthAgent
                .getReference()
                .child("users")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.children.forEach { snap ->
                            val contact = contacts.find { snap.key == it }
                            if (contact != null) {
                                profiles.add(Profile.parse(snap.value as Map<*, *>))
                            }
                        }
                        val intent = Intent(
                            this@MessengerActivity,
                            CreateDialogActivity::class.java
                        )
                        intent.putExtra("phone", phone)
                        intent.putExtra("profiles", SerializableObject(profiles))
                        startActivity(intent)
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }

        recyclerView = findViewById(R.id.chats_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        createDialogList()
        CallAgent.prepareListener(this, phone)
    }

    private fun createDialogList() {
        val data = mutableListOf<String>()
        dbReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                DBAgent.setOnline(true, phone)
                for (dialog in snapshot.children) {
                    val members = dialog.child("members").value as MutableList<String>
                    if (members.contains(phone)) {
                        data.add(members[if (members[0] == phone) 1 else 0])
                    }
                }

                val profiles = mutableListOf<Profile>()

                dbReferenceUsers.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (profile in snapshot.children) {
                            if (profile.child("phone").getValue(String::class.java) in data) {
                                profiles.add(Profile.parse(profile.value as Map<*, *>))
                            }
                        }

                        recyclerView.adapter = ChatsRecyclerAdapter(
                            profiles,
                            this@MessengerActivity,
                            Intent(this@MessengerActivity, ChatActivity::class.java),
                            phone
                        )
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
            }

            override fun onCancelled(error: DatabaseError) {}
        })
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
        val header = navigationView.inflateHeaderView(R.layout.drawer_header)

        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val name: TextView = header.findViewById(R.id.drawer_person_name)
        val nickname: TextView = header.findViewById(R.id.drawer_person_nickname)
        val phoneText: TextView = header.findViewById(R.id.drawer_person_phone)

        FirebaseAuthAgent
            .getReference()
            .child("users")
            .child(phone)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    phoneText.text = phone
                    name.text =
                        "${snapshot.child("firstname").value} ${snapshot.child("lastname").value}"
                    nickname.text = "@${snapshot.child("nickname").value}"
                    supportActionBar?.setDisplayHomeAsUpEnabled(true)
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.drawer_settings -> println("Settings")
                R.id.drawer_contact -> println("Contact")
                R.id.drawer_logout -> {
                    FirebaseAuthAgent.getInstance().signOut()
                    val intent = Intent(
                        this,
                        GreetingActivity::class.java
                    )
                    startActivity(intent)
                }
                R.id.drawer_qr -> {
                    val intent = Intent(
                        this,
                        QRScanActivity::class.java
                    )
                    startActivity(intent)
                }
            }
            true
        }
    }

    override fun onDestroy() {
        DBAgent.setOnline(false, phone)
        super.onDestroy()
    }

    override fun onResume() {
        DBAgent.setOnline(true, phone)
        super.onResume()
    }

    override fun onRestart() {
        DBAgent.setOnline(true, phone)
        super.onRestart()
    }

    override fun onBackPressed() {}
}