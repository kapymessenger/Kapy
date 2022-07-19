/*
 * This file is a part of Kapy Messenger project.
 * Original link: https://github.com/kapymessenger/Kapy
 */

package org.kapyteam.messenger.database

import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class FirebaseAuthAgent {
    companion object {
        @JvmStatic
        private var auth: FirebaseAuth = FirebaseAuth.getInstance()

        fun getInstance(): FirebaseAuth = auth

        fun getCurrentUser(): FirebaseUser? = auth.currentUser

        fun phoneAuth(phone: String, activity: Activity) {
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phone,
                120,
                TimeUnit.SECONDS,
                activity,
                verifyCallback())
        }

        private fun verifyCallback() =
            object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(
                    credential: PhoneAuthCredential
                ) {

                }

                override fun onVerificationFailed(
                    e: FirebaseException
                ) {

                }

                override fun onCodeSent(
                    id: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {

                }
            }
    }
}