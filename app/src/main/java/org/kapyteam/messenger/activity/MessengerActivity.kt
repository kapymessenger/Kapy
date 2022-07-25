/*
 * This file is a part of Kapy Messenger project.
 * Original link: https://github.com/kapymessenger/Kapy
 */

package org.kapyteam.messenger.activity

import android.annotation.SuppressLint
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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.*
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.kapyteam.messenger.R
import org.kapyteam.messenger.database.CallAgent
import org.kapyteam.messenger.database.DBAgent
import org.kapyteam.messenger.database.FirebaseAuthAgent
import org.kapyteam.messenger.databinding.ActivityMessengerBinding
import org.kapyteam.messenger.model.Profile
import org.kapyteam.messenger.threading.NewDialogActivityTask
import org.kapyteam.messenger.util.DialogUtil

class ChatsRecyclerAdapter(
    private val chats: List<Profile>,
    private val activity: Activity,
    private val intent: Intent,
    private val phone: String
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

    override fun onBindViewHolder(
        holder: MyViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {

        holder.itemView.setOnClickListener {
            FirebaseAuthAgent.getReference()
            intent.putExtra("member", chats[position])
            intent.putExtra("phone", phone)
            activity.startActivity(intent)
        }

        val refer = FirebaseAuthAgent
            .getReference()
            .child("chats")
            .ref


        refer
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val child = getChild(snapshot, position)
                    refer
                        .child(child)
                        .child("messages")
                        .orderByKey()
                        .limitToLast(1)
                        .addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot_: DataSnapshot) {
                                applyMetadata(snapshot_, holder)
//                                if (shouldUpdate) {
//                                    val json =
//                                        DialogUtil.loadMessagesMetadata(activity.applicationContext).asJsonObject
//
//                                    snapshot_.children.first().child("content").value.toString()
//                                        .let {
//                                            if (json.has(child)) {
//                                                updateJson(json, child, it)
//                                            } else {
//                                                json.add(child, JsonParser.parseString(it))
//                                                DialogUtil.saveMessagesMetadata(json, activity.applicationContext)
//                                            }
//                                        }
//                                }
                            }

                            override fun onCancelled(error: DatabaseError) {}
                        })
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        holder.contactName.text = chats[position].nickname
        holder.contactMessageCount.text = "1"
    }

    private fun updateJson(json: JsonObject, target: String, msg: String) {
        val new = JsonObject()

        json.keySet().iterator().let {
            while (it.hasNext()) {
                val key = it.next()
                if (key == target) {
                    new.add(key, JsonParser.parseString(msg))
                } else {
                    new.add(key, json.get(key))
                }
            }
        }

        DialogUtil.saveMessagesMetadata(new, activity.applicationContext)
    }

    private fun getChild(snapshot: DataSnapshot, position: Int): String {
        return if (snapshot.hasChild("${phone}&${chats[position].phone}")) {
            "${phone}&${chats[position].phone}"
        } else if (snapshot.hasChild("${chats[position].phone}&${phone}")) {
            "${chats[position].phone}&${phone}"
        } else {
            "null"
        }
    }

    private fun applyMetadata(snapshot: DataSnapshot, holder: MyViewHolder) {
        snapshot.children.first().let {
            holder.contactLastMessage.text = it
                .child("content")
                .value.toString()

            holder.contactLastMessageTime.text = it
                .child("createTime")
                .value.toString()
        }
    }

    override fun getItemCount(): Int {
        return chats.size
    }
}

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
            val task = NewDialogActivityTask(
                this@MessengerActivity,
                listOf(
                    "+12345678900",
                    "+12345678902",
                    "+12345678901",
                    "+12345678902",
                    "+12345678903"
                ),
                phone
            )
            task.execute()
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
                        if (members[0] == phone) {
                            data.add(members[1])
                        } else {
                            data.add(members[0])
                        }
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
                R.id.drawer_qr -> println("QR code")
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