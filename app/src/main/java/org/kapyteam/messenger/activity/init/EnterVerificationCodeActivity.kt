package org.kapyteam.messenger.activity.init

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.chaos.view.PinView
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import org.kapyteam.messenger.R
import org.kapyteam.messenger.activity.chats.MessengerActivity
import org.kapyteam.messenger.activity.profile.SetupProfileActivity
import org.kapyteam.messenger.database.FirebaseAuthAgent
import org.kapyteam.messenger.util.FileUtil

class EnterVerificationCodeActivity : AppCompatActivity() {
    private lateinit var verificationId: String
    private lateinit var verificationCode: PinView
    private lateinit var phone: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter_verification_code)
        supportActionBar?.hide()

        verificationCode = findViewById(R.id.verification_code)
        verificationId = intent.getStringExtra("verificationId")!!
        phone = intent.getStringExtra("phone")!!
        verificationCode.requestFocus()

        val inputMethodManager: InputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        inputMethodManager.toggleSoftInput(
            InputMethodManager.SHOW_FORCED,
            InputMethodManager.HIDE_IMPLICIT_ONLY
        )

        verificationCode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                p0.toString().let {
                    if (it.length == 6) {
                        verifyCode(it)
                    }
                }
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
    }

    private fun verifyCode(code: String) {
        val credentials = PhoneAuthProvider.getCredential(
            verificationId,
            code
        )

        FirebaseAuthAgent
            .getInstance()
            .signInWithCredential(credentials)
            .addOnFailureListener {
                verificationCode.text?.clear()
                Toast.makeText(this, "Invalid code. Please try again", 2)
            }
            .addOnCompleteListener {
                FirebaseAuthAgent
                    .getReference()
                    .child("users")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.hasChild(phone)) {
                                val intent = Intent(
                                    this@EnterVerificationCodeActivity,
                                    MessengerActivity::class.java
                                )
                                intent.putExtra("phone", phone)
                                FileUtil.saveData(
                                    "{\"phone\": \"$phone\"}",
                                    this@EnterVerificationCodeActivity
                                )
                                startActivity(intent)
                            } else {
                                val intentToSetup = Intent(
                                    applicationContext,
                                    SetupProfileActivity::class.java
                                )
                                intentToSetup.putExtra("phone", phone)
                                startActivity(intentToSetup)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })

            }
    }
}