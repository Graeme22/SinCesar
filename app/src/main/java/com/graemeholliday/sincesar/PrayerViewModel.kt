package com.graemeholliday.sincesar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PrayerViewModel(application: Application) : AndroidViewModel(application) {
    val allNotes: LiveData<List<Prayer>>
    val repository: PrayerRepository

    init {
        val dao = PrayerDatabase.getDatabase(application).getNotesDao()
        repository = PrayerRepository(dao)
        allNotes = repository.allNotes.asLiveData()
    }

    fun deleteNote(prayer: Prayer) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(prayer)
    }

    fun updateNote(prayer: Prayer) = viewModelScope.launch(Dispatchers.IO) {
        repository.update(prayer)
    }

    fun addNote(prayer: Prayer) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(prayer)
    }
}