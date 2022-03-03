package com.graemeholliday.sincesar

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.appbar.MaterialToolbar

class SettingsActivity : AppCompatActivity() {
    lateinit var etStart: EditText
    lateinit var etEnd: EditText
    lateinit var bar: MaterialToolbar
    lateinit var saveBtn: Button
    lateinit var etNotifications: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        etStart = findViewById(R.id.etStart)
        etEnd = findViewById(R.id.etEnd)
        bar = findViewById(R.id.toolbar)
        saveBtn = findViewById(R.id.idBtn)
        etNotifications = findViewById(R.id.notifications)

        //shared preferences
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        val startTime: String? = sharedPrefs.getString(getString(R.string.start_key), "09:00")
        val endTime: String? = sharedPrefs.getString(getString(R.string.end_key), "21:00")
        val notifications: Int = sharedPrefs.getInt(getString(R.string.notify_key), 5)
        etStart.setText(startTime)
        etEnd.setText(endTime)
        etNotifications.setText(notifications.toString())

        etStart.transformIntoTimePicker(this, 9)
        etEnd.transformIntoTimePicker(this, 21)

        //handle back button
        bar.setOnMenuItemClickListener { _ ->
            startActivity(Intent(applicationContext, MainActivity::class.java))
            this.finish()
            true
        }

        //handle save button
        saveBtn.setOnClickListener {
            with (sharedPrefs.edit()) {
                putString(getString(R.string.start_key), etStart.text.toString())
                putString(getString(R.string.end_key), etEnd.text.toString())
                putInt(getString(R.string.notify_key), etNotifications.text.toString().toInt())
                apply()
            }
            Toast.makeText(this, R.string.settings_saved, Toast.LENGTH_SHORT).show()

            //opening the new activity on below line
            startActivity(Intent(applicationContext, MainActivity::class.java))
            this.finish()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        //opening the new activity on below line
        startActivity(Intent(applicationContext, MainActivity::class.java))
        this.finish()
    }

    fun EditText.transformIntoTimePicker(context: Context, defaultHour: Int) {
        isFocusableInTouchMode = false
        isClickable = true
        isFocusable = false

        val timePickerOnDataSetListener =
            TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                setText(hour.toString().padStart(2, '0') + ":" + minute.toString().padStart(2, '0'))
            }

        setOnClickListener {
            TimePickerDialog(
                context, timePickerOnDataSetListener, defaultHour, 0, true
            ).run {
                show()
            }
        }
    }
}