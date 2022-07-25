/*
 * This file is a part of Kapy Messenger project.
 * Original link: https://github.com/kapymessenger/Kapy
 */

package org.kapyteam.messenger.model

import java.io.Serializable

data class Call(
    val sender: String,
    val receiver: String,
    val id: String,
    val callState: String,
    val callType: String
) : Serializable