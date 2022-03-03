package com.graemeholliday.sincesar

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notesTable")
class Prayer (
    @ColumnInfo(name = "title")val title: String,
    @ColumnInfo(name = "description")val description: String,
    @ColumnInfo(name = "frequency")val frequency: Int,
    @ColumnInfo(name = "type")val type: Int,
    @ColumnInfo(name = "expiration")val expiration: String,
    @ColumnInfo(name = "notifications")val notifications: Int
) {
    @PrimaryKey(autoGenerate = true)var id = 0
}