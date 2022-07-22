package org.kapyteam.messenger.util

import android.app.Activity
import android.provider.ContactsContract

object Contacts {
    fun getContacts(activity: Activity): List<String> {
        val cursor = activity.applicationContext.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )
        activity.startManagingCursor(cursor)
        return listOf(
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )
    }
}