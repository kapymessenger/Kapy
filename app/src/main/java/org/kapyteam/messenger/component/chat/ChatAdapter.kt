/*
 * This file is a part of Kapy Messenger project.
 * Original link: https://github.com/kapymessenger/Kapy
 */

package org.kapyteam.messenger.component.chat

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import org.kapyteam.messenger.model.Message

class ChatAdapter : RecyclerView.Adapter<ChatAdapter.MyViewHolder>() {
    private val messages = mutableListOf<Message>()
    private lateinit var context: Context

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false))
    }

    class MyViewHolder(@NonNull val view: View) : RecyclerView.ViewHolder(view)

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

    }
}

