/*
 * This file is a part of Kapy Messenger project.
 * Original link: https://github.com/kapymessenger/Kapy
 */

package org.kapyteam.messenger.component.chat

import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import org.kapyteam.messenger.R
import org.kapyteam.messenger.model.Message

class ChatAdapter(
    private var messages: MutableList<Message>,
    private val self: String
) : RecyclerView.Adapter<ChatAdapter.MyViewHolder>() {

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.chat_layout_adapter, parent, false)
        )
    }

    fun update(messages: MutableList<Message>) {
        this.messages = messages
    }

    class MyViewHolder(@NonNull val view: View) : RecyclerView.ViewHolder(view) {
        var anotherMessage: TextView = view.findViewById(R.id.another_message)
        var selfMessage: TextView = view.findViewById(R.id.self_message)
        var anotherMetadata: TextView = view.findViewById(R.id.another_msg_metadata)
        val selfMetadata: TextView = view.findViewById(R.id.self_msg_metadata)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val message = messages[position]
        if (message.sender == self) {
            holder.selfMessage.visibility = View.VISIBLE
            holder.anotherMessage.visibility = View.GONE
            holder.selfMessage.text = message.content
            holder.selfMetadata.text = message.createTime
            holder.anotherMetadata.text = ""
        } else {
            holder.selfMessage.visibility = View.GONE
            holder.anotherMessage.visibility = View.VISIBLE
            holder.anotherMessage.text = message.content
            holder.anotherMetadata.text = message.createTime
            holder.selfMetadata.text = ""
        }
    }
}

