package org.kapyteam.messenger.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.kapyteam.messenger.R


class TextEditor : AppCompatActivity(), View.OnClickListener {

    private lateinit var clearButton: Button
    private lateinit var copyButton: Button
    private lateinit var editText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_editor)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)




        clearButton = findViewById(R.id.clearButton)
        copyButton = findViewById(R.id.copyButton)
        editText = findViewById(R.id.editText)


        val sPref = getSharedPreferences("MyPref", MODE_PRIVATE)
        val savedText: String? = sPref.getString("Notes", "")

        editText.setText(savedText)

        clearButton.setOnClickListener(this)
        copyButton.setOnClickListener(this)

        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val sPref = getSharedPreferences("MyPref", MODE_PRIVATE)
                val ed = sPref.edit()
                ed.putString("Notes", editText.text.toString());
                ed.commit();
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()

            else -> super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.clearButton -> {
                val alertDialog = AlertDialog.Builder(this)

                alertDialog.apply {
                    setTitle("Are you sure?")
                    setPositiveButton("Yes") { _, _ ->
                        editText.text = null
                    }
                    setNegativeButton("No") { _, _ ->

                    }
                }.create().show()
            }
            R.id.copyButton -> {
                val clipboard: ClipboardManager =
                    this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("", editText.text)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(
                    this.applicationContext,
                    "Text was copied!",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }
    }
}
