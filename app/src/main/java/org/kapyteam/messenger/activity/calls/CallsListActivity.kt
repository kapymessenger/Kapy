package org.kapyteam.messenger.activity.calls

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import org.kapyteam.messenger.R

class CallsListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calls_list)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            else -> super.onOptionsItemSelected(item)
        }
        return true
    }
}