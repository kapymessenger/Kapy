/*
 * This file is a part of Kapy Messenger project.
 * Original link: https://github.com/kapymessenger/Kapy
 */

package org.kapyteam.messenger.activity.chats

import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
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
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import org.kapyteam.messenger.R
import org.kapyteam.messenger.activity.IgnoreListActivity
import org.kapyteam.messenger.activity.TextEditor
import org.kapyteam.messenger.activity.profile.ProfileActivity
import org.kapyteam.messenger.activity.init.GreetingActivity
import org.kapyteam.messenger.activity.profile.ProfileEditingActivity
import org.kapyteam.messenger.component.ChatsRecyclerAdapter
import org.kapyteam.messenger.database.CallAgent
import org.kapyteam.messenger.database.DBAgent
import org.kapyteam.messenger.database.FirebaseAuthAgent
import org.kapyteam.messenger.model.Profile
import org.kapyteam.messenger.util.SerializableObject
import com.dolatkia.animatedThemeManager.AppTheme
import com.dolatkia.animatedThemeManager.ThemeActivity
import com.dolatkia.animatedThemeManager.ThemeAnimationListener
import com.dolatkia.animatedThemeManager.ThemeManager
import org.kapyteam.messenger.databinding.ActivityMessengerBinding


interface MyAppTheme : AppTheme {
    fun firstActivityBackgroundColor(context: Context): Int
    fun firstActivityTextColor(context: Context): Int
    fun firstActivityIconColor(context: Context): Int
}

class LightTheme : MyAppTheme {

    override fun id(): Int { // set unique iD for each theme
        return 0
    }

    override fun firstActivityBackgroundColor(context: Context): Int {
        return ContextCompat.getColor(context, R.color.white)
    }

    override fun firstActivityTextColor(context: Context): Int {
        return ContextCompat.getColor(context, R.color.black)
    }

    override fun firstActivityIconColor(context: Context): Int {
        return ContextCompat.getColor(context, R.color.black)
    }
}

class DarkTheme : MyAppTheme {

    override fun id(): Int { // set unique iD for each theme
        return 1
    }

    override fun firstActivityBackgroundColor(context: Context): Int {
        return ContextCompat.getColor(context, R.color.black)
    }

    override fun firstActivityTextColor(context: Context): Int {
        return ContextCompat.getColor(context, R.color.white)
    }

    override fun firstActivityIconColor(context: Context): Int {
        return ContextCompat.getColor(context, R.color.white)
    }
}

class MyThemeAnimationListener(var context: Context, var drawer: DrawerLayout) : ThemeAnimationListener{
    override fun onAnimationStart(animation: Animator) {
    }

    override fun onAnimationEnd(animation: Animator) {
        println("Хуй")
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            println("Большой хуй")
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            println("Гигантский хуй")
        }
    }

    override fun onAnimationCancel(animation: Animator) {
    }

    override fun onAnimationRepeat(animation: Animator) {
    }
}

class MessengerActivity : ThemeActivity() {
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var binding: ActivityMessengerBinding
    private lateinit var addChatBtn: FloatingActionButton
    private lateinit var dbReference: DatabaseReference
    private lateinit var dbReferenceUsers: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var phone: String
    private lateinit var binder: ActivityMessengerBinding
    private lateinit var drawerLayout: DrawerLayout

    override fun syncTheme(appTheme: AppTheme) {
        // change ui colors with new appThem here

        val myAppTheme = appTheme as MyAppTheme
        // set background color
        binder.root.setBackgroundColor(myAppTheme.firstActivityBackgroundColor(this))

        //set text color
//        binder.navView.setBackgroundColor(myAppTheme.firstActivityBackgroundColor(this))
        binder.navigationView.setBackgroundColor(myAppTheme.firstActivityBackgroundColor(this))
        binder.navigationView.itemBackground = myAppTheme.firstActivityBackgroundColor(this).toDrawable()
        binder.navigationView.itemTextColor = ColorStateList.valueOf(myAppTheme.firstActivityTextColor(this))

    }

    // to save the theme for the next time, save it in onDestroy() (exp: in pref or DB) and return it here.
// it just used for the first time (first activity).
    override fun getStartTheme(): AppTheme {
        return LightTheme()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBottomDrawer()

        binder = ActivityMessengerBinding.inflate(LayoutInflater.from(this))
        setContentView(binder.root)

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
                                profiles.add(Profile.parse(snap.value as Map<*, *>, false))
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
                                profiles.add(Profile.parse(profile.value as Map<*, *>, false))
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

//        val navView: BottomNavigationView = binding.navView
//
//        val navController = findNavController(R.id.nav_host_fragment_activity_messenger)
//
//        val appBarConfiguration = AppBarConfiguration(
//            setOf(R.id.navigation_chats)
//        )
//        setupActionBarWithNavController(navController, appBarConfiguration)
//        navView.setupWithNavController(navController)
    }

    private fun initNavDrawer() {
        drawerLayout= findViewById(R.id.container)
        val navigationView: NavigationView = findViewById(R.id.navigation_view)
        val header = navigationView.inflateHeaderView(R.layout.drawer_header)

        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val name: TextView = header.findViewById(R.id.drawer_person_name)
        val nickname: TextView = header.findViewById(R.id.drawer_person_nickname)
        val phoneText: TextView = header.findViewById(R.id.drawer_person_phone)
        val avatar: CircleImageView = header.findViewById(R.id.drawer_person_avatar)

        FirebaseAuthAgent
            .getReference()
            .child("users")
            .child(phone)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    phoneText.text = phone
                    name.text =
                        "${snapshot.child("firstname").value} ${snapshot.child("lastname").value}"
                    nickname.text = "@${snapshot.child("nickname").value}"

                    snapshot.child("photo").value.toString().let {
                        if (it != "") Picasso.get().load(it).into(avatar)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        avatar.setOnClickListener {
            val intent = Intent(this, ProfileEditingActivity::class.java)
            intent.putExtra("phone", phone)
            startActivity(intent)
        }

        navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.drawer_settings -> println("Settings")
                R.id.drawer_contact -> println("Contact")
                R.id.theme_switch ->{
                    println(ThemeManager.instance.getCurrentTheme()
                        ?.id())
                    if (ThemeManager.instance.getCurrentTheme()
                            ?.id() == 0
                    ) {
                        ThemeManager.instance.changeTheme(DarkTheme(), navigationView)
                    } else if (ThemeManager.instance.getCurrentTheme()
                            ?.id() == 1
                    ) {
                        ThemeManager.instance.changeTheme(LightTheme(), navigationView)

                    }

                }
                R.id.drawer_qr -> {
                    scanCode()
                }
                R.id.ignore_list -> {
                    val intent = Intent(
                        this,
                        IgnoreListActivity::class.java
                    )
                    intent.putExtra("phone", phone)
                    startActivity(intent)
                }
                R.id.notes ->{
                    val intent = Intent(
                        this,
                        TextEditor::class.java
                    )
                    startActivity(intent)
                }
                R.id.drawer_logout -> {
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
        setThemeAnimationListener(MyThemeAnimationListener(this, drawerLayout))
    }

    public fun openDrawer(){
        drawerLayout.openDrawer(drawerLayout)
    }

    private fun scanCode() {
        val options = ScanOptions()
        options.setPrompt("Point the camera at the QR-code")
        options.setBeepEnabled(false)
        options.setOrientationLocked(false)
        barLauncher.launch(options)
    }

    private var barLauncher = registerForActivityResult(
        ScanContract()
    ) { result: ScanIntentResult ->
        if (result.contents != null) {
            FirebaseDatabase
                .getInstance()
                .getReference("users")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.hasChild(result.contents)) {
                            val intent = Intent(
                                this@MessengerActivity,
                                ProfileActivity::class.java
                            )
                            intent.putExtra(
                                "profile", Profile(
                                    firstname = snapshot.child(result.contents)
                                        .child("firstname").value.toString(),
                                    lastname = snapshot.child(result.contents)
                                        .child("lastname").value.toString(),
                                    phone = snapshot.child(result.contents)
                                        .child("phone").value.toString(),
                                    nickname = snapshot.child(result.contents)
                                        .child("nickname").value.toString(),
                                    photo = snapshot.child(result.contents)
                                        .child("photo").value.toString(),
                                    lastSeen = snapshot.child(result.contents)
                                        .child("lastSeen").value.toString(),
                                    online = snapshot.child(result.contents).child("online")
                                        .getValue(Boolean::class.java)!!
                                )
                            )
                            startActivity(intent)
                        } else {
                            val builder =
                                AlertDialog.Builder(this@MessengerActivity)
                            builder.setTitle("Error")
                            builder.setMessage("Profile not recognized. Try again.")
                            builder.setPositiveButton(
                                "OK"
                            ) { dialogInterface, _ -> dialogInterface.dismiss() }.show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }
    }

    override fun onBackPressed() {}
}