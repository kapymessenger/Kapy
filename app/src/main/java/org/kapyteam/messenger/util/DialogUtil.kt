/*
 * This file is a part of Kapy Messenger project.
 * Original link: https://github.com/kapymessenger/Kapy
 */

package org.kapyteam.messenger.util

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.OutputStreamWriter

object DialogUtil {
    fun saveMessagesMetadata(json: JsonObject, context: Context) {
        val fos = context.openFileOutput("dialog.json", Context.MODE_PRIVATE)
        val writer = OutputStreamWriter(fos)
        writer.write(Gson().toJson(json))
        writer.close()
    }

    fun loadMessagesMetadata(context: Context): JsonObject {
        return try {
            val fin = context.openFileInput("dialog.json")
            val bytes = fin.readBytes()
            JsonParser.parseString(String(bytes)).asJsonObject
        } catch (e: Exception) {
            JsonObject()
        }
    }
}