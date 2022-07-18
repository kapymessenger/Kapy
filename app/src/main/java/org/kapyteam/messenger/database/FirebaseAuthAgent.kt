/*
 * This file is a part of Kapy Messenger project.
 * Original link: https://github.com/kapymessenger/Kapy
 */

package org.kapyteam.messenger.database

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class FirebaseAuthAgent {
    companion object {
        @JvmStatic private var auth: FirebaseAuth = FirebaseAuth.getInstance()

        fun getInstance(): FirebaseAuth = auth

        fun getCurrentUser(): FirebaseUser? = auth.currentUser

        fun phoneAuth(): Boolean = TODO("Not implemented")
    }
}