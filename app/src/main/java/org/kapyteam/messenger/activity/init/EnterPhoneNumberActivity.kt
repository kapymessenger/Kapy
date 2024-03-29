package org.kapyteam.messenger.activity.init

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.kapyteam.messenger.R
import org.kapyteam.messenger.activity.chats.MessengerActivity
import org.kapyteam.messenger.database.FirebaseAuthAgent


class EnterPhoneNumberActivity : AppCompatActivity() {
    private lateinit var phoneEdit: EditText
    private lateinit var continueButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter_phone_number)

        phoneEdit = findViewById(R.id.register_phone_number)
        continueButton = findViewById(R.id.register_continue)

        if (FirebaseAuthAgent.getCurrentUser() != null) {
            startActivity(Intent(this, MessengerActivity::class.java))
            finish()
        }

        continueButton.setOnClickListener {
            phoneEdit.text.toString().let {
                if (PhoneNumberUtils.isGlobalPhoneNumber(it)) {

                    ProgressDialog.show(this, "", "Please wait...", true)

                    val intent = Intent(this, EnterVerificationCodeActivity::class.java)
                    intent.putExtra("phone", it)
                    FirebaseAuthAgent.phoneAuth(this, intent)
                } else {
                    Toast.makeText(this, "Please, enter a valid phone number", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}