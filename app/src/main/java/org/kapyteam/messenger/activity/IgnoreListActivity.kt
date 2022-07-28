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
import com.google.android.material.floatingactionbutton.FloatingActionButton
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
    private lateinit var dbReference: DatabaseReference
    private lateinit var dbReferenceUsers: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var phone: String
    private lateinit var refresh: FloatingActionButton
    private var archiveList: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ignore_list)
        dbReference = FirebaseDatabase.getInstance().getReference("chats")
        dbReferenceUsers = FirebaseDatabase.getInstance().getReference("users")

        if (intent.hasExtra("phone")) {
            phone = intent.getStringExtra("phone")!!
        } else {
            finish()
        }

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        recyclerView = findViewById(R.id.chats_recycler_view_ignore)
        recyclerView.layoutManager = LinearLayoutManager(this)
        createDialogList()
        CallAgent.prepareListener(this, phone)

        refresh = findViewById(R.id.refresh)

        refresh.setOnClickListener {
            createDialogList()
        }
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
                if (snapshot.hasChildren()) {
                    snapshot.children.forEach {
                        if (it.hasChild("members")) {
                            val members = it.child("members").value as MutableList<String>
                            if (members.contains(phone)) {
                                data.add(members[if (members[0] == phone) 1 else 0])
                            }
                        }
                    }
                }

                val profiles = mutableListOf<Profile>()

                dbReferenceUsers.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val archiveList = mutableListOf<String>()
                        for (profile in snapshot.children) {
                            if (profile.hasChild("phone")) {
                                if (profile.hasChild("archiveList")) {
                                    if (snapshot.child(phone).child("archiveList").hasChildren()) {
                                        snapshot.child(phone).child("archiveList").children.forEach { child ->
                                            if (child.value != "") archiveList.add(child.value.toString())
                                        }
                                    }
                                }
                                if (profile.child("phone").value.toString() in data && profile.child(
                                        "phone"
                                    ).value.toString() in archiveList
                                ) {
                                    profiles.add(Profile.parse(profile.value as Map<*, *>, false))
                                }
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

    override fun onBackPressed() {}
}