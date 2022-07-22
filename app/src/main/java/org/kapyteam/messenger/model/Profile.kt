/*
 * This file is a part of Kapy Messenger project.
 * Original link: https://github.com/kapymessenger/Kapy
 */

package org.kapyteam.messenger.model

import java.io.Serializable

data class Profile(
    var firstname: String,
    var lastname: String,
    var phone: String,
    var nickname: String,
    var photo: String = "",
    var lastSeen: String = "",
    var online: Boolean = false
) : Serializable {
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
