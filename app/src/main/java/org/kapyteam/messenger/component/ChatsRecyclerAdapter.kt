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
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import org.kapyteam.messenger.R
import org.kapyteam.messenger.database.FirebaseAuthAgent
import org.kapyteam.messenger.model.Profile

class ChatsRecyclerAdapter(
    private var chats: List<Profile>,
    private val activity: Activity,
    private val intent: Intent,
    private val phone: String
) : RecyclerView.Adapter<ChatsRecyclerAdapter.MyViewHolder>() {
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val contactImage: CircleImageView = itemView.findViewById(R.id.contact_image)
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
                                }

                                override fun onCancelled(error: DatabaseError) {}
                            })
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
            }


        holder.contactName.text = chats[position].nickname
        holder.contactMessageCount.text = "1"

        FirebaseAuthAgent
            .getReference()
            .child("users")
            .child(chats[position].phone)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.child("photo").value.toString().let { pic ->
                        if (pic != "") Picasso.get().load(pic).into(holder.contactImage)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun getChild(snapshot: DataSnapshot, position: Int): String {
        return if (snapshot.hasChild("${phone}&${chats[position].phone}")) {
            "${phone}&${chats[position].phone}"
        } else if (snapshot.hasChild("${chats[position].phone}&${phone}")) {
            "${chats[position].phone}&${phone}"
        } else {
            "null"
        }
    }

    fun update(profiles: List<Profile>) {
        this.chats = profiles
    }

    private fun applyMetadata(snapshot: DataSnapshot, holder: MyViewHolder) {
        if (snapshot.hasChildren()) {
            snapshot.children.first().let {
                holder.contactLastMessage.text = it
                    .child("content")
                    .value.toString()

                holder.contactLastMessageTime.text = it
                    .child("createTime")
                    .value.toString()
            }
        }
    }

    override fun getItemCount(): Int {
        return chats.size
    }
}
