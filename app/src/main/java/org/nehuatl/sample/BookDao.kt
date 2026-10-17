package org.nehuatl.sample

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BookDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFts(fts: BookFts)

    @Query("SELECT * FROM books")
    suspend fun getAllBooks(): List<BookEntity>

    @Query("SELECT COUNT(*) FROM books")
    suspend fun getBookCount(): Int

    @Query("""
        SELECT books.id, books.title, books.content
        FROM books
        JOIN books_fts ON books.id = books_fts.rowid
        WHERE books_fts MATCH :query
        ORDER BY bm25(books_fts)
        LIMIT 50
    """)
    suspend fun searchBooks(query: String): List<BookEntity>
}
