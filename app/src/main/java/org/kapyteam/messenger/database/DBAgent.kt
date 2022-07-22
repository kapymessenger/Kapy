package org.kapyteam.messenger.database

import android.app.Activity
import java.time.LocalDateTime

object DBAgent {
    fun parseContacts(activity: Activity) {
//            val contacts = listOf("+12345678900", "+12345678901", "+12345678902")
//            for (contact in contacts) {
//                if (isInDB(contact)) {
//                    getProfile(FirebaseAuthAgent.getReference().child("users"), contact, object : IWait {
//                        override fun onSuccess(snapshot: DataSnapshot) {
//                            if (snapshot.value != null) {
//                                println("Contact: $contact")
//                                profiles.add(Profile.parse(snapshot.value as Map<*, *>))
//                            } else {
//                                println("Contact $contact not found in DB")
//                            }
//                        }
//
//                        override fun onFail() {
//                            println("Contact $contact not found in DB")
//                        }
//
//                        override fun onStart() {
//                            println("Started parsing contact $contact")
//                        }
//                    })
//                }
//            }
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