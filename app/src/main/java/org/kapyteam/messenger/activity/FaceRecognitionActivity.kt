/*
 * This file is a part of Kapy Messenger project.
 * Original link: https://github.com/kapymessenger/Kapy
 */

package org.kapyteam.messenger.activity

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import org.kapyteam.messenger.R
import org.kapyteam.messenger.databinding.ActivityFaceRecognitionBinding
import org.kapyteam.messenger.ml.ModelUnquant
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.min


class FaceRecognitionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFaceRecognitionBinding
    private lateinit var button: Button
    private lateinit var resText: TextView
    private lateinit var imageView: ImageView

    private val imageSize = 224

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFaceRecognitionBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        button = binding.photoButton
        resText = binding.resultText
        imageView = binding.imageView2



        button.setOnClickListener{
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED)
            {
                takePicturePreview.launch(null)
            }
            else{
                requestedPermission.launch(android.Manifest.permission.CAMERA)
            }
        }

    }
    private val requestedPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()){granted->
        if(granted){
            takePicturePreview.launch(null)
        }
    }

    private val takePicturePreview = registerForActivityResult(ActivityResultContracts.TakePicturePreview()){bitmap ->
        if(bitmap != null){
            val dimension = min(bitmap.width, bitmap.height)
            var bitmap_fin = ThumbnailUtils.extractThumbnail(bitmap, dimension, dimension)

            bitmap_fin = Bitmap.createScaledBitmap(bitmap_fin, imageSize, imageSize, false)
            imageView.setImageBitmap(bitmap_fin)
            outputGenerator(bitmap_fin)
        }
    }

    private fun outputGenerator(bitmap: Bitmap){
        val model = ModelUnquant.newInstance(this)

// Creates inputs for reference.
        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)
        val byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3)
        byteBuffer.order(ByteOrder.nativeOrder())
        val intValues = IntArray(imageSize * imageSize)
        bitmap!!.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
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

        val classes = arrayOf("😀", "😡", "👆", "✊", "🤟")

        showDialog(classes[maxPos], confidences[maxPos] * 100)

        var s = ""
        for (i in classes.indices) {
            if((confidences[i]*100).toInt() != 0)
            s += String.format("%s: %.1f%%\n", classes[i], confidences[i] * 100)
        }
        resText.text = s

        model.close()

    }
    private fun showDialog(title: String, percent: Float) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_window)
        val resText = dialog.findViewById(R.id.resultText) as TextView
        val perText = dialog.findViewById(R.id.percent) as TextView
        resText.text = title
        perText.text = String.format("AI recognized it as %.1f%%\n", percent)
        val okButton = dialog.findViewById(R.id.okButton) as Button
        val copyButton = dialog.findViewById(R.id.copyButton) as TextView

        copyButton.setOnClickListener {
            val clipboard: ClipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("", title)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, title+" was copied!", Toast.LENGTH_SHORT).show()
        }

        okButton.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()

    }
}