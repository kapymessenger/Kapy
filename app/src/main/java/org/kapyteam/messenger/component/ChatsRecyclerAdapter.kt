/*
 * This file is a part of Kapy Messenger project.
 * Original link: https://github.com/kapymessenger/Kapy
 */

package org.kapyteam.messenger.component

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import org.kapyteam.messenger.R
import org.kapyteam.messenger.database.FirebaseAuthAgent
import org.kapyteam.messenger.model.Profile

class ChatsRecyclerAdapter(
    private val chats: List<Profile>,
    private val activity: Activity,
    private val intent: Intent,
    private val phone: String
) : RecyclerView.Adapter<ChatsRecyclerAdapter.MyViewHolder>() {
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val contactImage: ImageView = itemView.findViewById(R.id.contact_image)
        val contactName: TextView = itemView.findViewById(R.id.contact_name)
        val contactLastMessage: TextView = itemView.findViewById(R.id.contact_last_message)
        val contactLastMessageTime: TextView = itemView.findViewById(R.id.contact_last_message_time)
        val contactMessageCount: TextView = itemView.findViewById(R.id.contact_message_count)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.layout_dialog, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(
        holder: MyViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {

        holder.itemView.setOnClickListener {
            FirebaseAuthAgent.getReference()
            intent.putExtra("member", chats[position])
            intent.putExtra("phone", phone)
            activity.startActivity(intent)
        }

        FirebaseAuthAgent
            .getReference()
            .child("chats")
            .ref.let {
                it.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        it
                            .child(getChild(snapshot, position))
                            .child("messages")
                            .orderByKey()
                            .limitToLast(1)
                            .addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(snapshot_: DataSnapshot) {
                                    applyMetadata(snapshot_, holder)
//                                if (shouldUpdate) {
//                                    val json =
//                                        DialogUtil.loadMessagesMetadata(activity.applicationContext).asJsonObject
//
//                                    snapshot_.children.first().child("content").value.toString()
//                                        .let {
//                                            if (json.has(child)) {
//                                                updateJson(json, child, it)
//                                            } else {
//                                                json.add(child, JsonParser.parseString(it))
//                                                DialogUtil.saveMessagesMetadata(json, activity.applicationContext)
//                                            }
//                                        }
//                                }
                                }

                                override fun onCancelled(error: DatabaseError) {}
                            })
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
            }


        holder.contactName.text = chats[position].nickname
        holder.contactMessageCount.text = "1"
    }

//    private fun updateJson(json: JsonObject, target: String, msg: String) {
//        val new = JsonObject()
//
//        json.keySet().iterator().let {
//            while (it.hasNext()) {
//                val key = it.next()
//                if (key == target) {
//                    new.add(key, JsonParser.parseString(msg))
//                } else {
//                    new.add(key, json.get(key))
//                }
//            }
//        }
//
//        DialogUtil.saveMessagesMetadata(new, activity.applicationContext)
//    }

    private fun getChild(snapshot: DataSnapshot, position: Int): String {
        return if (snapshot.hasChild("${phone}&${chats[position].phone}")) {
            "${phone}&${chats[position].phone}"
        } else if (snapshot.hasChild("${chats[position].phone}&${phone}")) {
            "${chats[position].phone}&${phone}"
        } else {
            "null"
        }
    }

    private fun applyMetadata(snapshot: DataSnapshot, holder: MyViewHolder) {
        snapshot.children.first().let {
            holder.contactLastMessage.text = it
                .child("content")
                .value.toString()

            holder.contactLastMessageTime.text = it
                .child("createTime")
                .value.toString()
        }
    }

    override fun getItemCount(): Int {
        return chats.size
    }
}
