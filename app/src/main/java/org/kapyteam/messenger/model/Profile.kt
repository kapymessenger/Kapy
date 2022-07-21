/*
 * This file is a part of Kapy Messenger project.
 * Original link: https://github.com/kapymessenger/Kapy
 */

package org.kapyteam.messenger.model

data class Profile(
    val firstname: String,
    val lastname: String,
    val phone: String,
    val nickname: String,
    val photo: String = "",
    val lastSeen: String = "",
    val online: Boolean = false
) {
    companion object {
        fun parse(meta: Map<*, *>): Profile {
            return Profile(
                meta["firstname"] as String,
                meta["lastname"] as String,
                meta["phone"] as String,
                meta["nickname"] as String,
                meta["photo"] as String,
                meta["lastSeen"] as String,
                meta["online"] as Boolean
            )
        }
    }
}
