/*
 * This file is a part of Kapy Messenger project.
 * Original link: https://github.com/kapymessenger/Kapy
 */

package org.kapyteam.messenger.database

import android.app.Activity
import android.content.Intent
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.database.FirebaseDatabase
import org.kapyteam.messenger.model.Profile
import java.util.concurrent.TimeUnit

class FirebaseAuthAgent {
    companion object {
        private var auth: FirebaseAuth = FirebaseAuth.getInstance()
        private var dbReference = FirebaseDatabase
            .getInstance()
            .getReferenceFromUrl("https://kapy-messenger-default-rtdb.firebaseio.com/")

        fun getInstance(): FirebaseAuth = auth

        fun getCurrentUser(): FirebaseUser? = auth.currentUser

        fun phoneAuth(activity: Activity, intent: Intent) {
            val options = PhoneAuthOptions
                .newBuilder(auth)
                .setPhoneNumber(intent.getStringExtra("phone")!!)
                .setTimeout(120, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(verifyCallback(activity, intent))
                .build()

            PhoneAuthProvider.verifyPhoneNumber(options)
        }

        fun registerProfile(profile: Profile) {
            dbReference.child("users").let {
                val id = it.push().key
                it.child(id!!).setValue(profile)
            }
        }

        private fun verifyCallback(activity: Activity, intent: Intent) =
            object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationFailed(e: FirebaseException) {

                }

                override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                    intent.putExtra("verificationId", id)
                    activity.startActivity(intent)
                }

                override fun onVerificationCompleted(p0: PhoneAuthCredential) {}
            }

        fun test() {
            println(dbReference.child("users").child(auth.uid!!).toString())
        }
    }
}