package org.kapyteam.messenger.util

import com.google.firebase.database.DataSnapshot

// @see https://stackoverflow.com/questions/30659569/wait-until-firebase-retrieves-data
interface IWait {
    fun onSuccess(snapshot: DataSnapshot)
    fun onFail()
    fun onStart()
}