package org.kapyteam.messenger

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.kapyteam.messenger.databinding.ActivityFaceRecognitionBinding

class FaceRecognition : AppCompatActivity() {

    private lateinit var binding: ActivityFaceRecognitionBinding
    private lateinit var button: Button
    private lateinit var resText: TextView
    private val GALLERY_REQUEST_CODE = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFaceRecognitionBinding.inflate(layoutInflater)
        val view = binding.root

        button = binding.photoButton
        resText = binding.resultText

        //Я еще не дописал не трогайте это и одноименнуб активность, пж.
    }
}