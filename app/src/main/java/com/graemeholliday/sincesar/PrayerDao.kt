package com.graemeholliday.sincesar

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PrayerDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(prayer: Prayer)

    @Update
    suspend fun update(prayer: Prayer)

    @Delete
    suspend fun delete(prayer: Prayer)

    @Query("Select * from notesTable order by id ASC")
    fun getAllNotes(): Flow<List<Prayer>>
}