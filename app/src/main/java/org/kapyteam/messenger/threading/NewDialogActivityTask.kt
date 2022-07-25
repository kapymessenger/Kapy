/*
 * This file is a part of Kapy Messenger project.
 * Original link: https://github.com/kapymessenger/Kapy
 */

/*
 * This file is a part of Kapy Messenger project.
 * Original link: https://github.com/kapymessenger/Kapy
 */

package org.kapyteam.messenger.threading

import android.app.Activity
import android.content.Intent
import android.os.AsyncTask
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import org.kapyteam.messenger.activity.CreateDialogActivity
import org.kapyteam.messenger.database.FirebaseAuthAgent
import org.kapyteam.messenger.model.Profile
import org.kapyteam.messenger.util.SerializableObject

@Suppress("deprecation", "StaticFieldLeak")
class NewDialogActivityTask(
    private val activity: Activity,
    private val contacts: List<String>,
    private val phone: String
) : AsyncTask<Unit, Unit, Unit>() {

    val profiles = mutableListOf<Profile>()

    @Deprecated("Deprecated in Java")
    override fun doInBackground(vararg p0: Unit?) {
        for (contact in contacts) {
            println("Contact: $contact")
            FirebaseAuthAgent
                .getReference()
                .child("users")
                .child(contact)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        // nothing here
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.value != null) {
                            println("added profile")
                            profiles.add(Profile.parse(p0.value as Map<*, *>))
                        }
                    }
                })
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onPostExecute(result: Unit?) {
        // это капец костыль никогда так не делайте
        // (да, я ваще хз как дождаться окончания парсинга данных, раньше можно было юзать потоки и хандлеры)
        Thread {
            println("End")
            Thread.sleep(2000)
            println("End2")
            val intent = Intent(
                activity,
                CreateDialogActivity::class.java
            )
            intent.putExtra("phone", phone)
            intent.putExtra("profiles", SerializableObject(profiles))
            activity.startActivity(intent)
        }.start()
    }
}