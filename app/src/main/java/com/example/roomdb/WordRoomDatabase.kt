package com.example.roomdb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

//Annotates class to be a Room Database
@Database(entities = arrayOf(Word::class), version = 1, exportSchema = false)

public abstract class WordRoomDatabase : RoomDatabase(){

    abstract fun wordDao(): WordDao

    private class WordDataBaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback(){

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                scope.launch {
                    populateDatabase(database.wordDao())
                }
            }
        }

        suspend fun populateDatabase(wordDao: WordDao){
            //Delete all content here
            wordDao.deleteAll()

            //Add sample words
            var word = Word("Jakarta")
            wordDao.insert(word)
            word = Word("Capital City!")
            wordDao.insert(word)
        }
    }

    companion object{
        @Volatile
        private var INSTANCE: WordRoomDatabase? = null

        fun getDatabase(
            context: Context, scope: CoroutineScope
        ): WordRoomDatabase{
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WordRoomDatabase::class.java,
                    "word_database"
                )   .addCallback(WordDataBaseCallback(scope))
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }

}