package org.kapyteam.messenger.activity.profile

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import org.kapyteam.messenger.R
import org.kapyteam.messenger.activity.chats.MessengerActivity
import org.kapyteam.messenger.database.FirebaseAuthAgent
import org.kapyteam.messenger.model.Profile
import org.kapyteam.messenger.util.FileUtil

class SetupProfileActivity : AppCompatActivity() {
    private lateinit var firstnameEdit: EditText
    private lateinit var lastnameEdit: EditText
    private lateinit var nicknameEdit: EditText
    private lateinit var avatar: ImageView
    private var isUploading = false
    private var selectedImage: Uri? = null
    private lateinit var finishRegistrationButton: Button
    private lateinit var phoneNumber: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup_profile)

        firstnameEdit = findViewById(R.id.firstname_edit)
        lastnameEdit = findViewById(R.id.lastname_edit)
        nicknameEdit = findViewById(R.id.nickname_edit)
        avatar = findViewById(R.id.setup_image)
        finishRegistrationButton = findViewById(R.id.register_finish)
        phoneNumber = intent.getStringExtra("phone")!!

        finishRegistrationButton.setOnClickListener {
            register()
        }

        avatar.setOnClickListener {
            isUploading = true
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 45)
        }
    }

    private fun saveImageToStorage(profile: Profile) {
        if (isUploading) {
            if (selectedImage != null) {
                val reference = FirebaseStorage
                    .getInstance()
                    .reference
                    .child(phoneNumber)

                reference
                    .putFile(selectedImage!!)
                    .addOnSuccessListener {
                        val result = it.metadata!!.reference!!.downloadUrl
                        // ЭТО ГОВНО НЕ ТРОГАТЬ!!
                        result.addOnSuccessListener(object : OnSuccessListener<Uri> {
                            override fun onSuccess(p0: Uri?) {
                                profile.photo = p0.toString()
                                FirebaseAuthAgent.registerProfile(profile)
                                FileUtil.saveData("{\"phone\": \"$phoneNumber\"}", this@SetupProfileActivity)

                                val intent =
                                    Intent(this@SetupProfileActivity, MessengerActivity::class.java)
                                intent.putExtra("phone", phoneNumber)
                                startActivity(intent)
                            }

                        })
                    }
            }
        } else {
            FirebaseAuthAgent.registerProfile(profile)
            FileUtil.saveData("{\"phone\": \"$phoneNumber\"}", this)

            val intent = Intent(this, MessengerActivity::class.java)
            intent.putExtra("phone", phoneNumber)
            startActivity(intent)
        }
    }

    private fun register() {
        if (firstnameEdit.text.isEmpty() || lastnameEdit.text.isEmpty() || nicknameEdit.text.isEmpty()) {
            Toast.makeText(
                applicationContext,
                "Please fill all fields",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        saveImageToStorage(
            Profile(
                firstname = firstnameEdit.text.toString(),
                lastname = lastnameEdit.text.toString(),
                phone = phoneNumber,
                nickname = nicknameEdit.text.toString(),
                archiveList = listOf("")
            )
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        selectedImage = data?.data
        avatar.setImageURI(selectedImage)
    }
}