package org.kapyteam.messenger.database

import android.content.Context
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import org.kapyteam.messenger.model.Profile
import org.kapyteam.messenger.util.Contacts
import org.kapyteam.messenger.util.IWait

class DBAgent {
    companion object {
        fun parseContacts(context: Context): MutableList<Profile> {
            val contacts = Contacts.getContacts(context)
            val profiles = mutableListOf<Profile>()
            for (contact in contacts) {
//                if (isInDB(contact)) profiles.add(getProfileByPhone(contact)!!)
            }
            return profiles
        }

        fun isInDB(phone: String): Boolean {
            return FirebaseAuthAgent
                .getReference()
                .child("users")
                .child(phone)
                .get()
                .isSuccessful
        }

        fun getChild(ref: DatabaseReference, child: String, _interface: IWait) {
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

//        fun getProfileByPhone(phone: String): Profile {
//            val ref = FirebaseAuthAgent
//                .getReference()
//                .child("users")
//                .child(phone)
//
//            var metadata: Map<*, *>? = null
//
//            ref.addListenerForSingleValueEvent(object : ValueEventListener {
//                override fun onCancelled(p0: DatabaseError) {
//                    println("DDDDDDDDDDDDDDD")
//                }
//
//                override fun onDataChange(p0: DataSnapshot) {
//                    p0.value
//                }
//            })
//        }
    }
}