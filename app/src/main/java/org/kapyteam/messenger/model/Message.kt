/*
 * This file is a part of Kapy Messenger project.
 * Original link: https://github.com/kapymessenger/Kapy
 */

package org.kapyteam.messenger.model

data class Message(
    val sender: String,
    val receiver: String,
    val createTime: String,
    val dialog: String,
    val read: Boolean
)
