package org.kapyteam.messenger.ai

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import org.kapyteam.messenger.R
import org.kapyteam.messenger.activity.ChatActivity
import org.kapyteam.messenger.ml.ModelUnquant
import org.kapyteam.messenger.model.Message
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

object Recognizer {
    fun takePicture(activity: ChatActivity) {
        if (ContextCompat.checkSelfPermission(
                activity.applicationContext,
                android.Manifest.permission.CAMERA
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            activity.takePicturePreview.launch(null)
        } else {
            activity.requestedPermission.launch(android.Manifest.permission.CAMERA)
        }
    }

    fun outputGenerator(bitmap: Bitmap, activity: ChatActivity, imageSize: Int = 224) {
        val model = ModelUnquant.newInstance(activity)

// Creates inputs for reference.
        val inputFeature0 =
            TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)
        val byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3)
        byteBuffer.order(ByteOrder.nativeOrder())
        val intValues = IntArray(imageSize * imageSize)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        var pixel = 0
        for (i in 0 until imageSize) {
            for (j in 0 until imageSize) {
                val value = intValues[pixel++]
                byteBuffer.putFloat((value shr 16 and 0xFF) * (1f / 255f))
                byteBuffer.putFloat((value shr 8 and 0xFF) * (1f / 255f))
                byteBuffer.putFloat((value and 0xFF) * (1f / 255f))
            }
        }

        inputFeature0.loadBuffer(byteBuffer)

        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer


        val confidences: FloatArray = outputFeature0.floatArray
        var maxPos = 0
        var maxConfidence = 0f
        for (i in confidences.indices) {
            if (confidences[i] > maxConfidence) {
                maxConfidence = confidences[i]
                maxPos = i
            }
        }

        val classes = arrayOf("😀", "😡", "👆", "✊", "🤟", "🐱", "🐔", "🐴")

        showDialog(classes[maxPos], confidences[maxPos] * 100, bitmap, activity)

        var s = ""
        for (i in classes.indices) {
            if ((confidences[i] * 100).toInt() != 0)
                s += String.format("%s: %.1f%%\n", classes[i], confidences[i] * 100)
        }

        model.close()

    }

    private fun showDialog(title: String, percent: Float, bitmap: Bitmap, activity: ChatActivity) {
        val dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_window)
        val resText = dialog.findViewById(R.id.resultText) as TextView
        val perText = dialog.findViewById(R.id.percent) as TextView
        resText.text = title
        perText.text = String.format("AI is %.1f%% sure", percent)
        val imageView = dialog.findViewById(R.id.image) as ImageView
        imageView.setImageBitmap(bitmap)
        val okButton = dialog.findViewById(R.id.okButton) as Button
        val copyButton = dialog.findViewById(R.id.copyButton) as Button
        val sendButton = dialog.findViewById(R.id.sendButton) as Button

        sendButton.setOnClickListener {
            activity.sendMessage(Message(
                sender = activity.phone,
                receiver = activity.member.phone,
                createTime = "Generated via AI",
                content = title
            ))
            Toast.makeText(
                activity.applicationContext,
                "Message was sent!",
                Toast.LENGTH_LONG
            )
                .show()
            dialog.dismiss()
        }

        copyButton.setOnClickListener {
            val clipboard: ClipboardManager =
                activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("", title)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(
                activity.applicationContext,
                "$title was copied!",
                Toast.LENGTH_SHORT
            )
                .show()
        }

        okButton.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }
}