package com.graemeholliday.sincesar

import kotlinx.coroutines.flow.Flow

class PrayerRepository(private val prayerDao: PrayerDao) {
    val allNotes: Flow<List<Prayer>> = prayerDao.getAllNotes()

    suspend fun insert(prayer: Prayer) {
        prayerDao.insert(prayer)
    }

    suspend fun delete(prayer: Prayer){
        prayerDao.delete(prayer)
    }

    suspend fun update(prayer: Prayer){
        prayerDao.update(prayer)
    }
}