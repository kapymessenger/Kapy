package org.kapyteam.messenger.activity

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import com.chaos.view.PinView
import org.kapyteam.messenger.R

class EnterVerificationCodeActivity : AppCompatActivity() {

    private lateinit var verificationCode: PinView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter_verification_code)
        supportActionBar?.hide()

        verificationCode = findViewById(R.id.verification_code)

        verificationCode.requestFocus()
        val inputMethodManager : InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED,InputMethodManager.HIDE_IMPLICIT_ONLY)

        verificationCode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(p0.toString().length == 6){}
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })
    }
}