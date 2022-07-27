package org.kapyteam.messenger.activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import org.kapyteam.messenger.R
import org.kapyteam.messenger.activity.chats.ChatActivity
import org.kapyteam.messenger.component.ChatsRecyclerAdapter
import org.kapyteam.messenger.database.CallAgent
import org.kapyteam.messenger.database.DBAgent
import org.kapyteam.messenger.databinding.ActivityIgnoreListBinding
import org.kapyteam.messenger.model.Profile

class IgnoreListActivity : AppCompatActivity() {
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var binding: ActivityIgnoreListBinding
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

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)


        recyclerView = findViewById(R.id.chats_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        createDialogList()
        CallAgent.prepareListener(this, phone)


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            else -> super.onOptionsItemSelected(item)
        }
        return true
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
                            this@IgnoreListActivity,
                            Intent(this@IgnoreListActivity, ChatActivity::class.java),
                            phone
                        )
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }


    private fun initBottomDrawer() {
        binding = ActivityIgnoreListBinding.inflate(layoutInflater)
        setContentView(binding.root)


//        val navController = findNavController(R.id.nav_host_fragment_activity_messenger)
//
//        val appBarConfiguration = AppBarConfiguration(
//            setOf(R.id.navigation_chats)
//        )
//        setupActionBarWithNavController(navController, appBarConfiguration)
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