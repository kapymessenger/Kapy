package org.kapyteam.messenger.util

import android.content.Context
import android.provider.ContactsContract

class Contacts {
    companion object {
        fun getContacts(context: Context): List<String> {
            val cursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                null
            )
            return listOf(
                ContactsContract.CommonDataKinds.Phone.NUMBER
            )
        }
    }
}