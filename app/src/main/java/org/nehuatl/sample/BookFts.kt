package org.nehuatl.sample

package org.nehuatl.sample

import androidx.room.Entity
import androidx.room.Fts5

@Fts5
@Entity(tableName = "books_fts")
data class BookFts(
    val rowid: Long,
    val title: String,
    val content: String
)
