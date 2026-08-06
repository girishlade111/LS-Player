package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.FavoriteVideoEntity
import com.example.data.model.PlaylistEntity
import com.example.data.model.PlaylistItemEntity
import com.example.data.model.VideoEntity
import com.example.data.model.VideoProgressEntity

@Database(
    entities = [
        VideoEntity::class,
        VideoProgressEntity::class,
        FavoriteVideoEntity::class,
        PlaylistEntity::class,
        PlaylistItemEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class LsPlayerDatabase : RoomDatabase() {

    abstract fun videoDao(): VideoDao

    companion object {
        @Volatile
        private var INSTANCE: LsPlayerDatabase? = null

        fun getDatabase(context: Context): LsPlayerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LsPlayerDatabase::class.java,
                    "ls_player_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
