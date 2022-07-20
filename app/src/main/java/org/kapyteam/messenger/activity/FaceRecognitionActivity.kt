/*
 * This file is a part of Kapy Messenger project.
 * Original link: https://github.com/kapymessenger/Kapy
 */

package org.kapyteam.messenger.activity

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.text.Html
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import org.kapyteam.messenger.databinding.ActivityFaceRecognitionBinding
import org.kapyteam.messenger.ml.ModelUnquant
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

class FaceRecognitionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFaceRecognitionBinding
    private lateinit var button: Button
    private lateinit var resText: TextView

    private val imageSize = 224

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFaceRecognitionBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        button = binding.photoButton
        resText = binding.resultText

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
            outputGenerator(bitmap)
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
                val `val` = intValues[pixel++]
                byteBuffer.putFloat((`val` shr 16 and 0xFF) * (1f / 255f))
                byteBuffer.putFloat((`val` shr 8 and 0xFF) * (1f / 255f))
                byteBuffer.putFloat((`val` and 0xFF) * (1f / 255f))
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

        println(confidences[2])
        val classes = arrayOf("😐", "😀", "😥", "😲", "😨", "🤢", "😡", "😏")
        //resText!!.text = classes[maxPos]

        var s = ""
        for (i in classes.indices) {
            s += String.format("%s: %.1f%%\n", classes[i], confidences[i] * 100)
        }
        resText.text = s
// Releases model resources if no longer used.
        model.close()

    }
}