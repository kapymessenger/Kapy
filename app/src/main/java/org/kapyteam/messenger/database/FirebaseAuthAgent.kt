/*
 * This file is a part of Kapy Messenger project.
 * Original link: https://github.com/kapymessenger/Kapy
 */

package org.kapyteam.messenger.database

import android.app.Activity
import androidx.annotation.NonNull
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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

        fun phoneAuth(phone: String, activity: Activity) {
            // надо потом перестать юзать депрекейтед
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phone,
                300,
                TimeUnit.SECONDS,
                activity,
                verifyCallback())
        }

        fun registerProfile(profile: Profile) {
            dbReference.addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(@NonNull snapshot: DataSnapshot) {
                        snapshot.child("users").let {
                            if (it.hasChild(profile.phone) || it.hasChild(profile.nickname)) {

                            } else {
                                dbReference
                                    .child("users")
                                    .child(profile.phone)
                                    .setValue(profile)
                                }
                            }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                }
            )
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