/*
 * This file is a part of Kapy Messenger project.
 * Original link: https://github.com/kapymessenger/Kapy
 */

package org.kapyteam.messenger.activity

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
import org.kapyteam.messenger.R
import org.kapyteam.messenger.databinding.ActivityMessengerBinding
import org.kapyteam.messenger.threading.NewDialogActivityTask

data class Person(
    val name: String,
    val lats_message: String,
    val last_message_time: String,
    val message_count: Int
)

class ChatsRecyclerAdapter(private val chats: List<Person>) :
    RecyclerView.Adapter<ChatsRecyclerAdapter.MyViewHolder>() {
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
        holder.contactName.text = chats[position].name
        holder.contactLastMessage.text = chats[position].lats_message
        holder.contactLastMessageTime.text = chats[position].last_message_time
        holder.contactMessageCount.text = chats[position].message_count.toString()
    }

    override fun getItemCount(): Int {
        return chats.size
    }
}

class MessengerActivity : AppCompatActivity() {
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var binding: ActivityMessengerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBottomDrawer()
        initNavDrawer()

        val recyclerView: RecyclerView = findViewById(R.id.chats_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ChatsRecyclerAdapter(fillList())
    }

    private fun fillList(): List<Person> {
        val data = mutableListOf<Person>()
        (0..30).forEach { _ -> data.add(Person("Еблан", "Пошёл нахуй", "15:33", 99)) }
        return data
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
        val task = NewDialogActivityTask(
            this@MessengerActivity,
            listOf("+12345678900", "+12345678902", "+12345678901", "+12345678902", "+12345678903"),
        )
        task.execute()
    }
}