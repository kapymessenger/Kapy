/*
 * This file is a part of Kapy Messenger project.
 * Original link: https://github.com/kapymessenger/Kapy
 */

package org.kapyteam.messenger.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.*
import org.kapyteam.messenger.R
import org.kapyteam.messenger.database.DBAgent
import org.kapyteam.messenger.database.FirebaseAuthAgent
import org.kapyteam.messenger.databinding.ActivityMessengerBinding
import org.kapyteam.messenger.model.Profile
import org.kapyteam.messenger.threading.NewDialogActivityTask

class ChatsRecyclerAdapter(
    private val chats: List<Profile>,
    private val activity: Activity,
    private val intent: Intent
) : RecyclerView.Adapter<ChatsRecyclerAdapter.MyViewHolder>() {
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val contactImage: ImageView = itemView.findViewById(R.id.contact_image)
        val contactName: TextView = itemView.findViewById(R.id.contact_name)
        val contactLastMessage: TextView = itemView.findViewById(R.id.contact_last_message)
        val contactLastMessageTime: TextView = itemView.findViewById(R.id.contact_last_message_time)
        val contactMessageCount: TextView = itemView.findViewById(R.id.contact_message_count)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.layout_dialog, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.itemView.setOnClickListener {
            FirebaseAuthAgent.getReference()
            intent.putExtra("member", chats[position])
            activity.startActivity(intent)
        }
        holder.contactName.text = chats[position].nickname
        holder.contactLastMessage.text = "some shit"
        holder.contactLastMessageTime.text = "15:00"
        holder.contactMessageCount.text = "1"
    }

    override fun getItemCount(): Int {
        return chats.size
    }
}

class MessengerActivity : AppCompatActivity() {
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var binding: ActivityMessengerBinding
    private lateinit var dbReference: DatabaseReference
    private lateinit var dbReferenceUsers: DatabaseReference
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBottomDrawer()
        initNavDrawer()

        dbReference = FirebaseDatabase.getInstance().getReference("chats")
        dbReferenceUsers = FirebaseDatabase.getInstance().getReference("users")

        DBAgent.setOnline(true)

        recyclerView = findViewById(R.id.chats_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        createDialogList()
    }

    private fun createDialogList() {
        val data = mutableListOf<String>()
        dbReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (dialog in snapshot.children) {
                    val members = dialog.child("members").value as MutableList<String>
                    if (members.contains("+12345678900")) {
                        data.add(members[1])
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
                            Intent(this@MessengerActivity, ChatActivity::class.java)
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

        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

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
                R.id.drawer_qr -> println("QR code")
            }
            true
        }
    }

    override fun onBackPressed() {
        val task = NewDialogActivityTask(
            this@MessengerActivity,
            listOf("+12345678900", "+12345678902", "+12345678901", "+12345678902", "+12345678903"),
        )
        task.execute()
    }
}