package org.kapyteam.messenger.util

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.OutputStreamWriter

object FileUtil {
    private const val FILE_NAME = "metadata.json"

    fun saveData(metadata: String, context: Context) {
        val fos = context.openFileOutput(FILE_NAME, MODE_PRIVATE)
        val writer = OutputStreamWriter(fos)
        writer.write(metadata)
        writer.close()
    }

    fun loadData(context: Context): JsonObject {
        return try {
            val fin = context.openFileInput(FILE_NAME)
            val bytes = fin.readBytes()
            JsonParser.parseString(String(bytes)).asJsonObject
        } catch (e: Exception) {
            JsonObject()
        }
    }
}