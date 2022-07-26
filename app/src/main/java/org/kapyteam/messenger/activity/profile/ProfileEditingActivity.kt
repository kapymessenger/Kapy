/*
 * This file is a part of Kapy Messenger project.
 * Original link: https://github.com/kapymessenger/Kapy
 */

package org.kapyteam.messenger.activity.profile

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import org.kapyteam.messenger.R
import org.kapyteam.messenger.activity.chats.MessengerActivity
import org.kapyteam.messenger.database.FirebaseAuthAgent
import org.kapyteam.messenger.model.Profile
import org.kapyteam.messenger.util.FileUtil

class ProfileEditingActivity : AppCompatActivity() {
    private lateinit var confirmBtn: Button
    private lateinit var cancelBtn: Button
    private lateinit var firstnameEdit: EditText
    private lateinit var lastnameEdit: EditText
    private lateinit var nicknameEdit: EditText
    private lateinit var phone: String
    private var selectedImage: Uri? = null
    private lateinit var avatarEdit: CircleImageView
    private var isUploading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_editing)

        confirmBtn = findViewById(R.id.profile_editing_confirm)
        cancelBtn = findViewById(R.id.profile_editing_cancel)
        firstnameEdit = findViewById(R.id.profile_editing_firstname)
        lastnameEdit = findViewById(R.id.profile_editing_lastname)
        nicknameEdit = findViewById(R.id.profile_editing_nickname)
        avatarEdit = findViewById(R.id.profile_editing_image)
        phone = intent.getStringExtra("phone")!!

        findProfileAndContinue()
    }

    private fun findProfileAndContinue() {
        FirebaseAuthAgent
            .getReference()
            .child("users")
            .child(phone)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val profile = Profile(
                        firstname = snapshot.child("firstname").value as String,
                        lastname = snapshot.child("lastname").value as String,
                        nickname = snapshot.child("nickname").value as String,
                        photo = snapshot.child("photo").value as String,
                        phone = phone
                    )

                    firstnameEdit.setText(profile.firstname)
                    lastnameEdit.setText(profile.lastname)
                    nicknameEdit.setText(profile.nickname)

                    if (profile.photo != "") {
                        Picasso.get().load(profile.photo).into(avatarEdit)
                    }

                    setupClickListeners(profile)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun setupClickListeners(profile: Profile) {
        avatarEdit.setOnClickListener {
            isUploading = true
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 45)
        }

        confirmBtn.setOnClickListener {
            if (isUploading) {
                if (selectedImage != null) {
                    val reference = FirebaseStorage
                        .getInstance()
                        .reference
                        .child(phone)

                    reference
                        .putFile(selectedImage!!)
                        .addOnSuccessListener {
                            val result = it.metadata!!.reference!!.downloadUrl
                            // ЭТО ГОВНО НЕ ТРОГАТЬ!!
                            result.addOnSuccessListener(object : OnSuccessListener<Uri> {
                                override fun onSuccess(p0: Uri?) {
                                    profile.photo = p0.toString()
                                    saveToServer(profile)
                                }
                            })
                        }
                }
            } else {
                saveToServer(profile)
            }
        }

        cancelBtn.setOnClickListener {
            finish()
        }
    }

    private fun saveToServer(profile: Profile) {
        if (firstnameEdit.text.isEmpty() || lastnameEdit.text.isEmpty() || nicknameEdit.text.isEmpty()) {
            Toast.makeText(
                applicationContext,
                "Please fill all fields",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        profile.firstname = firstnameEdit.text.toString()
        profile.lastname = lastnameEdit.text.toString()
        profile.nickname = nicknameEdit.text.toString()

        FirebaseAuthAgent
            .getReference()
            .child("users")
            .orderByChild("nickname")
            .equalTo(profile.nickname)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists() && nicknameEdit.text.toString() != profile.nickname) {
                        Toast.makeText(
                            applicationContext,
                            "Profile with this nickname already exists",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }
                    FirebaseAuthAgent
                        .getReference()
                        .child("users")
                        .child(phone)
                        .setValue(profile)
                        .addOnSuccessListener {
                            Toast.makeText(
                                applicationContext,
                                "Profile updated",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        selectedImage = data?.data
        avatarEdit.setImageURI(selectedImage)
    }
}