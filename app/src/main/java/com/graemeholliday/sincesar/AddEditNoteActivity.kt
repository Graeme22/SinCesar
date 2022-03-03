package com.graemeholliday.sincesar

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.appbar.MaterialToolbar
import java.text.SimpleDateFormat
import java.util.*

class AddEditNoteActivity : AppCompatActivity() {
    //on below line we are creating variables for our UI components.
    lateinit var noteTitleEdt: EditText
    lateinit var noteEdt: EditText
    lateinit var saveBtn: Button
    lateinit var seekBar: SeekBar
    //radio buttons/group
    lateinit var rbGroup: RadioGroup
    //date and spinner that are hidden/visible
    lateinit var etDate: EditText
    lateinit var etNumber: EditText
    lateinit var bar: MaterialToolbar

    //on below line we are creating variable for viewmodal and and integer for our note id.
    lateinit var viewModel: PrayerViewModel
    var noteID = -1;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_note)

        //on below line we are initlaiing our view modal.
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(PrayerViewModel::class.java)
        //on below line we are initializing all our variables.
        noteTitleEdt = findViewById(R.id.idEdtNoteName)
        noteEdt = findViewById(R.id.idEdtNoteDesc)
        saveBtn = findViewById(R.id.idBtn)
        seekBar = findViewById(R.id.seekBar)
        rbGroup = findViewById(R.id.rbGroup)
        etDate = findViewById(R.id.etDate)
        etNumber = findViewById(R.id.etNumber)
        bar = findViewById(R.id.toolbar)

        //on below line we are getting data passsed via an intent.
        val noteEdit = intent.getStringExtra("noteEdit")
        //the note already exists and we're updating it
        if (noteEdit.equals("Edit")) {
            //on below line we are setting data to edit text.
            val noteTitle = intent.getStringExtra("noteTitle")
            val noteDescription = intent.getStringExtra("noteDescription")
            val noteFrequency = intent.getIntExtra("noteFrequency", 1)
            val noteType = intent.getIntExtra("noteType", R.id.rbPermanent)
            val noteExpiration = intent.getStringExtra("noteExpiration")
            val noteNotifications = intent.getIntExtra("noteNotifications", 5)
            noteID = intent.getIntExtra("noteId", -1)
            saveBtn.setText(R.string.update_prayer)
            noteTitleEdt.setText(noteTitle)
            noteEdt.setText(noteDescription)
            seekBar.setProgress(noteFrequency)
            if(noteType == R.id.rbExpires) {
                etDate.setText(noteExpiration)
                etDate.setVisibility(View.VISIBLE)
                rbGroup.check(R.id.rbExpires)
            } else if(noteType == R.id.rbFixed) {
                etNumber.setText(noteNotifications.toString())
                etNumber.setVisibility(View.VISIBLE)
                rbGroup.check(R.id.rbFixed)
            } else
                rbGroup.check(R.id.rbPermanent)
        }
        // this is a new note
        else {
            saveBtn.setText(R.string.save_prayer)
            rbGroup.check(R.id.rbPermanent)
        }

        //on below line we are adding click listner to our save button.
        saveBtn.setOnClickListener {
            //on below line we are getting title and desc from edit text.
            val noteTitle = noteTitleEdt.text.toString()
            val noteDescription = noteEdt.text.toString()
            val noteFrequency = seekBar.getProgress()
            val noteType = rbGroup.checkedRadioButtonId
            val noteExpiration = etDate.text.toString()
            val noteNotifications = etNumber.text.toString().toInt()
            //on below line we are checking the type and then saving or updating the data.
            if (noteTitle.isNotEmpty() && noteDescription.isNotEmpty()) {
                if (noteEdit.equals("Edit")) {
                    val updatedNote = Prayer(noteTitle, noteDescription, noteFrequency, noteType, noteExpiration, noteNotifications)
                    updatedNote.id = noteID
                    viewModel.updateNote(updatedNote)
                    Toast.makeText(this, R.string.toast_updated, Toast.LENGTH_SHORT).show()
                } else {
                    //if the string is not empty we are calling a add note method to add data to our room database.
                    viewModel.addNote(Prayer(noteTitle, noteDescription, noteFrequency, noteType, noteExpiration, noteNotifications))
                    Toast.makeText(this, R.string.toast_added, Toast.LENGTH_SHORT).show()
                }
            }
            //opening the new activity on below line
            startActivity(Intent(applicationContext, MainActivity::class.java))
            this.finish()
        }

        //handle radiogroup
        rbGroup.setOnCheckedChangeListener {
            _, checkedId ->
            if(checkedId == R.id.rbPermanent) {
                etDate.setVisibility(View.INVISIBLE)
                etNumber.setVisibility(View.INVISIBLE)
            } else if(checkedId == R.id.rbExpires) {
                etDate.setVisibility(View.VISIBLE)
                etNumber.setVisibility(View.INVISIBLE)
            } else {
                etDate.setVisibility(View.INVISIBLE)
                etNumber.setVisibility(View.VISIBLE)
            }
        }

        //handle back button
        bar.setOnMenuItemClickListener { _ ->
            startActivity(Intent(applicationContext, MainActivity::class.java))
            this.finish()
            true
        }

        //handle date
        etDate.transformIntoDatePicker(this, "dd/MM/yyyy", Date())
    }

    override fun onBackPressed() {
        super.onBackPressed()
        //opening the new activity on below line
        startActivity(Intent(applicationContext, MainActivity::class.java))
        this.finish()
    }

    fun EditText.transformIntoDatePicker(context: Context, format: String, minDate: Date? = null) {
        isFocusableInTouchMode = false
        isClickable = true
        isFocusable = false

        val myCalendar = Calendar.getInstance()
        val datePickerOnDataSetListener =
            OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                myCalendar.set(Calendar.YEAR, year)
                myCalendar.set(Calendar.MONTH, monthOfYear)
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val sdf = SimpleDateFormat(format, Locale.UK)
                setText(sdf.format(myCalendar.time))
            }

        setOnClickListener {
            DatePickerDialog(
                context, datePickerOnDataSetListener, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            ).run {
                minDate?.time?.also { datePicker.minDate = it }
                show()
            }
        }
    }
}