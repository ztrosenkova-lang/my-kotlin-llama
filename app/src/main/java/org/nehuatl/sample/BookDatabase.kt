package org.nehuatl.sample

import android.content.Context
import androidx.room.BuiltInTypeConverters
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@TypeConverters(builtInTypeConverters = BuiltInTypeConverters())
@Database(
    entities = [BookEntity::class, BookFts::class],
    version = 1,
    exportSchema = false
)
abstract class BookDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao

    companion object {
        @Volatile
        private var INSTANCE: BookDatabase? = null

        fun get(context: Context): BookDatabase {
            return INSTANCE ?: synchronized(this) {
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    BookDatabase::class.java,
                    "books.db"
                ).build()
                INSTANCE = db
                db
            }
        }
    }
}
