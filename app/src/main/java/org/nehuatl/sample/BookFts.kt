package org.nehuatl.sample

import androidx.room.Entity
import androidx.room.Fts5
import androidx.room.PrimaryKey

@Fts5
@Entity(tableName = "books_fts")
data class BookFts(
    @PrimaryKey val rowid: Long,
    val title: String,
    val content: String
)
