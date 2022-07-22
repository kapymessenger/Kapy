package org.kapyteam.messenger.database

import android.app.Activity
import android.content.Context
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import org.kapyteam.messenger.model.Profile
import org.kapyteam.messenger.util.Contacts
import org.kapyteam.messenger.util.IWait
import java.time.LocalDateTime

class DBAgent {
    companion object {
        private val profiles  = mutableListOf<Profile>()

        fun parseContacts(activity: Activity) {
            val contacts = listOf("+12345678900", "+12345678901", "+12345678902")
            for (contact in contacts) {
                if (isInDB(contact)) {
                    getProfile(FirebaseAuthAgent.getReference().child("users"), contact, object : IWait {
                        override fun onSuccess(snapshot: DataSnapshot) {
                            if (snapshot.value != null) {
                                println("Contact: $contact")
                                profiles.add(Profile.parse(snapshot.value as Map<*, *>))
                            } else {
                                println("Contact $contact not found in DB")
                            }
                        }

                        override fun onFail() {
                            println("Contact $contact not found in DB")
                        }

                        override fun onStart() {
                            println("Started parsing contact $contact")
                        }
                    })
                }
            }
        }

        fun isInDB(phone: String): Boolean {
            return true
        }

        fun getProfile(ref: DatabaseReference, child: String, _interface: IWait) {
            _interface.onStart()
            ref.child(child).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    _interface.onFail()
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    _interface.onSuccess(snapshot)
                }
            })
        }

        fun setOnline(online: Boolean) {
            FirebaseAuthAgent.getReference().child("users").child("+12345678900").let {
                if (online) {
                    it.child("online").setValue(true)
                } else {
                   it.child("lastSeen").setValue(LocalDateTime.now())
                    it.child("online").setValue(false)
                }
            }
        }
    }
}