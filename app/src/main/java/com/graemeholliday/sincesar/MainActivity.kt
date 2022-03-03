package com.graemeholliday.sincesar

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton

val CHANNEL_ID: String = "50288"

class MainActivity : AppCompatActivity(), NoteClickInterface, NoteClickDeleteInterface {
    //on below line we are creating a variable for our recycler view, exit text, button and viewmodal.
    lateinit var viewModel: PrayerViewModel
    lateinit var notesRV: RecyclerView
    lateinit var addFAB: FloatingActionButton
    lateinit var bar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //on below line we are initializing all our variables.
        notesRV = findViewById(R.id.notesRV)
        addFAB = findViewById(R.id.idFAB)
        bar = findViewById(R.id.toolbar)

        createNotificationChannel()
        createAlarm()

        //on below line we are setting layout manager to our recycler view.
        notesRV.layoutManager = LinearLayoutManager(this)
        //on below line we are initializing our adapter class.
        val prayerRVAdapter = PrayerRVAdapter(this, this)
        //on below line we are setting adapter to our recycler view.
        notesRV.adapter = prayerRVAdapter
        //on below line we are initializing our view modal.
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(PrayerViewModel::class.java)
        //on below line we are calling all notes method from our view model class to observe the changes in list.
        viewModel.allNotes.observe(this, Observer { list ->
            list?.let {
                //on below line we are updating our list.
                prayerRVAdapter.updateList(it)
            }
        })

        addFAB.setOnClickListener {
            //adding a click listner for fab button and opening a new intent to add a new note.
            val intent = Intent(this@MainActivity, AddEditNoteActivity::class.java)
            startActivity(intent)
            this.finish()
        }
        bar.setOnMenuItemClickListener { _ ->
            startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
            this.finish()
            true
        }
    }

    override fun onNoteClick(prayer: Prayer) {
        //opening a new intent and passing a data to it.
        val intent = Intent(this@MainActivity, AddEditNoteActivity::class.java)
        intent.putExtra("noteEdit", "Edit")
        intent.putExtra("noteTitle", prayer.title)
        intent.putExtra("noteDescription", prayer.description)
        intent.putExtra("noteFrequency", prayer.frequency)
        intent.putExtra("noteType", prayer.type)
        intent.putExtra("noteExpiration", prayer.expiration)
        intent.putExtra("noteNotifications", prayer.notifications)
        intent.putExtra("noteId", prayer.id)
        startActivity(intent)
        this.finish()
    }

    override fun onDeleteIconClick(prayer: Prayer) {
        //in on note click method we are calling delete method from our viw modal to delete our not.
        viewModel.deleteNote(prayer)
        //displaying a toast message
        Toast.makeText(this, R.string.toast_deleted, Toast.LENGTH_SHORT).show()
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createAlarm() {
        val manager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = Intent(this, AlarmReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(this, 0, intent, 0)
        }
        manager.setRepeating(AlarmManager.RTC, SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_FIFTEEN_MINUTES, AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent)
    }
}