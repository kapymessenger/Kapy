/*
 * This file is a part of Kapy Messenger project.
 * Original link: https://github.com/kapymessenger/Kapy
 */

package org.kapyteam.messenger.database

import android.app.Activity
import android.content.Intent
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.kapyteam.messenger.activity.IncomingCallActivity
import org.kapyteam.messenger.model.Call
import org.kapyteam.messenger.model.Profile

object CallAgent {
    fun prepareListener(activity: Activity, self: String) {
        FirebaseDatabase
            .getInstance()
            .getReference("calls")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (child in snapshot.children) {
                        if (child.child("receiver").value.toString() == self && child.child("callState").value.toString() == "PENDING") {
                            val call = Call(
                                id = child.child("id").value.toString(),
                                sender = child.child("sender").value.toString(),
                                receiver = self,
                                callState = child.child("callState").value.toString(),
                                callType = child.child("callType").value.toString()
                            )
                            val intent = Intent(
                                activity,
                                IncomingCallActivity::class.java
                            )
                            intent.putExtra("call", call)
                            intent.putExtra(
                                "incomingProfile",
                                Profile("", "", "", "", "", "", true)
                            )
                            activity.startActivity(intent)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun updateCall(id: String) {
        FirebaseDatabase
            .getInstance()
            .getReference("calls")
            .child(id)
            .child("callState")
            .setValue("COMPLETED")
    }

    fun sendCall(call: Call) {
        FirebaseDatabase
            .getInstance()
            .getReference("calls")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    FirebaseDatabase
                        .getInstance()
                        .getReference("calls")
                        .child(call.id)
                        .setValue(call)
                }

                override fun onCancelled(error: DatabaseError) {}

            })
    }
}