package org.kapyteam.messenger.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import com.chaos.view.PinView
import com.google.firebase.auth.PhoneAuthProvider
import org.kapyteam.messenger.R
import org.kapyteam.messenger.database.FirebaseAuthAgent

class EnterVerificationCodeActivity : AppCompatActivity() {
    private lateinit var verificationId: String
    private lateinit var verificationCode: PinView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter_verification_code)
        supportActionBar?.hide()

        verificationCode = findViewById(R.id.verification_code)
        verificationId = intent.getStringExtra("verificationId")!!
        verificationCode.requestFocus()

        val inputMethodManager: InputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        inputMethodManager.toggleSoftInput(
            InputMethodManager.SHOW_FORCED,
            InputMethodManager.HIDE_IMPLICIT_ONLY
        )

        verificationCode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                p0.toString().let {
                    if (it.length == 6) {
                        verifyCode(it)
                    }
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }

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
            .addOnCompleteListener {
                val intentToSetup = Intent(applicationContext, SetupProfileActivity::class.java)
                intentToSetup.putExtra("phone", intent.getStringExtra("phone"))
                startActivity(intentToSetup)
            }
    }
}